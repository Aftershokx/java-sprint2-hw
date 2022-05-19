package tasktracker.managers;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.*;
import tasktracker.server.KVServer;
import tasktracker.server.KVTaskClient;
import tasktracker.tasks.EpicTask;
import tasktracker.tasks.Status;
import tasktracker.tasks.SubTask;
import tasktracker.tasks.Task;
import tasktracker.utility.exceptions.RequestException;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class HttpTaskManagerTest extends TaskManagerTest<HttpTaskManager> {
    private static KVServer kvServer;
    private static KVTaskClient kvTaskClient;
    private static Gson gson;

    @BeforeAll
    public static void beforeAllHttpManagerTests () {
        try {
            kvServer = new KVServer ();
            kvServer.start ();

            gson = Managers.createDefaultGson ();

            kvTaskClient = new KVTaskClient ("http://localhost:8078/");
            kvTaskClient.setKeyApi ("DEBUG");

        } catch (IOException e) {
            e.printStackTrace ();
        }


    }

    @AfterAll
    public static void afterAllHttpManagerTests () {
        kvServer.stop ();
    }

    public void cleaning () {
        tasksManager.clearTasks ();
        tasksManager.clearSubTasks ();
        tasksManager.clearEpics ();
    }

    @Override
    void preLoadTaskManager () {
        tasksManager = new HttpTaskManager ("http://localhost:8078/");
    }

    @DisplayName("Должно пройти корректное сохранение эпика без подзадач")
    @Test
    public void shouldSaveEpicWithoutSubTasks () throws IOException, InterruptedException, RequestException {
        cleaning ();
        EpicTask epic = new EpicTask ("epic", "epic");
        tasksManager.createTask (epic);
        String responseFromKVServer = kvTaskClient.load ("tasks");
        JsonArray jsonArray = JsonParser.parseString (responseFromKVServer).getAsJsonArray ();
        EpicTask epicFromKVServer = gson.fromJson (jsonArray.get (0), EpicTask.class);
        assertEquals (epic, epicFromKVServer);
    }

    @DisplayName("Должно пройти корректное сохранение эпика с подзадачами")
    @Test
    public void shouldSaveEpicWithSubTasks () throws IOException, InterruptedException, RequestException {
        cleaning ();
        EpicTask epic = new EpicTask ("epic", "epic");
        tasksManager.createTask (epic);
        SubTask subtask1 = new SubTask ("subtask1 for Epic1", "s1", Status.NEW, epic.getIdentifier ());
        tasksManager.createTask (subtask1);
        SubTask subtask2 = new SubTask ("subtask2 for Epic1", "s2", Status.NEW, epic.getIdentifier ());
        tasksManager.createTask (subtask2);
        String responseFromKVServer = kvTaskClient.load ("tasks");

        JsonArray jsonArray = JsonParser.parseString (responseFromKVServer).getAsJsonArray ();
        EpicTask epicFromKVServer = gson.fromJson (jsonArray.get (2), EpicTask.class);

        Assertions.assertAll (
                () -> assertEquals (epic, epicFromKVServer),
                () -> Assertions.assertTrue (epicFromKVServer.getSubTasks ().contains (subtask1)),
                () -> Assertions.assertTrue (epicFromKVServer.getSubTasks ().contains (subtask2))
        );
    }

    @DisplayName("Должно пройти корректное сохранение задач")
    @Test
    public void shouldSaveTasks () throws IOException, InterruptedException, RequestException {
        cleaning ();
        Task task1 = new Task ("Task1", "t1", Status.NEW);
        Task task2 = new Task ("Task2", "t1", Status.NEW);
        tasksManager.createTask (task1);
        tasksManager.createTask (task2);
        String responseFromKVServer = kvTaskClient.load ("tasks");
        JsonArray jsonArray = JsonParser.parseString (responseFromKVServer).getAsJsonArray ();
        Task task1FromKVServer = gson.fromJson (jsonArray.get (0), Task.class);
        Task task2FromKVServer = gson.fromJson (jsonArray.get (1), Task.class);

        Assertions.assertAll (
                () -> assertEquals (task1, task1FromKVServer),
                () -> assertEquals (task2, task2FromKVServer)
        );
    }

    @DisplayName("Должно пройти корректное обновление задачи на сервере, после обновления в менеджере")
    @Test
    public void shouldUpdateTaskInKVServerWhenUpdatedTaskInTaskManager () throws IOException, InterruptedException, RequestException {
        cleaning ();
        Task task = new Task ("task1", "Disc Before Update", Status.NEW);
        tasksManager.createTask (task);
        String newDescription = "new disc for task";
        task.setDescription (newDescription);
        tasksManager.updateTask (task);

        String responseFromKVServer = kvTaskClient.load ("tasks");
        JsonArray jsonArray = JsonParser.parseString (responseFromKVServer).getAsJsonArray ();
        Task fromKVServer = gson.fromJson (jsonArray.get (0), Task.class);

        assertEquals (fromKVServer.getDescription (), newDescription);
    }

    @DisplayName("Должно пройти корректное обновление эпика на сервере, после обновления в менеджере")
    @Test
    public void shouldUpdateEpicInKVServerWhenUpdatedEpicInTaskManager () throws IOException, InterruptedException, RequestException {
        cleaning ();
        EpicTask epic = new EpicTask ("Epic1", "e1");
        tasksManager.createTask (epic);
        String newEpicTitle = "New epic title";
        epic.setDescription (newEpicTitle);
        tasksManager.updateTask (epic);

        String responseFromKVServer = kvTaskClient.load ("tasks");
        JsonArray jsonArray = JsonParser.parseString (responseFromKVServer).getAsJsonArray ();
        EpicTask epicFromKVServer = gson.fromJson (jsonArray.get (0), EpicTask.class);

        assertEquals (epicFromKVServer.getDescription (), newEpicTitle);

    }

    @DisplayName("Должно пройти корректное сохранение истории на сервере")
    @Test
    public void shouldBeSaveHistoryOnKVServer () throws IOException, InterruptedException, RequestException {
        cleaning ();
        EpicTask epic = new EpicTask ("epic", "epic");
        tasksManager.createTask (epic);
        SubTask subtask1 = new SubTask ("subtask1 for Epic1", "s1", Status.NEW, epic.getIdentifier ());
        SubTask subtask2 = new SubTask ("subtask2 for Epic1", "s2", Status.NEW, epic.getIdentifier ());
        tasksManager.createTask (subtask1);
        tasksManager.createTask (subtask2);
        Task task = new Task ("Task1", "t1", Status.NEW);
        tasksManager.createTask (task);

        tasksManager.searchSubTaskWithId (2);
        tasksManager.searchTaskWithId (4);
        tasksManager.searchEpicWithId (1);

        String responseFromKVServer = kvTaskClient.load ("history");
        JsonArray jsonArray = JsonParser.parseString (responseFromKVServer).getAsJsonArray ();
        SubTask subtask1FromJson = gson.fromJson (jsonArray.get (1), SubTask.class);
        Task TaskFromJson = gson.fromJson (jsonArray.get (2), Task.class);
        EpicTask epicFromJson = gson.fromJson (jsonArray.get (0), EpicTask.class);

        Assertions.assertAll (
                () -> assertEquals (subtask1, subtask1FromJson),
                () -> assertEquals (task, TaskFromJson),
                () -> assertEquals (epic, epicFromJson)
        );
    }

    @DisplayName("Должно пройти корректное сохранение истории на сервере, когда запрашивается уже сущ. задача в истории")
    @Test
    public void shouldUpdateHistoryOnKVServerWhenGetPreviouslyAddedTask () throws IOException, InterruptedException, RequestException {
        cleaning ();
        EpicTask epic = new EpicTask ("epic", "epic");
        tasksManager.createTask (epic);
        SubTask subtask1 = new SubTask ("subtask1 for Epic1", "s1", Status.NEW, epic.getIdentifier ());
        SubTask subtask2 = new SubTask ("subtask2 for Epic1", "s2", Status.NEW, epic.getIdentifier ());
        tasksManager.createTask (subtask1);
        tasksManager.createTask (subtask2);
        Task task = new Task ("Task1", "t1", Status.NEW);
        tasksManager.createTask (task);

        tasksManager.searchSubTaskWithId (2);
        tasksManager.searchTaskWithId (4);
        tasksManager.searchEpicWithId (1);
        tasksManager.searchSubTaskWithId (2);

        String responseFromKVServer = kvTaskClient.load ("history");
        JsonArray jsonArray = JsonParser.parseString (responseFromKVServer).getAsJsonArray ();
        Task TaskFromJson = gson.fromJson (jsonArray.get (1), Task.class);
        EpicTask epicFromJson = gson.fromJson (jsonArray.get (2), EpicTask.class);
        SubTask subtask1FromJson = gson.fromJson (jsonArray.get (0), SubTask.class);

        Assertions.assertAll (
                () -> assertEquals (subtask1, subtask1FromJson),
                () -> assertEquals (task, TaskFromJson),
                () -> assertEquals (epic, epicFromJson)
        );
    }

    @DisplayName ("Должна пройти корректная загрузка из сервера")
    @Test
    public void shouldLoadTaskManagerStateFromKVServer () {
        cleaning ();
        EpicTask epic = new EpicTask ("epic", "epic");
        tasksManager.createTask (epic);
        SubTask subtask1 = new SubTask ("subtask1 for Epic1", "s1", Status.NEW, epic.getIdentifier ());
        SubTask subtask2 = new SubTask ("subtask2 for Epic1", "s2", Status.NEW, epic.getIdentifier ());
        tasksManager.createTask (subtask1);
        tasksManager.createTask (subtask2);
        Task task = new Task ("Task1", "t1", Status.NEW);
        tasksManager.createTask (task);

        HttpTaskManager loadedTaskManager = new HttpTaskManager ("http://localhost:8078/");

        Assertions.assertAll (
                () -> assertEquals (tasksManager.getEpicTasks (), loadedTaskManager.getEpicTasks ()),
                () -> assertEquals (tasksManager.getTasks (), loadedTaskManager.getTasks ()),
                () -> assertEquals (tasksManager.getSubTasks (),
                        loadedTaskManager.getSubTasks ()),
                () -> assertEquals (tasksManager.history (), loadedTaskManager.history ())
        );
    }

}