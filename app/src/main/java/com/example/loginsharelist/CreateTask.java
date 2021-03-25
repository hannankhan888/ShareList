package com.example.loginsharelist;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.DateFormat;
import java.util.Date;

public class CreateTask extends AppCompatActivity {
    private RecyclerView recyclerViewTask;
    private FloatingActionButton addTaskButton;

    private DatabaseReference databaseReference;
    private FirebaseAuth auth;

    private String prevTaskName;
    private String prevTaskDescription;
    private String prevTaskID;
    private String prevCreationDate;
    private String prevDueDate;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_task);

        auth = FirebaseAuth.getInstance();
        // How do I pass data between Activities in Android application
        // https://stackoverflow.com/questions/2091465/how-do-i-pass-data-between-activities-in-android-application
        // How to use putExtra() and getExtra() for string data
        // https://stackoverflow.com/questions/5265913/how-to-use-putextra-and-getextra-for-string-data
        String groupNameStr = getIntent().getStringExtra("EXTRA_GROUP_NAME");
        // The task in the database is like
        // ---Task
        // ------group1
        // ---------task1
        // ---------task2
        // ---------task3
        // ------group2
        // ---------task1
        // ---------task2
        // ---------task3
        databaseReference = FirebaseDatabase.getInstance().getReference().child("Task").child(groupNameStr);

        // Rename app bar to GROUP_NAME - Tasks
        getSupportActionBar().setTitle(groupNameStr + " - Tasks");


        recyclerViewTask = (RecyclerView) findViewById(R.id.recyclerViewTask);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        recyclerViewTask.setHasFixedSize(true);
        recyclerViewTask.setLayoutManager(linearLayoutManager);

        addTaskButton = (FloatingActionButton) findViewById(R.id.addTaskButton);
        // We can use the statement lambda to make the code easier to understand
        addTaskButton.setOnClickListener((view) -> {
            addTaskActivity();
        });
    }

    // Add Task Button
    private void addTaskActivity() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        LayoutInflater layoutInflater = LayoutInflater.from(this);

        // Inflate the input task activity
        View view = layoutInflater.inflate(R.layout.activity_input_task_detail, null);
        alertDialog.setView(view);

        AlertDialog dialog = alertDialog.create();
        dialog.setCancelable(false);

        EditText taskName = view.findViewById(R.id.addTaskName);
        EditText taskDescription = view.findViewById(R.id.addTaskDescription);
        EditText taskDueDate = view.findViewById(R.id.addTaskDueDate);

        // Task save button
        Button taskSaveButton = view.findViewById(R.id.taskSaveButton);
        // We can use the statement lambda to make the code easier to understand
        taskSaveButton.setOnClickListener((v) -> {
            // Everything is converted to string
            String taskNameStr = taskName.getText().toString().trim();
            String taskDescriptionStr = taskDescription.getText().toString().trim();
            String id = databaseReference.push().getKey();
            String creationDate = DateFormat.getDateInstance().format(new Date());
            String dueDateStr = taskDueDate.getText().toString().trim();

            // Validate that everything is not empty
            if (taskNameStr.isEmpty()) {
                taskName.setError("It should not be empty. ");
                return;
            } else if (taskDescriptionStr.isEmpty()) {
                taskDescription.setError("It should not be empty. ");
                return;
            } else if (dueDateStr.isEmpty()) {
                taskDueDate.setError("It should not be empty. ");
                return;
            } else {
                Task task = new Task(taskNameStr, taskDescriptionStr, id, creationDate, dueDateStr);
                databaseReference.child(id).setValue(task).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull com.google.android.gms.tasks.Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(CreateTask.this, "The task has been added. ", Toast.LENGTH_LONG).show();
                            dialog.dismiss();
                        } else {
                            Toast.makeText(CreateTask.this, "The task has not been added. ", Toast.LENGTH_LONG).show();
                            dialog.dismiss();
                        }
                    }
                });
            }
        });

        Button cancelTaskButton = view.findViewById(R.id.taskCancelButton);
        cancelTaskButton.setOnClickListener((v -> dialog.dismiss()));

        dialog.show();
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<Task> firebaseRecyclerOptions = new FirebaseRecyclerOptions
                .Builder<Task>()
                .setQuery(databaseReference, Task.class)
                .build();

        FirebaseRecyclerAdapter<Task, TaskDisplay> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Task, TaskDisplay>(firebaseRecyclerOptions) {

            @NonNull
            @Override
            public TaskDisplay onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                // Inflate the task card view
                View view = LayoutInflater
                        .from(parent.getContext())
                        .inflate(R.layout.activity_display_task_database, parent, false);
                return new TaskDisplay(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull TaskDisplay holder, int position, @NonNull Task model) {
                // It is what is going to display on the task card view
                holder.setTaskName(model.getTaskName());
                holder.setTaskDescription(model.getTaskDescription());
                holder.setTaskDueDate(model.getTaskDueDate());

                // If you click the task, it will open the task menu
                holder.view.setOnClickListener((view) -> {
                    prevTaskName = model.getTaskName();
                    prevTaskDescription = model.getTaskDescription();
                    prevTaskID = getRef(position).getKey();
                    prevCreationDate = model.getTaskCreationDate();
                    prevDueDate = model.getTaskDueDate();
                    TaskMenuActivity();
                });
            }
        };

        // Attach the adapter
        recyclerViewTask.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.startListening();
    }

    private void TaskMenuActivity() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        LayoutInflater layoutInflater = LayoutInflater.from(this);
        View view = layoutInflater.inflate(R.layout.activity_task_menu, null);
        alertDialog.setView(view);

        AlertDialog dialog = alertDialog.create();

        Button taskUpdateNameButton = view.findViewById(R.id.taskMenuUpdateNameButton);
        Button taskUpdateDescriptionButton = view.findViewById(R.id.taskMenuUpdateDescriptionButton);
        Button taskUpdateDueDateButton = view.findViewById(R.id.taskMenuUpdateDueDateButton);
        Button taskUpdateAssignedUsersButton = view.findViewById(R.id.taskMenuUpdateAssignedUsersButton);
        // We can use the statement lambda to make the code easier to understand
        taskUpdateNameButton.setOnClickListener((v) -> {
            UpdateTaskNameActivity();
            dialog.dismiss();
        });

        // taskUpdateDescriptionButton goes here
