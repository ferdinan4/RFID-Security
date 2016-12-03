package es.furiios.restfulapi.callbacks;

import es.furiios.restfulapi.HttpResponse;

public abstract class RESTCallback {
    public abstract void onResult(HttpResponse response);
}
