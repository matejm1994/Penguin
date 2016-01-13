package com.penguin.fri.penguin;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.share.ShareApi;
import com.facebook.share.model.SharePhoto;
import com.facebook.share.model.SharePhotoContent;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class ShareActivity extends AppCompatActivity {

    private CallbackManager callbackManager;
    private LoginManager loginManager;

    EditText editTextCaption;
    Button buttonShareOnFb;
    ImageView imageView;
    OfferClass offer;
    Button buttonRetakePhoto;
    private static final int CAM_REQUEST = 1313;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share);

        offer = (OfferClass)getIntent().getSerializableExtra("OfferObject");

        buttonShareOnFb = (Button) findViewById(R.id.buttonShareOnFB); //share na fb
        buttonShareOnFb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String caption = editTextCaption.getText().toString();

                if (caption.toLowerCase().contains(offer.hashtags.toLowerCase()) && caption.toLowerCase().contains("#contestplace")){
                    connectToFaceook();
                    if (!offer.extras.equals("null")){ // ce imamo offer pri katerem stejemo slikce
                        RESTSetPhotoCount restSetPhotoCount = new RESTSetPhotoCount();
                        restSetPhotoCount.execute(offer.id); //ustrezno povecaj stevilo slik glede na izziv
                    }
                }else {
                    Toast.makeText(ShareActivity.this,
                            "Caption must contain hashtags "+offer.hashtags+" and "+"#contestPlace",
                            Toast.LENGTH_LONG).show();
                }



            }
        });


        imageView = (ImageView) findViewById(R.id.imageView);
        Bitmap image = (Bitmap) getIntent().getParcelableExtra("Image");
        imageView.setImageBitmap(image);




        editTextCaption = (EditText) findViewById(R.id.textViewCaption);
        editTextCaption.setText("#" + offer.hashtags + " " + "#contestPlace");

        RESTAddOfferToUsersOffers restAddOfferToUsersOffers = new RESTAddOfferToUsersOffers();
        restAddOfferToUsersOffers.execute(offer.id);


        buttonRetakePhoto = (Button) findViewById(R.id.buttonRetakePhoto);
        buttonRetakePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(cameraIntent, CAM_REQUEST);
            }
        });


    }

    private void connectToFaceook(){
        FacebookSdk.sdkInitialize(getApplicationContext());//povezava s facebookom
        callbackManager = CallbackManager.Factory.create();
        List<String> permissionNeeds = Arrays.asList("publish_actions");
        LoginManager manager = LoginManager.getInstance(); //this loginManager helps you eliminate adding a LoginButton to your UI
        manager.logInWithPublishPermissions(this, permissionNeeds);
        manager.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                sharePhotoToFacebook();
                Toast.makeText(getApplicationContext(), "your challenge was successfully posted on Facebook!", Toast.LENGTH_SHORT).show();


            }

            @Override
            public void onCancel() {
                Log.i("FB error", "prijava preklicana");
            }

            @Override
            public void onError(FacebookException e) {
                Log.i("FB error", "prijava ni uspela");
            }
        });
    }

    private void sharePhotoToFacebook() {
        Bitmap image = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
        SharePhoto photo = new SharePhoto.Builder()
                .setBitmap(image)
                .setCaption(editTextCaption.getText().toString()) //nastavimo bojavljeno besedilo
                .build();

        SharePhotoContent content = new SharePhotoContent.Builder()
                .addPhoto(photo)
                .build();


        ShareApi.share(content, null);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


        if (requestCode == CAM_REQUEST && resultCode==RESULT_OK){ //ce je ponovno zajemanje slike uspelo nastavi sliko
            Bitmap bitmapSlika = (Bitmap) data.getExtras().get("data");
            imageView.setImageDrawable(null);
            imageView.setImageBitmap(bitmapSlika);
        }else if (requestCode == CAM_REQUEST && resultCode==RESULT_CANCELED) {
        }
        else{
            callbackManager.onActivityResult(requestCode, resultCode, data);
        }



    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_share, menu);
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
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private class RESTAddOfferToUsersOffers extends AsyncTask<String, Void, Void> {
        private String URLAddOfferToUsersOffers = "http://10.0.2.2:8080/newpromo/"; //

        @Override
        protected Void doInBackground(String... params) {
            //String idPodtjetja = params[0];
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            String sharedPreferencesUserEmail = sharedPreferences.getString( "mail" , "null");

            URLAddOfferToUsersOffers+=sharedPreferencesUserEmail+"/"+params[0];

            try {
                Connection.postConnection(URLAddOfferToUsersOffers); //dodajanje ponudbe k uporabniku
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    private class RESTSetPhotoCount extends AsyncTask<String, Void, Void>{
        private String URLSetPhotoCount = "http://10.0.2.2:8080/offer/pics/"; //:userId/:offerId/:picsN";

        @Override
        protected Void doInBackground(String... params) {

            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            int sharedPreferencesID = sharedPreferences.getInt("id", -1);


            try {
                JSONObject jsonObjectOfferRules = new JSONObject(offer.extras); //pravila so shranjena v extras. po njih preverimo ce gre za pravilen cas itd


                if (jsonObjectOfferRules.getString("type").equals("time")){
                    //dobimo trenutni cas
                    Calendar rightNow = Calendar.getInstance();
                    int hour = rightNow.get(Calendar.HOUR_OF_DAY);
                    //System.out.println(hour);
                    if (hour >= Integer.valueOf(jsonObjectOfferRules.getString("clock"))) {
                        Connection.postConnection(URLSetPhotoCount + String.valueOf(sharedPreferencesID) + "/" + params[0] +"/"+ "1"); //povecamo st fotk za 1
                    }

                }


            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }
    }
}
