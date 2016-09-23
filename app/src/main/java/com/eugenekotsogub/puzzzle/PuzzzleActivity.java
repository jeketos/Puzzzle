package com.eugenekotsogub.puzzzle;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatImageButton;
import android.support.v7.widget.AppCompatImageView;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.eugenekotsogub.puzzzle.cell.CellView;
import com.eugenekotsogub.puzzzle.cell.CellsFabric;
import com.eugenekotsogub.puzzzle.listener.OnMoveTouchListener;
import com.eugenekotsogub.puzzzle.util.ImageUtils;
import com.eugenekotsogub.puzzzle.util.Permission;
import com.eugenekotsogub.puzzzle.util.Utils;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.games.Games;

import java.io.File;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.fabric.sdk.android.Fabric;
import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class PuzzzleActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = "PuzzzleActivity";
    private static final int REQUEST_CAMERA = 20;
    private static final int REQUEST_GALLERY = 21;
    public static final String PHOTO_PATH = "photo_path";
    public static final String COLUMN_COUNT = "column_count";
    public static final String ROW_COUNT = "row_count";
    public static final String TURNS_COUNT = "turns_count";
    public static final String TIME_IN_SECONDS = "time_in_seconds";
    public static final String FIELD_SIZE = "field_size";
    public static final int GOOGLE_API_CLIENT_RESOLUTION = 2001;
    public static final String DIALOG_ERROR = "dialog_error";
    public static final String RESOLVING_ERROR = "resolving_error";
    @BindView(R.id.main_container)
    ViewGroup mainContainer;

    @BindView(R.id.grid_layout)
    GridLayout layout;
    @BindView(R.id.moves_count)
    TextView movesText;
    @BindView(R.id.time_text)
    TextView timeText;
    @BindView(R.id.show_image)
    View showImage;
    @BindView(R.id.show_hide_numbers)
    AppCompatImageButton showHideNumbers;
    @BindView(R.id.full_image)
    AppCompatImageView fullImage;
    List<CellView> cells;
    int columnCount = 3, rowCount = 3;
    private ProgressDialog progressDialog;
    private String toCameraPath;
    private String photoPath;
    public static  int ITEM_MARGIN = 2;
    private int movesCount = 0;
    private long timeInSeconds = 0;
    private long savedTime = 1;
    private Subscription timerSubscribe;
    FieldSize fieldSize = FieldSize.X3_3;
    GoogleApiClient googleApiClient;
    private boolean resolvingError = false;



    @OnClick(R.id.rerun) void onRerunClick(){
        init();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setOptionsMenuAsAction();
        Fabric.with(this, new Crashlytics());
        setContentView(R.layout.activity_puzzzle);
        ButterKnife.bind(this);
        googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Games.API).addScope(Games.SCOPE_GAMES)
                .build();
        if(savedInstanceState != null){
            photoPath = savedInstanceState.getString(PHOTO_PATH);
            columnCount = savedInstanceState.getInt(COLUMN_COUNT);
            rowCount = savedInstanceState.getInt(ROW_COUNT);
            movesCount = savedInstanceState.getInt(TURNS_COUNT);
            savedTime = savedInstanceState.getLong(TIME_IN_SECONDS);
            fieldSize = (FieldSize) savedInstanceState.getSerializable(FIELD_SIZE);
            resolvingError = savedInstanceState.getBoolean(RESOLVING_ERROR);
            if(!TextUtils.isEmpty(photoPath)){
                fullImage.setImageBitmap(BitmapFactory.decodeFile(photoPath));
            }
        } else {
            init();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(googleApiClient != null  ) {
            if (!googleApiClient.isConnected()) {
                googleApiClient.connect();
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(timerSubscribe != null) {
            timerSubscribe.unsubscribe();
        }
    }

    private void setOptionsMenuAsAction() {
        try {
            ViewConfiguration config = ViewConfiguration.get(this);
            Field menuKeyField = ViewConfiguration.class.getDeclaredField("sHasPermanentMenuKey");
            if(menuKeyField != null) {
                menuKeyField.setAccessible(true);
                menuKeyField.setBoolean(config, false);
            }
        } catch (Exception ex) {
            // Ignore
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString(PHOTO_PATH, photoPath);
        outState.putInt(COLUMN_COUNT, columnCount);
        outState.putInt(ROW_COUNT, rowCount);
        outState.putInt(TURNS_COUNT, movesCount);
        outState.putLong(TIME_IN_SECONDS, timeInSeconds);
        outState.putSerializable(FIELD_SIZE, fieldSize);
        outState.putBoolean(RESOLVING_ERROR, resolvingError);
        super.onSaveInstanceState(outState);
    }

    private void init() {
        movesCount = 0;
        timeInSeconds = 0;
        savedTime = 1;
        if(timerSubscribe != null){
            timerSubscribe.unsubscribe();
        }
        if(!TextUtils.isEmpty(photoPath)){
            fullImage.setImageBitmap(BitmapFactory.decodeFile(photoPath));
        }
        setTimeText(timeInSeconds);
        movesText.setText(String.format(Locale.getDefault(), "%d", movesCount));
        showProgressDialog();
        Observable.fromCallable(this::createGame)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(coordinates -> {
                    cells = CellsFabric.create(this, photoPath, coordinates, columnCount, rowCount);
                    draw(cells);
                    setNumbersVisibility();
                    hideProgressDialog();
                }, error -> {
                    hideProgressDialog();
                    error.printStackTrace();
                });
        showHideNumbers.setOnClickListener(v -> changeNumbersVisibility());
        showImage.setOnTouchListener((view, event) -> {
            switch (event.getAction())
            {
                case MotionEvent.ACTION_DOWN:
                    fullImage.setVisibility(View.VISIBLE);
                    break;
                case MotionEvent.ACTION_UP:
                    fullImage.setVisibility(View.GONE);
                    break;

            }
            return true;
        });
    }

    private void changeNumbersVisibility() {
        if(showHideNumbers.getTag().equals("visible")){
            showHideNumbers.setTag("invisible");
            showHideNumbers.setImageResource(R.drawable.ic_visible_off);
            CellsFabric.hideNumbers(cells);
        } else {
            showHideNumbers.setTag("visible");
            showHideNumbers.setImageResource(R.drawable.ic_visible);
            CellsFabric.showNumbers(cells, rowCount);
        }
    }
    private void setNumbersVisibility() {
        if(showHideNumbers.getTag().equals("visible")){
            showHideNumbers.setImageResource(R.drawable.ic_visible);
            CellsFabric.showNumbers(cells, rowCount);
        } else {
            showHideNumbers.setImageResource(R.drawable.ic_visible_off);
            CellsFabric.hideNumbers(cells);
        }
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
                fieldSize = FieldSize.X3_3;
                break;
            case R.id.x4x4:
                columnCount = 4;
                rowCount = 4;
                fieldSize = FieldSize.X4_4;
                break;
            case R.id.x5x5:
                columnCount = 5;
                rowCount = 5;
                fieldSize = FieldSize.X5_5;
                break;
            case R.id.x6x6:
                columnCount = 6;
                rowCount = 6;
                fieldSize = FieldSize.X6_6;
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
            setAndRunTimer(savedTime);
            incrementAndShowTurnsCount();
            doMove(view);
        }
    };

    private void setAndRunTimer(long startTime) {
        if(timerSubscribe == null || timerSubscribe.isUnsubscribed()) {
            timerSubscribe = Observable.interval(1, TimeUnit.SECONDS)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(time -> {
                        setTimeText(startTime + time);
                    });
        }
    }

    private void setTimeText(Long time) {
        timeText.setText(new SimpleDateFormat("mm:ss", Locale.getDefault()).format(time*1000));
        timeInSeconds = time;
    }

    private void incrementAndShowTurnsCount() {
        movesText.setText(String.format(Locale.getDefault(), "%d", ++movesCount));
    }

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
                sendLeadboard(fieldSize, timeInSeconds, movesCount);
            }, OnMoveTouchListener.ANIMATION_DURATION);
        }
    }

    private void sendLeadboard(FieldSize fieldSize, long timeInSeconds, int movesCount) {
        String leadboard_time = "";
        String leadboard_moves = "";
        String archivement = "";
        String archivement_time = "";
        String archivement_moves = "";
        switch (fieldSize){
            case X3_3:
                leadboard_time = getString(R.string.leaderboard_3x3_time);
                leadboard_moves = getString(R.string.leaderboard_3x3_moves);
                archivement = getString(R.string.achievement_3x3_mastered);
                if(timeInSeconds < 30){
                    archivement_time = getString(R.string.achievement_less_than_0_5_minute);
                }
                if (movesCount < 20 ){
                    archivement_moves = getString(R.string.achievement_less_than_20_moves);
                }
                break;
            case X4_4:
                leadboard_time = getString(R.string.leaderboard_4x4_time);
                leadboard_moves = getString(R.string.leaderboard_4x4_moves);
                archivement = getString(R.string.achievement_4x4_mastered);
                if(timeInSeconds < 60){
                    archivement_time = getString(R.string.achievement_less_than_1_minute);
                }
                if (movesCount < 100 ){
                    archivement_moves = getString(R.string.achievement_less_than_100_moves);
                }
                break;
            case X5_5:
                leadboard_time = getString(R.string.leaderboard_5x5_time);
                leadboard_moves = getString(R.string.leaderboard_5x5_moves);
                archivement = getString(R.string.achievement_5x5_mastered);
                if(timeInSeconds < 3*60){
                    archivement_time = getString(R.string.achievement_less_than_3_minute);
                }
                if (movesCount < 250 ){
                    archivement_moves = getString(R.string.achievement_less_than_250_moves);
                }
                break;
            case X6_6:
                leadboard_time = getString(R.string.leaderboard_6x6_time);
                leadboard_moves = getString(R.string.leaderboard_6x6_moves);
                archivement = getString(R.string.achievement_6x6_mastered);
                if(timeInSeconds < 5*60){
                    archivement_time = getString(R.string.achievement_less_than_5_minute);
                }
                if (movesCount < 500 ){
                    archivement_moves = getString(R.string.achievement_less_than_500_moves);
                }

                break;
        }
        Games.Leaderboards.submitScore(googleApiClient, leadboard_time, timeInSeconds);
        Games.Leaderboards.submitScore(googleApiClient, leadboard_moves, movesCount);
        Games.Achievements.unlock(googleApiClient, archivement);
        if(!TextUtils.isEmpty(archivement_time)){
            Games.Achievements.unlock(googleApiClient, archivement_time);
        }
        if(!TextUtils.isEmpty(archivement_moves)){
            Games.Achievements.unlock(googleApiClient, archivement_moves);
        }

    }

    private boolean isPazzleDone() {
        for(CellView view : cells){
            if(!view.getCurrentCoordinate().equals(view.getAnchorCoordinate())){
                return false;
            }
        }
        timerSubscribe.unsubscribe();
        return true;
    }

    private void draw(List<CellView> cells) {
        layout.removeAllViews();
        layout.setColumnCount(columnCount);
        layout.setRowCount(rowCount);
        for (CellView view : cells) {
            view.setOnTouchListener(swipeListener);
            createLayoutParams(view, false);
            layout.addView(view);
        }
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
        view.getLayoutParams().height = layout.getHeight()/rowCount - 2*ITEM_MARGIN;
        view.getLayoutParams().width = layout.getWidth()/columnCount - 2*ITEM_MARGIN;
        view.setGravity(Gravity.CENTER);
        view.setTextSize((layout.getWidth()/rowCount - 2*ITEM_MARGIN)/5);
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
        } else if(requestCode == 2001 && resultCode == Activity.RESULT_OK){
            resolvingError = false;
            if (!googleApiClient.isConnecting() &&
                    !googleApiClient.isConnected()) {
                googleApiClient.connect();
            }

            googleApiClient.connect();
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.d(TAG, "onConnected() called. Sign in successful!");
    }

    @Override
    public void onConnectionSuspended(int i) {
        googleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d(TAG, "onConnectionFailed() called, result: " + connectionResult);

            if (resolvingError) {
                // Already attempting to resolve an error.
                //noinspection UnnecessaryReturnStatement
                return;
            }else if(connectionResult.hasResolution()) {
                try {
                    resolvingError = true;
                    connectionResult.startResolutionForResult(this, GOOGLE_API_CLIENT_RESOLUTION);
                } catch (IntentSender.SendIntentException e) {
                    e.printStackTrace();
                }
            } else {
                showErrorDialog(connectionResult.getErrorCode());
                resolvingError = true;

            }
    }

    public void onDialogDismissed() {
        resolvingError = false;
    }


    private void showErrorDialog(int errorCode) {
        // Create a fragment for the error dialog
        ErrorDialogFragment dialogFragment = new ErrorDialogFragment();
        // Pass the error that should be displayed
        Bundle args = new Bundle();
        args.putInt(DIALOG_ERROR, errorCode);
        dialogFragment.setArguments(args);
        dialogFragment.show(getSupportFragmentManager(), "error_dialog");
    }

    public static class ErrorDialogFragment extends DialogFragment {
        public ErrorDialogFragment() { }

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Get the error code and retrieve the appropriate dialog
            int errorCode = this.getArguments().getInt(DIALOG_ERROR);
            return GoogleApiAvailability.getInstance().getErrorDialog(
                    this.getActivity(), errorCode, GOOGLE_API_CLIENT_RESOLUTION);
        }

        @Override
        public void onDismiss(DialogInterface dialog) {
            ((PuzzzleActivity) getActivity()).onDialogDismissed();
        }
    }



}
