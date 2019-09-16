package com.jvvas.incomesoutcomes;

import android.content.Intent;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import static java.lang.Integer.parseInt;

public class MonthPieActivity extends AppCompatActivity {

    private static String TAG = "Month Pie" ;

    private FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    private DatabaseReference mRootReference = firebaseDatabase.getReference();
    private DatabaseReference transactionObjRef;
    private DatabaseReference mdatabaseRefEsoda;
    private DatabaseReference mdatabaseRefExoda;
    private TextView txtMonth;
    private PieChart pieChart;
    private String monthAndYear = null;

    private int counterForYData = 0 ;
    // yData : #Misthos, #Exo, #Tzogos, #Food, #Dei, #Ote, #Poto
    private int[] yData = {1,1,1,1,1,1,1};
    private String[] xData = {"Μισθος", "Εξ. Δουλεια" , "Τζογος" , "Φαγητα", "ΔΕΗ", "ΟΤΕ", "Ποτα"};
    private ArrayList<Transaction> transEsodaList = new ArrayList<>();
    private ArrayList<Transaction> transExodaList = new ArrayList<>();
    private boolean istheLastEsodo = false ;
    private boolean istheLastExodo = false ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_month_pie);

        Intent incomingDateIntentInfo = getIntent();
        monthAndYear = incomingDateIntentInfo.getStringExtra("monthAndYear");

        init();
        if(isOk())
            addElementsFromDbToArray();

        clickOnButtons();
    }

    private void addElementsFromDbToArray() {
        mdatabaseRefEsoda.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot child: dataSnapshot.getChildren()){
                    String key = child.getKey();
                    transactionObjRef = mdatabaseRefEsoda.child(key) ;
                    addEvLisEsoda(transactionObjRef);
                }
                istheLastEsodo = true;
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {  }
        });

        mdatabaseRefExoda.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot child: dataSnapshot.getChildren()){
                    String key = child.getKey();
                    transactionObjRef = mdatabaseRefExoda.child(key);
                    addEvLisExoda(transactionObjRef);
                }
                istheLastExodo = true;
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {  }
        });
    }

    private void addEvLisExoda(DatabaseReference transactionObjRef) {
        ValueEventListener eventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Transaction transaction = dataSnapshot.getValue(Transaction.class);
                transExodaList.add(transaction);
                addToYData();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
        transactionObjRef.addValueEventListener(eventListener);
    }

    private void addEvLisEsoda(DatabaseReference transactionObjRef) {
        ValueEventListener eventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Transaction transaction = dataSnapshot.getValue(Transaction.class);
                transEsodaList.add(transaction);
                addToYData();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
        transactionObjRef.addValueEventListener(eventListener);
    }

    private void addToYData() {

        Transaction tr ;
        int num = 0 ;

        if(transExodaList.size() > 0)
        {
            tr = transExodaList.get((transExodaList.size()-1));

            if(tr.getType().equals("Food")){
                num = Integer.parseInt(tr.getAmount());
                yData[3] += num ;
            }else if(tr.getType().equals("Dei")){
                num = Integer.parseInt(tr.getAmount());
                yData[4] += num ;
            }else if(tr.getType().equals("Ote")){
                num = Integer.parseInt(tr.getAmount());
                yData[5] += num ;
            }else if(tr.getType().equals("Poto")){
                num = Integer.parseInt(tr.getAmount());
                yData[6] += num ;
            }
        }else
            {
            tr = transEsodaList.get(transEsodaList.size()-1);

            if(tr.getType().equals("Misthos")){
                num = Integer.parseInt(tr.getAmount());
                yData[0] += num ;
            }else if(tr.getType().equals("Exo")){
                num = Integer.parseInt(tr.getAmount());
                yData[1] += num ;
            }else if(tr.getType().equals("Tzogos")){
                num = Integer.parseInt(tr.getAmount());
                yData[2] += num ;
            }
        }

        if(istheLastEsodo && istheLastExodo){
            createThePieChart();
        }
    }

    private void clickOnButtons() {
        pieChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, Highlight h) {
                String[] elements = h.toString().split("[: ,]+");
                int pos = parseInt(elements[2].substring(0,1));
                Log.d(TAG, "POS -------- : " + elements[2]);

                Toast.makeText(MonthPieActivity.this, "Ειδος : " + xData[pos]+
                        "\nΠοσο : " + elements[4] + " €", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNothingSelected() {

            }
        });
    }

    private void createThePieChart() {
        pieChart.setRotationEnabled(true);
        pieChart.setUsePercentValues(true);
        pieChart.setHoleColor(Color.DKGRAY);
        pieChart.setHoleRadius(35f);
        pieChart.setTransparentCircleColor(Color.LTGRAY);

        pieChart.setCenterText("Pie Chart");
        pieChart.setCenterTextColor(Color.WHITE);
        pieChart.setCenterTextSize(14);

        pieChart.setDrawEntryLabels(true);
        pieChart.setEntryLabelTextSize(20);

        addDataSet();
    }

    private void addDataSet() {
        ArrayList<PieEntry> yEntrys = new ArrayList<>();
        ArrayList<String> xEntrys = new ArrayList<>();

        for (int i = 0; i < yData.length; i++) {
            yEntrys.add(new PieEntry(yData[i], i));
        }

        for (int i = 1; i < xData.length; i++) {
            xEntrys.add(xData[i]);
        }

        //create the data set
        PieDataSet pieDataSet = new PieDataSet(yEntrys, "Months");
        pieDataSet.setSliceSpace(2);
        pieDataSet.setValueTextSize(15);
        pieDataSet.setValueTextColor(Color.BLACK);

        //add colors to dataset
        ArrayList<Integer> colors = new ArrayList<>();
        colors.add(Color.GRAY);
        colors.add(Color.BLUE);
        colors.add(Color.RED);
        colors.add(Color.GREEN);
        colors.add(Color.CYAN);
        colors.add(Color.YELLOW);
        colors.add(Color.MAGENTA);

        pieDataSet.setColors(colors);

        //add legend to chart
        Legend legend = pieChart.getLegend();
        legend.setForm(Legend.LegendForm.SQUARE);

        //create pie data object
        PieData pieData = new PieData(pieDataSet);
        pieChart.setData(pieData);
        pieChart.invalidate();
    }

    private boolean isOk() {
        if( !(monthAndYear == null) )
        {
            String[] elements = monthAndYear.split("[, ]");
            String currentYear = elements[3];
            String currentMonth = elements[1] + "ς" ;

            txtMonth.setText( currentMonth + " " + currentYear );

            mdatabaseRefEsoda = mRootReference.child("years").child(currentYear).child(currentMonth).child("esoda");
            mdatabaseRefExoda = mRootReference.child("years").child(currentYear).child(currentMonth).child("exoda");
            return true;
        }
        else
        {
            txtMonth.setText("Μηνας");
            return false;
        }
    }

    private void init() {
        txtMonth = (TextView) findViewById(R.id.textView_MonthOfPie);
        pieChart = (PieChart) findViewById(R.id.pieChartMonth);
    }
}
