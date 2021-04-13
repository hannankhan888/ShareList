package com.example.loginsharelist;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

/**
 * This class implements the MainActivity, which for our app ShareList is just the login page.
 */
public class MainActivity extends AppCompatActivity {
    private EditText loginEmail;
    private EditText loginPassword;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().hide();

        auth = FirebaseAuth.getInstance();

        // keep user logged in
        if (auth.getCurrentUser() != null) {
            startActivity(new Intent(MainActivity.this, CreateGroup.class));
        }

        loginEmail = findViewById(R.id.loginEmail);
        loginPassword = findViewById(R.id.loginPassword);

        // After user is done typing, edit text will scroll back to start.
        loginEmail.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                loginEmail.setSelection(0);
            }
        });
        loginPassword.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                loginEmail.setSelection(0);
            }
        });

        // If the user click the login button,
        // the user will go to the group activity
        Button loginButton = findViewById(R.id.loginButton);
        // We can use the statement lambda to make the code easier to understand
        loginButton.setOnClickListener((view) -> loginActivity());

        // If the user click the register link,
        // the user will go to the RegisterUser activity to create the account
        TextView registerLink = findViewById(R.id.registerLink);
        // We can use the statement lambda to make the code easier to understand
        registerLink.setOnClickListener((view) -> startActivity(new Intent(this, RegisterUser.class)));

        // If the user click the forget password link,
        // the user will go to the ForgetPassword activity to reset the password
        TextView forgetPasswordLink = findViewById(R.id.forgetPasswordLink);
        // We can use the statement lambda to make the code easier to understand
        forgetPasswordLink.setOnClickListener((view) -> startActivity(new Intent(this, ForgetPassword.class)));
    }

    /**
     * This method gets the email and password and uses Firebase to sign-in the user.
     * Displays a toast if it fails.
     */
    private void loginActivity() {
        // everything will be converted to string
        String email = loginEmail.getText().toString().trim();
        String password = loginPassword.getText().toString().trim();

        // validate that everything is not empty
        if (email.isEmpty()) {
            loginEmail.setError("It should not be empty. ");
            loginEmail.requestFocus();
            return;
        }
        else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            loginEmail.setError("Enter a valid email");
            loginEmail.requestFocus();
            return;
        }
        if (password.isEmpty()) {
            loginPassword.setError("It should not be empty. ");
            loginPassword.requestFocus();
            return;
        }

        // connect to the firebase
        // The user will use their email and password to login
        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                // If the user login, the user will go to the create group activity
                if (task.isSuccessful()) {
                    // Clears the password for if you logout.
                    loginPassword.setText("");
                    startActivity(new Intent(MainActivity.this, CreateGroup.class));
                } else {
                    Toast.makeText(MainActivity.this, "Incorrect Email or Password!", Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}
