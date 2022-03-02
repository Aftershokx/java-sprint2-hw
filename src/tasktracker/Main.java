package tasktracker;

import tasktracker.manager.Managers;
import tasktracker.manager.TaskManager;
import tasktracker.tasks.EpicTask;
import tasktracker.tasks.Status;
import tasktracker.tasks.SubTask;
import tasktracker.tasks.Task;


public class Main {
    public static void main(String[] args) {
        TaskManager manager = Managers.getDefault ();

        Task firstTask = new Task("1000 steps", "need to walk 1000 steps around the neighborhood", Status.NEW);
        manager.createTask(firstTask);                          //Создание 1й задачи
        Task secondTask = new Task("3000 steps", "need to walk 3000 steps around the neighborhood", Status.NEW);
        manager.createTask(secondTask);                        //Создание второй задачи
        EpicTask firstEpic = new EpicTask("Do some parts", "Do some parts on milling machine");
        manager.createEpicTask(firstEpic);                     //Создание первого эпика
        SubTask firstSubTask = new SubTask("First is drawing", "Look at the drawing", Status.NEW, firstEpic.getIdentifier());
        manager.createSubTask(firstSubTask);                   //Создание первой подзадачи первого эпика
        SubTask secondSubTask = new SubTask("Do the mill", "Mill parts on a milling machine", Status.NEW, firstEpic.getIdentifier());
        manager.createSubTask(secondSubTask);                  //Создание второй подзадачи первого эпика
        EpicTask secondEpic = new EpicTask("Go to lunch", "Go to the cafeteria");
        manager.createEpicTask(secondEpic);                    //Создание второго эпика
        SubTask thirdSubTask = new SubTask("To eat", "Just eat silently", Status.NEW, secondEpic.getIdentifier());
        manager.createSubTask(thirdSubTask);                   //Создание третьего эпика
        System.out.println(manager.getEpicTasks());           //Вывод эпиков
        System.out.println(manager.getSubTasks());            //Вывод подзадач
        System.out.println(manager.getTasks());               //Вывод задач
        System.out.println();
        System.out.println();


        manager.searchEpicWithId (3);                         //Просматриваю Эпик
        manager.searchSubTaskWithId (4);                      //Просматриваю сабтаск
        manager.searchTaskWithId (1);                         //Просматриваю таск
        System.out.println (manager.history ());           //Вывожу историю просмотров
        System.out.println();
        System.out.println();


        firstSubTask.setStatus(Status.IN_PROGRESS);
        manager.updateSubTask(firstSubTask);                   //Изменение статуса первой подзадачи
        firstTask.setStatus(Status.IN_PROGRESS);
        manager.updateTask(firstTask);                         //Изменение статуса первой задачи
        System.out.println(manager.getEpicTasks());
        System.out.println(manager.getSubTasks());
        System.out.println(manager.getTasks());
        System.out.println();
        System.out.println();


        System.out.println (manager.history ());           //Вывожу историю просмотров
        System.out.println();
        System.out.println();

        manager.removeTaskWithId(secondTask.getIdentifier());        //Удаление второй задачи
        manager.removeSubTaskWithId(thirdSubTask.getIdentifier());   //Удаление третьей подзадачи
        manager.removeEpicTaskWithId(secondEpic.getIdentifier());    //Удаление второго эпика
        System.out.println(manager.getEpicTasks());
        System.out.println(manager.getSubTasks());
        System.out.println(manager.getTasks());
        System.out.println();
        System.out.println();


        System.out.println (manager.history ());           //Вывожу историю просмотров
        System.out.println();
        System.out.println();


        manager.clearSubTasks();                                   //Удаление всех подзадач
        System.out.println(manager.getEpicTasks());
        System.out.println(manager.getSubTasks());
        System.out.println(manager.getTasks());
        System.out.println();

        manager.clearEpics();                                     //Удаление всех эпиков
        System.out.println(manager.getEpicTasks());
        System.out.println(manager.getSubTasks());
        System.out.println(manager.getTasks());
        System.out.println();

        manager.clearTasks();                                     //Удаление всех задач
        System.out.println(manager.getEpicTasks());
        System.out.println(manager.getSubTasks());
        System.out.println(manager.getTasks());
        System.out.println();


    }

}
