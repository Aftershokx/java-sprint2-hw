package tasktracker.server;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.net.httpserver.HttpServer;

import tasktracker.managers.TaskManager;
import tasktracker.utility.exceptions.IllegalHeaderException;
import tasktracker.utility.exceptions.RequestException;
import tasktracker.utility.exceptions.TaskException;
import tasktracker.server.handlers.HistoryHandler;
import tasktracker.server.handlers.AllTasksHandler;
import tasktracker.server.handlers.TaskHandler;
import tasktracker.server.adapters.DurationAdapter;
import tasktracker.server.adapters.ExceptionAdapter;
import tasktracker.server.adapters.LocalDateTimeAdapter;


import java.io.IOException;
import java.net.InetSocketAddress;
import java.time.Duration;
import java.time.LocalDateTime;

public class HttpTaskServer {
    private final HttpServer httpServer;

    public HttpTaskServer(TaskManager manager) throws IOException {
        Gson gson = new GsonBuilder ()
                .registerTypeAdapter (Duration.class, new DurationAdapter ())
                .registerTypeAdapter (LocalDateTime.class, new LocalDateTimeAdapter ())
                .registerTypeAdapter (TaskException.class, new ExceptionAdapter ())
                .registerTypeAdapter (RequestException.class, new ExceptionAdapter ())
                .registerTypeAdapter (IllegalHeaderException.class, new ExceptionAdapter ())
                .setPrettyPrinting ()
                .create ();

        httpServer = HttpServer.create(new InetSocketAddress(8080), 0);
        httpServer.createContext("/tasks/task", new TaskHandler (manager, gson));
        httpServer.createContext("/tasks/subtask", new TaskHandler (manager, gson));
        httpServer.createContext("/tasks/epic", new TaskHandler (manager, gson));
        httpServer.createContext("/tasks/history", new HistoryHandler (manager, gson));
        httpServer.createContext("/tasks/", new AllTasksHandler (manager, gson));
    }

    public void start() {
        httpServer.start();
    }

    public void stop() {
        httpServer.stop(1);
    }
}
