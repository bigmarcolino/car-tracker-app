package com.breakfun.cartracker;

import android.content.Context;
import android.graphics.Color;
import android.os.AsyncTask;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
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

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, ConnectivityReceiver.ConnectivityReceiverListener {
    private GoogleMap mMap;
    AsyncTask gc;
    JSONObject json, coordinates;
    JSONArray jsonArray;
    String time = null;
    double lat, lng;
    LatLng ponto;
    Snackbar snackbar;
    boolean isConnected;
    int intervalo;
    String rajadaOption, getOption;
    private final String URL_READY_RECEIVE = "https://tracker.gobbi.info/readyToReceive/arduino";
    private final String URL_STOP_RECEIVE = "https://tracker.gobbi.info/stopReceiving/arduino";
    private final String URL_POSITION = "https://tracker.gobbi.info/position/arduino";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

        intervalo = Integer.parseInt(getIntent().getStringExtra("intervalo")) * 1000;
        getOption = getIntent().getStringExtra("getOption");
        rajadaOption = getIntent().getStringExtra("rajadaOption");
    }

    private class GetCoordinates extends AsyncTask<Void, Void, Void> {
        Context cont;
        RequestQueue queue;
        String dataAux, data, hora;
        Boolean receive = false;

        GetCoordinates(Context context) {
            cont = context;
        }

        protected Void doInBackground(Void... voids) {
            queue = Volley.newRequestQueue(cont);

            StringRequest receiveRequest = new StringRequest(Request.Method.GET, URL_READY_RECEIVE, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    try {
                        json = new JSONObject(response);

                        if(json.getInt("status") == 200) {
                            receive = true;
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
            queue.add(receiveRequest);

            while (!receive);

            while(!isCancelled()) {
                StringRequest positionRequest = new StringRequest(Request.Method.GET, URL_POSITION, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            coordinates = new JSONObject(response);
                            jsonArray = coordinates.getJSONArray("coordinates");

                            if(jsonArray.length() > 0) {
                                if(rajadaOption.equals("Ativada")) {
                                    for (int i = 0; i < jsonArray.length(); i++) {
                                        json = jsonArray.getJSONObject(i);

                                        time = json.getString("time");
                                        dataAux = time.split("T")[0];
                                        data = dataAux.split("-")[2] + "/" + dataAux.split("-")[1] + "/" + dataAux.split("-")[0];
                                        hora = time.split("T")[1].split("\\.")[0];

                                        lat = ((double)Integer.parseInt(json.getString("latitude"))) / 1000000;
                                        lng = ((double)Integer.parseInt(json.getString("longitude"))) / 1000000;

                                        ponto = new LatLng(lat, lng);
                                        mMap.addMarker(new MarkerOptions().position(ponto).title(data + ", às " + hora).icon(BitmapDescriptorFactory.fromResource(R.drawable.marker)));
                                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(ponto, 18));
                                    }
                                }
                                else {
                                    json = jsonArray.getJSONObject(jsonArray.length()-1);

                                    time = json.getString("time");
                                    dataAux = time.split("T")[0];
                                    data = dataAux.split("-")[2] + "/" + dataAux.split("-")[1] + "/" + dataAux.split("-")[0];
                                    hora = time.split("T")[1].split("\\.")[0];

                                    lat = ((double)Integer.parseInt(json.getString("latitude"))) / 1000000;
                                    lng = ((double)Integer.parseInt(json.getString("longitude"))) / 1000000;

                                    ponto = new LatLng(lat, lng);
                                    mMap.addMarker(new MarkerOptions().position(ponto).title(data + ", às " + hora).icon(BitmapDescriptorFactory.fromResource(R.drawable.marker)));
                                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(ponto, 18));
                                }
                            }
                        }
                        catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(getApplicationContext(), "GET 'Position' não funcionou", Toast.LENGTH_LONG).show();
                    }
                });

                queue.add(positionRequest);

                try {
                    Thread.sleep(intervalo);
                } catch (InterruptedException e) {
                    return null;
                }
            }

            return null;
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        gc = new GetCoordinates(this).execute();
        snackbar = Snackbar.make(findViewById(R.id.map), "", Snackbar.LENGTH_INDEFINITE);
        checkConnection();
    }

    @Override
    public void onDestroy() {
        gc.cancel(true);

        RequestQueue queue = Volley.newRequestQueue(this);
        StringRequest stopReceiveRequest = new StringRequest(Request.Method.GET, URL_STOP_RECEIVE, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {}
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {}
        });
        queue.add(stopReceiveRequest);

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {}

        super.onDestroy();
    }

    private void checkConnection() {
        isConnected = ConnectivityReceiver.isConnected();
        showSnack(isConnected);
    }

    private void showSnack(boolean isConnected) {
        String message;
        int color;

        if (!isConnected) {
            if(!gc.isCancelled()) {
                gc.cancel(true);
            }

            message = "Sem conexão com a Internet";
            color = Color.WHITE;
            snackbar.setText(message);
            View sbView = snackbar.getView();
            TextView textView = (TextView) sbView.findViewById(android.support.design.R.id.snackbar_text);
            textView.setTextColor(color);
            snackbar.show();
        }
        else if(snackbar.isShown()){
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
    public void onNetworkConnectionChanged(boolean isConnected) {
        showSnack(isConnected);
    }
}