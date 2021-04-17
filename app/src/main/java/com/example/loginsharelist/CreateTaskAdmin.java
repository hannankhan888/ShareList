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
import android.renderscript.Sampler;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
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

    private static final int ASSIGN_USER_REQUEST_CODE = 0;
    private static final int REMOVE_ASSIGNED_USER_REQUEST_CODE = 1;
    private static final int ADD_USER_REQUEST_CODE = 2;

    private RecyclerView recyclerViewTask;
    private FloatingActionButton addTaskButton;

    private DatabaseReference databaseReference;
    private DatabaseReference databaseReferenceTask;
    private FirebaseAuth auth;

    private String prevTaskName;
    private String prevTaskDescription;
    private String prevTaskID;
    private String prevCreationDate;
    private String prevDueDate;
    private String prevBelongsToGroupID;
    private boolean prevMark;
    private Map<String, String> prevTaskAssignedUsers;
    private String groupNameStr;
    private String groupIDStr;

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
        databaseReference = FirebaseDatabase.getInstance().getReference();
        databaseReferenceTask = FirebaseDatabase.getInstance().getReference().child("Tasks");

        // Rename app bar to GROUP_NAME - Tasks
        getSupportActionBar().setTitle(groupNameStr + " - Tasks");


        recyclerViewTask = findViewById(R.id.recyclerViewTask);
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
        getMenuInflater().inflate(R.menu.corner_menu_for_create_task_admin, menu);
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


        // its NOT RECOMMENDED to use switch statement according to gradle.
        if (id == R.id.createTaskAdminCornerMenuGroupInfo) {
            // insert code to show group info Activity HERE.
            // TODO: add a group info activity.
            GroupInfoActivity();
            Log.d(TAG, "Group Info option pressed.");
        } else if (id == R.id.createTaskAdminCornerMenuRenameGroup){
            RenameGroupActivity();
            Log.d(TAG, "Rename Group option pressed.");
        }
        else if (id == R.id.createTaskAdminCornerMenuAddUser){
            AddUserActivity();
            Log.d(TAG, "Add User option pressed.");
        } else if (id == R.id.createTaskAdminCornerMenuRemoveUser){
//            RemoveUserActivity();
            Log.d(TAG, "Remove User option pressed.");
        } else if (id == R.id.createTaskAdminCornerMenuAddAdmin){
//            AddAdminActivity();
            // TODO: if user is not a member, then add as both member and a group admin.
            Log.d(TAG, "Add Admin option pressed.");
        } else if (id == R.id.createTaskAdminCornerMenuDeleteGroup){
//            DeleteGroupActivity();
            // TODO: delete all tasks associated with that group.
            Log.d(TAG, "Delete Group option pressed.");
        } else if (id == R.id.createTaskAdminCornerMenuLeaveGroup) {
            // We do the Leave Group stuff here.
            // TODO: add the Leave the Group stuff.
            // TODO: this does NOT mean pressing the back button.
            Log.d(TAG, "Leave Group option pressed.");
        }
        return super.onOptionsItemSelected(item);
    }

    private void GroupInfoActivity() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        LayoutInflater layoutInflater = LayoutInflater.from(this);
        View view = layoutInflater.inflate(R.layout.activity_group_info, null);
        alertDialog.setView(view);

        AlertDialog dialog = alertDialog.create();

        Button taskInfoOKButton = view.findViewById(R.id.groupInfoOKButton);

        taskInfoOKButton.setOnClickListener((v) -> dialog.dismiss());

        // It will receive the number of user in the create group activity and show it in the task info view
        TextView taskUserCount = view.findViewById(R.id.groupUserCount);
        String countUser = getIntent().getStringExtra("EXTRA_MEMBER_COUNT");
        taskUserCount.setText(countUser);

        // It will receive the number of admin in the create group activity and show it in the task info view
        TextView taskAdminCount = view.findViewById(R.id.groupAdminCount);
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
            String id = databaseReferenceTask.push().getKey();
            String creationDate = DateFormat.getDateInstance().format(new Date());
            String dueDateStr = taskDueDate.getText().toString().trim();
            Map<String, String> taskAssignedUsers = new HashMap<>();

            // Validate that everything is not empty
            if (taskNameStr.isEmpty()) {
                taskName.setError("Task name should not be empty. ");
                return;
            } else if (taskDescriptionStr.isEmpty()) {
                taskDescription.setError("Description should not be empty. ");
                return;
            } else if (dueDateStr.isEmpty() | dueDateStr.equals("Due Date")) {
                taskDueDate.setError("Due date should not be empty. ");
                return;
            }

            Task task = new Task(taskNameStr, taskDescriptionStr, id, creationDate, dueDateStr, groupIDStr, false, taskAssignedUsers);
            Log.d(TAG, "groupIDStr is " + groupIDStr);
            databaseReferenceTask.child(id).setValue(task).addOnCompleteListener(task1 -> {
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
                .setQuery(databaseReferenceTask.orderByChild("/taskBelongsToGroupID").equalTo(groupIDStr), Task.class)
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

                Map<String, String> taskAssignedUsers = model.getTaskAssignedUsers();
                if (taskAssignedUsers.size() >= 1){
                    for (String userID : taskAssignedUsers.keySet()) {
                        Query queryToGetUserName = databaseReference.child("User").child(userID).child("userName");
                        queryToGetUserName.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                String userName = snapshot.getValue(String.class);
                                holder.addUserToAssignedUsersStr(userName);
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

                // If you click the task, it will open the task menu
                holder.view.setOnClickListener((view) -> {
                    prevTaskName = model.getTaskName();
                    prevTaskDescription = model.getTaskDescription();
                    prevTaskID = getRef(position).getKey();
                    prevCreationDate = model.getTaskCreationDate();
                    prevDueDate = model.getTaskDueDate();
                    prevBelongsToGroupID = model.getTaskBelongsToGroupID();
                    prevMark = model.isMark();
                    prevTaskAssignedUsers = model.getTaskAssignedUsers();
                    TaskMenuActivity();
                });
            }
        };

        // Attach the adapter
        recyclerViewTask.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.startListening();
    }

    @Override
    protected void onActivityResult (int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // If the activity that finished is assignUser activity:
        if (requestCode == ASSIGN_USER_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                // TODO: check if user is already assigned or if user is not part of group.
                // we get the selectedUserID based on the selectedUserEmail
                String selectedUserEmail = data.getStringExtra("EXTRA_SELECTED_USER_EMAIL");
                String selectedUserID = data.getStringExtra("EXTRA_SELECTED_USER_ID");
                // we add to the prevTaskAssignedUsers map:
                prevTaskAssignedUsers.put(selectedUserID, selectedUserID);
                // we update the task in the database:
                Task task = new Task(prevTaskName, prevTaskDescription, prevTaskID, prevCreationDate,
                        prevDueDate, prevBelongsToGroupID, prevMark, prevTaskAssignedUsers);
                databaseReferenceTask.child(prevTaskID).setValue(task).addOnCompleteListener(task1 -> {
                    if (task1.isSuccessful()) {
                        Toast.makeText(CreateTaskAdmin.this, selectedUserEmail + " has been assigned.", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(CreateTaskAdmin.this, "User not assigned.", Toast.LENGTH_LONG).show();
                    }
                });
            }
        } else if (requestCode == REMOVE_ASSIGNED_USER_REQUEST_CODE) {
            if (resultCode == RESULT_OK){
                // TODO: check if user is already not assigned or if user is not part of group.
                // we get the selectedUserID based on the selectedUserEmail
                String selectedUserEmail = data.getStringExtra("EXTRA_SELECTED_USER_EMAIL");
                String selectedUserID = data.getStringExtra("EXTRA_SELECTED_USER_ID");
                // we add to the prevTaskAssignedUsers map:
                prevTaskAssignedUsers.remove(selectedUserID);
                // we update the task in the database:
                Task task = new Task(prevTaskName, prevTaskDescription, prevTaskID, prevCreationDate,
                        prevDueDate, prevBelongsToGroupID, prevMark, prevTaskAssignedUsers);
                databaseReferenceTask.child(prevTaskID).setValue(task).addOnCompleteListener(task1 -> {
                    if (task1.isSuccessful()) {
                        Toast.makeText(CreateTaskAdmin.this, selectedUserEmail + " has been removed.", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(CreateTaskAdmin.this, "User not removed.", Toast.LENGTH_LONG).show();
                    }
                });
            }
        } else if (requestCode == ADD_USER_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                // TODO: check if user is already not assigned or if user is not part of group.
                // we get the selectedUserID based on the selectedUserEmail
                String selectedUserEmail = data.getStringExtra("EXTRA_SELECTED_USER_EMAIL");
                String selectedUserID = data.getStringExtra("EXTRA_SELECTED_USER_ID");

                databaseReference.child("Groups").child(groupIDStr).child("groupMembers").child(selectedUserID).setValue(selectedUserID).addOnCompleteListener(task1 -> {
                    if (task1.isSuccessful()) {
                        Toast.makeText(CreateTaskAdmin.this, selectedUserEmail + " has been added to group.", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(CreateTaskAdmin.this, "User not added to group.", Toast.LENGTH_LONG).show();
                    }
                });
            }
        }
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

            Task task = new Task(taskNameStr, taskDescriptionStr, prevTaskID, prevCreationDate, dueDateStr, groupIDStr, prevMark, prevTaskAssignedUsers);

            databaseReferenceTask.child(prevTaskID).setValue(task).addOnCompleteListener(task1 -> {
                if (task1.isSuccessful()) {
                    Toast.makeText(CreateTaskAdmin.this, "The task has been updated.", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(CreateTaskAdmin.this, "The task has not been updated.", Toast.LENGTH_LONG).show();
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
        intent.putExtra("EXTRA_GROUP_NAME", groupNameStr);
        intent.putExtra("EXTRA_GROUP_ID", groupIDStr);
        startActivityForResult(intent, ASSIGN_USER_REQUEST_CODE);
    }

    private void removeAssignedUserActivity() {

        Intent intent = new Intent(this, AutoCompleteUserSearch.class);
        intent.putExtra("EXTRA_GROUP_NAME", groupNameStr);
        intent.putExtra("EXTRA_GROUP_ID", groupIDStr);
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
                    Task t2 = new Task(prevTaskName, prevTaskDescription, prevTaskID, prevCreationDate, prevDueDate, groupIDStr, stat.get(), prevTaskAssignedUsers);

                    databaseReferenceTask.child(prevTaskID).setValue(t2).addOnCompleteListener(task1 -> {
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
                        Toast.makeText(CreateTaskAdmin.this, "The task has been deleted.", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(CreateTaskAdmin.this, "The task has not been deleted.", Toast.LENGTH_LONG).show();
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

    private void AddUserActivity(){
        // Here we start an activity: autoCompleteUserSearch to GET ITS RESULT:

        Intent intent = new Intent(this, AutoCompleteUserSearch.class);
        intent.putExtra("EXTRA_GROUP_NAME", groupNameStr);
        intent.putExtra("EXTRA_GROUP_ID", groupIDStr);
        startActivityForResult(intent, ADD_USER_REQUEST_CODE);
    }

    private void RenameGroupActivity() {
        // create a layout activity_rename_group
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        LayoutInflater layoutInflater = LayoutInflater.from(this);
        View view = layoutInflater.inflate(R.layout.activity_update_group, null);
        alertDialog.setView(view);

        // create an alert dialog
        AlertDialog dialog = alertDialog.create();

        // set the alert dialogs layout to activity_rename_group

        EditText newGroupNameInput = view.findViewById(R.id.groupUpdateNameInput);
        Button saveButton = view.findViewById(R.id.groupUpdateButton);

        newGroupNameInput.setText(groupNameStr);
        saveButton.setText(R.string.update);

        // add and onclick listener to the button
        saveButton.setOnClickListener((v) -> {
            // Everything is converted to string
            groupNameStr = newGroupNameInput.getText().toString().trim();

            // Validate that everything is not empty
            if (groupNameStr.isEmpty()) {
                newGroupNameInput.setError("Group name should not be empty. ");
                return;
            }

            databaseReference.child("Groups").child(groupIDStr).setValue(groupIDStr).addOnCompleteListener(task1 -> {
                if (task1.isSuccessful()) {
                    Toast.makeText(CreateTaskAdmin.this, "The group has been updated.", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(CreateTaskAdmin.this, "The group has not been updated.", Toast.LENGTH_LONG).show();
                }
                dialog.dismiss();
            });
        });
        dialog.show();

        // update the database when the button is clicked.
    }
}

// Citation Source
// https://www.youtube.com/watch?v=IVT-XVV4cBk&list=PLlkSO32XQLGpF9HzRulWLpMbU3mWZYlJS&index=2&ab_channel=WilltekSoftwares
