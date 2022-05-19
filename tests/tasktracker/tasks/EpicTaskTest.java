package tasktracker.tasks;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class EpicTaskTest {

    protected EpicTask epicTask;
    protected SubTask subTask;
    protected SubTask subTask1;

    @BeforeEach
    protected void setUp () {
        epicTask = new EpicTask ("epic", "epic1");
        epicTask.setIdentifier (1);
        subTask = new SubTask ("sub1", "sub1desc", Status.NEW, 1);
        subTask.setIdentifier (2);
        subTask1 = new SubTask ("sub2", "sub2desc", Status.NEW, 1);
        subTask.setIdentifier (3);
    }

    @Test
    protected void addAndGetSubTasksIdsAndGetSubTasksAndRemoveSubTasks () {
        List<SubTask> subTaskList = new ArrayList<> ();
        subTaskList.add (subTask);
        epicTask.addSubtask (subTask);
        assertEquals (subTaskList, epicTask.getSubTasks ());

        ArrayList<Integer> subTasksIds = new ArrayList<> ();
        for (SubTask subTask : subTaskList) {
            subTasksIds.add (subTask.getIdentifier ());
        }
        assertEquals (subTasksIds, epicTask.getSubTasksIds ());

        subTaskList.add (subTask1);
        epicTask.addSubtask (subTask1);
        assertEquals (subTaskList, epicTask.getSubTasks ());
        epicTask.removeSubtask (subTask1);
        assertNotEquals (subTaskList, epicTask.getSubTasks ());
        subTaskList.remove (subTask1);
        assertEquals (subTaskList, epicTask.getSubTasks ());

    }

    @Test
    protected void getSubTasksThrowsWhenEmpty () {
        final IndexOutOfBoundsException exception = assertThrows (
                IndexOutOfBoundsException.class,
                () -> epicTask.getSubTasks ().get (0));
    }

    @Test
    protected void updateEpicStatus () {
        epicTask.updateEpicStatus ();
        assertEquals (Status.NEW, epicTask.getStatus ());

        epicTask.addSubtask (subTask);
        epicTask.addSubtask (subTask1);
        epicTask.updateEpicStatus ();
        assertEquals (Status.NEW, epicTask.getStatus ());

        subTask.setStatus (Status.DONE);
        subTask1.setStatus (Status.DONE);
        epicTask.updateEpicStatus ();
        assertEquals (Status.DONE, epicTask.getStatus ());

        subTask.setStatus (Status.NEW);
        subTask1.setStatus (Status.DONE);
        epicTask.updateEpicStatus ();
        assertEquals (Status.IN_PROGRESS, epicTask.getStatus ());

        subTask.setStatus (Status.IN_PROGRESS);
        subTask1.setStatus (Status.IN_PROGRESS);
        epicTask.updateEpicStatus ();
        assertEquals (Status.IN_PROGRESS, epicTask.getStatus ());

    }
}