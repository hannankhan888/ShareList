package com.example.loginsharelist;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.DatePickerDialog;
import android.content.Intent;
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
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicReference;

/**
 * This class implements the CreateTask activity for GROUP ADMINS ONLY. It gives admins multiple
 * options such as:
 * add task
 * update task contents
 * mark task as completed
 * display group info
 * leave the group
 * delete the group
 * Any data that is created/updated is also reflected in the database.
 */
public class CreateTaskAdmin extends AppCompatActivity {
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
    private String groupNameStr;
    private String groupIDStr;
    private boolean prevMark;

    public static boolean status = false;


    /**
     * This method sets all necessary vars for this activity. It also sets the app bar title to
     * reflect to the user what group they are currently in.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_task_admin);

        auth = FirebaseAuth.getInstance();
        // How do I pass data between Activities in Android application
        // https://stackoverflow.com/questions/2091465/how-do-i-pass-data-between-activities-in-android-application
        // How to use putExtra() and getExtra() for string data
        // https://stackoverflow.com/questions/5265913/how-to-use-putextra-and-getextra-for-string-data
        groupNameStr = getIntent().getStringExtra("EXTRA_GROUP_NAME");
        groupIDStr = getIntent().getStringExtra("EXTRA_GROUP_ID");
        // The task in the database is like
        // ---Task
        // -------task1
        // -------task2
        // -------task3
        // -------task1
        // -------task2
        // -------task3
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

    /**
     * Adds the corner menu layout for create task.
     * The buttons get created and checked for in onOptionsItemSelected(MenuItem item).
     *
     * @param menu - menu that gets the corner menu layout set.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.corner_menu_for_create_task, menu);
        return super.onCreateOptionsMenu(menu);
    }

    /**
     * Here we handle what happens when a corner menu item gets pressed.
     *
     * @param item - item that gets pressed.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        // its not recommended to use switch statement according to gradle.
        if (id == R.id.createTaskCornerMenuGroupInfoItem) {
            // insert code to show group info Activity HERE.
            // TODO: add a group info activity.
            TaskInfoActivity();
            Log.d(TAG, "Group Info option pressed.");
        } else if (id == R.id.createTaskCornerMenuLeaveGroupItem) {
            // We do the Leave Group stuff here.
            // TODO: add the Leave Group stuff.
            item.setOnMenuItemClickListener((v) -> {
                startActivity(new Intent(CreateTaskAdmin.this, CreateGroup.class));
                finish();
                return true;
            });
            Log.d(TAG, "Leave Group option pressed.");
        }
        return super.onOptionsItemSelected(item);
    }

    private void TaskInfoActivity() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        LayoutInflater layoutInflater = LayoutInflater.from(this);
        View view = layoutInflater.inflate(R.layout.activity_task_info, null);
        alertDialog.setView(view);

        AlertDialog dialog = alertDialog.create();

        Button taskInfoOKButton = view.findViewById(R.id.taskInfoOKButton);

        taskInfoOKButton.setOnClickListener((v) -> {
            dialog.dismiss();
        });

        // It will receive the number of user in the create group activity and show it in the task info view
        TextView taskUserCount = view.findViewById(R.id.taskUserCount);
        String countUser = getIntent().getStringExtra("EXTRA_MEMBER_COUNT");
        taskUserCount.setText(countUser);

        // It will receive the number of admin in the create group activity and show it in the task info view
        TextView taskAdminCount = view.findViewById(R.id.taskAdminCount);
        String countAdmin = getIntent().getStringExtra("EXTRA_ADMIN_COUNT");
        taskAdminCount.setText(countAdmin);

        dialog.show();
    }

    /**
     * Deals with the Task Button. Creates an alert dialog to show the user fields to input the task
     * details.
     * When `save` is pressed, this method will create a task with the contents of the alert dialog
     * and will update the database to match. A toast message is displayed on success.
     */
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
        Button taskDueDate = view.findViewById(R.id.addTaskDueDate);

        taskDueDate.setOnClickListener((v) -> ShowDatePickerDialog(taskDueDate, true));

