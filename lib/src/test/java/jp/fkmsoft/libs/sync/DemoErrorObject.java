package jp.fkmsoft.libs.sync;

/**
 * Demo object for error
 */
public class DemoErrorObject extends DemoObject {
    private Exception mException;

    public DemoErrorObject(Exception exception) {
        this.mException = exception;
    }

    public Exception getException() {
        return mException;
    }
}
