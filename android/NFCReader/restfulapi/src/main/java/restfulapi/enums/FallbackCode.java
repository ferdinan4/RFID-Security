package restfulapi.enums;

public enum FallbackCode {

    OTHERWISE(0), CON_FAILED(-1);

    int type;

    FallbackCode(int type) {
        this.type = type;
    }

    public int getCode() {
        return type;
    }
}

