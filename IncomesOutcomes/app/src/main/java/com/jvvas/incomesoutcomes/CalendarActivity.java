package com.jvvas.incomesoutcomes;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.CalendarView;

import java.text.DateFormat;
import java.util.Calendar;

public class CalendarActivity extends AppCompatActivity {

    private CalendarView calendar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);

        calendar = (CalendarView) findViewById(R.id.calendarView);

        calendar.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(@NonNull CalendarView view, int year, int month, int dayOfMonth) {
                String date = dayOfMonth + "-" + (month+1) + "-" + year;

                Intent intent = new Intent(CalendarActivity.this, MainActivity.class);
                intent.putExtra("date",date);
                startActivity(intent);
            }
        });
    }
}
