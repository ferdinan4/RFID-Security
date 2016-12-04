package restfulapi;

public class HttpResponse {
    private int code;
    private String url, body;
    private byte[] rawData;
    private boolean isCached;

    public HttpResponse(String url) {
        this.url = url;
    }

    public HttpResponse(String url, int code, String body) {
        this.url = url;
        this.code = code;
        this.body = body;
        isCached = false;
    }

    public String getUrl() {
        return url;
    }

    public int getCode() {
        return code;
    }

    public String getBody() {
        return body;
    }

    public byte[] getRawData() {
        return rawData;
    }

    public boolean isCached() {
        return this.isCached;
    }

    public void setRawData(byte[] rawData) {
        this.rawData = rawData;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public void setBody(String body) {
        this.body = body;
    }

    protected void setCached(boolean isCached) {
        this.isCached = isCached;
    }

}
