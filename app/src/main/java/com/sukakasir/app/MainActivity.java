package com.sukakasir.app;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.PermissionRequest;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

public class MainActivity extends AppCompatActivity {

    private WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mintaIzinSistem();

        webView = findViewById(R.id.webview_compontent);
        WebSettings settings = webView.getSettings();
        
        // Keamanan & Database
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setDatabaseEnabled(true);
        settings.setAllowFileAccess(true);
        
        // Anti View Source / Long Click
        webView.setOnLongClickListener(v -> true);
        webView.setLongClickable(false);
        webView.setHapticFeedbackEnabled(false);

        // Jembatan JavaScript ke Java (AndroidBridge)
        webView.addJavascriptInterface(new WebAppInterface(), "AndroidBridge");

        webView.setWebViewClient(new WebViewClient());
        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onPermissionRequest(final PermissionRequest request) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    request.grant(request.getResources());
                }
            }
        });

        webView.loadUrl("file:///android_asset/index.html");
    }

    // Class Jembatan untuk dipanggil dari HTML
    public class WebAppInterface {
        @JavascriptInterface
        public void toast(String msg) {
            Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
        }

        @JavascriptInterface
        public void checkBluetooth() {
            BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
            if (adapter == null) {
                toast("HP ini tidak support Bluetooth");
            } else if (!adapter.isEnabled()) {
                toast("Tolong aktifkan Bluetooth Anda!");
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED) {
                    startActivity(enableBtIntent);
                }
            } else {
                toast("Bluetooth Aktif & Siap Koneksi");
            }
        }
    }

    private void mintaIzinSistem() {
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
