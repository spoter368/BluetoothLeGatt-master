package com.amti.vela.bluetoothlegatt;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;

public class DevFragment extends android.support.v4.app.Fragment {
    RelativeLayout relativeLayout;
    CheckBox checkD, checkE, checkP;

    Handler btHandler;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        RelativeLayout relativeLayout = initGui(inflater, container);
        MainActivity mainActivity = (MainActivity)getActivity();
        btHandler = mainActivity.getBtHandler();

        return relativeLayout;
    }

    RelativeLayout initGui(LayoutInflater inflater, ViewGroup container)
    {
        relativeLayout = (RelativeLayout)inflater.inflate(R.layout.fragment_dev, container, false);
        checkD = (CheckBox)relativeLayout.findViewById(R.id.checkBoxD);
        checkE = (CheckBox)relativeLayout.findViewById(R.id.checkBoxE);
        checkP = (CheckBox)relativeLayout.findViewById(R.id.checkBoxP);

        checkD.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                sendHandlerMsg(isChecked ? "D" : "d");
                checkD.setText(isChecked ? "D" : "d");
            }
        });

        checkE.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                sendHandlerMsg(isChecked ? "E" : "e");
                checkE.setText(isChecked ? "E" : "e");
            }
        });

        checkP.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                sendHandlerMsg(isChecked ? "P" : "p");
                checkP.setText(isChecked ? "P" : "p");
            }
        });

        checkD.setChecked(true);
        checkE.setChecked(true);
        checkP.setChecked(true);

        return relativeLayout;
    }

    void sendHandlerMsg(String val)
    {
        Message msg = btHandler.obtainMessage(MainActivity.SEND_DEP_VALUE);
        msg.obj = val;
        btHandler.sendMessage(msg);
    }
}
