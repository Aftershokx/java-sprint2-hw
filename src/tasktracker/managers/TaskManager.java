package tasktracker.managers;

import tasktracker.tasks.EpicTask;
import tasktracker.tasks.SubTask;
import tasktracker.tasks.Task;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;


public interface TaskManager {

    ArrayList<EpicTask> getEpicTasks ();                  //Распаковка эпиков

    ArrayList<SubTask> getSubTasks ();                   //Распаковка подзадач

    ArrayList<Task> getTasks ();                          //Распаковка задач

    void createTask (Task task);                                            //Создание задач

    EpicTask searchEpicWithId (int id);                                     //Поиск эпика по Ид

    SubTask searchSubTaskWithId (int id);                                   //Поиск подзадачи по Ид

    Task searchTaskWithId (int id);                                         //Поиск задачи по Ид

    void clearEpics ();                                                     //Удаление всех эпиков

    void clearSubTasks ();                                                 //Удаление всех подзадач

    void clearTasks ();                                                    //Удаление всех задач

    void removeSubTaskWithId (int id);                                     //Удаление подзадач по ТД

    void removeTaskWithId (int id);                                        //Удаление задач по ИД

    void removeEpicTaskWithId (int id);                                    //Удаление Эпиков по ИД

    void updateEpicTask (EpicTask epicTask);                               //Замена эпика

    void updateTask (Task task);                                            //Замена задачи

    void updateSubTask (SubTask subTask);                                   //Замена подзадачи

    TreeSet<Task> getPrioritizedTasks ();                                   //Сортировка задач и подзадач по времени начала

    List<Task> history ();                                          //Получение истории

}

