package com.eugenekotsogub.puzzzle;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * Created by eugene.kotsogub on 8/26/16.
 *
 */
public class CellView extends TextView {

    public final Coordinate anchorCoordinate = new Coordinate();
    public Coordinate currentCoordinate = new Coordinate();


    public CellView(Context context) {
        super(context);
    }

    public CellView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CellView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public CellView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public Coordinate getAnchorCoordinate() {
        return anchorCoordinate;
    }

    public Coordinate getCurrentCoordinate() {
        return currentCoordinate;
    }

    public void setCurrentCoordinate(Coordinate coordinate) {
        this.currentCoordinate = coordinate;
    }

    public void setAnchorCoordiates(int row, int column) {
        this.anchorCoordinate.column = column;
        this.anchorCoordinate.row = row;
    }

    public void setCurrentCoordinate(int row, int column) {
        this.currentCoordinate.column = column;
        this.currentCoordinate.row = row;
    }
}
