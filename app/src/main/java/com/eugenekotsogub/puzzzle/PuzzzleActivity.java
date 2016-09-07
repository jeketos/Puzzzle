package com.eugenekotsogub.puzzzle;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.eugenekotsogub.puzzzle.cell.CellView;
import com.eugenekotsogub.puzzzle.cell.CellsFabric;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.fabric.sdk.android.Fabric;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class PuzzzleActivity extends AppCompatActivity {

    @BindView(R.id.main_container)
    ViewGroup mainContainer;
    List<CellView> cells;
    int columnCount = 3, rowCount = 3;
    private GridLayout layout;
    private int size;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());
        setContentView(R.layout.activity_puzzzle);
        ButterKnife.bind(this);
        size = getBoardWidth();
        init();
    }

    private void init() {
        showProgressDialog();
        Observable.fromCallable(this::createGame)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(coordinates -> {
                    cells = CellsFabric.create(this, coordinates, columnCount, rowCount);
                    draw(cells);
                    hideProgressDialog();
                }, error -> {
                    hideProgressDialog();
                    error.printStackTrace();
                });
    }

    //return shuffled coordinates
    private List<Coordinate> createGame() {
        GameView.INSTANCE.createGameBoard(rowCount,columnCount);
        return GameView.INSTANCE.shuffle();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.puzzle_menu,menu);
        MenuItem item = menu.findItem(R.id.x3x3);
        item.setChecked(true);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.x3x3:
                columnCount = 3;
                rowCount = 3;
                break;
            case R.id.x4x4:
                columnCount = 4;
                rowCount = 4;
                break;
            case R.id.x5x5:
                columnCount = 5;
                rowCount = 5;
                break;
            case R.id.x6x6:
                columnCount = 6;
                rowCount = 6;
                break;
        }
        item.setChecked(true);
        init();
        return super.onOptionsItemSelected(item);
    }

    private OnSwipeTouchListener swipeListener = new OnSwipeTouchListener(getBaseContext()){

        @Override
        public void onSwipeRight(View view) {
            if(view instanceof CellView){
                int row = ((CellView) view).getCurrentCoordinate().row;
                int column = ((CellView) view).getCurrentCoordinate().column + 1;
                if(canMoveSwipe(row,column)){
                    doMove(((CellView)view));
                }
            }
        }

        @Override
        public void onSwipeLeft(View view) {
            if(view instanceof CellView){
                int row = ((CellView) view).getCurrentCoordinate().row;
                int column = ((CellView) view).getCurrentCoordinate().column - 1;
                if(canMoveSwipe(row,column)){
                    doMove(((CellView)view));
                }
            }
        }

        @Override
        public void onSwipeTop(View view) {
            if(view instanceof CellView){
                int row = ((CellView) view).getCurrentCoordinate().row - 1;
                int column = ((CellView) view).getCurrentCoordinate().column;
                if(canMoveSwipe(row,column)){
                    doMove(((CellView)view));
                }
            }
        }

        @Override
        public void onSwipeBottom(View view) {
            if(view instanceof CellView){
                int row = ((CellView) view).getCurrentCoordinate().row + 1;
                int column = ((CellView) view).getCurrentCoordinate().column;
                if(canMoveSwipe(row,column)){
                    doMove(((CellView)view));
                }
            }
        }

        @Override
        void onClick(View view) {
            if (view instanceof CellView){
                CellView v = (CellView)view;
                if(canMove(v)) {
                    doMove(v);
                }
            }
        }
    };

    private boolean canMoveSwipe(int row, int column) {
        int freeRow = GameView.INSTANCE.getFreeCoordinate().row;
        int freeColumn = GameView.INSTANCE.getFreeCoordinate().column;
        return row == freeRow && column == freeColumn;
    }

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

    private void doMove(CellView v) {
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

    private boolean isPazzleDone() {
        for(CellView view : cells){
            if(!view.getCurrentCoordinate().equals(view.getAnchorCoordinate())){
                return false;
            }
        }
        return true;
    }


    private void draw(List<CellView> cells) {
        layout = new GridLayout(this);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(size, size);
        params.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
        layout.setLayoutParams(params);
        layout.setBackgroundColor(ContextCompat.getColor(this, R.color.colorAccent));
        layout.setColumnCount(columnCount);
        layout.setRowCount(rowCount);
        for (CellView view : cells) {
            view.setOnTouchListener(swipeListener);
            GridLayout.LayoutParams p= new GridLayout.LayoutParams();
            p.setMargins(10,10,10,10);
            p.columnSpec = GridLayout.spec(view.getCurrentCoordinate().column);
            p.rowSpec = GridLayout.spec(view.getCurrentCoordinate().row);
            view.setLayoutParams(p);
            view.getLayoutParams().height = size/rowCount - 20;
            view.getLayoutParams().width = size/columnCount - 20;
            view.setGravity(Gravity.CENTER);
            view.setTextSize((size/rowCount - 20)/5);
//            view.setText(Integer.toString(column*columnCount + row + 1));
//            view.setBackgroundColor(ContextCompat.getColor(this,R.color.colorPrimary));
            layout.addView(view);
        }

        mainContainer.addView(layout);
    }

    public void move(View view, int row, int column){
        try {
            layout.removeView(view);
            ((GridLayout.LayoutParams) view.getLayoutParams()).columnSpec = GridLayout.spec(column);
            ((GridLayout.LayoutParams) view.getLayoutParams()).rowSpec = GridLayout.spec(row);
            layout.addView(view);
        } catch (IllegalStateException ex){
            ex.printStackTrace();
            draw(cells);
        }
    }

    public int getBoardWidth() {
        DisplayMetrics dm = getResources().getDisplayMetrics();
        return dm.widthPixels - 2*getResources().getDimensionPixelSize(R.dimen.activity_horizontal_margin);
    }

    public void showProgressDialog(){
        if(progressDialog == null) {
            progressDialog = Utils.showProgress(this, false, null);
        }
        if(!progressDialog.isShowing()){
            progressDialog.show();
        }
    }

    public void hideProgressDialog(){
        if (progressDialog != null) {
                progressDialog.dismiss();
        }
    }
}
