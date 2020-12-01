package edu.uark.msk001.stepmusicstreamer;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;


import mehdi.sakout.fancybuttons.FancyButton;
import pl.pawelkleczkowski.customgauge.CustomGauge;


public class MainActivity extends AppCompatActivity /*implements SensorEventListener*/ {

    FancyButton customBtn;
    FancyButton measureBtn;
    CustomGauge gaugeView;
    TextView textView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // set button onclick
        setButtonOnClink();
        // set gaugeView
        setGuageView();
        // set textView
        setTextView();



    }

    private void setButtonOnClink(){
        customBtn = (FancyButton)findViewById(R.id.autoBpmBtn);
        measureBtn = (FancyButton)findViewById(R.id.autoBpmBtn2);

        customBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String value= textView.getText().toString();
                int bpm = Integer.parseInt(value);

                if (bpm <= 40){bpm = 40; textView.setText("40");}
                else if(bpm >= 220){bpm = 220; textView.setText("220");}

                Toast.makeText(getApplicationContext(),"Search song with bpm " + Integer.toString(bpm) + " !",Toast.LENGTH_SHORT).show();


                Intent musicListActivity = new Intent(getApplicationContext(),MusicListActivity.class);
                musicListActivity.putExtra("bpm" , bpm);
                startActivity(musicListActivity);

            }
        });
        measureBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getApplicationContext(),"Measure your steps!",Toast.LENGTH_SHORT).show();

                Intent stepCounterActivity = new Intent(getApplicationContext(),StepCounterActivity.class);
                startActivity(stepCounterActivity);
            }
        });

    }

    private void setGuageView() {
        gaugeView = (CustomGauge)findViewById(R.id.ArcGauge);
        gaugeView.setValue(40);

    }

    private void setTextView() {
        textView = (TextView)findViewById(R.id.textView);
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view){
                Toast.makeText(getApplicationContext(),"Edit your bpm for custom search!",Toast.LENGTH_SHORT).show();
                textView.setCursorVisible(true);
                textView.setFocusableInTouchMode(true);
                textView.setInputType(InputType.TYPE_CLASS_TEXT);
                textView.requestFocus(); //to trigger the soft input
            }

        });

    }

}