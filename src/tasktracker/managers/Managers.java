package tasktracker.managers;

public class Managers {

    public static TaskManager getDefault () {
        return new InMemoryTaskManager ();
    }

    public static HistoryManager getDefaultHistory () {
        return new InMemoryHistoryManager ();
    }

    public static FileBackedTasksManager getDefaultFileBacked(){
        return new FileBackedTasksManager ();
    }

}