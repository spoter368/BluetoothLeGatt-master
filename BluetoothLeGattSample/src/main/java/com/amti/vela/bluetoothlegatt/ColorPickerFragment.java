package com.amti.vela.bluetoothlegatt;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;

import com.larswerkman.holocolorpicker.ColorPicker;
import com.larswerkman.holocolorpicker.SaturationBar;
import com.larswerkman.holocolorpicker.ValueBar;

public class ColorPickerFragment extends android.support.v4.app.Fragment implements ColorPicker.OnColorChangedListener,
        ColorPicker.OnTouchListener {

    ColorPicker mColorPicker;
    RelativeLayout relativeLayout;
    SaturationBar mSaturationBar;
    int mPrevColor;
    ValueBar mValueBar;

    private final static int kFirstTimeColor = 0x0000ff;
    private int mSelectedColor;
    int r, g, b;

    Handler btHandler;
    Handler sendHandler = new Handler();

    Runnable sendTimerRunnable = new Runnable() {

        @Override
        public void run() {
            Message msg = btHandler.obtainMessage(MainActivity.SEND_COLOR_VALUES);
            btHandler.sendMessage(msg);
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        MainActivity mainActivity = (MainActivity)getActivity();
        btHandler = mainActivity.getBtHandler();

        RelativeLayout relativeLayout = initGui(inflater, container);
        return relativeLayout;
    }

    RelativeLayout initGui(LayoutInflater inflater, ViewGroup container)
    {
        relativeLayout = (RelativeLayout)inflater.inflate(R.layout.fragment_color_picker, container, false);
        mSaturationBar = (SaturationBar) relativeLayout.findViewById(R.id.saturationbar);
        mValueBar = (ValueBar) relativeLayout.findViewById(R.id.valuebar);
        mColorPicker = (ColorPicker) relativeLayout.findViewById(R.id.colorPicker);
        mColorPicker.addSaturationBar(mSaturationBar);
        mColorPicker.addValueBar(mValueBar);

        mSelectedColor = kFirstTimeColor;
        mColorPicker.setOldCenterColor(mSelectedColor);
        mColorPicker.setColor(mSelectedColor);

        mSaturationBar.setColor(mSelectedColor);
        mValueBar.setColor(mSelectedColor);

        mColorPicker.setOnColorChangedListener(this);

        mColorPicker.setOnTouchListener(this);
        mSaturationBar.setOnTouchListener(this);
        mValueBar.setOnTouchListener(this);

        return relativeLayout;
    }

    public String getColorString()
    {
        String redString = "";
        String greenString = "";
        String blueString = "";

        if (r < 100) { redString += "0"; }
        if (r < 10) { redString += "0"; }

        if (g < 100) { greenString += "0"; }
        if (g < 10) { greenString += "0"; }

        if (b < 100) { blueString += "0"; }
        if (b < 10) { blueString += "0"; }

        redString += Integer.toString(r);
        greenString += Integer.toString(g);
        blueString += Integer.toString(b);

        return redString + greenString + blueString;
    }

    public void initColors(int color)
    {
        mColorPicker.setColor(color);
    }

    @Override
    public void onColorChanged(int color) {
        // Save selected color
        mSelectedColor = color;

        r = (color >> 16) & 0xFF;
        g = (color >> 8) & 0xFF;
        b = (color >> 0) & 0xFF;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if(event.getAction() == MotionEvent.ACTION_UP)
        {
            sendHandler.removeCallbacksAndMessages(sendTimerRunnable);
            sendHandler.postDelayed(sendTimerRunnable, 100);
            mColorPicker.setOldCenterColor(mSelectedColor);
        }
        else if (event.getAction() == MotionEvent.ACTION_DOWN)
        {
            mPrevColor = mSelectedColor;
        }
        return false;
    }
}
