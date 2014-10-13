package jp.fkmsoft.libs.sync;

import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Task for {@link jp.fkmsoft.libs.sync.SyncTask}
 */
public class TestSyncTask {

    @Test
    public void test_0000_ok() {
        List<DemoObject> savedObjects = new ArrayList<DemoObject>();
        savedObjects.add(new DemoObject());
        List<DemoObject> localObjects = new ArrayList<DemoObject>();
        localObjects.add(new DemoObject());
        localObjects.add(new DemoObject());
        final DemoSyncTask task = new DemoSyncTask(savedObjects, localObjects);
        task.sync(new SyncTask.SyncListener() {
            @Override
            public void onSuccess(SyncResult result) {
                if (result == null) {
                    Assert.fail("result must not be null");
                }
                if (result.getUploadCount() != 2) {
                    Assert.fail("upload count must be 2 but " + result.getUploadCount());
                }
                if (result.getDownloadCount() != 1) {
                    Assert.fail("download count must be 1 but " + result.getDownloadCount());
                }
                if (task.mSavedObjects.size() != 2) {
                    Assert.fail("saved count must be 2 but " + task.mSavedObjects.size());
                }
                if (task.mUploadedObjets.size() != 2) {
                    Assert.fail("uploaded count must be 2 but " + task.mUploadedObjets.size());
                }
            }

            @Override
            public void onError(SyncResult result, Exception e) {
                Assert.fail("error result=" + result);
            }
        });
    }

    @Test
    public void test_0001_fetch_error() {
        List<DemoObject> savedObjects = new ArrayList<DemoObject>();
        savedObjects.add(new DemoObject());
        List<DemoObject> localObjects = new ArrayList<DemoObject>();
        localObjects.add(new DemoObject());
        localObjects.add(new DemoObject());
        final DemoSyncTask task = new DemoSyncTask(savedObjects, localObjects);
        task.setDoFetchError(new RuntimeException());
        task.sync(new SyncTask.SyncListener() {
            @Override
            public void onSuccess(SyncResult result) {
                Assert.fail("onSuccess must not be called result=" + result);
            }

            @Override
            public void onError(SyncResult result, Exception e) {
                if (e == null) {
                    Assert.fail("Exception must not be null " + e);
                }
                if (!(e instanceof RuntimeException)) {
                    Assert.fail("Unexpected exception " + e.getMessage());
                }

                if (result.getUploadCount() != 0) {
                    Assert.fail("Unexpected upload count " + result.getUploadCount());
                }
                if (result.getDownloadCount() != 0) {
                    Assert.fail("Unexpected download count " + result.getDownloadCount());
                }
            }
        });
    }

    @Test
    public void test_0002_put_error() {
        List<DemoObject> savedObjects = new ArrayList<DemoObject>();
        savedObjects.add(new DemoObject());
        savedObjects.add(new DemoErrorObject(new RuntimeException()));
        List<DemoObject> localObjects = new ArrayList<DemoObject>();
        localObjects.add(new DemoObject());
        localObjects.add(new DemoObject());
        final DemoSyncTask task = new DemoSyncTask(savedObjects, localObjects);
        task.sync(new SyncTask.SyncListener() {
            @Override
            public void onSuccess(SyncResult result) {
                Assert.fail("onSuccess must not be called result=" + result);
            }

            @Override
            public void onError(SyncResult result, Exception e) {
                if (e == null) {
                    Assert.fail("Exception must not be null " + e);
                }
                if (!(e instanceof RuntimeException)) {
                    Assert.fail("Unexpected exception " + e.getMessage());
                }

                if (result.getUploadCount() != 0) {
                    Assert.fail("Unexpected upload count " + result.getUploadCount());
                }
                if (result.getDownloadCount() != 2) {
                    Assert.fail("Unexpected download count " + result.getDownloadCount());
                }
                if (task.isRunning()) {
                    Assert.fail("Task must not be running");
                }
            }
        });
    }

    @Test
    public void test_0002_upload_error() {
        List<DemoObject> savedObjects = new ArrayList<DemoObject>();
        savedObjects.add(new DemoObject());
        List<DemoObject> localObjects = new ArrayList<DemoObject>();
        localObjects.add(new DemoObject());
        localObjects.add(new DemoErrorObject(new IOException()));
        localObjects.add(new DemoObject());
        final DemoSyncTask task = new DemoSyncTask(savedObjects, localObjects);
        task.sync(new SyncTask.SyncListener() {
            @Override
            public void onSuccess(SyncResult result) {
                Assert.fail("onSuccess must not be called result=" + result);
            }

            @Override
            public void onError(SyncResult result, Exception e) {
                if (e == null) {
                    Assert.fail("Exception must not be null " + e);
                }
                if (!(e instanceof IOException)) {
                    Assert.fail("Unexpected exception " + e);
                }

                if (result.getUploadCount() != 1) {
                    Assert.fail("Unexpected upload count " + result.getUploadCount());
                }
                if (result.getDownloadCount() != 1) {
                    Assert.fail("Unexpected download count " + result.getDownloadCount());
                }
                if (task.isRunning()) {
                    Assert.fail("Task must not be running");
                }
            }
        });
    }
}
