package edu.uark.msk001.stepmusicstreamer;


import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;


import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.JsonObject;
import com.spotify.android.appremote.api.ConnectionParams;
import com.spotify.android.appremote.api.Connector;
import com.spotify.android.appremote.api.SpotifyAppRemote;

import com.spotify.protocol.client.Subscription;
import com.spotify.protocol.types.PlayerState;
import com.spotify.protocol.types.Track;
import com.spotify.sdk.android.auth.AuthorizationClient;
import com.spotify.sdk.android.auth.AuthorizationRequest;
import com.spotify.sdk.android.auth.AuthorizationResponse;

import edu.uark.msk001.stepmusicstreamer.R;
import ir.androidexception.datatable.DataTable;
import ir.androidexception.datatable.model.DataTableHeader;
import ir.androidexception.datatable.model.DataTableRow;


public class StepCounterActivity extends AppCompatActivity implements SensorEventListener {
    private TextView tvStepCounter, tvOther;
    private SensorManager sensorManager;
    private Sensor mStepCounter;
    private Boolean isCounterSensorPresent;
    private Button spotifyBtn;
    int stepCount;
    double stepPerMin;
    long prevStepTimeInMillis, currTimeStepInMillis;
    ArrayList<Long> stepTimeArray;
    private Activity mActivity;
    String songName;
    StepCountPresenter mStepCounterPresenter;


    private static final int REQUEST_CODE = 1337;
    private static final String CLIENT_ID = "ccb0e0426c594fc7a02bfb3f0ed0f651";
    private static final String REDIRECT_URI = "http://edu.uark.msk001.stepmusicstreamer";
    private SpotifyAppRemote mSpotifyAppRemote;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mActivity = this;
        setContentView(R.layout.step_counter);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        tvStepCounter = (TextView)findViewById(R.id.tvNumSteps);
        tvOther = (TextView)findViewById(R.id.tvOtherSteps);
        spotifyBtn = findViewById(R.id.btnSpotifyPlay);
        mStepCounterPresenter = new StepCountPresenter();


        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        if(sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR) != null)
        {
            mStepCounter = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);
            isCounterSensorPresent = true;
        }
        else{
            tvStepCounter.setText("StepDetector Sensor is not present");
            isCounterSensorPresent = false;
        }
        
        stepCount = 0;
        stepPerMin = 0;

        tvStepCounter.setText(String.valueOf(stepCount));
        tvOther.setText(String.valueOf(stepPerMin));
        stepTimeArray = new ArrayList<Long>();

    }


    @Override
    public void onSensorChanged(SensorEvent event) {
        if(event.sensor == mStepCounter)
        {
            if(stepCount == 0) {
                stepCount += 1;//(int) event.values[0];
                prevStepTimeInMillis = System.currentTimeMillis();
                tvStepCounter.setText(String.valueOf(stepCount));
            }
            else
            {
                currTimeStepInMillis = System.currentTimeMillis();
                long timeBetweenSteps = currTimeStepInMillis - prevStepTimeInMillis;
                stepTimeArray.add(timeBetweenSteps);
                prevStepTimeInMillis = System.currentTimeMillis();
                stepCount +=1;// (int) event.values[0];
                tvStepCounter.setText(String.valueOf(stepCount));
            }
        }
        if(stepTimeArray.size() > 0) {
            for (int i = 0; i < stepTimeArray.size(); i++) {
                stepPerMin += stepTimeArray.get(i);
            }
            stepPerMin = 60000/(stepPerMin / stepTimeArray.size());
            //casting to an int before setting the text.
            tvOther.setText(String.valueOf((int)stepPerMin));
        }
    }

    //Won't happen
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d("onActivityResult", "Line 169");
        if(requestCode == REQUEST_CODE) {
            AuthorizationResponse response = AuthorizationClient.getResponse(resultCode, data);

            switch (response.getType()) {
                case TOKEN:
                    //handle successful response
                    Log.d("onActicityResult", "case: TOKEN");
                    mStepCounterPresenter.searchAndGetUri(response, this, mSpotifyAppRemote);
                    break;
                case ERROR:
                    break;
            }
        }
    }



    @Override
    protected void onResume() {
        super.onResume();
        if(sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR) != null)
        {
            sensorManager.registerListener(this, mStepCounter, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR) != null)
        {
            sensorManager.unregisterListener(this, mStepCounter);
            //stepCount = 0;
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        stepCount = 0;
         SpotifyAppRemote.disconnect(mSpotifyAppRemote);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d("OnStart", "ConnectionParams");
        ConnectionParams connectionParams =
                new ConnectionParams.Builder(CLIENT_ID)
                        .setRedirectUri(REDIRECT_URI)
                        .showAuthView(true)
                        .build();

        SpotifyAppRemote.connect(this, connectionParams,
                new Connector.ConnectionListener() {

                    public void onConnected(SpotifyAppRemote spotifyAppRemote) {
                        mSpotifyAppRemote = spotifyAppRemote;
                        Log.d("MainActivity", "Connected! Yay!");

                        // Now you can start interacting with App Remote
                        connected();

                    }

                    public void onFailure(Throwable throwable) {
                        Log.e("MyActivity", throwable.getMessage(), throwable);

                        // Something went wrong when attempting to connect! Handle errors here
                    }
                });
        Log.d("OnStart", "Connected");
    }

    private void connected()
    {
        spotifyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mStepCounterPresenter.play(stepPerMin, getApplicationContext(), mActivity);
            }
        });
    }

}