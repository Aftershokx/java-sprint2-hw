package tasktracker;


import tasktracker.managers.HttpTaskManager;
import tasktracker.managers.TaskManager;
import tasktracker.server.HttpTaskServer;
import tasktracker.server.KVServer;
import tasktracker.tasks.EpicTask;
import tasktracker.tasks.Status;
import tasktracker.tasks.SubTask;
import tasktracker.tasks.Task;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;


public class Main {
    public static void main (String[] args) throws IOException, InterruptedException {
        KVServer kvServer = new KVServer ();
        kvServer.start ();
        TaskManager taskManager = new HttpTaskManager ("http://localhost:8078/");
        HttpTaskServer httpTaskServer = new HttpTaskServer (taskManager);

        EpicTask epic = new EpicTask ("epic1", "descEpic1");
        taskManager.createTask (epic);
        SubTask subtask1 = new SubTask ("sub1", "descSub1", Status.NEW, epic.getIdentifier ());
        subtask1.setStartTime ("2022-03-13T12:15:30");
        subtask1.setDuration (10);
        taskManager.createTask (subtask1);
        SubTask subtask2 = new SubTask ("sub2", "descSub2", Status.NEW, epic.getIdentifier ());
        subtask2.setStartTime ("2022-02-13T12:15:30");
        subtask2.setDuration (10);
        taskManager.createTask (subtask2);
        Task task = new Task ("task1", "descTask1", Status.NEW);
        task.setStartTime ("2022-04-13T12:15:30");
        task.setDuration (10);
        taskManager.createTask (task);
        Task task2 = new Task ("task2", "descTask2", Status.NEW);
        task2.setStartTime ("2022-05-13T12:15:30");
        task2.setDuration (30);
        taskManager.createTask (task2);

        httpTaskServer.start ();

        HttpClient client = HttpClient.newHttpClient ();
        HttpRequest req = HttpRequest.newBuilder ()
                .GET ()
                .uri (URI.create ("http://localhost:8080/tasks/"))
                .version (HttpClient.Version.HTTP_1_1)
                .build ();

        HttpResponse<String> response = client.send (req, HttpResponse.BodyHandlers.ofString ());
        System.out.println (response);
    }
}
