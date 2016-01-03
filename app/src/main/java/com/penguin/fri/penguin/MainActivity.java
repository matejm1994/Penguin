package com.penguin.fri.penguin;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Intent;



import org.json.JSONArray;
import org.json.JSONObject;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

public class MainActivity extends AppCompatActivity {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;

    //Tole je test ce github deluje | Nejc :D

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);

        /*
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        */

    }




    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id) {
            case R.id.action_settings:
                //User choose Settings
              //  Intent intentSettings = new Intent(this, CompanyMainActivity.class);
               // startActivity(intentSettings);

                return true;

            case R.id.action_login:
                //Start new Login Activity

                //TODO: Remove after implementing new login
                //Intent intentLogin = new Intent(this, LoginActivityOLD.class);
                //startActivity(intentLogin);

                Intent intentLogin = new Intent(this, LoginActivity.class);
                startActivity(intentLogin);


                return true;
            default:
                // If we get here, user action is not recognized
                break;
        }


        return super.onOptionsItemSelected(item);
    }


    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).

            Fragment myFragment = null;
            switch (position) {
                case 0:
                    myFragment = new PrikazUporabnikovihIzbranihPonudb();
                    break;
                case 1:
                    myFragment = new PrikazPonudbFragment();
                    break;
                case 2:
                    myFragment = PlaceholderFragment.newInstance(position + 1);
                    break;
            }

            return myFragment;


            //return PlaceholderFragment.newInstance(position + 1);
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "MY CONTEST PLACE";
                case 1:
                    return "CHALLANGES";
                case 2:
                    return "STATISTICS";
            }
            return null;
        }
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            TextView textView = (TextView) rootView.findViewById(R.id.section_label);
            textView.setText(getString(R.string.section_format, getArguments().getInt(ARG_SECTION_NUMBER)));
            return rootView;
        }
    }

    //fragment za prikaz ponudb.
    public static class PrikazPonudbFragment extends Fragment {

        ListView list;
        String[] web;
        Integer[] imageId;
        View rootView;

        public CompanyClass[] podjetja;

        public static PrikazPonudbFragment newInstance(int sectionNumber) {
            PrikazPonudbFragment fragment = new PrikazPonudbFragment();
            return fragment;
        }

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

            rootView = inflater.inflate(R.layout.activity_prikaz_ponudb, container, false);
            RESTCallTaskGetcompanies restCallTaskGetcompanies = new RESTCallTaskGetcompanies();
            restCallTaskGetcompanies.execute();

            return rootView;
        }

        public void listViewInit() { //inicializacija ListView-a po izvdebi requesta
            CustomList adapter = new
                    CustomList(getActivity(), web, imageId);
            list = (ListView) rootView.findViewById(R.id.list);
            list.setAdapter(adapter);
            list.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> parent, View view,
                                        int position, long id) {
                    //nova aktivnost z imenom
                    Intent intent = new Intent(view.getContext(), PrikazPonudbePodjetjaActivity.class);
                    intent.putExtra("CompanyObject", podjetja[+position]);
                    startActivity(intent);

                    // Toast.makeText(getActivity(), "You Clicked at " + web[+position], Toast.LENGTH_SHORT).show();

                }
            });
        }



        private class RESTCallTaskGetcompanies extends AsyncTask<String, Void, String[]> { //testni za registracijo
            private final String URLCompanies = "http://10.0.2.2:8080/companies"; // za seznam
            //"http://192.168.0.101/wcfservice1/Service1.svc/Messages";


            @Override
            protected String[] doInBackground(String... params) {
                Log.i("TAG", "Tu smo KKKKKK");
                HttpClient hc = new DefaultHttpClient();
                String resultHttpRequest = null;
                String[] resultFinal = null;
                try {

                    //request za seznam podjetij
                    HttpGet getRequest = new HttpGet(URLCompanies);
                    HttpResponse response = hc.execute(getRequest);
                    HttpEntity entity = response.getEntity();
                    resultHttpRequest = EntityUtils.toString(entity);
                    JSONArray jsonArray = new JSONArray(resultHttpRequest);

                    //request za ponudbe


                    //StringBuffer sb = new StringBuffer();
                    web = new String[jsonArray.length()];
                    imageId = new Integer[jsonArray.length()];
                    podjetja = new CompanyClass[jsonArray.length()];

                    //za imena podjetij
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                        String line = //popravi
                                jsonObject.getString("name") + ", " + jsonObject.getString("email");
                        //sb.append(line + "\n");

                        //podatki o podjetju
                        web[i] = line;

                        //slika podjetja
                        imageId[i] = getContext().
                                getResources().
                                getIdentifier("image" + jsonObject.getString("id"), "drawable", getContext().
                                        getPackageName());

                        //seznam objektov podjetij
                        podjetja[i] = new CompanyClass(
                                Integer.valueOf(jsonObject.getString("id")),
                                jsonObject.getString("name"),
                                jsonObject.getString("email"));

                    }

                    resultFinal = web; //sb.toString();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                return resultFinal;
            }

            @Override
            protected void onPostExecute(String[] result) {
                if (result != null ){
                    listViewInit(); //Inicializiramo listView
                }

            }
        }

    }


    //fragment za prikaz ponudb, ki si jih je izbral uporabnik
    public static class PrikazUporabnikovihIzbranihPonudb extends Fragment{


        ListView list;
        TextView textViewUserEmail;
        String[] web = {"Prva ponudba", "druga ponudba", "tretja ponudba"};
        Integer[] imageId = {R.drawable.image1,R.drawable.image2,R.drawable.image3};
        View rootView;

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

            rootView = inflater.inflate(R.layout.activity_prikaz_uporabnikovih_izbranih_ponudb, container, false);
            textViewUserEmail = (TextView) rootView.findViewById(R.id.textViewImeUporabnika);
            RESTCallTaskGetUsersOffers restCallTaskGetUsersOffers = new RESTCallTaskGetUsersOffers();
            restCallTaskGetUsersOffers.execute();

            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(rootView.getContext());
            String sharedPreferencesEmail = sharedPreferences.getString( "mail" , "null");
            textViewUserEmail.setText(sharedPreferencesEmail);
            //listViewInit();
            return rootView;
        }


        @Override
        public void onResume() {
            super.onResume();
            RESTCallTaskGetUsersOffers restCallTaskGetUsersOffers = new RESTCallTaskGetUsersOffers();
            restCallTaskGetUsersOffers.execute();
        }

        private void listViewInit() {
            CustomList adapter = new
                    CustomList(getActivity(), web, imageId);
            list = (ListView) rootView.findViewById(R.id.listUsersOffers);
            list.setAdapter(adapter);
            list.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> parent, View view,
                                        int position, long id) {
                    //nova aktivnost z imenom
                    //Intent intent = new Intent(view.getContext(), PrikazPonudbePodjetjaActivity.class);
                    //intent.putExtra("CompanyObject", podjetja[+position]);
                    //startActivity(intent);

                    // Toast.makeText(getActivity(), "You Clicked at " + web[+position], Toast.LENGTH_SHORT).show();
                }
            });
        }
        //TODO: klic na server za prikaz uporabnikovih ponudb



        private class RESTCallTaskGetUsersOffers extends AsyncTask<String, Void, String[]> { //testni za registracijo
            private  String URLuserOffers = "http://10.0.2.2:8080/allpromos/"; // za seznam


            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(rootView.getContext());
            String sharedPreferencesEmail = sharedPreferences.getString( "mail" , "null");



            @Override
            protected String[] doInBackground(String... params) {

                System.out.println("email iz shared preferences TA MORA DELAT"+sharedPreferencesEmail);

                URLuserOffers+=sharedPreferencesEmail;

                HttpClient hc = new DefaultHttpClient();
                String resultHttpRequest = null;
                String[] resultFinal = null;
                try {

                    //request za seznam uporabnikovih ponudb
                    HttpGet getRequest = new HttpGet(URLuserOffers);
                    HttpResponse response = hc.execute(getRequest);
                    HttpEntity entity = response.getEntity();
                    resultHttpRequest = EntityUtils.toString(entity);
                    //String string = resultHttpRequest;
                    //System.out.println(string);
                    JSONObject jsonObjectResponse = new JSONObject(resultHttpRequest);
                    JSONArray jsonArray = jsonObjectResponse.getJSONArray("data");

                    //request za ponudbe


                    //StringBuffer sb = new StringBuffer();
                    web = new String[jsonArray.length()];
                    imageId = new Integer[jsonArray.length()];


                    //za imena uporabnikovih izzivov
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                        String line = //popravi
                                jsonObject.getString("name")+"\n #"+jsonObject.getString("hashtags");
                        //sb.append(line + "\n");

                        //podatki o ponudbi
                        web[i] = line;

                        //slika podjetja
                        imageId[i] = getContext().
                                getResources().
                                getIdentifier("image" + jsonObject.getString("company_id"), "drawable", getContext().
                                        getPackageName());


                    }

                    resultFinal = web; //sb.toString();
                } catch (Exception e) {
                    Log.i("Prikaz izzivov","Napaka");
                    e.printStackTrace();
                }

                return resultFinal;
            }

            @Override
            protected void onPostExecute(String[] result) {
                if (result != null ){

                    listViewInit(); //Inicializiramo listView
                }

            }
        }





    }


    @Override
    protected void onResume() {
        super.onResume();

    }
}
