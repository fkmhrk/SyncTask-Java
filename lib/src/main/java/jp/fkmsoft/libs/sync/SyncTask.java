package jp.fkmsoft.libs.sync;

import java.util.List;

import jp.fkmsoft.libs.task.TaskQueue;
import jp.fkmsoft.libs.task.impl.Task;
import jp.fkmsoft.libs.task.impl.TaskQueueImpl;

/**
 * Sync Task. Developer should extend this class and implement methods.
 */
abstract public class SyncTask<T> {
    public interface SyncListener {
        void onSuccess(SyncResult result);
        void onError(SyncResult result, Exception e);
    }

    private boolean mRunning;
    private SyncListener mCallback;
    private SyncResult mResult;
    private TaskQueue<T> mQueue;

    /**
     * Performs synchronization.
     * <ol>
     *  <li>Fetches modified objects from server.</li>
     *  <li>Updates local DB by downloaded objects.</li>
     *  <li>Gets modified objects from local DB.</li>
     *  <li>Uploads modified objects and Updates local DB by uploaded objects.</li>
     *  <li>Updates modified time.</li>
     * </ol>
     * @param callback callback listener
     */
    public void sync(SyncListener callback) {
        if (mRunning) {
            callback.onError(new SyncResult(), new SyncException());
            return;
        }
        mRunning = true;
        mResult = new SyncResult();
        doFetch();
    }

    /**
     * This method will be called when downloading will be started.
     * Call {@link jp.fkmsoft.libs.sync.SyncTask#doneFetch(java.util.List)} if download is succeeded.
     * Call {@link jp.fkmsoft.libs.sync.SyncTask#failed(Exception)} if failed.
     */
    protected abstract void doFetch();

    protected void failed(Exception e) {
        mCallback.onError(mResult, e);
        mRunning = false;
    }

    protected void doneFetch(List<T> fetchedObjects) {
        mResult.addDownloadCount(fetchedObjects.size());
        try {
            prePutDownloadedObject();
        } catch (Exception e) {
            failed(e);
            return;
        }

        try {
            for (T item : fetchedObjects) {
                putDownloadedObject(item);
            }
        } catch (Exception e) {
            failed(e);
            return;
        }

        try {
            postPutDownloadedObject();
        } catch (Exception e) {
            failed(e);
            return;
        }

        getModifiedObjects();
    }

    /**
     * This method will be called before putting downloaded objects to local storage.
     * In this method, developer will open local storage and start transaction.
     * @throws Exception when preparation is failed.
     */
    protected abstract void prePutDownloadedObject() throws Exception;

    /**
     * Put downloaded object to local storag
     * @param item the item
     * @throws Exception when execution is failed
     */
    protected abstract void putDownloadedObject(T item) throws Exception;

    /**
     * This method will be called after putting downloaded objects to local storage.
     * In this method, developer will close local storage and commit transaction.
     */
    protected abstract void postPutDownloadedObject();

    private void getModifiedObjects() {
        doGetModifiedObjects();
    }

    /**
     * This method will be called when this task needs to get modified objects.
     * Call {@link jp.fkmsoft.libs.sync.SyncTask#doneGetModifiedObjects(java.util.List)} if getting is done.
     * Call {@link jp.fkmsoft.libs.sync.SyncTask#failed(Exception)} if failed.
     */
    protected abstract void doGetModifiedObjects();

    protected void doneGetModifiedObjects(List<T> modifiedObjects) {
        mQueue = new TaskQueueImpl<T>();
        for (T item : modifiedObjects) {
            mQueue.add(new UploadTask(item));
        }
        mQueue.execute(new TaskQueue.TaskCallback<T>() {
            @Override
            public void onSuccess(List<T> list) {
                saveLastSyncTime();
                mRunning = false;
                mCallback.onSuccess(mResult);
            }

            @Override
            public void onError(List<T> uploadedObjects, Exception e) {
                failed(e);
            }
        });
    }

    /**
     * This method will be called when uploading modified object to server.
     * Call {@link jp.fkmsoft.libs.sync.SyncTask#doneUpload(Object)} if upload is succeeded.
     * Call {@link jp.fkmsoft.libs.sync.SyncTask#failedUpload(Exception)} if upload is failed.
     * @param obj modified object
     */
    protected abstract void doUploadObject(T obj);

    protected void doneUpload(T uploadedObject) {
        putModifiedObject(uploadedObject);
    }

    protected void failedUpload(Exception e) {
        mQueue.notifyError(e);
    }

    /**
     * This method will be called when uploading is done and need to update local storage.
     * Developer will put server ID / server modified time of this object.
     * Call {@link jp.fkmsoft.libs.sync.SyncTask#donePutModifiedObject(Object)} if putting is done.
     * @param uploadedObject
     */
    protected abstract void putModifiedObject(T uploadedObject);

    protected void donePutModifiedObject(T uploadedObject) {
        mResult.addUploadCount();
        mQueue.notifyResult(uploadedObject);
    }

    /**
     * This method will be called when uploading is done.
     */
    protected abstract void saveLastSyncTime();

    private class UploadTask extends Task<T> {
        private T mItem;

        UploadTask(T item) {
            this.mItem = item;
        }

        @Override
        public void execute(TaskQueue<T> queue) {
            doUploadObject(mItem);
        }
    }

}
