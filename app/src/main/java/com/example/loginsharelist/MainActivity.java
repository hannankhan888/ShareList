package com.example.loginsharelist;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private EditText loginEmail;
    private EditText loginPassword;
    private Button loginButton;
    private TextView registerLink;
    private TextView forgetPasswordLink;

    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().hide();

        auth = FirebaseAuth.getInstance();

        loginEmail = (EditText) findViewById(R.id.loginEmail);
        loginPassword = (EditText) findViewById(R.id.loginpPassword);

        // If the user click the login button,
        // the user will go to the edit task and edit group activity
        loginButton = (Button) findViewById(R.id.loginButton);
        loginButton.setOnClickListener(this);

        // If the user click the register link,
        // the user will go to the RegisterUser activity to create the account
        registerLink = (TextView) findViewById(R.id.registerLink);
        registerLink.setOnClickListener(this);

        // If the user click the forget password link,
        // the user will go to the ForgetPassword activity to reset the password
        forgetPasswordLink = (TextView) findViewById(R.id.forgetPasswordLink);
        forgetPasswordLink.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.registerLink) {
            // If the user click the register link,
            // the user will go to the RegisterUser activity to create the account
            startActivity(new Intent(this, RegisterUser.class));
        } else if (v.getId() == R.id.loginButton) {
            // If the user click the create login button
            // start the login activity
            loginActivity();
        } else if (v.getId() == R.id.forgetPasswordLink) {
            // If the user click the forget password link
            // start the forget password activity to reset the password
            startActivity(new Intent(this, ForgetPassword.class));
        }
    }

    private void loginActivity() {
        // everything will be converted to string
        String email = loginEmail.getText().toString().trim();
        String password = loginPassword.getText().toString().trim();

        // validate that everything is not empty
        if (email.isEmpty()) {
            loginEmail.setError("It should not be empty. ");
            return;
        }
        if (password.isEmpty()) {
            loginPassword.setError("It should not be empty. ");
            return;
        }

        // connect to the firebase
        // The user will use their email and password to login
        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                // If the user login, the user will go to the create group activity
                if (task.isSuccessful()) {
                    startActivity(new Intent(MainActivity.this, CreateGroup.class));
                } else {
                    Toast.makeText(MainActivity.this, "The email and the password is wrong. ", Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}









