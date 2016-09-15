package com.eugenekotsogub.puzzzle;

import android.view.MotionEvent;
import android.view.View;

import com.eugenekotsogub.puzzzle.cell.CellView;

/**
 * Created by eugene.kotsogub on 9/15/16.
 *
 */
public class
OnMoveTouchListener implements View.OnTouchListener {

    private float dX;
    private float dY;
    private float viewX;
    private float viewY;

    @Override
    public boolean onTouch(View view, MotionEvent event) {
        switch (event.getAction())
        {
            case MotionEvent.ACTION_MOVE:
                float x = event.getRawX() + dX;
                float y = event.getRawY() + dY;
                CellView v = (CellView) view;
                Move move = GameView.INSTANCE.canMove(v);
                switch (move){
                    case RIGHT:
                        if(x >= viewX && x <= viewX + v.getWidth() + PuzzzleActivity.ITEM_MARGIN){
                            view.animate()
                                    .x(x)
                                    .setDuration(0)
                                    .start();
                        }
                        break;
                    case LEFT:
                        if(x <= viewX && x >= viewX - v.getWidth() - PuzzzleActivity.ITEM_MARGIN){
                            view.animate()
                                    .x(x)
                                    .setDuration(0)
                                    .start();
                        }
                        break;
                    case UP:
                        if(y <= viewY && y >= viewY - v.getHeight() - PuzzzleActivity.ITEM_MARGIN){
                            view.animate()
                                    .y(y)
                                    .setDuration(0)
                                    .start();
                        }
                        break;
                    case DOWN:
                        if(y >= viewY && y <= viewY + v.getHeight() + PuzzzleActivity.ITEM_MARGIN){
                            view.animate()
                                    .y(y)
                                    .setDuration(0)
                                    .start();
                        }
                        break;
                    case NONE:

                        break;
                }
//                if(x + view.getWidth() >= right) {
//                    x = right - view.getWidth();
//                } else if(x <= left){
//                    x = left;
//                }
//                if(y + view.getHeight()>= bottom){
//                    y = bottom - view.getHeight();
//                } else if(y <= top){
//                    y = top;
//                }
//                Point point = isIntersect(view, (int)x, (int)y);
//                if( point != null){
//                    if(point.x != null){
//                        x = point.x;
//                    }
//                    if(point.y != null){
//                        y = point.y;
//                    }
//                }
//                view.animate()
//                        .x(x)
//                        .y(y)
//                        .setDuration(0)
//                        .start();
                break;
            case MotionEvent.ACTION_UP:
                    view.animate()
                        .x(viewX)
                        .y(viewY)
                        .setDuration(1000)
                        .start();
                break;
            case MotionEvent.ACTION_DOWN:
                viewX = view.getX();
                viewY = view.getY();
                dX = view.getX() - event.getRawX();
                dY = view.getY() - event.getRawY();
                break;
        }
        view.invalidate();
        return true;
    }
}
