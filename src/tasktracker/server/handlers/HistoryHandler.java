package tasktracker.server.handlers;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import tasktracker.managers.TaskManager;
import tasktracker.utility.exceptions.RequestException;


import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class HistoryHandler implements HttpHandler {
    private final TaskManager taskManager;
    private final Gson gson;

    public HistoryHandler(TaskManager taskManager, Gson gson) {
        this.taskManager = taskManager;
        this.gson = gson;
    }

    //Выбор метода
    @Override
    public void handle(HttpExchange httpExchange) {
        String response = "";
        int statusCode = 200;
        try {
            String method = httpExchange.getRequestMethod();
            if ("GET".equals (method)) {
                response = handleGetRequest ();
            } else {
                throw new RequestException ("Метод " + method + " не поддерживается");
            }
        } catch (RequestException e) {
            e.printStackTrace();
            statusCode = 400;
            response = gson.toJson(e);
        } finally {
            handlerResponse(httpExchange, response, statusCode);
        }
    }

    //Обработка метода Get = возврат истории
    private String handleGetRequest () {
        return gson.toJson(taskManager.history());
    }

    private void handlerResponse(HttpExchange httpExchange, String response, int statusCode) {
        try (OutputStream os = httpExchange.getResponseBody()) {
            httpExchange.sendResponseHeaders(statusCode, 0);
            os.write(response.getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
