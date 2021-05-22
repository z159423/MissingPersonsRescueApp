package com.example.missingpersonsrescueapp;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;

import java.util.HashMap;
import java.util.Map;

public class getRescueLocation extends StringRequest {
    //=========================================사용 안하는 함수=====================================================

    final static private String URL = "http://tmdghks992.dothome.co.kr/getRescueLocation.php";
    private Map<String, String> map;


    public getRescueLocation(String userID, String userPassword, Response.Listener<String> listener) {
        super(Method.POST, URL, listener, null);

        map = new HashMap<>();
        map.put("ID", userID);
        map.put("userPassword",userPassword);

    }

    @Override
    protected Map<String, String> getParams() throws AuthFailureError {
        return map;
    }

}
