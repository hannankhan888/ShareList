package com.example.loginsharelist;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
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

import java.util.ArrayList;
import java.util.Map;

public class AutoCompleteUserSearch extends AppCompatActivity {
    public static final String TAG = "AutoCompleteUserSearch";

    private DatabaseReference databaseReference;
    private DatabaseReference databaseReferenceUser;
    private RecyclerView autoUserSearchList;
    private EditText autoUserEmailInput;

    private String groupID;
    private String groupName;
    private String searchReason;
    private String taskName;
    private String taskID;

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

        autoUserSearchList = (RecyclerView) findViewById(R.id.autoCompleteUserSearchList);
        autoUserSearchList.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        autoUserSearchList.setHasFixedSize(true);

        autoUserEmailInput = (EditText) findViewById(R.id.autoCompleteUserEmailInput);


        switch (searchReason) {
            case "ASSIGN_USER_TO_TASK": {
                // only searches the current group members.
                taskName = getIntent().getStringExtra("EXTRA_TASK_NAME");
                getSupportActionBar().setTitle("Assign User To: " + taskName);
                autoUserEmailInput.setHint("Enter Username");
                GroupMembersSearch("");
                autoUserEmailInput.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {}

                    @Override
                    public void afterTextChanged(Editable s) {
                        GroupMembersSearch(s.toString().trim());
                    }
                });
                break;
            }
            case "REMOVE_ASSIGNED_USER": {
                // TODO: only search the current assigned users.
                // only searches the current assigned users.
                taskID = getIntent().getStringExtra("EXTRA_TASK_ID");
                taskName = getIntent().getStringExtra("EXTRA_TASK_NAME");
                getSupportActionBar().setTitle("Remove Assigned User: " + taskName);
                autoUserEmailInput.setHint("Enter Username");
                AssignedUsersSearch("");
                autoUserEmailInput.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {}

                    @Override
                    public void afterTextChanged(Editable s) {
                        AssignedUsersSearch(s.toString().trim());
                    }
                });
                break;
            }
            case "ADD_USER":
                getSupportActionBar().setTitle("Add User To " + groupName);
                autoUserEmailInput.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {}

                    @Override
                    public void afterTextChanged(Editable s) {
                        AllUserSearch(s.toString().trim());
                    }
                });
                break;
            case "ADD_ADMIN":
                // only searches the current group members.
                getSupportActionBar().setTitle("Add Admin To " + groupName);
                autoUserEmailInput.setHint("Enter Username");
                GroupMembersSearch("");
                autoUserEmailInput.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {}

                    @Override
                    public void afterTextChanged(Editable s) {
                        GroupMembersSearch(s.toString().trim());
                    }
                });
                break;
            case "REMOVE_ADMIN":
                getSupportActionBar().setTitle("Remove Admin From " + groupName);
                autoUserEmailInput.setHint("Enter Username");
                GroupAdminsSearch("");
                autoUserEmailInput.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {}

                    @Override
                    public void afterTextChanged(Editable s) {
                        GroupAdminsSearch(s.toString().trim());
                    }
                });
                break;
            default:
                getSupportActionBar().setTitle("ShareList - Search All Users");
        }
    }

    private void AllUserSearch(String userEmailStr){
        // This query will search ALL USERS so that the currUser can add a member to their group.
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

    private void GroupMembersSearch(String userNameString) {
//        Query query = databaseReference.child("Groups").orderByChild("/groupMembers/" + currUserID).equalTo(currUserID);
        Query keyQuery = databaseReference.child("Groups").child(groupID).child("groupMembers");

        // This way we get the groupMembers of the currGroup (Map<String, String>) and then we use
        // a keyQuery to parse through the keys of that Map (which are userIDs that are part of this
        // group). The keyQuery will search through database reference (in our case the users reference),
        // and return a User class (goes along with the reference). We can then parse the user class,
        // to see if the username matches the search parameter.
        FirebaseRecyclerOptions firebaseRecyclerOptions = new FirebaseRecyclerOptions
                .Builder<User>()
                .setIndexedQuery(keyQuery, databaseReferenceUser, new SnapshotParser<User>() {
                    @NonNull
                    @Override
                    public User parseSnapshot(@NonNull DataSnapshot snapshot) {
                        User tempUser = snapshot.getValue(User.class);
                        // this way we can search all groups that match or contain the current string.
                        if (tempUser.getUserName().toLowerCase().contains(userNameString.toLowerCase())){
                            return tempUser;
                        } else {
                            return new User();
                        }
                    }
                })
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
                // https://stackoverflow.com/questions/41223413/how-to-hide-an-item-from-recycler-view-on-a-particular-condition
                if (model.getUserName() == null) {
                    holder.itemView.setVisibility(View.GONE);
                    holder.itemView.setLayoutParams(new RecyclerView.LayoutParams(0, 0));

                } else {
                    holder.itemView.setVisibility(View.VISIBLE);
                    holder.itemView.setLayoutParams(new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

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
            }
        };

        // Attach the adapter
        firebaseRecyclerAdapter.startListening();
        autoUserSearchList.setAdapter(firebaseRecyclerAdapter);
    }

    private void AssignedUsersSearch(String userNameString) {
        // This will only search the assigned users of a task.
        Query keyQuery = databaseReference.child("Tasks").child(taskID).child("taskAssignedUsers");

        // This way we get the groupMembers of the currGroup (Map<String, String>) and then we use
        // a keyQuery to parse through the keys of that Map (which are userIDs that are part of this
        // group). The keyQuery will search through database reference (in our case the users reference),
        // and return a User class (goes along with the reference). We can then parse the user class,
        // to see if the username matches the search parameter.
        FirebaseRecyclerOptions firebaseRecyclerOptions = new FirebaseRecyclerOptions
                .Builder<User>()
                .setIndexedQuery(keyQuery, databaseReferenceUser, new SnapshotParser<User>() {
                    @NonNull
                    @Override
                    public User parseSnapshot(@NonNull DataSnapshot snapshot) {
                        User tempUser = snapshot.getValue(User.class);
                        // this way we can search all groups that match or contain the current string.
                        if (tempUser.getUserName().toLowerCase().contains(userNameString.toLowerCase())){
                            return tempUser;
                        } else {
                            return new User();
                        }
                    }
                })
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
                // https://stackoverflow.com/questions/41223413/how-to-hide-an-item-from-recycler-view-on-a-particular-condition
                if (model.getUserName() == null) {
                    holder.itemView.setVisibility(View.GONE);
                    holder.itemView.setLayoutParams(new RecyclerView.LayoutParams(0, 0));

                } else {
                    holder.itemView.setVisibility(View.VISIBLE);
                    holder.itemView.setLayoutParams(new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

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
            }
        };

        // Attach the adapter
        firebaseRecyclerAdapter.startListening();
        autoUserSearchList.setAdapter(firebaseRecyclerAdapter);
    }

    private void GroupAdminsSearch(String userNameString) {
        // This will only search the assigned users of a task.
        Query keyQuery = databaseReference.child("Groups").child(groupID).child("groupAdmins");

        // This way we get the groupAdmins of the currGroup (Map<String, String>) and then we use
        // a keyQuery to parse through the keys of that Map (which are userIDs that are part of this
        // group). The keyQuery will search through database reference (in our case the users reference),
        // and return a User class (goes along with the reference). We can then parse the user class,
        // to see if the username matches the search parameter.
        FirebaseRecyclerOptions firebaseRecyclerOptions = new FirebaseRecyclerOptions
                .Builder<User>()
                .setIndexedQuery(keyQuery, databaseReferenceUser, new SnapshotParser<User>() {
                    @NonNull
                    @Override
                    public User parseSnapshot(@NonNull DataSnapshot snapshot) {
                        User tempUser = snapshot.getValue(User.class);
                        // this way we can search all groups that match or contain the current string.
                        if (tempUser.getUserName().toLowerCase().contains(userNameString.toLowerCase())){
                            return tempUser;
                        } else {
                            return new User();
                        }
                    }
                })
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
                // https://stackoverflow.com/questions/41223413/how-to-hide-an-item-from-recycler-view-on-a-particular-condition
                if (model.getUserName() == null) {
                    holder.itemView.setVisibility(View.GONE);
                    holder.itemView.setLayoutParams(new RecyclerView.LayoutParams(0, 0));

                } else {
                    holder.itemView.setVisibility(View.VISIBLE);
                    holder.itemView.setLayoutParams(new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

                    holder.setUserName(model.getUserName());
                    holder.setUserEmail(model.getEmailAddress());

                    // here we handle what happens when we click on a user to select them.
                    holder.view.setOnClickListener((v) -> {
                        String selectedUserEmail = model.getEmailAddress();
                        String selectedUserID = model.getUserID();
                        String selectedUserName = model.getUserName();
                        Intent intent = new Intent();
                        intent.putExtra("EXTRA_SELECTED_USER_EMAIL", selectedUserEmail);
                        intent.putExtra("EXTRA_SELECTED_USER_ID", selectedUserID);
                        intent.putExtra("EXTRA_SELECTED_USER_NAME", selectedUserName);
                        // we set the result of the intent to be ok, and pass the intent data back to
                        // the previous activity.
                        setResult(RESULT_OK, intent);
                        finish();
                    });
                }
            }
        };

        // Attach the adapter
        firebaseRecyclerAdapter.startListening();
        autoUserSearchList.setAdapter(firebaseRecyclerAdapter);
    }
}
