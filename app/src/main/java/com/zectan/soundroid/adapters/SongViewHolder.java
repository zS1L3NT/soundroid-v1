package com.zectan.soundroid.adapters;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.zectan.soundroid.R;

import org.jetbrains.annotations.NotNull;

public class SongViewHolder extends RecyclerView.ViewHolder {
    public final View itemView;
    public final ImageView coverImage, menuImage;
    public final TextView titleText, artisteText;
    
    public SongViewHolder(@NonNull @NotNull View itemView) {
        super(itemView);
        this.itemView = itemView;
        
        coverImage = itemView.findViewById(R.id.song_list_item_cover);
        menuImage = itemView.findViewById(R.id.song_list_item_menu);
        titleText = itemView.findViewById(R.id.song_list_item_title);
        artisteText = itemView.findViewById(R.id.song_list_item_artiste);
    }
}