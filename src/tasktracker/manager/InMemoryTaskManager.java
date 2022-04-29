package tasktracker.manager;

import tasktracker.tasks.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;
import java.util.function.Predicate;


public class InMemoryTaskManager implements TaskManager {
    int uniId = 0;
    protected final HistoryManager historyManager = Managers.getDefaultHistory ();
    protected final TreeSet<Task> tasks = new TreeSet<> ();


    public int updateId () {                                                  //Счетчик
        int updatedId = uniId + 1;
        uniId = updatedId;
        return updatedId;
    }

    private final Predicate<Task> startTimeCheck = newTask -> {
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

    @Override
    public ArrayList<EpicTask> getEpicTasks () {                  //Распаковка эпиков
        ArrayList<EpicTask> epicTasks = new ArrayList<> ();
        for (Task task : tasks) {
            if (task.getType ().equals (Types.EPIC_TASK)) {
                epicTasks.add ((EpicTask) task);
            }
        }
        return epicTasks;
    }

    @Override
    public ArrayList<SubTask> getSubTasks () {                    //Распаковка подзадач
        ArrayList<SubTask> subTasks = new ArrayList<> ();
        for (Task task : tasks) {
            if (task.getType ().equals (Types.SUBTASK)) {
                subTasks.add ((SubTask) task);
            }
        }
        return subTasks;
    }

    @Override
    public ArrayList<Task> getTasks () {                          //Распаковка задач
        ArrayList<Task> tasksList = new ArrayList<> ();
        for (Task task : tasks) {
            if (task.getType ().equals (Types.TASK)) {
                tasksList.add (task);
            }
        }
        return tasksList;
    }

    @Override
    public void createTask (Task task) {
        if (task != null) {
            if (task.getType ().equals (Types.EPIC_TASK)) {
                EpicTask epicTask = (EpicTask) task;
                epicTask.updateEpicStatus ();
                epicTask.setIdentifier (updateId ());
                tasks.add (epicTask);
            } else if (task.getType ().equals (Types.SUBTASK)) {
                SubTask subTask = (SubTask) task;
                EpicTask prevEpic = searchEpicWithId (subTask.getEpicIdentifier ());
                if (prevEpic != null) {
                    if (subTask.getStatus () == Status.NEW) {
                        subTask.setIdentifier (updateId ());
                    }
                    prevEpic.addSubtask (subTask);
                    tasks.add (subTask);
                }
            } else {
                task.setType (Types.TASK);
                task.setIdentifier (updateId ());
                tasks.add (task);
            }
        }

   /* @Override
    public void createEpicTask (EpicTask epicTask) {                                 //создание Эпика
        if (epicTask != null) {
            epicTask.setType (Types.EPIC_TASK);
            epicTask.updateEpicStatus ();
            if (epicTask.getStatus () == Status.NEW) {
                epicTask.setIdentifier (updateId ());
            }
            epicTasks.put (epicTask.getIdentifier (), epicTask);
        }
    }

    @Override
    public void createSubTask (SubTask subTask) {                                   //Создание подзадач
        if (subTask != null) {
            subTask.setType (Types.SUBTASK);
            EpicTask prevEpic = searchEpicWithId (subTask.getEpicIdentifier ());
            if (prevEpic != null) {
                if (subTask.getStatus () == Status.NEW) {
                    subTask.setIdentifier (updateId ());

                }
                prevEpic.addSubtask (subTask);
                subTasks.put (subTask.getIdentifier (), subTask);
            }
        }
    }

    @Override
    public void createTask (Task task) {                                            //Создание задач
        if (task != null) {
            task.setType (Types.TASK);
            if (task.getStatus () == Status.NEW) {
                task.setIdentifier (updateId ());
            }
            tasks.put (task.getIdentifier (), task);
        }
    }
    */


        @Override
        public EpicTask searchEpicWithId ( int id)
        {                                     //Поиск эпика по Ид и добавление в историю
            EpicTask epicTask = epicTasks.get (id);
            historyManager.add (epicTask);
            return epicTasks.get (id);
        }

        @Override
        public SubTask searchSubTaskWithId ( int id)
        {                                   //Поиск подзадачи по Ид и добавление в историю
            SubTask subTask = subTasks.get (id);
            historyManager.add (subTask);
            return subTasks.get (id);
        }

        @Override
        public Task searchTaskWithId ( int id)
        {                                         //Поиск задачи по Ид и добавление в историю
            Task task = tasks.get (id);
            historyManager.add (task);
            return tasks.get (id);
        }

        @Override
        public void clearEpics () {                                                     //Удаление всех эпиков
            epicTasks.clear ();
            clearSubTasks ();
        }

        @Override
        public void clearSubTasks () {                                                 //Удаление всех подзадач
            subTasks.clear ();
        }

        @Override
        public void clearTasks () {                                                    //Удадение всех задач
            tasks.clear ();
        }

        @Override
        public void removeSubTaskWithId ( int id){                                     //Удаление подзадач по ТД
            SubTask subTask = searchSubTaskWithId (id);
            if (subTask != null) {
                EpicTask prevEpic = searchEpicWithId (subTask.getEpicIdentifier ());
                if (prevEpic != null) {
                    prevEpic.removeSubtask (subTask);
                    subTasks.remove (id);
                    historyManager.remove (id);
                }
            }
        }

        @Override
        public void removeTaskWithId ( int id){                                           //Удаление задач по ИД
            Task task = searchTaskWithId (id);
            if (task != null) {
                tasks.remove (id);
                historyManager.remove (id);
            }
        }

        @Override
        public void removeEpicTaskWithId ( int id){                                      //Удаление Эпиков по ИД
            EpicTask epicTask = searchEpicWithId (id);
            if (epicTask != null) {
                for (Integer subTask : epicTask.getSubTasksIds ()) {
                    removeSubTaskWithId (subTask);
                }
                epicTasks.remove (id);
                historyManager.remove (id);
            }
        }

        @Override
        public void updateEpicTask (EpicTask epicTask){                                //Замена эпика
            if (epicTask != null) {
                epicTasks.put (epicTask.getIdentifier (), epicTask);
            }
        }

        public void updateTask (Task task){                                            //Замена задачи
            if (task != null) {
                tasks.put (task.getIdentifier (), task);
            }
        }

        @Override
        public void updateSubTask (SubTask subTask){                                   //Замена подзадачи
            if (subTask != null) {
                EpicTask prevEpic = searchEpicWithId (subTask.getEpicIdentifier ());
                SubTask prevSubTask = searchSubTaskWithId (subTask.getIdentifier ());
                if (prevSubTask != null) {
                    removeSubTaskWithId (prevSubTask.getIdentifier ());
                    createSubTask (subTask);
                    prevEpic.updateEpicStatus ();
                }
            }
        }

        @Override
        public List<Task> history () {                                      // Получение списка истории
            return historyManager.getHistory ();
        }

        @Override
        public void getPrioritizedTasks () {

        }
    }


