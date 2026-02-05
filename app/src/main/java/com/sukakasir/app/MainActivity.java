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

// Import R secara manual untuk mengatasi error "package R does not exist"
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
        mintaSemuaIzin();

        webView = findViewById(R.id.webview_compontent);
        WebSettings settings = webView.getSettings();
        
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setAllowFileAccess(true);
        settings.setDatabaseEnabled(true);
        settings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);

        webView.addJavascriptInterface(new WebAppInterface(), "AndroidInterface");

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (url.startsWith("whatsapp:") || url.contains("wa.me")) {
                    try {
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
                        return true;
                    } catch (Exception e) {
                        Toast.makeText(MainActivity.this, "WhatsApp tidak terpasang", Toast.LENGTH_SHORT).show();
                        return true;
                    }
                }
                return false;
            }
        });

        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onPermissionRequest(final PermissionRequest request) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    runOnUiThread(() -> request.grant(request.getResources()));
                }
            }
        });

        webView.setDownloadListener((url, userAgent, contentDisposition, mimetype, contentLength) -> {
            if (url.startsWith("data:")) {
                simpanKeFolderDownload(url);
            }
        });

        webView.loadUrl("file:///android_asset/index.html");
    }

    public class WebAppInterface {
        @JavascriptInterface
        public void turnOnBluetooth() {
            if (bluetoothAdapter != null && !bluetoothAdapter.isEnabled()) {
                if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED || Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
                    Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivity(enableBtIntent);
                }
            }
        }
    }

    private void simpanKeFolderDownload(String dataUrl) {
        try {
            String base64Data = dataUrl.substring(dataUrl.indexOf(",") + 1);
            byte[] fileBytes = Base64.decode(base64Data, Base64.DEFAULT);
            String fileName = "Nota_" + System.currentTimeMillis() + ".jpg";
            
            File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            File file = new File(path, fileName);
            
            OutputStream os = new FileOutputStream(file);
            os.write(fileBytes);
            os.close();
            
            Toast.makeText(this, "Nota tersimpan di Download", Toast.LENGTH_LONG).show();
            
            Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            intent.setData(Uri.fromFile(file));
            sendBroadcast(intent);
        } catch (Exception e) {
            Toast.makeText(this, "Gagal simpan file", Toast.LENGTH_SHORT).show();
        }
    }

    private void mintaSemuaIzin() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ActivityCompat.requestPermissions(this, new String[]{
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.ACCESS_FINE_LOCATION
            }, 101);
        } else {
            ActivityCompat.requestPermissions(this, new String[]{
                Manifest.permission.ACCESS_FINE_LOCATION
            }, 101);
        }
    }
}
