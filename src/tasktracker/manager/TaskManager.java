package tasktracker.manager;

import tasktracker.tasks.EpicTask;
import tasktracker.tasks.SubTask;
import tasktracker.tasks.Task;

import java.util.ArrayList;
import java.util.List;


public interface TaskManager {

    ArrayList<EpicTask> getEpicTasks ();                  //Распаковка мапы эпиков

    ArrayList<SubTask> getSubTasks ();                   //Распаковка мапы подзадач

    ArrayList<Task> getTasks ();                          //Распаковка мапы задач

    void createEpicTask (EpicTask epicTask);                                //создание Эпика

    void createSubTask (SubTask subTask);                                  //Создание подзадач

    void createTask (Task task);                                            //Создание задач

    EpicTask searchEpicWithId (int id);                                     //Поиск эпика по Ид

    SubTask searchSubTaskWithId (int id);                                   //Поиск подзадачи по Ид

    Task searchTaskWithId (int id);                                         //Поиск задачи по Ид

    void clearEpics ();                                                     //Удаление всех эпиков

    void clearSubTasks ();                                                 //Удаление всех подзадач

    void clearTasks ();                                                    //Удадение всех задач

    void removeSubTaskWithId (int id);                                     //Удаление подзадач по ТД

    void removeTaskWithId (int id);                                           //Удаление задач по ИД

    void removeEpicTaskWithId (int id);                                      //Удаление Эпиков по ИД

    void updateEpicTask (EpicTask epicTask);                               //Замена эпика

    void updateTask (Task task);                                            //Замена задачи

    void updateSubTask (SubTask subTask);                                   //Замена подзадачи

    List<Task> history ();                                          //Получение истории

}

