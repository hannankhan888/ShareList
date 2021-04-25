package com.example.loginsharelist;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.firebase.ui.database.SnapshotParser;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * This class implements an autocomplete search for groups.*/
public class AutoCompleteGroupSearch extends AppCompatActivity {
    public static final String TAG = "AutoCompleteGroupSearch";

    private DatabaseReference databaseReference;
    private DatabaseReference databaseReferenceGroup;
    private RecyclerView autoGroupSearchList;
    private EditText autoGroupSearchInput;
    private FloatingActionButton autoCompleteCreateGroupButton;

    FirebaseRecyclerAdapter<Group, GroupDisplay> firebaseRecyclerAdapter;
    private FirebaseAuth auth;
    String currUserID;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auto_complete_group_search);

        Objects.requireNonNull(getSupportActionBar()).setTitle("Search Your Groups");

        auth = FirebaseAuth.getInstance();
        currUserID = auth.getUid();
        databaseReference = FirebaseDatabase.getInstance().getReference();
        databaseReferenceGroup = FirebaseDatabase.getInstance().getReference().child("Groups");

        autoGroupSearchList = findViewById(R.id.autoCompleteGroupSearchList);
        autoGroupSearchList.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        autoGroupSearchList.setHasFixedSize(true);

        autoCompleteCreateGroupButton = findViewById(R.id.autoCompleteCreateGroupButton);
        autoCompleteCreateGroupButton.setOnClickListener((view) -> addGroupActivity());

        autoGroupSearchInput = findViewById(R.id.autoCompleteGroupNameSearchInput);
        // Citation Source
        // https://www.youtube.com/watch?v=b_tz8kbFUsU&ab_channel=TVACStudio
        // https://www.youtube.com/watch?v=_nIoEAC3kLg&ab_channel=TechnicalSkillz
        // It shows you how to search data in the firebase realtime database
        // One video teach you how to use auto complete search
        // Another video teach you how to search it manually
        // One video is outdated, so the syntax has changed
        // So you have to change several line of code to make it work
        GroupSearch("");
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
                GroupSearch(s.toString().toLowerCase().trim());
            }
        });
    }

    /**
     * This method implements the addGroupActivity as described in CreateGroup.java.
     * */
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
            String id = databaseReferenceGroup.push().getKey();
            Map<String, String> groupMembers = new HashMap<>();
            Map<String, String> groupAdmins = new HashMap<>();

            // Validate everything is not empty
            if (groupNameStr.isEmpty()) {
                groupName.setError("Group name cannot be empty. ");
            } else {
                Group group = new Group(groupNameStr, id, groupMembers, groupAdmins);
                databaseReferenceGroup.child(databaseReferenceGroup.push().getKey()).setValue(group).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(AutoCompleteGroupSearch.this, "The group has been added. ", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(AutoCompleteGroupSearch.this, "The group has not been added. ", Toast.LENGTH_LONG).show();
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
     * This method implements the actual group search. It does this by using a query, that searches
     * for the group name (data). It also has the option to start the particular CreateTask activity
     * for the group selected.
     * */
    private void GroupSearch(String groupNameStr) {
//        Query query = databaseReference.orderByChild("groupName").startAt(groupNameStr).endAt(groupNameStr + "\uf8ff");
        Query query = databaseReferenceGroup.orderByChild("/groupMembers/" + currUserID).equalTo(currUserID);

        //https://stackoverflow.com/questions/52041870/does-using-snapshotparser-while-querying-firestore-an-expensive-operation
        FirebaseRecyclerOptions firebaseRecyclerOptions = new FirebaseRecyclerOptions
                .Builder<Group>()
                .setQuery(query, snapshot -> {
                    Group tempGroup = snapshot.getValue(Group.class);
                    // this way we can search all groups that match or contain the current string.
                    if (tempGroup.getGroupName().toLowerCase().contains(groupNameStr)){
                        return tempGroup;
                    } else {
                        return new Group();
                    }
                })
                .build();

        firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Group, GroupDisplay>(firebaseRecyclerOptions) {
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
                // https://stackoverflow.com/questions/41223413/how-to-hide-an-item-from-recycler-view-on-a-particular-condition
                if (model.getGroupId() == null) {
                    holder.itemView.setVisibility(View.GONE);
                    holder.itemView.setLayoutParams(new RecyclerView.LayoutParams(0, 0));
                } else {
                    holder.itemView.setVisibility(View.VISIBLE);
                    holder.itemView.setLayoutParams(new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

                    holder.setGroupSearchName(model.getGroupName(), groupNameStr);
                    Map<String, String> groupMembersMap = model.getGroupMembers();
                    if (groupMembersMap.size() > 1){
                        for (String userID : groupMembersMap.keySet()){
                            Query queryToGetMemberName = databaseReference.child("User").child(userID).child("userName");
                            queryToGetMemberName.addListenerForSingleValueEvent(new ValueEventListener(){
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot){
                                    String memberName = dataSnapshot.getValue(String.class);
                                    holder.addMemberToGroupMembersStr(memberName);
                                    Log.d(TAG, "Member Name: " + memberName);
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                    Log.d(TAG, "Unable to get groupMemberName: " + error);
                                }
                            });
                        }
                    } else {
                        holder.setGroupMembersStr("Only You");
                    }

                    // If you click the group, it will open the create task activity
                    holder.view.setOnClickListener((view) -> {
                        Intent intent = new Intent(AutoCompleteGroupSearch.this, CreateTaskAdmin.class);
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
            }
        };

        // Attach the adapter
        firebaseRecyclerAdapter.startListening();
        autoGroupSearchList.setAdapter(firebaseRecyclerAdapter);

    }
}
