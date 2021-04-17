package com.example.loginsharelist;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.firebase.ui.database.SnapshotParser;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

public class AutoCompleteUserSearch extends AppCompatActivity {
    public static final String TAG = "AutoCompleteUserSearch";

    private DatabaseReference databaseReference;
    private DatabaseReference databaseReferenceUser;
    private RecyclerView autoUserSearchList;
    private EditText autoUserEmailInput;

    private String groupID;
    private String groupName;
    private String searchReason;

    FirebaseRecyclerAdapter<User, UserDisplay> firebaseRecyclerAdapter;

    private FirebaseAuth auth;
    String currUserID;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auto_complete_user_search);

        auth = FirebaseAuth.getInstance();
        currUserID = auth.getUid();
        databaseReference = FirebaseDatabase.getInstance().getReference();
        databaseReferenceUser = FirebaseDatabase.getInstance().getReference().child("User");
        groupID = getIntent().getStringExtra("EXTRA_GROUP_ID");
        groupName = getIntent().getStringExtra("EXTRA_GROUP_NAME");
        searchReason = getIntent().getStringExtra("EXTRA_SEARCH_REASON");

        switch (searchReason) {
            case "ASSIGN_USER": {
                String taskName = getIntent().getStringExtra("EXTRA_TASK_NAME");
                getSupportActionBar().setTitle("Assign User To: " + taskName);
                break;
            }
            case "REMOVE_ASSIGNED_USER": {
                String taskName = getIntent().getStringExtra("EXTRA_TASK_NAME");
                getSupportActionBar().setTitle("Remove Assigned User: " + taskName);
                break;
            }
            case "ADD_USER":
                getSupportActionBar().setTitle("Add User To " + groupName);
                break;
            case "ADD_ADMIN":
                getSupportActionBar().setTitle("Add Admin To " + groupName);
                break;
            default:
                getSupportActionBar().setTitle("ShareList - Search Users");
        }

        autoUserSearchList = (RecyclerView) findViewById(R.id.autoCompleteUserSearchList);
        autoUserSearchList.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        autoUserSearchList.setHasFixedSize(true);

        autoUserEmailInput = (EditText) findViewById(R.id.autoCompleteUserEmailInput);
        autoUserEmailInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.toString() != null){
                    UserSearch(s.toString().trim());
                } else {
                    UserSearch(null);
                }
            }
        });
    }

    private void UserSearch(String userEmailStr){
        // TODO: if we search all email addresses, it becomes a security concern.
//        query = databaseReferenceUser.orderByChild("/emailAddress").startAt(userEmailStr).endAt(userEmailStr + "\uf8ff");
        // here we get the email address search.
        Query query = databaseReferenceUser.orderByChild("/emailAddress").equalTo(userEmailStr);

        FirebaseRecyclerOptions firebaseRecyclerOptions = new FirebaseRecyclerOptions
                .Builder<User>()
                .setQuery(query, User.class)
                .build();

        firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<User, UserDisplay>(firebaseRecyclerOptions) {
            @NonNull
            @Override
            public UserDisplay onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                // Inflate the user card view
                View view = LayoutInflater
                        .from(parent.getContext())
                        .inflate(R.layout.activity_display_user_database, parent, false);
                return new UserDisplay(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull UserDisplay holder, int position, @NonNull User model) {
                holder.setUserName(model.getUserName());
                holder.setUserEmail(model.getEmailAddress());

                // here we handle what happens when we click on a user to select them.
                holder.view.setOnClickListener((v) -> {
                    String selectedUserEmail = model.getEmailAddress();
                    String selectedUserID = model.getUserID();
                    Intent intent = new Intent();
                    intent.putExtra("EXTRA_SELECTED_USER_EMAIL", selectedUserEmail);
                    intent.putExtra("EXTRA_SELECTED_USER_ID", selectedUserID);
                    // we set the result of the intent to be ok, and pass the intent data back to
                    // the previous activity.
                    setResult(RESULT_OK, intent);
                    finish();
                });
            }
        };

        // Attach the adapter
        firebaseRecyclerAdapter.startListening();
        autoUserSearchList.setAdapter(firebaseRecyclerAdapter);
    }
}
