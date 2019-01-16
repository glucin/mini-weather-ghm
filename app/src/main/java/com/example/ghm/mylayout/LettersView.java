package com.example.ghm.mylayout;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.View;


public class LettersView extends View {
    private static final String TAG = "LettersView";
    private String[] strChars = {"A", "B", "C", "D", "E", "F", "G", "H",
    "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U",
    "V", "w", "X", "Y", "Z", "#"};

    private Paint mPaint;

    private int checkIndex;

    public LettersView(Context context){
        super(context);
        initview();
    }
    public LettersView(Context context, AttributeSet attrs){
        super(context, attrs);
        initview();
    }
    public LettersView(Context context, AttributeSet attrs, int defStyleAttr){
        super(context, attrs, defStyleAttr);
        initview();
    }
    private void initview(){
        mPaint = new Paint();
        mPaint.setTypeface(Typeface.DEFAULT_BOLD);
        mPaint.setAntiAlias(true);
        checkIndex = -1;
    }
    protected void onDraw(Canvas canvas){
        super.onDraw(canvas);
        int viewWidth = getWidth();
        int viewHeight = getHeight();
        int singleHeight = viewHeight/strChars.length;

        for(int i = 0; i < strChars.length; i++){
            if(i == checkIndex){
                mPaint.setColor(Color.WHITE);
                mPaint.setTextSize(50);
            }
            else {
                mPaint.setColor(Color.BLACK);
                mPaint.setTextSize(40);
            }
            float lettersX = (viewWidth - mPaint.measureText(strChars[i]))/2;
            float lettersY = singleHeight * i + singleHeight;
            canvas.drawText(strChars[i], lettersX, lettersY, mPaint);
            mPaint.reset();
        }
    }

    public interface OnLettersListViewListener {
        public void onLettersListener(String s);
    }



}
