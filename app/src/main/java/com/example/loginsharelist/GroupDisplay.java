package com.example.loginsharelist;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

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
// 123123