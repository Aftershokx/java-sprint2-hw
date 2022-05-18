package tasktracker.managers;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tasktracker.tasks.EpicTask;
import tasktracker.tasks.Status;
import tasktracker.tasks.SubTask;
import tasktracker.tasks.Task;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

abstract class TaskManagerTest<T extends TaskManager> {

    T tasksManager;
    Task firstTask;
    Task secondTask;
    EpicTask firstEpic;
    SubTask firstSubTask;
    SubTask secondSubTask;
    SubTask thirdSubTask;
    EpicTask secondEpic;

    abstract void preLoadTaskManager ();

    @BeforeEach
    void setUp () {
        preLoadTaskManager ();
        firstTask = new Task ("test1", "test1", Status.NEW);
        tasksManager.createTask (firstTask);
        secondTask = new Task ("test2", "test2", Status.NEW);
        tasksManager.createTask (secondTask);
        firstEpic = new EpicTask ("test3", "test3");
        tasksManager.createTask (firstEpic);
        firstSubTask = new SubTask ("test4", "test4", Status.NEW, firstEpic.getIdentifier ());
        tasksManager.createTask (firstSubTask);
        secondSubTask = new SubTask ("test5", "test5", Status.NEW, firstEpic.getIdentifier ());
        tasksManager.createTask (secondSubTask);
        thirdSubTask = new SubTask ("test6", "test6", Status.NEW, firstEpic.getIdentifier ());
        tasksManager.createTask (thirdSubTask);
        secondEpic = new EpicTask ("test7", "test7");
        tasksManager.createTask (secondEpic);
    }

    @AfterEach
    void afterEach () {
        tasksManager.clearTasks ();
        tasksManager.clearSubTasks ();
        tasksManager.clearEpics ();
    }

    @Test
    void createTasksPositiveReaction () {
        List<EpicTask> epicCheck = List.of (firstEpic);
        List<Task> taskCheck = List.of (firstTask, secondTask);
        List<SubTask> subTasksCheck = List.of (firstSubTask);

        Assertions.assertTrue (tasksManager.getEpicTasks ().containsAll (epicCheck),
                "Эпики были созданы не корректно");
        Assertions.assertTrue (tasksManager.getTasks ().containsAll (taskCheck),
                "Задачи были созданы не корректно");
        Assertions.assertTrue (tasksManager.getSubTasks ().containsAll (subTasksCheck),
                "Подзадачи были созданы не корректно");
    }

    @Test
    public void createAndUpdateEpicsWhenSubtasksIncluded () {
        EpicTask thirdEpic = new EpicTask ("3", "3");
        tasksManager.createTask (thirdEpic);
        assertTrue (thirdEpic.getSubTasks ().isEmpty (), "В эпики лишние подзадачи");
        SubTask testSubTask = new SubTask ("ttt", "ttt", Status.NEW, thirdEpic.getIdentifier ());
        tasksManager.createTask (testSubTask);
        assertTrue (tasksManager.getSubTasks ().contains (testSubTask),
                "Подзадача не была корректно сохранена менеджером");
        assertTrue (thirdEpic.getSubTasks ().contains (testSubTask),
                "Подзадача не была корректно добавлена в эпик");
        assertEquals (thirdEpic.getStatus (), Status.NEW, "Статус был передан эпику не корректно");
        testSubTask.setStatus (Status.IN_PROGRESS);
        tasksManager.updateSubTask (testSubTask);
        assertEquals (Status.IN_PROGRESS, thirdEpic.getStatus (),
                "При обновлении статуса подзадач не изменился статус эпика");
        testSubTask.setStartTime (String.valueOf (LocalDateTime.now ()));
        testSubTask.setDuration (10);
        tasksManager.updateSubTask (testSubTask);
        assertEquals (testSubTask.getStartTime (), thirdEpic.getStartTime (),
                "При обновлении времени начала выполнения подзадачи, " +
                        "время начала выполнения эпика не устанавливается");
        assertEquals (testSubTask.getDuration (), thirdEpic.getDuration (),
                "При обновлении продолжительности подзадачи, время продолжительности эпика не изменилось");
        assertEquals (testSubTask.getEndTime (), thirdEpic.getEndTime (), "Время эпика рассчиталось не корректно");
    }


    @Test
    void searchEpicWithIdPositiveReaction () {
        int id = firstEpic.getIdentifier ();
        assertEquals (firstEpic, tasksManager.searchEpicWithId (id), "Эпик не нашелся по ИД");
    }

    @Test
    void searchSubTaskWithIdPositiveReaction () {
        int id = firstSubTask.getIdentifier ();
        assertEquals (firstSubTask, tasksManager.searchSubTaskWithId (id), "Подзадача не нашлась по ИД");
    }

    @Test
    void searchTaskWithIdPositiveReaction () {
        int id = firstTask.getIdentifier ();
        assertEquals (firstTask, tasksManager.searchTaskWithId (id), "Задача не нашлась по ИД");
    }

