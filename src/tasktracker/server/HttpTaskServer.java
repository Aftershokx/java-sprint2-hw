package tasktracker.server;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpServer;
import tasktracker.managers.Managers;
import tasktracker.managers.TaskManager;
import tasktracker.server.handlers.AllTasksHandler;
import tasktracker.server.handlers.HistoryHandler;
import tasktracker.server.handlers.TaskHandler;

import java.io.IOException;
import java.net.InetSocketAddress;

public class HttpTaskServer {
    private final HttpServer httpServer;

    public HttpTaskServer(TaskManager manager) throws IOException {
        Gson gson = Managers.createDefaultGson ();
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
