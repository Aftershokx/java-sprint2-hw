package tasktracker.manager;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public class Managers {

    @Contract(" -> new")
    public static @NotNull TaskManager getDefault () {
        return new InMemoryTaskManager ();
    }


    @Contract(" -> new")
    public static @NotNull HistoryManager getDefaultHistory () {
        return new InMemoryHistoryManager ();
    }

}
