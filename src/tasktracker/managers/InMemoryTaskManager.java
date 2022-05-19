package tasktracker.managers;

import tasktracker.tasks.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;
import java.util.function.Predicate;

public class InMemoryTaskManager implements TaskManager {
    protected int uniId = 0;
    protected HistoryManager historyManager = Managers.getDefaultHistory ();
    protected final TreeSet<Task> tasks = new TreeSet<> ((o1, o2) -> {
        if (o1.getIdentifier () == o2.getIdentifier ()) {
            return 0;
        }
        if (o1.getStartTime () == null && o2.getStartTime () == null) {
            return 1;
        }
        if (o1.getStartTime () == null && o2.getStartTime () != null) {
            return 1;
        }
        if (o1.getStartTime () != null && o2.getStartTime () == null) {
            return -1;
        }
        if (o1.getStartTime ().equals (o2.getStartTime ())) {
            return 1;
        }
        if (o1.getStartTime ().isBefore (o2.getStartTime ())) {
            return -1;
        }
        if (o1.getStartTime ().isAfter (o2.getStartTime ())) {
            return 1;
        }
        return 0;
    });

    protected void setHistoryManager (HistoryManager historyManager) {
        this.historyManager = historyManager;
    }

    //Счетчик
    public int updateId (Task task) {
        List<Integer> currentIds = new ArrayList<> ();
        for (Task task1 : tasks) {
            currentIds.add (task1.getIdentifier ());
        }
        if (task.getIdentifier () > 0) {
            uniId = task.getIdentifier ();
            currentIds.add (uniId);
        } else {
            int maxId = 0;
            for (Integer currentId : currentIds) {
                if (currentId > maxId) {
                    maxId = currentId;
                }
            }
            uniId = maxId + 1;
        }

        if (uniId == 0) {
            uniId++;
        }
        return uniId;
    }

    //Проверка пересечения по времени
    private final Predicate<Task> intersectionCheck = newTask -> {
        if (newTask.getStartTime () == null) {
            return true;
        }

        LocalDateTime newTaskStart = newTask.getStartTime ();
        LocalDateTime newTaskFinish = newTask.getEndTime ();

        for (Task task : tasks) {
            if (task.getStartTime () == null) {
                break;
            }

            LocalDateTime taskStart = task.getStartTime ();
            LocalDateTime taskFinish = task.getEndTime ();

            if (newTaskStart.isBefore (taskStart) && newTaskFinish.isAfter (taskStart)) {
                return false;
            }

            if (newTaskStart.isBefore (taskFinish) && newTaskFinish.isAfter (taskFinish)) {
                return false;
            }

            if ((newTaskStart.isBefore (taskStart) && newTaskFinish.isBefore (taskStart)) &&
                    (newTaskStart.isBefore (taskFinish) && newTaskFinish.isBefore (taskFinish))) {
                break;
            }
        }
        return true;
    };

    //Распаковка эпиков
    @Override
    public ArrayList<EpicTask> getEpicTasks () {
        ArrayList<EpicTask> epicTasks = new ArrayList<> ();
        for (Task task : tasks) {
            if (task.getType ().equals (Types.EPIC_TASK)) {
                epicTasks.add ((EpicTask) task);
            }
        }
        return epicTasks;
    }

    //Распаковка подзадач
    @Override
    public ArrayList<SubTask> getSubTasks () {
        ArrayList<SubTask> subTasks = new ArrayList<> ();
        for (Task task : tasks) {
            if (task.getType ().equals (Types.SUBTASK)) {
                subTasks.add ((SubTask) task);
            }
        }
        return subTasks;
    }

    //Распаковка задач
    @Override
    public ArrayList<Task> getTasks () {
        ArrayList<Task> tasksList = new ArrayList<> ();
        for (Task task : tasks) {
            if (task.getType ().equals (Types.TASK)) {
                tasksList.add (task);
            }
        }
        return tasksList;
    }

    //Создание задач
    @Override
    public void createTask (Task task) {
        if (task != null) {
            if (intersectionCheck.test (task)) {
                if (task.getType () == null || task.getType () == Types.TASK) {
                    task.setType (Types.TASK);
                    task.setIdentifier (updateId (task));
                    tasks.add (task);
                } else if (task.getType ().equals (Types.EPIC_TASK)) {
                    EpicTask epicTask = (EpicTask) task;
                    epicTask.updateEpicStatus ();
                    epicTask.setIdentifier (updateId (task));
                    tasks.add (epicTask);
                } else if (task.getType ().equals (Types.SUBTASK)) {
                    SubTask subTask = (SubTask) task;
                    EpicTask prevEpic = searchEpicWithId (subTask.getEpicIdentifier ());
                    if (prevEpic != null) {
                        if (subTask.getStatus () == Status.NEW) {
                            subTask.setIdentifier (updateId (task));
                        }
                        prevEpic.addSubtask (subTask);
                        tasks.add (subTask);
                    }
                }
            }
        }
    }

