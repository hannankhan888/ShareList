package com.example.loginsharelist;

import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

/**
 * Implements a class to display a groups contents. This is used whenever we call a
 * FirebaseRecyclerAdapter to display the groups.
 * It shows up in CreateGroup.java and AutoCompleteGroupSearch.java.
 */
public class GroupDisplay extends RecyclerView.ViewHolder {
    private final static String TAG = "GroupDisplay";
    View view;

    public GroupDisplay(@NonNull View itemView) {
        super(itemView);
        view = itemView;
    }

    public void setGroupName(String groupName) {
        TextView groupNameDisplay = view.findViewById(R.id.groupNameDisplay);
        groupNameDisplay.setText(groupName);
    }

    public void setGroupMembersStr(String groupMembersStr){
        TextView groupMembersDisplay = view.findViewById(R.id.groupMembersDisplay);
        groupMembersDisplay.setText(groupMembersStr);
    }

    public void addMemberToGroupMembersStr(String member){
        TextView groupMembersDisplay = view.findViewById(R.id.groupMembersDisplay);
        String oldMembers = groupMembersDisplay.getText().toString();
        if (oldMembers.length() == 0){
            groupMembersDisplay.setText(member);
        } else {
            String newMembers = oldMembers + ", " + member;
            groupMembersDisplay.setText(newMembers);
        }
    }
}
