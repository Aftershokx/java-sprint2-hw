package tasktracker.server.getters;

import java.net.URI;
import java.util.OptionalInt;

public class QueryParametersGetter {

    //Получение ИД из параметров запроса
    public static OptionalInt getId (URI uri) {
        String[] params = uri.getQuery ().split ("&");

        for (String param : params) {
            String name = param.split ("=")[0];
            String value = param.split ("=")[1];
            if (name.equals ("id")) {
                return OptionalInt.of (Integer.parseInt (value));
            }
        }
        return OptionalInt.empty ();
    }

}
