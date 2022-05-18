package tasktracker.managers;

import tasktracker.tasks.*;
import tasktracker.utility.exceptions.ManagerSaveException;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

public class FileBackedTasksManager extends InMemoryTaskManager {

    private static final String path = "src/tasktracker/files/autosave.csv";

    //Перевод строки в таски
    private static Task fromString (String value) {
        String[] components = value.split (",");
        String type = (components[1]);
        Task task = null;
        if (type.equals ("TASK")) {
            task = new Task (components[2], components[4], Status.valueOf (components[3]));
            task.setIdentifier (Integer.parseInt (components[0]));
            task.setType (Types.TASK);
        }
        if (type.equals ("EPIC_TASK")) {
            task = new EpicTask (components[2], components[4]);
            task.setIdentifier (Integer.parseInt (components[0]));
            task.setStatus (Status.valueOf (components[3]));
            task.setType (Types.EPIC_TASK);
        }
        if (type.equals ("SUBTASK")) {
            task = new SubTask (components[2], components[4], Status.valueOf (components[3]),
                    Integer.parseInt (components[5]));
            task.setIdentifier (Integer.parseInt (components[0]));
            task.setType (Types.SUBTASK);
        }
        return task;
    }

    //Перевод истории в строку
    private static String toStringHistory (HistoryManager manager) {
        List<Task> history = manager.getHistory ();
        StringBuilder historyId = new StringBuilder ();
        historyId.append ("\n");
        for (Task task : history) {
            historyId.append (String.format ("%d,", task.getIdentifier ()));
        }
        return historyId.toString ();
    }

    //Перевод строки в историю
    private static List<Integer> fromStringHistory (String value) {
        List<Integer> history = new ArrayList<> ();
        String[] historyIds = value.split (",");
        for (String id : historyIds) {
            history.add (Integer.parseInt (id));
        }
        return history;
    }

    //Загрузка из файла
    public static FileBackedTasksManager loadFromFile (File file) {
        FileBackedTasksManager fileBackedTasksManager = new FileBackedTasksManager ();
        try (BufferedReader br = new BufferedReader (new FileReader (file, StandardCharsets.UTF_8))) {
            while (br.ready ()) {
                String line = br.readLine ();
                if (!line.isBlank ()) {
                    if (!line.equals ("id,type,name,status,description,epic")) {
                        Task task = fromString (line);
                        fileBackedTasksManager.tasks.add (task);
                        fileBackedTasksManager.updateId (task);
                        if (task.getType ().equals (Types.SUBTASK)) {
                            SubTask subTask = (SubTask) task;
                            for (Task testTask : fileBackedTasksManager.tasks) {
                                if (testTask.getIdentifier () == subTask.getEpicIdentifier ()) {
                                    EpicTask epicTask = (EpicTask) testTask;
                                    epicTask.addSubtask (subTask);
                                }
                            }
                        }
                    }
                } else {
                    String newLine = br.readLine ();
                    List<Integer> idHistory = fromStringHistory (newLine);
                    Task task;
                    for (Integer id : idHistory) {
                        for (Task currTask : fileBackedTasksManager.tasks) {
                            if (currTask.getType ().equals (Types.TASK)) {
                                task = fileBackedTasksManager.searchTaskWithId (id);
                                fileBackedTasksManager.historyManager.add (task);
                            } else if (currTask.getType ().equals (Types.EPIC_TASK)) {
                                task = fileBackedTasksManager.searchEpicWithId (id);
                                fileBackedTasksManager.historyManager.add (task);
                            } else if (currTask.getType ().equals (Types.SUBTASK)) {
                                task = fileBackedTasksManager.searchSubTaskWithId (id);
                                fileBackedTasksManager.historyManager.add (task);
                            }
                        }
                    }
                }
            }
        } catch (
                IOException e) {
            e.printStackTrace ();
        }
        return fileBackedTasksManager;
    }

    @Override
    public void createTask (Task task) {
        super.createTask (task);
        save ();
    }

    @Override
    public EpicTask searchEpicWithId (int id) {
        save ();
        return super.searchEpicWithId (id);
    }

    @Override
    public SubTask searchSubTaskWithId (int id) {
        save ();
        return super.searchSubTaskWithId (id);
    }

    @Override
    public Task searchTaskWithId (int id) {
        save ();
        return super.searchTaskWithId (id);
    }

    @Override
    public void clearEpics () {
        super.clearEpics ();
        save ();
    }

    @Override
    public void clearSubTasks () {
        super.clearSubTasks ();
        save ();
    }

    @Override
    public void clearTasks () {
        super.clearTasks ();
        save ();
    }

    @Override
    public void removeSubTaskWithId (int id) {
        super.removeSubTaskWithId (id);
        save ();
    }

    @Override
    public void removeTaskWithId (int id) {
        super.removeTaskWithId (id);
        save ();
    }

    @Override
    public void removeEpicTaskWithId (int id) {
        super.removeEpicTaskWithId (id);
        save ();
    }

    @Override
    public void updateEpicTask (EpicTask epicTask) {
        super.updateEpicTask (epicTask);
        save ();
    }

    @Override
    public void updateTask (Task task) {
        super.updateTask (task);
        save ();
    }

    @Override
    public void updateSubTask (SubTask subTask) {
        super.updateSubTask (subTask);
        save ();
    }

    @Override
    public int updateId (Task task) {
        return super.updateId (task);
    }

    @Override
    public TreeSet<Task> getPrioritizedTasks () {
        return super.getPrioritizedTasks ();
    }

    //Сохранение в файл
    protected void save () {
        try (Writer fileWriterStart = new FileWriter (path)) {
            fileWriterStart.write ("id,type,name,status,description,epic\n");
            for (Task task : tasks) {
                fileWriterStart.write (toString (task) + "\n");
            }
            if (historyManager.getHistory () != null)
                fileWriterStart.write (toStringHistory (historyManager));
        } catch (IOException e) {
            throw new ManagerSaveException ("При попытке сохранения произошла ошибка");
        }
    }

    //Перевод тасок в строку
    private String toString (Task task) {
        String result;
        if (task.getType ().equals (Types.TASK) || task.getType ().equals (Types.EPIC_TASK)) {
            result = String.format ("%d,%s,%s,%s,%s",
                    task.getIdentifier (),
                    task.getType (),
                    task.getName (),
                    task.getStatus (),
                    task.getDescription ()
            );
        } else {
            result = String.format ("%d,%s,%s,%s,%s,%s",
                    task.getIdentifier (),
                    task.getType (),
                    task.getName (),
                    task.getStatus (),
                    task.getDescription (),
                    ((SubTask) task).getEpicIdentifier ()
            );
        }
        return result;
    }

}