package com.zectan.soundroid.Fragments;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.transition.TransitionInflater;

import com.google.android.material.tabs.TabLayoutMediator;
import com.zectan.soundroid.Adapters.SearchViewPagerAdapter;
import com.zectan.soundroid.Classes.Fragment;
import com.zectan.soundroid.R;
import com.zectan.soundroid.Utils.Debounce;
import com.zectan.soundroid.databinding.FragmentSearchBinding;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SearchFragment extends Fragment<FragmentSearchBinding> {
    private static final String TAG = "(SounDroid) SearchFragment";
    private Debounce mDebounce;
    private final TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        @Override
        public void afterTextChanged(Editable s) {
            mDebounce.post(() -> {
                String text = s.toString();
                String search = mSearchVM.query.getValue();
                if (!search.equals(text)) {
                    mSearchVM.query.postValue(text);
                }
            });
        }
    };

    public SearchFragment() {
        super(FLAG_HIDE_NAVIGATOR);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TransitionInflater inflater = TransitionInflater.from(requireContext());
        setSharedElementEnterTransition(inflater.inflateTransition(R.transition.shared_image));
    }

    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        B = FragmentSearchBinding.inflate(inflater, container, false);
        super.onCreateView(inflater, container, savedInstanceState);
        mActivity.updateNavigator(0);

        // Observers
        mSearchVM.loading.observe(this, this::onLoadingChange);
        B.headerBackImage.setOnClickListener(__ -> mNavController.navigateUp());
        B.headerTextEditor.setText(mSearchVM.query.getValue());

        mDebounce = new Debounce(250);
        B.headerTextEditor.addTextChangedListener(textWatcher);

        mActivity.showKeyboard();
        B.viewPager.setAdapter(new SearchViewPagerAdapter(mActivity));
        new TabLayoutMediator(B.tabLayout, B.viewPager, (tab, position) -> tab.setText(position == 0 ? "Local" : "Server")).attach();

        return B.getRoot();
    }

    @Override
    public void onStop() {
        super.onStop();
        mActivity.hideKeyboard(requireView());
    }

    private void onLoadingChange(Boolean loading) {
        B.headerLoadingCircle.setVisibility(loading ? View.VISIBLE : View.INVISIBLE);
    }
}