package com.example.user.visiontranslate;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
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

import com.google.api.client.util.Lists;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.vision.v1.AnnotateImageRequest;
import com.google.cloud.vision.v1.AnnotateImageResponse;
import com.google.cloud.vision.v1.BatchAnnotateImagesResponse;
import com.google.cloud.vision.v1.EntityAnnotation;
import com.google.cloud.vision.v1.Feature;
import com.google.cloud.vision.v1.Image;
import com.google.cloud.vision.v1.ImageAnnotatorClient;
import com.google.protobuf.ByteString;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import io.grpc.Context;

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

        mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(mViewPager));

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

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
        int TAKE_PHOTO_CODE = 0;
        String photoURI = "";
        public static int count = 0;

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = getView();
            switch (getArguments().getInt(ARG_SECTION_NUMBER)) {
                case 1:
                    rootView = inflater.inflate(R.layout.fragment_main, container, false);
//                    TextView textView = (TextView) rootView.findViewById(R.id.section_label);
//                    textView.setText(getString(R.string.section_format, getArguments().getInt(ARG_SECTION_NUMBER)));

                    // Here, we are making a folder named picFolder to store

                    // pics taken by the camera using this application.
                    final String dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + "/picFolder/";
                    File newdir = new File(dir);
                    newdir.mkdirs();

                    Button capture = (Button) rootView.findViewById(R.id.btnCapture);
                    capture.setOnClickListener(new View.OnClickListener() {
                        public void onClick(View v) {
                            photoURI = Environment.getExternalStoragePublicDirectory(
                                    Environment.DIRECTORY_DCIM) + "/Camera/IMG_20180908_161408.jpg";
                            Log.d("result urlPath",photoURI);
                            try{
                                GoogleCredentials credentials = GoogleCredentials.getApplicationDefault();
                                Log.d("auth", credentials.toString());
                            } catch  (Exception e){
                                Log.e("fail",e.getMessage());
                            }
                            try {
                                detectText(photoURI,getActivity());
                            } catch (Exception e){
                                Log.e("error", e.getMessage());
                            }
                            // Here, the counter will be incremented each time, and the
                            // picture taken by camera will be stored as 1.jpg,2.jpg
                            // and likewise.
//                            count++;F
//                            String file = dir+count+".jpg";
////                            File newfile = new File(file);
////                            try {
////                                newfile.createNewFile();
////                            }
////                            catch (IOException e)
////                            {
////                            }
//
////                            Uri outputFileUri = Uri.fromFile(newfile);
//
////                            Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
////                            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);
////
////                            startActivityForResult(cameraIntent, TAKE_PHOTO_CODE);
//                            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//                            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
//                            Uri imageUri = FileProvider.getUriForFile(getContext(), getContext().getApplicationContext().getPackageName() + ".my.package.name.provider", new File(Environment.getExternalStoragePublicDirectory(
//                                    Environment.DIRECTORY_DCIM), "fname_" +
//                                    count + ".jpg"));
//                            intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
//                            photoURI = imageUri.toString();
//                            Log.d("urlPath",imageUri.toString());
//                            startActivityForResult(intent, TAKE_PHOTO_CODE);
                        }
                    });
                    break;
                case 2:
                    rootView = inflater.inflate(R.layout.fragment_collection, container, false);
                    break;
            }
//            photoURI = Environment.getExternalStoragePublicDirectory(
//                    Environment.DIRECTORY_DCIM) + "/Camera/IMG_20180908_161408.jpg";
//            Log.d("result urlPath",photoURI);
//            try {
//                detectText(photoURI);
//            } catch (Exception e){
//                Log.e("error", e.getMessage());
//            }
            return rootView;
        }
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

    public static void detectText(String filePath, Activity activity) throws Exception {
//                .createScoped(Lists.newArrayList("https://www.googleapis.com/auth/cloud-platform"));
//        Context.Storage storage = StorageOptions.newBuilder().setCredentials(credentials).build().getService();

//        Storage storage = StorageOptions.getDefaultInstance().getService();
        List<AnnotateImageRequest> requests = new ArrayList<>();

        String[] permissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};
        ActivityCompat.requestPermissions(activity,permissions,120);
        ByteString imgBytes = ByteString.readFrom(new FileInputStream(filePath));

        Image img = Image.newBuilder().setContent(imgBytes).build();
        Feature feat = Feature.newBuilder().setType(Feature.Type.TEXT_DETECTION).build();
        AnnotateImageRequest request =
                AnnotateImageRequest.newBuilder().addFeatures(feat).setImage(img).build();
        requests.add(request);

        try (ImageAnnotatorClient client = ImageAnnotatorClient.create()) {
            BatchAnnotateImagesResponse response = client.batchAnnotateImages(requests);
            List<AnnotateImageResponse> responses = response.getResponsesList();

            for (AnnotateImageResponse res : responses) {
                if (res.hasError()) {
//                    out.printf("Error: %s\n", res.getError().getMessage());
                    return;
                }

                // For full list of available annotations, see http://g.co/cloud/vision/docs
                for (EntityAnnotation annotation : res.getTextAnnotationsList()) {
                        Log.d("result",annotation.getDescription());
//                    out.printf("Text: %s\n", annotation.getDescription());
//                    out.printf("Position : %s\n", annotation.getBoundingPoly());
                }
            }
        }
    }
}
