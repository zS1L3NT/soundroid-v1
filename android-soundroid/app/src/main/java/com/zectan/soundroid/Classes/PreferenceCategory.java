package com.zectan.soundroid.Classes;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

import androidx.preference.PreferenceViewHolder;

import com.zectan.soundroid.MainActivity;
import com.zectan.soundroid.R;

public class PreferenceCategory extends androidx.preference.PreferenceCategory {
    public PreferenceCategory(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public PreferenceCategory(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public PreferenceCategory(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PreferenceCategory(Context context) {
        super(context);
    }

    @Override
    public void onBindViewHolder(PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);
        MainActivity activity = (MainActivity) holder.itemView.getContext();
        TextView textView = holder.itemView.findViewById(android.R.id.title);
        textView.setTextColor(activity.getAttributeResource(R.attr.colorOnBackground));
    }
}
