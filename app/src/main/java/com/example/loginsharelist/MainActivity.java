package com.example.loginsharelist;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private TextView registerLink;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // If the user click the register link,
        // the user will go to the RegisterUser activity to create the account
        registerLink = (TextView) findViewById(R.id.registerLink);
        registerLink.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        // If the user click the register link,
        // the user will go to the RegisterUser activity to create the account
        if (v.getId() == R.id.registerLink) {
            startActivity(new Intent(this, RegisterUser.class));
        }
    }
}