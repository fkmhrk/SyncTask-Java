package jp.fkmsoft.libs.sync;

import java.util.ArrayList;
import java.util.List;

/**
 * Demo task
 */
public class DemoSyncTask extends SyncTask<DemoObject> {
    private final List<DemoObject> mServerObjects;
    private final List<DemoObject> mLocalObjects;
    public final List<DemoObject> mUploadedObjets;
    public final List<DemoObject> mSavedObjects;
    private boolean mCalledPreDownloadObject;
    private boolean mCalledPostDownloadObject;

    private Exception mDoFetchError;

    public DemoSyncTask(List<DemoObject> serverObjects, List<DemoObject> localObjects) {
        this.mServerObjects = serverObjects;
        this.mLocalObjects = localObjects;
        mUploadedObjets = new ArrayList<DemoObject>(localObjects.size());
        mSavedObjects = new ArrayList<DemoObject>(localObjects.size());
        mCalledPreDownloadObject = false;
        mCalledPostDownloadObject = false;
    }

    @Override
    protected void doFetch() {
        if (mDoFetchError != null) {
            failed(mDoFetchError);
            return;
        }
        doneFetch(mServerObjects);
    }

    @Override
    protected void prePutDownloadedObject() throws Exception {
        mCalledPreDownloadObject = true;
    }

    @Override
    protected void putDownloadedObject(DemoObject item) throws Exception {
        if (item instanceof DemoErrorObject) {
            throw ((DemoErrorObject) item).getException();
        }
    }

    @Override
    protected void postPutDownloadedObject() {
        mCalledPostDownloadObject = true;
    }

    @Override
    protected void doGetModifiedObjects() {
        doneGetModifiedObjects(mLocalObjects);
    }

    @Override
    protected void doUploadObject(DemoObject obj) {
        if (obj instanceof DemoErrorObject) {
            failedUpload(((DemoErrorObject) obj).getException());
            return;
        }
        mUploadedObjets.add(obj);
        doneUpload(obj);
    }

    @Override
    protected void putModifiedObject(DemoObject uploadedObject) {
        mSavedObjects.add(uploadedObject);
        donePutModifiedObject(uploadedObject);
    }

    @Override
    protected void saveLastSyncTime() {

    }
    
    // getter setter

    public void setDoFetchError(Exception doFetchError) {
        this.mDoFetchError = doFetchError;
    }
}
