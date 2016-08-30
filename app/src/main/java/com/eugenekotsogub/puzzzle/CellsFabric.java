package com.eugenekotsogub.puzzzle;

import android.content.Context;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by eugene.kotsogub on 8/26/16.
 *
 */
public class CellsFabric {


    public static List<CellView> create(Context context, int column, int row){
        List<CellView> cells = new ArrayList<>();
        List<Coordinate> shuffled = createShuffledCoordinates(column,row);

        for (int i = 0; i < row; i++) {
            for (int j = 0; j < column; j++) {
                if(i*row + j == column*row - 1){
                    break;

                }
                CellView cellView = new CellView(context);
                String text = "" + (i*row + j + 1);// user friendly number
                cellView.setText(text);
//                cellView.setCurrentCoordinate(shuffled.get(i*column +j));
                cellView.setAnchorCoordiates(i,j);
                cellView.setCurrentCoordinate(i,j);
                cells.add(cellView);
            }
        }
        return cells;
    }

    private static List<Coordinate> createShuffledCoordinates(int column, int row) {
        List<Coordinate> shuffled = new ArrayList<>(column*row -1);
        for (int i = 0; i < row; i++) {
            for (int j = 0; j < column; j++) {
                if(i*row + j == column*row - 1){
                    break;
                }
                Coordinate coordinate = new Coordinate();
                coordinate.column = j;
                coordinate.row = i;
                shuffled.add(coordinate);
            }
        }
        Collections.shuffle(shuffled);
        return shuffled;
    }
}
