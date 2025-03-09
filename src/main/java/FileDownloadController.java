import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FileDownloadController {

    Set<String> downloadInQueue;
    Map<String, FileDownloader> downloadInProgressMap;
    Map<String, FileDownloader> downloadCompletedMap;

    ExecutorService executor = Executors.newFixedThreadPool(2);

    public FileDownloadController() {
        downloadInQueue = new HashSet<>();
        downloadInProgressMap = new HashMap<>();
        downloadCompletedMap = new HashMap<>();
    }

    public void submitDownloadRequest(String fileUrl, String downloadDir, String fileName, String downloadId, CountDownLatch countDownLatch) {
        downloadInQueue.add(downloadId);
        executor.submit(new Thread(() -> {
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            System.out.println("Starting Download for file " + fileName);
            countDownLatch.countDown();
            FileDownloader fileDownloader = new FileDownloader(fileUrl, downloadDir, fileName);
            this.downloadInQueue.remove(downloadId) ;
            downloadInProgressMap.put(downloadId, fileDownloader) ;
            fileDownloader.download();
        }));
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

    private String getSpeed(Long bytes, double denominator) {
        return String.format("%.2f", bytes / (denominator * 5)); //Divided by 5 because its average speed of last 5 secs
    }

    public String getDownloadSpeed(String downloadId) {
        if (downloadInProgressMap.containsKey(downloadId)) {
            Long bytesDownloadedInLastSec = downloadInProgressMap.get(downloadId).getBytesDownloadedInLastSec();
            String speed = "";
            if (bytesDownloadedInLastSec < 1024) {
                speed = getSpeed(bytesDownloadedInLastSec, 1) + " bytes/s";
            } else if (bytesDownloadedInLastSec < 1024 * 1024) {
                speed = getSpeed(bytesDownloadedInLastSec, 1024f) + " Kb/s";
            } else if (bytesDownloadedInLastSec < 1024 * 1024 * 1024L) {
                speed = getSpeed(bytesDownloadedInLastSec, 1024L * 1024) + " Mb/s";
            } else {
                speed = getSpeed(bytesDownloadedInLastSec, 1024L * 1024 * 1024) + " Gb/s";
            }
            return speed;
        } else {
            return "0 Kb/s";
        }
    }


}
