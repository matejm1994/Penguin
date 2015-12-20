package com.penguin.fri.penguin;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
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

import javax.xml.datatype.Duration;

public class RegisterActivity extends Activity {

    boolean isCompany = false;

    EditText etEmail;
    EditText etPassword;
    EditText etName;
    EditText etAddress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        //if it is company = true, else is false
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            isCompany = extras.getString("isCompany").equals("true");
        }



        etEmail = (EditText) findViewById(R.id.etRegisterEmail);
        etPassword = (EditText) findViewById(R.id.etRegisterPassword);
        etName = (EditText) findViewById(R.id.etRegisterName);
        etAddress = (EditText) findViewById(R.id.etRegisterAddress);

        if (!isCompany) {
            TextView tvName = (TextView) findViewById(R.id.tvName);
            TextView tvAddress = (TextView) findViewById(R.id.tvAddress);
            etName.setVisibility(View.GONE);
            etAddress.setVisibility(View.GONE);
            tvAddress.setVisibility(View.GONE);
            tvName.setVisibility(View.GONE);
        }
    }

    /**
     * When user click the registration button...
     */
    public void userRegistration(View view) {


        String email = etEmail.getText().toString();
        String password = etPassword.getText().toString();
        String name = "";
        String address = "";
        if (isCompany) {
            name = etName.getText().toString();
            address = etAddress.getText().toString();
        }

        // A little bit lazy... will implement this later.
        //TODO: Check for valid email address. Now, let's just assume that email is correct
        //TODO: We should probably check for password strength too...
        // TODO: None of the fields must be empty.

        //If everything is okay, we should run registration clas ... in background
        AsyncUserRegistrationTask registrationTask;
        if (isCompany) {
            registrationTask = new AsyncUserRegistrationTask(email, password, name, address);
        } else {
            registrationTask = new AsyncUserRegistrationTask(email, password);
        }
        registrationTask.execute((Void) null);


    }

    public class AsyncUserRegistrationTask extends AsyncTask<Void, Void, Void> {

        ProgressDialog pdLoading = new ProgressDialog(RegisterActivity.this);

        private String email = "";
        private String password = "";
        private boolean isCompany = false;
        private String name = "";
        private String address = "";

        boolean openLoginActivity = false;
        String errorMessage = "";


        AsyncUserRegistrationTask(String email, String password) {
            this.email = email;
            this.password = password;
            this.isCompany = false;
        }

        AsyncUserRegistrationTask(String email, String password, String name, String address) {
            this.email = email;
            this.password = password;
            this.name = name;
            this.address = address;
            this.isCompany = true;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            // It would be awesome to show user some interface while Katya's database is
            // struggling to make responses to our register demands.
            pdLoading.setMessage("\tLoading...");
            pdLoading.show();

        }


        @Override
        protected Void doInBackground(Void... params) {
            // prej: 10.0.2.2 potem 192.168.0.101
            String registerURL = isCompany ? "http://192.168.0.101:8080/company/register/" + email +
                    "/" + password + "/" + name + "/" + address : "http://192.168.0.101:8080/register/"
                    + email + "/" + password;
            String result;
            JSONObject response;
            boolean registerSuccessful;

            try {
                // Trying to connect to database and register new user or company
                result = postConnection(registerURL);
                response = new JSONObject(result);
                if (isCompany) {
                    registerSuccessful = response.getBoolean("status");
                } else {
                    registerSuccessful = response.getBoolean("result");
                }

                if (registerSuccessful) {
                    //If registration is successful, we must log in but for now,
                    // just open the LoginActivity (at onPostExecute method)
                    // TODO: Change api so that register return ID of company/user
                    openLoginActivity = true;
                } else {
                    String error = "";
                    //Get error message.
                    if (isCompany) {
                        error = response.getString("response");
                    } else {
                        error = response.getString("result");
                    }

                    //Set up what kind of error we show to user
                    if (error.equals("User Already exists!") || error.equals("Company Already exists!")) {
                        errorMessage = (isCompany ? "Company" : "User") + "with this email already exist";
                    } else if (error.equals("Registration failed")) {
                        errorMessage = "Registration failed. Please check your internet connection and try again";
                    } else if (error.equals("Registration Failed, wrong email format")) {
                        errorMessage = "Wrong email format";
                    } else {
                        //An error has occurred!
                        errorMessage = "Somewhere something went wrong. Please try again later or contact us on our email address";
                    }

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

            //if registration was successful, lets open loginActivity
            if (openLoginActivity) {
                Intent login = new Intent(RegisterActivity.this, LoginActivity.class);
                startActivity(login);
            } else {
                Toast.makeText(RegisterActivity.this, errorMessage, Toast.LENGTH_LONG);
            }

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
    }


}

