package com.example.loginsharelist;

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
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
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
import java.util.Objects;
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
    private static final String TAG = "CreateTaskAdmin";

    private static final int ASSIGN_USER_TO_TASK_REQUEST_CODE = 0;
    private static final int REMOVE_ASSIGNED_USER_REQUEST_CODE = 1;
    private static final int ADD_USER_REQUEST_CODE = 2;
    private static final int REMOVE_USER_REQUEST_CODE = 3;
    private static final int ADD_ADMIN_REQUEST_CODE = 4;
    private static final int REMOVE_ADMIN_REQUEST_CODE = 5;
    public static boolean status = false;
    private RecyclerView recyclerViewTask;
    private FloatingActionButton addTaskButton;
    private FloatingActionButton searchTaskButton;
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
    private String currUserID;


    /**
     * This method sets all necessary vars for this activity. It also sets the app bar title to
     * reflect to the user what group they are currently in.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_task_admin);

        auth = FirebaseAuth.getInstance();
        currUserID = auth.getUid();
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
        Objects.requireNonNull(getSupportActionBar()).setTitle(groupNameStr + " - Tasks");


        recyclerViewTask = findViewById(R.id.recyclerViewTask);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        recyclerViewTask.setHasFixedSize(true);
        recyclerViewTask.setLayoutManager(linearLayoutManager);

        addTaskButton = findViewById(R.id.addTaskButton);
        // We can use the statement lambda to make the code easier to understand
        addTaskButton.setOnClickListener((view) -> addTaskActivity());

        searchTaskButton = findViewById(R.id.createTaskAdminSearchButton);
        searchTaskButton.setOnClickListener((view) -> searchTaskActivity());
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
            GroupInfoActivity();
            Log.d(TAG, "Group Info option pressed.");
        } else if (id == R.id.createTaskAdminCornerMenuRenameGroup) {
            RenameGroupActivity();
            Log.d(TAG, "Rename Group option pressed.");
        } else if (id == R.id.createTaskAdminCornerMenuAddUser) {
            AddUserActivity();
            Log.d(TAG, "Add User option pressed.");
        } else if (id == R.id.createTaskAdminCornerMenuRemoveUser) {
            RemoveUserActivity();
            Log.d(TAG, "Remove User option pressed.");
        } else if (id == R.id.createTaskAdminCornerMenuAddAdmin) {
            AddAdminActivity();
            Log.d(TAG, "Add Admin option pressed.");
        } else if (id == R.id.createTaskAdminCornerMenuRemoveAdmin) {
            RemoveAdminActivity();
        } else if (id == R.id.createTaskAdminCornerMenuDeleteGroup) {
            // deletes all tasks associated with that group, and the group itself.
            DeleteGroupActivity();
            Log.d(TAG, "Delete Group option pressed.");
        } else if (id == R.id.createTaskAdminCornerMenuLeaveGroup) {
            // We do the Leave Group stuff here.
            // this does NOT mean pressing the back button.
            Log.d(TAG, "Leave Group option pressed.");
            LeaveGroupActivity();
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

        // It will receive the number of admin in the create group activity and show it in the task info view
        TextView taskAdminCount = view.findViewById(R.id.groupAdminCount);

        databaseReference.child("Groups").child(groupIDStr).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Group tempGroup = snapshot.getValue(Group.class);
                taskUserCount.setText(String.valueOf(tempGroup.getGroupMembers().size()));
                taskAdminCount.setText(String.valueOf(tempGroup.getGroupAdmins().size()));

                databaseReferenceTask.orderByChild("/taskBelongsToGroupID").equalTo(groupIDStr).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        int numOfTasks = 0;
                        int numCompletedTasks = 0;

                        for (DataSnapshot task : snapshot.getChildren()) {
                            Task tempTask = task.getValue(Task.class);
                            numOfTasks += 1;
                            if (tempTask.isMark()) {
                                numCompletedTasks += 1;
                            }
                            Log.d("Firebase_groupinfo", String.valueOf(tempTask.getTaskId()));
                        }
                        Log.e("firebase_groupinfo", String.valueOf(numOfTasks));
                        Log.e("firebase_groupcompl", String.valueOf(numCompletedTasks));
                        TextView totalTasks = view.findViewById(R.id.groupTasks);
                        TextView completedTasks = view.findViewById(R.id.completedTasks);
                        TextView remainingTasks = view.findViewById(R.id.remainTasks);
                        totalTasks.setText(String.valueOf(numOfTasks));
                        completedTasks.setText(String.valueOf(numCompletedTasks));
                        remainingTasks.setText(String.valueOf(numOfTasks - numCompletedTasks));
                        dialog.show();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.d("Firebase_groupinfo", "FireGrouperror");
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d(TAG, "Error getting group data in GROUP INFO ACTIVITY.");
            }
        });
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
                View view = LayoutInflater
                        .from(parent.getContext())
                        .inflate(R.layout.activity_display_task_database, parent, false);
                return new TaskDisplay(view);
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
                if (taskAssignedUsers.size() >= 1) {
                    for (String userID : taskAssignedUsers.keySet()) {
                        Query queryToGetUserName = databaseReference.child("User").child(userID).child("userName");
                        queryToGetUserName.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                String userName = snapshot.getValue(String.class);
                                holder.addUserToAssignedUsersStr(userName);
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // If the activity that finished is assignUser activity:
        if (requestCode == ASSIGN_USER_TO_TASK_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                // we get the selectedUserID based on the selectedUserEmail
                String selectedUserEmail = data.getStringExtra("EXTRA_SELECTED_USER_EMAIL");
                String selectedUserID = data.getStringExtra("EXTRA_SELECTED_USER_ID");
                String selectedUserName = data.getStringExtra("EXTRA_SELECTED_USER_NAME");

                databaseReferenceTask.child(prevTaskID).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Task tempTask = snapshot.getValue(Task.class);
                        // we check if the selectedUser is already assigned to the task:
                        if (tempTask.getTaskAssignedUsers().containsKey(selectedUserID)) {
                            Toast.makeText(CreateTaskAdmin.this, selectedUserName + " is already assigned to this task.", Toast.LENGTH_LONG).show();
                        } else {
                            // if the user is not already assigned:
                            // we add to the prevTaskAssignedUsers map:
                            prevTaskAssignedUsers.put(selectedUserID, selectedUserID);
                            // we update the task in the database:
                            Task task = new Task(prevTaskName, prevTaskDescription, prevTaskID, prevCreationDate,
                                    prevDueDate, prevBelongsToGroupID, prevMark, prevTaskAssignedUsers);
                            databaseReferenceTask.child(prevTaskID).setValue(task).addOnCompleteListener(task1 -> {
                                if (task1.isSuccessful()) {
                                    Toast.makeText(CreateTaskAdmin.this, selectedUserName + " has been assigned.", Toast.LENGTH_LONG).show();
                                } else {
                                    Toast.makeText(CreateTaskAdmin.this, "Error assigning user.", Toast.LENGTH_LONG).show();
                                }
                            });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.d(TAG, "Error retrieving task data in ONACTIVITYRESULT ASSIGN USER TO TASK.");
                    }
                });
            }
        } else if (requestCode == REMOVE_ASSIGNED_USER_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                // we get the selectedUserID based on the selectedUserEmail
                String selectedUserEmail = data.getStringExtra("EXTRA_SELECTED_USER_EMAIL");
                String selectedUserID = data.getStringExtra("EXTRA_SELECTED_USER_ID");
                String selectedUserName = data.getStringExtra("EXTRA_SELECTED_USER_NAME");

                // we add to the prevTaskAssignedUsers map:
                prevTaskAssignedUsers.remove(selectedUserID);
                // we update the task in the database:
                Task task = new Task(prevTaskName, prevTaskDescription, prevTaskID, prevCreationDate,
                        prevDueDate, prevBelongsToGroupID, prevMark, prevTaskAssignedUsers);
                databaseReferenceTask.child(prevTaskID).setValue(task).addOnCompleteListener(task1 -> {
                    if (task1.isSuccessful()) {
                        Toast.makeText(CreateTaskAdmin.this, selectedUserName + " has been removed.", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(CreateTaskAdmin.this, "User not removed.", Toast.LENGTH_LONG).show();
                    }
                });
            }
        } else if (requestCode == ADD_USER_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                // Also checks if user is already part of group.
                // we get the selectedUserID based on the selectedUserEmail
                String selectedUserEmail = data.getStringExtra("EXTRA_SELECTED_USER_EMAIL");
                String selectedUserID = data.getStringExtra("EXTRA_SELECTED_USER_ID");
                String selectedUserName = data.getStringExtra("EXTRA_SELECTED_USER_NAME");

                databaseReference.child("Groups").child(groupIDStr).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Group selectedGroup = snapshot.getValue(Group.class);

                        // PLEASE ADD YOUR LOGIC CODE HERE:
                        Map<String, String> selectedGroupMembers = selectedGroup.getGroupMembers();
                        if (selectedGroupMembers.containsKey(selectedUserID)) {
                            Toast.makeText(CreateTaskAdmin.this, selectedUserName + " is already part of " + groupNameStr + ".", Toast.LENGTH_LONG).show();
                        } else {
                            databaseReference.child("Groups").child(groupIDStr).child("groupMembers").child(selectedUserID).setValue(selectedUserID).addOnCompleteListener(task1 -> {
                                if (task1.isSuccessful()) {
                                    Toast.makeText(CreateTaskAdmin.this, selectedUserName + " has been added to group.", Toast.LENGTH_LONG).show();
                                } else {
                                    Toast.makeText(CreateTaskAdmin.this, "Failed to add user to group.", Toast.LENGTH_LONG).show();
                                }
                            });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.d(TAG, "Selected Group " + groupIDStr + " Not retrieving data for ADD_USER");
                    }
                });
            }
        } else if (requestCode == REMOVE_USER_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                // check here if the selected group member to be removed is an admin or not. If
                // so, confirm that the selected user will be removed as BOTH AN ADMIN AND A MEMBER.
                String selectedUserEmail = data.getStringExtra("EXTRA_SELECTED_USER_EMAIL");
                String selectedUserID = data.getStringExtra("EXTRA_SELECTED_USER_ID");
                String selectedUserName = data.getStringExtra("EXTRA_SELECTED_USER_NAME");
                AlertDialog.Builder areYouSureDialog = new AlertDialog.Builder(this);
                areYouSureDialog.setTitle("Confirm Removal");

                databaseReference.child("Groups").child(groupIDStr).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Group selectedGroup = snapshot.getValue(Group.class);

                        // LOGIC CODE HERE:
                        Map<String, String> selectedGroupMembers = selectedGroup.getGroupMembers();
                        Map<String, String> selectedGroupAdmins = selectedGroup.getGroupAdmins();
                        // if user is both an admin and a member:
                        if (selectedGroupMembers.containsKey(selectedUserID) && selectedGroupAdmins.containsKey(selectedUserID)) {
                            // confirm dialog will mention that user is both an admin and a member.
                            // make sure to differentiate between a curr user removing themselves (tell
                            // them to use `Leave Group`), and a curr user removing someone else.
                            if (selectedUserID.equals(currUserID)) {
                                Toast.makeText(CreateTaskAdmin.this, "Please use the Leave Group button instead.", Toast.LENGTH_LONG).show();
                                return;
                            } else {
                                areYouSureDialog.setMessage("\nTHIS WILL REMOVE " + selectedUserName + " AS BOTH ADMIN AND MEMBER.\n\nAre you sure?");
                                areYouSureDialog.setPositiveButton("REMOVE", (dialog, which) -> {
                                    // Here we remove the ADMIN from the database.
                                    databaseReference.child("Groups").child(groupIDStr).child("groupAdmins").child(selectedUserID).removeValue().addOnCompleteListener(task1 -> {
                                        if (task1.isSuccessful()) {
                                            // Here we remove the MEMBER from the database.
                                            databaseReference.child("Groups").child(groupIDStr).child("groupMembers").child(selectedUserID).removeValue().addOnCompleteListener(task -> {
                                                if (task.isSuccessful()) {
                                                    Toast.makeText(CreateTaskAdmin.this, selectedUserName + " has been removed as both admin and member.", Toast.LENGTH_SHORT).show();
                                                } else {
                                                    Toast.makeText(CreateTaskAdmin.this, " Failed to removed member.", Toast.LENGTH_LONG).show();
                                                }
                                            });
                                        } else {
                                            Toast.makeText(CreateTaskAdmin.this, "Failed to remove admin.", Toast.LENGTH_LONG).show();
                                        }
                                    });
                                    dialog.dismiss();
                                });
                            }
                        } else if (selectedGroupMembers.containsKey(selectedUserID) && !(selectedGroupAdmins.containsKey(selectedUserID))) {
                            // here we remove the user as usual. make sure the curr user cannot remove themselves.
                            areYouSureDialog.setMessage("\nTHIS WILL REMOVE " + selectedUserName + " FROM THE GROUP.\n\nAre you sure?");
                            areYouSureDialog.setPositiveButton("REMOVE", (dialog, which) -> databaseReference.child("Groups").child(groupIDStr).child("groupMembers").child(selectedUserID).removeValue().addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    Toast.makeText(CreateTaskAdmin.this, selectedUserName + " has been removed.", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(CreateTaskAdmin.this, "Failed to remove member.", Toast.LENGTH_SHORT).show();
                                }
                            }));
                        }

                        areYouSureDialog.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

                        AlertDialog dialog = areYouSureDialog.create();
                        dialog.show();

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.d(TAG, "Selected Group " + groupIDStr + " Not retrieving data for REMOVE_USER");
                    }
                });
            }
        } else if (requestCode == ADD_ADMIN_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                // we get the selectedUserID based on the selectedUserEmail
                String selectedUserEmail = data.getStringExtra("EXTRA_SELECTED_USER_EMAIL");
                String selectedUserID = data.getStringExtra("EXTRA_SELECTED_USER_ID");
                String selectedUserName = data.getStringExtra("EXTRA_SELECTED_USER_NAME");

                // `any code that needs the data from the database, needs to be inside your onSuccess method or be called from there.`
                // src: https://stackoverflow.com/questions/66698325/how-to-wait-for-firebase-task-to-complete-to-get-result-as-an-await-function

                // THIS SELECTED GROUP WILL HAVE ALL INFO OF YOUR GROUP.
                databaseReference.child("Groups").child(groupIDStr).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Group selectedGroup = snapshot.getValue(Group.class);

                        // PLEASE ADD YOUR LOGIC CODE HERE:
                        Map<String, String> selectedGroupMembers = selectedGroup.getGroupMembers();
                        Map<String, String> selectedGroupAdmins = selectedGroup.getGroupAdmins();

                        // if user is already admin:
                        if (selectedGroupAdmins.containsKey(selectedUserID)) {
                            // if curr user is trying to add themselves as admin AGAIN:
                            if (selectedUserID.equals(currUserID)) {
                                Toast.makeText(CreateTaskAdmin.this, " You are already an admin of this group.", Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(CreateTaskAdmin.this, selectedUserName + " is already an admin of this group.", Toast.LENGTH_LONG).show();
                            }
                        } else {
                            Log.d(TAG, "Selected user is a member but not admin for ADD_ADMIN");
                            // Here we add the user, that is currently a member, to the admins list, and notify the currUser.
                            databaseReference.child("Groups").child(groupIDStr).child("groupAdmins").child(selectedUserID).setValue(selectedUserID).addOnCompleteListener(task1 -> {
                                if (task1.isSuccessful()) {
                                    Toast.makeText(CreateTaskAdmin.this, selectedUserName + " is now a Group Admin.", Toast.LENGTH_LONG).show();
                                } else {
                                    Toast.makeText(CreateTaskAdmin.this, "User not added as Group Admin.", Toast.LENGTH_LONG).show();
                                }
                            });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.d(TAG, "Selected Group " + groupIDStr + " Not retrieving data for ADD_ADMIN");
                    }
                });
            }
        } else if (requestCode == REMOVE_ADMIN_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                // here we create a confirmation dialogue. And remove the admin.
                String selectedUserEmail = data.getStringExtra("EXTRA_SELECTED_USER_EMAIL");
                String selectedUserID = data.getStringExtra("EXTRA_SELECTED_USER_ID");
                String selectedUserName = data.getStringExtra("EXTRA_SELECTED_USER_NAME");

                // now we do confirmation dialogue:
                AlertDialog.Builder areYouSureDialog = new AlertDialog.Builder(this);
                areYouSureDialog.setTitle("Confirm Removal");

                databaseReference.child("Groups").child(groupIDStr).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Group tempGroup = snapshot.getValue(Group.class);

                        if (selectedUserID.equals(currUserID)) {
                            // if the currUser decides to remove themselves as admin:
                            if ((tempGroup.getGroupAdmins().size() == 1) && (tempGroup.getGroupMembers().size() > 1)) {
                                Toast.makeText(CreateTaskAdmin.this, "Please make sure to make someone else admin before leaving.", Toast.LENGTH_LONG).show();
                            } else if ((tempGroup.getGroupAdmins().size() == 1) && (tempGroup.getGroupMembers().size() == 1)) {
                                // if the user is alone in the group:
                                Toast.makeText(CreateTaskAdmin.this, "Please delete the group if you want to leave.", Toast.LENGTH_LONG).show();
                            } else if ((tempGroup.getGroupAdmins().size() > 1) && (tempGroup.getGroupMembers().size() > 1)) {
                                // there are more than 1 admins, in a group with more than 1 members.
                                areYouSureDialog.setMessage("\nThis will remove YOU as admin.\n\nAre you sure?");
                                areYouSureDialog.setPositiveButton("REMOVE", (dialog, which) -> {
                                    // Here we remove the user admin status from the database.
                                    databaseReference.child("Groups").child(groupIDStr).child("groupAdmins").child(selectedUserID).removeValue().addOnCompleteListener(task1 -> {
                                        if (task1.isSuccessful()) {
                                            Toast.makeText(CreateTaskAdmin.this, "You have been removed as admin.", Toast.LENGTH_LONG).show();
                                        } else {
                                            Toast.makeText(CreateTaskAdmin.this, "Failed to remove admin.", Toast.LENGTH_LONG).show();
                                        }
                                    });
                                    dialog.dismiss();
                                    //finish the activity cause currUser is no longer admin.
                                    finish();
                                });
                                areYouSureDialog.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

                                AlertDialog dialog = areYouSureDialog.create();
                                dialog.show();

                            }
                        } else {
                            // if the curr user wants to remove someone else as admin.
                            areYouSureDialog.setMessage("\nThis will remove " + selectedUserName + " as admin.\n\nAre you sure?");
                            areYouSureDialog.setPositiveButton("REMOVE", (dialog, which) -> {
                                // Here we remove the task from the database.
                                databaseReference.child("Groups").child(groupIDStr).child("groupAdmins").child(selectedUserID).removeValue().addOnCompleteListener(task1 -> {
                                    if (task1.isSuccessful()) {
                                        Toast.makeText(CreateTaskAdmin.this, selectedUserName + " has been removed as admin.", Toast.LENGTH_LONG).show();
                                    } else {
                                        Toast.makeText(CreateTaskAdmin.this, "Failed to remove admin.", Toast.LENGTH_LONG).show();
                                    }
                                });
                                dialog.dismiss();
                            });
                            areYouSureDialog.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

                            AlertDialog dialog = areYouSureDialog.create();
                            dialog.show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.d(TAG, "Error getting group data for REMOVE_ADMIN.");
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
        if (prevMark) {
            taskMenuMarkButton.setText(R.string.unmark);
        }
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
    private void UpdateTaskContentsActivity() {
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
                newTaskNameInput.setError("Task name should not be empty.");
                return;
            } else if (taskDescriptionStr.isEmpty()) {
                newTaskDescriptionInput.setError("Description should not be empty.");
                return;
            } else if (dueDateStr.isEmpty()) {
                newTaskDueDateButtonInput.setError("Due date should not be empty.");
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

    private void UpdateTaskAssignedUsersActivity() {
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
        // start an intent to show assigned users in separate activity.
        Intent intent = new Intent(CreateTaskAdmin.this, AutoCompleteUserSearch.class);
        intent.putExtra("EXTRA_GROUP_NAME", groupNameStr);
        intent.putExtra("EXTRA_GROUP_ID", groupIDStr);
        intent.putExtra("EXTRA_SEARCH_REASON", "VIEW_ASSIGNED_USERS");
        intent.putExtra("EXTRA_TASK_NAME", prevTaskName);
        intent.putExtra("EXTRA_TASK_ID", prevTaskID);
        startActivity(intent);
    }

    private void assignUserActivity() {
        // Here we start an activity: autoCompleteUserSearch to GET ITS RESULT:

        Intent intent = new Intent(this, AutoCompleteUserSearch.class);
        intent.putExtra("EXTRA_GROUP_NAME", groupNameStr);
        intent.putExtra("EXTRA_GROUP_ID", groupIDStr);
        intent.putExtra("EXTRA_SEARCH_REASON", "ASSIGN_USER_TO_TASK");
        intent.putExtra("EXTRA_TASK_NAME", prevTaskName);
        startActivityForResult(intent, ASSIGN_USER_TO_TASK_REQUEST_CODE);
    }

    private void removeAssignedUserActivity() {

        Intent intent = new Intent(this, AutoCompleteUserSearch.class);
        intent.putExtra("EXTRA_GROUP_NAME", groupNameStr);
        intent.putExtra("EXTRA_GROUP_ID", groupIDStr);
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
        // we make it clear that clicking on mark as complete, on a marked task, will UNMARK it. (update button).
        Query q = databaseReferenceTask.child(prevTaskID);
        AtomicReference<Boolean> stat = new AtomicReference<>(true);
        Log.e("task_status", String.valueOf(stat.get()));
        q.get().addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                Log.e("CreateTasK_updateMark", "Error getting data", task.getException());
            } else {
                Task t = Objects.requireNonNull(task.getResult()).getValue(Task.class);
                assert t != null;
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

    private void RemoveTaskActivity() {
        AlertDialog.Builder areYouSureDialog = new AlertDialog.Builder(this);
        areYouSureDialog.setTitle("Confirm Delete");
        areYouSureDialog.setMessage("This will permanently delete the task.\nAre you sure?");

        areYouSureDialog.setPositiveButton("DELETE", (dialog, which) -> {
            // Here we remove the task from the database.
            databaseReferenceTask.child(prevTaskID).removeValue().addOnCompleteListener(task1 -> {
                if (task1.isSuccessful()) {
                    Toast.makeText(CreateTaskAdmin.this, "The task has been deleted.", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(CreateTaskAdmin.this, "The task has not been deleted.", Toast.LENGTH_LONG).show();
                }
            });
            dialog.dismiss();
        });

        areYouSureDialog.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

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
                assert date != null;
                calendar.setTime(date);
            } catch (ParseException e) {
                Log.d(TAG, "Date was not able to be parsed: " + prevDueDate);
            }
        }

        // I didn't replace the OnDateSetListener with a lambda because lambda is too confusing.
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    String correct_month = String.valueOf(month + 1);
                    String date = correct_month + "/" + dayOfMonth + "/" + year;
                    taskDueDate.setText(date);
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    private void AddUserActivity() {
        // Here we start an activity: autoCompleteUserSearch to GET ITS RESULT:

        Intent intent = new Intent(this, AutoCompleteUserSearch.class);
        intent.putExtra("EXTRA_GROUP_NAME", groupNameStr);
        intent.putExtra("EXTRA_GROUP_ID", groupIDStr);
        intent.putExtra("EXTRA_SEARCH_REASON", "ADD_USER");
        startActivityForResult(intent, ADD_USER_REQUEST_CODE);
    }

    private void RemoveUserActivity() {
        Intent intent = new Intent(this, AutoCompleteUserSearch.class);
        intent.putExtra("EXTRA_GROUP_NAME", groupNameStr);
        intent.putExtra("EXTRA_GROUP_ID", groupIDStr);
        intent.putExtra("EXTRA_SEARCH_REASON", "REMOVE_USER");
        startActivityForResult(intent, REMOVE_USER_REQUEST_CODE);
    }

    private void AddAdminActivity() {
        // Here we start an activity: autoCompleteUserSearch to GET ITS RESULT:
        System.out.println("Inside the add admin activity");
        Intent intent = new Intent(this, AutoCompleteUserSearch.class);
        intent.putExtra("EXTRA_GROUP_NAME", groupNameStr);
        intent.putExtra("EXTRA_GROUP_ID", groupIDStr);
        intent.putExtra("EXTRA_SEARCH_REASON", "ADD_ADMIN");
        startActivityForResult(intent, ADD_ADMIN_REQUEST_CODE);
    }

    private void RemoveAdminActivity() {
        // Here we start an activity: autoCompleteUserSearch to GET ITS RESULT:
        Intent intent = new Intent(this, AutoCompleteUserSearch.class);
        intent.putExtra("EXTRA_GROUP_NAME", groupNameStr);
        intent.putExtra("EXTRA_GROUP_ID", groupIDStr);
        intent.putExtra("EXTRA_SEARCH_REASON", "REMOVE_ADMIN");
        startActivityForResult(intent, REMOVE_ADMIN_REQUEST_CODE);
    }

    private void DeleteGroupActivity() {
        // we will ask confirmation. Then delete each task in the group, and finally the group itself.
        AlertDialog.Builder areYouSureDialog = new AlertDialog.Builder(this);
        areYouSureDialog.setTitle("Confirm Deletion");
        areYouSureDialog.setMessage("\nTHIS WILL PERMANENTLY DELETE THIS GROUP AND ALL TASKS ASSOCIATED WITH THIS GROUP.\n\nAre you sure?");
        areYouSureDialog.setPositiveButton("DELETE", (dialog, which) -> {
            // first we get and delete all tasks:
            databaseReferenceTask.orderByChild("/taskBelongsToGroupID").equalTo(groupIDStr).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    // we iterate through each task here:
                    for (DataSnapshot task : snapshot.getChildren()) {
                        Task tempTask = task.getValue(Task.class);
                        databaseReferenceTask.child(tempTask.getTaskId()).removeValue().addOnCompleteListener(task1 -> {
                            if (task1.isSuccessful()) {
                                Log.d(TAG, tempTask.getTaskId() + " task deleted.");
                            } else {
                                Log.d(TAG, "Error deleting task: " + tempTask.getTaskId());
                            }
                        });
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.d(TAG, "Error calling by child in DeleteGroupActivity.");
                }
            });

            // next we delete the group itself:
            databaseReference.child("Groups").child(groupIDStr).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull com.google.android.gms.tasks.Task<Void> task) {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "Deleted group successfully.");
                        finish();
                    } else {
                        Log.d(TAG, "Error deleting group: " + groupIDStr);
                    }
                }
            });
        });

        areYouSureDialog.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = areYouSureDialog.create();
        dialog.show();
    }

    private void LeaveGroupActivity() {
        // first we make confirmation dialogue object:
        AlertDialog.Builder areYouSureDialog = new AlertDialog.Builder(this);
        areYouSureDialog.setTitle("Confirm Leave");

        // second we check how many group members there are:
        databaseReference.child("Groups").child(groupIDStr).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Group tempGroup = snapshot.getValue(Group.class);

                if ((tempGroup.getGroupAdmins().size() == 1) && (tempGroup.getGroupMembers().size() > 1)) {
                    // if currUser is the only groupAdmin for this group, and the group has many members.
                    Toast.makeText(CreateTaskAdmin.this, "Please make sure to make someone else admin before leaving.", Toast.LENGTH_LONG).show();
                } else if ((tempGroup.getGroupAdmins().size() == 1) && (tempGroup.getGroupMembers().size() == 1)) {
                    // if currUser is the only member and admin of this group (aka private group).
                    Toast.makeText(CreateTaskAdmin.this, "Please delete the group if you want to leave.", Toast.LENGTH_LONG).show();
                } else if ((tempGroup.getGroupAdmins().size() > 1) && (tempGroup.getGroupMembers().size() > 1)) {
                    // if there are multiple admins in a group of many members.
                    areYouSureDialog.setMessage("\nThis will remove YOU from the group.\n\nAre you sure?");
                    areYouSureDialog.setPositiveButton("LEAVE", (dialog, which) -> {
                        // here we remove the admin from the database.
                        databaseReference.child("Groups").child(groupIDStr).child("groupAdmins").child(currUserID).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull com.google.android.gms.tasks.Task<Void> task) {
                                if (task.isSuccessful()) {
                                    databaseReference.child("Groups").child(groupIDStr).child("groupMembers").child(currUserID).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull com.google.android.gms.tasks.Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                // now we loop through all tasks that belong to the group ID.
                                                // If the user is assigned to any, the currUser will be removed (unassigned).
                                                databaseReferenceTask.orderByChild("/taskBelongsToGroupID").equalTo(groupIDStr).addListenerForSingleValueEvent(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(@NonNull DataSnapshot snapshot1) {
                                                        // Here we iterate through all tasks that belong to the group.
                                                        for (DataSnapshot ds : snapshot1.getChildren()){
                                                            Task tempTask = ds.getValue(Task.class);
                                                            if (tempTask.getTaskAssignedUsers().containsKey(currUserID)) {
                                                                tempTask.getTaskAssignedUsers().remove(currUserID);
                                                                // now we update the database to match the removal.
                                                                databaseReferenceTask.child(tempTask.getTaskId()).setValue(tempTask).addOnCompleteListener(task1 -> {
                                                                    if (task1.isSuccessful()) {
                                                                        Log.d(TAG, "Curr user removed from assignment to task: " + tempTask.getTaskId());
                                                                    } else {
                                                                        Log.d(TAG, "Error removing currUser from assignment to task: " + tempTask.getTaskId());
                                                                    }
                                                                });
                                                            }
                                                        }
                                                        // After the iteration is done, we can exit the activity.
                                                        Toast.makeText(CreateTaskAdmin.this, "You have left the group.", Toast.LENGTH_LONG).show();
                                                        finish();
                                                    }
                                                    @Override
                                                    public void onCancelled(@NonNull DatabaseError error) {
                                                        Log.d(TAG, "Error getting tasks from group in LeaveGroupActivity.");
                                                    }
                                                });
                                            } else {
                                                Log.d(TAG, "Error removing user from groupMembers in LeaveGroupActivity.");
                                            }
                                        }
                                    });
                                } else {
                                    Log.d(TAG, "Error removing admin from group in LeaveGroupActivity.");
                                }
                            }
                        });
                        dialog.dismiss();
                    });

                    areYouSureDialog.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
                    AlertDialog dialog = areYouSureDialog.create();
                    dialog.show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d(TAG, "Error retrieving group data for LeaveGroupActivity.");
            }
        });
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
        newGroupNameInput.setText(groupNameStr);
        Button saveButton = view.findViewById(R.id.groupUpdateButton);

        newGroupNameInput.setText(groupNameStr);

        // add and onclick listener to the button
        // update the database when the button is clicked.
        saveButton.setOnClickListener((v) -> {
            // Everything is converted to string
            groupNameStr = newGroupNameInput.getText().toString().trim();

            // Validate that everything is not empty
            if (groupNameStr.isEmpty()) {
                newGroupNameInput.setError("Group name should not be empty. ");
                return;
            }

            databaseReference.child("Groups").child(groupIDStr).child("groupName").setValue(groupNameStr).addOnCompleteListener(task1 -> {
                if (task1.isSuccessful()) {
                    Toast.makeText(CreateTaskAdmin.this, "The group name has been updated.", Toast.LENGTH_LONG).show();
                    Objects.requireNonNull(getSupportActionBar()).setTitle(groupNameStr + " - Tasks");
                } else {
                    Toast.makeText(CreateTaskAdmin.this, "The group name has not been updated.", Toast.LENGTH_LONG).show();
                }
                dialog.dismiss();
            });
        });

        dialog.show();
    }

    private void searchTaskActivity() {
        Intent intent = new Intent(CreateTaskAdmin.this, AutoCompleteTaskSearch.class);
        intent.putExtra("EXTRA_GROUP_ID", groupIDStr);
        intent.putExtra("EXTRA_GROUP_NAME", groupNameStr);
        intent.putExtra("EXTRA_CURR_USER_IS_ADMIN", true);
        startActivity(intent);
    }
}

// Citation Source
// https://www.youtube.com/watch?v=IVT-XVV4cBk&list=PLlkSO32XQLGpF9HzRulWLpMbU3mWZYlJS&index=2&ab_channel=WilltekSoftwares
