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
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

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
public class CreateTaskUser extends AppCompatActivity {
    private static final String TAG = "CreateTask";

    private RecyclerView recyclerViewTask;

    private DatabaseReference databaseReference;
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

    public static boolean status = false;


    /**
     * This method sets all necessary vars for this activity. It also sets the app bar title to
     * reflect to the user what group they are currently in.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_task_user);

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

        searchTaskButton = (FloatingActionButton) findViewById(R.id.createTaskUserSearchButton);
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
            // insert code to show group info Activity HERE.
            // TODO: add a group info activity.
            Log.d(TAG, "Group Info option pressed.");
        } else if (id == R.id.createTaskCornerMenuLeaveGroupItem) {
            // We do the Leave Group stuff here.
            // TODO: add the Leave Group stuff.

            Log.d(TAG, "Leave Group option pressed.");
            leaveGroupDialog();
//            item.setOnMenuItemClickListener((v) -> {
//
//                startActivity(new Intent(CreateTaskUser.this, CreateGroup.class));
//                finish();
//                return true;
//            });
        }
        return super.onOptionsItemSelected(item);
    }

    private void leaveGroupDialog() {
        Log.d(TAG, "Inside Leave alert.");
        Log.d(TAG, auth.getUid() + " left Group: "+ groupNameStr);

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        LayoutInflater layoutInflater = LayoutInflater.from(this);
        View view = layoutInflater.inflate(R.layout.dialog_leave_group, null);
        alertDialog.setView(view);

        AlertDialog dialog = alertDialog.create();

        Button leaveGroupOkButton = view.findViewById(R.id.leaveGroupOKButton);
        Button leaveGroupCancelButton = view.findViewById(R.id.leaveGroupCancelButton);
        TextView groupName = view.findViewById(R.id.groupName);
        groupName.setText(groupNameStr);
        leaveGroupCancelButton.setOnClickListener((v) -> dialog.dismiss());
        leaveGroupOkButton.setOnClickListener(view1 -> {

//            databaseReferenceGroup.child(id).setValue(group).addOnCompleteListener(task -> {
//                if (task.isSuccessful()) {
//                    Toast.makeText(CreateGroup.this, "The group has been added. ", Toast.LENGTH_LONG).show();
//                } else {
//                    Toast.makeText(CreateGroup.this, "The group has not been added. ", Toast.LENGTH_LONG).show();
//                }
//                dialog.dismiss();
//            });

            Log.d(TAG, auth.getUid() + " left Group " + groupNameStr);
            dialog.dismiss();
            startActivity(new Intent(this, CreateGroup.class));
            finish();
        });

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
        Query q = databaseReference.child(prevTaskID);
        AtomicReference<Boolean> stat = new AtomicReference<>(true);
        Log.e("task_statusa", String.valueOf(stat.get()));
        q.get().addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                Log.e("CreateTasK_updateMark", "Error getting data", task.getException());
            } else {
                Task t = task.getResult().getValue(Task.class);
                stat.set(!(t.isMark()));
                Log.e("task_statusb", String.valueOf(stat.get()));
                Task t2 = new Task(prevTaskName, prevTaskDescription, prevTaskID, prevCreationDate, prevDueDate, groupIDStr, stat.get(), prevTaskAssignedUsers);

                databaseReference.child(prevTaskID).setValue(t2).addOnCompleteListener(task1 -> {
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
}
// Citation Source
// https://www.youtube.com/watch?v=IVT-XVV4cBk&list=PLlkSO32XQLGpF9HzRulWLpMbU3mWZYlJS&index=2&ab_channel=WilltekSoftwares
