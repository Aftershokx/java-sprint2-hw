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

        Task firstTask = new Task ("1000 steps", "need to walk 1000 steps around the neighborhood", Status.NEW);
        manager.createTask (firstTask);                          //Создание 1й задачи
        Task secondTask = new Task ("3000 steps", "need to walk 3000 steps around the neighborhood", Status.NEW);
        manager.createTask (secondTask);                        //Создание 2й задачи
        EpicTask firstEpic = new EpicTask ("Do some parts", "Do some parts on milling machine");
        manager.createEpicTask (firstEpic);                     //Создание 1го эпика
        SubTask firstSubTask = new SubTask ("First is drawing", "Look at the drawing", Status.NEW, firstEpic.getIdentifier ());
        manager.createSubTask (firstSubTask);                   //Создание 1й сабтаски 1 эпика
        SubTask secondSubTask = new SubTask ("Do the mill", "Mill parts on a milling machine", Status.NEW, firstEpic.getIdentifier ());
        manager.createSubTask (secondSubTask);                  //Создание 2й сабтаски 1 эпика
        SubTask thirdSubTask = new SubTask ("To eat", "Just eat silently", Status.NEW, firstEpic.getIdentifier ());
        manager.createSubTask (thirdSubTask);                   //Создание 3й сабтаски 1 эпика
        EpicTask secondEpic = new EpicTask ("Go to lunch", "Go to the cafeteria");
        manager.createEpicTask (secondEpic);                    //Создание 2 эпика

        manager.searchEpicWithId (3);                         //Просматриваю Эпик
        manager.searchEpicWithId (7);                         //Просматриваю Эпик
        System.out.println (manager.history ());              //Вывожу историю просмотров
        manager.searchSubTaskWithId (4);                      //Просматриваю сабтаск
        manager.searchSubTaskWithId (4);                      //Просматриваю сабтаск
        manager.searchSubTaskWithId (5);                      //Просматриваю сабтаск
        manager.searchSubTaskWithId (6);                      //Просматриваю сабтаск
        System.out.println (manager.history ());              //Вывожу историю просмотров
        manager.searchTaskWithId (1);                         //Просматриваю таск
        manager.searchTaskWithId (2);                         //Просматриваю таск
        manager.searchTaskWithId (2);                         //Просматриваю таск
        System.out.println (manager.history ());              //Вывожу историю просмотров
        manager.searchTaskWithId (1);                         //Просматриваю таск
        manager.searchTaskWithId (2);                         //Просматриваю таск
        manager.searchTaskWithId (2);                         //Просматриваю таск
        System.out.println (manager.history ());              //Вывожу историю просмотров
        manager.removeTaskWithId (2);                         //Удаляю таск №2
        System.out.println (manager.history ());              //Вывожу историю просмотров
        manager.removeEpicTaskWithId (3);                     //Удаляю эпик, в котором было 3 подзадачи
        System.out.println (manager.history ());              //Вывожу историю просмотров
    }

}
