package com.penguin.fri.penguin;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import javax.xml.datatype.Duration;

public class CompanyMainActivity extends Activity {

    private String mail;
    private int id;
    private String name;
    private String address;
    ListView lvCompanyOffers;


    //Using for populating listview of company offers
    ArrayList<HashMap<String, String>> arrLV = new ArrayList<HashMap<String, String>>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_company_main);

        TextView companyName = (TextView) findViewById(R.id.companyName);
        lvCompanyOffers = (ListView) findViewById(R.id.lvCompanyOffers);

        //Get company info from shared
        SharedPreferences informations = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        mail = informations.getString("mail", "wrongMail");
        id = informations.getInt("id", -1);
        name = informations.getString("name", "noName");
        address = informations.getString("address", "WrongAddress");

        Log.i("COMPANY INFO", "Mail: " + mail + " ID: " + id + " Name: " + name + " Address: " + address);

        //set company name
        companyName.setText(name);
        refreshLV();
    }

    private void refreshLV() {
        if (isConnected()) {
            HttpAsyncTask loginTask = new HttpAsyncTask();
            loginTask.execute((Void) null);
        } else {
            //Inform user that he is not connected
            Toast.makeText(this, "You are NOT connected to internet", Toast.LENGTH_LONG);
            Log.v("CONNECTION", "Phone is not connected to internet");
        }
    }

    //Check if mobile phone is connected to internet
    public boolean isConnected() {
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Activity.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected())
            return true;
        else
            return false;
    }


    public void addNewOffer(View view) {

        //we redirect to new page
        Intent i = new Intent(this, AddNewOfferActivity.class);
        startActivity(i);

    }

    private class HttpAsyncTask extends AsyncTask<Void, Void, Void> {

        ProgressDialog pdLoading = new ProgressDialog(CompanyMainActivity.this);

        String getURL = "http://10.0.2.2:8080/offers/" + id;
        String result;
        JSONArray response;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            // It would be awesome to show user some interface while Katya's database is
            // struggling to make responses to our login demands.
            pdLoading.setMessage("\tFetching data from server...");
            pdLoading.show();
        }


        @Override
        protected Void doInBackground(Void... params) {
            try {
                // We get JSON from database as  JSONObject
                //TODO: If this work, remo this line and getConnection mehod
                //result = getConnection(getURL);
                result = Connection.getConnection(getURL);
                response = new JSONArray(result);





            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            // We got what we wanted. Let's close progressDialog now. All hail database!
            pdLoading.dismiss();
            try {
                for (int i = 0; i < response.length(); i++) {
                    //Get JSON object form JSONArray
                    JSONObject object = response.getJSONObject(i);

                    //Storing JSONObject in a variables
                    String offerID = Integer.toString(object.getInt("id"));
                    String offerName = object.getString("name");
                    String offerRules = object.getString("rules");
                    //String offerHashtag = object.getString("hashtag");

                    //adding information to map and then to arraylist
                    HashMap<String, String> map = new HashMap<String, String>();
                    map.put("id", offerID);
                    map.put("name", offerName);
                    map.put("rules", offerRules);
                    // I would put hashtag here..... IF I HAD ONE!!
                    //map.put("hashtag",offerHashtag);
                    arrLV.add(map);
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }

            ListAdapter adapter = new SimpleAdapter(CompanyMainActivity.this, arrLV,
                    R.layout.company_offer_layout, new String[]{"name", "rules"}, new int[]{
                    R.id.tvOfferName, R.id.tvOfferRules});
            lvCompanyOffers.setAdapter(adapter);

            lvCompanyOffers.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> parent, View view,
                                        int position, long id) {

                    Intent i = new Intent(CompanyMainActivity.this,UpdateOfferActivity.class);
                    i.putExtra("offerID",arrLV.get(+position).get("id"));
                    i.putExtra("name",arrLV.get(+position).get("name"));
                    i.putExtra("rules",arrLV.get(+position).get("rules"));
                    //i.putExtra("hashtag",arrLV.get(+position).get("id"));
                    startActivity(i);
                    //Toast.makeText(CompanyMainActivity.this, "You Clicked at " + arrLV.get(+position).get("id"), Toast.LENGTH_SHORT).show();

                }
            });

        }

        /*
         * Method for GET connection to HTTP server,
         * Method returns answer from database
         */
        /*
        public String getConnection(String URL) throws IOException {
            HttpClient hc = new DefaultHttpClient();
            HttpGet httpGetCompanyOffers = new HttpGet(URL);
            HttpResponse httpResponse = hc.execute(httpGetCompanyOffers);
            HttpEntity httpEntity = httpResponse.getEntity();
            return EntityUtils.toString(httpEntity);
        }
        */
    }


}
