package com.eugenekotsogub.puzzzle;

/**
 * Created by eugene.kotsogub on 8/26/16.
 *
 */
public class Coordinate {

    public int row;
    public int column;

    public Coordinate(){

    }

    public Coordinate(int column, int row) {
        this.row = row;
        this.column = column;
    }

    public void set(int row, int column){
        this.row = row;
        this.column = column;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Coordinate that = (Coordinate) o;

        return column == that.column && row == that.row;

    }

    @Override
    public int hashCode() {
        int result = column;
        result = 31 * result + row;
        return result;
    }
}
