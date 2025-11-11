package com.example.widgetapp;

import org.json.JSONObject;

public interface OnConnectionCompleteListener {
    void onSuccess(JSONObject response);
    void onFail(String message);
}