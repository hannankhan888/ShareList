package com.example.loginsharelist;

import java.util.HashMap;
import java.util.Map;

/**
 * This class implements a Task object which stores information about a task.
 */
public class Task {
    private String taskName;
    private String taskDescription;
    private String taskId;
    private String taskCreationDate;
    private String taskDueDate;
    private String taskBelongsToGroupID;
    private boolean mark;
    private Map<String, String> taskAssignedUsers = new HashMap<>();

    public boolean isMark() {
        return mark;
    }

    public void setMark(boolean mark) {
        this.mark = mark;
    }

    public String getTaskDueDate() {
        return taskDueDate;
    }

    public void setTaskDueDate(String taskDueDate) {
        this.taskDueDate = taskDueDate;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public void setTaskDescription(String taskDescription) {
        this.taskDescription = taskDescription;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public void setTaskCreationDate(String taskCreationDate) {
        this.taskCreationDate = taskCreationDate;
    }

    public String getTaskName() {
        return taskName;
    }

    public String getTaskDescription() {
        return taskDescription;
    }

    public String getTaskId() {
        return taskId;
    }

    public String getTaskCreationDate() {
        return taskCreationDate;
    }

    public String getTaskBelongsToGroupID() {
        return taskBelongsToGroupID;
    }

    public void setTaskBelongsToGroupID(String groupID){
        this.taskBelongsToGroupID = groupID;
    }

    public void addAssignedUser(String userID){
        this.taskAssignedUsers.put(userID, userID);
    }

    public Map<String, String> getTaskAssignedUsers(){
        return this.taskAssignedUsers;
    }

    public void removeAssignedUser(String userID){
        this.taskAssignedUsers.remove(userID);
    }

    public Task(String taskName, String taskDescription, String taskId, String taskCreationDate,
                String taskDueDate, String taskBelongsToGroupID, boolean mark, Map<String, String> taskAssignedUsers) {
        this.taskName = taskName;
        this.taskDescription = taskDescription;
        this.taskId = taskId;
        this.taskCreationDate = taskCreationDate;
        this.taskDueDate = taskDueDate;
        this.taskBelongsToGroupID = taskBelongsToGroupID;
        this.mark = mark;
        this.taskAssignedUsers = taskAssignedUsers;
    }

    public Task() {

    }
}
