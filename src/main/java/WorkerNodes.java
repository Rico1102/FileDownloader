import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;

public class WorkerNodes implements Runnable {

    private final String fileUrl;
    private final String downloadDir;
    private final String fileName;
    private final long startByte;
    private final long endByte;

    private final CompletionTracker completionTracker;

    public WorkerNodes(String fileUrl, String downloadDir, String fileName, long startByte, long endByte, CompletionTracker completionTracker) {
        this.fileUrl = fileUrl;
        this.downloadDir = downloadDir;
        this.fileName = fileName;
        this.startByte = startByte;
        this.endByte = endByte;
        this.completionTracker = completionTracker;
    }

    public void run() {
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(this.fileUrl).openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Range", "bytes=" + this.startByte + "-" + this.endByte);
//            System.out.println("Thread " + Thread.currentThread().getId() + " is downloading bytes " + this.startByte + " to " + this.endByte);
            InputStream inputStream = connection.getInputStream();
            byte[] buffer = new byte[1024 * 1024];
            int bytesRead;
            RandomAccessFile randomAccessFile = new RandomAccessFile(this.downloadDir + "\\" + this.fileName, "rw");
            randomAccessFile.seek(this.startByte);
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                // Write the buffer to the file
                randomAccessFile.write(buffer, 0, bytesRead);
            }
            completionTracker.markChunkCompleted();
            randomAccessFile.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
