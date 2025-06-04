package com.example.appstopsmoke.viewmodel;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.example.appstopsmoke.data.repository.SmokeRepository;

public class ViewModelFactory implements ViewModelProvider.Factory {
    private final SmokeRepository repository;

    public ViewModelFactory(SmokeRepository repository) {
        this.repository = repository;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(MainViewModel.class)) {
            return (T) new MainViewModel(repository);
        } else if (modelClass.isAssignableFrom(StatsViewModel.class)) {
            return (T) new StatsViewModel(repository);
        } else if (modelClass.isAssignableFrom(CompareViewModel.class)) {
            return (T) new CompareViewModel(repository);
        }
        throw new IllegalArgumentException("Unknown ViewModel class");
    }
}