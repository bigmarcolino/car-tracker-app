package com.breakfun.cartracker;

import android.content.Context;
import android.graphics.Color;
import android.os.AsyncTask;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, ConnectivityReceiver.ConnectivityReceiverListener {
    private GoogleMap google_map;
    AsyncTask gc;
    JSONObject json;
    JSONArray json_array;
    String time = null;
    double lat, lng;
    LatLng point;
    Snackbar snackbar;
    boolean is_connected;
    int interval;
    String burst_option, get_option;
    private final String URL_READY_RECEIVE = "https://tracker.gobbi.info/readyToReceive/arduino";
    private final String URL_STOP_RECEIVE = "https://tracker.gobbi.info/stopReceiving/arduino";
    private final String URL_POSITION = "https://tracker.gobbi.info/position/arduino";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        SupportMapFragment mapFragment = (SupportMapFragment)getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

        interval = Integer.parseInt(getIntent().getStringExtra("interval")) * 1000;
        get_option = getIntent().getStringExtra("get_option");
        burst_option = getIntent().getStringExtra("burst_option");
    }

    private class GetCoordinates extends AsyncTask<Void, Void, Void> {
        Context cont;
        RequestQueue queue;
        String date_aux, date, hour;
        Boolean receive = false;
        FileOutputStream fos;
        Long ts;

        GetCoordinates(Context context) {
            cont = context;
        }

        protected Void doInBackground(Void... voids) {
            queue = Volley.newRequestQueue(cont);

            try {
                fos = openFileOutput("android_metrics.txt", Context.MODE_PRIVATE);
            }
            catch (IOException e) {}

            while(!isCancelled() && !receive) {
                JsonObjectRequest receive_request = new JsonObjectRequest(Request.Method.GET, URL_READY_RECEIVE, null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            if(response.getInt("status") == 200) {
                                receive = true;
                                ts = System.currentTimeMillis()/1000;
                                fos.write(ts.toString().getBytes());
                            }
                        }
                        catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(getApplicationContext(), "GET 'Ready To Receive' não funcionou", Toast.LENGTH_LONG).show();
                    }
                });

                if(receive)
                    break;
                else
                    queue.add(receive_request);

                try {
                    Thread.sleep(3000);
                } catch(InterruptedException e) {
                    return null;
                }
            }

            while(!isCancelled()) {
                JsonObjectRequest position_request = new JsonObjectRequest(Request.Method.GET, URL_POSITION, null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            json_array = response.getJSONArray("coordinates");

                            if(json_array.length() > 0) {
                                ts = System.currentTimeMillis()/1000;
                                fos.write(ts.toString().getBytes());

                                if(burst_option.equals("Ativada")) {
                                    for(int i = 0; i < json_array.length(); i++) {
                                        json = json_array.getJSONObject(i);

                                        time = json.getString("time");
                                        date_aux = time.split("T")[0];
                                        date = date_aux.split("-")[2] + "/" + date_aux.split("-")[1] + "/" + date_aux.split("-")[0];
                                        hour = time.split("T")[1].split("\\.")[0];

                                        lat = ((double)Integer.parseInt(json.getString("latitude"))) / 1000000;
                                        lng = ((double)Integer.parseInt(json.getString("longitude"))) / 1000000;

                                        point = new LatLng(lat, lng);
                                        google_map.addMarker(new MarkerOptions().position(point).title(date + ", às " + hour).snippet(lat + ", " + lng).icon(BitmapDescriptorFactory.fromResource(R.drawable.marker)));
                                        google_map.animateCamera(CameraUpdateFactory.newLatLngZoom(point, 18));
                                    }
                                }
                                else {
                                    json = json_array.getJSONObject(json_array.length()-1);

                                    time = json.getString("time");
                                    date_aux = time.split("T")[0];
                                    date = date_aux.split("-")[2] + "/" + date_aux.split("-")[1] + "/" + date_aux.split("-")[0];
                                    hour = time.split("T")[1].split("\\.")[0];

                                    lat = ((double)Integer.parseInt(json.getString("latitude"))) / 1000000;
                                    lng = ((double)Integer.parseInt(json.getString("longitude"))) / 1000000;

                                    point = new LatLng(lat, lng);
                                    google_map.addMarker(new MarkerOptions().position(point).title(date + ", às " + hour).snippet(lat + ", " + lng).icon(BitmapDescriptorFactory.fromResource(R.drawable.marker)));
                                    google_map.animateCamera(CameraUpdateFactory.newLatLngZoom(point, 18));
                                }
                            }
                        }
                        catch(Exception e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error){}
                });

                queue.add(position_request);

                try {
                    Thread.sleep(interval);
                } catch(InterruptedException e) {
                    return null;
                }
            }

            try {
                fos.close();
            }
            catch (IOException e) {}

            return null;
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        google_map = googleMap;
        gc = new GetCoordinates(this).execute();
        snackbar = Snackbar.make(findViewById(R.id.map), "", Snackbar.LENGTH_INDEFINITE);
        checkConnection();
    }

    @Override
    public void onDestroy() {
        try {
            FileInputStream fis = this.openFileInput("android_metrics.txt");
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader bufferedReader = new BufferedReader(isr);
            StringBuilder sb = new StringBuilder();
            String line;

            while ((line = bufferedReader.readLine()) != null)
                sb.append(line);

            Log.d("metricas", sb.toString());
        } catch(Exception e) {}

        gc.cancel(true);

        RequestQueue queue = Volley.newRequestQueue(this);
        StringRequest stop_receive_request = new StringRequest(Request.Method.GET, URL_STOP_RECEIVE, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {}
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {}
        });
        queue.add(stop_receive_request);

        try {
            Thread.sleep(1000);
        } catch(InterruptedException e) {}

        super.onDestroy();
    }

    private void checkConnection() {
        is_connected = ConnectivityReceiver.isConnected();
        showSnack(is_connected);
    }

    private void showSnack(boolean is_connected) {
        String message;
        int color;

        if(!is_connected) {
            if(!gc.isCancelled())
                gc.cancel(true);

            message = "Sem conexão com a Internet";
            color = Color.WHITE;
            snackbar.setText(message);
            View sb_view = snackbar.getView();
            TextView text_view = (TextView)sb_view.findViewById(android.support.design.R.id.snackbar_text);
            text_view.setTextColor(color);
            snackbar.show();
        }
        else if(snackbar.isShown()) {
            gc = new GetCoordinates(this).execute();
            snackbar.dismiss();
            snackbar = Snackbar.make(findViewById(R.id.map), "", Snackbar.LENGTH_INDEFINITE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        MyApplication.getInstance().setConnectivityListener(this);
    }

    @Override
    public void onNetworkConnectionChanged(boolean is_connected) {
        showSnack(is_connected);
    }
}