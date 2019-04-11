package com.example.anonymous.testload;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;


import com.example.castle_scheduler.castle_scheduler;

import java.util.regex.Pattern;

import static java.lang.Thread.sleep;

public class MainActivity extends AppCompatActivity {
    castle_scheduler cs=new castle_scheduler(this);
    private int decision=0;
    int astar=98,b=140,ddl=8000;//8000*1000
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        final SharedPreferences sharedPreferences=getApplicationContext().getSharedPreferences("SNR",0);
        final SharedPreferences.Editor editor=sharedPreferences.edit();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if ((ActivityCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) &&
                (ActivityCompat.checkSelfPermission(MainActivity.this,
                        Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED)) {
            if (ContextCompat.checkSelfPermission(MainActivity.this,
                    Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {

                if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                        Manifest.permission.ACCESS_FINE_LOCATION)) {

                } else {
                    ActivityCompat.requestPermissions(MainActivity.this,
                            new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 111);
                }

            }

            return;
        }

        if ((ActivityCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.READ_PHONE_STATE)
                != PackageManager.PERMISSION_GRANTED)) {
            if (ContextCompat.checkSelfPermission(MainActivity.this,
                    Manifest.permission.READ_PHONE_STATE)
                    != PackageManager.PERMISSION_GRANTED) {

                if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                        Manifest.permission.READ_PHONE_STATE)) {

                } else {
                    ActivityCompat.requestPermissions(MainActivity.this,
                            new String[]{Manifest.permission.READ_PHONE_STATE}, 112);
                }

            }

            return;
        }
        if ((ActivityCompat.checkSelfPermission(MainActivity.this,Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) &&
                (ActivityCompat.checkSelfPermission(MainActivity.this,Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED)) {
            if (ContextCompat.checkSelfPermission(MainActivity.this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {

                if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

                } else {
                    ActivityCompat.requestPermissions(MainActivity.this,
                            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 777);
                }

            }
        }

        if ((ActivityCompat.checkSelfPermission(MainActivity.this,Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) &&
                (ActivityCompat.checkSelfPermission(MainActivity.this,Manifest.permission.READ_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED)) {
            if (ContextCompat.checkSelfPermission(MainActivity.this,
                    Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {

                if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                        Manifest.permission.READ_EXTERNAL_STORAGE)) {

                } else {
                    ActivityCompat.requestPermissions(MainActivity.this,
                            new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 777);
                }

            }
        }
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        final TelephonyManager mTelephonyManager=(TelephonyManager)this.getSystemService(Context.TELEPHONY_SERVICE);
        final PhoneStateListener mPhoneStateListener=new PhoneStateListener(){
            public void onSignalStrengthsChanged(SignalStrength signalStrength) {
                Log.d("castle_library","signal strength changed");
                super.onSignalStrengthsChanged(signalStrength);
                Pattern SPACE_STR = Pattern.compile(" ");
                String[] splitSignals = SPACE_STR.split(signalStrength.toString());
                if (splitSignals.length < 3) {
                    splitSignals = signalStrength.toString().split("[ .,|:]+");
                }
                String s_rsrp = splitSignals[9];
                String s_rsrq = splitSignals[10];
                String s_snr = splitSignals[11];
                String s_cqi = splitSignals[12];
                int snr=Integer.valueOf(s_snr);
                Log.d("snr sensed1:\t",""+snr);
                editor.putInt("snr_value",snr);
                boolean edit_ok=editor.commit();
                while(!edit_ok){
                    edit_ok=editor.commit();
                }
            }
        };
        Thread sensing= new Thread(new Runnable() {
            @Override
            public void run() {
                while(true) {
                    mTelephonyManager.listen(mPhoneStateListener, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
                    mTelephonyManager.listen(mPhoneStateListener, PhoneStateListener.LISTEN_NONE);
                    try{
                        sleep(1000);
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            }
        });
        sensing.setPriority(10);
        Thread initialise=new Thread(new Runnable() {
            @Override
            public void run() {
                cs.init(sharedPreferences,astar,b,ddl);//8000*1000
            }
        });
        Thread download_decision=new Thread(new Runnable() {
            @Override
            public void run() {
                while(true){
                    int predict_class=cs.castle_predict_class_long();
                    Log.d("application","\nPredicted class\t"+predict_class+"\n"+"Predicted load percentage\t"+
                            cs.castle_compute_load_percentage()+"\n");
                    decision=cs.castle_schedule();
                    if(decision==1){
                        Log.d("application","*****************Start downloading*" +
                                "**************");
                    }
                }
            }
        });
        initialise.setPriority(8);
        download_decision.setPriority(6);
        Thread main_thread=Thread.currentThread();
        main_thread.setPriority(4);
        sensing.start();
        initialise.start();
        download_decision.start();
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

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    @Override
    public void onBackPressed(){
        new AlertDialog.Builder(this).setMessage("Exit or Background")
                .setPositiveButton("Exit", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        moveTaskToBack(true);
                        finish();
                    }
                })
                .setNegativeButton("Background", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        Intent intent = new Intent();
                        intent.setAction(Intent.ACTION_MAIN);
                        intent.addCategory(Intent.CATEGORY_HOME);
                        startActivity(intent);
                    }
                }).show();

        return;
    }
}
