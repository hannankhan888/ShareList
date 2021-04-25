package com.example.loginsharelist;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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
public class CreateTaskUser extends AppCompatActivity {
    private static final String TAG = "CreateTask";

    private RecyclerView recyclerViewTask;

    private DatabaseReference databaseReference;
    private DatabaseReference databaseReferenceTask;
    private FloatingActionButton searchTaskButton;
    private FirebaseAuth auth;

    private String prevTaskName;
    private String prevTaskDescription;
    private String prevTaskID;
    private String prevCreationDate;
    private String prevDueDate;
    private boolean prevMark;
    private String prevTaskBelongsToGroupID;
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
        setContentView(R.layout.activity_create_task_user);

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

        searchTaskButton = findViewById(R.id.createTaskUserSearchButton);
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
        getMenuInflater().inflate(R.menu.corner_menu_for_create_task_user, menu);
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
            // code to show group info Activity HERE.
            Log.d(TAG, "Group Info option pressed.");
            GroupInfoActivity();
        } else if (id == R.id.createTaskCornerMenuLeaveGroupItem) {
            // We do the Leave Group stuff here.
            Log.d(TAG, "Leave Group option pressed.");
            LeaveGroupActivity();
        }
        return super.onOptionsItemSelected(item);
    }

    private void LeaveGroupActivity() {
        // TODO: remove user from all assigned tasks first.
        // first we make confirmation dialogue object:
        AlertDialog.Builder areYouSureDialog = new AlertDialog.Builder(this);
        areYouSureDialog.setTitle("Confirm Leave");

        areYouSureDialog.setMessage("This will remove YOU from the group.\n\nAre you sure?");
        // since the currUser is a groupMember only, and NOT an admin, we can just remove them from group.
        areYouSureDialog.setPositiveButton("LEAVE", (dialog, which) -> databaseReference.child("Groups").child(groupIDStr).child("groupMembers").child(currUserID).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull com.google.android.gms.tasks.Task<Void> task) {
                if (task.isSuccessful()) {
                    // here we iterate through all tasks of the group
                    // if the currUser is assigned to any task, it will remove (un-assign the user).
                    databaseReferenceTask.orderByChild("/taskBelongsToGroupID").equalTo(groupIDStr).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            for (DataSnapshot ds : snapshot.getChildren()) {
                                Task tempTask = ds.getValue(Task.class);
                                if (tempTask.getTaskAssignedUsers().containsKey(currUserID)) {
                                    // here we update the database to un-assign the currUser.
                                    tempTask.getTaskAssignedUsers().remove(currUserID);
                                    databaseReferenceTask.child(tempTask.getTaskId()).setValue(tempTask).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull com.google.android.gms.tasks.Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                Log.d(TAG, "Curr user removed from assignment to task: " + tempTask.getTaskId());
                                            } else {
                                                Log.d(TAG, "Error removing currUser from assignment to task: " + tempTask.getTaskId());
                                            }
                                        }
                                    });
                                }
                            }
                            // here we notify the user they have left the group.
                            Toast.makeText(CreateTaskUser.this, "You have left the group.", Toast.LENGTH_LONG).show();
                            finish();
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Log.d(TAG, "Error retrieving tasks data in LEAVE GROUP.");
                        }
                    });
                } else {
                    Toast.makeText(CreateTaskUser.this, "Error leaving the group.", Toast.LENGTH_LONG).show();
                }
            }
        }));

        areYouSureDialog.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = areYouSureDialog.create();
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
                    prevMark = model.isMark();
                    prevTaskBelongsToGroupID = model.getTaskBelongsToGroupID();
                    prevTaskAssignedUsers = model.getTaskAssignedUsers();
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
        View view = layoutInflater.inflate(R.layout.activity_task_menu_user, null);
        alertDialog.setView(view);

        AlertDialog dialog = alertDialog.create();

        Button taskMenuUserMarkButton = view.findViewById(R.id.taskMenuUserMarkButton);
        if (prevMark) {
            taskMenuUserMarkButton.setText(R.string.unmark);
        }
        // We can use the statement lambda to make the code easier to understand
        taskMenuUserMarkButton.setOnClickListener((v) -> {
            UpdateTaskMarkActivity();
            dialog.dismiss();
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
        Query q = databaseReferenceTask.child(prevTaskID);
        AtomicReference<Boolean> stat = new AtomicReference<>(true);
        Log.e("task_statusa", String.valueOf(stat.get()));
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
                        Toast.makeText(CreateTaskUser.this, "The task has been updated. ", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(CreateTaskUser.this, "The task has not been updated. ", Toast.LENGTH_LONG).show();
                    }
                    Log.e("task_statusc", String.valueOf(stat.get()));
                });
            }
        });
    }

    private void searchTaskActivity() {
        Intent intent = new Intent(CreateTaskUser.this, AutoCompleteTaskSearch.class);
        intent.putExtra("EXTRA_GROUP_ID", groupIDStr);
        intent.putExtra("EXTRA_GROUP_NAME", groupNameStr);
        intent.putExtra("EXTRA_CURR_USER_IS_ADMIN", false);
        startActivity(intent);
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
        Query tasksQuery = databaseReferenceTask.orderByChild("/taskBelongsToGroupID").equalTo(groupIDStr);
        tasksQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int numOfTasks = 0;
                int numCompletedTasks = 0;

                for (DataSnapshot task : snapshot.getChildren()) {
                    Task tempTask = task.getValue(Task.class);
                    numOfTasks += 1;
                    assert tempTask != null;
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
}
// Citation Source
// https://www.youtube.com/watch?v=IVT-XVV4cBk&list=PLlkSO32XQLGpF9HzRulWLpMbU3mWZYlJS&index=2&ab_channel=WilltekSoftwares
