package com.eugenekotsogub.puzzzle;

import com.eugenekotsogub.puzzzle.cell.CellView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Created by eugene.kotsogub on 8/26/16.
 *
 */
public enum  GameView {
    INSTANCE;

    public Coordinate freeCoordinate;
    private List<Coordinate> gameBoard;
    private int rowCount, columnCount;
    int[] neighbour = {-1,1};

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

    public void createGameBoard(int row, int column){
        rowCount = row;
        columnCount = column;
        createEmptyCoordinate(columnCount - 1, rowCount - 1);
        gameBoard = new ArrayList<>();
        for (int i = 0; i < row; i++) {
            for (int j = 0; j < column; j++) {
                gameBoard.add(new Coordinate(j,i));
            }
        }
    }

    public List<Coordinate> shuffle(){
        Random random = new Random();
        for(int i = 0; i < 10000*rowCount; i++){
            int row = freeCoordinate.row;
            int column = freeCoordinate.column;
            if(random.nextBoolean()) {
                row += neighbour[random.nextInt(2)];
            } else {
                column += neighbour[random.nextInt(2)];
            }
            if(0<=row && row < rowCount && 0 <= column && column < columnCount ) {
                    int bufFreeRow = freeCoordinate.row;
                    int bufFreeColumn = freeCoordinate.column;
                    freeCoordinate.row = row;

                    freeCoordinate.column = column;
                    Collections.swap(gameBoard, bufFreeRow * rowCount + bufFreeColumn, freeCoordinate.row * rowCount + freeCoordinate.column);
            }
        }
        return gameBoard;
    }

    public Move canMove(CellView view) {
        int row = view.getCurrentCoordinate().row;
        int column = view.getCurrentCoordinate().column;
        int freeRow = GameView.INSTANCE.getFreeCoordinate().row;
        int freeColumn = GameView.INSTANCE.getFreeCoordinate().column;
        column += 1;
        if (row == freeRow && column == freeColumn) {
            return Move.RIGHT;
        } else {
            column -= 2;
            if (row == freeRow && column == freeColumn) {
                return Move.LEFT;
            } else {
                column += 1;
                row += 1;
                if (row == freeRow && column == freeColumn) {
                    return Move.DOWN;
                } else {
                    row -= 2;
                    if (row == freeRow && column == freeColumn) {
                        return Move.UP;
                    } else {
                        return Move.NONE;
                    }
                }
            }
        }
    }
}

