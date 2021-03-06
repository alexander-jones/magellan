package com.magellan.magellan;

import android.content.Context;
import android.graphics.Matrix;
import android.support.v4.view.GestureDetectorCompat;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

import com.github.mikephil.charting.charts.CombinedChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.utils.ViewPortHandler;

public class ChartGestureHandler extends ScaleGestureDetector.SimpleOnScaleGestureListener implements
    GestureDetector.OnGestureListener, View.OnTouchListener, GestureDetector.OnDoubleTapListener{

    public interface OnHighlightListener
    {
        void OnEntryHighlighted(Entry entry);
        void OnHighlightFinished();
    }

    private static final float DOUBLE_TAB_ZOOM_LEVEL = 3.0f;
    private boolean mSelecting = false;
    private boolean mScrolling = false;
    private Context mContext;
    private GestureDetectorCompat mDetector;
    private ScaleGestureDetector mScaleDetector;
    private CombinedChart mChart;
    private OnHighlightListener mListener;

    public ChartGestureHandler(Context context, CombinedChart chart, OnHighlightListener listener) {
        mContext = context;
        mChart = chart;
        mChart.setOnTouchListener(this);
        mDetector = new GestureDetectorCompat(mContext,this);
        mDetector.setOnDoubleTapListener(this);
        mScaleDetector = new ScaleGestureDetector(context, this);
        mListener = listener;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event){
        mScaleDetector.onTouchEvent(event);
        if (!mDetector.onTouchEvent(event)) {
            //Manually handle the event.
            if (event.getAction() == MotionEvent.ACTION_MOVE)
            {
                if (mSelecting)
                {
                    Entry entry = mChart.getEntryByTouchPoint(event.getX(), event.getY()); // this is not the xth element but the x screen pos...
                    mListener.OnEntryHighlighted(entry);
                }
            }
            else if (event.getAction() == MotionEvent.ACTION_UP)
            {
                mListener.OnHighlightFinished();
                mSelecting = false;
                mScrolling = false;
            }
        }
        return true;
    }

    @Override
    public boolean onScale(ScaleGestureDetector detector) {
        mChart.zoom(detector.getScaleFactor(), detector.getScaleFactor(), detector.getFocusX(), detector.getFocusY());
        return true;
    }

    @Override
    public void onLongPress(MotionEvent me) {
        if (!mScrolling)
        {
            Entry entry = mChart.getEntryByTouchPoint(me.getX(), me.getY()); // this is not the xth element but the x screen pos...
            mListener.OnEntryHighlighted(entry);
            mSelecting = true;
        }
    }

    @Override
    public boolean onScroll(MotionEvent event1, MotionEvent event2, float dX, float dY) {
        mScrolling = true;
        if (mChart.getScaleX() > 1.0f)
        {
            ViewPortHandler vph = mChart.getViewPortHandler();
            Matrix translateMatrix = vph.getMatrixTouch();
            translateMatrix.postTranslate(-dX, -dY);
            vph.refresh(translateMatrix, mChart, true);
        }
        return false;
    }

    @Override
    public boolean onDoubleTap(MotionEvent event) {

        if (mChart.getScaleX() > 1.0f)
            mChart.fitScreen();
        else
            mChart.zoom(DOUBLE_TAB_ZOOM_LEVEL, DOUBLE_TAB_ZOOM_LEVEL, event.getX(), mChart.getHeight() - event.getY());

        return false;
    }

    //region Unused Callbacks
    @Override
    public boolean onDoubleTapEvent(MotionEvent event) {
        return false;
    }

    @Override
    public boolean onSingleTapConfirmed(MotionEvent event) {
        return false;
    }

    @Override
    public void onShowPress(MotionEvent event) {
    }

    @Override
    public boolean onSingleTapUp(MotionEvent event) {
        return false;
    }

    @Override
    public boolean onDown(MotionEvent event) {
        return false;
    }

    @Override
    public boolean onFling(MotionEvent event1, MotionEvent event2, float velocityX, float velocityY) {
        return false;
    }
    //endregion
}
