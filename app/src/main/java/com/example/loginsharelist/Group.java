package com.example.loginsharelist;

import android.util.Log;

import java.util.HashMap;
import java.util.Map;

public class Group {

    private String groupName;
    private String groupId;
    private Map<String, String> groupMembers = new HashMap<>();
    private Map<String, String> groupAdmins = new HashMap<>();

    public Group() {

    }

    public Group(String groupName, String groupId, Map<String, String> groupMembers, Map<String, String> groupAdmins) {
        this.groupName = groupName;
        this.groupId = groupId;
        this.groupMembers = groupMembers;
        this.groupAdmins = groupAdmins;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public Map<String, String> getGroupMembers() {
        return groupMembers;
    }

    public void setGroupMembers(Map<String, String> groupMembers) {
        this.groupMembers = groupMembers;
    }

    public Map<String, String> getGroupAdmins() {
        return groupAdmins;
    }

    public void setGroupAdmins(Map<String, String> groupAdmins) {
        this.groupAdmins = groupAdmins;
    }

    public void addGroupMember(String userID){
        this.groupMembers.put(userID, userID);
    }

    public void removeGroupMember(String userID){
        this.groupMembers.remove(userID);
    }

    public void addGroupAdmin(String userID){
        this.groupAdmins.put(userID, userID);
    }

    public void removeGroupAdmin(String userID){
        this.groupAdmins.remove(userID);
    }

    public void printGroupData(){
        for (String groupMember: this.groupMembers.keySet()) {
            String val = this.groupMembers.get(groupMember);
            Log.d("TAGG",groupMember + ":DUEDATE" + val);
        }
    }
}
