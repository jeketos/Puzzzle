package com.eugenekotsogub.puzzzle;

/**
 * Created by eugene.kotsogub on 8/26/16.
 *
 */
public class Coordinate {

    public int column;
    public int row;

    public Coordinate(){

    }

    public Coordinate(int column, int row) {
        this.column = column;
        this.row = row;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof Coordinate){
            return column == ((Coordinate) obj).column && row == ((Coordinate) obj).row;
        }
        return false;
    }
}
