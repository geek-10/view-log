package com.example.temperatureloggerproject;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


import org.w3c.dom.Text;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    TextView showTemperature;
    TextView showTime;
    EditText inputMinute;
    Button signalButton;
    Boolean buttonState = true;
    Boolean once = true;
    Boolean inputError = false;
    String myData;
    String previousDate = "0";
    String currentDate;
    String previousValue;
    String myEntriesData;
    Boolean resultEntries = false;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        showTemperature = (TextView) findViewById(R.id.outputTemperature);
        showTime = (TextView) findViewById(R.id.lastUpdateTime);
        inputMinute = (EditText) findViewById(R.id.inputMinute);
        signalButton = (Button) findViewById(R.id.signalButton);


        signalButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (buttonState == true) {
                    String timeForDelay = inputMinute.getText().toString();
                    if (timeForDelay.isEmpty()) {
                        Toast.makeText(MainActivity.this,"Please input number of minutes",Toast.LENGTH_SHORT).show();
                        inputMinute.requestFocus();
                        inputError = true;
                    }

                    else if(inputError == false){
                        HashMap<String, Object> signal = new HashMap<>();
                        signal.put("signal", true);
                        signal.put("timeForDelay", Integer.parseInt(timeForDelay));

                        FirebaseDatabase.getInstance().getReference().child("signal").updateChildren(signal);

                        buttonState = false;
                        signalButton.setText("Stop");
                        signalButton.setTextColor(Color.parseColor("#A21B1B"));
                        inputMinute.clearFocus();

                        displayValue();
                    }

                }
                else if(buttonState == false) {
                    HashMap<String, Object> signal = new HashMap<>();
                    signal.put("signal", false);
                    signal.put("timeForDelay", Integer.parseInt("1"));

                    FirebaseDatabase.getInstance().getReference().child("signal").updateChildren(signal);
                    buttonState = true;
                    signalButton.setText("Start");
                    signalButton.setTextColor(Color.parseColor("#0D8A12"));
                    inputMinute.setText("");
                    inputMinute.clearFocus();
                    notifyUser();

                    showTemperature.setText("--");
                    showTime.setText("--:--");
                }

                inputError = false;
            }
        });

    }

    private void notifyUser() {
        DatabaseReference previousEntries = FirebaseDatabase.getInstance().getReference().child("Entries");
        previousEntries.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot listOfElements: snapshot.getChildren()) {
                    myEntriesData = snapshot.getValue().toString();
                    Integer index = myEntriesData.indexOf("=");
                    previousValue = myEntriesData.substring(index+1,myEntriesData.length());
                    previousValue = previousValue.replace("}","");
                    previousValue.trim();

                    Integer countPrevious = Integer.parseInt(previousValue);

                    if (countPrevious == 9999) {
                        Toast.makeText(MainActivity.this,"History will be cleared from the cloud server",Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    private void displayValue() {
        currentDate = dateFormatter().toString();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child(currentDate);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot listOfElements: snapshot.getChildren()) {
                    myData = snapshot.getValue().toString();
                }

                Integer index = myData.indexOf("=");
                String timeOfTemp = myData.substring(0,index);
                String Temperature = myData.substring(index+1,myData.length());

                timeOfTemp = timeOfTemp.replace("{", " ");
                timeOfTemp = timeOfTemp.replace("-", ":");
                timeOfTemp = timeOfTemp.trim();

                Temperature = Temperature.replace("}", " ");
                Temperature = Temperature.trim();

                showTemperature.setText(Temperature);
                showTime.setText(timeOfTemp);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    private String dateFormatter() {
        SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");
        Date date = new Date();
        return formatter.format(date);
    }


}