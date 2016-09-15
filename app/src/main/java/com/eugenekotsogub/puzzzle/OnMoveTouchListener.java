package com.eugenekotsogub.puzzzle;

import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;

import com.eugenekotsogub.puzzzle.cell.CellView;

/**
 * Created by eugene.kotsogub on 9/15/16.
 *
 */
public abstract class OnMoveTouchListener implements View.OnTouchListener {

    public static final int ANIMATION_DURATION = 50;
    private float dX;
    private float dY;
    private float viewX;
    private float viewY;
    private float x;
    private float y;
    private Move move = null;

    @Override
    public boolean onTouch(View view, MotionEvent event) {
        switch (event.getAction())
        {
            case MotionEvent.ACTION_DOWN:
                actionDown(view, event);
                break;
            case MotionEvent.ACTION_MOVE:
                actionMove(view, event);
                break;
            case MotionEvent.ACTION_UP:
                actionUp(view);
                break;

        }
        view.invalidate();
        return true;
    }

    private void actionDown(View view, MotionEvent event) {
        viewX = view.getX();
        viewY = view.getY();
        dX = view.getX() - event.getRawX();
        dY = view.getY() - event.getRawY();
        x = viewX;
        y = viewY;
    }

    private void actionMove(View view, MotionEvent event) {
        float x = event.getRawX() + dX;
        float y = event.getRawY() + dY;
        CellView v = (CellView) view;
        move = GameView.INSTANCE.canMove(v);
        switch (move){
            case RIGHT:
                if(x >= viewX && x <= viewX + v.getWidth() + PuzzzleActivity.ITEM_MARGIN){
                    setXY(x,y);
                    view.animate()
                            .x(x)
                            .setDuration(0)
                            .start();
                }
                break;
            case LEFT:
                if(x <= viewX && x >= viewX - v.getWidth() - PuzzzleActivity.ITEM_MARGIN){
                    setXY(x,y);
                    view.animate()
                            .x(x)
                            .setDuration(0)
                            .start();
                }
                break;
            case UP:
                if(y <= viewY && y >= viewY - v.getHeight() - PuzzzleActivity.ITEM_MARGIN){
                    setXY(x,y);
                    view.animate()
                            .y(y)
                            .setDuration(0)
                            .start();
                }
                break;
            case DOWN:
                if(y >= viewY && y <= viewY + v.getHeight() + PuzzzleActivity.ITEM_MARGIN){
                    setXY(x,y);
                    view.animate()
                            .y(y)
                            .setDuration(0)
                            .start();
                }
                break;
            case NONE:

                break;
        }
    }

    private void actionUp(View view) {
        if(move != null) {
            view.setOnTouchListener(null);
            switch (move){
                case RIGHT:
                    if(x - viewX >= view.getWidth()/2){
                        view.animate()
                                .x(viewX + view.getWidth() + 2*PuzzzleActivity.ITEM_MARGIN)
                                .setDuration(ANIMATION_DURATION)
                                .start();
                        finishMoveDelayed(() ->
                                onMoveFinish((CellView)view));
                    } else {
                        view.animate()
                                .x(viewX)
                                .setDuration(ANIMATION_DURATION)
                                .start();
                    }

                    break;
                case LEFT:
                    if(viewX - x >= view.getWidth()/2){
                        view.animate()
                                .x(viewX - view.getWidth() - 2*PuzzzleActivity.ITEM_MARGIN)
                                .setDuration(ANIMATION_DURATION)
                                .start();
                        finishMoveDelayed(() ->
                                onMoveFinish((CellView)view));
                    } else {
                        view.animate()
                                .x(viewX)
                                .setDuration(ANIMATION_DURATION)
                                .start();
                    }

                    break;
                case DOWN:
                    if(y - viewY >= view.getHeight()/2){
                        view.animate()
                                .y(viewY + view.getHeight() + 2*PuzzzleActivity.ITEM_MARGIN)
                                .setDuration(ANIMATION_DURATION)
                                .start();
                        finishMoveDelayed(() ->
                                onMoveFinish((CellView)view));
                    } else {
                        view.animate()
                                .y(viewY)
                                .setDuration(ANIMATION_DURATION)
                                .start();
                    }

                    break;
                case UP:
                    if(viewY - y >= view.getHeight()/2){
                        view.animate()
                                .y(viewY - view.getHeight() - 2*PuzzzleActivity.ITEM_MARGIN)
                                .setDuration(ANIMATION_DURATION)
                                .start();
                        finishMoveDelayed(() ->
                                onMoveFinish((CellView)view));
                    } else {
                        view.animate()
                                .y(viewY)
                                .setDuration(ANIMATION_DURATION)
                                .start();
                    }
                    break;
                case NONE:

                    break;
            }
            new Handler().postDelayed(() -> view.setOnTouchListener(this), ANIMATION_DURATION);
        }
    }

    private void finishMoveDelayed(Runnable runnable) {
        new Handler().postDelayed(runnable, ANIMATION_DURATION);
    }

    protected abstract void onMoveFinish(CellView view);

    private void setXY(float x, float y) {
        this.x = x;
        this.y = y;
    }
}
