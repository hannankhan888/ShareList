package com.example.loginsharelist;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

public class AccountInfo extends AppCompatActivity {

    private DatabaseReference databaseReference;
    private FirebaseAuth auth;
    String currUserID;

    private EditText usernameText;
    private EditText emailText;
    private EditText phoneText;
    private Button updateBt;
    private Button resetPassBt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_info);
        getSupportActionBar().setTitle("Account Info");

        auth = FirebaseAuth.getInstance();
        currUserID = auth.getUid();
        databaseReference = FirebaseDatabase.getInstance().getReference().child("User");
        Query query = databaseReference.child(currUserID);

        usernameText = findViewById(R.id.editTextTextPersonName);
        emailText = findViewById(R.id.editTextTextEmailAddress);
        phoneText = findViewById(R.id.editTextPhone);
        updateBt = findViewById(R.id.updateaccountbutton);
        resetPassBt = findViewById(R.id.resetpassbutton);


        query.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if (!task.isSuccessful()) {
                    Log.e("firebase", "Error getting data", task.getException());
                }
                else {
                    User user = task.getResult().getValue(User.class);
                    usernameText.setText(user.userName);
                    emailText.setText(user.emailAddress);
                    phoneText.setText(user.phoneNumber);
                    Log.e("firebase_account_user",user.userName);
                }
            }
        });

        resetPassBt.setOnClickListener((view) -> startActivity(new Intent(this, ForgetPassword.class)));


    }
}