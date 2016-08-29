package com.eugenekotsogub.puzzzle;

/**
 * Created by eugene.kotsogub on 8/26/16.
 *
 */
public enum  GameView {
    INSTANCE;

    public Coordinate freeCoordinate;

    public Coordinate getFreeCoordinate() {
        return freeCoordinate;
    }

    public void setFreeCoordinate(Coordinate freeCoordinate) {
        this.freeCoordinate.row = freeCoordinate.row;
        this.freeCoordinate.column = freeCoordinate.column;
    }

    public void createEmptyCoordinate(int column, int row) {
        freeCoordinate = new Coordinate(column, row);
    }
}
