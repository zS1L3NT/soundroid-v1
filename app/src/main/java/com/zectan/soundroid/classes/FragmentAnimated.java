package com.zectan.soundroid.classes;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.transition.TransitionInflater;
import androidx.viewbinding.ViewBinding;

import com.zectan.soundroid.R;

public class FragmentAnimated<T extends ViewBinding> extends Fragment<T> {

    @Override
    public void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TransitionInflater inflater = TransitionInflater.from(requireContext());
        setSharedElementEnterTransition(inflater.inflateTransition(R.transition.shared_image));
    }

}
