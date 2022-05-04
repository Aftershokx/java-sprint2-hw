package tasktracker.manager;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import tasktracker.tasks.EpicTask;
import tasktracker.tasks.Status;
import tasktracker.tasks.SubTask;
import tasktracker.tasks.Task;

import java.util.ArrayList;

class InMemoryHistoryManagerTest {


    private static Task firstTask;
    private static Task secondTask;
    private static EpicTask firstEpic;
    private static SubTask firstSubTask;
    private static SubTask secondSubTask;
    private static EpicTask secondEpic;

    @BeforeAll
    public static void beforeAll () {
        FileBackedTasksManager fileBackedTasksManager = new FileBackedTasksManager ();
        firstTask = new Task ("test1", "test1", Status.NEW);
        fileBackedTasksManager.createTask (firstTask);
        secondTask = new Task ("test2", "test2", Status.NEW);
        fileBackedTasksManager.createTask (secondTask);
        firstEpic = new EpicTask ("test3", "test3");
        fileBackedTasksManager.createTask (firstEpic);
        firstSubTask = new SubTask ("test4", "test4", Status.NEW, firstEpic.getIdentifier ());
        fileBackedTasksManager.createTask (firstSubTask);
        secondSubTask = new SubTask ("test5", "test5", Status.NEW, firstEpic.getIdentifier ());
        fileBackedTasksManager.createTask (secondSubTask);
        secondEpic = new EpicTask ("test7", "test7");
        fileBackedTasksManager.createTask (secondEpic);
    }

    @Test
    void add () {
        HistoryManager historyManager = new InMemoryHistoryManager ();
        ArrayList<Task> checkList = new ArrayList<> ();

        historyManager.add (firstTask);
        checkList.add (firstTask);

        historyManager.add (firstEpic);
        checkList.add (firstEpic);

        historyManager.add (secondEpic);
        checkList.add (secondEpic);

        historyManager.add (firstSubTask);
        checkList.add (firstSubTask);

        historyManager.add (secondSubTask);
        checkList.add (secondSubTask);

        Assertions.assertEquals (checkList, historyManager.getHistory ());

        historyManager.add (firstTask);
        checkList.remove (firstTask);
        checkList.add (firstTask);

        Assertions.assertEquals (checkList, historyManager.getHistory ());
    }

    @Test
    void remove () {
        HistoryManager historyManager = new InMemoryHistoryManager ();
        ArrayList<Task> checkList = new ArrayList<> ();

        historyManager.add (secondTask);
        checkList.add (secondTask);

        historyManager.add (firstSubTask);
        checkList.add (firstSubTask);

        historyManager.add (secondSubTask);
        checkList.add (secondSubTask);

        historyManager.add (firstEpic);
        checkList.add (firstEpic);

        historyManager.add (secondEpic);
        checkList.add (secondEpic);

        historyManager.remove (firstSubTask.getIdentifier ());
        checkList.remove (firstSubTask);

        Assertions.assertEquals (checkList, historyManager.getHistory ());

        historyManager.remove (secondTask.getIdentifier ());
        checkList.remove (secondTask);

        Assertions.assertEquals (checkList, historyManager.getHistory ());

        historyManager.remove (firstEpic.getIdentifier ());
        checkList.remove (firstEpic);

        Assertions.assertEquals (checkList, historyManager.getHistory ());
    }

    @Test
    public void shouldBeEmptyListByEmptyHistory () {
        HistoryManager historyManager = new InMemoryHistoryManager ();
        Assertions.assertTrue (historyManager.getHistory ().isEmpty ());
    }
}