package com.zectan.soundroid.objects;

import android.view.View;
import android.view.ViewGroup.MarginLayoutParams;

/**
 * Allows an ObjectAnimator to set/get margins of a view
 */
public class MarginProxy {
    private final View mView;
    
    public MarginProxy(View view) {
        mView = view;
    }
    
    public int getLeftMargin() {
        MarginLayoutParams lp = (MarginLayoutParams) mView.getLayoutParams();
        return lp.leftMargin;
    }
    
    public void setLeftMargin(int margin) {
        MarginLayoutParams lp = (MarginLayoutParams) mView.getLayoutParams();
        lp.setMargins(margin, lp.topMargin, lp.rightMargin, lp.bottomMargin);
        mView.requestLayout();
    }
    
    public int getTopMargin() {
        MarginLayoutParams lp = (MarginLayoutParams) mView.getLayoutParams();
        return lp.topMargin;
    }
    
    public void setTopMargin(int margin) {
        MarginLayoutParams lp = (MarginLayoutParams) mView.getLayoutParams();
        lp.setMargins(lp.leftMargin, margin, lp.rightMargin, lp.bottomMargin);
        mView.requestLayout();
    }
    
    public int getRightMargin() {
        MarginLayoutParams lp = (MarginLayoutParams) mView.getLayoutParams();
        return lp.rightMargin;
    }
    
    public void setRightMargin(int margin) {
        MarginLayoutParams lp = (MarginLayoutParams) mView.getLayoutParams();
        lp.setMargins(lp.leftMargin, lp.topMargin, margin, lp.bottomMargin);
        mView.requestLayout();
    }
    
    public int getBottomMargin() {
        MarginLayoutParams lp = (MarginLayoutParams) mView.getLayoutParams();
        return lp.bottomMargin;
    }
    
    public void setBottomMargin(int margin) {
        MarginLayoutParams lp = (MarginLayoutParams) mView.getLayoutParams();
        lp.setMargins(lp.leftMargin, lp.topMargin, lp.rightMargin, margin);
        mView.requestLayout();
    }
}