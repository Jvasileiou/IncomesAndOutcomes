package com.jvvas.incomesoutcomes;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity{


    private FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    private DatabaseReference mRootReference = firebaseDatabase.getReference();
    private DatabaseReference transactionObjRef;

    private TextView textViewNeaSunallagi,textViewCalendar, textViewMonth,textViewYear,textViewIncomes,textViewOutcomes, textViewMsg ;
    private ImageView addButton,ee ;
    private ListView listViewEsoda, listViewExoda;
    private ArrayList<String> arrListEsoda, arrListExoda;

    public ArrayAdapter<String> arrayAdapterEsoda, arrayAdapterExoda ;
    private DatabaseReference mdatabaseRefEsoda;
    private DatabaseReference mdatabaseRefExoda;

    private ArrayList<Transaction> transEsodaList;
    private ArrayList<Transaction> transExodaList;
    private String currentYear = null ;
    private String currentMonth = null ;


    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializeButtons();
        changeTextOfDate();
        arrListEsoda = new ArrayList<>();
        arrListExoda = new ArrayList<>();
        transEsodaList = new ArrayList<>();
        transExodaList = new ArrayList<>();

        clickOnButtons();

        if( !(currentYear==null) && !(currentMonth==null) ){
            mdatabaseRefEsoda = mRootReference.child("years").child(currentYear).child(currentMonth).child("esoda") ;
            mdatabaseRefExoda = mRootReference.child("years").child(currentYear).child(currentMonth).child("exoda") ;
            addElementsFromDbToArrList();
        }
    }

    private void setClickOnListsEsoda(String temp) {
        arrListEsoda.add(temp);

        arrayAdapterEsoda = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1 , arrListEsoda );
        listViewEsoda.setAdapter(arrayAdapterEsoda);

        listViewEsoda.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Transaction tr1;
                tr1 = transEsodaList.get(position);
                if(tr1.getPhoto()==1)
                    openImageActivity(tr1);
            }
        });
    }

    private void setClickOnListsExoda(String temp) {
        arrListExoda.add(temp);

        arrayAdapterExoda = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1 , arrListExoda);
        listViewExoda.setAdapter(arrayAdapterExoda);

        listViewExoda.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Transaction tr2;
                tr2 = transExodaList.get(position);
                if(tr2.getPhoto()==1)
                    openImageActivity(tr2);
            }
        });

    }

    private void openImageActivity(Transaction tr) {
        Intent intent = new Intent(this, ImageActivity.class);
        intent.putExtra("url",tr.getImgUrl());
        startActivity(intent);
    }

    private void changeTextOfDate() {
        Intent incomingDateIntentInfo = getIntent();
        String currentDate = incomingDateIntentInfo.getStringExtra("date");

        if(currentDate == null){
            listViewEsoda.setVisibility(View.INVISIBLE);
            listViewExoda.setVisibility(View.INVISIBLE);
            textViewMonth.setText("Μηνας");
            textViewYear.setText("Ετος");
        }else{
            listViewEsoda.setVisibility(View.VISIBLE);
            listViewExoda.setVisibility(View.VISIBLE);
            textViewMsg.setVisibility(View.GONE);
            currentMonth = returnTheNameOfMonth(currentDate);
            currentYear = returnTheNameOfYear(currentDate);
            textViewYear.setText("Δες " + currentYear);
            textViewMonth.setText("Δες " + currentMonth.substring(0, currentMonth.length() - 1));
        }
    }

    private String returnTheNameOfYear(String date) {
        String[] elements = date.split("-");
        return elements[2] ;
    }

    private String returnTheNameOfMonth(String date) {
        String[] elements = date.split("-");
        int numOfMonths = Integer.parseInt(elements[1]);

        switch (numOfMonths) {
            case 1:
                return "Ιανουαριος" ;
            case 2:
                return "Φεβρουαριος" ;
            case 3:
                return "Μαρτιος" ;
            case 4:
                return "Απριλιος";
            case 5:
                return "Μαιος";
            case 6:
                return "Ιουνιος";
            case 7:
                return "Ιουλιος";
            case 8:
                return "Αυγουστος";
            case 9:
                return "Σεπτεμβριος";
            case 10:
                return "Οκτωβριος";
            case 11:
                return "Νοεμβριος";
            case 12:
                return "Δεκεμβριος";
            default:
                return "";
        }

    }

    private void clickOnButtons() {

        textViewMonth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, MonthPieActivity.class);

                String monthAndYear = textViewMonth.getText().toString() + "," + textViewYear.getText().toString() ;
                intent.putExtra("monthAndYear" , monthAndYear );

                startActivity(intent);
            }
        });

        textViewYear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, YearPieActivity.class);
                intent.putExtra("year" , textViewYear.getText().toString() );
                startActivity(intent);
            }
        });

        textViewCalendar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, CalendarActivity.class);
                startActivity(intent);
            }
        });

        // ADD NEW transaction
        textViewNeaSunallagi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addNewTransaction();
            }
        });
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addNewTransaction();
            }
        });
    }

    private void addElementsFromDbToArrList() {
        mdatabaseRefEsoda.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot child: dataSnapshot.getChildren()){
                    String key = child.getKey();
                    transactionObjRef = mdatabaseRefEsoda.child(key) ;
                    addEvLisEsoda(transactionObjRef);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {  }
        });

        mdatabaseRefExoda.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot child: dataSnapshot.getChildren()){
                    String key = child.getKey();
                    transactionObjRef = mdatabaseRefExoda.child(key) ;
                    addEvLisExoda(transactionObjRef);
                }
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
                setClickOnListsExoda(transaction.toString());
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
                setClickOnListsEsoda(transaction.toString());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
        transactionObjRef.addValueEventListener(eventListener);
    }

    private void addNewTransaction() {
        Intent intent = new Intent(MainActivity.this, TransactionActivity.class);
        startActivity(intent);
    }

    private void initializeButtons() {
        addButton = (ImageView) findViewById(R.id.imageView_Add);
        ee = (ImageView) findViewById(R.id.imageView_ee);
        listViewEsoda = (ListView) findViewById(R.id.listViewEsoda);
        listViewExoda = (ListView) findViewById(R.id.listViewExoda);
        textViewNeaSunallagi = (TextView) findViewById(R.id.textView_NeaSunallagi);
        textViewCalendar  = (TextView) findViewById(R.id.textView_OpenCalendar);
        textViewMonth = (TextView) findViewById(R.id.textView_MonthOfMain);
        textViewYear = (TextView) findViewById(R.id.textView_YearOfMain);
        textViewIncomes = (TextView) findViewById(R.id.textView_Incomes);
        textViewOutcomes = (TextView) findViewById(R.id.textView_Outcomes);
        textViewMsg = (TextView) findViewById(R.id.textView_MsgNoData);
    }
}
