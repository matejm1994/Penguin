package com.penguin.fri.penguin;

import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    }

    public void userLogin(View view) {
        EditText etEmail = (EditText) findViewById(R.id.etEmail);
        EditText etPassword = (EditText) findViewById(R.id.etPassword);
        Switch sCompany = (Switch) findViewById(R.id.sCompany);

        String email = etEmail.getText().toString();
        String password = etPassword.getText().toString();
        Boolean isCompany = sCompany.isActivated();

        // A little bit lazy... will implement this later.
        //TODO: Check for valid email address. Now, let's just assume that email is correct
        //TODO: We should probably check for password strength too...

        //If everything is okay, we should run login method ... in background
        AsyncUserLoginTask loginTask = new AsyncUserLoginTask(email, password, isCompany);
        loginTask.execute((Void) null);

    }

    public class AsyncUserLoginTask extends AsyncTask<Void, Void, Void> {

        ProgressDialog pdLoading = new ProgressDialog(LoginActivity.this);

        private String email = "";
        private String password = "";
        private boolean isCompany = false;

        AsyncUserLoginTask(String email, String password, boolean isCompany) {
            this.email = email;
            this.password = password;
            this.isCompany = isCompany;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            // It would be awesome to show user some interface while Katya's database is
            // struggling to make responses to our login demands.
            pdLoading.setMessage("\tLoading...");
            pdLoading.show();

        }


        @Override
        protected Void doInBackground(Void... params) {

            String loginURL = isCompany ? "http://10.0.2.2:8080/company/login/" + email +
                    "/" + password : "http://10.0.2.2:8080/login/" + email + "/" + password;
            String result;
            JSONObject response;
            boolean loginSuccessful;

            try {
                // Try connecting to database and get a response
                result = postConnection(loginURL);
                response = new JSONObject(result);
                loginSuccessful = response.getBoolean("result");

                if(loginSuccessful){
                    //If login is successful, save data to shared preferences
                    saveLoginResponse(isCompany, email, response);

                    Log.i("LOGIN", "Login successful");
                }else{
                    //TODO: Inform user to check email and password
                    Log.i("LOGIN", "Login failed");
                }


            } catch (IOException e) {
                e.printStackTrace();
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
        }



        /*
         * Method for POST connection to HTTP server,
         * Method returns answer from database
         */
        public String postConnection(String URL) throws IOException {
            HttpClient hc = new DefaultHttpClient();
            HttpPost httpPostLoginRequest = new HttpPost(URL);
            HttpResponse httpResponse = hc.execute(httpPostLoginRequest);
            HttpEntity httpEntity = httpResponse.getEntity();
            return EntityUtils.toString(httpEntity);
        }


        /*
         * Method for saving Login response to sharedPreferences
         * BOOLEAN: isCompany   <- if user who is logged in represent a company or not
         * STRING:  mail        <- email of logged in user or company
         * INT:     id          <- id of logged in user or company
         */
        private void saveLoginResponse(boolean isCompany, String mEmail, JSONObject response) throws IOException, JSONException {

            //for now, this will be enough
            String email = mEmail;
            int id = response.getInt("id");

            //let's save it..
            SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            SharedPreferences.Editor editor  = settings.edit();
            editor.putBoolean("company", isCompany);
            editor.putString("mail",email);
            editor.putInt("id",id);
            editor.commit();

            // TODO: If user IS eventually a company, we should save more information
            if (isCompany) {

            }
        }


    }
}
