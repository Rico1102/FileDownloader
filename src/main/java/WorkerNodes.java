import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class WorkerNodes implements Runnable {

    private final String fileUrl;
    private final String downloadDir;
    private final String fileName;
    private final long startByte;
    private final long endByte;

    private final CompletionTracker completionTracker;

    private final CountDownLatch countDownLatch;

    private final ReentrantLock reentrantLock = new ReentrantLock();
    private final Condition condition = reentrantLock.newCondition();
    private final String threadName;
    private boolean isPaused = false;
    private boolean isCancelled = false;

    public WorkerNodes(String fileUrl, String downloadDir, String fileName, long startByte, long endByte, CompletionTracker completionTracker, CountDownLatch countDownLatch, String threadName) {
        this.fileUrl = fileUrl;
        this.downloadDir = downloadDir;
        this.fileName = fileName;
        this.startByte = startByte;
        this.endByte = endByte;
        this.completionTracker = completionTracker;
        this.countDownLatch = countDownLatch;
        this.threadName = threadName;
    }

    @Override
    public void run() {
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(this.fileUrl).openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Range", "bytes=" + this.startByte + "-" + this.endByte);
//            System.out.println("Thread " + Thread.currentThread().getId() + " is downloading bytes " + this.startByte + " to " + this.endByte);
            InputStream inputStream = connection.getInputStream();
            byte[] buffer = new byte[4 * 1024 * 1024];
            int bytesRead;
            RandomAccessFile randomAccessFile = new RandomAccessFile(this.downloadDir + "\\" + this.fileName, "rw");
            randomAccessFile.seek(this.startByte);
//            System.out.println("Thread " + Thread.currentThread().getId() + " is writing to file from " + this.startByte + " to " + this.endByte);
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                try {
                    this.reentrantLock.lock();
                    while (this.isPaused) {
                        this.condition.await();
                    }
                    // Write the buffer to the file
                    completionTracker.markBytesDownloaded((long) bytesRead);
                    randomAccessFile.write(buffer, 0, bytesRead);
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    this.isCancelled = true;
                    break;
                } finally {
                    this.reentrantLock.unlock();
                }
            }
            if(!this.isCancelled){
                completionTracker.markChunkCompleted();
                countDownLatch.countDown();
            }
            randomAccessFile.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean pause() {
        this.reentrantLock.lock();
        try {
            System.out.println("Pausing the thread {}" + Thread.currentThread().getName());
            this.isPaused = true;
        } finally {
            this.reentrantLock.unlock();
        }
        return true;
    }

    public boolean resume() {
        try {
            this.reentrantLock.lock();
            System.out.println("Resuming the thread {}" + Thread.currentThread().getName());
            this.isPaused = false;
            this.condition.signal();
        } finally {
            this.reentrantLock.unlock();
        }
        return true;
    }

    public String getThreadName() {
        return this.threadName;
    }
}
