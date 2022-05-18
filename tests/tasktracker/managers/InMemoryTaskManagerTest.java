package tasktracker.managers;

public class InMemoryTaskManagerTest extends TaskManagerTest<InMemoryTaskManager> {
    @Override
    void preLoadTaskManager () {
        tasksManager = (InMemoryTaskManager) Managers.getDefault();
    }
}
