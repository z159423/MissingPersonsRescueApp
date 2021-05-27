package com.example.missingpersonsrescueapp;

import android.app.Activity;
import android.app.Application;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ListView;

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

public class GetRescueRequestData  {

    Context currentContext;
    private static String TAG = "phpexample";
    private String mJsonString;
    private JSONArray rescueDataArray;

    public GetRescueRequestData(Context context)
    {
        currentContext = context;

    }

    public JSONArray getRescueReqeustData()
    {
        GetData task = new GetData();
        task.execute( "http://tmdghks992.dothome.co.kr/FindAllRescueReqeust.php", "");

        return rescueDataArray;
    }

    private class GetData extends AsyncTask<String, Void, String> {

        ProgressDialog progressDialog;
        String errorString = null;
        private String mJsonString;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            Log.d("context가져오기", String.valueOf(currentContext));

            progressDialog = ProgressDialog.show(currentContext,
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
            JSONObject jsonObject = new JSONObject(mJsonString);
            JSONArray jsonArray = jsonObject.getJSONArray(TAG_JSON);

            Log.d("총 구조요청 개수 : ", String.valueOf(jsonArray.length()));

            rescueDataArray = jsonArray;

        } catch (JSONException e) {
            Log.d(TAG, "showResult : ", e);
        }

    }
}
