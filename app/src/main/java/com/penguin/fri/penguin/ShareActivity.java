package com.penguin.fri.penguin;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
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

import java.util.Arrays;
import java.util.List;

public class ShareActivity extends AppCompatActivity {

    private CallbackManager callbackManager;
    private LoginManager loginManager;

    EditText editTextCaption;
    Button buttonShareOnFb;
    ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share);

        buttonShareOnFb = (Button) findViewById(R.id.buttonShareOnFB); //share na fb
        buttonShareOnFb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String caption = editTextCaption.getText().toString();
                connectToFaceook();
            }
        });


        imageView = (ImageView) findViewById(R.id.imageView);
        Bitmap image = (Bitmap) getIntent().getParcelableExtra("Image");
        imageView.setImageBitmap(image);



        OfferClass offer = (OfferClass)getIntent().getSerializableExtra("OfferObject");
        editTextCaption = (EditText) findViewById(R.id.textViewCaption);
        editTextCaption.setText("#"+offer.hashtags + " " + "#contestPlace");
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
                Toast.makeText(getApplicationContext(), "Objava na fb uspela", Toast.LENGTH_SHORT).show();
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
        callbackManager.onActivityResult(requestCode,resultCode,data);
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
}
