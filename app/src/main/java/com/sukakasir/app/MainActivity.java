package com.sukakasir.app;

import android.Manifest;
import android.app.Activity; // Menggunakan Activity standar
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Base64;
import android.webkit.JavascriptInterface;
import android.webkit.PermissionRequest;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;

public class MainActivity extends Activity { // Tidak lagi pakai AppCompatActivity

    private WebView webView;
    private BluetoothAdapter bluetoothAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        try {
            setContentView(R.layout.activity_main);

            webView = findViewById(R.id.webview_compontent);
            bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

            if (webView != null) {
                setupWebView();
            } else {
                Toast.makeText(this, "Layout Error: WebView tidak ditemukan", Toast.LENGTH_LONG).show();
            }

            mintaIzinSistem();

        } catch (Exception e) {
            Toast.makeText(this, "Startup Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void setupWebView() {
        WebSettings s = webView.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setDatabaseEnabled(true);
        s.setAllowFileAccess(true);
        s.setAllowContentAccess(true);
        s.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);

        webView.addJavascriptInterface(new WebAppInterface(), "AndroidInterface");

        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onPermissionRequest(final PermissionRequest request) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    request.grant(request.getResources());
                }
            }
        });

        webView.setWebViewClient(new WebViewClient());
        webView.loadUrl("file:///android_asset/index.html");
    }

    public class WebAppInterface {
        @JavascriptInterface
        public void turnOnBluetooth() {
            if (bluetoothAdapter != null && !bluetoothAdapter.isEnabled()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    if (checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED) {
                        startActivity(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE));
                    }
                } else {
                    startActivity(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE));
                }
            }
        }

        @JavascriptInterface
        public void saveImageToGallery(String base64Data, String fileName) {
            try {
                String pureBase64 = base64Data.substring(base64Data.indexOf(",") + 1);
                byte[] decodedBytes = Base64.decode(pureBase64, Base64.DEFAULT);

                File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                File file = new File(path, fileName);
                
                FileOutputStream fos = new FileOutputStream(file);
                fos.write(decodedBytes);
                fos.close();

                sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(file)));
                
                runOnUiThread(() -> Toast.makeText(MainActivity.this, "Nota tersimpan di Downloads", Toast.LENGTH_SHORT).show());
            } catch (Exception e) {
                runOnUiThread(() -> Toast.makeText(MainActivity.this, "Gagal simpan: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        }
    }

    private void mintaIzinSistem() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                requestPermissions(new String[]{
                    Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.BLUETOOTH_CONNECT,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                }, 1);
            } else {
                requestPermissions(new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                }, 1);
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (webView != null && webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }
}
