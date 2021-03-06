package com.example.loginsharelist;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

/**
 * This class implements a ForgetPassword activity. This is used when a USER forgot their password.
 * It works by using Firebase to send a password reset email to the users email address.
 */
public class ForgetPassword extends AppCompatActivity {
    private EditText resetEmail;
    private Button resetPasswordButton;

    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forget_password);

        auth = FirebaseAuth.getInstance();

        resetEmail = findViewById(R.id.resetEmail);

        // If the user click the reset password button,
        // it will reset the password
        resetPasswordButton = findViewById(R.id.resetPasswordButton);
        // We can use the statement lambda to make the code easier to understand
        resetPasswordButton.setOnClickListener((view) -> resetPasswordActivity());
    }

    private void resetPasswordActivity() {
        // everything will be converted to the string
        String email = resetEmail.getText().toString().trim();

        // validate that everything is not empty
        if (email.isEmpty()) {
            resetEmail.setError("It should not be empty. ");
            return;
        }

        auth.sendPasswordResetEmail(email).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(ForgetPassword.this, "Check your email to reset the password. ", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(ForgetPassword.this, "The email is wrong. ", Toast.LENGTH_LONG).show();
            }
        });
    }
}
