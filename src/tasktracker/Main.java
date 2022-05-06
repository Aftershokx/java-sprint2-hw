package tasktracker;

import tasktracker.manager.Managers;
import tasktracker.manager.TaskManager;
import tasktracker.tasks.EpicTask;
import tasktracker.tasks.Status;
import tasktracker.tasks.SubTask;

import java.time.LocalDateTime;

public class Main {
    public static void main(String[] args) {
        TaskManager manager = Managers.getDefault ();
        EpicTask epicTask = new EpicTask ("test", "test");
        manager.createTask (epicTask);
        SubTask subTask = new SubTask ("test", "test", Status.NEW, epicTask.getIdentifier ());
        subTask.setStartTime (String.valueOf (LocalDateTime.now ()));
        subTask.setDuration (10);
        manager.createTask (subTask);
        epicTask.getEndTime ();
        epicTask.getEndTime ();
    }
}
