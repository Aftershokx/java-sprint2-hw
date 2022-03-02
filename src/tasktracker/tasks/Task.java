package tasktracker.tasks;

import java.util.Objects;

public class Task {
    private String name;
    private String description;
    private Status status;
    private int identifier;

    public Task(String name, String description, Status status) {
        this.name = name;
        this.description = description;
        this.status = status;
    }

    public Task(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Status getStatus() {
        return status;
    }

    public int getIdentifier() {
        return identifier;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public void setIdentifier(int identifier) {
        this.identifier = identifier;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (this.getClass() != obj.getClass()) return false;
        Task otherTask = (Task) obj;
        return Objects.equals(name, otherTask.name) &&
                Objects.equals(description, otherTask.description) && Objects.equals(status, otherTask.status) &&
                identifier == otherTask.identifier;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, description, identifier, status);
    }

    @Override
    public String toString() {
        return "task {" +
                "name = '" + name + '\'' +
                ", description = '" + description + '\'' +
                ", identifier = " + identifier +
                ", status = '" + status + '\'' +
                '}';
    }
}
