package com.jvvas.incomesoutcomes;

import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

public class TransactionActivity extends AppCompatActivity {
    private String TAG = "TransactionActivity : ";
    private static final int PICK_IMAGE_REQUEST = 1 ;

    // DB
    private FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    private DatabaseReference mRootReference = firebaseDatabase.getReference();
    private DatabaseReference mRefYears ;

    private StorageReference mStorageRef;
    private Uri mImageUri;

    private boolean isThereFoto = false;
    private ImageView img;
    private ProgressBar progresBar;
    private CheckBox checkBoxIncome,checkBoxMisthos,checkBoxExoterikiDouleia,checkBoxTzogos ;
    private CheckBox checkBoxOutcome,checkBoxFood,checkBoxDei,checkBoxOte,checkBoxPoto ;
    private TextView textViewImerominia,textViewFoto;
    private EditText edtAmount;
    private Button okButton;
    private String amount = "0" ;
    private String date = "0-0-0000" ;
    private String currentMonth;
    private String currentYear;
    private String type;
    private String esodaExoda = "esoda";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transaction);

        mRefYears = mRootReference.child("years") ;
        mStorageRef = FirebaseStorage.getInstance().getReference("uploads");

        initializeTheButtons();
        clickOnButtoons();
        changeTextOfDate();
    }

    private void changeTextOfDate() {
        Intent incomingDateIntentInfo = getIntent();
        String date = incomingDateIntentInfo.getStringExtra("date");
        textViewImerominia.setText(date);
    }

    private void openFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK
                && (data != null) && (data.getData()!= null) )
        {
            mImageUri = data.getData();
            isThereFoto = true;
            Picasso.with(this).load(mImageUri).into(img);
            // Picasso Lib do this for us
            //img.setImageURI(mImageUri);
        }
    }

    private void clickOnButtoons() {

        textViewFoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFileChooser();
            }
        });

        checkBoxIncome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(checkBoxOutcome.isChecked())
                    checkBoxOutcome.setChecked(false);
                setVisibilitiesOfIncomesOn();
                setVisibilitiesOfOutcomesOff();
            }
        });

        checkBoxOutcome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(checkBoxIncome.isChecked())
                    checkBoxIncome.setChecked(false);
                setVisibilitiesOfOutcomesOn();
                setVisibilitiesOfIncomesOff();
            }
        });

        textViewImerominia.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(TransactionActivity.this, CalendarForType.class);
                startActivity(intent);
            }
        });

        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(isEverythingOk()){
                    Intent intent = new Intent(TransactionActivity.this, MainActivity.class);
                    startActivity(intent);
                }else{
                    Toast.makeText(TransactionActivity.this, "Ωχ, κατι ξεχασες να συμπληρωσεις.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private boolean isEverythingOk() {

        amount = edtAmount.getText().toString();
        date = textViewImerominia.getText().toString();
        currentMonth = returnTheNameOfMonth(date);
        currentYear = returnTheNameOfYear(date);
        type = getTheTypeOfTransaction();

        if(type.equals("No") && !(date==null) && !(amount==null)){
            return false;
        }else{
            // Write to the DB
            Transaction trans;

            if(isThereFoto) {
                uploadFile();
            }else {
                trans = new Transaction(amount, date, type, 0, "");
                DatabaseReference mothRef = mRefYears.child(currentYear).child(currentMonth).child(esodaExoda).push();
                mothRef.setValue(trans);
            }
            return true;
        }
    }

    private String getFileExtension(Uri uri) {
        ContentResolver cR = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cR.getType(uri));
    }

    public void uploadFile() {
        if (mImageUri != null) {
            final StorageReference storageReference = mStorageRef.child(System.currentTimeMillis() + "." + getFileExtension(mImageUri));

            storageReference.putFile(mImageUri).continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }
                    return storageReference.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()) {
                        Uri downloadUri = task.getResult();
                        Log.e(TAG, "then: " + downloadUri.toString());

                        progresBar.setVisibility(View.INVISIBLE);
                        Transaction trans;

                        trans = new Transaction(amount, date, type, 1, downloadUri.toString());

                        DatabaseReference mothRef = mRefYears.child(currentYear).child(currentMonth).child(esodaExoda).push();
                        mothRef.setValue(trans);
                    } else {
                        Toast.makeText(TransactionActivity.this, "upload failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            });

        }
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

    private String returnTheNameOfYear(String date) {
        String[] elements = date.split("-");
        return elements[2] ;
    }

    private String getTheTypeOfTransaction() {
        if(checkBoxIncome.isChecked()){
            esodaExoda = "esoda" ;
            if(checkBoxMisthos.isChecked())
                return "Misthos" ;
            else if(checkBoxExoterikiDouleia.isChecked())
                return "Exo";
            else if(checkBoxTzogos.isChecked())
                return "Tzogos";
        }else if(checkBoxOutcome.isChecked()){
            esodaExoda = "exoda";
            if(checkBoxFood.isChecked())
                return "Food" ;
            else if(checkBoxDei.isChecked())
                return "Dei";
            else if(checkBoxOte.isChecked())
                return "Ote";
            else if(checkBoxPoto.isChecked())
                return "Poto";
        }
        return "No";
    }

    private void setVisibilitiesOfOutcomesOn(){
        checkBoxFood.setVisibility(View.VISIBLE);
        checkBoxDei.setVisibility(View.VISIBLE);
        checkBoxOte.setVisibility(View.VISIBLE);
        checkBoxPoto.setVisibility(View.VISIBLE);
    }

    private void setVisibilitiesOfIncomesOn() {
        checkBoxMisthos.setVisibility(View.VISIBLE);
        checkBoxExoterikiDouleia.setVisibility(View.VISIBLE);
        checkBoxTzogos.setVisibility(View.VISIBLE);
    }

    private void setVisibilitiesOfOutcomesOff() {
        checkBoxFood.setVisibility(View.GONE);
        checkBoxDei.setVisibility(View.GONE);
        checkBoxOte.setVisibility(View.GONE);
        checkBoxPoto.setVisibility(View.GONE);
    }

    private void setVisibilitiesOfIncomesOff() {
        checkBoxMisthos.setVisibility(View.GONE);
        checkBoxExoterikiDouleia.setVisibility(View.GONE);
        checkBoxTzogos.setVisibility(View.GONE);
    }

    private void initializeTheButtons() {
        // CheckBoxes
        checkBoxIncome = (CheckBox) findViewById(R.id.checkBox_Income);
        checkBoxMisthos = (CheckBox) findViewById(R.id.checkBox_Misthos);
        checkBoxExoterikiDouleia = (CheckBox) findViewById(R.id.checkBox_Exo);
        checkBoxTzogos = (CheckBox) findViewById(R.id.checkBox_Tzogos);

        checkBoxOutcome = (CheckBox) findViewById(R.id.checkBox_Outcome);
        checkBoxFood = (CheckBox) findViewById(R.id.checkBox_Food);
        checkBoxDei = (CheckBox) findViewById(R.id.checkBox_Dei);
        checkBoxOte = (CheckBox) findViewById(R.id.checkBox_Ote);
        checkBoxPoto = (CheckBox) findViewById(R.id.checkBox_Poto);

        img = (ImageView) findViewById(R.id.image_Apodeixi);
        progresBar = (ProgressBar) findViewById(R.id.progressBar);
        progresBar.setVisibility(View.GONE);
        textViewFoto = (TextView) findViewById(R.id.textView_AddFoto);

        textViewImerominia = (TextView) findViewById(R.id.textViewEditTextImerominia);
        edtAmount = (EditText) findViewById(R.id.editText_Amount);
        okButton = (Button) findViewById(R.id.button_Ok);
        setVisibilitiesOfIncomesOff();
        setVisibilitiesOfOutcomesOff();
    }
}
