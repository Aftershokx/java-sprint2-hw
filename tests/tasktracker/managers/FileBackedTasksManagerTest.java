package tasktracker.managers;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.File;

public class FileBackedTasksManagerTest extends TaskManagerTest<FileBackedTasksManager> {

    private final String path = "src/tasktracker/files/autosave.csv";

    @Override
    void preLoadTaskManager () {
        tasksManager = new FileBackedTasksManager ();
    }

    @Test
    void saveAndLoadFromFileShouldRestoreTasksCorrectly () {
        tasksManager.searchEpicWithId (3);
        tasksManager.searchEpicWithId (7);
        tasksManager.searchSubTaskWithId (4);
        tasksManager.searchSubTaskWithId (4);
        tasksManager.searchSubTaskWithId (5);
        tasksManager.searchSubTaskWithId (6);
        tasksManager.searchTaskWithId (2);
        tasksManager.searchTaskWithId (1);
        tasksManager.searchTaskWithId (2);

        FileBackedTasksManager fileBackedTasksManager2 = FileBackedTasksManager.loadFromFile (new File (path));

        Assertions.assertTrue (tasksManager.history ().containsAll (fileBackedTasksManager2.history ()),
                "История загрузилась с ошибкой");
        Assertions.assertTrue (tasksManager.getEpicTasks ().containsAll (fileBackedTasksManager2.getEpicTasks ()),
                "Эпики загрузились с ошибкой");
        Assertions.assertTrue (tasksManager.getTasks ().containsAll (fileBackedTasksManager2.getTasks ()),
                "Задачи загрузились с ошибкой");
        Assertions.assertTrue (tasksManager.getSubTasks ().containsAll (fileBackedTasksManager2.getSubTasks ()),
                "Подзадачи загрузились с ошибкой");

    }
}
