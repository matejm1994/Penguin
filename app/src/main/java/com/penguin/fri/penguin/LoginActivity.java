package com.penguin.fri.penguin;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.app.LoaderManager.LoaderCallbacks;

import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;

import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
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
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import static android.Manifest.permission.READ_CONTACTS;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity implements LoaderCallbacks<Cursor> {


    public static boolean IS_COMPANY = false;

    /**
     * Id to identity READ_CONTACTS permission request.
     */
    private static final int REQUEST_READ_CONTACTS = 0;

    /**
     * A dummy authentication store containing known user names and passwords.
     * TODO: remove after connecting to a real authentication system.
     */
    private static final String[] DUMMY_CREDENTIALS = new String[]{
            "foo@example.com:hello", "bar@example.com:world"
    };
    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private UserLoginTask mAuthTask = null;

    // UI references.
    private AutoCompleteTextView mEmailView;
    private EditText mPasswordView;
    private TextInputLayout TextInputLayoutmNameView;
    private TextInputLayout TextInputLayoutmAddressView;
    private EditText mName;
    private EditText mAddress;
    private CheckBox checkBox;
    private View mProgressView;
    private View mLoginFormView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        setupActionBar();
        // Set up the login form.
        mEmailView = (AutoCompleteTextView) findViewById(R.id.email);
        populateAutoComplete();

        mPasswordView = (EditText) findViewById(R.id.password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        mName = (EditText) findViewById(R.id.name);
        mAddress = (EditText) findViewById(R.id.address);

        TextInputLayoutmNameView = (TextInputLayout) findViewById(R.id.textInputLayoutName);
        TextInputLayoutmAddressView = (TextInputLayout) findViewById(R.id.textInputLayoutAddress);
        checkBox = (CheckBox) findViewById(R.id.checkBox);
        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (checkBox.isChecked()) {
                    IS_COMPANY = true;
                    TextInputLayoutmNameView.setVisibility(View.VISIBLE);
                    TextInputLayoutmAddressView.setVisibility(View.VISIBLE);
                    TextInputLayoutmNameView.requestLayout();
                    TextInputLayoutmAddressView.requestLayout();
                } else {
                    IS_COMPANY = false;
                    TextInputLayoutmNameView.setVisibility(View.GONE);
                    TextInputLayoutmAddressView.setVisibility(View.GONE);
                    TextInputLayoutmNameView.requestLayout();
                    TextInputLayoutmAddressView.requestLayout();
                }
            }
        });


        Button mEmailSignInButton = (Button) findViewById(R.id.email_sign_in_button);
        mEmailSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);
    }

    private void populateAutoComplete() {
        if (!mayRequestContacts()) {
            return;
        }

        getLoaderManager().initLoader(0, null, this);
    }

    private boolean mayRequestContacts() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }
        if (checkSelfPermission(READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        if (shouldShowRequestPermissionRationale(READ_CONTACTS)) {
            Snackbar.make(mEmailView, R.string.permission_rationale, Snackbar.LENGTH_INDEFINITE)
                    .setAction(android.R.string.ok, new View.OnClickListener() {
                        @Override
                        @TargetApi(Build.VERSION_CODES.M)
                        public void onClick(View v) {
                            requestPermissions(new String[]{READ_CONTACTS}, REQUEST_READ_CONTACTS);
                        }
                    });
        } else {
            requestPermissions(new String[]{READ_CONTACTS}, REQUEST_READ_CONTACTS);
        }
        return false;
    }

    /**
     * Callback received when a permissions request has been completed.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == REQUEST_READ_CONTACTS) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                populateAutoComplete();
            }
        }
    }

    /**
     * Set up the {@link android.app.ActionBar}, if the API is available.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void setupActionBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            // Show the Up button in the action bar.
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {
        if (mAuthTask != null) {
            return;
        }

        // Reset errors.
        mEmailView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String email = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();
        String name = mName.getText().toString();
        String address = mAddress.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        } else if (!isEmailValid(email)) {
            mEmailView.setError(getString(R.string.error_invalid_email));
            focusView = mEmailView;
            cancel = true;
        }



        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);
            mAuthTask = new UserLoginTask(email, password, name, address);
            mAuthTask.execute((Void) null);
        }
    }

    private boolean isEmailValid(String email) {
        //TODO: Replace this with your own logic
        return email.contains("@");
    }

    private boolean isPasswordValid(String password) {
        //TODO: Replace this with your own logic
        return password.length() > 4;
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return new CursorLoader(this,
                // Retrieve data rows for the device user's 'profile' contact.
                Uri.withAppendedPath(ContactsContract.Profile.CONTENT_URI,
                        ContactsContract.Contacts.Data.CONTENT_DIRECTORY), ProfileQuery.PROJECTION,

                // Select only email addresses.
                ContactsContract.Contacts.Data.MIMETYPE +
                        " = ?", new String[]{ContactsContract.CommonDataKinds.Email
                .CONTENT_ITEM_TYPE},

                // Show primary email addresses first. Note that there won't be
                // a primary email address if the user hasn't specified one.
                ContactsContract.Contacts.Data.IS_PRIMARY + " DESC");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        List<String> emails = new ArrayList<>();
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            emails.add(cursor.getString(ProfileQuery.ADDRESS));
            cursor.moveToNext();
        }

        addEmailsToAutoComplete(emails);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {

    }

    private interface ProfileQuery {
        String[] PROJECTION = {
                ContactsContract.CommonDataKinds.Email.ADDRESS,
                ContactsContract.CommonDataKinds.Email.IS_PRIMARY,
        };

        int ADDRESS = 0;
        int IS_PRIMARY = 1;
    }


    private void addEmailsToAutoComplete(List<String> emailAddressCollection) {
        //Create adapter to tell the AutoCompleteTextView what to show in its dropdown list.
        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(LoginActivity.this,
                        android.R.layout.simple_dropdown_item_1line, emailAddressCollection);

        mEmailView.setAdapter(adapter);
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {

        private final String mEmail;
        private final String mPassword;
        private final String mName;
        private final String mAddress;

        boolean company = checkBox.isChecked();

        UserLoginTask(String email, String password, String name, String address) {
            mEmail = email;
            mPassword = password;
            mName = name;
            mAddress = address;
        }


        @Override
        protected Boolean doInBackground(Void... params) {
            // TODO: attempt authentication against a network service.

            String result = "";
            boolean loginSucessful = false;
            boolean registerSucessful = false;
            String loginError = "";

            JSONObject response;

            if (IS_COMPANY) {
                String companyLoginURL = "http://10.0.2.2:8080/company/login/" + mEmail + "/" + mPassword;

                //get result from server
                try {
                    result = postConnection(companyLoginURL);
                    response = new JSONObject(result);
                    loginSucessful = response.getBoolean("result");
                    loginError = response.getString("response");

                    if(loginSucessful){
                        //Company is already in database. We just sign in!
                        //TODO: Save to some global class or better in shared preferences.
                        Log.i("LOGIN", "Login sucessfull. Hello Company :)");
                    }else{
                        //Login is already registered, but apparently someone mistyped password and should try again.
                        if(loginError.equals("Login Failed, wrong password")){
                            Log.i("LOGIN", "Login failed. Check you password and try again.");
                            return false;
                        }

                        //Company email is not in database. We will register and then sign in.
                        String companyRegisterURL = "http://10.0.2.2:8080/company/register/" + mEmail + "/" + mPassword;
                        result = postConnection(companyRegisterURL);
                        response = new JSONObject(result);
                        registerSucessful = response.getBoolean("status");

                        if(registerSucessful){
                            // Registration successfully completed
                            Log.i("LOGIN", "Registration sucessfull. You are also signed in app now.");
                            //TODO: Save to some global class or better in shared preferences. So the company will be signed into app
                        }else{
                            // O stari, o fak, o ne me je*bat :), somewhere something went wrong and registration failed!
                            Log.i("LOGIN", "Registration failed. Please try again and check email and password");
                            return false;
                        }
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }else{ // if it is user, not company

                String userLoginURL = "http://10.0.2.2:8080/login/" + mEmail + "/" + mPassword;

                //get result from server
                try {
                    result = postConnection(userLoginURL);
                    response = new JSONObject(result);
                    loginSucessful = response.getBoolean("result");

                    if(loginSucessful){
                        //User is already in database. We just sign in!
                        //TODO: Save to some global class or better in shared preferences.
                        Log.i("LOGIN","Login sucessfull. Hello User :)");

                    }else{
                        //Login is already registered, but apparently someone mistyped password and should try again.
                        if(loginError.equals("Login Failed, wrong password")){
                            Log.i("LOGIN", "Login failed. Check you password and try again.");
                            return false;
                        }
                        //User is not in database. We will register and then sign in.
                        String userRegisterURL = "http://10.0.2.2:8080/register/" + mEmail + "/" + mPassword;
                        result = postConnection(userRegisterURL);
                        response = new JSONObject(result);
                        registerSucessful = response.getBoolean("result");

                        if(registerSucessful){
                            // Registration successfully completed

                            Log.i("LOGIN", "Registration sucessfull. You are also signed in app now.");
                            //TODO: Save to some global class or better in shared preferences. So the company will be signed into app
                        }else{
                            // O stari, o fak, o ne me je*bat :), somewhere something went wrong and registration failed!
                            Log.i("LOGIN", "Registration failed. Please try again and check email and password");
                            return false;
                        }
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                Log.i("LOGIN", "You are now signed in.");
                return true;
            }

            /*
            String URLCompanyAndUserLogin = "http://10.0.2.2:8080/login/" + mEmail + "/" + mPassword; //login NI za oba je enak
            String URLUserRegister = "http://10.0.2.2:8080/register/" + mEmail + "/" + mPassword; //register za userja
            String URLCompanyRegister = "http://10.0.2.2:8080/company/register/" + mEmail + "/" + mPassword +
                    "/" + mName + "/" + mAddress; //registracija za podjetje


            String result = "";

            try {
                //login
                result = postConnection(URLCompanyAndUserLogin);
                JSONObject responeString = new JSONObject(result);
                String response = responeString.getString("response");

                if (response.equals("Login Sucess")) {
                    return true;
                }
                if (response.equals("Login Failed, user does not exist!")) {//ce user ne obstaja ga registriramo
                    //nastavimo primeren URL za registracijo
                    String URLregister = company ? URLCompanyRegister : URLUserRegister;
                    URLregister = URLregister.replaceAll(" ", "%20"); //potrebno zaradi presledkov
                    //URLregister = URLEncoder.encode(URLregister, "UTF-8");
                    String registrationResponse = postConnection(URLregister); //registracija
                    Log.e("Registracija", registrationResponse);
                    String loginResponse = postConnection(URLCompanyAndUserLogin); //login
                    Log.e("Registracija", loginResponse);
                    return true;
                }
                if (response.equals("Login Failed, wrong password")) {
                    return false;
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

            */
            // TODO: register the new account here.
            return true;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mAuthTask = null;
            showProgress(false);
            if (success) {
                finish();
            } else {
                mPasswordView.setError(getString(R.string.error_incorrect_password));
                mPasswordView.requestFocus();
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
        }

        //post klic
        public String postConnection(String URL) throws IOException {
            HttpClient hc = new DefaultHttpClient();
            HttpPost httpPostLoginRequest = new HttpPost(URL);
            HttpResponse httpResponse = hc.execute(httpPostLoginRequest);
            HttpEntity httpEntity = httpResponse.getEntity();
            return EntityUtils.toString(httpEntity);
        }
    }


}

