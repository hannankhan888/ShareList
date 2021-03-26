package com.example.loginsharelist;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class CreateTask extends AppCompatActivity {
    private static final String TAG = "CreateTask";

    private RecyclerView recyclerViewTask;
    private FloatingActionButton addTaskButton;

    private DatabaseReference databaseReference;
    private FirebaseAuth auth;

    private String prevTaskName;
    private String prevTaskDescription;
    private String prevTaskID;
    private String prevCreationDate;
    private String prevDueDate;
    private boolean prevMark;

    public static boolean status = false;


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
        String groupIDStr = getIntent().getStringExtra("EXTRA_GROUP_ID");
        // The task in the database is like
        // ---Task
        // -------task1
        // -------task2
        // -------task3
        // -------task1
        // -------task2
        // -------task3
        // TODO: remove child(groupNAmeStr) to store tasks correctly.
        // TODO: make sure to delete the relative database on firebase.com
        databaseReference = FirebaseDatabase.getInstance().getReference().child("Tasks");

        // Rename app bar to GROUP_NAME - Tasks
        getSupportActionBar().setTitle(groupNameStr + " - Tasks");


        recyclerViewTask = (RecyclerView) findViewById(R.id.recyclerViewTask);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        recyclerViewTask.setHasFixedSize(true);
        recyclerViewTask.setLayoutManager(linearLayoutManager);

        addTaskButton = findViewById(R.id.addTaskButton);
        // We can use the statement lambda to make the code easier to understand
        addTaskButton.setOnClickListener((view) -> addTaskActivity());
    }

    // add the corner menu layout for create task.
    // the buttons get created and checked for in onOptionsItemSelected(MenuItem item)
    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.corner_menu_for_create_task, menu);
        return super.onCreateOptionsMenu(menu);
    }

    // Here we handle what happens when a corner menu item gets pressed.
    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        int id = item.getItemId();

        // its not recommended to use switch statement according to gradle.
        if (id == R.id.createTaskCornerMenuGroupInfoItem) {
            // insert code to show group info Activity HERE.
            // TODO: add a group info activity.
            Log.d(TAG, "Group Info option pressed.");
        } else if (id == R.id.createTaskCornerMenuLeaveGroupItem){
            // We do the Leave Group stuff here.
            // TODO: add the Leave Group stuff.
            Log.d(TAG, "Leave Group option pressed.");
        }
        return super.onOptionsItemSelected(item);
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
        EditText taskDescription= view.findViewById(R.id.addTaskDescription);
        Button taskDueDate = view.findViewById(R.id.addTaskDueDate);

        taskDueDate.setOnClickListener((v) -> ShowDatePickerDialog(taskDueDate, true));

        // Task save button
        Button taskSaveButton = view.findViewById(R.id.taskSaveButton);
        // We can use the statement lambda to make the code easier to understand
        taskSaveButton.setOnClickListener((v) -> {
            // Everything is converted to string
            String taskNameStr = taskName.getText().toString().trim();
            String taskDescriptionStr = taskDescription.getText().toString().trim();
            // TODO: find out what id this refers to and log it. or comment it.
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
            }

            Task task = new Task(taskNameStr, taskDescriptionStr, id, creationDate, dueDateStr, false);
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
        });

        Button cancelTaskButton = view.findViewById(R.id.taskCancelButton);
        cancelTaskButton.setOnClickListener((v -> dialog.dismiss()));

        dialog.show();
    }

    @Override
    protected void onStart() {
        // TODO: update what content gets displayed for the group. Update the setQuery(databaseReference.
        super.onStart();

        FirebaseRecyclerOptions<Task> firebaseRecyclerOptions = new FirebaseRecyclerOptions
                .Builder<Task>()
                .setQuery(databaseReference, Task.class)
                .build();

        FirebaseRecyclerAdapter<Task, TaskDisplay> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Task, TaskDisplay>(firebaseRecyclerOptions) {
            @Override
            public int getItemViewType(int position) {
                // Multiple view in the RecyclerView
                // https://stackoverflow.com/questions/46216540/getting-two-different-views-in-single-recyclerview-using-firebase-in-android
                Task task = getItem(position);
                if (task.isMark() == false) {
                    return 0;
                } else {
                    return 1;
                }
            }

            @NonNull
            @Override
            public TaskDisplay onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                if (viewType == 0) {
                    // It is mark is false
                    // Inflate the task card view
                    View view = LayoutInflater
                            .from(parent.getContext())
                            .inflate(R.layout.activity_display_task_database, parent, false);
                    return new TaskDisplay(view);
                } else {
                    // It is mark is true
                    // Inflate the task card view
                    // It is going to show you the same layout if you mark the task
                    // If you are going to use the more beautiful layout you can put it here
                    // Then you have to update the onBindViewHolder
                    View view = LayoutInflater
                            .from(parent.getContext())
                            .inflate(R.layout.activity_display_task_database, parent, false);
                    return new TaskDisplay(view);
                }
            }

            @Override
            protected void onBindViewHolder(@NonNull TaskDisplay holder, int position, @NonNull Task model) {
                if (holder.getItemViewType() == 0) {
                    // It is mark is false
                    // It is what is going to display on the task card view
                    holder.setTaskName(model.getTaskName());
                    holder.setTaskDescription(model.getTaskDescription());
                    holder.setTaskDueDate(model.getTaskDueDate());
                } else if (holder.getItemViewType() == 1) {
                    // It is mark is true
                    // It is what is going to display on the task card view
                    holder.setCrossTaskName(model.getTaskName());
                    holder.setCrossTaskDescription(model.getTaskDescription());
                    holder.setCrossTaskDueDate(model.getTaskDueDate());
                }


//                if (status) {
//                    // It is what is going to display on the task card view
//                    holder.setCrossTaskName(model.getTaskName());
//                    holder.setCrossTaskDescription(model.getTaskDescription());
//                    holder.setCrossTaskDueDate(model.getTaskDueDate());
//                } else {
//                    // It is what is going to display on the task card view
//                    holder.setTaskName(model.getTaskName());
//                    holder.setTaskDescription(model.getTaskDescription());
//                    holder.setTaskDueDate(model.getTaskDueDate());
//                }

                // If you click the task, it will open the task menu
                holder.view.setOnClickListener((view) -> {
                    prevTaskName = model.getTaskName();
                    prevTaskDescription = model.getTaskDescription();
                    prevTaskID = getRef(position).getKey();
                    prevCreationDate = model.getTaskCreationDate();
                    prevDueDate = model.getTaskDueDate();
                    prevMark = model.isMark();
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

        taskUpdateDescriptionButton.setOnClickListener((v) -> {
            UpdateTaskDescriptionActivity();
            dialog.dismiss();
        });

        // taskUpdateDueDateButton goes here
        taskUpdateDueDateButton.setOnClickListener((v) -> {
            UpdateTaskDueDateActivity();
            dialog.dismiss();
        });

        // taskMenuUpdateAssignedUsersButton goes here
//        taskUpdateAssignedUsersButton.setOnClickListener((v) -> {
//            UpdateTaskAssignedUsersActivity();
//            dialog.dismiss();
//        });

        Button taskMenuMarkButton = view.findViewById(R.id.taskMenuMarkButton);
        // We can use the statement lambda to make the code easier to understand
        taskMenuMarkButton.setOnClickListener((v) -> {
            UpdateTaskMarkActivity();
            dialog.dismiss();
        });

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
            String updateTaskNameStr = taskUpdateNameInput.getText().toString().trim();

            // Validate everything that is not empty
            if (updateTaskNameStr.isEmpty()) {
                taskUpdateNameInput.setError("It should not be empty. ");
                return;
            }

            Task task = new Task(updateTaskNameStr, prevTaskDescription, prevTaskID, prevCreationDate, prevDueDate, prevMark);

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

    private void UpdateTaskDescriptionActivity(){
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        LayoutInflater layoutInflater = LayoutInflater.from(this);
        View view = layoutInflater.inflate(R.layout.activity_update_task_description, null);
        alertDialog.setView(view);

        AlertDialog dialog = alertDialog.create();

        EditText taskUpdateDescriptionInput = view.findViewById(R.id.taskUpdateDescriptionInput);

        taskUpdateDescriptionInput.setText(prevTaskDescription);
        taskUpdateDescriptionInput.setSelection(prevTaskDescription.length());

        Button taskUpdateDescriptionButton = view.findViewById(R.id.taskUpdateDescriptionButton);

        taskUpdateDescriptionButton.setOnClickListener((v) -> {
            // everything is converted to string
            String updateTaskDescriptionStr = taskUpdateDescriptionInput.getText().toString().trim();

            // Validate everything that is not empty
            if (updateTaskDescriptionStr.isEmpty()) {
                taskUpdateDescriptionInput.setError("It should not be empty.");
                return;
            }

            Task task = new Task(prevTaskName, updateTaskDescriptionStr, prevTaskID, prevCreationDate, prevDueDate, prevMark);

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


    private void UpdateTaskDueDateActivity(){
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        LayoutInflater layoutInflater = LayoutInflater.from(this);
        View view = layoutInflater.inflate(R.layout.activity_update_task_due_date, null);
        alertDialog.setView(view);

        AlertDialog dialog = alertDialog.create();

        Button taskUpdateDueDateInputButton = view.findViewById(R.id.taskUpdateDueDateInputButton);
        taskUpdateDueDateInputButton.setText(prevDueDate);
        taskUpdateDueDateInputButton.setOnClickListener((v) -> ShowDatePickerDialog(taskUpdateDueDateInputButton, false));

        Button taskUpdateDueDateSubmitButton = view.findViewById(R.id.taskUpdateDueDateSubmitButton);
        taskUpdateDueDateSubmitButton.setOnClickListener((v) -> {
            // By now, the taskUpdateDueDateInputButton has the value for chosen date.
            // everything is converted to string
            String updateTaskDueDateStr = taskUpdateDueDateInputButton.getText().toString().trim();

            // Validate everything that is not empty
            if (updateTaskDueDateStr.isEmpty()) {
                taskUpdateDueDateInputButton.setError("It should not be empty.");
                return;
            }

            Task task = new Task(prevTaskName, prevTaskDescription, prevTaskID, prevCreationDate, updateTaskDueDateStr, prevMark);

            databaseReference.child(prevTaskID).setValue(task).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull com.google.android.gms.tasks.Task<Void> task) {
                    if (task.isSuccessful()) {
                        Toast.makeText(CreateTask.this, "The task has been updated.", Toast.LENGTH_LONG).show();
                        dialog.dismiss();
                    } else {
                        Toast.makeText(CreateTask.this, "The task has not been updated.", Toast.LENGTH_LONG).show();
                        dialog.dismiss();
                    }
                }
            });
        });

        dialog.show();
    }

    private void UpdateTaskMarkActivity() {
        Task task = new Task(prevTaskName, prevTaskDescription, prevTaskID, prevCreationDate, prevDueDate, true);

        databaseReference.child(prevTaskID).setValue(task).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull com.google.android.gms.tasks.Task<Void> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(CreateTask.this, "The task has been updated. ", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(CreateTask.this, "The task has not been updated. ", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void ShowDatePickerDialog(Button taskDueDate, Boolean setToToday){
        Calendar calendar = Calendar.getInstance();

        if (!setToToday) {
            try{
                // We parse the date from the selected task.
                Date date = new SimpleDateFormat("MM/dd/yyyy", Locale.US).parse(prevDueDate);
                // We set the calendar to match the prevDueDate.
                calendar.setTime(date);
            } catch (ParseException e){
                Log.d(TAG, "Date was not able to be parsed: " + prevDueDate);
            }
        }

        // I didn't replace the OnDateSetListener with a lambda because lambda is too confusing.
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        String correct_month = String.valueOf(month + 1);
                        String date = correct_month + "/" + dayOfMonth + "/" + year;
                        taskDueDate.setText(date);
                    }
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }
}


//private void UpdateTaskAssignedUsersActivity(){
//
//}


// Citation Source
// https://www.youtube.com/watch?v=IVT-XVV4cBk&list=PLlkSO32XQLGpF9HzRulWLpMbU3mWZYlJS&index=2&ab_channel=WilltekSoftwares
