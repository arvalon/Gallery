package ru.arvalon.gallery.uitools;

import android.content.Context;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

/** листенер кликов по RecyclerView. Достал из какого-то своего старого проекта, в 2016 году это
 * было рабочий способ создания листенера для RecyclerView и в нём не было deprecated вызовов */
public class RecyclerTouchListener implements RecyclerView.OnItemTouchListener {

    public interface ClickListener {
        void onClick(View view, int position);
    }

    private GestureDetector gestureDetector;

    private ClickListener clickListener;

    public RecyclerTouchListener(Context context,
                                 final RecyclerView recyclerView,
                                 final ClickListener clickListener) {

        this.clickListener = clickListener;

        gestureDetector = new GestureDetector(context,
                new GestureDetector.SimpleOnGestureListener() {

            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                return true;
            }
        });
    }

    @Override
    public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {

        View child = rv.findChildViewUnder(e.getX(), e.getY());
        if (child != null && clickListener != null && gestureDetector.onTouchEvent(e)) {
            clickListener.onClick(child, rv.getChildPosition(child));
        }
        return false;
    }

    @Override
    public void onTouchEvent(RecyclerView rv, MotionEvent e) {
    }

    @Override
    public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

    }
}