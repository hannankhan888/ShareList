// Group 6
// citation sources
// https://www.youtube.com/watch?v=Z-RE1QuUWPg&ab_channel=CodeWithMazn
// https://heartbeat.fritz.ai/implementing-email-and-password-based-authentication-on-android-using-firebase-a3c196952ae2
// https://firebase.google.com/docs/android/setup
package com.example.loginsharelist;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import android.util.Patterns;

/**
 * This activity is used to register users. It can be accessed from the MainActivity.
 * It creates a User object, and updates the database to match.
 */
public class RegisterUser extends AppCompatActivity {
    private EditText createUserName;
    private EditText createPhoneNumber;
    private EditText createEmail;
    private EditText createPassword;
    private Button createAccountButton;

    private FirebaseAuth auth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_user);

        auth = FirebaseAuth.getInstance();

        createUserName = (EditText) findViewById(R.id.createUserName);
        createPhoneNumber = (EditText) findViewById(R.id.createPhoneNumber);
        createEmail = (EditText) findViewById(R.id.createEmail);
        createPassword = (EditText) findViewById(R.id.createPassword);

        // If the user click the create account button,
        // it will create account on the firebase
        createAccountButton = (Button) findViewById(R.id.createAccountButton);
        // We can use the statement lambda to make the code easier to understand
        createAccountButton.setOnClickListener((view) -> createAccountActivity());
    }

    /**
     * Gathers info about users new account. Multiple accounts with the SAME EMAIL are not allowed.
     * Uses the firebase current authentication to create a user and update the database.
     * Displays toasts in both success and failure.
     */
    private void createAccountActivity() {
        // everything is converted to string
        String userName = createUserName.getText().toString().trim();
        String phoneNumber = createPhoneNumber.getText().toString().trim();
        String email = createEmail.getText().toString().trim();
        String password = createPassword.getText().toString().trim();


        // Validate that the entry should not be empty
        if (userName.isEmpty()) {
            createUserName.setError("It should not be empty. ");
            createUserName.requestFocus();
            return;
        }
        if (phoneNumber.isEmpty()) {
            createPhoneNumber.setError("It should not be empty. ");
            createPhoneNumber.requestFocus();
            return;
        }
        if (email.isEmpty()) {
            createEmail.setError("It should not be empty. ");
            createEmail.requestFocus();
            return;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            createEmail.setError("Invalid email");
            createEmail.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            createPassword.setError("It should not be empty. ");
            createPassword.requestFocus();
            return;
        }

        // connect to the firebase
        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                User user = new User(userName, phoneNumber, email, password);
                // We will send everything in user to the firebase database
                FirebaseDatabase.getInstance()
                        .getReference("User")
                        .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                        .setValue(user).addOnCompleteListener(task1 -> {
                            if (task1.isSuccessful()) {
                                Toast.makeText(RegisterUser.this, "The account has been created successfully. ", Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(RegisterUser.this, "The account creation failed!", Toast.LENGTH_LONG).show();
                            }
                        });
            } else {
                Toast.makeText(RegisterUser.this, "The account creation failed!", Toast.LENGTH_LONG).show();
            }
        });
    }
}
