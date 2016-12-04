package restfulapi.utils;

import java.util.HashMap;
import java.util.Iterator;

public class QueryParameters {

    private HashMap<String, String> parameters;

    public QueryParameters() {
        parameters = new HashMap<String, String>();
    }

    public QueryParameters add(String key, String value) {
        parameters.put(key, value);
        return this;
    }

    public String getWellFormedQueryParameters() {
        String key;
        StringBuilder result = new StringBuilder();
        Iterator<String> keysIterator = parameters.keySet().iterator();

        if (keysIterator.hasNext()) {
            key = keysIterator.next();
            result.append("?" + key + "=" + parameters.get(key));
            while (keysIterator.hasNext()) {
                key = keysIterator.next();
                result.append("&" + key + "=" + parameters.get(key));
            }
        }

        return result.toString();
    }
}
