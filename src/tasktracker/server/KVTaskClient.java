package tasktracker.server;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Objects;

public class KVTaskClient {
    private final HttpClient client;
    private final String url;
    private String keyApi;

    public KVTaskClient(String url) {
        this.client = HttpClient.newHttpClient();
        this.url = url;
        this.keyApi = registration();
    }

    public void setKeyApi(String keyApi) {
        this.keyApi = keyApi;
    }

    private String registration() {
        URI uri = URI.create(url + "register/");
        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(uri)
                .version(HttpClient.Version.HTTP_1_1)
                .build();

        return Objects.requireNonNull (sendRequest (request)).body();
    }

    public void put(String key, String json) {
        URI uri = URI.create(url + "save/" + key + "?API_KEY=" + keyApi);
        HttpRequest.BodyPublisher body = HttpRequest.BodyPublishers.ofString(json);
        HttpRequest request = HttpRequest.newBuilder()
                .POST(body)
                .uri(uri)
                .version(HttpClient.Version.HTTP_1_1)
                .header("Accept", "application/json")
                .build();

        sendRequest(request);
    }

    public String load(String key) {
        URI uri = URI.create(url + "load/" + key + "?API_KEY=" + keyApi);
        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(uri)
                .version(HttpClient.Version.HTTP_1_1)
                .build();
        return Objects.requireNonNull (sendRequest (request)).body();
    }

    private HttpResponse<String> sendRequest(HttpRequest request) {
        HttpResponse.BodyHandler<String> handler = HttpResponse.BodyHandlers.ofString();

        try {
            return client.send(request, handler);
        } catch (IOException | InterruptedException e) {
            e.getStackTrace();
            return null;
        }
    }
}
