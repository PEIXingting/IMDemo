package hk.edu.cuhk.ie.iems5722.group5;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

public class LocationActivity extends AppCompatActivity {
    private double latitude = 0.0;
    private double longitude = 0.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.location_webview);
        WebView webView = findViewById(R.id.web_view);

        Toolbar toolbar = findViewById(R.id.location_toolbar);
        setSupportActionBar(toolbar);


        Intent intent = getIntent();
        int type = intent.getIntExtra("type",0);

        int selectLocation = 0;
        int showLocation = 1;
        if (type == selectLocation) {
            LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION},1);
                }
                Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                if (location != null) {
                    latitude = location.getLatitude();
                    longitude = location.getLongitude();
                }
            } else {
                LocationListener locationListener = new LocationListener() {
                    @Override
                    public void onLocationChanged(Location location) {
                        if (location != null) {
                            Log.e("Map", "Location changed: Lat: " + location.getLatitude() + "Lng: " + location.getLongitude());
                        }
                    }

                    @Override
                    public void onStatusChanged(String s, int i, Bundle bundle) {

                    }

                    @Override
                    public void onProviderEnabled(String s) {

                    }

                    @Override
                    public void onProviderDisabled(String s) {

                    }
                };
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,1000,0,locationListener);
                Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                if (location != null) {
                    latitude = location.getLatitude();
                    longitude = location.getLongitude();
                }
            }

            String mUrl = "https://apis.map.qq.com/tools/locpicker?search=1&type=0&zoom=18&coordtype=1&coord=" + latitude + "," + longitude + "&backurl=http://callback&key=EOEBZ-FOHAW-A3GRM-OYLTD-BA3JV-H4BMU&referer=myapp";

            webView.getSettings().setJavaScriptEnabled(true);
            webView.setWebViewClient(new WebViewClient() {
                @Override
                public boolean shouldOverrideUrlLoading(WebView view, String url) {
                    if (!url.startsWith("http://callback")) {
                        view.loadUrl(url);
                    } else {
                        try {
                            String decode = URLDecoder.decode(url, "UTF-8");
                            Uri uri = Uri.parse(decode);
                            String latng = uri.getQueryParameter("latng");
                            String name = uri.getQueryParameter("name");
                            String address = uri.getQueryParameter("addr");
                            Intent intent1 = new Intent();
                            intent1.putExtra("latng", latng);
                            intent1.putExtra("name", name);
                            intent1.putExtra("address", address);
                            setResult(200, intent1);
                            finish();
                            overridePendingTransition(R.anim.return_enter, R.anim.return_exit);

                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }
                    }
                    return true;
                }
            });
            webView.loadUrl(mUrl);
        } else if (type == showLocation) {
            String mUrl = intent.getStringExtra("url");
            webView.getSettings().setJavaScriptEnabled(true);
            webView.setWebViewClient(new WebViewClient());
            webView.loadUrl(mUrl);
        }

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Intent intent1 = new Intent();
//                setResult(0, intent1);
                finish();
                overridePendingTransition(R.anim.return_enter, R.anim.return_exit);

            }
        });


    }
}
