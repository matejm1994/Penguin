package com.penguin.fri.penguin;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
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
        Boolean isCompany = sCompany.isChecked();

        // A little bit lazy... will implement this later.
        //TODO: Check for valid email address. Now, let's just assume that email is correct
        //TODO: We should probably check for password strength too...

        //If everything is okay, we should run login class ... in background
        AsyncUserLoginTask loginTask = new AsyncUserLoginTask(email, password, isCompany, this);
        loginTask.execute((Void) null);

    }

    public void userRegister(View view) {
        //Run new intent with extra information isCompany - company or user
        Intent registerIntent = new Intent(this, RegisterActivity.class);
        Switch sCompany = (Switch) findViewById(R.id.sCompany);
        registerIntent.putExtra("isCompany", sCompany.isChecked() ? "true" : "false");
        startActivity(registerIntent);

    }

    public class AsyncUserLoginTask extends AsyncTask<Void, Void, Void> {

        ProgressDialog pdLoading = new ProgressDialog(LoginActivity.this);

        boolean loginToCompanySuccesful = false;
        String errorMessage = "";

        private String email = "";
        private String password = "";
        private boolean isCompany = false;

        Context ctx;
        int userID = -1;


        AsyncUserLoginTask(String email, String password, boolean isCompany, Context ctx) {
            this.email = email;
            this.password = password;
            this.isCompany = isCompany;
            this.ctx = ctx;

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
            // prej: 10.0.2.2 potem 192.168.0.101
            String loginURL = isCompany ? "http://192.168.0.101:8080/company/login/" + email +
                    "/" + password : "http://192.168.0.101:8080/login/" + email + "/" + password;
            String result;
            JSONObject response;
            boolean loginSuccessful;


            Log.i("URL", loginURL);


            try {
                // Try connecting to database and get a response
                //TODO: If everything works, remove this line and postConnection Method
                result = Connection.postConnection(loginURL);
                //result = postConnection(loginURL);
                response = new JSONObject(result);
                loginSuccessful = response.getBoolean("result");

                if (loginSuccessful) {
                    //If login is successful, save data to shared preferences
                    saveLoginResponse(isCompany, email, response);

                    if (isCompany) {
                        loginToCompanySuccesful = true;
                    }

                    //get user or company ID to pass it to new class
                    userID = response.getInt("id");

                    Log.i("LOGIN", "Login successful");
                } else {
                    Log.i("LOGIN", "Login failed");
                    //loginToCompanySuccesful is false, so we can inform user in postExecute method
                    errorMessage = "Login failed. Please check your password/username and try again.";
                }


            } catch (IOException e) {
                Log.e("NAPAKA", e.toString());
                errorMessage = "Whops!. Somewhere something went wrong. Please try again in few minutes. ";
            } catch (JSONException e) {
                errorMessage = "Whops!. Somewhere something went wrong. Please try again in few minutes. ";
                e.printStackTrace();
            }


            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);

            // We got what we wanted. Let's close progressDialog now. All hail database!
            pdLoading.dismiss();

            if (isCompany) {
                //Open CompanyMainActivity class or inform user that there is some error.
                if (loginToCompanySuccesful) {
                    Intent intent = new Intent(ctx, CompanyMainActivity.class);
                    startActivity(intent);

                    Log.i("LOGIN", "redirecting...");
                } else {
                    Toast.makeText(ctx, "Please check ", Toast.LENGTH_LONG).show();
                }

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

        /*
         * Method for saving Login response to sharedPreferences
         * BOOLEAN: isCompany   <- if user who is logged in represent a company or not
         * STRING:  mail        <- email of logged in user or company
         * INT:     id          <- id of logged in user or company
         * TODO: Add additional info...
         */
        private void saveLoginResponse(boolean isCompany, String mEmail, JSONObject response) throws IOException, JSONException {

            //for now, this will be enough
            String email = mEmail;
            int id = response.getInt("id");

            //let's save it..
            SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            SharedPreferences.Editor editor = settings.edit();
            editor.putBoolean("company", isCompany);
            editor.putString("mail", email);
            editor.putInt("id", id);


            if (isCompany) {
                String name = response.getString("name");
                String address = response.getString("address");
                editor.putString("name",name);
                editor.putString("address",address);
            }
            editor.commit();
        }


    }
}
