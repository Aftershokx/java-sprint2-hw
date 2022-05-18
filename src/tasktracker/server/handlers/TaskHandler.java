package tasktracker.server.handlers;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import tasktracker.managers.TaskManager;
import tasktracker.server.getters.QueryParametersGetter;
import tasktracker.tasks.EpicTask;
import tasktracker.tasks.SubTask;
import tasktracker.tasks.Task;
import tasktracker.utility.exceptions.IllegalHeaderException;
import tasktracker.utility.exceptions.RequestException;
import tasktracker.utility.exceptions.TaskException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.OptionalInt;

public class TaskHandler implements HttpHandler {
    private final TaskManager taskManager;
    private final Gson gson;

    public TaskHandler (TaskManager taskManager, Gson gson) {
        this.taskManager = taskManager;
        this.gson = gson;
    }

    //Считывание метода
    @Override
    public void handle (HttpExchange httpExchange) {
        String response = "";
        int statusCode = 200;

        try {
            String method = httpExchange.getRequestMethod ();
            switch (method) {
                case "GET":
                    response = handleGetRequest (httpExchange);
                    break;
                case "POST":
                    handlePostRequest (httpExchange);
                    break;
                case "DELETE":
                    handleDeleteRequest (httpExchange);
                    break;
                default:
                    throw new RequestException ("Метод " + method + " не поддерживается");
            }

        } catch (IllegalHeaderException | RequestException | TaskException e) {
            e.printStackTrace ();
            System.out.println (e.getMessage ());
            statusCode = 400;
            response = gson.toJson (e);
        } catch (IOException e) {
            e.printStackTrace ();
        } finally {
            handlerResponse (httpExchange, response, statusCode);
        }


    }

    private void handlerResponse (HttpExchange httpExchange, String response, int statusCode) {
        try (OutputStream os = httpExchange.getResponseBody ()) {
            httpExchange.sendResponseHeaders (statusCode, 0);
            os.write (response.getBytes (StandardCharsets.UTF_8));
        } catch (IOException e) {
            e.printStackTrace ();
        }
    }

    //преобразование задач
    private Task parseJsonToTask (String body) throws RequestException {
        JsonElement jsonElement = JsonParser.parseString (body);
        if (!jsonElement.isJsonObject ()) {
            throw new RequestException ("Содержимое тела не является json объектом");
        }

        String type = jsonElement.getAsJsonObject ().get ("type").getAsString ();

        switch (type) {
            case "TASK":
                return gson.fromJson (body, Task.class);
            case "SUBTASK":
                return gson.fromJson (body, SubTask.class);
            case "EPIC_TASK":
                return gson.fromJson (body, EpicTask.class);
            default:
                throw new RequestException ("Unchecked case " + type);
        }
    }

    /*Обработка метода GET
      http://localhost:8080/tasks/task             вывод всех задач
      http://localhost:8080/tasks/subtask          вывод всех подзадач
      http://localhost:8080/tasks/epic             вывод всех эпиков

      http://localhost:8080/tasks/                 вывод полного списка всех типов задач

      http://localhost:8080/tasks/task?id=4        вывод задачи №4
      http://localhost:8080/tasks/subtask?id=2     вывод подзадачи №2
      http://localhost:8080/tasks/epic?id=1        вывод эпика №1
    */
    private String handleGetRequest (HttpExchange httpExchange) throws TaskException, RequestException {
        String response = "";
        URI uri = httpExchange.getRequestURI ();
        String query = httpExchange.getRequestURI ().getQuery ();
        OptionalInt id;

        if (query == null && uri.getPath ().endsWith ("/task")) {
            return gson.toJson (taskManager.getTasks ());
        } else if (query == null && uri.getPath ().endsWith ("/subtask")) {
            return gson.toJson (taskManager.getSubTasks ());
        } else if (query == null && uri.getPath ().endsWith ("/epic")) {
            return gson.toJson (taskManager.getEpicTasks ());
        }

        id = QueryParametersGetter.getId (uri);
        if (id.isPresent ()) {
            if (uri.getRawPath ().endsWith ("/task")) {
                Task task = taskManager.searchTaskWithId (id.getAsInt ());
                if (task == null) {
                    throw new TaskException ("Задача c id=" + id.getAsInt () + " не найдена");
                }
                response = gson.toJson (task);
            }
            if (uri.getRawPath ().endsWith ("/subtask")) {
                SubTask subTask = taskManager.searchSubTaskWithId (id.getAsInt ());
                if (subTask == null) {
                    throw new TaskException ("Подзадача c id=" + id.getAsInt () + " не найдена");
                }
                response = gson.toJson (subTask);
            }
            if (uri.getRawPath ().endsWith ("/epic")) {
                EpicTask epic = taskManager.searchEpicWithId (id.getAsInt ());
                if (epic == null) {
                    throw new TaskException ("Эпик c id=" + id.getAsInt () + " не найдена");
                }
                response = gson.toJson (epic);
            }
        } else {
            throw new RequestException ("Параметр id в строке запроса не был найден");
        }
        return response;
    }

