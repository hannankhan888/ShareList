package com.example.loginsharelist;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class UserDisplay extends RecyclerView.ViewHolder {
    private static final String TAG = "UserDisplay";
    View view;

    public UserDisplay(@NonNull View itemView) {
        super(itemView);
        view = itemView;
    }

    public void setUserName(String userName){
        TextView userNameDisplay = view.findViewById(R.id.userNameDisplay);
        userNameDisplay.setText(userName);
    }

    public void setUserEmail(String userEmail){
        TextView userEmailDisplay = view.findViewById(R.id.userEmailDisplay);
        userEmailDisplay.setText(userEmail);
    }
}
