package com.sukakasir.app;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.bluetooth.BluetoothAdapter;
import android.webkit.JavascriptInterface;
import android.webkit.PermissionRequest;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

public class MainActivity extends AppCompatActivity {
    private WebView myWebView;
    private BluetoothAdapter bluetoothAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mintaIzinSistem();

        myWebView = findViewById(R.id.webview_compontent);
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        WebSettings settings = myWebView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true); // Fix Login Pop-up
        settings.setDatabaseEnabled(true);
        settings.setAllowFileAccess(true);

        // Proteksi: Matikan klik kanan / tahan (Anti View Source)
        myWebView.setOnLongClickListener(v -> true);
        myWebView.setLongClickable(false);

        // Jembatan: Panggil di JS pakai AndroidInterface.turnOnBluetooth()
        myWebView.addJavascriptInterface(new WebAppInterface(), "AndroidInterface");

        myWebView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                String url = request.getUrl().toString();
                if (url.startsWith("whatsapp:") || url.contains("wa.me")) {
                    try {
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
                        return true;
                    } catch (Exception e) { return false; }
                }
                return false;
            }
        });

        myWebView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onPermissionRequest(final PermissionRequest request) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    request.grant(request.getResources());
                }
            }
        });

        myWebView.loadUrl("file:///android_asset/index.html");
    }

    public class WebAppInterface {
        @JavascriptInterface
        public void turnOnBluetooth() {
            if (bluetoothAdapter == null) {
                runOnUiThread(() -> Toast.makeText(MainActivity.this, "HP tidak support Bluetooth", Toast.LENGTH_SHORT).show());
            } else {
                if (!bluetoothAdapter.isEnabled()) {
                    // Cara resmi Android terbaru untuk minta nyalakan BT
                    Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED || Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
                        startActivity(enableBtIntent);
                    }
                } else {
                    runOnUiThread(() -> Toast.makeText(MainActivity.this, "Bluetooth Sudah Aktif & Siap Print", Toast.LENGTH_SHORT).show());
                }
            }
        }
    }

    private void mintaIzinSistem() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ActivityCompat.requestPermissions(this, new String[]{
                Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.ACCESS_FINE_LOCATION
            }, 1);
        } else {
            ActivityCompat.requestPermissions(this, new String[]{
                Manifest.permission.BLUETOOTH, Manifest.permission.ACCESS_FINE_LOCATION
            }, 1);
        }
    }
}
