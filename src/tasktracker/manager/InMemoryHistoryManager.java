package tasktracker.manager;

import tasktracker.tasks.Task;

import java.util.ArrayList;

public class InMemoryHistoryManager implements HistoryManager {
    public final ArrayList<Task> history;

    public InMemoryHistoryManager () {
        this.history = new ArrayList<> ();
    }

    @Override
    public void add (Task task) {
         if (history.size () == 10) {
            history.remove (0);
        }history.add (task);
    }

    @Override
    public ArrayList<Task> getHistory () {
        return history;
    }

}
