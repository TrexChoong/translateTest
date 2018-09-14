package com.example.user.visiontranslate;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.design.widget.TabLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.FileProvider;
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

import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Cache;
import com.android.volley.Network;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.BasicNetwork;
import com.android.volley.toolbox.DiskBasedCache;
import com.android.volley.toolbox.HurlStack;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.api.client.util.Lists;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.vision.v1.AnnotateImageRequest;
import com.google.cloud.vision.v1.AnnotateImageResponse;
import com.google.cloud.vision.v1.BatchAnnotateImagesResponse;
import com.google.cloud.vision.v1.EntityAnnotation;
import com.google.cloud.vision.v1.Feature;
import com.google.cloud.vision.v1.Image;
import com.google.cloud.vision.v1.ImageAnnotatorClient;
import com.google.cloud.vision.v1.ImageSource;
import com.google.protobuf.ByteString;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import io.grpc.Context;

public class MainActivity extends AppCompatActivity {
    public String captureStatus="";
    public String captureText="";
    public RequestQueue mRequestQueue;

    private TextView textStatus;
    private TextView textCaptured;

    public static View captureView;
    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Cache cache = new DiskBasedCache(getCacheDir(), 1024 * 1024); // 1MB cap

        Network network = new BasicNetwork(new HurlStack());
        mRequestQueue =  new RequestQueue(cache, network);
        mRequestQueue.start();
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

        mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(mViewPager));
//
//        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
//            }
//        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(!data.getStringExtra(ViewActivity.TextBlockObject).isEmpty()) {

            if (resultCode == CommonStatusCodes.SUCCESS) {
                if (data != null) {
                    String text = data.getStringExtra(ViewActivity.TextBlockObject);
                    captureStatus = getResources().getString(R.string.ocr_success);
                    captureText = text;
                } else {
                    captureStatus = getResources().getString(R.string.ocr_failure);
                }
            } else {
                captureStatus = String.format(getString(R.string.ocr_error),
                        CommonStatusCodes.getStatusCodeString(resultCode));
            }

        }
        else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        // getMenuInflater().inflate(R.menu.menu_main, menu);
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

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        public PlaceholderFragment() {
        }

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

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = getView();
            switch (getArguments().getInt(ARG_SECTION_NUMBER)) {
                case 1:
                    rootView = inflater.inflate(R.layout.fragment_main, container, false);
                    Button capture = (Button) rootView.findViewById(R.id.btnCapture);
                    capture.setOnClickListener(new View.OnClickListener() {
                        public void onClick(View v) {

                            // launch Ocr capture activity.
                            Intent intent = new Intent(getActivity(), ViewActivity.class);
                            intent.putExtra(ViewActivity.AutoFocus, true);
                            intent.putExtra(ViewActivity.UseFlash, false);

                            startActivityForResult(intent, 9003);
                        }
                    });
                    break;
                case 2:
                    rootView = inflater.inflate(R.layout.fragment_collection, container, false);

                    Button translate = (Button) rootView.findViewById(R.id.btnTranslate);
                    translate.setOnClickListener(new View.OnClickListener() {
                        public void onClick(View v) {

                            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getContext());
                            String holder = preferences.getString("selection","");

                            Toast toast2 = Toast.makeText(getActivity(),"Saved Data: " +  holder ,Toast.LENGTH_SHORT);
                            toast2.show();
                            String url = "https://translation.googleapis.com/language/translate/v2?target=es&key=AIzaSyBCQdqCtwkabyNwvkaSlb4D4ySYjFh-JA8&q=My%20name%20is%20Steve";

                            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                                    (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {

                                        @Override
                                        public void onResponse(JSONObject response) {
                                            String transalatedText = "";
                                            try {
                                                JSONObject data = response.getJSONObject("data");
                                                JSONArray translations = data.getJSONArray("translations");
                                                transalatedText = translations.getJSONObject(0).getString("translatedText");
                                            }catch (JSONException e) {
                                                Log.e("Error", e.getMessage());
                                                e.printStackTrace();
                                            }
                                            Toast toast = Toast.makeText(getActivity(),"Translated Data: " +  transalatedText ,Toast.LENGTH_SHORT);
                                            toast.show();
                                        }
                                    }, new Response.ErrorListener() {

                                        @Override
                                        public void onErrorResponse(VolleyError error) {
                                            Toast toast = Toast.makeText(getActivity(),"Translation Failed: " +  error.toString() ,Toast.LENGTH_SHORT);
                                            toast.show();
                                        }
                                    });

                            ((MainActivity)getActivity()).getmRequestQueue().add(jsonObjectRequest);
                        }
                    });


                    break;
            }
            return rootView;
        }
    }

    public RequestQueue getmRequestQueue(){
        return  mRequestQueue;
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d("resumed", captureStatus + captureText);

        if(!captureText.isEmpty()){
            Toast toast = Toast.makeText(this,"Saved : " + captureText,Toast.LENGTH_SHORT);
            toast.show();
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            String holder = preferences.getString("selection","");
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString("selection", holder + captureText + "@#@");
            editor.commit();

            Toast toast2 = Toast.makeText(this,"Saved Data: " +  holder + captureText + "@#@",Toast.LENGTH_SHORT);
            toast2.show();
        }
//        if(getApplicationContext()!= null){
//            Activity rootView = this.getView();
//            TextView textStatus = (TextView)rootView.findViewById(R.id.status);
//            textStatus.setText(captureStatus);
//
//            TextView textCaptured = (TextView)rootView.findViewById(R.id.result);
//            textCaptured.setText(captureText);
//            Log.d("resumed2", captureStatus + captureText);
//        }
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
            return PlaceholderFragment.newInstance(position + 1);
        }

        @Override
        public int getCount() {
            // Show 2 total pages.
            return 2;
        }
    }
}
