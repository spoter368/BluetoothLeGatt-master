package com.amti.vela.bluetoothlegatt;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class DeviceFragment extends android.support.v4.app.Fragment{
    public static final int LOW_BATTERY_THRESHOLD = 20;

    AlertDialog.Builder builder;
    EditText input;
    ScrollView scrollView;
    ImageView batteryFiller;
    ImageView criticalFiller;
    TextView batteryText;
    TextView deviceNameText;
    Button editButton;
    Spinner msgTypeSpinner;
    Button chooseContactButton;
    EditText msgText;
    TextView toText;

    String contactNumber;
    String contactName;
    String text;

    static final int PICK_CONTACT=1;
    SharedPreferences prefs;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ScrollView scrollView = initGui(inflater, container);
        return scrollView;
    }

    ScrollView initGui(LayoutInflater inflater, ViewGroup container)
    {
        prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());

        scrollView = (ScrollView)inflater.inflate(R.layout.fragment_device, container, false);
        batteryFiller = (ImageView) scrollView.findViewById(R.id.battery_filler);
        criticalFiller = (ImageView) scrollView.findViewById(R.id.critical_filler);
        batteryText = (TextView) scrollView.findViewById(R.id.battery_text);
        deviceNameText = (TextView) scrollView.findViewById(R.id.device_name);
        editButton = (Button) scrollView.findViewById(R.id.deviceNameEdit);
        msgTypeSpinner = (Spinner) scrollView.findViewById(R.id.msgType);
        chooseContactButton = (Button) scrollView.findViewById(R.id.chooseContact);
        msgText = (EditText) scrollView.findViewById(R.id.messageEditText);
        toText = (TextView) scrollView.findViewById(R.id.toText);

        builder = new AlertDialog.Builder(getActivity());

        // Set up the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String newName = input.getText().toString();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                builder.setTitle("Type a device name");

                // Set up the input
                input = new EditText(getActivity());
                builder.setView(input);

                builder.show();

                input.requestFocus();
                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
            }
        });

        msgText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                prefs.edit().putString(Preferences.PREFS_SMS_TEXT, msgText.getText().toString()).apply();
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        List<String> list = new ArrayList<String>();
        list.add("SMS Text Message");
        list.add("Hangouts Message");
        list.add("Email");
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, list);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        msgTypeSpinner.setAdapter(dataAdapter);

        msgTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        chooseContactButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
                startActivityForResult(intent, PICK_CONTACT);
            }
        });

        contactNumber = prefs.getString(Preferences.PREFS_SMS_NUMBER, "");
        contactName = prefs.getString(Preferences.PREFS_SMS_NAME, "");
        msgText.setText(prefs.getString(Preferences.PREFS_SMS_TEXT, ""));
        toText.setText("To: " + contactName);


        updateBattery(0);

        return scrollView;
    }

    public void updateBattery(int batteryLevel)
    {
        int px;
        if(batteryLevel <= LOW_BATTERY_THRESHOLD)
        {
            float batteryFactor = batteryLevel / 100.0f;
            int defaultWidth = (int) getResources().getDimension(R.dimen.battery_filler_default_width);
            px = (int) (batteryFactor * defaultWidth + 0.5f);
            criticalFiller.getLayoutParams().width = px;
            criticalFiller.requestLayout();

            batteryFiller.setVisibility(View.GONE);
            criticalFiller.setVisibility(View.VISIBLE);
        }
        else
        {
            float batteryFactor = batteryLevel / 100.0f;
            int defaultWidth = (int) getResources().getDimension(R.dimen.battery_filler_default_width);
            px = (int) (batteryFactor * defaultWidth + 0.5f);
            batteryFiller.getLayoutParams().width = px;
            batteryFiller.requestLayout();

            batteryFiller.setVisibility(View.VISIBLE);
            criticalFiller.setVisibility(View.GONE);
        }

        batteryText.setText(Integer.toString(batteryLevel) + "%");
    }

    public void setDevice(String name)
    {
        deviceNameText.setText(name);
    }

    public String getContactNumber()
    {
        return contactNumber;
    }

    public String getMsgText()
    {
        return msgText.getText().toString();
    }

    @Override
    public void onActivityResult(int reqCode, int resultCode, Intent data) {
        super.onActivityResult(reqCode, resultCode, data);
        switch (reqCode) {
            case (PICK_CONTACT):
                if (resultCode == Activity.RESULT_OK) {

                    Uri contactData = data.getData();
                    Cursor c = getActivity().managedQuery(contactData, null, null, null, null);
                    if (c.moveToFirst()) {
                        String id = c.getString(c.getColumnIndexOrThrow(ContactsContract.Contacts._ID));
                        String hasPhone = c.getString(c.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER));
                        String cNumber = null;
                        if (hasPhone.equalsIgnoreCase("1")) {
                            Cursor phones = getActivity().getContentResolver().query(
                                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                                    ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = " + id,
                                    null, null);
                            phones.moveToFirst();
                            cNumber = phones.getString(phones.getColumnIndex("data1"));
                            System.out.println("number is:" + cNumber);
                        }
                        contactName = c.getString(c.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                        toText.setText("To: " + contactName);
                        contactNumber = cNumber;

                        if(contactNumber == null || contactName == null)
                        {
                            Toast.makeText(getActivity(), "You selected an invalid SMS contact", Toast.LENGTH_LONG).show();
                            contactNumber = null;
                            msgText.setText("");
                        }

                        prefs.edit().putString(Preferences.PREFS_SMS_NUMBER, contactNumber).apply();
                        prefs.edit().putString(Preferences.PREFS_SMS_NAME, contactName).apply();
                    }
                }
                break;
        }
    }
}
