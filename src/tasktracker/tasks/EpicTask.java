package tasktracker.tasks;

import java.util.ArrayList;


public class EpicTask extends Task {

    private final ArrayList<SubTask> subTasks = new ArrayList<>();

    public EpicTask(String name, String description) {
        super(name, description);
    }

    public ArrayList<Integer> getSubTasksIds() {                                  //получение списка Ид подзадач
        ArrayList<Integer> subTasksIds = new ArrayList<>();
        for (SubTask subTask : subTasks) {
            subTasksIds.add(subTask.getIdentifier());
        }
        return subTasksIds;
    }

    public ArrayList<SubTask> getSubTasks() {                                     //получение подзадач
        return subTasks;
    }

    public void addSubtask(SubTask subTask) {                                   //Добавление плдзадачи
        if (subTask != null) {
            subTasks.add(subTask);
        }
    }

    public void removeSubtask(SubTask subTask) {                                //удаление подзадачи
        subTasks.remove(subTask);
    }

    //Обновление статуса эпика
    public void updateEpicStatus() {
        int stateNew = 0;
        int stateDone = 0;
        for (SubTask subTask : getSubTasks()) {
            switch (subTask.getStatus()) {
                case NEW:
                    stateNew += 1;
                    break;
                case DONE:
                    stateDone += 1;
                    break;
            }
        }

        Status status;

        if (stateNew == getSubTasksIds().size()) {
            status = Status.NEW;
        } else if (stateDone == getSubTasksIds().size()) {
            status = Status.DONE;
        } else {
            status = Status.IN_PROGRESS;
        }

        setStatus(status);

    }


    @Override
    public String toString() {
        return "epic {" +
                "name = '" + getName() + '\'' +
                ", description = '" + getDescription() + '\'' +
                ", id = " + getIdentifier() +
                ", status = '" + getStatus() + '\'' +
                ", subTasksIds included = '" + getSubTasksIds() + '\'' +
                '}';
    }
}


