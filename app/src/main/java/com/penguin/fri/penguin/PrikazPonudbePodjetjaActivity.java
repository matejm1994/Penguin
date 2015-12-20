package com.penguin.fri.penguin;

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

public class PrikazPonudbePodjetjaActivity extends AppCompatActivity {
    TextView textViewPonudba;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_prikaz_ponudbe_podjetja);

        Intent intent = getIntent();
        CompanyClass company = (CompanyClass)intent.getSerializableExtra("CompanyObject");
        textViewPonudba = (TextView)findViewById(R.id.textViewPonudba);

        RESTCallTaskGetOffers restCallTaskGetOffers = new RESTCallTaskGetOffers();
        restCallTaskGetOffers.execute(String.valueOf(company.id));

        //Toast.makeText(this, "Podjetje"+company.name, Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_prikaz_ponudbe_podjetja, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            //User choose Settings
            Intent intentSettings = new Intent(this, SettingsActivity.class);
            startActivity(intentSettings);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    //prikaz ponudb podjetja
    @TargetApi(Build.VERSION_CODES.CUPCAKE)
    private class RESTCallTaskGetOffers extends AsyncTask<String, Void, String[]> { //testni za registracijo
        private String URLCompanyOffer = "http://192.168.0.101:8080/offers/"; // za seznam

        @Override
        protected String[] doInBackground(String... params) {
            String idPodtjetja = params[0];
            URLCompanyOffer+=idPodtjetja; //dodamo podatek za katero podjetje gre

            HttpClient hc = new DefaultHttpClient();
            String resultHttpRequest = null;
            String [] resultFinal = null;

            try {

                //request za ponudbe
                HttpGet getRequest = new HttpGet(URLCompanyOffer);
                HttpResponse response = hc.execute(getRequest);
                HttpEntity entity = response.getEntity();
                resultHttpRequest = EntityUtils.toString(entity);
                JSONArray jsonArray = new JSONArray(resultHttpRequest);

                StringBuffer sb = new StringBuffer();
                resultFinal = new String[jsonArray.length()]; //nastavimo velikost tabele
                //za imena ponudb
                for (int i=0; i<jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    String line =
                            jsonObject.getString("name")+", "+ jsonObject.getString("rules");
                    resultFinal[i]=line;

                }

            } catch (Exception e) {
                e.printStackTrace();
            }

            return resultFinal;
        }

        @Override
        protected void onPostExecute(String[] result) {
                for (int i=0; i<result.length; i++){
                    textViewPonudba.setText(textViewPonudba.getText()+result[i]);
                }
        }
    }




}
