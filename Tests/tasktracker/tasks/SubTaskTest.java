package tasktracker.tasks;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SubTaskTest {

    @Test
    protected void getEpicIdentifier () {
        EpicTask epicTask = new EpicTask ("epic", "epic1");
        epicTask.setStatus (Status.NEW);
        epicTask.setIdentifier (1);
        SubTask subTask = new SubTask ("sub", "sub1", Status.NEW, 1);
        assertEquals (1, subTask.getEpicIdentifier ());
    }

}