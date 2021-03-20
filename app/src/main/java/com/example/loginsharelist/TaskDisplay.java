package com.example.loginsharelist;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class TaskDisplay extends RecyclerView.ViewHolder {
    View view;

    public TaskDisplay(@NonNull View itemView) {
        super(itemView);
        view = itemView;
    }

    public void setTaskName(String taskName) {
        TextView taskNameDisplay = view.findViewById(R.id.taskNameDisplay);
        taskNameDisplay.setText(taskName);
    }

    public void setTaskDescription(String description) {
        TextView taskDescriptionDisplay = view.findViewById(R.id.taskDescriptionDisplay);
        taskDescriptionDisplay.setText(description);
    }

    public void setTaskDueDate(String taskDueDate) {
        TextView taskDueDateDisplay = view.findViewById(R.id.taskDueDateDisplay);
        taskDueDateDisplay.setText(taskDueDate);
    }
}
