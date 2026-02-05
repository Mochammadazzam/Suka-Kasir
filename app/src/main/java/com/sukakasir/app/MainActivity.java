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
import android.webkit.DownloadListener;
import android.webkit.PermissionRequest;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

public class MainActivity extends AppCompatActivity {

    private WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mintaIzinDulu();

        webView = findViewById(R.id.webview_compontent);
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setAllowFileAccess(true);
        
        // Agar bisa scan Bluetooth
        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onPermissionRequest(final PermissionRequest request) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    request.grant(request.getResources());
                }
            }
        });

        webView.setWebViewClient(new WebViewClient());

        webView.setDownloadListener(new DownloadListener() {
            @Override
            public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimetype, long contentLength) {
                if (url.startsWith("data:")) {
                    simpanBase64(url, mimetype);
                }
            }
        });

        webView.loadUrl("file:///android_asset/index.html");
    }

    private void simpanBase64(String dataUrl, String mimeType) {
        try {
            String base64Data = dataUrl.substring(dataUrl.indexOf(",") + 1);
            byte[] fileBytes = Base64.decode(base64Data, Base64.DEFAULT);
            String fileName = "Nota_" + System.currentTimeMillis() + (mimeType.contains("csv") ? ".csv" : ".jpg");
            File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            File file = new File(path, fileName);
            OutputStream os = new FileOutputStream(file);
            os.write(fileBytes);
            os.close();
            Toast.makeText(this, "Berhasil simpan ke Download", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "Gagal simpan", Toast.LENGTH_SHORT).show();
        }
    }

    private void mintaIzinDulu() {
        // Minta Izin Bluetooth & Lokasi
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ActivityCompat.requestPermissions(this, new String[]{
                Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.ACCESS_FINE_LOCATION
            }, 1);
        } else {
            ActivityCompat.requestPermissions(this, new String[]{
                Manifest.permission.BLUETOOTH, Manifest.permission.ACCESS_FINE_LOCATION
            }, 1);
        }

        // Minta Izin Storage (Pola Admin)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                Intent it = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                it.setData(Uri.parse("package:" + getPackageName()));
                startActivity(it);
            }
        }
    }
}
