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

        final EditText etOfferName = (EditText)findViewById(R.id.etCompanyAddOfferName);
        final EditText etOfferRules = (EditText)findViewById(R.id.etCompanyAddOfferRule);

        //remove this line AFTER implementation of login
        final EditText etID = (EditText)findViewById(R.id.etCompanyNewOfferID);

        // Button to add new offer for company
        Button button = (Button) findViewById(R.id.bCompanyAddOffer);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String companyOfferName = etOfferName.getText().toString();
                String companyOfferRules = etOfferRules.getText().toString();

                if(companyOfferName.length()==0 || companyOfferRules.length() == 0){
                    Log.e("NAPAK", "Niso vsa polja....");

                }else if(etID.getText().toString().length()==0){
                    //remove this IF statement AFTER implementation of login
                    // finding company ID should be automatic
                    Log.e("NAPAK", "Niso vsa polja....mora biti tudi ID - zacasno");
                }else{
                    Log.e("JAAAA", etID.getText().toString());
                    Log.e("JAAAA", companyOfferName);
                    Log.e("JAAAA", companyOfferRules);
                   new AddNewOfferTask().execute(etID.getText().toString(),companyOfferName,companyOfferRules);
                }
            }
        });

    }


    public class AddNewOfferTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            try {
                HttpClient httpclient = new DefaultHttpClient();
                Log.e("URLLL","http://10.0.2.2:8080/company/add/"+params[0]+"/"+params[1]+"/"+params[2]);
                HttpPost httppost = new HttpPost("http://10.0.2.2:8080/company/add/"+params[0]+"/"+params[1]+"/"+params[2]);


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


