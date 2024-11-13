package com.example.demoshop;

import android.content.Context;

import android.os.Bundle;

import android.util.Log;
import android.webkit.JavascriptInterface;

import com.google.firebase.analytics.FirebaseAnalytics;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;

public class AnalyticsWebInterface {
    public static final String TAG = "AnalyticsWebInterface";
    private FirebaseAnalytics mAnalytics;

    public AnalyticsWebInterface(Context context) {
        mAnalytics = FirebaseAnalytics.getInstance(context);
    }

    @JavascriptInterface
    public void logEvent(String name, String jsonParams) {
        LOGD("logEvent: " + name + ", params: " + jsonParams);
        Bundle paramsBundle = bundleFromJson(jsonParams);
        mAnalytics.logEvent(name, paramsBundle);
    }

    @JavascriptInterface
    public void setUserProperty(String name, String value) {
        LOGD("setUserProperty: " + name + ", value: " + value);
        mAnalytics.setUserProperty(name, value);
    }

    private void LOGD(String message) {
        // Only log on debug builds, for privacy
        if (BuildConfig.DEBUG) {
            Log.d(TAG, message);
        }
    }

    private Bundle bundleFromJson(String json) {
        Bundle bundle = new Bundle();
        try {
            JSONObject jsonObject = new JSONObject(json);
            bundle = jsonToBundle(jsonObject);
        } catch (JSONException e) {
            Log.e(TAG, "Error parsing JSON", e);
        }
        return bundle;
    }

    private Bundle jsonToBundle(JSONObject jsonObject) throws JSONException {
        Bundle bundle = new Bundle();
        Iterator<String> keys = jsonObject.keys();

        while (keys.hasNext()) {
            String key = keys.next();
            Object value = jsonObject.get(key);

            if (value instanceof JSONObject) {
                // Recursively convert JSONObject to Bundle
                bundle.putBundle(key, jsonToBundle((JSONObject) value));
            } else if (value instanceof JSONArray) {
                JSONArray array = (JSONArray) value;

                if (array.length() == 0) {
                    // Empty array, skip or handle as needed
                    continue;
                }

                Object firstElement = array.get(0);

                if (firstElement instanceof JSONObject) {
                    // Array of JSONObjects, convert to array of Bundles
                    Bundle[] bundles = new Bundle[array.length()];
                    for (int i = 0; i < array.length(); i++) {
                        JSONObject arrayElement = array.getJSONObject(i);
                        bundles[i] = jsonToBundle(arrayElement);
                    }
                    bundle.putParcelableArray(key, bundles);
                } else if (firstElement instanceof String) {
                    // Array of Strings
                    String[] stringArray = new String[array.length()];
                    for (int i = 0; i < array.length(); i++) {
                        stringArray[i] = array.getString(i);
                    }
                    bundle.putStringArray(key, stringArray);
                } else if (firstElement instanceof Integer) {
                    // Array of Integers
                    int[] intArray = new int[array.length()];
                    for (int i = 0; i < array.length(); i++) {
                        intArray[i] = array.getInt(i);
                    }
                    bundle.putIntArray(key, intArray);
                } else if (firstElement instanceof Double) {
                    // Array of Doubles
                    double[] doubleArray = new double[array.length()];
                    for (int i = 0; i < array.length(); i++) {
                        doubleArray[i] = array.getDouble(i);
                    }
                    bundle.putDoubleArray(key, doubleArray);
                } else if (firstElement instanceof Long) {
                    // Array of Longs
                    long[] longArray = new long[array.length()];
                    for (int i = 0; i < array.length(); i++) {
                        longArray[i] = array.getLong(i);
                    }
                    bundle.putLongArray(key, longArray);
                } else if (firstElement instanceof Boolean) {
                    // Array of Booleans
                    boolean[] boolArray = new boolean[array.length()];
                    for (int i = 0; i < array.length(); i++) {
                        boolArray[i] = array.getBoolean(i);
                    }
                    bundle.putBooleanArray(key, boolArray);
                } else {
                    // Handle other types if necessary
                    Log.d(TAG, "Unhandled array type for key: " + key);
                }
            } else if (value instanceof String) {
                bundle.putString(key, (String) value);
            } else if (value instanceof Integer) {
                bundle.putInt(key, (Integer) value);
            } else if (value instanceof Long) {
                bundle.putLong(key, (Long) value);
            } else if (value instanceof Double) {
                bundle.putDouble(key, (Double) value);
            } else if (value instanceof Boolean) {
                bundle.putBoolean(key, (Boolean) value);
            } else if (value == JSONObject.NULL) {
                // Skip null values or handle as needed
                Log.d(TAG, "Skipping null value for key: " + key);
            } else {
                // Handle other types if necessary
                Log.d(TAG, "Unhandled type for key: " + key + ", value: " + value.toString());
            }
        }

        return bundle;
    }
}
