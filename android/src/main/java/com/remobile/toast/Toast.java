package com.remobile.toast;

import android.view.Gravity;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.CountDownTimer;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;

import com.facebook.common.logging.FLog;
import com.facebook.react.bridge.*;

public class Toast extends ReactContextBaseJavaModule implements LifecycleEventListener {

    private android.widget.Toast mostRecentToast;

    // note that webView.isPaused() is not Xwalk compatible, so tracking it poor-man style
    private boolean isPaused;

    public Toast(ReactApplicationContext reactContext) {
        super(reactContext);
    }

    @Override
    public String getName() {
        return "RCTToast";
    }

    @ReactMethod
    public void show(ReadableMap options) throws Exception {
        if (this.isPaused) {
            return;
        }


        final String message = options.getString("message");
        final String duration = options.getString("duration");
        final String position = options.getString("position");
        final int addPixelsY = options.hasKey("addPixelsY") ? options.getInt("addPixelsY") : 0;

        UiThreadUtil.runOnUiThread(new Runnable() {
            public void run() {
                android.widget.Toast toast = android.widget.Toast.makeText(
                        getReactApplicationContext(),
                        message,
                        "short".equals(duration) ? android.widget.Toast.LENGTH_SHORT : android.widget.Toast.LENGTH_LONG);

                if ("top".equals(position)) {
                    toast.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL, 0, 20 + addPixelsY);
                } else if ("bottom".equals(position)) {
                    toast.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 100);
                } else if ("center".equals(position)) {
                    toast.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL, 0, addPixelsY);
                } else {
                    FLog.e("RCTToast", "invalid position. valid options are 'top', 'center' and 'bottom'");
                    return;
                }

                final String backgroundColor = "#28BF82";
                final String textColor = "#ffffff";
                final double opacity = 0.9;
                final int cornerRadius = 5;
                final int horizontalPadding = 35;
                final int verticalPadding = 20;

                final Double textSize = 17.0;
                Typeface tf = Typeface.createFromAsset(getReactApplicationContext().getAssets(), "fonts/avenir.otf");

                GradientDrawable shape = new GradientDrawable();
                shape.setCornerRadius(cornerRadius);
                shape.setAlpha((int)(opacity * 255)); // 0-255, where 0 is an invisible background
                shape.setColor(Color.parseColor(backgroundColor));
                toast.getView().setBackground(shape);

                final TextView toastTextView;
                toastTextView = (TextView) toast.getView().findViewById(android.R.id.message);
                toastTextView.setTextColor(Color.parseColor(textColor));
                toastTextView.setTextSize(textSize.floatValue());
                toastTextView.setTypeface(tf);

                toast.getView().setPadding(horizontalPadding, verticalPadding, horizontalPadding, verticalPadding);

                toast.show();
                mostRecentToast = toast;
            }
        });
    }

    @ReactMethod
    public void hide() throws Exception {
        if (mostRecentToast != null) {
            mostRecentToast.cancel();
        }
    }

    @Override
    public void initialize() {
        getReactApplicationContext().addLifecycleEventListener(this);
    }


    @Override
    public void onHostPause() {
        if (mostRecentToast != null) {
            mostRecentToast.cancel();
        }
        this.isPaused = true;
    }

    @Override
    public void onHostResume() {
        this.isPaused = false;
    }

    @Override
    public void onHostDestroy() {
        this.isPaused = true;
    }
}
