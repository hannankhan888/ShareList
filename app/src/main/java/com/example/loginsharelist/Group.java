package com.example.loginsharelist;

import java.util.ArrayList;

public class Group {
    private String groupName;
    private String groupId;
    private ArrayList<String> groupMembers = new ArrayList<>();
    private ArrayList<String> groupAdmins = new ArrayList<>();

    public Group() {

    }

    public Group(String groupName, String groupId) {
        this.groupName = groupName;
        this.groupId = groupId;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public void addGroupMember(String userID){
        this.groupMembers.add(userID);
    }

    public ArrayList<String> getGroupMembers(){
        return this.groupMembers;
    }

    public void removeGroupMember(String userID){
        this.groupMembers.remove(userID);
    }

    public void addGroupAdmin(String userID){
        this.groupAdmins.add(userID);
    }

    public ArrayList<String> getGroupAdmins(){
        return this.groupAdmins;
    }

    public void removeGroupAdmin(String userID){
        this.groupAdmins.remove(userID);
    }
}
