package com.zectan.soundroid.fragments;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.zectan.soundroid.MainActivity;
import com.zectan.soundroid.R;
import com.zectan.soundroid.adapters.OptionsAdapter;
import com.zectan.soundroid.databinding.FragmentOptionsMenuBinding;
import com.zectan.soundroid.objects.Option;
import com.zectan.soundroid.viewmodels.OptionsMenuViewModel;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class OptionsMenuFragment extends Fragment {
    private OptionsAdapter optionsAdapter;
    private MainActivity activity;
    private FragmentOptionsMenuBinding B;

    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        B = FragmentOptionsMenuBinding.inflate(inflater, container, false);
        activity = (MainActivity) getActivity();
        assert activity != null;

        // View Models
        OptionsMenuViewModel optionsMenuVM = new ViewModelProvider(activity).get(OptionsMenuViewModel.class);

        // Recycler View
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(activity);
        optionsAdapter = new OptionsAdapter();
        B.recyclerView.setLayoutManager(layoutManager);
        B.recyclerView.setAdapter(optionsAdapter);
        B.recyclerView.setHasFixedSize(true);

        // Observables
        optionsMenuVM.url.observe(activity, this::onUrlChange);
        optionsMenuVM.title.observe(activity, this::onTitleChange);
        optionsMenuVM.colorHex.observe(activity, this::onColorHexChange);
        optionsMenuVM.description.observe(activity, this::onDescriptionChange);
        optionsMenuVM.options.observe(activity, this::onOptionsChange);
        B.backImage.setOnClickListener(__ -> activity.onBackPressed());

        return B.getRoot();
    }

    private void onUrlChange(String url) {
        Glide
            .with(activity)
            .load(url)
            .into(B.coverImage);
    }

    private void onTitleChange(String title) {
        B.titleText.setText(title);
    }

    private void onColorHexChange(String colorHex) {
        int[] colors = {Color.parseColor(colorHex), activity.getColor(R.color.theme_4)};
        GradientDrawable drawable = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, colors);
        B.parent.setBackground(drawable);
    }

    private void onDescriptionChange(String description) {
        B.descriptionText.setText(description);
    }

    private void onOptionsChange(List<Option> options) {
        optionsAdapter.updateOptions(options);
    }
}