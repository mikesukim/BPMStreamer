package edu.uark.msk001.stepmusicstreamer;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;


import java.util.ArrayList;
import java.util.Calendar;
import com.spotify.android.appremote.api.ConnectionParams;
import com.spotify.android.appremote.api.Connector;
import com.spotify.android.appremote.api.SpotifyAppRemote;

import com.spotify.protocol.client.Subscription;
import com.spotify.protocol.types.PlayerState;
import com.spotify.protocol.types.Track;


public class MainActivity extends AppCompatActivity /*implements SensorEventListener*/ {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
    }

}