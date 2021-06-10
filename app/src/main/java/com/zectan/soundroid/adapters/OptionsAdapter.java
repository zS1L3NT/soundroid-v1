package com.zectan.soundroid.adapters;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.zectan.soundroid.R;
import com.zectan.soundroid.databinding.OptionsMenuListItemBinding;
import com.zectan.soundroid.objects.Option;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class OptionsAdapter extends RecyclerView.Adapter<OptionViewHolder> {
    private final List<Option> mOptions;

    public OptionsAdapter() {
        mOptions = new ArrayList<>();
    }

    @NonNull
    @Override
    public OptionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater
            .from(parent.getContext())
            .inflate(R.layout.options_menu_list_item, parent, false);

        return new OptionViewHolder(itemView);
    }

    public void updateOptions(List<Option> options) {
        mOptions.clear();
        mOptions.addAll(options);
        notifyDataSetChanged();
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull OptionViewHolder holder, int position) {
        holder.bind(mOptions.get(position));
    }

    @Override
    public int getItemCount() {
        return mOptions.size();
    }

}

class OptionViewHolder extends RecyclerView.ViewHolder {
    private final OptionsMenuListItemBinding B;

    public OptionViewHolder(@NonNull @NotNull View itemView) {
        super(itemView);
        B = OptionsMenuListItemBinding.bind(itemView);
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    public void bind(Option option) {
        Option.Callback callback = option.getCallback();
        int drawable = option.getDrawable();
        String title = option.getTitle();

        B.parent.setOnClickListener(__ -> callback.run());
        B.iconImage.setImageDrawable(B.parent.getContext().getDrawable(drawable));
        B.titleText.setText(title);
    }
}