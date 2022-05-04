package tasktracker.manager;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tasktracker.tasks.EpicTask;
import tasktracker.tasks.Status;
import tasktracker.tasks.SubTask;
import tasktracker.tasks.Task;

import java.io.File;
import java.time.LocalDateTime;
import java.util.List;

class TaskManagerTest<T extends TaskManager> {

    TaskManager taskManager;
    FileBackedTasksManager fileBackedTasksManager;

    @BeforeEach
    void setUp () {
        taskManager = Managers.getDefault ();
        fileBackedTasksManager = new FileBackedTasksManager ();
    }

    @Test
    void loadFromFileShouldRestoreTasksCorrectly () {
        String path = "src/tasktracker/files/autosave.csv";
        Task firstTask = new Task ("test1", "test1", Status.NEW);
        fileBackedTasksManager.createTask (firstTask);
        Task secondTask = new Task ("test2", "test2", Status.NEW);
        fileBackedTasksManager.createTask (secondTask);
        EpicTask firstEpic = new EpicTask ("test3", "test3");
        fileBackedTasksManager.createTask (firstEpic);
        SubTask firstSubTask = new SubTask ("test4", "test4", Status.NEW, firstEpic.getIdentifier ());
        fileBackedTasksManager.createTask (firstSubTask);
        SubTask secondSubTask = new SubTask ("test5", "test5", Status.NEW, firstEpic.getIdentifier ());
        fileBackedTasksManager.createTask (secondSubTask);
        SubTask thirdSubTask = new SubTask ("test6", "test6", Status.NEW, firstEpic.getIdentifier ());
        fileBackedTasksManager.createTask (thirdSubTask);
        EpicTask secondEpic = new EpicTask ("test7", "test7");
        fileBackedTasksManager.createTask (secondEpic);

        fileBackedTasksManager.searchEpicWithId (3);
        fileBackedTasksManager.searchEpicWithId (7);
        fileBackedTasksManager.searchSubTaskWithId (4);
        fileBackedTasksManager.searchSubTaskWithId (4);
        fileBackedTasksManager.searchSubTaskWithId (5);
        fileBackedTasksManager.searchSubTaskWithId (6);
        fileBackedTasksManager.searchTaskWithId (2);
        fileBackedTasksManager.searchTaskWithId (1);
        fileBackedTasksManager.searchTaskWithId (2);

        FileBackedTasksManager fileBackedTasksManager2 = FileBackedTasksManager.loadFromFile (new File (path));

        Assertions.assertTrue (fileBackedTasksManager.history ().containsAll (fileBackedTasksManager2.history ()));
        Assertions.assertTrue (fileBackedTasksManager.getEpicTasks ().containsAll (fileBackedTasksManager2.getEpicTasks ()));
        Assertions.assertTrue (fileBackedTasksManager.getTasks ().containsAll (fileBackedTasksManager2.getTasks ()));
        Assertions.assertTrue (fileBackedTasksManager.getSubTasks ().containsAll (fileBackedTasksManager2.getSubTasks ()));

    }

    @Test
    void createTasksPositiveReaction () {
        Task firstTask = new Task ("test1", "test1", Status.NEW);
        fileBackedTasksManager.createTask (firstTask);
        Task secondTask = new Task ("test2", "test2", Status.NEW);
        fileBackedTasksManager.createTask (secondTask);
        EpicTask firstEpic = new EpicTask ("test3", "test3");
        fileBackedTasksManager.createTask (firstEpic);
        SubTask firstSubTask = new SubTask ("test4", "test4", Status.NEW, firstEpic.getIdentifier ());
        fileBackedTasksManager.createTask (firstSubTask);

        List<EpicTask> epicCheck = List.of (firstEpic);
        List<Task> taskCheck = List.of (firstTask, secondTask);
        List<SubTask> subTasksCheck = List.of (firstSubTask);

        Assertions.assertTrue (fileBackedTasksManager.getEpicTasks ().containsAll (epicCheck));
        Assertions.assertTrue (fileBackedTasksManager.getTasks ().containsAll (taskCheck));
        Assertions.assertTrue (fileBackedTasksManager.getSubTasks ().containsAll (subTasksCheck));

    }

    @Test
    void searchEpicWithIdPositiveReaction () {
        EpicTask firstEpic = new EpicTask ("test3", "test3");
        fileBackedTasksManager.createTask (firstEpic);
        int id = firstEpic.getIdentifier ();
        Assertions.assertEquals (firstEpic, fileBackedTasksManager.searchEpicWithId (id));
    }

