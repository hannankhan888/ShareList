package com.example.loginsharelist;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import java.util.HashMap;
import java.util.Map;

/**
 * This class implements the CreateGroup activity. This activity is available to ALL USERS, and is
 * the first thing they see after signing in. It has options to create group, search for group.
 *
 * Its corner menu implements options such as account info, logout.
 * */
public class CreateGroup extends AppCompatActivity {
    private static final String TAG = "CreateGroup";

    private RecyclerView recyclerViewGroup;
    private FloatingActionButton addGroupButton;
    private FloatingActionButton groupSearchButton;

    private DatabaseReference databaseReference;
    private FirebaseAuth auth;
    String currUserID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_group);
        getSupportActionBar().setTitle("Your Groups");

        auth = FirebaseAuth.getInstance();
        currUserID = auth.getUid();
        databaseReference = FirebaseDatabase.getInstance().getReference().child("Groups");

        recyclerViewGroup = (RecyclerView) findViewById(R.id.recyclerViewGroup);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        recyclerViewGroup.setHasFixedSize(true);
        recyclerViewGroup.setLayoutManager(linearLayoutManager);

        addGroupButton = (FloatingActionButton) findViewById(R.id.addGroupButton);
        addGroupButton.setOnClickListener((view) -> addGroupActivity());

        groupSearchButton = (FloatingActionButton) findViewById(R.id.groupSearchButton);
        groupSearchButton.setOnClickListener((view) -> groupSearchActivity());
    }

    /**
     * Adds the corner menu layout for create group.
     * The buttons get created and checked for in onOptionsItemSelected(MenuItem item)
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.corner_menu_for_create_group, menu);
        return super.onCreateOptionsMenu(menu);
    }

    /** Here we handle what happens when a corner menu item gets pressed.
     *
     * @param item - item that is selected.
     * @return boolean
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        int id = item.getItemId();

        // its not recommended to use switch statement according to gradle.
        if (id == R.id.createGroupMenuAccountItem) {
            // insert code to show User Account Description Activity HERE.
            // TODO: add a user account description activity.
            Log.d(TAG, "Account option pressed.");
        } else if (id == R.id.createGroupMenuLogoutItem){
            // We do the logout stuff here.
            // TODO: add the logout stuff. Make sure to sign out using Firebase specifically,
            // TODO: and return to the login activity. (make sure the password field is empty then).
            Log.d(TAG, "Logout option pressed.");
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Creates an alert dialog to get information about what group to create. This method will then
     * create the group and set the group admin as the current user. It will also add the current
     * user to the group members hash map.
     *
     * All of this info gets updated in the database, with a toast message signaling success.
     */
    private void addGroupActivity() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        LayoutInflater layoutInflater = LayoutInflater.from(this);

        View view = layoutInflater.inflate(R.layout.activity_input_group_detail, null);
        alertDialog.setView(view);

        AlertDialog dialog = alertDialog.create();
        dialog.setCancelable(false);

        EditText groupName = view.findViewById(R.id.addGroupName);
        Button groupSaveButton = view.findViewById(R.id.groupSaveButton);
        groupSaveButton.setOnClickListener((v) -> {
            // Everything is converted to string
            String groupNameStr = groupName.getText().toString().trim();
            // The id generated here is an ID for the group we will create.
            String id = databaseReference.push().getKey();
            Map<String, String> groupMembers = new HashMap<>();
            Map<String, String> groupAdmins = new HashMap<>();


            // Validate everything is not empty
            if (groupNameStr.isEmpty()) {
                groupName.setError("Group name cannot be empty. ");
                return;
            } else {
                Group group = new Group(groupNameStr, id, groupMembers, groupAdmins);
                group.addGroupMember(currUserID);
                group.addGroupAdmin(currUserID);
//                src: https://stackoverflow.com/questions/39815117/add-an-item-to-a-list-in-firebase-database
//                I have started re-iterating to myself: whenever you find yourself doing
//                array.contains("xyz"), you should probably be using a set instead of an array.
//                The above mapping with "key": true is an implementation of a set on Firebase.

                databaseReference.child(id).setValue(group).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(CreateGroup.this, "The group has been added. ", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(CreateGroup.this, "The group has not been added. ", Toast.LENGTH_LONG).show();
                    }
                    dialog.dismiss();
                });
            }
        });

        Button cancelGroupButton = view.findViewById(R.id.groupCancelButton);
        cancelGroupButton.setOnClickListener((v -> dialog.dismiss()));

        dialog.show();
    }

    /**
     * Starts the group search activity.
     */
    private void groupSearchActivity() {
        startActivity(new Intent(CreateGroup.this, GroupSearch.class));
    }


    /**
     * This method creates a query based on the current userID. This gets the groups that the user
     * is part of. These groups are displayed via a FirebaseRecycler and Adapter.
     */
    @Override
    protected void onStart() {
        super.onStart();

        // NOT ALLOWED: using multiple `orderBy` statements in the same line.
        Log.d(TAG, currUserID);
        Query query = databaseReference.orderByChild("/groupMembers/" + currUserID).equalTo(currUserID);

        FirebaseRecyclerOptions<Group> firebaseRecyclerOptions = new FirebaseRecyclerOptions
                .Builder<Group>()
                .setQuery(query, Group.class)
                .build();

        FirebaseRecyclerAdapter<Group, GroupDisplay> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Group, GroupDisplay>(firebaseRecyclerOptions) {
            @NonNull
            @Override
            public GroupDisplay onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                // Inflate the group card view
                View view = LayoutInflater
                        .from(parent.getContext())
                        .inflate(R.layout.activity_display_group_database, parent, false);
                return new GroupDisplay(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull GroupDisplay holder, int position, @NonNull Group model) {
                model.printGroupData();
                // It is what is going to display on the group card view
                holder.setGroupName(model.getGroupName());

                // If you click the group, it will open the create task activity
                holder.view.setOnClickListener((view) -> {
                    Intent intent = new Intent(CreateGroup.this, CreateTask.class);
                    // How do I pass data between Activities in Android application
                    // https://stackoverflow.com/questions/2091465/how-do-i-pass-data-between-activities-in-android-application
                    // How to use putExtra() and getExtra() for string data
                    // https://stackoverflow.com/questions/5265913/how-to-use-putextra-and-getextra-for-string-data
                    // It is the name of group that you click
                    String groupNameStr = model.getGroupName();
                    intent.putExtra("EXTRA_GROUP_NAME", groupNameStr);
                    intent.putExtra("EXTRA_GROUP_ID", model.getGroupId());
                    startActivity(intent);
                });
            }
        };

        // Attach the adapter
        recyclerViewGroup.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.startListening();
    }
}
