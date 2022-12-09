package com.zectan.soundroid.Classes;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.motion.widget.MotionLayout;

import com.zectan.soundroid.MainActivity;

import org.jetbrains.annotations.NotNull;

public class SavingMotionLayout extends MotionLayout {
    private MainActivity mActivity;

    public SavingMotionLayout(@NonNull @NotNull Context context) {
        super(context);
        try {
            mActivity = (MainActivity) context;
        } catch (Exception ignored) {
        }

        initialiseNavigatorUpdater();
    }

    public SavingMotionLayout(@NonNull @NotNull Context context, @Nullable @org.jetbrains.annotations.Nullable AttributeSet attrs) {
        super(context, attrs);
        try {
            mActivity = (MainActivity) context;
        } catch (Exception ignored) {
        }

        initialiseNavigatorUpdater();
    }

    public SavingMotionLayout(@NonNull @NotNull Context context, @Nullable @org.jetbrains.annotations.Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        try {
            mActivity = (MainActivity) context;
        } catch (Exception ignored) {
        }

        initialiseNavigatorUpdater();
    }

    /**
     * Set up transition listener
     */
    private void initialiseNavigatorUpdater() {
        if (mActivity != null) {
            setTransitionListener(new TransitionListener() {
                @Override
                public void onTransitionStarted(MotionLayout motionLayout, int i, int i1) {
                    mActivity.updateNavigator(1f - motionLayout.getProgress());
                }

                @Override
                public void onTransitionChange(MotionLayout motionLayout, int i, int i1, float v) {
                    mActivity.updateNavigator(1f - motionLayout.getProgress());
                }

                @Override
                public void onTransitionCompleted(MotionLayout motionLayout, int i) {
                    mActivity.updateNavigator(1f - motionLayout.getProgress());
                }

                @Override
                public void onTransitionTrigger(MotionLayout motionLayout, int i, boolean b, float v) {
                }
            });
        }
    }

    @Nullable
    @Override
    protected Parcelable onSaveInstanceState() {
        return new State(
            super.onSaveInstanceState(),
            getStartState(),
            getEndState(),
            getProgress()
        );
    }

    @Override
    protected void onRestoreInstanceState(Parcelable parcelable) {
        if (parcelable == null) return;
        if (parcelable instanceof State) {
            // When restoring, get the previous state
            State state = (State) parcelable;
            super.onRestoreInstanceState(state.superParcel);
            setTransition(state.startState, state.endState);
            setProgress(state.progress);
            if (mActivity != null) {
                mActivity.updateNavigator(1f - state.progress);
            }
        }
    }

    public static class State implements Parcelable {
        public static final Creator<State> CREATOR = new Creator<State>() {
            @Override
            public State createFromParcel(Parcel in) {
                return new State(in);
            }

            @Override
            public State[] newArray(int size) {
                return new State[size];
            }
        };
        public final Parcelable superParcel;
        public final int startState;
        public final int endState;
        public final float progress;

        public State(Parcelable superParcel, int startState, int endState, float progress) {
            this.superParcel = superParcel;
            this.startState = startState;
            this.endState = endState;
            this.progress = progress;
        }

        public State(Parcel in) {
            this.superParcel = in.readParcelable(getClass().getClassLoader());
            this.startState = in.readInt();
            this.endState = in.readInt();
            this.progress = in.readFloat();
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(this.startState);
            dest.writeInt(this.endState);
            dest.writeFloat(this.progress);
        }
    }

}


