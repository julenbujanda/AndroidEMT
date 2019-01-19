package ga.julen.busemt;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

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
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    private Context context;
    private GoogleMap map;
    private LocationManager locationManager;
    private ProgressDialog progressDialog;
    private FloatingActionButton fab;

    private String idClient;
    private String passKey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        idClient = getString(R.string.idClient);
        passKey = getString(R.string.passKey);
        fab = findViewById(R.id.floatingActionButton);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cargarMapa();
            }
        });
        context = this;
        if (checkLocationPermission())
            cargarMapa();
    }

    private void cargarMapa() {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        locationManager.requestSingleUpdate(LocationManager.GPS_PROVIDER, locationListener, null);
    }

    private boolean checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }
        return ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            progressDialog = new ProgressDialog(MainActivity.this);
            progressDialog.setTitle("EMT");
            progressDialog.setMessage("Cargando...");
            progressDialog.setIndeterminate(false);
            progressDialog.show();
            paradas(location);
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 17.0f));
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    };

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        checkLocationPermission();
        map.setMyLocationEnabled(true);
        map.getUiSettings().setMyLocationButtonEnabled(true);
        StopInfoWindow stopInfoWindow = new StopInfoWindow(context);
        map.setInfoWindowAdapter(stopInfoWindow);
        map.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        map.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                proximasLlegadas(((Parada) marker.getTag()).getId());
            }
        });
    }

    private void paradas(Location location) {
        final HashMap<String, String> params = new HashMap<>();
        params.put("idClient", idClient);
        params.put("passKey", passKey);
        params.put("Radius", "300");
        params.put("latitude", String.valueOf(location.getLatitude()));
        params.put("longitude", String.valueOf(location.getLongitude()));
        RequestQueue requestQueue = Volley.newRequestQueue(context);
        StringRequest stringRequest = new StringRequest(
                Request.Method.POST,
                "https://openbus.emtmadrid.es:9443/emt-proxy-server/last/geo/GetStopsFromXY.php",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        ArrayList<Parada> stops = new ArrayList<>();
                        Log.d("response", response);
                        try {
                            Object objParadas = new JSONObject(response).get("stop");
                            if (objParadas instanceof JSONArray) {
                                JSONArray paradas = (JSONArray) objParadas;
                                for (int i = 0; i < paradas.length(); i++) {
                                    stops.add(generarParada(paradas.getJSONObject(i)));
                                }
                            } else if (objParadas != null) {
                                stops.add(generarParada((JSONObject) objParadas));
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        for (Parada parada : stops) {
                            map.addMarker(generarMarker(parada)).setTag(parada);
                        }
                        progressDialog.dismiss();
                    }

                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("responseError", error.getMessage());
                    }
                }) {
            @Override
            protected Map<String, String> getParams() {
                return params;
            }
        };
        requestQueue.add(stringRequest);
    }

    private MarkerOptions generarMarker(Parada parada) {
        return new MarkerOptions()
                .position(new LatLng(parada.getLatitud(), parada.getLongitud()));
    }

    private Parada generarParada(JSONObject parada) throws JSONException {
        String id = (String) parada.get("stopId");
        String direccion = (String) parada.get("postalAddress");
        double latParada = (double) parada.get("latitude");
        double longParada = (double) parada.get("longitude");
        Object line = parada.get("line");
        String lines = "";
        if (line instanceof JSONArray) {
            JSONArray lineas = (JSONArray) line;
            for (int i = 0; i < lineas.length(); i++) {
                JSONObject linea = lineas.getJSONObject(i);
                lines += linea.get("line") + " ";
            }
        } else {
            JSONObject linea = (JSONObject) line;
            lines += linea.get("line") + " ";
        }
        return new Parada(Integer.parseInt(id), direccion, latParada, longParada, lines.trim());
    }

    private void proximasLlegadas(int stopId) {
        progressDialog = new ProgressDialog(MainActivity.this);
        progressDialog.setTitle("Próximas llegadas");
        progressDialog.setMessage("Cargando...");
        progressDialog.setIndeterminate(false);
        progressDialog.show();
        final HashMap<String, String> params = new HashMap<>();
        params.put("idClient", idClient);
        params.put("passKey", passKey);
        params.put("idStop", String.valueOf(stopId));
        RequestQueue requestQueue = Volley.newRequestQueue(context);
        StringRequest stringRequest = new StringRequest(
                Request.Method.POST,
                "https://openbus.emtmadrid.es:9443/emt-proxy-server/last/geo/GetArriveStop.php",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            Log.d("response", response);
                            JSONArray llegadas = new JSONObject(response).getJSONArray("arrives");
                            StringBuilder info = new StringBuilder();
                            for (int i = 0; i < llegadas.length(); i++) {
                                JSONObject llegada = llegadas.getJSONObject(i);
                                String linea = (String) llegada.get("lineId");
                                String destino = (String) llegada.get("destination");
                                int tiempo = (int) llegada.get("busTimeLeft");
                                info.append("Línea: ").append(linea).append("\n").append("    Destino: ").append(destino);
                                if (tiempo == 999999) {
                                    info.append("\n    Tiempo restante: Más de 20 minutos\n");
                                } else if (tiempo > 60) {
                                    info.append("\n    Tiempo restante: ").append(tiempo / 60).append(" minutos\n");
                                } else {
                                    info.append("\n    Tiempo restante: ").append(tiempo).append(" segundos\n");
                                }
                            }
                            progressDialog.dismiss();
                            AlertDialog.Builder builder = new AlertDialog.Builder(context);
                            builder.setTitle("Próximas llegadas")
                                    .setMessage(info.toString())
                                    .setPositiveButton("OK", null)
                                    .show();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("error", error.getMessage());
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                return params;
            }
        };
        requestQueue.add(stringRequest);
    }

}
