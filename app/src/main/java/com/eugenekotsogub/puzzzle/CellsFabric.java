package com.eugenekotsogub.puzzzle;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by eugene.kotsogub on 8/26/16.
 *
 */
public class CellsFabric {


    public static List<CellView> create(Context context, List<Coordinate> shuffled, int column, int row){
        List<CellView> cells = new ArrayList<>();
        for (int i = 0; i < row; i++) {
            for (int j = 0; j < column; j++) {
                Coordinate coordinate = shuffled.get(i*row + j);
                if(!(coordinate.row == row -1 && coordinate.column == column - 1)){
                    CellView cellView = new CellView(context);
                    String text = "" + (coordinate.row*row + coordinate.column + 1);// user friendly number
                    cellView.setText(text);
                    cellView.setAnchorCoordiates(coordinate);
                    cellView.setCurrentCoordinate(i,j);
                    cells.add(cellView);
                }
            }
        }
        return cells;
    }


}
