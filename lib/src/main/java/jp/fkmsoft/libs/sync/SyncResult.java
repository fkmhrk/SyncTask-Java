package jp.fkmsoft.libs.sync;

/**
 * Describes a result of synchronization.
 */
public class SyncResult {
    private int mUploadCount;
    private int mDownloadCount;

    public void addUploadCount() {
        ++mUploadCount;
    }

    public void addDownloadCount(int count) {
        mDownloadCount += count;
    }
}
