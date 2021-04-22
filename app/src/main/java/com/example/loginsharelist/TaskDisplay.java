package com.example.loginsharelist;

import android.graphics.Color;
import android.graphics.Paint;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.w3c.dom.Text;

/**
 * This class implements a TaskDisplay which the FirebaseRecyclerView will use to display the tasks
 * associated with a certain group. The methods in this class are used to update the RecyclerView.
 */
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

    public void setTaskSearchName(String taskName, String searchStr) {
        int start = taskName.toLowerCase().indexOf(searchStr.toLowerCase());

        if (start != -1) {
            SpannableStringBuilder builder = new SpannableStringBuilder();

            SpannableString str1 = new SpannableString(taskName);
            Log.d("Task Display", "taskName is: " + taskName);
            Log.d("Task Display", "searchStr is: " + searchStr);
            Log.d("Task Display", "int start is: " + start);
            str1.setSpan(new ForegroundColorSpan(0xFF03DAC5), start, start+searchStr.length(), 0);
            builder.append(str1);

            TextView taskNameDisplay = view.findViewById(R.id.taskNameDisplay);
            taskNameDisplay.setText(builder, TextView.BufferType.SPANNABLE);
        } else {
            setTaskName(taskName);
        }
    }

    public void setTaskDescription(String description) {
        TextView taskDescriptionDisplay = view.findViewById(R.id.taskDescriptionDisplay);
        taskDescriptionDisplay.setText(description);
    }

    public void setTaskSearchDescription(String description, String searchStr) {
        int start = description.toLowerCase().indexOf(searchStr.toLowerCase());

        if (start != -1) {
            SpannableStringBuilder builder = new SpannableStringBuilder();

            SpannableString str1 = new SpannableString(description);
            Log.d("Task Display", "description is: " + description);
            Log.d("Task Display", "searchStr is: " + searchStr);
            Log.d("Task Display", "int start is: " + start);
            str1.setSpan(new ForegroundColorSpan(0xFF03DAC5), start, start+searchStr.length(), 0);
            builder.append(str1);

            TextView taskDescriptionDisplay = view.findViewById(R.id.taskDescriptionDisplay);
            taskDescriptionDisplay.setText(builder, TextView.BufferType.SPANNABLE);
        } else {
            setTaskDescription(description);
        }
    }

    public void setTaskDueDate(String taskDueDate) {
        TextView taskDueDateDisplay = view.findViewById(R.id.taskDueDateDisplay);
        taskDueDateDisplay.setText(taskDueDate);
    }

    // TODO: Perhaps we can combine all of these into one setCrossTask() function?
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

    public void setAssignedUsersStr(String assignedUsersStr){
        TextView taskAssignedUsersDisplay = view.findViewById(R.id.taskAssignedUsersDisplay);
        taskAssignedUsersDisplay.setText(assignedUsersStr);
    }

    public void addUserToAssignedUsersStr(String userName){
        TextView taskAssignedUsersDisplay = view.findViewById(R.id.taskAssignedUsersDisplay);
        String oldUsers = taskAssignedUsersDisplay.getText().toString();
        if (oldUsers.length() == 0){
            taskAssignedUsersDisplay.setText(userName);
        } else {
            String newMembers = oldUsers + ", " + userName;
            taskAssignedUsersDisplay.setText(newMembers);
        }
    }
}
