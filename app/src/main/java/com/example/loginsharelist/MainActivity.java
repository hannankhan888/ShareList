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

public class MainActivity extends AppCompatActivity {
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
        loginPassword = (EditText) findViewById(R.id.loginPassword);

        // After user is done typing, edit text will scroll back to start.
        loginEmail.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    loginEmail.setSelection(0);
                }
            }
        });
        loginPassword.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    loginEmail.setSelection(0);
                }
            }
        });

        // If the user click the login button,
        // the user will go to the group activity
        loginButton = (Button) findViewById(R.id.loginButton);
        // We can use the statement lambda to make the code easier to understand
        loginButton.setOnClickListener((view) -> {
            loginActivity();
        });

        // If the user click the register link,
        // the user will go to the RegisterUser activity to create the account
        registerLink = (TextView) findViewById(R.id.registerLink);
        // We can use the statement lambda to make the code easier to understand
        registerLink.setOnClickListener((view) -> {
            startActivity(new Intent(this, RegisterUser.class));
        });

        // If the user click the forget password link,
        // the user will go to the ForgetPassword activity to reset the password
        forgetPasswordLink = (TextView) findViewById(R.id.forgetPasswordLink);
        // We can use the statement lambda to make the code easier to understand
        forgetPasswordLink.setOnClickListener((view) -> {
            startActivity(new Intent(this, ForgetPassword.class));
        });
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
