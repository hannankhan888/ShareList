package com.example.loginsharelist;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
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
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


public class CreateGroup extends AppCompatActivity {
    private RecyclerView recyclerViewGroup;
    private FloatingActionButton addGroupButton;

    private DatabaseReference databaseReference;
    private FirebaseAuth auth;
    private FirebaseUser user;
    private String onlineUserID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_group);

        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        onlineUserID = user.getUid();
        databaseReference = FirebaseDatabase.getInstance().getReference().child("Group");

        recyclerViewGroup = (RecyclerView) findViewById(R.id.recyclerViewGroup);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        recyclerViewGroup.setHasFixedSize(true);
        recyclerViewGroup.setLayoutManager(linearLayoutManager);

        addGroupButton = (FloatingActionButton) findViewById(R.id.addGroupButton);
        addGroupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addGroupActivity();
            }
        });
    }

    // Add Group Button
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
            String id = databaseReference.push().getKey();

            // Validate everything is not empty
            if (groupNameStr.isEmpty()) {
                groupName.setError("It should not be empty. ");
                return;
            } else {
                Group group = new Group(groupNameStr, id);
                databaseReference.child(databaseReference.push().getKey()).setValue(group).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(CreateGroup.this, "The group has been added. ", Toast.LENGTH_LONG).show();
                            dialog.dismiss();
                        } else {
                            Toast.makeText(CreateGroup.this, "The group has not been added. ", Toast.LENGTH_LONG).show();
                            dialog.dismiss();
                        }
                    }
                });
            }
        });

        Button cancelGroupButton = view.findViewById(R.id.groupCancelButton);
        cancelGroupButton.setOnClickListener((v -> dialog.dismiss()));

        dialog.show();
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<Group> firebaseRecyclerOptions = new FirebaseRecyclerOptions
                .Builder<Group>()
                .setQuery(databaseReference, Group.class)
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
                // It is what is going to display on the group card view
                holder.setGroupName(model.getGroupName());

                // If you click the group, it will open the create task activity
                holder.view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(CreateGroup.this, CreateTask.class);
                        // How do I pass data between Activities in Android application
                        // https://stackoverflow.com/questions/2091465/how-do-i-pass-data-between-activities-in-android-application
                        intent.putExtra("EXTRA_GROUP_NAME", model.getGroupName());
                        startActivity(intent);
//                        startActivity(new Intent(CreateGroup.this, CreateTask.class));
                    }
                });
            }
        };

        // Attach the adapter
        recyclerViewGroup.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.startListening();
    }
}


