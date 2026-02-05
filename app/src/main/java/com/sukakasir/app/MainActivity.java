package com.sukaadmin.app;

import android.Manifest;
import android.app.DownloadManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.util.Base64;
import android.webkit.DownloadListener;
import android.webkit.URLUtil;
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

        mintaIzinPenyimpanan();

        webView = findViewById(R.id.webview_compontent);
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setAllowFileAccess(true);

        webView.setWebViewClient(new WebViewClient());

        webView.setDownloadListener(new DownloadListener() {
            @Override
            public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimetype, long contentLength) {
                // LOGIKA ANTI-GAGAL UNTUK BASE64 (JPEG & SHEET)
                if (url.startsWith("data:")) {
                    simpanFileBase64(url, mimetype);
                } else {
                    // Logika untuk WhatsApp atau Link Standar
                    try {
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                        startActivity(intent);
                    } catch (Exception e) {
                        Toast.makeText(MainActivity.this, "Gagal membuka link", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        webView.loadUrl("file:///android_asset/index.html");
    }

    private void simpanFileBase64(String dataUrl, String mimeType) {
        try {
            // Ekstrak data base64
            String base64Data = dataUrl.substring(dataUrl.indexOf(",") + 1);
            byte[] fileBytes = Base64.decode(base64Data, Base64.DEFAULT);
            
            // Tentukan ekstensi file
            String extension = mimeType.contains("csv") ? ".csv" : ".jpg";
            String fileName = "Laporan_SukaAdmin_" + System.currentTimeMillis() + extension;
            
            File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            File file = new File(path, fileName);
            
            OutputStream os = new FileOutputStream(file);
            os.write(fileBytes);
            os.close();
            
            Toast.makeText(this, "Berhasil! Cek folder Download: " + fileName, Toast.LENGTH_LONG).show();
            
            // Beritahu sistem agar file muncul di Galeri/File Manager
            Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            intent.setData(Uri.fromFile(file));
            sendBroadcast(intent);
            
        } catch (Exception e) {
            Toast.makeText(this, "Gagal simpan file: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void mintaIzinPenyimpanan() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                intent.setData(Uri.parse("package:" + getPackageName()));
                startActivity(intent);
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
            }
        }
    }
}
