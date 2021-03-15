// Group 6
// citation sources
// https://www.youtube.com/watch?v=Z-RE1QuUWPg&ab_channel=CodeWithMazn
// https://heartbeat.fritz.ai/implementing-email-and-password-based-authentication-on-android-using-firebase-a3c196952ae2
// https://firebase.google.com/docs/android/setup
package com.example.loginsharelist;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

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
import com.google.firebase.database.FirebaseDatabase;

public class RegisterUser extends AppCompatActivity implements View.OnClickListener {
    private FirebaseAuth mAuth;
    private TextView registerTitle;
    private EditText createUserName;
    private EditText createPhoneNumber;
    private EditText createEmail;
    private EditText createPassword;
    private Button createAccountButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_user);

        mAuth = FirebaseAuth.getInstance();

        createAccountButton = (Button) findViewById(R.id.createAccountButton);
        createAccountButton.setOnClickListener(this);

        createUserName = (EditText) findViewById(R.id.createUserName);
        createPhoneNumber = (EditText) findViewById(R.id.createPhoneNumber);
        createEmail = (EditText) findViewById(R.id.createEmail);
        createPassword = (EditText) findViewById(R.id.createPassword);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.createAccountButton) {
            createAccountActivity();
        }
    }

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
        }
        if (password.isEmpty()) {
            createPassword.setError("It should not be empty. ");
            createPassword.requestFocus();
            return;
        }

        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    User user = new User(userName, phoneNumber, email);
                    FirebaseDatabase.getInstance()
                            .getReference("User")
                            .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                            .setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(RegisterUser.this, "The account has been created successfully. ", Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(RegisterUser.this, "The account has not been created successfully. ", Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                } else {
                    Toast.makeText(RegisterUser.this, "The account has not been created successfully. ", Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}














































