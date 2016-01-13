package com.penguin.fri.penguin;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class PrikazPonudbePodjetjaActivity extends AppCompatActivity {
    private static final int CAM_REQUEST = 1313;
    TextView textViewPonudba;
    ListView list;
    String[] web;
    Integer[] imageId;
    OfferClass[] offersArray; //tabela za akcije
    int offerPosition = 0;
    Button buttonChallenegeAccepted;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_prikaz_ponudbe_podjetja);

        buttonChallenegeAccepted = (Button) findViewById(R.id.buttonChallengeAccepted);
        buttonChallenegeAccepted.setOnClickListener(new ButtonTakePhotoClicker());


        Intent intent = getIntent();
        CompanyClass company = (CompanyClass)intent.getSerializableExtra("CompanyObject");
        textViewPonudba = (TextView)findViewById(R.id.textVirwOfferInfo);

        RESTCallTaskGetOffers restCallTaskGetOffers = new RESTCallTaskGetOffers();
        restCallTaskGetOffers.execute(String.valueOf(company.id));


        //Toast.makeText(this, "Podjetje"+company.name, Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_prikaz_ponudbe_podjetja, menu);
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
            //User choose Settings
            Intent intentSettings = new Intent(this, SettingsActivity.class);
            startActivity(intentSettings);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }




    //prikaz ponudb podjetja
    private class RESTCallTaskGetOffers extends AsyncTask<String, Void, String[]> { //testni za registracijo
        private String URLCompanyOffer = "http://10.0.2.2:8080/offers/"; // za seznam

        @Override
        protected String[] doInBackground(String... params) {
            String idPodtjetja = params[0];
            URLCompanyOffer+=idPodtjetja; //dodamo podatek za katero podjetje gre

            HttpClient hc = new DefaultHttpClient();
            String resultHttpRequest = null;
            String [] resultFinal = null;

            try {

                //request za ponudbe
                HttpGet getRequest = new HttpGet(URLCompanyOffer);
                HttpResponse response = hc.execute(getRequest);
                HttpEntity entity = response.getEntity();
                resultHttpRequest = EntityUtils.toString(entity);

                JSONObject jsonObjectResponse = new JSONObject(resultHttpRequest);
                //JSONArray jsonArrayResponse = jsonObjectResponse.getJSONArray("data");
                JSONArray jsonArray = jsonObjectResponse.getJSONArray("data");

                StringBuffer sb = new StringBuffer();
                resultFinal = new String[jsonArray.length()]; //nastavimo velikost tabele

                web = new String[resultFinal.length];
                imageId = new Integer[resultFinal.length];
                offersArray = new OfferClass[jsonArray.length()]; //tabela za akcije

                //za imena ponudb
                for (int i=0; i<jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);

                    web[i] = jsonObject.getString("name");

                    //slika podjetja
                    imageId[i] = getApplicationContext().
                            getResources().
                            getIdentifier("image" + idPodtjetja, "drawable", getApplicationContext().
                                    getPackageName());

                    offersArray[i] = new OfferClass(jsonObject.getString("id"),
                            jsonObject.getString("company_id"), jsonObject.getString("rules"),
                            jsonObject.getString("name"),jsonObject.getString("hashtags"),
                            jsonObject.getString("prize"), jsonObject.getString("start"),
                            jsonObject.getString("finish"), jsonObject.getString("extra")
                    );

                }

            } catch (Exception e) {
                e.printStackTrace();
            }

            return resultFinal;
        }

        @Override
        protected void onPostExecute(String[] result) {
                if (web != null){
                    listViewInit();
                    offerSet(0);
                }


               // for (int i=0; i<result.length; i++){
                //    textViewPonudba.setText(textViewPonudba.getText()+result[i]);
              //  }
        }
    }

    private void offerSet(int position) {
        //nastavimo ponudbo prvega offerja
        if (offersArray != null && offersArray.length>0){
            textViewPonudba.setText("Prize: "+offersArray[position].prize+"" +
                            "\n"+"Rules: "+offersArray[position].rules
                            +"\n" +"Hashtags: #"+offersArray[position].hashtags+" #contestPlace"+
                            "\n"+"Start: "+offersArray[position].start+
                            "\n"+"Finish: "+offersArray[position].finish
                    );
            offerPosition = position;
        }

    }


    private void listViewInit() {
        CustomList adapter = new
                CustomList(this, web, imageId);
        list = (ListView) findViewById(R.id.listViewPrikazPonudbPodjetja); //popravi
        list.setAdapter(adapter);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {



            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {


                offerSet(+position);
                //Intent intent = new Intent(getApplicationContext(), ShareActivity.class);
                //intent.putExtra("OfferObject", offersArray[+position]);
                //startActivity(intent);

                 //Toast.makeText(view.getContext() , "You Clicked at " + web[+position], Toast.LENGTH_SHORT).show();
            }
        });
    }

    class ButtonTakePhotoClicker implements Button.OnClickListener{

        @Override
        public void onClick(View v) {


            Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            startActivityForResult(cameraIntent, CAM_REQUEST);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CAM_REQUEST && resultCode==RESULT_OK){ //ce je slikanje uspelo poslji v naslednji acitity
            Bitmap bitmapSlika = (Bitmap) data.getExtras().get("data");

            /*/shranjevanje v file
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            bitmapSlika.compress(Bitmap.CompressFormat.PNG, 100, bytes);
            //4
            File file = new File(Environment.getExternalStorageDirectory()+File.separator + "image.jpg");
            try {
                file.createNewFile();
                FileOutputStream fo = new FileOutputStream(file);
                //5
                fo.write(bytes.toByteArray());
                fo.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            */

            Intent intent = new Intent(getApplicationContext(), ShareActivity.class);
            OfferClass offerClass = offersArray[0];
            intent.putExtra("OfferObject", offersArray[offerPosition]);
            intent.putExtra("Image", bitmapSlika);
            startActivity(intent);
        }
    }







}
