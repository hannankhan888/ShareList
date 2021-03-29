package com.example.loginsharelist;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

/**
 * Implements a class to display a groups contents. This is used whenever we call a
 * FirebaseRecyclerAdapter to display the groups.
 * It shows up in CreateGroup.java and AutoCompleteGroupSearch.java.
 * TODO: implement a group members display.
 */
public class GroupDisplay extends RecyclerView.ViewHolder {
    View view;

    public GroupDisplay(@NonNull View itemView) {
        super(itemView);
        view = itemView;
    }

    public void setGroupName(String groupName) {
        TextView groupNameDisplay = view.findViewById(R.id.groupNameDisplay);
        groupNameDisplay.setText(groupName);
    }
}
