package com.zectan.soundroid.classes;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.motion.widget.MotionLayout;

import com.zectan.soundroid.MainActivity;

import org.jetbrains.annotations.NotNull;

public class SavingMotionLayout extends MotionLayout {

    public SavingMotionLayout(@NonNull @NotNull Context context) {
        super(context);
    }

    public SavingMotionLayout(@NonNull @NotNull Context context, @Nullable @org.jetbrains.annotations.Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public SavingMotionLayout(@NonNull @NotNull Context context, @Nullable @org.jetbrains.annotations.Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        MainActivity activity = (MainActivity) getContext();
        activity.showNavigator();
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
            State state = (State) parcelable;
            Log.d("STATES_EQUAL", String.valueOf(state.progress));
            super.onRestoreInstanceState(state.superParcel);
            setTransition(state.startState, state.endState);
            setProgress(state.progress);

            MainActivity activity = (MainActivity) getContext();
            if (state.progress == 0) {
                activity.showNavigator();
            }
            if (state.progress == 1) {
                activity.hideNavigator();
            }
        }
    }

}

class State implements Parcelable {
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
