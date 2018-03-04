package com.exceptions.trabahunt;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TimePicker;
import android.text.format.DateFormat;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceDetectionClient;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Calendar;
import java.util.HashMap;


public class JobPostingActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener{
    protected GeoDataClient mGeoDataClient;
    protected PlaceDetectionClient mPlaceDetectionClient;
    private DatabaseReference mRootRef;
    private String uid;
    private FirebaseAuth mAuth;

    int REQUEST_PLACE_PICKER = 1;
    EditText jobTitle, jobDescription, jobLoc, jobSched, jobPay;
    Button location, schedule, postJob;

    int day, month, year, hour, minutes;
    int dayFinal, monthFinal, yearFinal, hourFinal, minutesFinal;

    String jobid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_job_posting);
        mAuth = FirebaseAuth.getInstance();
        mRootRef = FirebaseDatabase.getInstance().getReference();
        // Construct a GeoDataClient.
        mGeoDataClient = Places.getGeoDataClient(this, null);

        // Construct a PlaceDetectionClient.
        mPlaceDetectionClient = Places.getPlaceDetectionClient(this, null);
        uid = mAuth.getCurrentUser().getUid();
        System.out.println("--------------------------------------" + uid);


        location = (Button) findViewById(R.id.getLocation);
        schedule = (Button) findViewById(R.id.getSchedule);
        jobTitle = (EditText) findViewById(R.id.jobTitle);
        jobDescription = (EditText) findViewById(R.id.jobDescription);
        jobSched = (EditText) findViewById(R.id.jobSchedule);
        jobLoc = (EditText) findViewById(R.id.jobLocation);
        jobPay = (EditText) findViewById(R.id.jobPay);
        postJob = findViewById(R.id.postJob);

        location.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    PlacePicker.IntentBuilder intentBuilder =
                            new PlacePicker.IntentBuilder();
                    Intent intent = intentBuilder.build(JobPostingActivity.this);
                    startActivityForResult(intent, REQUEST_PLACE_PICKER);

                } catch (GooglePlayServicesRepairableException e) {
                    e.printStackTrace();
                } catch (GooglePlayServicesNotAvailableException e) {
                    e.printStackTrace();
                }

            }
        });

        schedule.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Calendar c = Calendar.getInstance();
                year = c.get(Calendar.YEAR);
                month = c.get(Calendar.MONTH);
                day = c.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog datePickerDialog = new DatePickerDialog(JobPostingActivity.this, JobPostingActivity.this,
                        year, month, day);
                datePickerDialog.show();
            }
        });

        postJob.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                jobid = mRootRef.child("Jobs").push().getKey();
                HashMap<String, String> jobMap = new HashMap<>();
                jobMap.put("Giver", uid);
                jobMap.put("Title", String.valueOf(jobTitle.getText()));
                jobMap.put("Description", String.valueOf(jobDescription.getText()));
                jobMap.put("Location", String.valueOf(jobLoc.getText()));
                jobMap.put("Schedule", String.valueOf(jobSched.getText()));
                jobMap.put("Pay", String.valueOf(jobPay.getText()));

                mRootRef.child("Jobs").child(jobid).setValue(jobMap);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode,
                                    int resultCode, Intent data) {

        if (requestCode == REQUEST_PLACE_PICKER
                && resultCode == Activity.RESULT_OK) {

            // The user has selected a place. Extract the name and address.
            Place place = PlacePicker.getPlace(data, this);

            final CharSequence address = place.getAddress();
            jobLoc.setText(address);

        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onDateSet(DatePicker datePicker, int i, int i1, int i2){
        yearFinal = i;
        monthFinal = i1 + 1;
        dayFinal = i2;

        Calendar c = Calendar.getInstance();
        hour = c.get(Calendar.HOUR_OF_DAY);
        minutes = c.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(JobPostingActivity.this, JobPostingActivity.this,
                hour, minutes, DateFormat.is24HourFormat(this));

        timePickerDialog.show();
    }

    @Override
    public void onTimeSet(TimePicker timePicker, int i, int i1){
        hourFinal = i;
        minutesFinal = i1;

        jobSched.setText(" " +monthFinal + " " + dayFinal + ", " + yearFinal + " " + hourFinal + ":" + minutesFinal);
    }
}