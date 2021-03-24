package com.example.loginsharelist;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

public class AutoCompleteGroupSearch extends AppCompatActivity {
    private DatabaseReference databaseReference;
    private RecyclerView autoGroupSearchList;
    private EditText autoGroupSearchInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auto_complete_group_search);

        databaseReference = FirebaseDatabase.getInstance().getReference().child("Group");

        autoGroupSearchList = (RecyclerView) findViewById(R.id.autoCompleteGroupSearchList);
        autoGroupSearchList.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        autoGroupSearchList.setHasFixedSize(true);

        GroupSearch("");

        autoGroupSearchInput = (EditText) findViewById(R.id.autoCompleteGroupNameSearchInput);
        // Citation Source
        // https://www.youtube.com/watch?v=b_tz8kbFUsU&ab_channel=TVACStudio
        // https://www.youtube.com/watch?v=_nIoEAC3kLg&ab_channel=TechnicalSkillz
        // It shows you how to search data in the firebase realtime database
        // One video teach you how to use auto complete search
        // Another video teach you how to search it manually
        // One video is outdated, so the syntax has changed
        // So you have to change several line of code to make it work
        autoGroupSearchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            // It is waiting for the text to be changed
            // https://developer.android.com/reference/android/text/TextWatcher.html
            // https://stackoverflow.com/questions/26992407/ontextchanged-vs-aftertextchanged-in-android-live-examples-needed
            public void afterTextChanged(Editable s) {
                if (s.toString() != null) {
                    GroupSearch(s.toString());
                } else {
                    GroupSearch("");
                }
            }
        });
    }

    private void GroupSearch(String data) {
        Query query = databaseReference.orderByChild("groupName").startAt(data).endAt(data + "\uf8ff");

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
                    Intent intent = new Intent(AutoCompleteGroupSearch.this, CreateTask.class);
                    // How do I pass data between Activities in Android application
                    // https://stackoverflow.com/questions/2091465/how-do-i-pass-data-between-activities-in-android-application
                    // How to use putExtra() and getExtra() for string data
                    // https://stackoverflow.com/questions/5265913/how-to-use-putextra-and-getextra-for-string-data
                    // It is the name of group that you click
                    String groupNameStr = model.getGroupName();
                    intent.putExtra("EXTRA_GROUP_NAME", groupNameStr);
                    startActivity(intent);
                });
            }
        };

        // Attach the adapter
        firebaseRecyclerAdapter.startListening();
        autoGroupSearchList.setAdapter(firebaseRecyclerAdapter);

    }
}









































