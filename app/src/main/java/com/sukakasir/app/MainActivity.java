package com.sukakasir.app;

import android.Manifest;
import android.os.Build;
import android.os.Bundle;
import android.webkit.PermissionRequest;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

public class MainActivity extends AppCompatActivity {

    private WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Minta Izin Bluetooth & Lokasi (Wajib untuk koneksi printer)
        mintaIzinDulu();

        webView = findViewById(R.id.webview_compontent);
        WebSettings settings = webView.getSettings();
        
        // --- SETTINGAN WAJIB AGAR LOGIN & DATABASE JALAN ---
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true); // AGAR LOCALSTORAGE & LOGIN BISA DISIMPAN
        settings.setDatabaseEnabled(true);
        settings.setAllowFileAccess(true);
        settings.setAllowContentAccess(true);
        settings.setJavaScriptCanOpenWindowsAutomatically(true);
        
        // --- AGAR TAMPILAN TIDAK RUSAK ---
        settings.setLoadWithOverviewMode(true);
        settings.setUseWideViewPort(true);

        webView.setWebViewClient(new WebViewClient());

        // --- JEMBATAN BLUETOOTH & NOTIF DI APK ---
        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onPermissionRequest(final PermissionRequest request) {
                // Memberikan izin otomatis saat HTML minta akses Bluetooth
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    request.grant(request.getResources());
                }
            }
        });

        webView.loadUrl("file:///android_asset/index.html");
    }

    private void mintaIzinDulu() {
        String[] perms;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            perms = new String[]{
                Manifest.permission.BLUETOOTH_SCAN, 
                Manifest.permission.BLUETOOTH_CONNECT, 
                Manifest.permission.ACCESS_FINE_LOCATION
            };
        } else {
            perms = new String[]{
                Manifest.permission.BLUETOOTH, 
                Manifest.permission.BLUETOOTH_ADMIN, 
                Manifest.permission.ACCESS_FINE_LOCATION
            };
        }
        ActivityCompat.requestPermissions(this, perms, 1);
    }
}
