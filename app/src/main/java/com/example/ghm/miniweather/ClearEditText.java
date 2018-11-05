//package com.example.ghm.miniweather;
//
//import android.content.Context;
//import android.graphics.drawable.Drawable;
//import android.text.Editable;
//import android.text.TextWatcher;
//import android.util.AttributeSet;
//import android.view.MotionEvent;
//import android.view.View;
//import android.widget.EditText;
//
//
//public class ClearEditText extends EditText implements View.OnFocusChangeListener, TextWatcher {
//    private Drawable mClearDrawable;
//    public ClearEditText(Context context){
//        this(context,null);
//    }
//    public ClearEditText(Context context, AttributeSet attrs){
//        this(context, attrs, android.R.attr.editTextStyle);
//    }
//
//    public ClearEditText(Context context, AttributeSet attrs, int defStyle){
//        super(context, attrs, defStyle);
//        init();
//    }
//
//    private void init(){
//        mClearDrawable = getCompoundDrawables()[2];
//        if(mClearDrawable == null){
//            mClearDrawable = getResources().getDrawable(R.drawable.emotionstore_progresscancelbtn);
//        }
//        mClearDrawable.setBounds(0, 0, mClearDrawable.getIntrinsicWidth(),mClearDrawable.getIntrinsicHeight());
//        setClearIconVisible(false);
//        setOnFocusChangeListener(this);
//        addTextChangedListener(this);
//    }
//
//    public boolean onTouchEvent(MotionEvent event){
//        if(getCompoundDrawables()[2] != null){
//            if(event.getAction() == MotionEvent.ACTION_UP){
//                boolean touchable = event.getX() > (getWidth() - getPaddingRight() - mClearDrawable.getIntrinsicWidth()) && (event.getX()
//                < ((getWidth() - getPaddingRight())));
//                if(touchable){
//                    this.setText("");
//                }
//            }
//        }
//        return super.onTouchEvent(event);
//    }
//
//    @Override
//    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
//
//    }
//
//    @Override
//    public void onTextChanged(CharSequence s, int start, int before, int count) {
//        setClearIconVisible(s.length() > 0);
//    }
//
//    @Override
//    public void afterTextChanged(Editable s) {
//
//    }
//
//    @Override
//    public void onFocusChange(View v, boolean hasFocus) {
//        if(hasFocus){
//            setClearIconVisible(getText().length() > 0);
//        }
//        else {
//            setClearIconVisible(false);
//        }
//    }
//
//    private void setClearIconVisible(boolean b) {
//        Drawable right = b ? mClearDrawable : null;
//        setCompoundDrawables(getCompoundDrawables()[0],getCompoundDrawables()[1],right,getCompoundDrawables()[3]);
//    }
//    public void setShakeAnimation(){
//        this.setAnimation(shakeAnimation(5));
//    }
//}
