package com.dev_guy_jay.smsreader;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.AdapterView;

import java.util.ArrayList;


public class MainActivity extends AppCompatActivity {
    ListView listView;
    private static final int PERMISSION_REQUEST_READ_CONTACTS = 100;
    static ArrayList<String> contactList;
    ArrayList<String>mNumberList;
    static ArrayList<String> numberList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        listView = (ListView) findViewById(R.id.idList);

        int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS);

        if(permissionCheck== PackageManager.PERMISSION_GRANTED){
            showContacts();
        }else{
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_SMS}, PERMISSION_REQUEST_READ_CONTACTS);
        }
    }

    protected void onStart() {
        super.onStart();

        int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS);

        if(permissionCheck== PackageManager.PERMISSION_GRANTED){
            showContacts();
        }else{
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_SMS}, PERMISSION_REQUEST_READ_CONTACTS);
        }

    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if(requestCode == PERMISSION_REQUEST_READ_CONTACTS) {
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
                //Permission is granted
                showContacts();
            }else{
                Toast.makeText(this, "Until you grant the permission, we cannot display the names", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void showContacts() {
        //Make the Inbox box URI
        Uri inboxURI = Uri.parse("content://sms/inbox");
        Uri a = Uri.parse("");
        contactList = new ArrayList<String>();
        mNumberList = new ArrayList<String>();
        numberList = new ArrayList<String>();
        ContentResolver cr = getContentResolver();

        //Fetch Inbox SMS
        Cursor c = cr.query(inboxURI, null, null, null, null);
        while(c.moveToNext()) {
            boolean isInList = false;

            final String Number = c.getString(c.getColumnIndexOrThrow("address")).toString();

            for (String num : numberList) {
                if(num == null){
                    numberList.add(Number);
                }
                if(Number.equals(num)) {
                    isInList = true;
                    break;
                }
            }
            if(isInList){
                continue;
            }
            numberList.add(Number);
            final String Body = c.getString(c.getColumnIndexOrThrow("body")).toString();
            contactList.add(getContactName(getApplicationContext(), Number)+ ": " +Number + "\n" + "\n" + Body + "\n");
            mNumberList.add(Number);
        }
        c.close();
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, contactList);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l){
                Intent i = new Intent(getBaseContext(), ContactMessage.class);
                i.putExtra("number", mNumberList.get(position));
                startActivity(i);
            }
        });
        listView.setAdapter(adapter);
    }

    public void addContact(String Number) {
        numberList.add(Number);
        //tv.setText(Number);
    }

    public String getContactName(Context context, String phoneNumber) {
        String contactName = null;
        ContentResolver cr = context.getContentResolver();
        //Create the Lookup Uri
        Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
                Uri.encode(phoneNumber));
        Cursor cursor = cr.query(uri, new String[] { ContactsContract.PhoneLookup.DISPLAY_NAME }, null, null, null);
        //if contact number does not have a name
        if (cursor == null) {
            return "";
        }
        if (cursor.moveToFirst()) {
            contactName = cursor.getString(cursor.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME));
        }
        if (cursor != null && !cursor.isClosed()) {
            cursor.close();
        }
        return contactName;
    }
}

