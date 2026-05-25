package com.example.writeai_android.utils;

public interface RepositoryCallback<T> {
    void onSuccess(T result);
    void onError(String message);
}
