import java.util.HashMap;
import java.util.Map;

public class FileDownloadController {

    Map<String, FileDownloader> downloadInProgressMap;
    Map<String, FileDownloader> downloadCompletedMap;

    public FileDownloadController() {
        downloadInProgressMap = new HashMap<>();
        downloadCompletedMap = new HashMap<>();
    }

    public void submitDownloadRequest(String fileUrl, String downloadDir, String fileName, String downloadId) {
        FileDownloader fileDownloader = new FileDownloader(fileUrl, downloadDir, fileName);
        downloadInProgressMap.put(downloadId, fileDownloader);
        new Thread(() -> {
            fileDownloader.download();
            downloadInProgressMap.remove(downloadId);
            downloadCompletedMap.put(downloadId, fileDownloader);
        }).start();
    }

    public float getDownloadProgress(String downloadId) {
        if (downloadInProgressMap.containsKey(downloadId)) {
            return downloadInProgressMap.get(downloadId).getDownloadProgress();
        } else if (downloadCompletedMap.containsKey(downloadId)) {
            return 100.0f;
        } else {
            return 0.0f;
        }
    }


}
