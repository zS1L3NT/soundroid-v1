package com.zectan.soundroid;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.transition.TransitionInflater;

public class AnimatedFragment extends Fragment {

    @Override
    public void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TransitionInflater inflater = TransitionInflater.from(requireContext());
        setSharedElementEnterTransition(inflater.inflateTransition(android.R.transition.move));
        setSharedElementReturnTransition(inflater.inflateTransition(android.R.transition.move));
    }

}
