package tasktracker.managers;


import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import tasktracker.server.KVTaskClient;
import tasktracker.tasks.EpicTask;
import tasktracker.tasks.SubTask;
import tasktracker.tasks.Task;
import tasktracker.tasks.Types;
import tasktracker.utility.exceptions.RequestException;

import java.io.IOException;
import java.util.ArrayList;


public class HttpTaskManager extends FileBackedTasksManager {
    private final KVTaskClient kvTaskClient;
    private final Gson gson;

    public HttpTaskManager (String url) {
        this.kvTaskClient = new KVTaskClient (url);
        this.gson = Managers.createDefaultGson ();
        try {
            this.loadFromServer ();
        } catch (IOException | InterruptedException | RequestException e) {
            e.printStackTrace ();
        }
    }

    //Сохранение на сервере
    @Override
    protected void save () {
        ArrayList<Task> tasks = new ArrayList<> (getTasks ());
        tasks.addAll (getSubTasks ());
        tasks.addAll (getEpicTasks ());
        String tasksJson = gson.toJson (tasks);
        String historyJson = gson.toJson (history ());

        try {
            kvTaskClient.put ("tasks", tasksJson);
            kvTaskClient.put ("history", historyJson);
        } catch (RequestException | IOException | InterruptedException e) {
            e.printStackTrace ();
        }

    }

    //Загрузка с сервера
    public void loadFromServer () throws IOException, InterruptedException, RequestException {
        String tasksJson = kvTaskClient.load ("tasks");
        String historyJson = kvTaskClient.load ("history");
        if (!tasksJson.isEmpty ()) {

            ArrayList<Task> tasks = parseJsonToTasksList (tasksJson);
            ArrayList<Task> history = parseJsonToTasksList (historyJson);

            HistoryManager historyManager = new InMemoryHistoryManager ();
            for (Task task : tasks) {
                if (task.getType ().equals (Types.EPIC_TASK)) {
                    super.createTask (task);
                    super.searchEpicWithId (task.getIdentifier ()).RemoveAllSubTasks ();
                }
            }
            tasks.removeAll (super.getEpicTasks ());
            tasks.forEach (super::createTask);

            int lastId = 0;

            for (Task task : super.tasks) {
                if (task.getIdentifier () > lastId) {
                    lastId = task.getIdentifier ();
                    super.uniId = lastId;
                }
            }

            history.forEach (historyManager::add);

            setHistoryManager (historyManager);
        }
    }

    //Преобразования Json в список Задач
    private ArrayList<Task> parseJsonToTasksList (String json) {
        ArrayList<Task> result = new ArrayList<> ();
        JsonElement jsonElement = JsonParser.parseString (json);
        JsonArray jsonArray = jsonElement.getAsJsonArray ();

        for (JsonElement element : jsonArray) {
            String type = element.getAsJsonObject ().get ("type").getAsString ();
            switch (type) {
                case "TASK":
                    result.add (gson.fromJson (element, Task.class));
                    break;
                case "SUBTASK":
                    result.add (gson.fromJson (element, SubTask.class));
                    break;
                case "EPIC_TASK":
                    result.add (gson.fromJson (element, EpicTask.class));
                    break;
            }
        }
        return result;
    }
}