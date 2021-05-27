package com.example.missingpersonsrescueapp;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ListView;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class RescueRequest extends StringRequest {

    //=========================================데이터베이스 서버에 구조요청 보내기(현재 좌표 보내기)=====================================================

    //서버 URL 설정(php 파일 연동)
    final static private String URL = "http://tmdghks992.dothome.co.kr/rescueRequest.php";
    private Map<String, String> map;
    //private Map<String, String>parameters;

    public RescueRequest(double Lat, double Lng, String roadaddress, String androidID, Response.Listener<String> listener) throws JSONException {
        super(Method.POST, URL, listener, null);

            map = new HashMap<>();
            map.put("Lat", Lat + "");
            map.put("Lng", Lng + "");
            map.put("roadAddress", roadaddress);
            map.put("androidID",androidID);

    }

    @Override
    protected Map<String, String>getParams() throws AuthFailureError {
        return map;
    }

}
