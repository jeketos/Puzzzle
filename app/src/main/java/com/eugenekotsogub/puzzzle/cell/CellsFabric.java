package com.eugenekotsogub.puzzzle.cell;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.TextUtils;

import com.eugenekotsogub.puzzzle.Coordinate;
import com.eugenekotsogub.puzzzle.R;
import com.eugenekotsogub.puzzzle.util.Utils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by eugene.kotsogub on 8/26/16.
 *
 */
public class CellsFabric {

    public static CellView lastTile;

    public static List<CellView> create(Context context, String imagePath, List<Coordinate> shuffled, int column, int row){
        List<CellView> cells = new ArrayList<>();
        List<Bitmap> imageTiles = getImageTiles(context, imagePath, column, row);
        for (int i = 0; i < row; i++) {
            for (int j = 0; j < column; j++) {
                Coordinate coordinate = shuffled.get(i*row + j);
                if(!(coordinate.row == row -1 && coordinate.column == column - 1)){
                    CellView cellView = new CellView(context);
                    Utils.setBackground(cellView, imageTiles.get(coordinate.row*row + coordinate.column));
                    cellView.setAnchorCoordiates(coordinate);
                    cellView.setCurrentCoordinate(i,j);
                    cells.add(cellView);
                } else {
                    CellView cellView = new CellView(context);
                    Utils.setBackground(cellView, imageTiles.get(coordinate.row*row + coordinate.column));
                    cellView.setAnchorCoordiates(coordinate);
                    cellView.setCurrentCoordinate(i,j);
                    lastTile = cellView;
                }
            }
        }
        return cells;
    }


    public static List<Bitmap> getImageTiles(Context context, String imagePath, int column, int row){
        List<Bitmap> list = new ArrayList<>();
        Bitmap image;
        if (TextUtils.isEmpty(imagePath)){
            image = BitmapFactory.decodeResource(context.getResources(), R.drawable.cat);
        } else {
            image = BitmapFactory.decodeFile(imagePath);
        }
        int tileWidth = image.getWidth()/column;
        int tileHeight = image.getHeight()/row;
        for(int i = 0; i < row; i++) {
            for (int j = 0; j < column; j++) {
                Bitmap tile = Bitmap.createBitmap(image, j*tileHeight, i*tileWidth,tileWidth,tileHeight);
                list.add(tile);
            }
        }
        return list;
    }

}
