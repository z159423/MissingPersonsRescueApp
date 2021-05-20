package com.example.missingpersonsrescueapp;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;

import java.util.HashMap;
import java.util.Map;

public class RescueRequest extends StringRequest {

    //서버 URL 설정(php 파일 연동)
    final static private String URL = "http://tmdghks992.dothome.co.kr/rescueRequest.php";
    private Map<String, String> map;
    //private Map<String, String>parameters;

    public RescueRequest( double Lat, double Lng, String roadaddress, Response.Listener<String> listener) {
        super(Method.POST, URL, listener, null);

        map = new HashMap<>();
        map.put("Lat", Lat + "");
        map.put("Lng", Lng + "");
        map.put("roadAddress", roadaddress);

    }

    @Override
    protected Map<String, String>getParams() throws AuthFailureError {
        return map;
    }
}
