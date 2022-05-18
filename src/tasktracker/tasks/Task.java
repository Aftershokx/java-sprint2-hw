package tasktracker.tasks;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;

public class Task {
    private String name;
    private String description;
    private Status status;
    private int identifier;
    protected Types type;
    protected Duration duration;
    protected LocalDateTime startTime;

    public Task(String name, String description, Status status) {
        this.name = name;
        this.description = description;
        this.status = status;
    }

    public Duration getDuration() {
        return duration;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public LocalDateTime getEndTime () {
        if (getStartTime() == null) {
            return null;
        }
        return getStartTime().plus(getDuration());
    }

    public void setDuration(long durationOfMinuts) {
        this.duration = Duration.ofMinutes(durationOfMinuts);
    }

    public void setStartTime(String startTime) {
        if (!startTime.equals("null")) {
            this.startTime = LocalDateTime.parse(startTime);
        }
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

    public void setDescription (String description) {
        this.description = description;
    }

    public void setStatus (Status status) {
        this.status = status;
    }

    public void setType (Types type) {
        this.type = type;
    }

    public Types getType () {
        return type;
    }

    public void setIdentifier (int identifier) {
        this.identifier = identifier;
    }

    @Override
    public boolean equals (Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (this.getClass () != obj.getClass ()) return false;
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
    public String toString () {
        return "Task{" +
                "name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", status=" + status +
                ", identifier=" + identifier +
                ", type=" + type +
                ", duration=" + duration +
                ", startTime=" + startTime +
                '}';
    }
}
