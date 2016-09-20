package com.eugenekotsogub.puzzzle.cell;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
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
                    CellView cellView = createCellView(context, row, imageTiles, i, j, coordinate);
                    cells.add(cellView);
                } else {
                    lastTile = createCellView(context, row, imageTiles, i, j, coordinate);
                    lastTile.setText(null);
                }
            }
        }
        return cells;
    }

    @NonNull
    private static CellView createCellView(Context context, int row, List<Bitmap> imageTiles, int i, int j, Coordinate coordinate) {
        CellView cellView = new CellView(context);
        Utils.setBackground(cellView, imageTiles.get(coordinate.row*row + coordinate.column));
        cellView.setAnchorCoordiates(coordinate);
        String number = Integer.toString((coordinate.row*row + coordinate.column + 1));
        cellView.setText(number);
        cellView.setTextColor(ContextCompat.getColor(context,R.color.colorAccent));
        cellView.setShadowLayer(2,0,1,ContextCompat.getColor(context,R.color.black));
        cellView.setCurrentCoordinate(i,j);
        return cellView;
    }


    private static List<Bitmap> getImageTiles(Context context, String imagePath, int column, int row){
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

    public static void hideNumbers(List<CellView> cells) {
        for (CellView view :
                cells) {
            view.setText(null);
        }
        lastTile.setText(null);
    }

    public static void showNumbers(List<CellView> cells, int row) {
        for (CellView view :
                cells) {
            String number = Integer.toString(view.getAnchorCoordinate().row * row + view.getAnchorCoordinate().column + 1);
            view.setText(number);
        }
        String num = Integer.toString(row * row);
        lastTile.setText(num);
    }
}