    @Test
    void searchSubTaskWithIdPositiveReaction () {
        EpicTask firstEpic = new EpicTask ("test3", "test3");
        fileBackedTasksManager.createTask (firstEpic);
        SubTask firstSubTask = new SubTask ("test4", "test4", Status.NEW, firstEpic.getIdentifier ());
        fileBackedTasksManager.createTask (firstSubTask);
        int id = firstSubTask.getIdentifier ();
        Assertions.assertEquals (firstSubTask, fileBackedTasksManager.searchSubTaskWithId (id));
    }

    @Test
    void searchTaskWithIdPositiveReaction () {
        Task firstTask = new Task ("test1", "test1", Status.NEW);
        fileBackedTasksManager.createTask (firstTask);
        int id = firstTask.getIdentifier ();
        Assertions.assertEquals (firstTask, fileBackedTasksManager.searchTaskWithId (id));
    }

    @Test
    void clearEpicsPositiveReaction () {
        EpicTask firstEpic = new EpicTask ("test3", "test3");
        fileBackedTasksManager.createTask (firstEpic);
        EpicTask secondEpic = new EpicTask ("test4", "test4");
        fileBackedTasksManager.createTask (secondEpic);
        List<EpicTask> epics = List.of (firstEpic, secondEpic);
        Assertions.assertTrue (fileBackedTasksManager.getEpicTasks ().containsAll (epics));
        fileBackedTasksManager.clearEpics ();
        Assertions.assertTrue (fileBackedTasksManager.getEpicTasks ().isEmpty ());
    }

    @Test
    void clearSubTasksPositiveReaction () {
        EpicTask firstEpic = new EpicTask ("test3", "test3");
        fileBackedTasksManager.createTask (firstEpic);
        SubTask firstSubTask = new SubTask ("test4", "test4", Status.NEW, firstEpic.getIdentifier ());
        fileBackedTasksManager.createTask (firstSubTask);
        SubTask secondSubTask = new SubTask ("test5", "test5", Status.NEW, firstEpic.getIdentifier ());
        fileBackedTasksManager.createTask (secondSubTask);
        List<SubTask> subTasks = List.of (firstSubTask, secondSubTask);
        Assertions.assertTrue (fileBackedTasksManager.getSubTasks ().containsAll (subTasks));
        fileBackedTasksManager.clearSubTasks ();
        Assertions.assertTrue (fileBackedTasksManager.getSubTasks ().isEmpty ());
    }

    @Test
    void clearTasksPositiveReaction () {
        Task firstTask = new Task ("test1", "test1", Status.NEW);
        fileBackedTasksManager.createTask (firstTask);
        Task secondTask = new Task ("test2", "test2", Status.NEW);
        fileBackedTasksManager.createTask (secondTask);
        List<Task> tasks = List.of (firstTask, secondTask);
        Assertions.assertTrue (fileBackedTasksManager.getTasks ().containsAll (tasks));
        fileBackedTasksManager.clearTasks ();
        Assertions.assertTrue (fileBackedTasksManager.getSubTasks ().isEmpty ());
    }

    @Test
    void removeSubTaskWithId () {
        EpicTask firstEpic = new EpicTask ("test3", "test3");
        fileBackedTasksManager.createTask (firstEpic);
        SubTask firstSubTask = new SubTask ("test4", "test4", Status.NEW, firstEpic.getIdentifier ());
        fileBackedTasksManager.createTask (firstSubTask);
        SubTask secondSubTask = new SubTask ("test5", "test5", Status.NEW, firstEpic.getIdentifier ());
        fileBackedTasksManager.createTask (secondSubTask);
        List<SubTask> subTasks = List.of (firstSubTask, secondSubTask);
        Assertions.assertTrue (fileBackedTasksManager.getSubTasks ().containsAll (subTasks));
        fileBackedTasksManager.removeSubTaskWithId (firstSubTask.getIdentifier ());
        Assertions.assertTrue (fileBackedTasksManager.getSubTasks ().contains (secondSubTask));
        Assertions.assertFalse (fileBackedTasksManager.getSubTasks ().contains (firstSubTask));
    }

    @Test
    void removeTaskWithId () {
        Task firstTask = new Task ("test1", "test1", Status.NEW);
        fileBackedTasksManager.createTask (firstTask);
        Task secondTask = new Task ("test2", "test2", Status.NEW);
        fileBackedTasksManager.createTask (secondTask);
        List<Task> tasks = List.of (firstTask, secondTask);
        Assertions.assertTrue (fileBackedTasksManager.getTasks ().containsAll (tasks));
        fileBackedTasksManager.removeTaskWithId (firstTask.getIdentifier ());
        Assertions.assertTrue (fileBackedTasksManager.getTasks ().contains (secondTask));
        Assertions.assertFalse (fileBackedTasksManager.getTasks ().contains (firstTask));
    }

