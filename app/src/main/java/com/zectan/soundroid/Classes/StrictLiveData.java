package com.zectan.soundroid.Classes;

import androidx.annotation.NonNull;
import androidx.lifecycle.LifecycleOwner;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class StrictLiveData<@NotNull T> extends androidx.lifecycle.MutableLiveData<@NotNull T> {

    /**
     * Strict Live Data is an extension of MutableLiveData with @NotNull @NonNull annotations.
     * Makes sure the value from Live Data is not null
     *
     * @param value Value
     */
    public StrictLiveData(@NonNull @NotNull T value) {
        super(value);
    }

    @Override
    public void postValue(@NonNull @NotNull T value) {
        super.postValue(value);
    }

    public void observe(@NonNull @NotNull LifecycleOwner owner, @NonNull @NotNull Observer<? super @NotNull T> observer) {
        super.observe(owner, observer);
    }

    @NonNull
    @NotNull
    @Override
    public T getValue() {
        return Objects.requireNonNull(super.getValue());
    }

    @Override
    public void setValue(@NonNull @NotNull T value) {
        super.setValue(value);
    }

    public interface Observer<@NotNull T> extends androidx.lifecycle.Observer<@NotNull T> {
        @Override
        void onChanged(@NonNull @NotNull T t);
    }
}