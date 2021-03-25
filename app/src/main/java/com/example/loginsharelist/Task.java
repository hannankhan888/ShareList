package com.example.loginsharelist;

public class Task {
    private String taskName;
    private String taskDescription;
    private String taskId;
    private String taskCreationDate;
    private String taskDueDate;
    private boolean mark;

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

    public Task(String taskName, String taskDescription, String taskId, String taskCreationDate, String taskDueDate, boolean mark) {
        this.taskName = taskName;
        this.taskDescription = taskDescription;
        this.taskId = taskId;
        this.taskCreationDate = taskCreationDate;
        this.taskDueDate = taskDueDate;
        this.mark = mark;
    }

    public Task() {

    }
}
