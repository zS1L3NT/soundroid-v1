package com.zectan.soundroid.anonymous;

import android.view.View;
import android.view.ViewGroup;

/**
 * Class to handle margins in an element
 */
public class MarginProxy {
    private final View mView;

    public MarginProxy(View view) {
        mView = view;
    }

    public int getLeftMargin() {
        ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) mView.getLayoutParams();
        return lp.leftMargin;
    }

    public void setLeftMargin(int margin) {
        ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) mView.getLayoutParams();
        lp.setMargins(margin, lp.topMargin, lp.rightMargin, lp.bottomMargin);
        mView.requestLayout();
    }

    public int getTopMargin() {
        ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) mView.getLayoutParams();
        return lp.topMargin;
    }

    public void setTopMargin(int margin) {
        ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) mView.getLayoutParams();
        lp.setMargins(lp.leftMargin, margin, lp.rightMargin, lp.bottomMargin);
        mView.requestLayout();
    }

    public int getRightMargin() {
        ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) mView.getLayoutParams();
        return lp.rightMargin;
    }

    public void setRightMargin(int margin) {
        ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) mView.getLayoutParams();
        lp.setMargins(lp.leftMargin, lp.topMargin, margin, lp.bottomMargin);
        mView.requestLayout();
    }

    public int getBottomMargin() {
        ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) mView.getLayoutParams();
        return lp.bottomMargin;
    }

    public void setBottomMargin(int margin) {
        ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) mView.getLayoutParams();
        lp.setMargins(lp.leftMargin, lp.topMargin, lp.rightMargin, margin);
        mView.requestLayout();
    }
}