package com.example.orbcodescaner;

public interface HttpCallbackListener {
    void onFinish(String response);
    void onError(Exception e);
}
