package com.zectan.soundroid.adapters;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.ernestoyaquello.dragdropswiperecyclerview.DragDropSwipeAdapter;
import com.zectan.soundroid.R;

import org.jetbrains.annotations.NotNull;

public class QueueViewHolder extends DragDropSwipeAdapter.ViewHolder {
    public final View itemView;
    public final ImageView coverImage, dragImage;
    public final TextView titleText, artisteText, idText;
    
    public QueueViewHolder(@NotNull View itemView) {
        super(itemView);
        this.itemView = itemView;
        
        coverImage = itemView.findViewById(R.id.queue_list_item_cover);
        dragImage = itemView.findViewById(R.id.queue_list_item_drag);
        titleText = itemView.findViewById(R.id.queue_list_item_title);
        artisteText = itemView.findViewById(R.id.queue_list_item_artiste);
        idText = itemView.findViewById(R.id.queue_list_item_id);
    }
    
}