    @Test
    void removeEpicTaskWithId () {
        EpicTask firstEpic = new EpicTask ("test3", "test3");
        fileBackedTasksManager.createTask (firstEpic);
        EpicTask secondEpic = new EpicTask ("test4", "test4");
        fileBackedTasksManager.createTask (secondEpic);
        SubTask firstSubTask = new SubTask ("test4", "test4", Status.NEW, firstEpic.getIdentifier ());
        fileBackedTasksManager.createTask (firstSubTask);
        SubTask secondSubTask = new SubTask ("test7", "test7", Status.NEW, firstEpic.getIdentifier ());
        fileBackedTasksManager.createTask (secondSubTask);
        List<EpicTask> epicTasks = List.of (firstEpic, secondEpic);
        List<SubTask> subTasks = List.of (firstSubTask, secondSubTask);
        Assertions.assertTrue (fileBackedTasksManager.getSubTasks ().containsAll (subTasks));
        Assertions.assertTrue (fileBackedTasksManager.getEpicTasks ().containsAll (epicTasks));
        int id = firstEpic.getIdentifier ();
        fileBackedTasksManager.removeEpicTaskWithId (id);
        Assertions.assertTrue (fileBackedTasksManager.getEpicTasks ().contains (secondEpic));
        Assertions.assertFalse (fileBackedTasksManager.getEpicTasks ().contains (firstEpic));
        Assertions.assertFalse (fileBackedTasksManager.getSubTasks ().containsAll (subTasks));
    }

    @Test
    void updateEpicTask () {
        EpicTask firstEpic = new EpicTask ("test3", "test3");
        fileBackedTasksManager.createTask (firstEpic);
        EpicTask secondEpic = new EpicTask ("test4", "test4");
        fileBackedTasksManager.createTask (secondEpic);
        List<EpicTask> epicTasks = List.of (firstEpic, secondEpic);
        Assertions.assertTrue (fileBackedTasksManager.getEpicTasks ().containsAll (epicTasks));
        firstEpic.setName ("sss");
        firstEpic.setDescription ("www");
        secondEpic.setName ("eee");
        secondEpic.setDescription ("rrr");
        fileBackedTasksManager.updateEpicTask (firstEpic);
        fileBackedTasksManager.updateEpicTask (secondEpic);
        List<EpicTask> updatedEpicTasks = List.of (firstEpic, secondEpic);
        Assertions.assertTrue (fileBackedTasksManager.getEpicTasks ().containsAll (updatedEpicTasks));
    }

    @Test
    void updateTask () {
        Task firstTask = new Task ("test1", "test1", Status.NEW);
        fileBackedTasksManager.createTask (firstTask);
        Task secondTask = new Task ("test2", "test2", Status.NEW);
        fileBackedTasksManager.createTask (secondTask);
        List<Task> tasks = List.of (firstTask, secondTask);
        Assertions.assertTrue (fileBackedTasksManager.getTasks ().containsAll (tasks));
        firstTask.setName ("sss");
        firstTask.setDescription ("www");
        secondTask.setName ("eee");
        secondTask.setDescription ("rrr");
        fileBackedTasksManager.updateTask (firstTask);
        fileBackedTasksManager.updateTask (secondTask);
        List<Task> updatedTasks = List.of (firstTask, secondTask);
        Assertions.assertTrue (fileBackedTasksManager.getTasks ().containsAll (updatedTasks));
    }

    @Test
    void updateSubTask () {
        EpicTask firstEpic = new EpicTask ("test3", "test3");
        fileBackedTasksManager.createTask (firstEpic);
        SubTask firstSubTask = new SubTask ("test4", "test4", Status.NEW, firstEpic.getIdentifier ());
        fileBackedTasksManager.createTask (firstSubTask);
        SubTask secondSubTask = new SubTask ("test5", "test5", Status.NEW, firstEpic.getIdentifier ());
        fileBackedTasksManager.createTask (secondSubTask);
        List<SubTask> subTasks = List.of (firstSubTask, secondSubTask);
        Assertions.assertTrue (fileBackedTasksManager.getSubTasks ().containsAll (subTasks));
        firstSubTask.setName ("sss");
        firstSubTask.setDescription ("www");
        secondSubTask.setName ("eee");
        secondSubTask.setDescription ("rrr");
        fileBackedTasksManager.updateSubTask (firstSubTask);
        fileBackedTasksManager.updateSubTask (secondSubTask);
        List<SubTask> updatedSubTasks = List.of (firstSubTask, secondSubTask);
        Assertions.assertTrue (fileBackedTasksManager.getSubTasks ().containsAll (updatedSubTasks));
    }

