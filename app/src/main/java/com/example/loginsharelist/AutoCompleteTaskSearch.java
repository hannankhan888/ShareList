package com.example.loginsharelist;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.firebase.ui.database.SnapshotParser;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

/**
 * This class implements an autocomplete search for groups.*/
public class AutoCompleteTaskSearch extends AppCompatActivity {
    public static final String TAG = "AutoCompleteTaskSearch";

    private static final int ASSIGN_USER_TO_TASK_REQUEST_CODE = 0;
    private static final int REMOVE_ASSIGNED_USER_REQUEST_CODE = 1;

    private DatabaseReference databaseReference;
    private DatabaseReference databaseReferenceTask;
    private RecyclerView autoTaskSearchList;
    private EditText autoTaskSearchInput;

    FirebaseRecyclerAdapter<Task, TaskDisplay> firebaseRecyclerAdapter;
    private FirebaseAuth auth;

    private String prevTaskName;
    private String prevTaskDescription;
    private String prevTaskID;
    private String prevCreationDate;
    private String prevDueDate;
    private String prevBelongsToGroupID;
    private boolean prevMark;

    private String currUserID;
    private String currGroupID;
    private String currGroupName;
    private Boolean currUserIsAdmin;

    private Map<String, String> prevTaskAssignedUsers;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auto_complete_task_search);


        auth = FirebaseAuth.getInstance();
        currUserID = auth.getUid();
        databaseReference = FirebaseDatabase.getInstance().getReference();
        databaseReferenceTask = FirebaseDatabase.getInstance().getReference().child("Tasks");

        autoTaskSearchList = (RecyclerView) findViewById(R.id.autoCompleteTaskSearchList);
        autoTaskSearchList.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        autoTaskSearchList.setHasFixedSize(true);

        currGroupID = getIntent().getStringExtra("EXTRA_GROUP_ID");
        currGroupName = getIntent().getStringExtra("EXTRA_GROUP_NAME");
        currUserIsAdmin = getIntent().getBooleanExtra("EXTRA_CURR_USER_IS_ADMIN", false);
        getSupportActionBar().setTitle(currGroupName + " - Search Tasks");

        autoTaskSearchInput = (EditText) findViewById(R.id.autoCompleteTaskSearchInput);
        // Citation Source
        // https://www.youtube.com/watch?v=b_tz8kbFUsU&ab_channel=TVACStudio
        // https://www.youtube.com/watch?v=_nIoEAC3kLg&ab_channel=TechnicalSkillz
        // It shows you how to search data in the firebase realtime database
        // One video teach you how to use auto complete search
        // Another video teach you how to search it manually
        // One video is outdated, so the syntax has changed
        // So you have to change several line of code to make it work
        TaskSearch("");
        autoTaskSearchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            // It is waiting for the text to be changed
            // https://developer.android.com/reference/android/text/TextWatcher.html
            // https://stackoverflow.com/questions/26992407/ontextchanged-vs-aftertextchanged-in-android-live-examples-needed
            public void afterTextChanged(Editable s) {
                TaskSearch(s.toString().toLowerCase().trim());
            }
        });
    }

    /**
     * This method implements the actual group search. It does this by using a query, that searches
     * for the group name (data). It also has the option to start the particular CreateTask activity
     * for the group selected.
     * */
    private void TaskSearch(String taskNameStr) {
        Query query = databaseReferenceTask.orderByChild("/taskBelongsToGroupID").equalTo(currGroupID);

        //https://stackoverflow.com/questions/52041870/does-using-snapshotparser-while-querying-firestore-an-expensive-operation
        FirebaseRecyclerOptions firebaseRecyclerOptions = new FirebaseRecyclerOptions
                .Builder<Task>()
                .setQuery(query, new SnapshotParser<Task>() {
                    @NonNull
                    @Override
                    public Task parseSnapshot(@NonNull DataSnapshot snapshot) {
                        Task tempTask = snapshot.getValue(Task.class);
                        if (tempTask.getTaskName().toLowerCase().contains(taskNameStr) | tempTask.getTaskDescription().toLowerCase().contains(taskNameStr)) {
                            return tempTask;
                        } else {
                            return new Task();
                        }
                    }
                })
                .build();

        firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Task, TaskDisplay>(firebaseRecyclerOptions) {
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
                // Inflate the task card view
//                if (viewType == 0) {
//                    // It is mark is false
//                    // Inflate the task card view
//                    View view = LayoutInflater
//                            .from(parent.getContext())
//                            .inflate(R.layout.activity_display_task_database, parent, false);
//                    return new TaskDisplay(view);
//                } else {
//                    // It is mark is true
//                    // Inflate the task card view
//                    // It is going to show you the same layout if you mark the task
//                    // If you are going to use the more beautiful layout you can put it here
//                    // Then you have to update the onBindViewHolder
//                    View view = LayoutInflater
//                            .from(parent.getContext())
//                            .inflate(R.layout.activity_display_task_database, parent, false);
//                    return new TaskDisplay(view);
//                }
                View view = LayoutInflater
                        .from(parent.getContext())
                        .inflate(R.layout.activity_display_task_database, parent, false);
                return new TaskDisplay(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull TaskDisplay holder, int position, @NonNull Task model) {
                if (model.getTaskId() == null) {
                    holder.itemView.setVisibility(View.GONE);
                    holder.itemView.setLayoutParams(new RecyclerView.LayoutParams(0, 0));
                } else {
                    holder.itemView.setVisibility(View.VISIBLE);
                    holder.itemView.setLayoutParams(new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

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

                    Map<String, String> taskAssignedUsers = model.getTaskAssignedUsers();
                    if (taskAssignedUsers.size() >= 1){
                        for (String userID : taskAssignedUsers.keySet()) {
                            Query queryToGetUserName = databaseReference.child("User").child(userID).child("userName");
                            queryToGetUserName.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    String userName = snapshot.getValue(String.class);
                                    holder.addUserToAssignedUsersStr(userName);
                                    //TODO: here we have an example of a remove listener that is working.
                                    queryToGetUserName.removeEventListener(this);
                                    Log.d(TAG, "Assigned User Name: " + userName);
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                    Log.d(TAG, "Unable to get Assigned User Name: " + error);
                                }
                            });
                        }
                    } else {
                        holder.setAssignedUsersStr("No one.");
                    }


                    // Check if current user is an ADMIN. Launch the respective activity.
                    holder.view.setOnClickListener((view) -> {
                        prevTaskName = model.getTaskName();
                        prevTaskDescription = model.getTaskDescription();
                        prevTaskID = getRef(position).getKey();
                        prevCreationDate = model.getTaskCreationDate();
                        prevDueDate = model.getTaskDueDate();
                        prevBelongsToGroupID = model.getTaskBelongsToGroupID();
                        prevMark = model.isMark();
                        prevTaskAssignedUsers = model.getTaskAssignedUsers();

                        if (currUserIsAdmin) {
                            TaskMenuActivity();
                        } else {
                            TaskMenuActivityForUsers();
                        }
                    });
                }
            }
        };
        // Attach the adapter
        firebaseRecyclerAdapter.startListening();
        autoTaskSearchList.setAdapter(firebaseRecyclerAdapter);
    }

    private void TaskMenuActivityForUsers() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        LayoutInflater layoutInflater = LayoutInflater.from(this);
        View view = layoutInflater.inflate(R.layout.activity_task_menu_user, null);
        alertDialog.setView(view);

        AlertDialog dialog = alertDialog.create();

        Button taskMenuUserMarkButton = view.findViewById(R.id.taskMenuUserMarkButton);
        // We can use the statement lambda to make the code easier to understand
        taskMenuUserMarkButton.setOnClickListener((v) -> {
            UpdateTaskMarkActivity();
            dialog.dismiss();
        });

        dialog.show();
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

        Button taskUpdateTaskContentsButton = view.findViewById(R.id.taskMenuUpdateTaskContentsButton);
        // We can use the statement lambda to make the code easier to understand
        taskUpdateTaskContentsButton.setOnClickListener((v) -> {
            UpdateTaskContentsActivity();
            dialog.dismiss();
        });

        // taskMenuUpdateAssignedUsersButton goes here
        Button taskUpdateAssignedUsersButton = view.findViewById(R.id.taskMenuUpdateAssignedUsersButton);
        taskUpdateAssignedUsersButton.setOnClickListener((v) -> {
            UpdateTaskAssignedUsersActivity();
            dialog.dismiss();
        });

        Button taskMenuMarkButton = view.findViewById(R.id.taskMenuMarkButton);
        // We can use the statement lambda to make the code easier to understand
        taskMenuMarkButton.setOnClickListener((v) -> {
            UpdateTaskMarkActivity();
            dialog.dismiss();
        });

        Button taskRemoveTaskButton = view.findViewById(R.id.taskRemoveTaskButton);
        taskRemoveTaskButton.setOnClickListener((v) -> {
            RemoveTaskActivity();
            dialog.dismiss();
        });

        dialog.show();
    }

    /**
     * This method deals with updating the selected tasks contents.
     * It creates an alert dialog, inflates it to the correct layout.
     * Upon pressing `update`, this method will create a new task, find the old task in the database
     * via the prevTaskID, and update its contents to match.
     * A toast message is displayed on success.
     */
    private void UpdateTaskContentsActivity(){
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        LayoutInflater layoutInflater = LayoutInflater.from(this);
        View view = layoutInflater.inflate(R.layout.activity_input_task_detail, null);
        alertDialog.setView(view);

        AlertDialog dialog = alertDialog.create();

        EditText newTaskNameInput = view.findViewById(R.id.addTaskName);
        EditText newTaskDescriptionInput = view.findViewById(R.id.addTaskDescription);
        Button newTaskDueDateButtonInput = view.findViewById(R.id.addTaskDueDate);
        Button saveButton = view.findViewById(R.id.taskSaveButton);
        Button cancelButton = view.findViewById(R.id.taskCancelButton);

        newTaskNameInput.setText(prevTaskName);
        newTaskDescriptionInput.setText(prevTaskDescription);
        newTaskDueDateButtonInput.setText(prevDueDate);
        saveButton.setText(R.string.update);

        newTaskDueDateButtonInput.setOnClickListener((v) -> ShowDatePickerDialog(newTaskDueDateButtonInput, false));
        cancelButton.setOnClickListener((v) -> dialog.dismiss());

        saveButton.setOnClickListener((v) -> {
            // Everything is converted to string
            String taskNameStr = newTaskNameInput.getText().toString().trim();
            String taskDescriptionStr = newTaskDescriptionInput.getText().toString().trim();
            String dueDateStr = newTaskDueDateButtonInput.getText().toString().trim();

            // Validate that everything is not empty
            if (taskNameStr.isEmpty()) {
                newTaskNameInput.setError("Task name should not be empty. ");
                return;
            } else if (taskDescriptionStr.isEmpty()) {
                newTaskDescriptionInput.setError("Description should not be empty. ");
                return;
            } else if (dueDateStr.isEmpty()) {
                newTaskDueDateButtonInput.setError("Due date should not be empty. ");
                return;
            }

            Task task = new Task(taskNameStr, taskDescriptionStr, prevTaskID, prevCreationDate, dueDateStr, currGroupID, prevMark, prevTaskAssignedUsers);

            databaseReferenceTask.child(prevTaskID).setValue(task).addOnCompleteListener(task1 -> {
                if (task1.isSuccessful()) {
                    Toast.makeText(this, "The task has been updated.", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(this, "The task has not been updated.", Toast.LENGTH_LONG).show();
                }
                dialog.dismiss();
            });
        });

        dialog.show();
    }

    private void UpdateTaskAssignedUsersActivity(){
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        LayoutInflater layoutInflater = LayoutInflater.from(this);
        View view = layoutInflater.inflate(R.layout.activity_update_assigned_users_menu, null);
        alertDialog.setView(view);

        AlertDialog dialog = alertDialog.create();

        Button viewAssignedUsersButton = view.findViewById(R.id.assignedUsersMenuViewAssignedUsersButton);
        Button assignUserButton = view.findViewById(R.id.assignedUsersMenuAssignUserButton);
        Button removeAssignedUserButton = view.findViewById(R.id.assignedUsersMenuRemoveAssignedUserButton);

        viewAssignedUsersButton.setOnClickListener((v) -> {
            viewAssignedUsersActivity();
            dialog.dismiss();
        });
        assignUserButton.setOnClickListener((v) -> {
            assignUserActivity();
            dialog.dismiss();
        });
        removeAssignedUserButton.setOnClickListener((v) -> {
            removeAssignedUserActivity();
            dialog.dismiss();
        });

        dialog.show();
    }

    private void viewAssignedUsersActivity() {
        // TODO: start an intent to show assigned users in separate activity.
    }

    private void assignUserActivity() {
        // Here we start an activity: autoCompleteUserSearch to GET ITS RESULT:

        Intent intent = new Intent(this, AutoCompleteUserSearch.class);
        intent.putExtra("EXTRA_GROUP_NAME", currGroupName);
        intent.putExtra("EXTRA_GROUP_ID", currGroupID);
        intent.putExtra("EXTRA_SEARCH_REASON", "ASSIGN_USER_TO_TASK");
        intent.putExtra("EXTRA_TASK_NAME", prevTaskName);
        startActivityForResult(intent, ASSIGN_USER_TO_TASK_REQUEST_CODE);
    }

    private void removeAssignedUserActivity() {

        Intent intent = new Intent(this, AutoCompleteUserSearch.class);
        intent.putExtra("EXTRA_GROUP_NAME", currGroupName);
        intent.putExtra("EXTRA_GROUP_ID", currGroupID);
        intent.putExtra("EXTRA_SEARCH_REASON", "REMOVE_ASSIGNED_USER");
        intent.putExtra("EXTRA_TASK_NAME", prevTaskName);
        intent.putExtra("EXTRA_TASK_ID", prevTaskID);
        startActivityForResult(intent, REMOVE_ASSIGNED_USER_REQUEST_CODE);
    }

    /**
     * This method deals with updating the selected tasks mark.
     * It creates an alert dialog, inflates it to the correct layout.
     * Upon pressing `update`, this method will create a new task, find the old task in the database
     * via the prevTaskID, and update its contents to match.
     * A toast message is displayed on success.
     */
    private void UpdateTaskMarkActivity() {
        // TODO: make it clear that clicking on mark as complete, on a marked task, will UNMARK it. (update button).
        Query q = databaseReferenceTask.child(prevTaskID);
        AtomicReference<Boolean> stat = new AtomicReference<>(true);
        Log.e("task_status", String.valueOf(stat.get()));
        q.get().addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                Log.e("CreateTasK_updateMark", "Error getting data", task.getException());
            }
            else {
                Task t = task.getResult().getValue(Task.class);
                stat.set(!(t.isMark()));
                Log.e("task_statusb", String.valueOf(stat.get()));
                Task t2 = new Task(prevTaskName, prevTaskDescription, prevTaskID, prevCreationDate, prevDueDate, currGroupID, stat.get(), prevTaskAssignedUsers);

                databaseReferenceTask.child(prevTaskID).setValue(t2).addOnCompleteListener(task1 -> {
                    if (task1.isSuccessful()) {
                        Toast.makeText(this, "The task has been updated. ", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(this, "The task has not been updated. ", Toast.LENGTH_LONG).show();
                    }
                    Log.e("task_statusc", String.valueOf(stat.get()));
                });
            }
        });
    }

    private void RemoveTaskActivity(){
        AlertDialog.Builder areYouSureDialog = new AlertDialog.Builder(this);
        areYouSureDialog.setTitle("Confirm Delete");
        areYouSureDialog.setMessage("This will permanently delete the task.\nAre you sure?");

        areYouSureDialog.setPositiveButton("DELETE", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Here we remove the task from the database.
                databaseReferenceTask.child(prevTaskID).removeValue().addOnCompleteListener(task1 -> {
                    if (task1.isSuccessful()) {
                        Toast.makeText(AutoCompleteTaskSearch.this, "The task has been deleted.", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(AutoCompleteTaskSearch.this, "The task has not been deleted.", Toast.LENGTH_LONG).show();
                    }
                });
                dialog.dismiss();
            }
        });

        areYouSureDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        AlertDialog dialog = areYouSureDialog.create();
        dialog.show();
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
