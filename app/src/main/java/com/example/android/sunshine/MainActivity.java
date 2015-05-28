package com.example.android.sunshine;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;


public class MainActivity extends ActionBarActivity {
    private  final String LOG_TAG = MainActivity.class.getSimpleName();
    private final String FORECASTFRAGMENT_TAG = "FFTAG";
    private String mLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        //add below two lines for parse useage

//        ParseObject testCase = new ParseObject("SunshineTestCase");
//        testCase.put("test1","SunshineApp1");
//        testCase.put("test2","SunshineApp2");
//        testCase.put("test3","SunshineApp3");
//        testCase.put("test4","SunshineApp4");
//        testCase.put("test5","SunshineApp5");

//        testCase.saveInBackground();
//        ParseAnalytics.trackAppOpenedInBackground(getIntent());

        setContentView(R.layout.activity_main);
        if(savedInstanceState == null){
            getSupportFragmentManager()
                    .beginTransaction() //add tag here to check if fragment changed or ot
                    .add(R.id.container, new ForecastFragment(), FORECASTFRAGMENT_TAG)
                    .commit();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
//        getMenuInflater().inflate(R.menu.forecastfragment,menu);
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
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }
        if(id == R.id.action_map){
            openPreferredLocationInMap();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    private void openPreferredLocationInMap(){
//        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
//        String location = preferences.getString(getString(R.string.pref_location_key), getString(R.string.pref_location_default));
        String location = Utility.getPreferredLocation(this);
        Uri mapLocation = Uri.parse("geo:0.0?").buildUpon().appendQueryParameter("q",location).build();
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(mapLocation);
        if(intent.resolveActivity(getPackageManager())!=null){
            startActivity(intent);
        }else{
            Log.d(LOG_TAG, "Couldn't call " + location + ", no receiving apps installed!");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        String location = Utility.getPreferredLocation(this);
        if(location!=null && !location.equals(mLocation)){
            ForecastFragment forecastFragment = (ForecastFragment)getSupportFragmentManager().findFragmentByTag(FORECASTFRAGMENT_TAG);
            if(forecastFragment!=null)
                forecastFragment.onLocationChanged();
            mLocation = location;
        }
    }
}
