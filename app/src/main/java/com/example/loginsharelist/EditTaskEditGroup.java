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
import android.widget.TextView;
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

public class EditTaskEditGroup extends AppCompatActivity {
    private RecyclerView recyclerView;
    private FloatingActionButton addTaskButton;

    private DatabaseReference databaseReference;
    private FirebaseAuth auth;
    private FirebaseUser user;
    private String onlineUserID;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_task_edit_group);

        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        onlineUserID = user.getUid();
        databaseReference = FirebaseDatabase.getInstance().getReference().child("task").child(onlineUserID);


        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        addTaskButton = (FloatingActionButton) findViewById(R.id.addTaskButton);
        addTaskButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addTaskActivity();
            }
        });
    }

    private void addTaskActivity() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        LayoutInflater layoutInflater = LayoutInflater.from(this);

        View view = layoutInflater.inflate(R.layout.activity__add__task, null);
        alertDialog.setView(view);

        AlertDialog dialog = alertDialog.create();
        dialog.setCancelable(false);

        EditText taskName = view.findViewById(R.id.addTaskName);
        EditText taskDescription = view.findViewById(R.id.addTaskDescription);
        EditText taskDueDate = view.findViewById(R.id.addTaskDueDate);

        Button saveTaskButton = view.findViewById(R.id.taskSaveButton);
        saveTaskButton.setOnClickListener((v) -> {
            String taskNameStr = taskName.getText().toString().trim();
            String taskDescriptionStr = taskDescription.getText().toString().trim();
            String id = databaseReference.push().getKey();
            String creationDate = DateFormat.getDateInstance().format(new Date());
            String dueDate = taskDueDate.getText().toString().trim();

            if (taskNameStr.isEmpty()) {
                taskName.setError("It should not be empty. ");
                return;
            } else if (taskDescriptionStr.isEmpty()) {
                taskDescription.setError("It should not be empty. ");
                return;
            } else {
                Task task = new Task(taskNameStr, taskDescriptionStr, id, creationDate, dueDate);
                databaseReference.child(databaseReference.push().getKey()).setValue(task).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull com.google.android.gms.tasks.Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(EditTaskEditGroup.this, "The task has been added. ", Toast.LENGTH_LONG).show();
                            dialog.dismiss();
                        } else {
                            Toast.makeText(EditTaskEditGroup.this, "The task has not been added. ", Toast.LENGTH_LONG).show();
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
                View view = LayoutInflater
                        .from(parent.getContext())
                        .inflate(R.layout.activity_receive__task__database, parent, false);
                return new TaskDisplay(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull TaskDisplay holder, int position, @NonNull Task model) {
                holder.setTaskName(model.getTaskName());
                holder.setTaskDescription(model.getDescription());
                holder.setTaskDueDate(model.getDueDate());
            }
        };

        recyclerView.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.startListening();
    }

    public static class TaskDisplay extends RecyclerView.ViewHolder {
        View view;

        public TaskDisplay(@NonNull View itemView) {
            super(itemView);
            view = itemView;
        }

        public void setTaskName(String task) {
            TextView taskNameDisplay = view.findViewById(R.id.taskNameDisplay);
            taskNameDisplay.setText(task);
        }

        public void setTaskDescription(String description) {
            TextView taskDescriptionDisplay = view.findViewById(R.id.taskDescriptionDisplay);
            taskDescriptionDisplay.setText(description);
        }

        public void setTaskDueDate(String dueDate) {
            TextView taskDueDateDisplay = view.findViewById(R.id.taskDueDateDisplay);
            taskDueDateDisplay.setText(dueDate);
        }
    }
}


// Citation Source
// https://www.youtube.com/watch?v=IVT-XVV4cBk&list=PLlkSO32XQLGpF9HzRulWLpMbU3mWZYlJS&index=2&ab_channel=WilltekSoftwares






















































