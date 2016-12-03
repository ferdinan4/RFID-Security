package es.furiios.restfulapi;

import android.content.Context;

import java.util.ArrayList;
import java.util.HashMap;

import es.furiios.restfulapi.exceptions.NoSuchInstanceException;
import es.furiios.restfulapi.modules.RESTfulAPIModule;

public class RESTfulAPI {

    private Context context;
    private String host = null;
    private ArrayList<RESTfulAPIModule> modules = new ArrayList<RESTfulAPIModule>();
    private static HashMap<String, RESTfulAPI> instances = new HashMap<String, RESTfulAPI>();

    private RESTfulAPI(Context context, String host) {
        this.context = context;
        this.host = host;
    }

    public static RESTfulAPI getInstance(String host) throws NoSuchInstanceException {
        if (instances.containsKey(host)) {
            return instances.get(host);
        }
        throw new NoSuchInstanceException();
    }

    public static RESTfulAPI createInstance(Context context, String host) {
        if (!instances.containsKey(host)) {
            instances.put(host, new RESTfulAPI(context, host));
        }
        return instances.get(host);
    }

    public void addModule(RESTfulAPIModule module) {
        modules.add(module);
    }

    public void removeModule(RESTfulAPIModule module) {
        modules.remove(module);
    }

    public ArrayList<RESTfulAPIModule> getModules() {
        return modules;
    }

    public HttpResponse execute(final HttpMethod request) {
        return exec(request, true);
    }

    public void executeAsync(final HttpMethod request) {
        exec(request, false);
    }

    private HttpResponse exec(final HttpMethod request, final boolean sync) {
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                request.execute(context, host, sync);
            }
        });
        t.start();

        if (sync) {
            try {
                t.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return request.getResponse();
    }
}
