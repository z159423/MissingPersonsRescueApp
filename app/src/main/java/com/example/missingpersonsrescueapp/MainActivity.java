package com.example.missingpersonsrescueapp;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.Volley;

import net.daum.mf.map.api.CameraUpdateFactory;
import net.daum.mf.map.api.MapPOIItem;
import net.daum.mf.map.api.MapPoint;
import net.daum.mf.map.api.MapPointBounds;
import net.daum.mf.map.api.MapPolyline;
import net.daum.mf.map.api.MapReverseGeoCoder;
import net.daum.mf.map.api.MapView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Locale;


public class MainActivity extends AppCompatActivity implements MapView.MapViewEventListener, MapView.CurrentLocationEventListener, MapReverseGeoCoder.ReverseGeoCodingResultListener {

    private static final String LOG_TAG = "MainActivity";

    private static final int GPS_ENABLE_REQUEST_CODE = 2001;
    private static final int PERMISSIONS_REQUEST_CODE = 100;
    private MapPoint MARKER_POINT = null;
    String[] REQUIRED_PERMISSIONS  = {Manifest.permission.ACCESS_FINE_LOCATION};

    private MapView mMapView;

    private Button rescueRequestBtn;
    private Button reFreshBtn;
    private Button currentMyLocationBtn;
    private Button currentDroneLocationBtn;

    private TextView Lat, Lng, RoadAddress;
    private TextView droneLat, droneLng;
    private TextView distance;

    String address;

    private static String TAG = "phpexample";
    private String mJsonString, mJsonString2, mJsonString3;

    private String droneID, droneLocationLat, droneLocationLng;

    private MapPOIItem droneMarker = new MapPOIItem();

    private String android_id;

    public Context mContext;

    boolean updateData = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //getHashKey();

        android_id = Settings.Secure.getString(

                getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID);

        mContext = getApplicationContext();

        Log.d("??????????????? ????????? : ", android_id);

        mMapView = new MapView(this);

        ViewGroup mapViewContainer = (ViewGroup) findViewById(R.id.map_view);
        //mMapView.setCurrentLocationTrackingMode(MapView.CurrentLocationTrackingMode.TrackingModeOnWithHeading);
        mMapView.setMapViewEventListener(this);
        mMapView.setShowCurrentLocationMarker(true);
        mMapView.setCurrentLocationEventListener(this);

        if (!checkLocationServicesStatus()) {

            showDialogForLocationServiceSetting();
        }else {

            checkRunTimePermission();
        }

        mapViewContainer.addView(mMapView);

        rescueRequestBtn = findViewById(R.id.rescueRequestBtn);
        Lat = findViewById(R.id.Lat);
        Lng = findViewById(R.id.Lng);
        RoadAddress = findViewById(R.id.roadAddress);

        reFreshBtn = findViewById(R.id.reFreshBtn);
        currentMyLocationBtn = findViewById(R.id.currentMyLocationBtn);
        currentDroneLocationBtn = findViewById(R.id.currentDroneLocationBtn);

        droneLat = findViewById(R.id.droneLat);
        droneLng = findViewById(R.id.droneLng);

        distance = findViewById(R.id.distance);

        //???????????? ??????
        reFreshBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearDroneMarker();

