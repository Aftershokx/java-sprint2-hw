package tasktracker.managers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.*;
import tasktracker.server.KVServer;
import tasktracker.server.KVTaskClient;
import tasktracker.server.adapters.DurationAdapter;
import tasktracker.server.adapters.ExceptionAdapter;
import tasktracker.server.adapters.LocalDateTimeAdapter;
import tasktracker.tasks.EpicTask;
import tasktracker.tasks.Status;
import tasktracker.tasks.SubTask;
import tasktracker.tasks.Task;
import tasktracker.utility.exceptions.RequestException;
import tasktracker.utility.exceptions.TaskException;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class HttpTaskManagerTest extends FileBackedTasksManagerTest {
    private static KVServer kvServer;
    private HttpTaskManager HttpTaskManager;
    private static KVTaskClient kvTaskClient;
    private static Gson gson;

    @BeforeAll
    public static void beforeAllHttpTaskManagerTests () {
        try {
            kvServer = new KVServer ();
            kvServer.start ();

            gson = new GsonBuilder ()
                    .registerTypeAdapter (Duration.class, new DurationAdapter ())
                    .registerTypeAdapter (LocalDateTime.class, new LocalDateTimeAdapter ())
                    .registerTypeAdapter (TaskException.class, new ExceptionAdapter ())
                    .registerTypeAdapter (RequestException.class, new ExceptionAdapter ())
                    .setPrettyPrinting ()
                    .create ();

            kvTaskClient = new KVTaskClient ("http://localhost:8078/");
            kvTaskClient.setKeyApi ("DEBUG");

        } catch (IOException e) {
            e.printStackTrace ();
        }


    }

    @BeforeEach
    public void beforeEachHttpTaskManagerTests () {
        HttpTaskManager = new HttpTaskManager ("http://localhost:8078/");
    }

    @DisplayName ("Должно пройти корректное сохранение эпика без подзадач")
    @Test
    public void shouldSaveEpicWithoutSubTasks () {
        EpicTask epic = new EpicTask ("epic", "epic");
        HttpTaskManager.createTask (epic);
        String responseFromKVServer = kvTaskClient.load ("tasks");
        JsonArray jsonArray = JsonParser.parseString (responseFromKVServer).getAsJsonArray ();
        EpicTask epicFromKVServer = gson.fromJson (jsonArray.get (0), EpicTask.class);
        assertEquals (epic, epicFromKVServer);
    }

    @DisplayName ("Должно пройти корректное сохранение эпика с подзадачами")
    @Test
    public void shouldSaveEpicWithSubTasks () {
        EpicTask epic = new EpicTask ("epic", "epic");
        HttpTaskManager.createTask (epic);
        SubTask subtask1 = new SubTask ("subtask1 for Epic1", "s1",  Status.NEW, epic.getIdentifier ());
        HttpTaskManager.createTask (subtask1);
        SubTask subtask2 = new SubTask ("subtask2 for Epic1", "s2",  Status.NEW, epic.getIdentifier ());
        HttpTaskManager.createTask (subtask2);
        String responseFromKVServer = kvTaskClient.load ("tasks");

        JsonArray jsonArray = JsonParser.parseString (responseFromKVServer).getAsJsonArray ();
        EpicTask epicFromKVServer = gson.fromJson (jsonArray.get (2), EpicTask.class);

        Assertions.assertAll (
                () -> assertEquals (epic, epicFromKVServer),
                () -> Assertions.assertTrue (epicFromKVServer.getSubTasks ().contains (subtask1)),
                () -> Assertions.assertTrue (epicFromKVServer.getSubTasks ().contains (subtask2))
        );
    }

    @DisplayName ("Должно пройти корректное сохранение задач")
    @Test
    public void shouldSaveTasks () {
        Task task1 = new Task ( "Task1", "t1", Status.NEW);
        Task task2 = new Task ( "Task2", "t1", Status.NEW);
        HttpTaskManager.createTask (task1);
        HttpTaskManager.createTask (task2);
        String responseFromKVServer = kvTaskClient.load ("tasks");
        JsonArray jsonArray = JsonParser.parseString (responseFromKVServer).getAsJsonArray ();
        Task task1FromKVServer = gson.fromJson (jsonArray.get (0), Task.class);
        Task task2FromKVServer = gson.fromJson (jsonArray.get (1), Task.class);

        Assertions.assertAll (
                () -> assertEquals (task1, task1FromKVServer),
                () -> assertEquals (task2, task2FromKVServer)
        );
    }

    @DisplayName ("Должно пройти корректное обновление задачи на сервере, после обновления в менеджере")
    @Test
    public void shouldUpdateTaskInKVServerWhenUpdatedTaskInTaskManager () {
        Task task = new Task ( "task1", "Disc Before Update", Status.NEW);
        HttpTaskManager.createTask (task);
        String newDescription = "new disc for task";
        task.setDescription (newDescription);
        HttpTaskManager.updateTask (task);

        String responseFromKVServer = kvTaskClient.load ("tasks");
        JsonArray jsonArray = JsonParser.parseString (responseFromKVServer).getAsJsonArray ();
        Task monoFromKVServer = gson.fromJson (jsonArray.get (0), Task.class);

        assertEquals (monoFromKVServer.getDescription (), newDescription);
    }

    @DisplayName ("Должно пройти корректное обновление эпика на сервере, после обновления в менеджере")
    @Test
    public void shouldUpdateEpicInKVServerWhenUpdatedEpicInTaskManager () {
        EpicTask epic = new EpicTask ( "Epic1", "e1");
        HttpTaskManager.createTask (epic);
        String newEpicTitle = "New epic title";
        epic.setDescription (newEpicTitle);
        HttpTaskManager.updateTask (epic);

        String responseFromKVServer = kvTaskClient.load ("tasks");
        JsonArray jsonArray = JsonParser.parseString (responseFromKVServer).getAsJsonArray ();
        EpicTask epicFromKVServer = gson.fromJson (jsonArray.get (0), EpicTask.class);

        assertEquals (epicFromKVServer.getDescription (), newEpicTitle);

    }

    @DisplayName ("Должно пройти корректное сохранение истории на сервере")
    @Test
    public void shouldBeSaveHistoryOnKVServer () {
        EpicTask epic = new EpicTask ("epic", "epic");
        HttpTaskManager.createTask (epic);
        SubTask subtask1 = new SubTask ("subtask1 for Epic1", "s1",  Status.NEW, epic.getIdentifier ());
        SubTask subtask2 = new SubTask ("subtask2 for Epic1", "s2",  Status.NEW, epic.getIdentifier ());
        HttpTaskManager.createTask (subtask1);
        HttpTaskManager.createTask (subtask2);
        Task task = new Task ( "Task1", "t1", Status.NEW);
        HttpTaskManager.createTask (task);

        HttpTaskManager.searchSubTaskWithId (2);
        HttpTaskManager.searchTaskWithId (4);
        HttpTaskManager.searchEpicWithId (1);

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

    @DisplayName ("Должно пройти корректное сохранение истории на сервере, когда запрашивается уже сущ. задача в истории")
    @Test
    public void shouldUpdateHistoryOnKVServerWhenGetPreviouslyAddedTask () {
        EpicTask epic = new EpicTask ("epic", "epic");
        HttpTaskManager.createTask (epic);
        SubTask subtask1 = new SubTask ("subtask1 for Epic1", "s1",  Status.NEW, epic.getIdentifier ());
        SubTask subtask2 = new SubTask ("subtask2 for Epic1", "s2",  Status.NEW, epic.getIdentifier ());
        HttpTaskManager.createTask (subtask1);
        HttpTaskManager.createTask (subtask2);
        Task task = new Task ( "Task1", "t1", Status.NEW);
        HttpTaskManager.createTask (task);

        HttpTaskManager.searchSubTaskWithId (2);
        HttpTaskManager.searchTaskWithId (4);
        HttpTaskManager.searchEpicWithId (1);
        HttpTaskManager.searchSubTaskWithId (2);

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
        EpicTask epic = new EpicTask ("epic", "epic");
        HttpTaskManager.createTask (epic);
        SubTask subtask1 = new SubTask ("subtask1 for Epic1", "s1",  Status.NEW, epic.getIdentifier ());
        SubTask subtask2 = new SubTask ("subtask2 for Epic1", "s2",  Status.NEW, epic.getIdentifier ());
        HttpTaskManager.createTask (subtask1);
        HttpTaskManager.createTask (subtask2);
        Task task = new Task ( "Task1", "t1", Status.NEW);
        HttpTaskManager.createTask (task);

        HttpTaskManager loadedTaskManager = new HttpTaskManager ("http://localhost:8078/");
        loadedTaskManager.loadFromServer ("DEBUG");

        Assertions.assertAll (
                () -> assertEquals (HttpTaskManager.getEpicTasks (), loadedTaskManager.getEpicTasks ()),
                () -> assertEquals (HttpTaskManager.getTasks (), loadedTaskManager.getTasks ()),
                () -> assertEquals (HttpTaskManager.getSubTasks(),
                        loadedTaskManager.getSubTasks ()),
                () -> assertEquals (HttpTaskManager.history (), loadedTaskManager.history ())
        );
    }

    @AfterAll
    public static void afterAllHttpTaskManagerTests () {
        kvServer.stop ();
    }
}