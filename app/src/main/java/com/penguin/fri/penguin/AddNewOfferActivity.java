package com.penguin.fri.penguin;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;


public class AddNewOfferActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_new_offer);
    }

    public void saveOfferToDB(View view) {
        //references...
        EditText edOfferName = (EditText) findViewById(R.id.etNewOfferName);
        EditText edOfferRule = (EditText) findViewById(R.id.etNewOfferRule);
        EditText edOfferHashtag = (EditText) findViewById(R.id.etNewOfferHashtag);

        // we must replace hashtag with at, because api can't handle it.
        String name = edOfferName.getText().toString();
        String rule = edOfferRule.getText().toString();
        String hashtag = edOfferHashtag.getText().toString().replaceAll("#", "@");

        //Get company info from shared
        SharedPreferences informations = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        int id = informations.getInt("id", -1);

        if (isConnected()) {
            //If everything is okay, we should run login class ... in background
            AsyncAddNewOfferTask addOfferTask = new AsyncAddNewOfferTask(name, rule, hashtag, id, this);
            addOfferTask.execute((Void) null);
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

    public class AsyncAddNewOfferTask extends AsyncTask<Void, Void, Void> {

        ProgressDialog pdLoading = new ProgressDialog(AddNewOfferActivity.this);

        private String name;
        private String rule;
        private String hashtag;
        private int id;

        private Context ctx;
        private boolean error = false;

        AsyncAddNewOfferTask(String name, String rule, String hashtag, int id, Context ctx) {
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
            String offerURL = "http://192.168.0.101:8080/company/add/" + id + "/" + name + "/" + rule + "/" + hashtag;




            try {
                // I think that should do... nothing should go wrong
                Connection.postConnection(offerURL);
                //TODO: If everything works, remove this line and postConnection Method
                 //postConnection(offerURL);

            } catch (IOException e) {
                Log.e("NAPAKA", e.toString());
                error = true;

            }
            return null;
        }
        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);

            // We got what we wanted. Let's close progressDialog now. All hail database!
            pdLoading.dismiss();

            if(error){
                Toast.makeText(AddNewOfferActivity.this, "There was an error, please try again later", Toast.LENGTH_LONG).show();
            }else{
                Log.i("OFFER", "Offer added successfully");
                Toast.makeText(AddNewOfferActivity.this, "Offer added successfully", Toast.LENGTH_LONG).show();
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
