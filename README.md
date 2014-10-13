SyncTask-Java
=============

Task for Synchronization

How to use
==========
1. copy m2repository to your project
2. add the following entry to your build.gradle

```
repositories {
  maven {
    url "file://<path-to-m2repository>"
  }
}

dependencies {
  compile "jp.fkmsoft.libs:SyncTask:1.0.0"
}
```

3. create your syncTask class which extends SyncTask<T>

```
public class MySyncTask extends SyncTask<MyEntitiy> {
  @Override
  protected void doFetch() {
    mMyCloudAPI.getAll(new Callback() {
      @Override
      public void onSuccess(List<CloudObject> result>) {
        List<MyEntity> list = parseResult(result);
        doneFetch(list);
      }
    });

  }

  @Override
  protected void prePutDownloadedObject() throws Exception {
    mLastSyncTime = loadLastSyncTime();
    mDB = openDatabase();
    mDB.beginTransaction();
  }

  @Override
  protected void putDownloadedObject(MyEntitiy item) throws Exception {
    // insert MyEntity to local database.
    insert(mDB, item, mLastSyncTime);
  }

  @Override
  protected void postPutDownloadedObject() {
    mDB.commit();
    mDB.close();
    mCurrentSyncTime = System.currentTimeMillis();
  }

  @Override
  protected void doGetModifiedObjects() {
    mDB = openDatabase();
    Cursor cursor = mDB.query();
    List<MyEntity> list = parseResult(cursor);
    doneGetModifiedObjects(list);
  }

  @Override
  protected void doUploadObject(MyEntitiy obj) {
    // upload object to your cloud
    mMyCloudAPI.upload(toCloudObject(obj), new Callback() {
      @Override
      public void onSuccess(CloudObject obj) {
        MyEntity entity = parseCloudObject(obj);
        doneUpload(entity);
      }
    });
  }

  @Override
  protected void putModifiedObject(MyEntitiy uploadedObject) {
    update(mDB, uploadedObject);
    donePutModifiedObject(uploadedObject, mCurrentSyncTime);
  }

  @Override
  protected void saveLastSyncTime() {
    saveSyncTime(mCurrentSyncTime);
    mDB.close();
  }  
}

```

4. call sync() method

```
final DemoSyncTask task = new DemoSyncTask(savedObjects, localObjects);
task.sync(new SyncTask.SyncListener() {
  @Override
  public void onSuccess(SyncResult result) {
    // sync is done!
  }

  @Override
  public void onError(SyncResult result, Exception e) {
    Assert.fail("error result=" + result);
  }          
});
```

