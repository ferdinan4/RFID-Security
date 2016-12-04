package restfulapi.callbacks;

import restfulapi.HttpResponse;

public abstract class RESTCallback {
    public abstract void onResult(HttpResponse response);
}
