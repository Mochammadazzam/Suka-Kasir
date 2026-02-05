package com.sukakasir.app;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.util.Base64;
import android.webkit.PermissionRequest;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

public class MainActivity extends AppCompatActivity {

    private WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Langsung minta izin saat aplikasi dibuka
        mintaIzinSistem();

        webView = findViewById(R.id.webview_compontent);
        WebSettings settings = webView.getSettings();
        
        // --- SETTINGAN AGAR BLUETOOTH JALAN ---
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setAllowFileAccess(true);
        settings.setAllowContentAccess(true);
        
        // Buka blokir konten campuran (sering bikin Bluetooth gagal)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            settings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        }

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                String url = request.getUrl().toString();
                if (url.startsWith("whatsapp:") || url.contains("wa.me")) {
                    try {
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
                        return true;
                    } catch (Exception e) {
                        return false;
                    }
                }
                return false; 
            }
        });

        // --- INI KUNCI FIX BLUETOOTH ---
        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onPermissionRequest(final PermissionRequest request) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    // Memaksa WebView memberikan izin kamera/bluetooth/lokasi ke HTML
                    runOnUiThread(() -> request.grant(request.getResources()));
                }
            }
        });

        // Load HTML kamu
        webView.loadUrl("file:///android_asset/index.html");
    }

    private void mintaIzinSistem() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ActivityCompat.requestPermissions(this, new String[]{
                Manifest.permission.BLUETOOTH_SCAN, 
                Manifest.permission.BLUETOOTH_CONNECT, 
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            }, 1);
        } else {
            ActivityCompat.requestPermissions(this, new String[]{
                Manifest.permission.BLUETOOTH, 
                Manifest.permission.BLUETOOTH_ADMIN, 
                Manifest.permission.ACCESS_FINE_LOCATION
            }, 1);
        }
    }
}
