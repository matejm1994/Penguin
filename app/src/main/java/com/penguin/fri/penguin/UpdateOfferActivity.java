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
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class UpdateOfferActivity extends Activity {

    public String offerID = "error";
    public String offerName = "error";
    public String offerRules = "error";
    public String offerHashtag = "not supported yet! Waiting for api.";


    public EditText etOfferName;
    public EditText etOfferRule;
    public EditText etOfferHashtag;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_new_offer);

        //set informations about offer and company ID
        Bundle extra = getIntent().getExtras();
        if (extra != null) {
            offerID = extra.getString("offerID");
            offerName = extra.getString("name");
            offerRules = extra.getString("rules");
            //offerHashtag = extra.getString("offerHashtag");
        }


        //references...
        Button button = (Button) findViewById(R.id.bAddNewOffer);
        etOfferName = (EditText) findViewById(R.id.etNewOfferName);
        etOfferRule = (EditText) findViewById(R.id.etNewOfferRule);
        etOfferHashtag = (EditText) findViewById(R.id.etNewOfferHashtag);

        //Update button text, because it is same layout
        button.setText("Update offer");

        //Set text to editText fields
        etOfferName.setText(offerName);
        etOfferRule.setText(offerRules);
        etOfferHashtag.setText(offerHashtag);

    }

    public void saveOfferToDB(View view) {

        // we must replace hashtag with at, because api can't handle it.
        String name = etOfferName.getText().toString();
        String rule = etOfferRule.getText().toString();
        String hashtag = etOfferHashtag.getText().toString().replaceAll("#", "@");

        //Get company info from shared
        SharedPreferences informations = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        int id = informations.getInt("id", -1);

        if (isConnected()) {
            //If everything is okay, we should run updateOffer class ... in background
            AsyncUpdateOfferTask updateOfferTask = new AsyncUpdateOfferTask(name, rule, hashtag, id, this);
            updateOfferTask.execute((Void)null);
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

    public class AsyncUpdateOfferTask extends AsyncTask<Void, Void, Void> {

        ProgressDialog pdLoading = new ProgressDialog(UpdateOfferActivity.this);

        private String name;
        private String rule;
        private String hashtag;
        private int id;

        private Context ctx;
        private boolean error = false;


        AsyncUpdateOfferTask(String name, String rule, String hashtag, int id, Context ctx) {
            this.name = name;
            this.rule = rule;
            this.hashtag = hashtag;
            this.id = id;
            this.ctx = ctx;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            // It would be awesome to show user some interface while Katya's database is
            // struggling to make responses to our login demands.
            pdLoading.setMessage("\tConnecting...");
            pdLoading.show();

        }

        @Override
        protected Void doInBackground(Void... params) {
            String offerURL = "http://192.168.0.101:8080/company/updateoffer/"+offerID+"/"+id+"/"+rule+"/"+name+"/"+hashtag;

            String result;
            JSONObject response;
            try {
                // I think that should do... nothing should go wrong
                result = Connection.putConnection(offerURL);
                response = new JSONObject(result);

                //but if something goes wrong...
                if(response.getBoolean("result")){

                }else{
                    error = true;
                }


            } catch (IOException e) {
                Log.e("NAPAKA", e.toString());
                error = true;

            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }
        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);

            // We got what we wanted. Let's close progressDialog now. All hail database!
            pdLoading.dismiss();

            if(error){
                Toast.makeText(UpdateOfferActivity.this, "There was an error, please try again later", Toast.LENGTH_LONG).show();
            }else{
                Log.i("OFFER", "Offer added successfully");
                Toast.makeText(UpdateOfferActivity.this, "Offer updated successfully", Toast.LENGTH_LONG).show();
                Intent i = new Intent(ctx, CompanyMainActivity.class);
                startActivity(i);
            }


        }


        /*
         * Method for POST connection to HTTP server,
         * Method returns answer from database
         */
        /*
        public String postConnection(String URL) throws IOException {
            // To encode URL, so it works over POST connection!
            final String ALLOWED_URI_CHARS = "@#&=*+-_.,:!?()/~'%";
            URL = Uri.encode(URL, ALLOWED_URI_CHARS);
            HttpClient hc = new DefaultHttpClient();
            HttpPost httpPostLoginRequest = new HttpPost(URL);
            HttpResponse httpResponse = hc.execute(httpPostLoginRequest);
            HttpEntity httpEntity = httpResponse.getEntity();
            return EntityUtils.toString(httpEntity);
        }
        */
    }
}
