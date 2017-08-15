package com.magellan.magellan;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.support.constraint.solver.widgets.Rectangle;
import android.support.v4.content.ContextCompat;
import android.util.TypedValue;

public class TextDrawable extends Drawable {

    private final int mFudge;
    private final Rect mTextSize = new Rect();
    private final String mText;
    private final Paint mPaint;

    public TextDrawable(Context context, String text) {
        mText = text;
        mPaint = new Paint();
        mPaint.setColor(ContextCompat.getColor(context, R.color.colorPrimaryDarkest));
        int size = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 25, context.getResources().getDisplayMetrics());
        mFudge = size / 2;
        mPaint.setTextSize(mFudge);
        mPaint.setAntiAlias(true);
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setTextAlign(Paint.Align.LEFT);
        mPaint.getTextBounds(mText, 0, mText.length() - 1, mTextSize);
    }

    @Override
    public void draw(Canvas canvas) {
        Rect rect = canvas.getClipBounds();
        canvas.drawText(mText, rect.right - mTextSize.width() - mFudge, (rect.bottom / 2) + (mTextSize.height() / 2), mPaint);
    }

    @Override
    public void setAlpha(int alpha) {
        mPaint.setAlpha(alpha);
    }

    @Override
    public void setColorFilter(ColorFilter cf) {
        mPaint.setColorFilter(cf);
    }

    @Override
    public int getOpacity() {
        return PixelFormat.TRANSLUCENT;
    }
}