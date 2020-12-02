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
        /*spotifyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                play();
            }
        });*/
        stepCount = 0;
        stepPerMin = 0;

        tvStepCounter.setText(String.valueOf(stepCount));
        tvOther.setText(String.valueOf(stepPerMin));
        stepTimeArray = new ArrayList<Long>();

    }


    private void play()
    {
        int bpm = (int) stepPerMin;
        RequestQueue queue = Volley.newRequestQueue(this);
        String url = "https://api.getsongbpm.com/tempo/?api_key=607d39180d5c90afc1efcd4fc2fc512b&bpm=" + String.valueOf(bpm);
        Log.d("BPM", url);
        // Request a string response from the provided URL.
        JsonObjectRequest  jsonRequest = new JsonObjectRequest(Request.Method.GET, url,null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Toast.makeText(getApplicationContext(),"Succeed!",Toast.LENGTH_SHORT).show();

                        try {
                            JSONArray songs = response.getJSONArray("tempo");
                            JSONObject song = songs.getJSONObject(0);
                            songName = song.getString("song_title");
                            songName = songName.replaceAll(" ", "%20");
                            Log.d("Replace spaces", songName);
                            songName = songName.replaceAll("&", "%26");
                            Log.d("Replace Special Chars", songName);


                            Log.d("SongName", songName);


                            AuthorizationRequest.Builder builder =
                                    new AuthorizationRequest.Builder(CLIENT_ID, AuthorizationResponse.Type.TOKEN, REDIRECT_URI);
                            builder.setScopes(new String[]{"streaming"});
                            AuthorizationRequest request = builder.build();

                            AuthorizationClient.openLoginActivity(mActivity, REQUEST_CODE, request);

                        } /*catch (JSONException e) {
                            e.printStackTrace();
                        }*/ catch (Exception e) {
                            e.printStackTrace();
                        }


                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("BPM", error.toString());
                Toast.makeText(getApplicationContext(),"Failed to get data",Toast.LENGTH_SHORT).show();
            }
        });
        queue.add(jsonRequest);

        /*AuthorizationRequest.Builder builder =
                new AuthorizationRequest.Builder(CLIENT_ID, AuthorizationResponse.Type.TOKEN, REDIRECT_URI);
        builder.setScopes(new String[]{"streaming"});
        AuthorizationRequest request = builder.build();

        AuthorizationClient.openLoginActivity(this, REQUEST_CODE, request);*/



       /* mSpotifyAppRemote.getPlayerApi().play("spotify:playlist:37i9dQZF1DX2sUQwD7tbmL");

        // Subscribe to PlayerState
        mSpotifyAppRemote.getPlayerApi()
                .subscribeToPlayerState()
                .setEventCallback(playerState -> {
                    final Track track = playerState.track;
                    if (track != null) {
                        Log.d("MainActivity", track.name + " by " + track.artist.name);
                    }
                });*/
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
                    searchAndGetUri(response);
                    break;
                case ERROR:
                    break;
            }
        }
    }

    private void searchAndGetUri(AuthorizationResponse response)
    {
        RequestQueue queue = Volley.newRequestQueue(this);
        Log.d("searchAndGetUri", "accessed the method");
        String token = response.getAccessToken();
        String searchName = songName;
        String url = "https://api.spotify.com/v1/search?q="+searchName + "&type=track"; /*-H \"Authorization: Bearer " + token + "\"";*/
        Log.d("searchAndGetUri", url);
        Log.d("token", token);
        JsonObjectRequest jsonRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.d("StepCounterActivity", "Search success");
                try {
                    Log.d("onResponse", "Try");
                    Log.d("Response", response.toString());
                    JSONObject tracks = response.getJSONObject("tracks");
                    Log.d("onResponse", "Got tracks");
                    JSONArray items = tracks.getJSONArray("items");
                    JSONObject song = items.getJSONObject(0);
                    String uri = song.getString("uri");
                    Log.d("URI for song is", uri);
                    mSpotifyAppRemote.getPlayerApi().play(uri);

                    // Subscribe to PlayerState
                    mSpotifyAppRemote.getPlayerApi()
                            .subscribeToPlayerState()
                            .setEventCallback(playerState -> {
                                final Track track = playerState.track;
                                if (track != null) {
                                    Log.d("SearchAndGetUri", track.name + " by " + track.artist.name);
                                }
                            });
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("BPM", error.toString());
                Toast.makeText(getApplicationContext(),"Failed to get data",Toast.LENGTH_SHORT).show();
            }
        }) {

        public Map<String, String> getHeaders() throws AuthFailureError {
            Map<String, String> headers = new HashMap<>();
            // Basic Authentication
            //String auth = "Basic " + Base64.encodeToString(CONSUMER_KEY_AND_SECRET.getBytes(), Base64.NO_WRAP);

            headers.put("Authorization", "Bearer " + token);
            return headers;
        }
    };
        queue.add(jsonRequest);
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
                play();
            }
        });
    }

}