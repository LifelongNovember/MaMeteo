package org.appducegep.mameteo;

import android.os.Bundle;
import android.os.StrictMode;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
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

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

public class PageMeteo extends AppCompatActivity {

    private TextView libelleTitre;

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
        if (android.os.Build.VERSION.SDK_INT > 9)
        {
            StrictMode.ThreadPolicy policy = new
                    StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        libelleTitre = (TextView) findViewById(R.id.message);
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        String CLE = "";
        String xml = "";

        try {
            URL url = new URL("https://api.apixu.com/v1/current.xml?key="+CLE+"&q=Matane");
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
            if(soleilOuNuage.compareTo("Sunny") == 0) soleilOuNuage = "Ensoleillé";
            else soleilOuNuage = "Nuageux";

            System.out.println("Meteo = " + soleilOuNuage);
            System.out.println("Vent : " + ventDirection + " " + ventForce + "\n");
            System.out.println("Humidite = " + humidite);

            TextView affichageMeteo = (TextView)this.findViewById(R.id.meteo);
            affichageMeteo.setText(soleilOuNuage + "\n");
            affichageMeteo.append("\n\n\n\n\n");
            affichageMeteo.append("Vent : " + ventDirection + " " + ventForce + "\n");
            affichageMeteo.append("Humidite : " + humidite + "\n");


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