        // Task save button
        Button taskSaveButton = view.findViewById(R.id.taskSaveButton);
        // We can use the statement lambda to make the code easier to understand
        taskSaveButton.setOnClickListener((v) -> {
            // Everything is converted to string
            String taskNameStr = taskName.getText().toString().trim();
            String taskDescriptionStr = taskDescription.getText().toString().trim();
            // This ID is a new ID created for the task we are about to store in the database.
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

            Task task = new Task(taskNameStr, taskDescriptionStr, id, creationDate, dueDateStr, false, groupIDStr);
            Log.d(TAG, "groupIDStr is " + groupIDStr);
            databaseReference.child(id).setValue(task).addOnCompleteListener(task1 -> {
                if (task1.isSuccessful()) {
                    Toast.makeText(CreateTaskAdmin.this, "The task has been added. ", Toast.LENGTH_LONG).show();
                    dialog.dismiss();
                } else {
                    Toast.makeText(CreateTaskAdmin.this, "The task has not been added. ", Toast.LENGTH_LONG).show();
                    dialog.dismiss();
                }
            });
        });

        Button cancelTaskButton = view.findViewById(R.id.taskCancelButton);
        cancelTaskButton.setOnClickListener((v -> dialog.dismiss()));

        dialog.show();
    }

    /**
     * Creates the FirebaseRecyclerView to hold all tasks for a certain group (groupIDStr).
     */
    @Override
    protected void onStart() {
        super.onStart();

        // The query within SetQuery does the following:
        // gets the database reference (the TASKS table), and orders it by the taskBelongsToGroupID
        // ID stored in each task. Then it checks which ones are equal to the current groupIDStr,
        // and returns those only.
        FirebaseRecyclerOptions<Task> firebaseRecyclerOptions = new FirebaseRecyclerOptions
                .Builder<Task>()
                .setQuery(databaseReference.orderByChild("/taskBelongsToGroupID").equalTo(groupIDStr), Task.class)
                .build();

        FirebaseRecyclerAdapter<Task, TaskDisplay> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Task, TaskDisplay>(firebaseRecyclerOptions) {
            @Override
            public int getItemViewType(int position) {
                // Multiple view in the RecyclerView
                // https://stackoverflow.com/questions/46216540/getting-two-different-views-in-single-recyclerview-using-firebase-in-android
                Task task = getItem(position);
                if (!task.isMark()) {
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

    /**
     * This method deals with the Task Menu. This menu displays options for the GROUP ADMIN to take
     * on the task that was selected. Options include:
     * update contents of task
     * update assigned users
     * mark task as completed
     * This method directs the buttons to their respective activities when they are pressed.
     */
    private void TaskMenuActivity() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        LayoutInflater layoutInflater = LayoutInflater.from(this);
        View view = layoutInflater.inflate(R.layout.activity_task_menu_admin, null);
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

    /**
     * This method deals with updating the selected tasks name.
     * It creates an alert dialog, inflates it to the correct layout.
     * Upon pressing `update`, this method will create a new task, find the old task in the database
     * via the prevTaskID, and update its contents to match.
     * A toast message is displayed on success.
     */
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

            Task task = new Task(updateTaskNameStr, prevTaskDescription, prevTaskID, prevCreationDate, prevDueDate, prevMark, groupIDStr);

            databaseReference.child(prevTaskID).setValue(task).addOnCompleteListener(task1 -> {
                if (task1.isSuccessful()) {
                    Toast.makeText(CreateTaskAdmin.this, "The task has been updated. ", Toast.LENGTH_LONG).show();
                    dialog.dismiss();
                } else {
                    Toast.makeText(CreateTaskAdmin.this, "The task has not been updated. ", Toast.LENGTH_LONG).show();
                    dialog.dismiss();
                }
            });
        });

        dialog.show();
    }

    /**
     * This method deals with updating the selected tasks description.
     * It creates an alert dialog, inflates it to the correct layout.
     * Upon pressing `update`, this method will create a new task, find the old task in the database
     * via the prevTaskID, and update its contents to match.
     * A toast message is displayed on success.
     */
    private void UpdateTaskDescriptionActivity() {
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

            Task task = new Task(prevTaskName, updateTaskDescriptionStr, prevTaskID, prevCreationDate, prevDueDate, prevMark, groupIDStr);

            databaseReference.child(prevTaskID).setValue(task).addOnCompleteListener(task1 -> {
                if (task1.isSuccessful()) {
                    Toast.makeText(CreateTaskAdmin.this, "The task has been updated. ", Toast.LENGTH_LONG).show();
                    dialog.dismiss();
                } else {
                    Toast.makeText(CreateTaskAdmin.this, "The task has not been updated. ", Toast.LENGTH_LONG).show();
                    dialog.dismiss();
                }
            });
        });

        dialog.show();
    }

    /**
     * This method deals with updating the selected tasks due date.
     * It creates an alert dialog, inflates it to the correct layout.
     * Upon pressing `update`, this method will create a new task, find the old task in the database
     * via the prevTaskID, and update its contents to match.
     * A toast message is displayed on success.
     */
    private void UpdateTaskDueDateActivity() {
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

            Task task = new Task(prevTaskName, prevTaskDescription, prevTaskID, prevCreationDate, updateTaskDueDateStr, prevMark, groupIDStr);

            databaseReference.child(prevTaskID).setValue(task).addOnCompleteListener(task1 -> {
                if (task1.isSuccessful()) {
                    Toast.makeText(CreateTaskAdmin.this, "The task has been updated.", Toast.LENGTH_LONG).show();
                    dialog.dismiss();
                } else {
                    Toast.makeText(CreateTaskAdmin.this, "The task has not been updated.", Toast.LENGTH_LONG).show();
                    dialog.dismiss();
                }
            });
        });

        dialog.show();
    }

    /**
     * This method deals with updating the selected tasks mark.
     * It creates an alert dialog, inflates it to the correct layout.
     * Upon pressing `update`, this method will create a new task, find the old task in the database
     * via the prevTaskID, and update its contents to match.
     * A toast message is displayed on success.
     */
    private void UpdateTaskMarkActivity() {
        Query q = databaseReference.child(prevTaskID);
        AtomicReference<Boolean> stat = new AtomicReference<>(true);
        Log.e("task_statusa", String.valueOf(stat.get()));
        q.get().addOnCompleteListener(task -> {
                if (!task.isSuccessful()) {
                    Log.e("CreateTasK_updateMark", "Error getting data", task.getException());
                }
                else {
                    Task t = task.getResult().getValue(Task.class);
                    stat.set(!(t.isMark()));
                    Log.e("task_statusb", String.valueOf(stat.get()));
                    Task t2 = new Task(prevTaskName, prevTaskDescription, prevTaskID, prevCreationDate, prevDueDate, stat.get(), groupIDStr);

                    databaseReference.child(prevTaskID).setValue(t2).addOnCompleteListener(task1 -> {
                        if (task1.isSuccessful()) {
                            Toast.makeText(CreateTaskAdmin.this, "The task has been updated. ", Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(CreateTaskAdmin.this, "The task has not been updated. ", Toast.LENGTH_LONG).show();
                        }
                        Log.e("task_statusc", String.valueOf(stat.get()));
                    });
                }
        });


    }

    /**
     * This method shows a date picker.
     *
     * @param taskDueDate - the button that shows the tasks due date. If no previous due date is set
     *                    the button will show the current date.
     * @param setToToday  - boolean, when set to true, it will show the taskDueDate button as today's
     *                    date. Otherwise the button will show what the tasks due date originally is.
     */
    private void ShowDatePickerDialog(Button taskDueDate, Boolean setToToday) {
        Calendar calendar = Calendar.getInstance();

        if (!setToToday) {
            try {
                // We parse the date from the selected task.
                Date date = new SimpleDateFormat("MM/dd/yyyy", Locale.US).parse(prevDueDate);
                // We set the calendar to match the prevDueDate.
                calendar.setTime(date);
            } catch (ParseException e) {
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
