package com.dev_guy_jay.smsreader;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.telephony.SmsManager;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

/**
 * Created by dev_g on 2/24/2018.
 */

public class ContactMessage extends Activity {

    private static final int PERMISSION_REQUEST_SEND_SMS = 0;

    private String mNumber;
    ListView listView;
    Button btnSend;
    EditText mMessage;
    ArrayList<String> messageList;


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.message_list);
        listView = (ListView) findViewById(R.id.idList);
        btnSend = (Button) findViewById(R.id.Button);
        mMessage = (EditText) findViewById(R.id.EditText);

        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendSms(v);
            }
        });

        Bundle extras = getIntent().getExtras();
        if(extras !=null)
        {
            mNumber = extras.getString("number");
        }

        showMessages();

    }

    public void showMessages() {
        //Create Inbox box URI
        Uri inboxURI = Uri.parse("content://sms");
        messageList = new ArrayList<String>();

        ContentResolver cr = getContentResolver();

        Cursor c = cr.query(inboxURI, null, null, null, null);
        while(c.moveToNext()) {
            final String Number = c.getString(c.getColumnIndexOrThrow("address")).toString();

            if (Number.equals(mNumber)){
                final String Body = c.getString(c.getColumnIndexOrThrow("body")).toString();
                messageList.add(Body + "\n");
            }
        }
        c.close();
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, messageList);
        listView.setAdapter(adapter);
    }

    public void MyMessage() {
        String myMsg = mMessage.getText().toString().trim();
        if(TextUtils.isDigitsOnly(mNumber)){
            SmsManager smsManager =SmsManager.getDefault();
            smsManager.sendTextMessage(mNumber, null, myMsg, null, null);
            Toast.makeText(this, "Message sent", Toast.LENGTH_SHORT).show();
        }else{
            Toast.makeText(this, "Error while trying to send message", Toast.LENGTH_SHORT).show();
        }

        mMessage.setText("");
        Intent i = new Intent(getBaseContext(), MainActivity.class);
        startActivity(i);
    }

    public void sendSms (View v) {
        int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS);

        if(permissionCheck== PackageManager.PERMISSION_GRANTED){
            //Name of Method for calling message
            MyMessage();
        }else{
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_SMS}, PERMISSION_REQUEST_SEND_SMS);
        }
    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if(requestCode == PERMISSION_REQUEST_SEND_SMS) {
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
                //Permission is granted
                MyMessage();
            }else{
                Toast.makeText(this, "Until you grant the permission, You cannot send messages", Toast.LENGTH_SHORT).show();
            }
        }
    }

}
