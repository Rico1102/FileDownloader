import java.util.*;
import java.util.concurrent.*;

public class FileDownloadController {

    Set<String> downloadInQueue;
    Map<String, FileDownloader> downloadInProgressMap;
    Map<String, FileDownloader> downloadCompletedMap;

    List<Future<?>> futures = new ArrayList<>();

    ExecutorService executor = Executors.newFixedThreadPool(2);

    public FileDownloadController() {
        downloadInQueue = new HashSet<>();
        downloadInProgressMap = new HashMap<>();
        downloadCompletedMap = new HashMap<>();
    }

    public void submitDownloadRequest(String fileUrl, String downloadDir, String fileName, String downloadId, CountDownLatch countDownLatch) {
        downloadInQueue.add(downloadId);
        futures.add(executor.submit(new Thread(() -> {
            System.out.println("Starting Download for file " + fileName);
            countDownLatch.countDown();
            FileDownloader fileDownloader = new FileDownloader(fileUrl, downloadDir, fileName);
            this.downloadInQueue.remove(downloadId) ;
            downloadInProgressMap.put(downloadId, fileDownloader) ;
            fileDownloader.download();
        })));
    }

    public void pauseDownload(String downloadId) {
        System.out.println("Pausing download for " + downloadId);
        if (downloadInProgressMap.containsKey(downloadId)) {
            downloadInProgressMap.get(downloadId).pauseDownload();
        }
    }

    public void resumeDownload(String downloadId) {
        System.out.println("Resuming download for " + downloadId);
        if (downloadInProgressMap.containsKey(downloadId)) {
            downloadInProgressMap.get(downloadId).resumeDownload();
        }
    }

    public void cancelDownload(String downloadId) {
        System.out.println("Cancelling download for " + downloadId);
        if (downloadInProgressMap.containsKey(downloadId)) {
            downloadInProgressMap.get(downloadId).cancelDownload();
        }
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

    public void shutdown() {
        try{
            logExecutorStatus();
            executor.shutdownNow();
            executor.awaitTermination(2, TimeUnit.SECONDS);
            System.out.println("Gracefully shutting down the executor service");
            logExecutorStatus();
            System.exit(0);
        } catch (InterruptedException e) {
            e.printStackTrace();
            Thread.currentThread().interrupt();
        }
    }

    private void logExecutorStatus() {
        System.out.println("Executor Service Is Shutdown : " + executor.isShutdown());
        System.out.println("Active Threads: " + ((ThreadPoolExecutor) executor).getActiveCount());
    }


}
