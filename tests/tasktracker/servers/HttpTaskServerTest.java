package tasktracker.servers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.*;
import tasktracker.managers.Managers;
import tasktracker.managers.TaskManager;
import tasktracker.server.HttpTaskServer;
import tasktracker.server.KVServer;
import tasktracker.server.adapters.DurationAdapter;
import tasktracker.server.adapters.ExceptionAdapter;
import tasktracker.server.adapters.LocalDateTimeAdapter;
import tasktracker.tasks.*;
import tasktracker.utility.exceptions.RequestException;
import tasktracker.utility.exceptions.TaskException;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class HttpTaskServerTest {
    private static KVServer kvServer;
    private static HttpTaskServer httpTaskServer;
    private static TaskManager taskManager;
    private static HttpClient httpClient;
    private static String url;
    private static EpicTask epic;
    private static SubTask subtask;
    private static Task task;
    private static Gson gson;

    @BeforeAll
    public static void httpTaskServerTestBeforeAll () throws IOException {
        taskManager = Managers.getDefault ();
        httpTaskServer = new HttpTaskServer (taskManager);
        kvServer = new KVServer ();
        kvServer.start ();

        epic = new EpicTask ("epic", "epic");
        taskManager.createTask (epic);
        subtask = new SubTask ("subtask1", "s1", Status.NEW, epic.getIdentifier ());
        taskManager.createTask (subtask);
        task = new Task ("Task1", "t1", Status.NEW);
        taskManager.createTask (task);

        httpTaskServer.start ();

        httpClient = HttpClient.newHttpClient ();
        url = "http://localhost:8080";
        gson = new GsonBuilder ()
                .registerTypeAdapter (Duration.class, new DurationAdapter ())
                .registerTypeAdapter (LocalDateTime.class, new LocalDateTimeAdapter ())
                .registerTypeAdapter (TaskException.class, new ExceptionAdapter ())
                .registerTypeAdapter (RequestException.class, new ExceptionAdapter ())
                .setPrettyPrinting ()
                .create ();
    }

    @AfterAll
    public static void httpTaskServerTestAfterAll () {
        httpTaskServer.stop ();
        kvServer.stop ();
    }

    @BeforeEach
    public void beforeEachHttpTaskServerTest () {
        taskManager.clearTasks ();
        taskManager.clearSubTasks ();
        taskManager.clearEpics ();
        taskManager.createTask (epic);
        taskManager.createTask (subtask);
        taskManager.createTask (task);

    }

    @DisplayName("Должен вернуть EpicTask при GET-запросе с id по адресу tasks/epic?id=1")
    @Test
    public void shouldReturnEpicTaskByGetRequestWithId () throws IOException, InterruptedException {
        URI uri = URI.create (url + "/tasks/epic?id=1");
        HttpRequest request = HttpRequest.newBuilder ()
                .GET ()
                .uri (uri)
                .version (HttpClient.Version.HTTP_1_1)
                .build ();
        HttpResponse.BodyHandler<String> handler = HttpResponse.BodyHandlers.ofString ();
        HttpResponse<String> response = httpClient.send (request, handler);
        EpicTask epicFromServer = gson.fromJson (response.body (), EpicTask.class);
        assertEquals (epic, epicFromServer);
    }

    @DisplayName("Должен вернуть Subtask при GET-запросе с id по адресу tasks/subtask?id=2")
    @Test
    public void shouldReturnSubtaskByGetRequestWithId () throws IOException, InterruptedException {
        URI uri = URI.create (url + "/tasks/subtask?id=2");
        HttpRequest request = HttpRequest.newBuilder ()
                .GET ()
                .uri (uri)
                .version (HttpClient.Version.HTTP_1_1)
                .build ();
        HttpResponse.BodyHandler<String> handler = HttpResponse.BodyHandlers.ofString ();
        HttpResponse<String> response = httpClient.send (request, handler);
        SubTask fromServer = gson.fromJson (response.body (), SubTask.class);
        assertEquals (subtask, fromServer);
    }

    @DisplayName("Должен вернуть task при GET-запросе с id по адресу tasks/task?id=3")
    @Test
    public void shouldReturnTaskByGetRequestWithId () throws IOException, InterruptedException {
        URI uri = URI.create (url + "/tasks/task?id=3");
        HttpRequest request = HttpRequest.newBuilder ()
                .GET ()
                .uri (uri)
                .version (HttpClient.Version.HTTP_1_1)
                .build ();
        HttpResponse.BodyHandler<String> handler = HttpResponse.BodyHandlers.ofString ();
        HttpResponse<String> response = httpClient.send (request, handler);
        Task fromServer = gson.fromJson (response.body (), Task.class);
        assertEquals (task, fromServer);
    }

    @DisplayName("Должен вернуть статус 400 и информацию об ошибке, если задача не была найдена по id")
    @Test
    public void shouldReturn400AndErrorInfoIfTaskNotFoundWithId () throws IOException, InterruptedException {
        URI uri = URI.create (url + "/tasks/task?id=111");
        HttpRequest request = HttpRequest.newBuilder ()
                .GET ()
                .uri (uri)
                .version (HttpClient.Version.HTTP_1_1)
                .build ();
        HttpResponse.BodyHandler<String> handler = HttpResponse.BodyHandlers.ofString ();
        HttpResponse<String> response = httpClient.send (request, handler);
        JsonObject jsonObj = JsonParser.parseString (response.body ()).getAsJsonObject ();
        Assertions.assertAll (() -> assertEquals (jsonObj.get ("status").getAsInt (), 400), () ->
                assertEquals (jsonObj.get ("message").getAsString (), "Задача c id=111 не найдена"), () ->
                Assertions.assertNotNull (jsonObj.get ("stackTrace")));
    }

    @DisplayName("Должен вернуть статус 400 и информацию об ошибке, не передан параметр запроса id")
    @Test
    public void shouldReturn400AndErrorInfoIfRequestParameterIdNotFound () throws IOException, InterruptedException {
        URI uri = URI.create (url + "/tasks/task?ids=111");
        HttpRequest request = HttpRequest.newBuilder ()
                .GET ()
                .uri (uri)
                .version (HttpClient.Version.HTTP_1_1)
                .build ();

        HttpResponse<String> response = httpClient.send (request, HttpResponse.BodyHandlers.ofString ());
        JsonObject jsonObj = JsonParser.parseString (response.body ()).getAsJsonObject ();

        Assertions.assertAll (() -> assertEquals (jsonObj.get ("status").getAsInt (), 400), () ->
                assertEquals (jsonObj.get ("message").getAsString (), "Параметр id в строке запроса не был найден"), () ->
                Assertions.assertNotNull (jsonObj.get ("stackTrace")));
    }

    @DisplayName("Должен обновить Epic при POST запросе по адресу tasks/epic c заголовком X-context:update")
    @Test
    public void shouldUpdateEpicTaskWithPostRequestWithHeaderXContextUpdate () throws IOException, InterruptedException {
        URI uri = URI.create (url + "/tasks/epic");
        EpicTask updatedEpic = new EpicTask ("updatedEpic", "");
        updatedEpic.setIdentifier (epic.getIdentifier ());
        HttpRequest.BodyPublisher bodyPublisher = HttpRequest.BodyPublishers.ofString (gson.toJson (updatedEpic));
        HttpRequest request = HttpRequest.newBuilder ()
                .POST (bodyPublisher)
                .uri (uri)
                .header ("X-context", "update")
                .version (HttpClient.Version.HTTP_1_1)
                .build ();
        httpClient.send (request, HttpResponse.BodyHandlers.ofString ());
        Assertions.assertEquals (taskManager.searchEpicWithId (epic.getIdentifier ()).getName (), updatedEpic.getName ());
    }

    @DisplayName("Должен обновить Subtask при POST запросе по адресу tasks/subtask c заголовком X-context:update")
    @Test
    public void shouldUpdateSubtaskTaskWithPostRequestWithHeaderXContextUpdate () throws IOException, InterruptedException {
        URI uri = URI.create (url + "/tasks/task");
        SubTask updatedSubtask = new SubTask ("subtask1", " ", Status.NEW, epic.getIdentifier ());
        updatedSubtask.setIdentifier (subtask.getIdentifier ());
        HttpRequest.BodyPublisher bodyPublisher = HttpRequest.BodyPublishers.ofString (gson.toJson (updatedSubtask));
        HttpRequest request = HttpRequest.newBuilder ()
                .POST (bodyPublisher)
                .uri (uri)
                .header ("X-context", "update")
                .version (HttpClient.Version.HTTP_1_1)
                .build ();
        httpClient.send (request, HttpResponse.BodyHandlers.ofString ());
        Assertions.assertEquals (taskManager.searchSubTaskWithId (subtask.getIdentifier ()).getName (), updatedSubtask.getName ());
    }

    @DisplayName("Должен обновить task при POST запросе по адресу tasks/task c заголовком X-context:update")
    @Test
    public void shouldUpdateTaskWithPostRequestWithHeaderXContextUpdate () throws IOException, InterruptedException {
        URI uri = URI.create (url + "/tasks/task");
        Task updatedTask = new Task ("updated task", "", Status.NEW);
        updatedTask.setIdentifier (task.getIdentifier ());
        updatedTask.setType (Types.TASK);
        HttpRequest.BodyPublisher bodyPublisher = HttpRequest.BodyPublishers.ofString (gson.toJson (updatedTask));
        HttpRequest request = HttpRequest.newBuilder ()
                .POST (bodyPublisher)
                .uri (uri)
                .header ("X-context", "update")
                .version (HttpClient.Version.HTTP_1_1)
                .build ();
        httpClient.send (request, HttpResponse.BodyHandlers.ofString ());
        Assertions.assertEquals (taskManager.searchTaskWithId (task.getIdentifier ()).getName (), updatedTask.getName ());
    }

    @DisplayName("Должен добавить task при POST запросе по адресу tasks/task c заголовком X-context:create")
    @Test
    public void shouldAddTaskWithPostRequestWithHeaderXContextCreate () throws IOException, InterruptedException {
        URI uri = URI.create (url + "/tasks/task");
        Task newTask = new Task ("New task", "", Status.NEW);
        newTask.setType (Types.TASK);
        newTask.setIdentifier (10);
        HttpRequest.BodyPublisher bodyPublisher = HttpRequest.BodyPublishers.ofString (gson.toJson (newTask));
        HttpRequest request = HttpRequest.newBuilder ()
                .POST (bodyPublisher)
                .uri (uri)
                .header ("X-context", "create")
                .version (HttpClient.Version.HTTP_1_1).build ();
        httpClient.send (request, HttpResponse.BodyHandlers.ofString ());
        Assertions.assertEquals (taskManager.searchTaskWithId (newTask.getIdentifier ()).getName (), newTask.getName ());
    }

    @DisplayName("Должен добавить Epic при POST запросе по адресу tasks/epic c заголовком X-context:create")
    @Test
    public void shouldAddEpicTaskWithPostRequestWithHeaderXContextCreate () throws IOException, InterruptedException {
        URI uri = URI.create (url + "/tasks/epic");
        EpicTask newEpic = new EpicTask ("New epic", "");
        newEpic.setIdentifier (6);
        HttpRequest.BodyPublisher bodyPublisher = HttpRequest.BodyPublishers.ofString (gson.toJson (newEpic));
        HttpRequest request = HttpRequest.newBuilder ()
                .POST (bodyPublisher)
                .uri (uri)
                .header ("X-context", "create")
                .version (HttpClient.Version.HTTP_1_1).build ();
        httpClient.send (request, HttpResponse.BodyHandlers.ofString ());
        Assertions.assertEquals (taskManager.searchEpicWithId (newEpic.getIdentifier ()).getName (), newEpic.getName ());
    }

    @DisplayName("Должен добавить Subtask при POST запросе по адресу tasks/subtask c заголовком X-context:create")
    @Test
    public void shouldAddSubtaskTaskWithPostRequestWithHeaderXContextCreate () throws IOException, InterruptedException {
        URI uri = URI.create (url + "/tasks/subtask");
        SubTask newSubtask = new SubTask ("New subtask", "", Status.NEW, epic.getIdentifier ());
        newSubtask.setIdentifier (7);
        HttpRequest.BodyPublisher bodyPublisher = HttpRequest.BodyPublishers.ofString (gson.toJson (newSubtask));
        HttpRequest request = HttpRequest.newBuilder ()
                .POST (bodyPublisher)
                .uri (uri)
                .header ("X-context", "create")
                .version (HttpClient.Version.HTTP_1_1)
                .build ();
        httpClient.send (request, HttpResponse.BodyHandlers.ofString ());
        Assertions.assertEquals (taskManager.searchSubTaskWithId (newSubtask.getIdentifier ()).getName (), newSubtask.getName ());
    }

    @DisplayName("Должен вернуть 400 ошибку и описание, если заголовка X-context не было")
    @Test
    public void shouldReturn400AndErrorInfoIfHeaderXContextNotFound () throws IOException, InterruptedException {
        URI uri = URI.create (url + "/tasks/task");
        SubTask newSubtask = new SubTask ("New subtask", "", Status.NEW, epic.getIdentifier ());
        HttpRequest.BodyPublisher bodyPublisher = HttpRequest.BodyPublishers.ofString (gson.toJson (newSubtask));
        HttpRequest request = HttpRequest.newBuilder ()
                .POST (bodyPublisher)
                .uri (uri)
                .version (HttpClient.Version.HTTP_1_1)
                .build ();
        HttpResponse<String> response = httpClient.send (request, HttpResponse.BodyHandlers.ofString ());
        JsonObject jsonObject = JsonParser.parseString (response.body ()).getAsJsonObject ();
        Assertions.assertAll (() -> assertEquals (jsonObject.get ("status").getAsInt (), 400), ()
                -> assertEquals (jsonObject.get ("message").getAsString (), "Нет заголовка 'X-context'"));
    }

    @DisplayName("Должен вернуть 400 ошибку и описание, если происходит ошибка при добавлении")
    @Test
    public void shouldReturn400AndInfoWhenAddingThrowsAnError () throws IOException, InterruptedException {
        URI uri = URI.create (url + "/tasks/subtask");
        SubTask newSubtask = new SubTask ("New subtask", "", Status.NEW, epic.getIdentifier ());
        newSubtask.setIdentifier (3);
        HttpRequest.BodyPublisher bodyPublisher = HttpRequest.BodyPublishers.ofString (gson.toJson (newSubtask));
        HttpRequest request = HttpRequest.newBuilder ()
                .POST (bodyPublisher)
                .header ("X-context", "create")
                .uri (uri)
                .version (HttpClient.Version.HTTP_1_1)
                .build ();
        HttpResponse<String> response = httpClient.send (request, HttpResponse.BodyHandlers.ofString ());
        JsonObject jsonObject = JsonParser.parseString (response.body ()).getAsJsonObject ();
        Assertions.assertAll (() -> assertEquals (jsonObject.get ("status").getAsInt (), 400), ()
                -> assertEquals (jsonObject.get ("message").getAsString (),
                "Не удалось добавить новую задачу с id=3, возможно, она была добавлена ранее"));
    }

    @DisplayName("Должен вернуть 400 ошибку и описание, если происходит ошибка при обновлении")
    @Test
    public void shouldReturn400AndInfoWhenUpdatingAnError () throws IOException, InterruptedException {
        URI uri = URI.create (url + "/tasks/subtask");
        SubTask newSubtask = new SubTask ("New subtask", "", Status.NEW, epic.getIdentifier ());
        newSubtask.setIdentifier (999);
        HttpRequest.BodyPublisher bodyPublisher = HttpRequest.BodyPublishers.ofString (gson.toJson (newSubtask));
        HttpRequest request = HttpRequest.newBuilder ()
                .POST (bodyPublisher)
                .header ("X-context", "update")
                .uri (uri)
                .version (HttpClient.Version.HTTP_1_1)
                .build ();
        HttpResponse<String> response = httpClient.send (request, HttpResponse.BodyHandlers.ofString ());
        JsonObject jsonObject = JsonParser.parseString (response.body ()).getAsJsonObject ();
        Assertions.assertAll (() -> assertEquals (jsonObject.get ("status").getAsInt (), 400), ()
                -> assertEquals (jsonObject.get ("message").getAsString (),
                "Не удалось обновить задачу c id=999. Проверьте, была ли ранее создана задача с данным ИД"));
    }

    @DisplayName("Должен вернуть корректный список истории при запросе GET по адресу tasks/history")
    @Test
    public void shouldReturnHistory () throws IOException, InterruptedException {
        URI uri = URI.create (url + "/tasks/history");
        HttpRequest request = HttpRequest.newBuilder ()
                .GET ()
                .uri (uri)
                .version (HttpClient.Version.HTTP_1_1)
                .build ();
        HttpResponse<String> response = httpClient.send (request, HttpResponse.BodyHandlers.ofString ());
        Assertions.assertEquals (gson.toJson (taskManager.history ()), response.body ());
    }

    @DisplayName("Должен вернуть корректный список всех типов задач при запросе GET по адресу /tasks/")
    @Test
    public void shouldReturnPrioritizedTasksList () throws IOException, InterruptedException {
        URI uri = URI.create (url + "/tasks/");
        HttpRequest request = HttpRequest.newBuilder ()
                .GET ()
                .uri (uri)
                .version (HttpClient.Version.HTTP_1_1)
                .build ();
        HttpResponse<String> response = httpClient.send (request, HttpResponse.BodyHandlers.ofString ());
        Assertions.assertEquals (response.body (), gson.toJson (taskManager.getPrioritizedTasks ()));
    }

    @DisplayName("Должно пройти корректное удаление задачи по ИД, при запросе DELETE по адресу tasks/task?id=3")
    @Test
    public void shouldDeleteTasksCorrectly () throws IOException, InterruptedException {
        URI uri = URI.create (url + "/tasks/task?id=3");
        HttpRequest request = HttpRequest.newBuilder ()
                .DELETE ()
                .uri (uri)
                .version (HttpClient.Version.HTTP_1_1)
                .build ();
        HttpResponse<String> response = httpClient.send (request, HttpResponse.BodyHandlers.ofString ());
        Assertions.assertAll (() -> assertEquals (response.statusCode (), 200), ()
                -> assertTrue (taskManager.getTasks ().isEmpty ()), ()
                -> assertTrue (taskManager.getSubTasks ().contains (subtask)), ()
                -> assertTrue (taskManager.getEpicTasks ().contains (epic)));
    }

    @DisplayName("Должно пройти корректное удаление всех задач, при запросе DELETE по адресу tasks/task")
    @Test
    public void shouldDeleteAllTasksCorrectly () throws IOException, InterruptedException {
        URI uri = URI.create (url + "/tasks/task");
        HttpRequest request = HttpRequest.newBuilder ()
                .DELETE ()
                .uri (uri)
                .version (HttpClient.Version.HTTP_1_1)
                .build ();
        HttpResponse<String> response = httpClient.send (request, HttpResponse.BodyHandlers.ofString ());
        Assertions.assertAll (() -> assertEquals (response.statusCode (), 200), ()
                -> assertTrue (taskManager.getTasks ().isEmpty ()), ()
                -> assertTrue (taskManager.getSubTasks ().contains (subtask)), ()
                -> assertTrue (taskManager.getEpicTasks ().contains (epic)));
    }

    @DisplayName("Должен вернуть 400 ошибку и описание, если происходит ошибка при удалении задачи по ИД")
    @Test
    public void shouldReturn400WhenDeleteTasksWithIncorrectId () throws IOException, InterruptedException {
        URI uri = URI.create (url + "/tasks/task?id=10");
        HttpRequest request = HttpRequest.newBuilder ()
                .DELETE ()
                .uri (uri)
                .version (HttpClient.Version.HTTP_1_1)
                .build ();
        HttpResponse<String> response = httpClient.send (request, HttpResponse.BodyHandlers.ofString ());
        JsonObject jsonObject = JsonParser.parseString (response.body ()).getAsJsonObject ();
        Assertions.assertAll (() -> assertEquals (jsonObject.get ("status").getAsInt (), 400), ()
                -> assertEquals (jsonObject.get ("message").getAsString (),
                "Не удалось удалить задачу c id=10. Проверьте, была ли ранее создана задача с данным ИД"));
    }
}