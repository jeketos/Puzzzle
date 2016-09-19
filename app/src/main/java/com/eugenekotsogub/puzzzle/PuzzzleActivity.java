package com.eugenekotsogub.puzzzle;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.eugenekotsogub.puzzzle.cell.CellView;
import com.eugenekotsogub.puzzzle.cell.CellsFabric;
import com.eugenekotsogub.puzzzle.util.ImageUtils;
import com.eugenekotsogub.puzzzle.util.Permission;
import com.eugenekotsogub.puzzzle.util.Utils;

import java.io.File;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.fabric.sdk.android.Fabric;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class PuzzzleActivity extends AppCompatActivity {

    private static final int REQUEST_CAMERA = 20;
    private static final int REQUEST_GALLERY = 21;
    public static final String PHOTO_PATH = "photo_path";
    public static final String COLUMN_COUNT = "column_count";
    public static final String ROW_COUNT = "row_count";
    @BindView(R.id.main_container)
    ViewGroup mainContainer;

    List<CellView> cells;
    int columnCount = 3, rowCount = 3;
    private GridLayout layout;
    private int size;
    private ProgressDialog progressDialog;
    private String toCameraPath;
    private String photoPath;
    public static  int ITEM_MARGIN = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(savedInstanceState != null){
            photoPath = savedInstanceState.getString(PHOTO_PATH);
            columnCount = savedInstanceState.getInt(COLUMN_COUNT);
            rowCount = savedInstanceState.getInt(ROW_COUNT);
        }
        Fabric.with(this, new Crashlytics());
        setContentView(R.layout.activity_puzzzle);
        ButterKnife.bind(this);
        size = getBoardWidth();
        init();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString(PHOTO_PATH, photoPath);
        outState.putInt(COLUMN_COUNT, columnCount);
        outState.putInt(ROW_COUNT, rowCount);
        super.onSaveInstanceState(outState);
    }

    private void init() {
        showProgressDialog();
        Observable.fromCallable(this::createGame)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(coordinates -> {
                    cells = CellsFabric.create(this, photoPath, coordinates, columnCount, rowCount);
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
            case R.id.add_image:
                toGetPhotoDialog();
                return super.onOptionsItemSelected(item);
        }
        item.setChecked(true);
        init();
        return super.onOptionsItemSelected(item);
    }

    private OnMoveTouchListener swipeListener = new OnMoveTouchListener() {
        @Override
        protected void onMoveFinish(CellView view) {
            doMove(view);
        }
    };

    private void doMove(CellView v) {
        Coordinate free = GameView.INSTANCE.getFreeCoordinate();
        int freeRow = free.row;
        int freeColumn = free.column;
        Coordinate current = v.getCurrentCoordinate();
        GameView.INSTANCE.setFreeCoordinate(current);
        v.setCurrentCoordinate(freeRow, freeColumn);
        if (isPazzleDone()) {
            new Handler().postDelayed(() -> {
                clearViewTouch(layout);
                doFinalAnimation(layout);
//                Toast.makeText(PuzzzleActivity.this, "Ай да молодец! Выиграл!", Toast.LENGTH_SHORT).show();
            }, OnMoveTouchListener.ANIMATION_DURATION);
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
        if(layout == null) {
            layout = new GridLayout(this);
        }
        mainContainer.removeAllViews();
        layout.removeAllViews();
        layout.setMotionEventSplittingEnabled(false);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(size, size);
        params.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
        layout.setLayoutParams(params);
        layout.setColumnCount(columnCount);
        layout.setRowCount(rowCount);
        //noinspection ResourceType
        layout.setId(15);
        for (CellView view : cells) {
            view.setOnTouchListener(swipeListener);
            createLayoutParams(view, false);
            layout.addView(view);
        }
        AppCompatButton button = createShowImageButton();
        mainContainer.addView(layout);
        mainContainer.addView(button);

    }

    @NonNull
    private AppCompatButton createShowImageButton() {
        AppCompatButton button = new AppCompatButton(this);
        button.setText(R.string.show_image);
        RelativeLayout.LayoutParams p = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        p.addRule(RelativeLayout.BELOW, layout.getId());
        p.addRule(RelativeLayout.CENTER_HORIZONTAL);
        button.setLayoutParams(p);
        button.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            View view = LayoutInflater.from(this).inflate(R.layout.item_full_screen_image, null);
            ImageView imageView = (ImageView) view.findViewById(R.id.image_view);
            Bitmap image;
            if (TextUtils.isEmpty(photoPath)){
                image = BitmapFactory.decodeResource(getResources(), R.drawable.cat);
            } else {
                image = BitmapFactory.decodeFile(photoPath);
            }
            imageView.setImageDrawable(new BitmapDrawable(getResources(), image));
            builder.setView(view);
            builder.show();
        });
        return button;
    }

    private void createLayoutParams(CellView view, boolean isAnchor) {
        GridLayout.LayoutParams p= new GridLayout.LayoutParams();
        p.setMargins(ITEM_MARGIN,ITEM_MARGIN,ITEM_MARGIN,ITEM_MARGIN);
        if(isAnchor){
            p.columnSpec = GridLayout.spec(view.getAnchorCoordinate().column);
            p.rowSpec = GridLayout.spec(view.getAnchorCoordinate().row);
        } else {
            p.columnSpec = GridLayout.spec(view.getCurrentCoordinate().column);
            p.rowSpec = GridLayout.spec(view.getCurrentCoordinate().row);
        }
        view.setLayoutParams(p);
        view.getLayoutParams().height = size/rowCount - 2*ITEM_MARGIN;
        view.getLayoutParams().width = size/columnCount - 2*ITEM_MARGIN;
        view.setGravity(Gravity.CENTER);
        view.setTextSize((size/rowCount - 2*ITEM_MARGIN)/5);
    }

    public void clearViewTouch(ViewGroup viewGroup){
        for (int i = 0; i < viewGroup.getChildCount(); i++) {
            viewGroup.getChildAt(i).setOnTouchListener(null);
        }
    }

    public void doFinalAnimation(ViewGroup viewGroup){
        createLayoutParams(CellsFabric.lastTile, true);
        viewGroup.addView(CellsFabric.lastTile);
        for (int i = 0; i < viewGroup.getChildCount(); i++) {
            View view = viewGroup.getChildAt(i);
            Utils.createViewRotateAnimation(0, 360, 1000, view);
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

    public void toGetPhotoDialog(){
        final CharSequence[] items = { getString(R.string.take_photo), getString(R.string.from_gallery) };
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.photo_dialog_title);
        builder.setItems(items, (dialog, item) -> {
            if (item == 0) {
//                isCameraRequest = true;
                if(Permission.isGranted(this,Permission.REQUEST_CAMERA, Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE )) {
                    //noinspection MissingPermission
                    toCameraPath = ImageUtils.takePhoto(this,REQUEST_CAMERA);
//                    isCameraRequest = false;
                }  else {
                    showToast(getString(R.string.permission_camera_denied));
                }
            }
            else if (item == 1) {
                if(Permission.isGranted(this, Permission.REQUEST_READ_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    //noinspection MissingPermission
                    ImageUtils.getPhoto(this,REQUEST_GALLERY);
                }else {
                    showToast(getString(R.string.permission_read_denied));
                }
            }
        });
        builder.show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case Permission.REQUEST_CAMERA:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        //noinspection MissingPermission
                        toCameraPath = ImageUtils.takePhoto(this,REQUEST_CAMERA);
                } else {
                    showToast(getString(R.string.permission_camera_denied));
                }
                break;
            case Permission.REQUEST_READ_EXTERNAL_STORAGE:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //noinspection MissingPermission
                    ImageUtils.getPhoto(this,REQUEST_GALLERY);
                }else {
                    showToast(getString(R.string.permission_read_denied));
                }
                break;
        }
    }

    private void showToast(String text) {
        Toast.makeText(this,text,Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CAMERA && resultCode == Activity.RESULT_OK){
            Observable.fromCallable(() -> ImageUtils.getImageFile(this, Uri.fromFile(new File(toCameraPath))))
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(s -> {
                        photoPath = s;
                        init();
                    }, Throwable::printStackTrace);
        } else if (requestCode == REQUEST_GALLERY && resultCode == Activity.RESULT_OK){
            Observable.fromCallable(() -> ImageUtils.getImageFile(this,data.getData()))
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(s -> {
                        photoPath = s;
                        if (photoPath != null) init();
                    }, Throwable::printStackTrace);
        }
    }
}
