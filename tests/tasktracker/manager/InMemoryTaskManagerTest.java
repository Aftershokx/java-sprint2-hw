package tasktracker.manager;

public class InMemoryTaskManagerTest extends TaskManagerTest<InMemoryTaskManager> {
    @Override
    void preLoadTaskManager () {
        tasksManager = (InMemoryTaskManager) Managers.getDefault();
    }
}
