package tasktracker.managers;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tasktracker.tasks.EpicTask;
import tasktracker.tasks.Status;
import tasktracker.tasks.SubTask;
import tasktracker.tasks.Task;

import java.util.ArrayList;

class HistoryManagerTest {

    private static Task firstTask;
    private static Task secondTask;
    private static EpicTask firstEpic;
    private static SubTask firstSubTask;
    private static SubTask secondSubTask;
    private static EpicTask secondEpic;
    HistoryManager historyManager;

    void removeAll(){
        historyManager.remove (1);
        historyManager.remove (2);
        historyManager.remove (3);
        historyManager.remove (4);
        historyManager.remove (5);
        historyManager.remove (6);
    }

    @BeforeEach
    public void beforeEach () {
        historyManager = Managers.getDefaultHistory ();
        firstTask = new Task ("test1", "test1", Status.NEW);
        firstTask.setIdentifier (1);
        secondTask = new Task ("test2", "test2", Status.NEW);
        secondTask.setIdentifier (2);
        firstEpic = new EpicTask ("test3", "test3");
        firstEpic.setIdentifier (3);
        firstSubTask = new SubTask ("test4", "test4", Status.NEW, firstEpic.getIdentifier ());
        firstSubTask.setIdentifier (4);
        secondSubTask = new SubTask ("test5", "test5", Status.NEW, firstEpic.getIdentifier ());
        secondSubTask.setIdentifier (5);
        firstEpic.addSubtask (firstSubTask);
        firstEpic.addSubtask (secondSubTask);
        secondEpic = new EpicTask ("test6", "test6");
        secondEpic.setIdentifier (6);
    }

    @Test
     void add () {
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
        Assertions.assertEquals (checkList, historyManager.getHistory (),
                "В историю не были добавлены все файлы");
        historyManager.add (secondTask);
        checkList.add (secondTask);
        Assertions.assertEquals (checkList, historyManager.getHistory (),
                "В историю не была добавлена дополнительная подзадача");
    }

    @Test
    void remove () {
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
        Assertions.assertEquals (checkList, historyManager.getHistory (),
                "Из истории не была удалена первая подзадача");
        historyManager.remove (secondTask.getIdentifier ());
        checkList.remove (secondTask);
        Assertions.assertEquals (checkList, historyManager.getHistory (),
                "Из история не была удалена вторая задача");
        historyManager.remove (firstEpic.getIdentifier ());
        checkList.remove (firstEpic);
        Assertions.assertEquals (checkList, historyManager.getHistory (), "Из истории не был удален эпик");
    }

    @Test
    public void shouldBeEmptyListByEmptyHistory () {
        removeAll ();
        Assertions.assertTrue (historyManager.getHistory ().isEmpty (), "История не пуста");
    }
}