    //Поиск эпика по Ид и добавление в историю
    @Override
    public EpicTask searchEpicWithId (int id) {
        for (Task task : tasks) {
            if (task.getIdentifier () == id) {
                if (task.getType ().equals (Types.EPIC_TASK)) {
                    EpicTask epicTask = (EpicTask) task;
                    historyManager.add (epicTask);
                    return epicTask;
                }
            }
        }
        return null;
    }

    //Поиск подзадачи по Ид и добавление в историю
    @Override
    public SubTask searchSubTaskWithId (int id) {
        for (Task task : tasks) {
            if (task.getIdentifier () == id) {
                if (task.getType ().equals (Types.SUBTASK)) {
                    SubTask subTask = (SubTask) task;
                    historyManager.add (subTask);
                    return subTask;
                }
            }
        }
        return null;
    }

    //Поиск задачи по Ид и добавление в историю
    @Override
    public Task searchTaskWithId (int id) {
        for (Task task : tasks) {
            if (task.getIdentifier () == id) {
                if (task.getType ().equals (Types.TASK)) {
                    historyManager.add (task);
                    return task;
                }
            }
        }
        return null;
    }

    //Удаление всех эпиков
    @Override
    public void clearEpics () {
        List<Integer> epicId = new ArrayList<> ();
        for (Task task : tasks) {
            if (task.getType ().equals (Types.EPIC_TASK)) {
                epicId.add (task.getIdentifier ());
            }
        }
        for (Integer integer : epicId) {
            removeEpicTaskWithId (integer);
        }
    }

    //Удаление всех подзадач
    @Override
    public void clearSubTasks () {
        List<Integer> subId = new ArrayList<> ();
        for (Task task : tasks) {
            if (task.getType ().equals (Types.SUBTASK)) {
                subId.add (task.getIdentifier ());
            }
        }
        for (Integer integer : subId) {
            removeSubTaskWithId (integer);
        }
    }

    //Удаление всех задач
    @Override
    public void clearTasks () {
        List<Integer> taskId = new ArrayList<> ();
        for (Task task : tasks) {
            if (task.getType ().equals (Types.TASK)) {
                taskId.add (task.getIdentifier ());
            }
        }
        for (Integer integer : taskId) {
            removeTaskWithId (integer);
        }
    }

    //Удаление подзадач по ИД
    @Override
    public void removeSubTaskWithId (int id) {
        SubTask subTask = searchSubTaskWithId (id);
        if (subTask != null) {
            EpicTask prevEpic = searchEpicWithId (subTask.getEpicIdentifier ());
            if (prevEpic != null) {
                prevEpic.removeSubtask (subTask);
                tasks.removeIf (subTask::equals);
                historyManager.remove (id);
            }
        }
    }

    //Удаление задач по ИД
    @Override
    public void removeTaskWithId (int id) {
        Task task = searchTaskWithId (id);
        if (task != null) {
            tasks.removeIf (task::equals);
            historyManager.remove (id);
        }
    }

    //Удаление Эпиков по ИД
    @Override
    public void removeEpicTaskWithId (int id) {
        EpicTask epicTask = searchEpicWithId (id);
        if (epicTask != null) {
            for (Integer subTaskId : epicTask.getSubTasksIds ()) {
                removeSubTaskWithId (subTaskId);
            }
            tasks.removeIf (task -> task.equals (epicTask));
            historyManager.remove (id);
        }
    }

    //Замена эпика
    @Override
    public void updateEpicTask (EpicTask epicTask) {
        if (epicTask != null) {
            EpicTask prevEpic = searchEpicWithId (epicTask.getIdentifier ());
            List<SubTask> subTasks = new ArrayList<> (epicTask.getSubTasks ());
            if (intersectionCheck.test (epicTask)) {
                removeEpicTaskWithId (prevEpic.getIdentifier ());
                tasks.add (epicTask);
                tasks.addAll (subTasks);
                for (SubTask subTask : subTasks) {
                    epicTask.addSubtask (subTask);
                }
            }
        }
    }

    //Замена задачи
    public void updateTask (Task task) {
        if (task != null) {
            if (intersectionCheck.test (task)) {
                removeTaskWithId (task.getIdentifier ());
                tasks.add (task);
            }
        }
    }

    //Замена подзадачи
    @Override
    public void updateSubTask (SubTask subTask) {
        if (subTask != null) {
            if (intersectionCheck.test (subTask)) {
                EpicTask prevEpic = searchEpicWithId (subTask.getEpicIdentifier ());
                SubTask prevSubTask = searchSubTaskWithId (subTask.getIdentifier ());
                if (prevSubTask != null) {
                    removeSubTaskWithId (prevSubTask.getIdentifier ());
                    tasks.add (subTask);
                    prevEpic.addSubtask (subTask);
                    prevEpic.updateEpicStatus ();
                }
            }
        }
    }

    // Получение списка истории
    @Override
    public List<Task> history () {
        return historyManager.getHistory ();
    }

    // Получение списка отсортированных задач
    @Override
    public TreeSet<Task> getPrioritizedTasks () {
        return tasks;
    }

}


