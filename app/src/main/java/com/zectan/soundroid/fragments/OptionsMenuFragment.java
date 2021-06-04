package com.zectan.soundroid.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.zectan.soundroid.MainActivity;
import com.zectan.soundroid.R;
import com.zectan.soundroid.adapters.OptionsAdapter;
import com.zectan.soundroid.objects.Option;
import com.zectan.soundroid.viewmodels.OptionsMenuViewModel;

import java.util.List;

public class OptionsMenuFragment extends Fragment {
    private OptionsAdapter optionsAdapter;
    private MainActivity activity;
    
    private ImageView coverImage;
    private TextView titleText, descriptionText;
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_options_menu, container, false);
        activity = (MainActivity) getActivity();
        assert activity != null;
        
        // View Models
        OptionsMenuViewModel optionsMenuVM = new ViewModelProvider(activity).get(OptionsMenuViewModel.class);
        
        // Reference Views
        RecyclerView recyclerView = view.findViewById(R.id.options_menu_recycler_view);
        coverImage = view.findViewById(R.id.options_menu_cover);
        titleText = view.findViewById(R.id.options_menu_title);
        descriptionText = view.findViewById(R.id.options_menu_description);
        
        // Recycler View
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(activity);
        optionsAdapter = new OptionsAdapter(activity);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(optionsAdapter);
        recyclerView.setHasFixedSize(true);
        
        // Observables
        optionsMenuVM.url.observe(activity, this::onUrlChange);
        optionsMenuVM.title.observe(activity, this::onTitleChange);
        optionsMenuVM.description.observe(activity, this::onDescriptionChange);
        optionsMenuVM.options.observe(activity, this::onOptionsChange);
        
        return view;
    }
    
    private void onUrlChange(String url) {
        Glide
            .with(activity)
            .load(url)
            .into(coverImage);
    }
    
    private void onTitleChange(String title) {
        titleText.setText(title);
    }
    
    private void onDescriptionChange(String description) {
        descriptionText.setText(description);
    }
    
    private void onOptionsChange(List<Option> options) {
        optionsAdapter.updateOptions(options);
    }
    
    
}