//        taskUpdateDescriptionButton.setOnClickListener((v) -> {
//            UpdateTaskDescriptionActivity();
//            dialog.dismiss();
//        });

        // taskUpdateDueDateButton goes here
//        taskUpdateDueDateButton.setOnClickListener((v) -> {
//            UpdateTaskDueDateActivity();
//            dialog.dismiss();
//        });

        // taskMenuUpdateAssignedUsersButton goes here
//        taskUpdateAssignedUsersButton.setOnClickListener((v) -> {
//            UpdateTaskAssignedUsersActivity();
//            dialog.dismiss();
//        });

        dialog.show();
    }

    private void UpdateTaskNameActivity() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        LayoutInflater layoutInflater = LayoutInflater.from(this);
        View view = layoutInflater.inflate(R.layout.activity_update_task_name, null);
        alertDialog.setView(view);

        AlertDialog dialog = alertDialog.create();

        EditText taskUpdateNameInput = view.findViewById(R.id.taskUpdateNameInput);

        taskUpdateNameInput.setText(prevTaskName);
        taskUpdateNameInput.setSelection(prevTaskName.length());

        Button taskUpdateNameInputButton = view.findViewById(R.id.taskUpdateNameInputButton);
        // We can use the statement lambda to make the code easier to understand
        taskUpdateNameInputButton.setOnClickListener((v) -> {
            // everything is converted to string
            String taskStr = taskUpdateNameInput.getText().toString().trim();

            // Validate everything that is not empty
            if (taskStr.isEmpty()) {
                taskUpdateNameInput.setError("It should not be empty. ");
                return;
            }

            Task task = new Task(taskStr, prevTaskDescription, prevTaskID, prevCreationDate, prevDueDate);

            databaseReference.child(prevTaskID).setValue(task).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull com.google.android.gms.tasks.Task<Void> task) {
                    if (task.isSuccessful()) {
                        Toast.makeText(CreateTask.this, "The task has been updated. ", Toast.LENGTH_LONG).show();
                        dialog.dismiss();
                    } else {
                        Toast.makeText(CreateTask.this, "The task has not been updated. ", Toast.LENGTH_LONG).show();
                        dialog.dismiss();
                    }
                }
            });
        });

        dialog.show();
    }
}

//private void UpdateTaskDescriptionActivity(){
//
//}

//private void UpdateTaskDueDateActivity(){
//
//}

//private void UpdateTaskAssignedUsersActivity(){
//
//}


// Citation Source
// https://www.youtube.com/watch?v=IVT-XVV4cBk&list=PLlkSO32XQLGpF9HzRulWLpMbU3mWZYlJS&index=2&ab_channel=WilltekSoftwares






















































