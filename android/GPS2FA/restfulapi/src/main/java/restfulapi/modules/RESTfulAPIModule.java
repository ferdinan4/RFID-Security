package restfulapi.modules;

import restfulapi.HttpMethod;
import restfulapi.HttpResponse;
import restfulapi.RESTfulAPI;

public abstract class RESTfulAPIModule {

    private final String tag;

    protected RESTfulAPIModule(String tag) {
        this.tag = tag;
    }

    public void onPreExecute(RESTfulAPI restApi, HttpMethod request, boolean sync) {
    }

    public void onPostExecute(RESTfulAPI restApi, HttpMethod request, HttpResponse response, boolean sync) {
    }

    public String getTag() {
        return tag;
    }

}
