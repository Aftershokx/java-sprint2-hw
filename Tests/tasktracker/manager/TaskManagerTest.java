package tasktracker.manager;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tasktracker.tasks.Task;

import java.io.File;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static tasktracker.tasks.Status.NEW;

abstract class TaskManagerTest <T extends TaskManager> {

    TaskManager taskManager;
    FileBackedTasksManager fileBackedTasksManager;
    @BeforeEach
    void setUp () {
        taskManager = Managers.getDefault ();
        fileBackedTasksManager = new FileBackedTasksManager ();
    }

    @Test
    void loadFromFile () {
    }

    @Test
    void createEpicTask () {
    }

    @Test
    void createSubTask () {
    }

    @Test
    void createTask () {

    }

    @Test
    void searchEpicWithId () {
    }

    @Test
    void searchSubTaskWithId () {
    }

    @Test
    void searchTaskWithId () {
    }

    @Test
    void clearEpics () {
    }

    @Test
    void clearSubTasks () {
    }

    @Test
    void clearTasks () {
    }

    @Test
    void removeSubTaskWithId () {
    }

    @Test
    void removeTaskWithId () {
    }

    @Test
    void removeEpicTaskWithId () {
    }

    @Test
    void updateEpicTask () {
    }

    @Test
    void updateTask () {
    }

    @Test
    void updateSubTask () {
    }

    @Test
    void history () {
    }

    @Test
    void getEpicTasks () {
    }

    @Test
    void getSubTasks () {
    }

    @Test
    void getTasks () {
    }

    @Test
    void updateId () {
    }
}
