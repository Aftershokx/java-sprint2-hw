package tasktracker.managers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import tasktracker.server.adapters.DurationAdapter;
import tasktracker.server.adapters.ExceptionAdapter;
import tasktracker.server.adapters.LocalDateTimeAdapter;
import tasktracker.utility.exceptions.RequestException;
import tasktracker.utility.exceptions.TaskException;

import java.time.Duration;
import java.time.LocalDateTime;

public class Managers {

    public static TaskManager getDefault () {
        return new InMemoryTaskManager ();
    }

    public static HistoryManager getDefaultHistory () {
        return new InMemoryHistoryManager ();
    }

    public static FileBackedTasksManager getDefaultFileBacked () {
        return new FileBackedTasksManager ();
    }

    public static Gson createDefaultGson () {
        return new GsonBuilder ().registerTypeAdapter (Duration.class, new DurationAdapter ())
                .registerTypeAdapter (LocalDateTime.class, new LocalDateTimeAdapter ())
                .registerTypeAdapter (TaskException.class, new ExceptionAdapter ())
                .registerTypeAdapter (RequestException.class, new ExceptionAdapter ())
                .setPrettyPrinting ()
                .create ();
    }

}
