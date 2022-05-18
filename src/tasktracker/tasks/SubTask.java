package tasktracker.tasks;

public class SubTask extends Task {

    private final int epicIdentifier;

    public SubTask(String name, String description, Status status, int epicIdentifier) {
        super(name, description, status);
        this.epicIdentifier = epicIdentifier;
        this.type = getType ();
    }

    public int getEpicIdentifier() {
        return epicIdentifier;
    }

    @Override
    public Types getType () {
        return Types.SUBTASK;
    }

    @Override
    public String toString() {
        return "subTask {" +
                "name = '" + getName() + '\'' +
                ", description = '" + getDescription() + '\'' +
                ", identifier = " + getIdentifier() +
                ", status = '" + getStatus() + '\'' +
                ", epicIdentifier = '" + epicIdentifier +
                ", type = '" + getType () + '\'' +
                ", duration=" + getDuration () +
                ", startTime=" + getStartTime () +
                '}';
    }


}
