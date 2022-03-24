package tasktracker.manager;

import tasktracker.tasks.Task;
import tasktracker.utility.Node;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class InMemoryHistoryManager implements HistoryManager {

    private Node<Task> head;
    private Node<Task> tail;

    private final HashMap<Integer, Node<Task>> history = new HashMap<> ();

    public void removeNode (Node<Task> node) {                                          //Удаление узла
        if (node == null) {
            return;
        }
        final Node<Task> next = node.next;
        final Node<Task> prev = node.prev;

        if (prev == null) {
            head = next;
        } else {
            prev.next = next;
            node.prev = null;
        }
        if (next == null) {
            tail = prev;
        } else {
            next.prev = prev;
            node.next = null;
        }
        node.data = null;
    }

    private void linkLast (Task task) {                                                  //Добавление в хвост
        remove (task.getIdentifier ());
        final Node<Task> oldTail = tail;
        final Node<Task> newNode = new Node<> (tail, task, null);
        tail = newNode;
        if (oldTail == null)
            head = newNode;
        else
            oldTail.next = newNode;
        history.put (task.getIdentifier (), newNode);
    }

    @Override
    public void remove (int id) {                                                        //Удаление задачи из истории
        removeNode (history.get (id));
    }

    @Override
    public void add (Task task) {                                                         //Добавление задач в историю
        if (task == null) {
            return;
        }
        if (history.size () >= 10) {
            remove (10);
        }
        linkLast (task);
    }

    @Override
    public List<Task> getHistory () {                                                     //Преобразование в список
        final ArrayList<Task> tasks = new ArrayList<> ();
        Node<Task> current = head;
        while (current != null) {
            tasks.add (current.data);
            current = current.next;
        }
        return tasks;
    }
}
