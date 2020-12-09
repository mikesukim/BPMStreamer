package edu.uark.msk001.stepmusicstreamer;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Context;
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


import com.spotify.android.appremote.api.SpotifyAppRemote;
import com.spotify.protocol.client.Subscription;
import com.spotify.protocol.types.PlayerState;
import com.spotify.protocol.types.Track;
import com.spotify.sdk.android.auth.AuthorizationClient;
import com.spotify.sdk.android.auth.AuthorizationRequest;
import com.spotify.sdk.android.auth.AuthorizationResponse;


public class StepCountPresenter {

    private String songName;

    private static final int REQUEST_CODE = 1337;
    private static final String CLIENT_ID = "ccb0e0426c594fc7a02bfb3f0ed0f651";
    private static final String REDIRECT_URI = "http://edu.uark.msk001.stepmusicstreamer";

    //constructor
    public StepCountPresenter()
    {

    }

    public void play(double stepPerMin, Context context, Activity mActivity)
    {
        int bpm = (int) stepPerMin;
        RequestQueue queue = Volley.newRequestQueue(context);
        String url = "https://api.getsongbpm.com/tempo/?api_key=607d39180d5c90afc1efcd4fc2fc512b&bpm=" + String.valueOf(bpm);
        Log.d("BPM", url);
        // Request a string response from the provided URL.
        JsonObjectRequest  jsonRequest = new JsonObjectRequest(Request.Method.GET, url,null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                       // Toast.makeText(getApplicationContext(),"Succeed!",Toast.LENGTH_SHORT).show();

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
                //Toast.makeText(getApplicationContext(),"Failed to get data",Toast.LENGTH_SHORT).show();
            }
        });
        queue.add(jsonRequest);
    }


    public void searchAndGetUri(AuthorizationResponse response, Context context, SpotifyAppRemote mSpotifyAppRemote)
    {
        RequestQueue queue = Volley.newRequestQueue(context);
        Log.d("searchAndGetUri", "accessed the method");
        String token = response.getAccessToken();
        String searchName = songName;
        String url = "https://api.spotify.com/v1/search?q=" + searchName + "&type=track"; /*-H \"Authorization: Bearer " + token + "\"";*/
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
                //Toast.makeText(getApplicationContext(),"Failed to get data",Toast.LENGTH_SHORT).show();
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


}