    /*Обработка метода POST
      {
		"name": "task4",
		"description": "task4",
		"status": "new",
		"identifier": 6,
		"type": "TASK",
		"duration": 10,
		"startTime": "2023-04-13T12:15:30"
	   } тело задачи

      http://localhost:8080/tasks/task     header: X-context, value: update = обновление задачи
      http://localhost:8080/tasks/subtask  header: X-context, value: update = обновление подзадачи
      http://localhost:8080/tasks/epic     header: X-context, value: update = обновление эпика

      http://localhost:8080/tasks/task     header: X-context, value: create = создание задачи
      http://localhost:8080/tasks/subtask  header: X-context, value: create = создание подзадачи
      http://localhost:8080/tasks/epic     header: X-context, value: create = создание эпика

    */
    private void handlePostRequest (HttpExchange httpExchange) throws IllegalHeaderException, IOException, RequestException, TaskException {
        URI uri = httpExchange.getRequestURI ();
        List<String> context = httpExchange.getRequestHeaders ().get ("X-context");
        InputStream inputStream = httpExchange.getRequestBody ();
        String body = new String (inputStream.readAllBytes (), StandardCharsets.UTF_8);

        if (context == null) {
            throw new IllegalHeaderException ("Нет заголовка 'X-context'");
        }

        Task task = parseJsonToTask (body);
        if (uri.getRawPath ().endsWith ("/task")) {
            if (context.contains ("create")) {
                taskManager.createTask (task);
                if (!taskManager.getTasks ().contains (task)) {
                    throw new TaskException ("Не удалось добавить новую задачу с id=" + task.getIdentifier () +
                            ", возможно, она была добавлена ранее");
                }
            } else if (context.contains ("update")) {
                Task prevTask = taskManager.searchTaskWithId (task.getIdentifier ());
                if (prevTask != null) {
                    taskManager.updateTask (task);
                } else {
                    throw new TaskException ("Не удалось обновить задачу c id=" + task.getIdentifier () +
                            ". Проверьте, была ли ранее создана задача с данным ИД");
                }
            }
        }
        if (uri.getRawPath ().endsWith ("/subtask")) {
            SubTask subTask = (SubTask) task;
            if (context.contains ("create")) {
                taskManager.createTask (subTask);
                if (!taskManager.getTasks ().contains (subTask)) {
                    throw new TaskException ("Не удалось добавить новую задачу с id=" + subTask.getIdentifier () +
                            ", возможно, она была добавлена ранее");
                }
            } else if (context.contains ("update")) {
                SubTask prevTask = taskManager.searchSubTaskWithId (subTask.getIdentifier ());
                if (prevTask != null) {
                    taskManager.updateSubTask (subTask);
                } else {
                    throw new TaskException ("Не удалось обновить задачу c id=" + subTask.getIdentifier () +
                            ". Проверьте, была ли ранее создана задача с данным ИД");
                }
            }
        }
        if (uri.getRawPath ().endsWith ("/epic")) {
            EpicTask epicTask = (EpicTask) task;
            if (context.contains ("create")) {
                taskManager.createTask (epicTask);
                if (!taskManager.getTasks ().contains (epicTask)) {
                    throw new TaskException ("Не удалось добавить новую задачу с id=" + epicTask.getIdentifier () +
                            ", возможно, она была добавлена ранее");
                }
            } else if (context.contains ("update")) {
                EpicTask prevTask = taskManager.searchEpicWithId (epicTask.getIdentifier ());
                if (prevTask != null) {
                    taskManager.updateEpicTask (epicTask);
                } else {
                    throw new TaskException ("Не удалось обновить задачу c id=" + epicTask.getIdentifier () +
                            ". Проверьте, была ли ранее создана задача с данным ИД");
                }
            }
        }

    }

    /*Обработка метода DELETE
      http://localhost:8080/tasks/task             удаление всех задач
      http://localhost:8080/tasks/subtask          удаление всех подзадач
      http://localhost:8080/tasks/epic             удаление всех эпиков

      http://localhost:8080/tasks/task?id=4        удаление задачи №4
      http://localhost:8080/tasks/subtask?id=2     удаление подзадачи №2
      http://localhost:8080/tasks/epic?id=1        удаление эпика №1
    */
    private void handleDeleteRequest (HttpExchange httpExchange) throws TaskException {
        URI uri = httpExchange.getRequestURI ();
        String query = httpExchange.getRequestURI ().getQuery ();
        OptionalInt id;

        if (query == null && uri.getPath ().endsWith ("/task")) {
            taskManager.clearTasks ();
        } else if (query == null && uri.getPath ().endsWith ("/subtask")) {
            taskManager.clearSubTasks ();
        } else if (query == null && uri.getPath ().endsWith ("/epic")) {
            taskManager.clearEpics ();
        }

        id = QueryParametersGetter.getId (uri);

        if (id.isPresent () && uri.getRawPath ().endsWith ("/task")) {
            Task prevTask = taskManager.searchTaskWithId (id.getAsInt ());
            if (prevTask != null) {
                taskManager.removeTaskWithId (id.getAsInt ());
            } else {
                throw new TaskException ("Не удалось удалить задачу c id=" + id.getAsInt () +
                        ". Проверьте, была ли ранее создана задача с данным ИД");
            }
        }
        if (id.isPresent () && uri.getRawPath ().endsWith ("/subtask")) {
            SubTask prevTask = taskManager.searchSubTaskWithId (id.getAsInt ());
            if (prevTask != null) {
                taskManager.removeSubTaskWithId (id.getAsInt ());
            } else {
                throw new TaskException ("Не удалось удалить задачу c id=" + id.getAsInt () +
                        ". Проверьте, была ли ранее создана задача с данным ИД");
            }
        }
        if (id.isPresent () && uri.getRawPath ().endsWith ("/epic")) {
            EpicTask prevTask = taskManager.searchEpicWithId (id.getAsInt ());
            if (prevTask != null) {
                taskManager.removeEpicTaskWithId (id.getAsInt ());
            } else {
                throw new TaskException ("Не удалось удалить задачу c id=" + id.getAsInt () +
                        ". Проверьте, была ли ранее создана задача с данным ИД");
            }
        }
    }
}
