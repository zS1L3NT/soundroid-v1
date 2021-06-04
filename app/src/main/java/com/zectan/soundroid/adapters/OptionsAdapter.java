package com.zectan.soundroid.adapters;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.zectan.soundroid.MainActivity;
import com.zectan.soundroid.R;
import com.zectan.soundroid.objects.Option;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class OptionsAdapter extends RecyclerView.Adapter<OptionsAdapter.ViewHolder> {
    private final MainActivity activity;
    private List<Option> options;
    
    public OptionsAdapter(MainActivity activity) {
        this.activity = activity;
        this.options = new ArrayList<>();
    }
    
    @NonNull
    @Override
    public OptionsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater
            .from(parent.getContext())
            .inflate(R.layout.options_menu_list_item, parent, false);
        
        return new OptionsAdapter.ViewHolder(itemView);
    }
    
    public void updateOptions(List<Option> options) {
        this.options = options;
        notifyDataSetChanged();
    }
    
    @SuppressLint("UseCompatLoadingForDrawables")
    @Override
    public void onBindViewHolder(@NonNull @NotNull OptionsAdapter.ViewHolder holder, int position) {
        Option option = options.get(position);
        
        Option.Callback callback = option.getCallback();
        int drawable = option.getDrawable();
        String title = option.getTitle();
        
        holder.itemView.setOnClickListener(__ -> callback.run());
        holder.iconImage.setImageDrawable(activity.getDrawable(drawable));
        holder.titleText.setText(title);
    }
    
    @Override
    public int getItemCount() {
        return options.size();
    }
    
    public static class ViewHolder extends RecyclerView.ViewHolder {
        public final View itemView;
        public final ImageView iconImage;
        public final TextView titleText;
        
        public ViewHolder(@NonNull @NotNull View itemView) {
            super(itemView);
            this.itemView = itemView;
            
            iconImage = itemView.findViewById(R.id.options_menu_list_item_icon);
            titleText = itemView.findViewById(R.id.options_menu_list_item_title);
        }
    }
    
}
