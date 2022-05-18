package tasktracker.server.adapters;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.util.Arrays;

public class ExceptionAdapter extends TypeAdapter<Exception> {

    @Override
    public void write(JsonWriter jsonWriter, Exception e) throws IOException {
        jsonWriter.beginObject()
                .name("status").value(400)
                .name("message").value(e.getMessage())
                .name("stackTrace").value(Arrays.toString(e.getStackTrace()))
                .endObject();
    }

    @Override
    public Exception read(JsonReader jsonReader) throws IOException {
        return null;
    }
}