    @Test
    void history () {
        EpicTask firstEpic = new EpicTask ("test3", "test3");
        fileBackedTasksManager.createTask (firstEpic);
        SubTask firstSubTask = new SubTask ("test4", "test4", Status.NEW, firstEpic.getIdentifier ());
        fileBackedTasksManager.createTask (firstSubTask);
        SubTask secondSubTask = new SubTask ("test5", "test5", Status.NEW, firstEpic.getIdentifier ());
        fileBackedTasksManager.createTask (secondSubTask);
        fileBackedTasksManager.searchSubTaskWithId (firstSubTask.getIdentifier ());
        fileBackedTasksManager.searchEpicWithId (firstEpic.getIdentifier ());
        fileBackedTasksManager.searchSubTaskWithId (secondSubTask.getIdentifier ());
        List<SubTask> subTasks = List.of (firstSubTask, secondSubTask);
        List<EpicTask> epicTasks = List.of (firstEpic);
        Assertions.assertTrue (fileBackedTasksManager.history ().containsAll (subTasks));
        Assertions.assertTrue (fileBackedTasksManager.history ().containsAll (epicTasks));
    }

    @Test
    void getEpicTasks () {
        EpicTask firstEpic = new EpicTask ("test3", "test3");
        fileBackedTasksManager.createTask (firstEpic);
        EpicTask secondEpic = new EpicTask ("test2", "test2");
        fileBackedTasksManager.createTask (secondEpic);
        List<EpicTask> epicTasks = List.of (firstEpic, secondEpic);
        Assertions.assertTrue (fileBackedTasksManager.getEpicTasks ().containsAll (epicTasks));
    }

    @Test
    void getSubTasks () {
        EpicTask firstEpic = new EpicTask ("test3", "test3");
        fileBackedTasksManager.createTask (firstEpic);
        SubTask firstSubTask = new SubTask ("test4", "test4", Status.NEW, firstEpic.getIdentifier ());
        fileBackedTasksManager.createTask (firstSubTask);
        SubTask secondSubTask = new SubTask ("test5", "test5", Status.NEW, firstEpic.getIdentifier ());
        fileBackedTasksManager.createTask (secondSubTask);
        List<SubTask> subTasks = List.of (firstSubTask, secondSubTask);
        Assertions.assertTrue (fileBackedTasksManager.getSubTasks ().containsAll (subTasks));
    }

    @Test
    void getTasks () {
        Task firstTask = new Task ("test1", "test1", Status.NEW);
        fileBackedTasksManager.createTask (firstTask);
        Task secondTask = new Task ("test2", "test2", Status.NEW);
        fileBackedTasksManager.createTask (secondTask);
        List<Task> tasks = List.of (firstTask, secondTask);
        Assertions.assertTrue (fileBackedTasksManager.getTasks ().containsAll (tasks));
    }

    @Test
    void updateId () {
        Task firstTask = new Task ("test1", "test1", Status.NEW);
        fileBackedTasksManager.createTask (firstTask);
        Task secondTask = new Task ("test2", "test2", Status.NEW);
        fileBackedTasksManager.createTask (secondTask);
        Assertions.assertEquals (2, secondTask.getIdentifier ());
        Task thirdTask = new Task ("test3", "test3", Status.NEW);
        fileBackedTasksManager.createTask (thirdTask);
        Assertions.assertEquals (3, thirdTask.getIdentifier ());
    }

    @Test
    void getPrioritizedTasks () {
        Task firstTask = new Task ("test1", "test1", Status.NEW);
        firstTask.setStartTime (String.valueOf (LocalDateTime.now ().plusHours (1)));
        firstTask.setDuration (10);
        fileBackedTasksManager.createTask (firstTask);
        Task secondTask = new Task ("test2", "test2", Status.NEW);
        secondTask.setStartTime ((String.valueOf (LocalDateTime.now ())));
        secondTask.setDuration (20);
        fileBackedTasksManager.createTask (secondTask);
        Assertions.assertEquals (fileBackedTasksManager.getPrioritizedTasks ().first (), secondTask);
        Assertions.assertEquals (fileBackedTasksManager.getPrioritizedTasks ().last (), firstTask);
    }

    @Test
    void intersectionCheckTest () {
        Task firstTask = new Task ("test1", "test1", Status.NEW);
        firstTask.setStartTime (String.valueOf (LocalDateTime.now ()));
        firstTask.setDuration (10);
        fileBackedTasksManager.createTask (firstTask);
        Task secondTask = new Task ("test2", "test2", Status.NEW);
        secondTask.setStartTime ((String.valueOf (LocalDateTime.now ())));
        secondTask.setDuration (10);
        fileBackedTasksManager.createTask (secondTask);
        Assertions.assertEquals (1, fileBackedTasksManager.getTasks ().size ());
        Assertions.assertFalse (fileBackedTasksManager.getTasks ().contains (secondTask));
    }

}
