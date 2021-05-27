package com.example.missingpersonsrescueapp;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONException;

import java.util.HashMap;
import java.util.Map;

public class UpdateRescueReqeust extends StringRequest {

    //=========================================데이터베이스 서버에 구조요청 보내기(이미 구조요청을 보낸경우 업데이트)=====================================================

    //서버 URL 설정(php 파일 연동)
    final static private String URL = "http://tmdghks992.dothome.co.kr/updateRescueRequest.php";
    private Map<String, String> map;
    //private Map<String, String>parameters;

    public UpdateRescueReqeust(double Lat, double Lng, String roadaddress, String androidID, Response.Listener<String> listener) throws JSONException {
        super(Method.POST, URL, listener, null);

        map = new HashMap<>();
        map.put("Lat", Lat + "");
        map.put("Lng", Lng + "");
        map.put("roadAddress", roadaddress);
        map.put("androidID", androidID);

    }

    @Override
    protected Map<String, String>getParams() throws AuthFailureError {
        return map;
    }
}
