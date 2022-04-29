package tasktracker.tasks;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TaskTest {

    Task task;

    @BeforeEach
    public void setUp () {
        task = new Task ("testName", "testDescription", Status.NEW);
    }

    @Test
    protected void setAndGetName () {
        String name = "testName";
        assertEquals (name, task.getName ());
        String newName = "newName";
        task.setName (newName);
        assertEquals (newName, task.getName ());
    }

    @Test
    protected void setAndGetDescription () {
        String description = "testDescription";
        assertEquals (description, task.getDescription ());
        String newDescription = "newDescription";
        task.setDescription (newDescription);
        assertEquals (newDescription, task.getDescription ());
    }

    @Test
    protected void setAndGetStatus () {
        assertEquals (Status.NEW, task.getStatus ());
        task.setStatus (Status.IN_PROGRESS);
        assertEquals (Status.IN_PROGRESS, task.getStatus ());
    }

    @Test
    protected void setAndGetStatusThrowsWhenStatusUndifined () {
        final NullPointerException exception = assertThrows (
                NullPointerException.class,
                new Executable () {
                    @Override
                    public void execute () {
                        Task task2 = new Task ("testName", "testDesc");
                        String status = task2.getStatus ().name ();
                    }
                });
    }

    @Test
    protected void setAndGetIdentifier () {
        task.setIdentifier (1);
        assertEquals (1, task.getIdentifier ());
    }

    @Test
    protected void setAndGetType () {
        task.setType (Types.TASK);
        assertEquals (Types.TASK, task.getType ());
    }

}