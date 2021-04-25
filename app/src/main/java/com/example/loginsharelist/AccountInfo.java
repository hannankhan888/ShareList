package com.example.loginsharelist;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import java.util.Objects;

public class AccountInfo extends AppCompatActivity {

    private DatabaseReference databaseReferenceUser;
    private FirebaseAuth auth;
    private String currUserID;
    private User currUserObject;

    private EditText usernameText;
    private EditText emailText;
    private EditText phoneText;
    private Button updateBt;
    private Button resetPassBt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_info);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Account Info");

        auth = FirebaseAuth.getInstance();
        currUserID = auth.getUid();
        databaseReferenceUser = FirebaseDatabase.getInstance().getReference().child("User");
        Query query = databaseReferenceUser.child(currUserID);

        usernameText = findViewById(R.id.editTextTextPersonName);
        emailText = findViewById(R.id.editTextTextEmailAddress);
        phoneText = findViewById(R.id.editTextPhone);
        updateBt = findViewById(R.id.updateaccountbutton);
        resetPassBt = findViewById(R.id.resetpassbutton);


        query.get().addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                Log.e("firebase", "Error getting data", task.getException());
            }
            else {
                currUserObject = Objects.requireNonNull(task.getResult()).getValue(User.class);
                assert currUserObject != null;
                usernameText.setText(currUserObject.userName);
                emailText.setText(currUserObject.emailAddress);
                phoneText.setText(currUserObject.phoneNumber);
                Log.e("firebase_account_user", currUserObject.userName);
            }
        });

        resetPassBt.setOnClickListener(v -> {
            startActivity(new Intent(this, ForgetPassword.class));
            finish();
        });

        updateBt.setOnClickListener((view) -> UpdateAccountInfoActivity());
    }

    private void UpdateAccountInfoActivity() {
        // create a new user object with updated values.
        User updatedUser = new User();
        updatedUser.setEmailAddress(emailText.getText().toString().trim());
        updatedUser.setUserID(currUserID);
        updatedUser.setPhoneNumber(phoneText.getText().toString().trim());
        updatedUser.setUserName(usernameText.getText().toString().trim());
        updatedUser.setPassword(currUserObject.getPassword());

        if (updatedUser == currUserObject) {
            Toast.makeText(this, "No Account Info Has Changed.", Toast.LENGTH_LONG).show();
        } else {
            databaseReferenceUser.child(currUserID).setValue(updatedUser).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(this, "Account Info Updated", Toast.LENGTH_LONG).show();
                    finish();
                } else {
                    Toast.makeText(this, "ERROR UPDATING Account Info", Toast.LENGTH_LONG).show();
                }
            });
        }
    }
}