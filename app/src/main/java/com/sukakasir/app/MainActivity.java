package com.sukakasir.app;

import android.Manifest;
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

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.File;
import java.io.FileOutputStream;

public class MainActivity extends AppCompatActivity {

    private WebView webView;
    private BluetoothAdapter bluetoothAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        try {
            // Memastikan layout dipanggil dengan benar
            setContentView(R.layout.activity_main);

            // Inisialisasi Bluetooth
            bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            mintaIzinSistem();

            // Inisialisasi WebView dengan ID yang ada di activity_main.xml
            webView = findViewById(R.id.webview_compontent);
            
            if (webView == null) {
                tampilkanPesan("Error: ID webview_compontent tidak ditemukan di layout");
                return;
            }

            konfigurasiWebView();

        } catch (Exception e) {
            // Jika crash saat startup, pesan ini akan muncul di HP
            tampilkanPesan("Gagal memuat aplikasi: " + e.getMessage());
        }
    }

    private void konfigurasiWebView() {
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setDatabaseEnabled(true);
        settings.setAllowFileAccess(true);
        settings.setAllowContentAccess(true);
        settings.setLoadWithOverviewMode(true);
        settings.setUseWideViewPort(true);
        settings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);

        // Menghubungkan ke JavaScript (AndroidInterface)
        webView.addJavascriptInterface(new WebAppInterface(), "AndroidInterface");

        // Menangani izin fitur web (seperti Bluetooth)
        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onPermissionRequest(final PermissionRequest request) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    request.grant(request.getResources());
                }
            }
        });

        webView.setWebViewClient(new WebViewClient());
        
        // Memuat file dari assets
        webView.loadUrl("file:///android_asset/index.html");
    }

    // --- Jembatan JavaScript ke Android ---
    public class WebAppInterface {

        @JavascriptInterface
        public void turnOnBluetooth() {
            if (bluetoothAdapter != null && !bluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED || Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
                    startActivity(enableBtIntent);
                } else {
                    tampilkanPesan("Izin Bluetooth Connect tidak diberikan");
                }
            }
        }

        @JavascriptInterface
        public void saveImageToGallery(String base64Data, String fileName) {
            try {
                String pureBase64 = base64Data.substring(base64Data.indexOf(",") + 1);
                byte[] decodedBytes = Base64.decode(pureBase64, Base64.DEFAULT);

                File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                if (!path.exists()) path.mkdirs();
                
                File file = new File(path, fileName);
                FileOutputStream fos = new FileOutputStream(file);
                fos.write(decodedBytes);
                fos.flush();
                fos.close();

                // Refresh galeri
                Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                intent.setData(Uri.fromFile(file));
                sendBroadcast(intent);

                tampilkanPesan("Nota disimpan di: " + file.getAbsolutePath());
            } catch (Exception e) {
                tampilkanPesan("Gagal simpan gambar: " + e.getMessage());
            }
        }
    }

    private void mintaIzinSistem() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ActivityCompat.requestPermissions(this, new String[]{
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE
            }, 1);
        } else {
            ActivityCompat.requestPermissions(this, new String[]{
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE
            }, 1);
        }
    }

    private void tampilkanPesan(final String pesan) {
        runOnUiThread(() -> Toast.makeText(MainActivity.this, pesan, Toast.LENGTH_LONG).show());
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
