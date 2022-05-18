package tasktracker.server;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;


public class KVServer {
    public static final int PORT = 8078;
    private final String API_KEY;
    private final HttpServer server;
    private final Map<String, String> data = new HashMap<>();

    public KVServer() throws IOException {
        API_KEY = generateApiKey();
        server = HttpServer.create(new InetSocketAddress("localhost", PORT), 0);
        HttpHandler handlerForRegister = (h) -> {
            try {
                if ("GET".equals (h.getRequestMethod ())) {
                    sendText (h, API_KEY);
                } else {
                    System.out.println ("/register ждёт GET-запрос, а получил " + h.getRequestMethod ());
                    h.sendResponseHeaders (405, 0);
                }
            } finally {
                h.close ();
            }
        };
        server.createContext("/register", handlerForRegister);
        HttpHandler handlerForSave = (h) -> {
            try {
                if (hasAuths (h)) {
                    System.out.println ("Запрос не авторизован, нужен параметр в query API_KEY со значением апи-ключа");
                    h.sendResponseHeaders (403, 0);
                    return;
                }
                if ("POST".equals (h.getRequestMethod ())) {
                    String key = h.getRequestURI ().getPath ().substring ("/save/".length ());
                    if (key.isEmpty ()) {
                        System.out.println ("Key для сохранения пустой. key указывается в пути: /save/{key}");
                        h.sendResponseHeaders (400, 0);
                        return;
                    }
                    String value = readText (h);
                    if (value.isEmpty ()) {
                        System.out.println ("Value для сохранения пустой. value указывается в теле запроса");
                        h.sendResponseHeaders (400, 0);
                        return;
                    }
                    data.put (key, value);
                    System.out.println ("Значение для ключа " + key + " успешно обновлено!");
                    h.sendResponseHeaders (200, 0);
                } else {
                    System.out.println ("/save ждёт POST-запрос, а получил: " + h.getRequestMethod ());
                    h.sendResponseHeaders (405, 0);
                }
            } finally {
                h.close ();
            }
        };
        server.createContext("/save", handlerForSave);
        HttpHandler handlerForLoad = (h) -> {
            try {
                if (hasAuths (h)) {
                    System.out.println ("Запрос не авторизован, нужен параметр в query API_KEY со значением апи-ключа");
                    h.sendResponseHeaders (403, 0);
                    return;
                }

                if ("GET".equals (h.getRequestMethod ())) {
                    String key = h.getRequestURI ().getPath ().substring ("/load/".length ());
                    if (key.isEmpty ()) {
                        System.out.println ("Key для сохранения пустой. key указывается в пути: /save/{key}");
                        h.sendResponseHeaders (400, 0);
                        return;
                    }
                    String value = data.get (key);
                    if (value != null) {
                        sendText (h, value);
                        System.out.println ("Значение для ключа " + key + " успешно получено!");
                    } else {
                        System.out.println ("Значение для ключа " + key + " нет!");
                        h.sendResponseHeaders (400, 0);
                    }
                } else {
                    System.out.println ("/load ждёт GET-запрос, а получил: " + h.getRequestMethod ());
                    h.sendResponseHeaders (405, 0);
                }
            } finally {
                h.close ();
            }
        };
        server.createContext("/load", handlerForLoad);
    }

    public void start() {
        System.out.println("Запускаем KV сервер на порту " + PORT);
        System.out.println("API_KEY: " + API_KEY);
        server.start();
    }

    public void stop() {
        server.stop(1);
    }

    private String generateApiKey() {
        return "" + System.currentTimeMillis();
    }

    protected boolean hasAuths (HttpExchange h) {
        String rawQuery = h.getRequestURI().getRawQuery();
        return rawQuery == null || (!rawQuery.contains ("API_KEY=" + API_KEY) && !rawQuery.contains ("API_KEY=DEBUG"));
    }

    protected String readText(HttpExchange h) throws IOException {
        return new String(h.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
    }

    protected void sendText(HttpExchange h, String text) throws IOException {
        byte[] resp = text.getBytes(StandardCharsets.UTF_8);
        h.getResponseHeaders().add("Content-Type", "application/json");
        h.sendResponseHeaders(200, resp.length);
        h.getResponseBody().write(resp);
    }
}
