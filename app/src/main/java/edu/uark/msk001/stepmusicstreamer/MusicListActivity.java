package edu.uark.msk001.stepmusicstreamer;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

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
import java.util.Random;

import ir.androidexception.datatable.DataTable;
import ir.androidexception.datatable.model.DataTableHeader;
import ir.androidexception.datatable.model.DataTableRow;

public class MusicListActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_list);

        int bpm = 40;
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            bpm = extras.getInt("bpm");
            //The key argument here must match that used in the other activity
        }
        requestAPIbyBPM(bpm);
    }

    private void requestAPIbyBPM(int bpm){

        // Request GetSongBpm API
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
                            // Construct rows from JSON Array
                            ArrayList<DataTableRow> rows = new ArrayList<>();
                            JSONArray songs = response.getJSONArray("tempo");
                            for (int i = 0; i < songs.length(); i++) {
                                JSONObject song = songs.getJSONObject(i);
                                DataTableRow row = new DataTableRow.Builder()
                                        .value(song.getString("song_title"))
                                        .value(song.getJSONObject("artist").getString("name"))
                                        .value(song.getString("tempo"))
                                        .value(song.getJSONObject("album").getString("year"))
                                        .build();
                                rows.add(row);
                            }

                            // Configure Table
                            DataTable dataTable = (DataTable) findViewById(R.id.data_table);
                            DataTableHeader header = createTableHeader();
                            dataTable.setHeader(header);
                            dataTable.setRows(rows);
                            dataTable.inflate(getApplicationContext());

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
        });

        // Add the request to the RequestQueue.
        queue.add(jsonRequest);
    }

    private DataTableHeader createTableHeader(){
        DataTableHeader header = new DataTableHeader.Builder()
                .item("Title", 2)
                .item("Artist", 2)
                .item("BPM", 2)
                .item("Year", 2)
                .build();

        return header;
    }



}