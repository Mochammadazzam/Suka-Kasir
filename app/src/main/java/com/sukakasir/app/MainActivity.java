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

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.File;
import java.io.FileOutputStream;

public class MainActivity extends AppCompatActivity {

    private WebView webView;
    private BluetoothAdapter bluetoothAdapter;
    private static final int PERMISSION_CODE = 123;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Inisialisasi Bluetooth Adapter
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        
        // Minta Izin Sistem saat aplikasi pertama kali dibuka
        mintaIzinSistem();

        webView = findViewById(R.id.webview_compontent);
        WebSettings settings = webView.getSettings();
        
        // Konfigurasi WebView agar mendukung JavaScript dan fitur modern
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setAllowFileAccess(true);
        settings.setAllowContentAccess(true);
        settings.setDatabaseEnabled(true);
        settings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        
        // Responsivitas tampilan
        settings.setUseWideViewPort(true);
        settings.setLoadWithOverviewMode(true);
        settings.setSupportZoom(false);

        // DAFTARKAN INTERFACE: AndroidInterface (Nama ini wajib sama dengan di JavaScript kamu)
        webView.addJavascriptInterface(new WebAppInterface(), "AndroidInterface");

        // Menangani permintaan izin dari browser (seperti Bluetooth API di web)
        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onPermissionRequest(final PermissionRequest request) {
                MainActivity.this.runOnUiThread(() -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        request.grant(request.getResources());
                    }
                });
            }
        });

        webView.setWebViewClient(new WebViewClient());
        
        // Memuat file HTML dari folder Assets
        webView.loadUrl("file:///android_asset/index.html");
    }

    // --- CLASS UNTUK KOMUNIKASI WEB KE ANDROID ---
    public class WebAppInterface {

        @JavascriptInterface
        public void turnOnBluetooth() {
            if (bluetoothAdapter == null) {
                tampilkanPesan("HP tidak mendukung Bluetooth");
                return;
            }

            if (!bluetoothAdapter.isEnabled()) {
                // Cek izin BLUETOOTH_CONNECT untuk Android 12+
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED) {
                        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                        startActivity(enableBtIntent);
                    } else {
                        mintaIzinSistem();
                    }
                } else {
                    Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivity(enableBtIntent);
                }
            }
        }

        @JavascriptInterface
        public void saveImageToGallery(String base64Data, String fileName) {
            try {
                // Proses konversi Base64 ke Byte Array
                String pureBase64 = base64Data.substring(base64Data.indexOf(",") + 1);
                byte[] decodedBytes = Base64.decode(pureBase64, Base64.DEFAULT);

                // Lokasi folder Download (Agar mudah dicari user)
                File downloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                if (!downloadDir.exists()) downloadDir.mkdirs();

                File file = new File(downloadDir, fileName);

                FileOutputStream fos = new FileOutputStream(file);
                fos.write(decodedBytes);
                fos.flush();
                fos.close();

                // Notifikasi ke Galeri Android agar file baru langsung muncul
                Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                Uri contentUri = Uri.fromFile(file);
                mediaScanIntent.setData(contentUri);
                sendBroadcast(mediaScanIntent);

                tampilkanPesan("Berhasil: " + fileName + " tersimpan di folder Download");

            } catch (Exception e) {
                tampilkanPesan("Gagal menyimpan gambar: " + e.getMessage());
            }
        }
    }

    // Fungsi pembantu untuk Toast di thread UI
    private void tampilkanPesan(String pesan) {
        MainActivity.this.runOnUiThread(() -> 
            Toast.makeText(MainActivity.this, pesan, Toast.LENGTH_LONG).show()
        );
    }

    // --- LOGIKA IZIN SISTEM ---
    private void mintaIzinSistem() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // Izin khusus Android 12 ke atas (Scan & Connect)
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.BLUETOOTH_CONNECT,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
            }, PERMISSION_CODE);
        } else {
            // Izin untuk Android 11 ke bawah
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE
            }, PERMISSION_CODE);
        }
    }

    // Handler Tombol Back
    @Override
    public void onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }
}
