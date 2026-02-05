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
import com.sukakasir.app.R;
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

        webView = findViewById(R.id.webview_compontent);
        WebSettings settings = webView.getSettings();
        
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setAllowFileAccess(true);
        settings.setDatabaseEnabled(true);

        // Bridge Java ke JavaScript
        webView.addJavascriptInterface(new WebAppInterface(), "AndroidInterface");

        // FIX: Agar daftar Bluetooth muncul (OnPermissionRequest)
        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onPermissionRequest(final PermissionRequest request) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    runOnUiThread(() -> request.grant(request.getResources()));
                }
            }
        });

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (url.startsWith("whatsapp:") || url.contains("wa.me")) {
                    try {
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
                        return true;
                    } catch (Exception e) {
                        Toast.makeText(MainActivity.this, "WA tidak ditemukan", Toast.LENGTH_SHORT).show();
                        return true;
                    }
                }
                return false;
            }
        });

        webView.setDownloadListener((url, userAgent, contentDisposition, mimetype, contentLength) -> {
            if (url.startsWith("data:")) {
                simpanNota(url);
            }
        });

        webView.loadUrl("file:///android_asset/index.html");
    }

    public class WebAppInterface {
        @JavascriptInterface
        public void turnOnBluetooth() {
            if (bluetoothAdapter != null && !bluetoothAdapter.isEnabled()) {
                Intent it = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED || Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
                    startActivity(it);
                }
            }
        }
    }

    private void simpanNota(String dataUrl) {
        try {
            String base64Data = dataUrl.substring(dataUrl.indexOf(",") + 1);
            byte[] bytes = Base64.decode(base64Data, Base64.DEFAULT);
            File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            File file = new File(path, "Nota_" + System.currentTimeMillis() + ".jpg");
            OutputStream os = new FileOutputStream(file);
            os.write(bytes);
            os.close();
            Toast.makeText(this, "Nota disimpan di Download", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "Gagal simpan", Toast.LENGTH_SHORT).show();
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
                Manifest.permission.ACCESS_FINE_LOCATION
            }, 1);
        }
    }
}