    @Test
    void clearEpicsPositiveReaction () {
        List<EpicTask> epics = List.of (firstEpic, secondEpic);
        Assertions.assertTrue (tasksManager.getEpicTasks ().containsAll (epics),
                "Не все эпики добавлены корректно");
        tasksManager.clearEpics ();
        Assertions.assertTrue (tasksManager.getEpicTasks ().isEmpty (), "Список эпиков не пуст");
    }

    @Test
    void clearSubTasksPositiveReaction () {
        tasksManager.clearSubTasks ();
        Assertions.assertTrue (tasksManager.getSubTasks ().isEmpty (), "Список подзадач не пуст");
    }

    @Test
    void clearTasksPositiveReaction () {
        List<Task> tasks = List.of (firstTask, secondTask);
        Assertions.assertTrue (tasksManager.getTasks ().containsAll (tasks),
                "Не все задачи были добавлены корректно");
        tasksManager.clearTasks ();
        Assertions.assertTrue (tasksManager.getTasks ().isEmpty (), "Список задач не пуст");
    }

    @Test
    void removeSubTaskWithId () {
        List<SubTask> subTasks = List.of (firstSubTask, secondSubTask);
        Assertions.assertTrue (tasksManager.getSubTasks ().containsAll (subTasks),
                "Не все подзадачи были добавлены корректно");
        tasksManager.removeSubTaskWithId (firstSubTask.getIdentifier ());
        Assertions.assertTrue (tasksManager.getSubTasks ().contains (secondSubTask),
                "Удалилась не корректная подзадача");
        Assertions.assertFalse (tasksManager.getSubTasks ().contains (firstSubTask),
                "Удаленная подзадача осталась в списке");
    }

    @Test
    void removeTaskWithId () {
        List<Task> tasks = List.of (firstTask, secondTask);
        Assertions.assertTrue (tasksManager.getTasks ().containsAll (tasks),
                "Не все задачи были добавлены корректно");
        tasksManager.removeTaskWithId (firstTask.getIdentifier ());
        Assertions.assertTrue (tasksManager.getTasks ().contains (secondTask),
                "Удалилась не корректная задача");
        Assertions.assertFalse (tasksManager.getTasks ().contains (firstTask),
                "Удаленная задача осталась в списке");
    }

    @Test
    void removeEpicTaskWithId () {
        List<EpicTask> epicTasks = List.of (firstEpic, secondEpic);
        List<SubTask> subTasks = List.of (firstSubTask, secondSubTask);
        Assertions.assertTrue (tasksManager.getSubTasks ().containsAll (subTasks),
                "Не все подзадачи были добавлены корректно");
        Assertions.assertTrue (tasksManager.getEpicTasks ().containsAll (epicTasks),
                "Не все эпики были добавлены корректно");
        int id = firstEpic.getIdentifier ();
        tasksManager.removeEpicTaskWithId (id);
        Assertions.assertTrue (tasksManager.getEpicTasks ().contains (secondEpic),
                "Удалился не корректный эпик");
        Assertions.assertFalse (tasksManager.getEpicTasks ().contains (firstEpic),
                "Удаленный эпик остался в списке");
        Assertions.assertFalse (tasksManager.getSubTasks ().containsAll (subTasks),
                "После удаления эпика не удалились подзадачи");
    }

    @Test
    void updateEpicTask () {
        List<EpicTask> epicTasks = List.of (firstEpic, secondEpic);
        Assertions.assertTrue (tasksManager.getEpicTasks ().containsAll (epicTasks),
                "Не все эпики были добавлены корректно");
        firstEpic.setName ("sss");
        firstEpic.setDescription ("www");
        secondEpic.setName ("eee");
        secondEpic.setDescription ("rrr");
        tasksManager.updateEpicTask (firstEpic);
        tasksManager.updateEpicTask (secondEpic);
        List<EpicTask> updatedEpicTasks = List.of (firstEpic, secondEpic);
        Assertions.assertTrue (tasksManager.getEpicTasks ().containsAll (updatedEpicTasks),
                "Эпики не были изменены корректно");
    }

    @Test
    void updateTask () {
        List<Task> tasks = List.of (firstTask, secondTask);
        Assertions.assertTrue (tasksManager.getTasks ().containsAll (tasks),
                "Не все задачи была добавлены корректно");
        firstTask.setName ("sss");
        firstTask.setDescription ("www");
        secondTask.setName ("eee");
        secondTask.setDescription ("rrr");
        tasksManager.updateTask (firstTask);
        tasksManager.updateTask (secondTask);
        List<Task> updatedTasks = List.of (firstTask, secondTask);
        Assertions.assertTrue (tasksManager.getTasks ().containsAll (updatedTasks),
                "Задачи не были обновлены корректно");
    }

    @Test
    void updateSubTask () {
        List<SubTask> subTasks = List.of (firstSubTask, secondSubTask);
        Assertions.assertTrue (tasksManager.getSubTasks ().containsAll (subTasks),
                "Не все подзадачи была добавлены корректно");
        firstSubTask.setName ("sss");
        firstSubTask.setDescription ("www");
        secondSubTask.setName ("eee");
        secondSubTask.setDescription ("rrr");
        tasksManager.updateSubTask (firstSubTask);
        tasksManager.updateSubTask (secondSubTask);
        List<SubTask> updatedSubTasks = List.of (firstSubTask, secondSubTask);
        Assertions.assertTrue (tasksManager.getSubTasks ().containsAll (updatedSubTasks),
                "Подзадачи не были обновлены корректно");
    }

