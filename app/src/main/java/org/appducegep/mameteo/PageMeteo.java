package org.appducegep.mameteo;

import android.Manifest;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;
import android.os.StrictMode;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.widget.TextView;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.tasks.OnSuccessListener;

public class PageMeteo extends AppCompatActivity {

    private TextView libelleTitre;
    private String CLE = "2f930d9f16d84b77a96194904192202";
    private String xml = "";
    private final int MY_PERMISSIONS_REQUEST_LOCATION = 1;

    private LocationRequest mRequest;
    private FusedLocationProviderClient mLocationClient;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_meteo:
                    libelleTitre.setText(R.string.titre_accueil);
                    return true;
                case R.id.navigation_meteo_detail:
                    libelleTitre.setText(R.string.titre_meteo_detail);
                    return true;
                case R.id.navigation_notifications:
                    libelleTitre.setText(R.string.title_notifications);
                    return true;
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.page_meteo);
        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new
                    StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        libelleTitre = (TextView) findViewById(R.id.message);
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        getLocation();
    }

    private void getLocation() {
        mRequest = LocationRequest.create();
        mRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        mRequest.setInterval(10000);
        mRequest.setFastestInterval(10000);
        mLocationClient = LocationServices.getFusedLocationProviderClient(this);
        LocationCallback mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                List<Location> locationList = locationResult.getLocations();
                if (locationList.size() > 0) {
                    //The last location in the list is the newest
                    Location location = locationList.get(locationList.size() - 1);
                    String coordinates = String.format("%.7f", location.getLatitude()) + "," + String.format("%.7f", location.getLongitude());
                    displayLocationAndData(coordinates, xml, CLE);
                }
            }
        };
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                //Location Permission already granted
                mLocationClient.requestLocationUpdates(mRequest, mLocationCallback, Looper.myLooper());
        } else {
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.ACCESS_FINE_LOCATION)) {
                    new AlertDialog.Builder(this)
                        .setTitle("Location Permission Needed")
                        .setMessage("This app needs the Location permission, please accept to use location functionality")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                            //Prompt the user once explanation has been shown
                            ActivityCompat.requestPermissions(PageMeteo.this,
                                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                MY_PERMISSIONS_REQUEST_LOCATION );
                            }
                        })
                    .create()
                    .show();
                }
            else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION );
            }
        }
    }

    protected void displayLocationAndData(String localisation, String xml, String CLE) {
        if(localisation == null) localisation = "New York";
        try {
            URL url = new URL("https://api.apixu.com/v1/current.xml?key="+CLE+"&q="+localisation);
            System.out.println("URL : " + url);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            try {
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                StringBuilder stringBuilder = new StringBuilder();
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    stringBuilder.append(line).append("\n");
                }
                bufferedReader.close();
                xml = stringBuilder.toString();
            }
            finally{
                urlConnection.disconnect();
            }
        }
        catch(Exception e) {
            Log.e("ERROR", e.getMessage(), e);
        }
        System.out.println(xml);

        try {
            DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = null;
            docBuilder = builderFactory.newDocumentBuilder();
            Document doc = null;
            doc = docBuilder.parse(new ByteArrayInputStream(xml.getBytes("UTF-8")));
            Element elementHumidite = (Element)doc.getElementsByTagName("humidity").item(0);
            String humidite = elementHumidite.getTextContent();
            Element elementVentForce = (Element)doc.getElementsByTagName("wind_kph").item(0);
            String ventForce = elementVentForce.getTextContent();
            Element elementVentDirection = (Element)doc.getElementsByTagName("wind_dir").item(0);
            String ventDirection = elementVentDirection.getTextContent();
            Element elementCondition = (Element)doc.getElementsByTagName("condition").item(0);
            Element elementSoleilOuNuage = (Element)elementCondition.getElementsByTagName("text").item(0);
            String soleilOuNuage = elementSoleilOuNuage.getTextContent();
            Element elementEndroit = (Element)doc.getElementsByTagName("name").item(0);
            String endroit = elementEndroit.getTextContent();
            Element elementTemperature = (Element)doc.getElementsByTagName("temp_c").item(0);
            String temperature = elementTemperature.getTextContent();
            if(soleilOuNuage.compareTo("Sunny") == 0) soleilOuNuage = "Ensoleillé";
            else soleilOuNuage = "Nuageux";

            System.out.println("Meteo = " + soleilOuNuage);
            System.out.println("Vent : " + ventDirection + " " + ventForce + "\n");
            System.out.println("Humidite = " + humidite);

            TextView affichageMeteo = (TextView)this.findViewById(R.id.meteo);
            affichageMeteo.setText(soleilOuNuage + "\n");
            affichageMeteo.append("\n\n\n");
            affichageMeteo.append("Vent : " + ventDirection + " " + ventForce + "\n");
            affichageMeteo.append("Humidite : " + humidite + "\n");
            affichageMeteo.append("Température : " + temperature + "°C\n");
            affichageMeteo.append("Localisation : " + endroit + "\n");

            MeteoDAO meteoDAO = new MeteoDAO(getApplicationContext());
            meteoDAO.ajouterMeteo(soleilOuNuage);


        } catch (IOException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        }
         catch (ParserConfigurationException e) {
            e.printStackTrace();
        }

    }

}