                drawLowPoly();
            }
        });

        currentMyLocationBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mMapView.setMapCenterPoint(MapPoint.mapPointWithGeoCoord(Double.valueOf((String) Lat.getText()) ,Double.valueOf((String) Lng.getText())), true);
            }
        });

        currentDroneLocationBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(droneLocationLat == null || droneLocationLng == null)
                {
                    Toast.makeText(getApplicationContext(), "???????????? ????????? ????????????.", Toast.LENGTH_SHORT).show();
                    return;
                }

                mMapView.setMapCenterPoint(MapPoint.mapPointWithGeoCoord(Double.valueOf(droneLocationLat) ,Double.valueOf(droneLocationLng)), true);
            }
        });


        //=========================================???????????? ?????? ????????? ????????? ??????????????? ??????(?????? ?????? ????????? ??????)=====================================================
        rescueRequestBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                GetRescueData task = new GetRescueData();
                task.execute( "http://tmdghks992.dothome.co.kr/FindAllRescueReqeust.php", "");

            }
        });

        //php????????? ???????????? ???
        GetData task = new GetData();
        task.execute( "http://tmdghks992.dothome.co.kr/getDroneLocation.php", "");

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mMapView.setCurrentLocationTrackingMode(MapView.CurrentLocationTrackingMode.TrackingModeOff);
        mMapView.setShowCurrentLocationMarker(false);
    }

    @Override
    public void onMapViewInitialized(MapView mapView) {

    }

    @Override
    public void onMapViewCenterPointMoved(MapView mapView, MapPoint mapPoint) {

    }

    @Override
    public void onMapViewZoomLevelChanged(MapView mapView, int i) {

    }

    @Override
    public void onMapViewSingleTapped(MapView mapView, MapPoint mapPoint) {

    }

    @Override
    public void onMapViewDoubleTapped(MapView mapView, MapPoint mapPoint) {

    }

    @Override
    public void onMapViewLongPressed(MapView mapView, MapPoint mapPoint) {

    }

    @Override
    public void onMapViewDragStarted(MapView mapView, MapPoint mapPoint) {

    }

    @Override
    public void onMapViewDragEnded(MapView mapView, MapPoint mapPoint) {

    }

    @Override
    public void onMapViewMoveFinished(MapView mapView, MapPoint mapPoint) {

    }

    //=========================================??????????????? ?????? ????????? ?????????????????? ?????? ??????=====================================================
    @Override
    public void onCurrentLocationUpdate(MapView mapView, MapPoint currentLocation, float accuracyInMeters) {
        MapPoint.GeoCoordinate mapPointGeo = currentLocation.getMapPointGeoCoord();

        mapPointGeo = currentLocation.getMapPointGeoCoord();
        Lat.setText(String.valueOf(mapPointGeo.latitude));
        Lng.setText(String.valueOf(mapPointGeo.longitude));

        address = getCompleteAddressString(this, mapPointGeo.latitude, mapPointGeo.longitude);

        RoadAddress.setText(address);
        Log.i(LOG_TAG, String.format("MapView onCurrentLocationUpdate (%f,%f) accuracy (%f)", mapPointGeo.latitude, mapPointGeo.longitude, accuracyInMeters));

        if(droneLocationLat != null || droneLocationLng != null)
            drawLowPoly();
    }

    @Override
    public void onCurrentLocationDeviceHeadingUpdate(MapView mapView, float v) {

    }

    @Override
    public void onCurrentLocationUpdateFailed(MapView mapView) {

    }

    @Override
    public void onCurrentLocationUpdateCancelled(MapView mapView) {

    }

    //=========================================????????? ?????? ??????=====================================================
    void checkRunTimePermission(){

        //????????? ????????? ??????
        // 1. ?????? ???????????? ????????? ????????? ???????????????.
        int hasFineLocationPermission = ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.ACCESS_FINE_LOCATION);


        if (hasFineLocationPermission == PackageManager.PERMISSION_GRANTED ) {

            // 2. ?????? ???????????? ????????? ?????????
            // ( ??????????????? 6.0 ?????? ????????? ????????? ???????????? ???????????? ????????? ?????? ????????? ?????? ???????????????.)


            // 3.  ?????? ?????? ????????? ??? ??????
            mMapView.setCurrentLocationTrackingMode(MapView.CurrentLocationTrackingMode.TrackingModeOnWithoutHeading);


        } else {  //2. ????????? ????????? ????????? ?????? ????????? ????????? ????????? ???????????????. 2?????? ??????(3-1, 4-1)??? ????????????.

            // 3-1. ???????????? ????????? ????????? ??? ?????? ?????? ????????????
            if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, REQUIRED_PERMISSIONS[0])) {

                // 3-2. ????????? ???????????? ?????? ?????????????????? ???????????? ????????? ????????? ???????????? ????????? ????????????.
                Toast.makeText(MainActivity.this, "??? ?????? ??????????????? ?????? ?????? ????????? ???????????????.", Toast.LENGTH_LONG).show();
                // 3-3. ??????????????? ????????? ????????? ?????????. ?????? ????????? onRequestPermissionResult?????? ???????????????.
                ActivityCompat.requestPermissions(MainActivity.this, REQUIRED_PERMISSIONS,
                        PERMISSIONS_REQUEST_CODE);


            } else {
                // 4-1. ???????????? ????????? ????????? ??? ?????? ?????? ???????????? ????????? ????????? ?????? ?????????.
                // ?????? ????????? onRequestPermissionResult?????? ???????????????.
                ActivityCompat.requestPermissions(MainActivity.this, REQUIRED_PERMISSIONS,
                        PERMISSIONS_REQUEST_CODE);
            }

        }

    }

    private void showDialogForLocationServiceSetting() {

        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("?????? ????????? ????????????");
        builder.setMessage("?????? ???????????? ???????????? ?????? ???????????? ???????????????.\n"
                + "?????? ????????? ???????????????????");
        builder.setCancelable(true);
        builder.setPositiveButton("??????", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                Intent callGPSSettingIntent
                        = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivityForResult(callGPSSettingIntent, GPS_ENABLE_REQUEST_CODE);
            }
        });
        builder.setNegativeButton("??????", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });
        builder.create().show();
    }

    public boolean checkLocationServicesStatus() {
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    private void getHashKey(){      //api ????????? ???????????? ?????????
        PackageInfo packageInfo = null;
        try {
            packageInfo = getPackageManager().getPackageInfo(getPackageName(), PackageManager.GET_SIGNATURES);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        if (packageInfo == null)
            Log.e("KeyHash", "KeyHash:null");

        for (Signature signature : packageInfo.signatures) {
            try {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.d("KeyHash", Base64.encodeToString(md.digest(), Base64.DEFAULT));
            } catch (NoSuchAlgorithmException e) {
                Log.e("KeyHash", "Unable to get MessageDigest. signature=" + signature, e);
            }
        }
    }

    @Override
    public void onReverseGeoCoderFoundAddress(MapReverseGeoCoder mapReverseGeoCoder, String s) {
        mapReverseGeoCoder.toString();
        onFinishReverseGeoCoding(s);
    }

    @Override
    public void onReverseGeoCoderFailedToFindAddress(MapReverseGeoCoder mapReverseGeoCoder) {
        onFinishReverseGeoCoding("Fail");
    }

    private void onFinishReverseGeoCoding(String result) {
//        Toast.makeText(LocationDemoActivity.this, "Reverse Geo-coding : " + result, Toast.LENGTH_SHORT).show();
    }


    //========================================????????? ????????? ????????? ????????? ??????=====================================================
    public static String getCompleteAddressString(Context context, double LATITUDE, double LONGITUDE) {

        String strAdd = "";
        Geocoder geocoder = new Geocoder(context, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(LATITUDE, LONGITUDE, 1);
            if (addresses != null) {
                Address returnedAddress = addresses.get(0);
                StringBuilder strReturnedAddress = new StringBuilder("");


                for (int i = 0; i <= returnedAddress.getMaxAddressLineIndex(); i++) {
                    strReturnedAddress.append(returnedAddress.getAddressLine(i)).append("\n");
                }
                strAdd = strReturnedAddress.toString();
                Log.w("MyCurrentloctionaddress", strReturnedAddress.toString());
            } else {
                Log.w("MyCurrentloctionaddress", "No Address returned!");
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.w("MyCurrentloctionaddress", "Canont get Address!");
        }

        // "???????????? " ?????? ????????????
        strAdd = strAdd.substring(5);

        return strAdd;
    }

    private class GetData extends AsyncTask<String, Void, String> {

        ProgressDialog progressDialog;
        String errorString = null;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            progressDialog = ProgressDialog.show(MainActivity.this,
                    "Please Wait", null, true, true);
        }


        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            progressDialog.dismiss();
            //mTextViewResult.setText(result);
            Log.d(TAG, "response - " + result);

            if (result == null){

                //mTextViewResult.setText(errorString);
            }
            else {

                mJsonString = result;
                GetDroneLocation();
            }
        }


        @Override
        protected String doInBackground(String... params) {

            String serverURL = params[0];
            String postParameters = params[1];

            try {

                URL url = new URL(serverURL);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();


                httpURLConnection.setReadTimeout(5000);
                httpURLConnection.setConnectTimeout(5000);
                httpURLConnection.setRequestMethod("POST");
                httpURLConnection.setDoInput(true);
                httpURLConnection.connect();


                OutputStream outputStream = httpURLConnection.getOutputStream();
                outputStream.write(postParameters.getBytes("UTF-8"));
                outputStream.flush();
                outputStream.close();


                int responseStatusCode = httpURLConnection.getResponseCode();
                Log.d(TAG, "response code - " + responseStatusCode);

                InputStream inputStream;
                if(responseStatusCode == HttpURLConnection.HTTP_OK) {
                    inputStream = httpURLConnection.getInputStream();
                }
                else{
                    inputStream = httpURLConnection.getErrorStream();
                }


                InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "UTF-8");
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

                StringBuilder sb = new StringBuilder();
                String line;

                while((line = bufferedReader.readLine()) != null){
                    sb.append(line);
                }

                bufferedReader.close();

                return sb.toString().trim();


            } catch (Exception e) {

                Log.d(TAG, "GetData : Error ", e);
                errorString = e.toString();

                return null;
            }

        }

    }


    //=========================================?????????????????? ???????????? ?????? ?????? ?????? ????????????=====================================================
    private void GetDroneLocation()
    {
        String TAG_JSON="result";

        try {
            JSONObject jsonObject = new JSONObject(mJsonString);
            JSONArray jsonArray = jsonObject.getJSONArray(TAG_JSON);


            //Log.d("??? ???????????? ?????? : ", String.valueOf(jsonArray.length()));

            if(jsonArray.length() == 0)
            {
                Log.d("?????? ????????? DB??? ????????????. -> ", String.valueOf(jsonArray.length()));

                droneID = null;
                droneLocationLat = null;
                droneLocationLng = null;
            }

            for(int i=0;i<jsonArray.length();i++){

                JSONObject item = jsonArray.getJSONObject(i);

                droneID = item.getString("ID");
                droneLocationLat = item.getString("Lat");
                droneLocationLng = item.getString("Lng");

                Log.d("?????????????????? ????????????","" + droneID + " " + droneLocationLat + " " + droneLocationLng);

                addDroneMarker(mMapView ,MapPoint.mapPointWithGeoCoord(Double.parseDouble(droneLocationLat) ,Double.parseDouble(droneLocationLng)));

                droneLat.setText(droneLocationLat);
                droneLng.setText(droneLocationLng);
            }

        } catch (JSONException e) {
            Log.d(TAG, "showResult : ", e);
        }

    }
    //===============================================================================================================================================



    //=========================================??????????????? ?????? ?????? ??????=====================================================
    private void addDroneMarker(MapView mapview, MapPoint markerpoint)
    {
        MapPOIItem marker = new MapPOIItem();
        marker.setItemName("?????? ?????? ??????");
        marker.setTag(0);
        marker.setMapPoint(markerpoint);
        marker.setMarkerType(MapPOIItem.MarkerType.BluePin); // ???????????? ???????????? BluePin ?????? ??????.
        marker.setSelectedMarkerType(MapPOIItem.MarkerType.RedPin); // ????????? ???????????????, ???????????? ???????????? RedPin ?????? ??????.

        droneMarker = marker;

        mapview.addPOIItem(marker);

    }


    //=========================================?????? ???????????? ?????? ?????? ??????=====================================================
    private void clearDroneMarker()
    {
        mMapView.removePOIItem(droneMarker);

        //????????? ?????? ????????? ?????? ??????
        GetData task = new GetData();
        task.execute( "http://tmdghks992.dothome.co.kr/getDroneLocation.php", "");
    }


    //=========================================?????? ????????? ?????? ?????? ????????? ????????? ?????????=====================================================
    private void drawLowPoly()
    {
        mMapView.removeAllPolylines();

        if(droneLocationLat == null || droneLocationLng == null)
        {
            Log.d("?????????", "?????? ????????? ????????????.");
            return;
        }


        MapPolyline polyline = new MapPolyline();
        polyline.setTag(1000);
        polyline.setLineColor(Color.argb(128, 255, 51, 0)); // Polyline ?????? ??????.

        // Polyline ?????? ??????.
        polyline.addPoint(MapPoint.mapPointWithGeoCoord(Double.valueOf((String) Lat.getText()) ,Double.valueOf((String) Lng.getText())));
        polyline.addPoint(MapPoint.mapPointWithGeoCoord(Double.parseDouble(droneLocationLat),Double.parseDouble(droneLocationLng)));

        // Polyline ????????? ?????????.
        mMapView.addPolyline(polyline);

        // ???????????? ??????????????? ???????????? Polyline??? ?????? ???????????? ??????.
        //MapPointBounds mapPointBounds = new MapPointBounds(polyline.getMapPoints());
        //int padding = 100; // px
        //mMapView.moveCamera(CameraUpdateFactory.newMapPointBounds(mapPointBounds, padding));

        String dis = String.format("%.0f", getDistance(Double.valueOf((String) Lat.getText()) ,Double.valueOf((String) Lng.getText()),Double.parseDouble(droneLocationLat),Double.parseDouble(droneLocationLng)));

        distance.setText(dis + " ??????");
    }


    //=========================================???????????? ??????=====================================================
    public double getDistance(double lat1 , double lng1 , double lat2 , double lng2 ){
        double distance;

        Location locationA = new Location("point A");
        locationA.setLatitude(lat1);
        locationA.setLongitude(lng1);

        Location locationB = new Location("point B");
        locationB.setLatitude(lat2);
        locationB.setLongitude(lng2);

        distance = locationA.distanceTo(locationB);

        return distance;
    }

    private class GetRescueData extends AsyncTask<String, Void, String> {

        ProgressDialog progressDialog;
        String errorString = null;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            progressDialog = ProgressDialog.show(MainActivity.this,
                    "Please Wait", null, true, true);
        }


        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            progressDialog.dismiss();
            //mTextViewResult.setText(result);
            Log.d(TAG, "response - " + result);

            if (result == null){

                //mTextViewResult.setText(errorString);
            }
            else {

                mJsonString2 = result;
                GetRescueData();
            }
        }


        @Override
        protected String doInBackground(String... params) {

            String serverURL = params[0];
            String postParameters = params[1];

            try {

                URL url = new URL(serverURL);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();


                httpURLConnection.setReadTimeout(5000);
                httpURLConnection.setConnectTimeout(5000);
                httpURLConnection.setRequestMethod("POST");
                httpURLConnection.setDoInput(true);
                httpURLConnection.connect();


                OutputStream outputStream = httpURLConnection.getOutputStream();
                outputStream.write(postParameters.getBytes("UTF-8"));
                outputStream.flush();
                outputStream.close();


                int responseStatusCode = httpURLConnection.getResponseCode();
                Log.d(TAG, "response code - " + responseStatusCode);

                InputStream inputStream;
                if(responseStatusCode == HttpURLConnection.HTTP_OK) {
                    inputStream = httpURLConnection.getInputStream();
                }
                else{
                    inputStream = httpURLConnection.getErrorStream();
                }


                InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "UTF-8");
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

                StringBuilder sb = new StringBuilder();
                String line;

                while((line = bufferedReader.readLine()) != null){
                    sb.append(line);
                }

                bufferedReader.close();

                return sb.toString().trim();


            } catch (Exception e) {

                Log.d(TAG, "GetData : Error ", e);
                errorString = e.toString();

                return null;
            }

        }

    }

    private void GetRescueData()
    {
        String TAG_JSON="result";

        try {
            JSONObject jsonObject = new JSONObject(mJsonString2);
            JSONArray jsonArray = jsonObject.getJSONArray(TAG_JSON);

            Log.d("???????????? ???????????? : ", String.valueOf(jsonObject));
            Log.d("??? ???????????? ?????? : ", String.valueOf(jsonArray.length()));

            //rescueDataArray = jsonArray;

            for(int i=0;i<jsonArray.length();i++){
                JSONObject item = null;

                try {
                    item = jsonArray.getJSONObject(i);

                    String DBandroidID = item.getString("androidID");

                    Log.d("androidID ?????? ", android_id + " == " + DBandroidID);
                    Log.d("android_id", String.valueOf(android_id.equals(DBandroidID)));

                    if(android_id.equals(DBandroidID))
                    {
                        updateData = true;

                        UpdateRescueData task = new UpdateRescueData();
                        task.execute( "http://tmdghks992.dothome.co.kr/updateRescueRequest.php", "");
                        return;
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            Log.d("updateData ", String.valueOf(updateData));

            if(updateData == false)
            {
                Response.Listener<String> responseListener = new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            boolean success = jsonObject.getBoolean("success");

                            if (success) {
                                Toast.makeText(getApplicationContext(), "??????????????? ??????????????????.", Toast.LENGTH_LONG).show();

                            } else {
                                Toast.makeText(getApplicationContext(), "??????????????? ??????????????????.", Toast.LENGTH_SHORT).show();
                                return;
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                };
                //????????? ???????????? ????????? ???
                RescueRequest rescueRequest = null;
                try {
                    rescueRequest = new RescueRequest(Double.valueOf((String) Lat.getText()) ,Double.valueOf((String) Lng.getText()), (String)RoadAddress.getText(), android_id, responseListener);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
                queue.add(rescueRequest);
            }

        } catch (JSONException e) {
            Log.d(TAG, "showResult : ", e);
        }

    }

    private class UpdateRescueData extends AsyncTask<String, Void, String> {

        ProgressDialog progressDialog;
        String errorString = null;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            progressDialog = ProgressDialog.show(MainActivity.this,
                    "Please Wait", null, true, true);
        }


        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            progressDialog.dismiss();
            //mTextViewResult.setText(result);
            Log.d(TAG, "response - " + result);

            if (result == null){

                //mTextViewResult.setText(errorString);
            }
            else {

                mJsonString3 = result;
                UpdateRescueData();
            }
        }


        @Override
        protected String doInBackground(String... params) {

            String serverURL = params[0];
            String postParameters = params[1];

            try {

                URL url = new URL(serverURL);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();


                httpURLConnection.setReadTimeout(5000);
                httpURLConnection.setConnectTimeout(5000);
                httpURLConnection.setRequestMethod("POST");
                httpURLConnection.setDoInput(true);
                httpURLConnection.connect();


                OutputStream outputStream = httpURLConnection.getOutputStream();
                outputStream.write(postParameters.getBytes("UTF-8"));
                outputStream.flush();
                outputStream.close();


                int responseStatusCode = httpURLConnection.getResponseCode();
                Log.d(TAG, "response code - " + responseStatusCode);

                InputStream inputStream;
                if(responseStatusCode == HttpURLConnection.HTTP_OK) {
                    inputStream = httpURLConnection.getInputStream();
                }
                else{
                    inputStream = httpURLConnection.getErrorStream();
                }


                InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "UTF-8");
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

                StringBuilder sb = new StringBuilder();
                String line;

                while((line = bufferedReader.readLine()) != null){
                    sb.append(line);
                }

                bufferedReader.close();

                return sb.toString().trim();


            } catch (Exception e) {

                Log.d(TAG, "GetData : Error ", e);
                errorString = e.toString();

                return null;
            }

        }

    }

    private void UpdateRescueData()
    {
        updateData = true;

        Response.Listener<String> responseListener = new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    Log.d("response ", response);

                    JSONObject jsonObject = new JSONObject(response);
                    //Log.d("test", mJsonString2);
                    boolean success = jsonObject.getBoolean("success");

                    if (success) {
                        Toast.makeText(getApplicationContext(), "??????????????? ???????????????????????????.", Toast.LENGTH_LONG).show();

                    } else {
                        Toast.makeText(getApplicationContext(), "??????????????? ??????????????????.", Toast.LENGTH_SHORT).show();
                        return;
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        };
        //????????? ???????????? ????????? ???
        UpdateRescueReqeust updateRescueRequest = null;
        try {
            if (Lat.getText() == null || Lng.getText() == null) {
                Toast.makeText(getApplicationContext(), "?????? ????????? ????????? ??? ????????????.", Toast.LENGTH_SHORT).show();
                return;
            }
            updateRescueRequest = new UpdateRescueReqeust(Double.valueOf((String) Lat.getText()), Double.valueOf((String) Lng.getText()), (String) RoadAddress.getText(), android_id, responseListener);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
        queue.add(updateRescueRequest);
        return;

    }
}