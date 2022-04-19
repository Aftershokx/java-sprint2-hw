package tasktracker.manager;

import tasktracker.tasks.*;
import tasktracker.utility.exceptions.ManagerSaveException;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class FileBackedTasksManager extends InMemoryTaskManager {

    private static final String path = "src/tasktracker/files/autosave.csv";

    public static void main (String[] args) {
        FileBackedTasksManager fileBackedTasksManager = new FileBackedTasksManager ();
        Task firstTask = new Task ("test1", "test1", Status.NEW);
        fileBackedTasksManager.createTask (firstTask);                          //Создание 1й задачи
        Task secondTask = new Task ("test2", "test2", Status.NEW);
        fileBackedTasksManager.createTask (secondTask);                        //Создание 2й задачи
        EpicTask firstEpic = new EpicTask ("test3", "test3");
        fileBackedTasksManager.createEpicTask (firstEpic);                     //Создание 1го эпика
        SubTask firstSubTask = new SubTask ("test4", "test4", Status.NEW, firstEpic.getIdentifier ());
        fileBackedTasksManager.createSubTask (firstSubTask);                   //Создание 1й сабтаски 1 эпика
        SubTask secondSubTask = new SubTask ("test5", "test5", Status.NEW, firstEpic.getIdentifier ());
        fileBackedTasksManager.createSubTask (secondSubTask);                  //Создание 2й сабтаски 1 эпика
        SubTask thirdSubTask = new SubTask ("test6", "test6", Status.NEW, firstEpic.getIdentifier ());
        fileBackedTasksManager.createSubTask (thirdSubTask);                   //Создание 3й сабтаски 1 эпика
        EpicTask secondEpic = new EpicTask ("test7", "test7");
        fileBackedTasksManager.createEpicTask (secondEpic);                    //Создание 2 эпика

        fileBackedTasksManager.searchEpicWithId (3);                         //Просматриваю Эпик
        fileBackedTasksManager.searchEpicWithId (7);                         //Просматриваю Эпик
        fileBackedTasksManager.searchSubTaskWithId (4);                      //Просматриваю сабтаск
        fileBackedTasksManager.searchSubTaskWithId (4);                      //Просматриваю сабтаск
        fileBackedTasksManager.searchSubTaskWithId (5);                      //Просматриваю сабтаск
        fileBackedTasksManager.searchSubTaskWithId (6);                      //Просматриваю сабтаск
        fileBackedTasksManager.searchTaskWithId (2);                         //Просматриваю таск
        fileBackedTasksManager.searchTaskWithId (1);                         //Просматриваю таск
        fileBackedTasksManager.searchTaskWithId (2);                         //Просматриваю таск

        System.out.println ("Вывожу историю просмотров до загрузки из файла: ");
        System.out.println (fileBackedTasksManager.history ());              //Вывожу историю просмотров
        System.out.println ("Эпики до загрузки: ");
        System.out.println (fileBackedTasksManager.getEpicTasks ());
        System.out.println ("Таски до загрузки: ");
        System.out.println (fileBackedTasksManager.getTasks ());
        System.out.println ("Сабтаски до загрузки: ");
        System.out.println (fileBackedTasksManager.getSubTasks ());

        FileBackedTasksManager fileBackedTasksManager2 = loadFromFile (new File (path));
        System.out.println ("Вывожу историю просмотров после загрузки из файла: ");
        System.out.println (fileBackedTasksManager2.history ());
        System.out.println ("Эпики после загрузки: ");
        System.out.println (fileBackedTasksManager2.getEpicTasks ());
        System.out.println ("Таски после загрузки: ");
        System.out.println (fileBackedTasksManager2.getTasks ());
        System.out.println ("Сабтаски после загрузки: ");
        System.out.println (fileBackedTasksManager2.getSubTasks ());

    }

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
                        if (task.getType ().equals (Types.TASK)) {
                            fileBackedTasksManager.tasks.put (task.getIdentifier (), task);
                        }
                        if (task.getType ().equals (Types.EPIC_TASK)) {
                            fileBackedTasksManager.epicTasks.put (task.getIdentifier (), (EpicTask) task);
                        }
                        if (task.getType ().equals (Types.SUBTASK)) {
                            fileBackedTasksManager.subTasks.put (task.getIdentifier (), (SubTask) task);
                            if (!fileBackedTasksManager.epicTasks.isEmpty ()) {
                                for (Integer id : fileBackedTasksManager.epicTasks.keySet ()) {
                                    if (id == fileBackedTasksManager.subTasks.get (task.getIdentifier ()).getEpicIdentifier ()) {
                                        fileBackedTasksManager.epicTasks.get (id).addSubtask ((SubTask) task);
                                    }
                                }
                            }
                        }
                    }
                } else {
                    String newLine = br.readLine ();
                    List<Integer> idHistory = fromStringHistory (newLine);
                    Task task;
                    for (Integer id : idHistory) {
                        if (fileBackedTasksManager.tasks.containsKey (id)) {
                            task = fileBackedTasksManager.tasks.get (id);
                            fileBackedTasksManager.historyManager.add (task);
                        } else if (fileBackedTasksManager.epicTasks.containsKey (id)) {
                            task = fileBackedTasksManager.epicTasks.get (id);
                            fileBackedTasksManager.historyManager.add (task);
                        } else if (fileBackedTasksManager.subTasks.containsKey (id)) {
                            task = fileBackedTasksManager.subTasks.get (id);
                            fileBackedTasksManager.historyManager.add (task);
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
    public void createEpicTask (EpicTask epicTask) {
        super.createEpicTask (epicTask);
        save ();
    }

    @Override
    public void createSubTask (SubTask subTask) {
        super.createSubTask (subTask);
        save ();
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
    public List<Task> history () {
        save ();
        return super.history ();
    }

    @Override
    public ArrayList<EpicTask> getEpicTasks () {
        save ();
        return super.getEpicTasks ();
    }

    @Override
    public ArrayList<SubTask> getSubTasks () {
        save ();
        return super.getSubTasks ();
    }

    @Override
    public ArrayList<Task> getTasks () {
        save ();
        return super.getTasks ();
    }

    @Override
    public int updateId () {
        return super.updateId ();
    }

    //Сохранение в файл
    private void save () {
        try (Writer fileWriterStart = new FileWriter (path)) {
            fileWriterStart.write ("id,type,name,status,description,epic\n");

            for (Task task : tasks.values ()) {
                fileWriterStart.write (toString (task) + "\n");
            }
            for (EpicTask epic : epicTasks.values ()) {
                fileWriterStart.write (toString (epic) + "\n");
            }
            for (SubTask subtask : subTasks.values ()) {
                fileWriterStart.write (toString (subtask) + "\n");
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