    @Test
    void history () {
        tasksManager.searchSubTaskWithId (firstSubTask.getIdentifier ());
        tasksManager.searchEpicWithId (firstEpic.getIdentifier ());
        tasksManager.searchSubTaskWithId (secondSubTask.getIdentifier ());
        List<SubTask> subTasks = List.of (firstSubTask, secondSubTask);
        List<EpicTask> epicTasks = List.of (firstEpic);
        Assertions.assertTrue (tasksManager.history ().containsAll (subTasks),
                "В историю не были добавлены все подзадачи");
        Assertions.assertTrue (tasksManager.history ().containsAll (epicTasks),
                "В историю не были добавлены все эпики");
    }

    @Test
    void getEpicTasks () {
        List<EpicTask> epicTasks = List.of (firstEpic, secondEpic);
        Assertions.assertTrue (tasksManager.getEpicTasks ().containsAll (epicTasks),
                "Эпики возвращаются не корректно");
    }

    @Test
    void getSubTasks () {
        List<SubTask> subTasks = List.of (firstSubTask, secondSubTask);
        Assertions.assertTrue (tasksManager.getSubTasks ().containsAll (subTasks),
                "Подзадачи возвращаются не корректно");
    }

    @Test
    void getTasks () {
        List<Task> tasks = List.of (firstTask, secondTask);
        Assertions.assertTrue (tasksManager.getTasks ().containsAll (tasks),
                "Задачи возвращаются не корректно");
    }

    @Test
    void updateId () {
        assertEquals (1, firstTask.getIdentifier ());
        assertEquals (2, secondTask.getIdentifier ());
        SubTask eight = new SubTask ("eight", "eight", Status.NEW, secondEpic.getIdentifier ());
        tasksManager.createTask (eight);
        assertEquals (8, eight.getIdentifier (), "Не корректно присваиваются ИД для новых задач");
    }

    @Test
    void getPrioritizedTasks () {
        tasksManager.clearTasks ();
        tasksManager.clearSubTasks ();
        tasksManager.clearEpics ();
        Task firstTask = new Task ("test1", "test1", Status.NEW);
        firstTask.setStartTime (String.valueOf (LocalDateTime.now ().plusHours (1)));
        firstTask.setDuration (10);
        tasksManager.createTask (firstTask);
        Task secondTask = new Task ("test2", "test2", Status.NEW);
        secondTask.setStartTime ((String.valueOf (LocalDateTime.now ())));
        secondTask.setDuration (20);
        tasksManager.createTask (secondTask);
        assertEquals (tasksManager.getPrioritizedTasks ().first (), secondTask, "Список отсортирован не верно");
        assertEquals (tasksManager.getPrioritizedTasks ().last (), firstTask, "Список отсортирован не верно");
        firstTask.setStartTime (String.valueOf (LocalDateTime.now ()));
        firstTask.setDuration (20);
        tasksManager.updateTask (firstTask);
        secondTask.setStartTime ((String.valueOf (LocalDateTime.now ().plusHours (1))));
        secondTask.setDuration (10);
        tasksManager.updateTask (secondTask);
        assertEquals (tasksManager.getPrioritizedTasks ().first (), firstTask,
                "Список отсортирован не верно после обновления времени задач");
        assertEquals (tasksManager.getPrioritizedTasks ().last (), secondTask,
                "Список отсортирован не верно после обновления времени задач");
    }

    @Test
    void intersectionCheckTest () {
        tasksManager.clearTasks ();
        tasksManager.clearSubTasks ();
        tasksManager.clearEpics ();
        Task firstTask = new Task ("test1", "test1", Status.NEW);
        firstTask.setStartTime (String.valueOf (LocalDateTime.now ()));
        firstTask.setDuration (10);
        tasksManager.createTask (firstTask);
        Task secondTask = new Task ("test2", "test2", Status.NEW);
        secondTask.setStartTime ((String.valueOf (LocalDateTime.now ())));
        secondTask.setDuration (10);
        tasksManager.createTask (secondTask);
        assertEquals (1, tasksManager.getTasks ().size (),
                "Конфликтующие по времени задачи все равно были добавлены");
        Assertions.assertFalse (tasksManager.getTasks ().contains (secondTask),
                "Была добавлена задача с конфликтующим временем");
        secondTask.setStartTime ((String.valueOf (LocalDateTime.now ().plusHours (1))));
        tasksManager.createTask (secondTask);
        assertEquals (2, tasksManager.getTasks ().size (),
                "При исправлении конфликта времени, некоторые задачи отсутствуют в списке");
        Assertions.assertTrue (tasksManager.getTasks ().contains (secondTask),
                "Задача с исправленным временем не была добавлена");
    }

}
