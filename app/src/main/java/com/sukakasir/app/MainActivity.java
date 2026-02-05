package com.sukakasir.app;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.util.Base64;
import android.webkit.JavascriptInterface;
import android.webkit.PermissionRequest;
import android.webkit.WebChromeClient;
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
    private BluetoothAdapter bluetoothAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        mintaIzinSistem();

        // ID harus 'webview_compontent' sesuai kodingan kita sebelumnya
        webView = findViewById(R.id.webview_compontent);
        WebSettings settings = webView.getSettings();
        
        // --- POWER SETTINGS ---
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true); // Wajib untuk Firebase Login
        settings.setAllowFileAccess(true);
        settings.setAllowContentAccess(true);
        settings.setCacheMode(WebSettings.LOAD_DEFAULT);

        // --- BRIDGE: HTML memanggil AndroidInterface.turnOnBluetooth() ---
        webView.addJavascriptInterface(new WebAppInterface(), "AndroidInterface");

        // --- HANDLING REDIRECT (WA & URL) ---
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (url.startsWith("whatsapp:") || url.contains("wa.me")) {
                    try {
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
                        return true;
                    } catch (Exception e) {
                        Toast.makeText(MainActivity.this, "WhatsApp tidak ditemukan", Toast.LENGTH_SHORT).show();
                        return true;
                    }
                }
                return false;
            }
        });

        // --- HANDLING PERMISSION (Bluetooth Web API) ---
        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onPermissionRequest(final PermissionRequest request) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    runOnUiThread(() -> request.grant(request.getResources()));
                }
            }
        });

        // --- HANDLING DOWNLOAD (Nota JPG) ---
        webView.setDownloadListener((url, userAgent, contentDisposition, mimetype, contentLength) -> {
            if (url.startsWith("data:")) {
                simpanNotaKeFolderDownload(url, mimetype);
            }
        });

        webView.loadUrl("file:///android_asset/index.html");
    }

    // Jembatan Java - JavaScript
    public class WebAppInterface {
        @JavascriptInterface
        public void turnOnBluetooth() {
            if (bluetoothAdapter == null) {
                runOnUiThread(() -> Toast.makeText(MainActivity.this, "Device tidak support Bluetooth", Toast.LENGTH_SHORT).show());
            } else if (!bluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED || Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
                    startActivity(enableBtIntent);
                }
            }
        }
    }

    private void simpanNotaKeFolderDownload(String dataUrl, String mimeType) {
        try {
            String base64Data = dataUrl.substring(dataUrl.indexOf(",") + 1);
            byte[] fileBytes = Base64.decode(base64Data, Base64.DEFAULT);
            String fileName = "Nota_Laundry_" + System.currentTimeMillis() + ".jpg";
            
            File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            File file = new File(path, fileName);
            
            OutputStream os = new FileOutputStream(file);
            os.write(fileBytes);
            os.close();
            
            Toast.makeText(this, "Nota Tersimpan di Download", Toast.LENGTH_LONG).show();
            
            // Scan agar file langsung muncul di Gallery/File Manager
            Intent scanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            scanIntent.setData(Uri.fromFile(file));
            sendBroadcast(scanIntent);
        } catch (Exception e) {
            Toast.makeText(this, "Gagal simpan nota", Toast.LENGTH_SHORT).show();
        }
    }

    private void mintaIzinSistem() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ActivityCompat.requestPermissions(this, new String[]{
                Manifest.permission.BLUETOOTH_SCAN, 
                Manifest.permission.BLUETOOTH_CONNECT, 
                Manifest.permission.ACCESS_FINE_LOCATION
            }, 1);
        } else {
            ActivityCompat.requestPermissions(this, new String[]{
                Manifest.permission.BLUETOOTH, 
                Manifest.permission.ACCESS_FINE_LOCATION
            }, 1);
        }
    }
}
