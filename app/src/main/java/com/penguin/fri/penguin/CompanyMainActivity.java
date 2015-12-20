package com.penguin.fri.penguin;

import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EdgeEffect;
import android.widget.EditText;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;

import javax.xml.datatype.Duration;

public class CompanyMainActivity extends Activity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_company_main);

        Bundle b = getIntent().getExtras();
        final int companyID = b.getInt("id");

        final EditText etOfferName = (EditText)findViewById(R.id.etCompanyAddOfferName);
        final EditText etOfferRules = (EditText)findViewById(R.id.etCompanyAddOfferRule);

        // Button to add new offer for company
        Button button = (Button) findViewById(R.id.bCompanyAddOffer);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String companyOfferName = etOfferName.getText().toString();
                String companyOfferRules = etOfferRules.getText().toString();

                if(companyOfferName.length()==0 || companyOfferRules.length() == 0){
                    Log.e("NAPAK", "Niso vsa polja....");

                }else{
                   new AddNewOfferTask().execute(Integer.toString(companyID),companyOfferName,companyOfferRules);
                }
            }
        });

    }


    public class AddNewOfferTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            try {
                HttpClient httpclient = new DefaultHttpClient();
                Log.e("URLLL","http://192.168.0.101:8080/company/add/"+params[0]+"/"+params[1]+"/"+params[2]);
                HttpPost httppost = new HttpPost("http://192.168.0.101:8080/company/add/"+params[0]+"/"+params[1]+"/"+params[2]);


                // Execute HTTP Post Request
                HttpResponse response = httpclient.execute(httppost);

                return "Koncano";
            } catch (Exception e) {
                e.printStackTrace();

            }
            return "Ni koncano";

        }
    }
}


