package com.example.loginsharelist;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

/**
 * This class implements a search activity for searching for groups.
 * TODO: update so that it only searches for groups that the user is currently part of.
 */
public class GroupSearch extends AppCompatActivity {
    private EditText groupNameSearchInput;
    private Button groupNameSearchButton;
    private RecyclerView recyclerView;
    private DatabaseReference databaseReference;

    private FirebaseAuth auth;
    String currUserID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_search);

        auth = FirebaseAuth.getInstance();
        currUserID = auth.getUid();

        databaseReference = FirebaseDatabase.getInstance().getReference().child("Groups");

        recyclerView = (RecyclerView) findViewById(R.id.autoCompletegroupSearchList);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        groupNameSearchInput = (EditText) findViewById(R.id.groupNameSearchInput);
        groupNameSearchButton = (Button) findViewById(R.id.groupNameSearchButton);
        // Citation Source
        // https://www.youtube.com/watch?v=b_tz8kbFUsU&ab_channel=TVACStudio
        // https://www.youtube.com/watch?v=_nIoEAC3kLg&ab_channel=TechnicalSkillz
        // It shows you how to search data in the firebase realtime database
        // One video teach you how to use auto complete search
        // Another video teach you how to search it manually
        // One video is outdated, so the syntax has changed.
        // So you have to change several line of code to make it work
        groupNameSearchButton.setOnClickListener((view) -> {
            String groupNameStr = groupNameSearchInput.getText().toString().trim();
            GroupSearchActivity(groupNameStr);
        });
    }

    /**
     * This method implements the actual group search. It does this by using a query, that searches
     * for the group name (data). It also has the option to start the particular CreateTask activity
     * for the group selected.
     * */
    private void GroupSearchActivity(String groupNameStr) {
        // TODO: update this to only search the users current groups.
        Query query = databaseReference.orderByChild("groupName").startAt(groupNameStr).endAt(groupNameStr + "\uf8ff");
//        groupNameStr = groupNameStr.trim();

//        Query query = databaseReference.orderByChild("/groupMembers/" + currUserID).equalTo(currUserID);

        FirebaseRecyclerOptions firebaseRecyclerOptions = new FirebaseRecyclerOptions
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
                holder.setGroupName(model.getGroupName());

                // If you click the group, it will open the create task activity
                holder.view.setOnClickListener((view) -> {
                    Intent intent = new Intent(GroupSearch.this, CreateTask.class);
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
        firebaseRecyclerAdapter.startListening();
        recyclerView.setAdapter(firebaseRecyclerAdapter);

    }
}




























