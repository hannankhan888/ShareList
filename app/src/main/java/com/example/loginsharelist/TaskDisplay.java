package com.example.loginsharelist;

import android.graphics.Paint;
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

    public void setCrossTaskName(String taskName) {
        TextView taskNameDisplay = view.findViewById(R.id.taskNameDisplay);
        taskNameDisplay.setText(taskName);
        // https://stackoverflow.com/questions/9786544/creating-a-strikethrough-text
        taskNameDisplay.setPaintFlags(taskNameDisplay.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
    }

    public void setCrossTaskDescription(String description) {
        TextView taskDescriptionDisplay = view.findViewById(R.id.taskDescriptionDisplay);
        taskDescriptionDisplay.setText(description);
        // https://stackoverflow.com/questions/9786544/creating-a-strikethrough-text
        taskDescriptionDisplay.setPaintFlags(taskDescriptionDisplay.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
    }

    public void setCrossTaskDueDate(String taskDueDate) {
        TextView taskDueDateDisplay = view.findViewById(R.id.taskDueDateDisplay);
        taskDueDateDisplay.setText(taskDueDate);
        // https://stackoverflow.com/questions/9786544/creating-a-strikethrough-text
        taskDueDateDisplay.setPaintFlags(taskDueDateDisplay.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
    }
}
