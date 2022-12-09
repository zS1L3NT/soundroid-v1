package com.zectan.soundroid.Adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.zectan.soundroid.Fragments.SearchLocalFragment;
import com.zectan.soundroid.Fragments.SearchServerFragment;

import java.util.ArrayList;
import java.util.List;

public class SearchViewPagerAdapter extends FragmentStateAdapter {

    private final List<Fragment> mFragments;

    public SearchViewPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
        mFragments = new ArrayList<>();
        mFragments.add(new SearchLocalFragment());
        mFragments.add(new SearchServerFragment());
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return mFragments.get(position);
    }

    @Override
    public int getItemCount() {
        return mFragments.size();
    }
}
