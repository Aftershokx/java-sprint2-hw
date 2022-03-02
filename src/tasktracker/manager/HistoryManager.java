package tasktracker.manager;

import tasktracker.tasks.Task;

import java.util.ArrayList;

public interface HistoryManager {

    void add (Task task);

    ArrayList<Task> getHistory ();

}
