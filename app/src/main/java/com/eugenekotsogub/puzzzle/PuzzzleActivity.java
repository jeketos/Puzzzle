package com.eugenekotsogub.puzzzle;

import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;

public class PuzzzleActivity extends AppCompatActivity {

    @BindView(R.id.main_container)
    ViewGroup mainContainer;
    List<CellView> cells;
    int columnCount = 4, rowCount = 4;
    private View.OnClickListener clickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (view instanceof CellView){
                CellView v = (CellView)view;
                if(canMove(v)) {
                    Coordinate free = GameView.INSTANCE.getFreeCoordinate();
                    int freeRow = free.row;
                    int freeColumn = free.column;
                    Coordinate current = v.getCurrentCoordinate();
                    move(v, free.row, free.column);
                    GameView.INSTANCE.setFreeCoordinate(current);
                    v.setCurrentCoordinate(freeRow, freeColumn);
                    if (isPazzleDone()) {
                        Toast.makeText(PuzzzleActivity.this, "Ай да молодец! Выиграл!", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }
    };

    private boolean canMove(CellView view) {
        int row  = view.getCurrentCoordinate().row;
        int column = view.getCurrentCoordinate().column;
        int freeRow = GameView.INSTANCE.getFreeCoordinate().row;
        int freeColumn = GameView.INSTANCE.getFreeCoordinate().column;
        column += 1;
        if(!(row == freeRow && column == freeColumn)){
            column -= 2;
            if(!(row == freeRow && column == freeColumn)){
                column += 1;
                row += 1;
                if(!(row == freeRow && column == freeColumn)){
                    row -= 2;
                    if(!(row == freeRow && column == freeColumn)){
                        return  false;
                    }
                }
            }
        }
        return true;
    }

    private boolean isPazzleDone() {
        for(CellView view : cells){
            if(!view.getCurrentCoordinate().equals(view.getAnchorCoordinate())){
                return false;
            }
        }
        return true;
    }

    private GridLayout layout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_puzzzle);
        ButterKnife.bind(this);
        cells = CellsFabric.create(this, columnCount, rowCount);
        GameView.INSTANCE.createEmptyCoordinate(columnCount - 1, rowCount - 1);
        draw(cells);
        Timber.d("onCreate: ");
    }

    private void draw(List<CellView> cells) {

        layout = new GridLayout(this);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(columnCount *220, rowCount *220);
        params.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
        layout.setLayoutParams(params);
        layout.setBackgroundColor(ContextCompat.getColor(this, R.color.colorAccent));
        layout.setColumnCount(columnCount);
        layout.setRowCount(rowCount);
        for (CellView view : cells) {
            view.setOnClickListener(clickListener);
            GridLayout.LayoutParams p= new GridLayout.LayoutParams();
            p.setMargins(10,10,10,10);
            int column = view.getAnchorCoordinate().column;
            int row = view.getAnchorCoordinate().row;
            p.columnSpec = GridLayout.spec(view.getCurrentCoordinate().column);
            p.rowSpec = GridLayout.spec(view.getCurrentCoordinate().row);
            view.setLayoutParams(p);
            view.getLayoutParams().height = 200;
            view.getLayoutParams().width = 200;
            view.setGravity(Gravity.CENTER);
            view.setTextSize(24);//            view.setText(Integer.toString(column*columnCount + row + 1));

            view.setBackgroundColor(ContextCompat.getColor(this,R.color.colorPrimary));
            layout.addView(view);
        }

        mainContainer.addView(layout);
    }

    public void move(View view, int row, int column){
        layout.removeView(view);
        ((GridLayout.LayoutParams)view.getLayoutParams()).columnSpec = GridLayout.spec(column);
        ((GridLayout.LayoutParams)view.getLayoutParams()).rowSpec = GridLayout.spec(row);
        layout.addView(view);
    }

}
