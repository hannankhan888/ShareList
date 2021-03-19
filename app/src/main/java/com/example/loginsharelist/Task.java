package com.example.loginsharelist;

public class Task {
    private String taskName;
    private String description;
    private String id;
    private String creationDate;
    private String dueDate;

    public String getDueDate() {
        return dueDate;
    }

    public void setDueDate(String dueDate) {
        this.dueDate = dueDate;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setCreationDate(String creationDate) {
        this.creationDate = creationDate;
    }

    public String getTaskName() {
        return taskName;
    }

    public String getDescription() {
        return description;
    }

    public String getId() {
        return id;
    }

    public String getCreationDate() {
        return creationDate;
    }

    public Task(String taskName, String description, String id, String creationDate, String dueDate) {
        this.taskName = taskName;
        this.description = description;
        this.id = id;
        this.creationDate = creationDate;
        this.dueDate = dueDate;
    }

    public Task() {

    }
}
