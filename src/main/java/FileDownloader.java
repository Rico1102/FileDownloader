import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class FileDownloader {

    private final String downloadDir;
    private final String fileName;
    private final CompletionTracker completionTracker;
    private final long fileSize;
    private Long chunkSize = 1024L * 1024L * 100; //10Mb
    private final ThreadPoolExecutor threadPoolExecutor;
    private final long maxFileSize = 1024L * 1024 * 1024 * 5; //max allowed file size is 5 gb
    private String fileUrl;

    public FileDownloader(String fileUrl, String downloadDir, String fileName) {
        this.fileUrl = this.reformFileUrl(fileUrl);
        this.downloadDir = downloadDir;
        this.fileName = fileName;
        this.fileSize = this.getFileSize(fileUrl);
        this.setChunkSize();
        this.completionTracker = new CompletionTracker(Math.max(this.fileSize / this.chunkSize, 1L));
        this.threadPoolExecutor = new ThreadPoolExecutor(5, 5, 1000, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>((int) (maxFileSize / this.chunkSize + 1)));
    }

    private void setChunkSize(){
        if(this.fileSize<(1024L*1024L*100)){
            //if less than 100mb set chunk size as 10mb
            this.chunkSize = 1024*1024*10L ;
        }
        else if(this.fileSize<(1024L*1024L*1024L)){
            //if between 100mb and 1 gb set chunk size as 100mb
            this.chunkSize = 1024*1024*100L ;
        }
        else{
            //if more than 1gb then set chunk size as 200mb
            this.chunkSize = 1024*1024*200L ;
        }
    }

    private String reformFileUrl(String fileUrl) {
        String updatedUrl = fileUrl;
        if (fileUrl.contains("drive.google.com")) {
            String fileId = fileUrl.split("/")[5];
            updatedUrl = "https://drive.usercontent.google.com/download?confirm=t&id=" + fileId;
        }
        System.out.println("Updated URL: " + updatedUrl);
        return updatedUrl;
    }

    private long getFileSize(String fileUrl) {
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(fileUrl).openConnection();
            connection.setRequestMethod("HEAD");
            long fileSize = connection.getContentLengthLong();
            connection.disconnect();
            System.out.println(String.format("File Size: %d", fileSize));
            return fileSize;
        } catch (IOException e) {
//            e.printStackTrace();
            System.out.println("Not Able to reach the url");
        }
        return 0;
    }

    public void download() {
        // Download the file using multiple threads
        try {
            if (this.fileSize > this.maxFileSize) {
                System.out.println("File size is too large, Aborting the download.");
                return;
            }
            RandomAccessFile randomAccessFile = new RandomAccessFile(this.downloadDir + "\\" + this.fileName, "rw");
            randomAccessFile.setLength(this.fileSize);
            randomAccessFile.close();
            long numberOfThreads = Math.max(this.fileSize / this.chunkSize, 1L);
            System.out.println(String.format("Number of threads: %d", numberOfThreads));
            long remainingBytes = this.fileSize % this.chunkSize;
            System.out.println(String.format("Remaining bytes: %d", remainingBytes));
            Thread progressThread = new Thread(this.completionTracker::showProgress);
            progressThread.start();
            for (int i = 0; i < numberOfThreads; ++i) {
                long startByte = i * this.chunkSize;
                long endByte = (i + 1) * this.chunkSize - 1;
                if (i == numberOfThreads - 1) {
                    endByte += remainingBytes;
                }
                WorkerNodes workerNode = new WorkerNodes(this.fileUrl, this.downloadDir, this.fileName, startByte, endByte, this.completionTracker);
                threadPoolExecutor.submit(workerNode);
            }
            threadPoolExecutor.shutdown();
            threadPoolExecutor.awaitTermination(30, TimeUnit.MINUTES);
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Not Able to create the file in desired location");
        } catch (InterruptedException e) {
            e.printStackTrace();
            System.out.println("Thread pool executor interrupted");
        }
    }

    public float getDownloadProgress() {
        return this.completionTracker.getPercentage();
    }

    public Long getBytesDownloadedInLastSec() {
        return this.completionTracker.getDownloadSpeed();
    }

}
