import java.sql.Timestamp;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class CompletionTracker {
    private final Long chunks;
    private final Deque<Long> deque;
    private final ReentrantLock reentrantLock = new ReentrantLock();
    private final Condition condition = reentrantLock.newCondition();
    private Long chunksDownloaded = 0L;
    private Long bytesDownloadedLatest = 0L;
    private boolean isPaused = false;

    public CompletionTracker(Long chunks) {
        if (chunks == 0) {
            throw new IllegalArgumentException("Chunks cannot be zero");
        }
        this.chunks = chunks;
        System.out.println("Total Chunks: " + this.chunks);
        this.deque = new ArrayDeque<>(5);
        //initial speeds
        this.deque.add(0L);
        this.deque.add(0L);
        this.deque.add(0L);
        this.deque.add(0L);
        this.deque.add(0L);
    }

    public synchronized void markChunkCompleted() {
        this.chunksDownloaded += 1L;
    }

    public synchronized void markBytesDownloaded(Long bytesDownloaded) {
        this.bytesDownloadedLatest += bytesDownloaded;
    }

    public synchronized Long getDownloadSpeed() {
        this.deque.poll();
        this.deque.add(bytesDownloadedLatest);
        return this.deque.peekLast() - this.deque.peekFirst();
    }

    public synchronized Float getPercentage() {
        return (this.chunksDownloaded.floatValue() / this.chunks.floatValue()) * 100.0f;
    }

    public void showProgress() {
        Timestamp startTimestamp = new Timestamp(System.currentTimeMillis());
        while (!this.chunksDownloaded.equals(this.chunks)) {
            try {
                this.reentrantLock.lock();
                while (this.isPaused) {
                    this.condition.await();
                }
                Float progress = this.getPercentage();
                String bar = "#".repeat((int) (progress / 2)) + "-".repeat((int) (50 - (progress / 2)));
                System.out.print("\r[" + bar + "] " + progress + "% Time Spent: " + (System.currentTimeMillis() - startTimestamp.getTime()) / 1000 + "s");
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
                System.out.println("Completion Tracker Thread interrupted!!!");
                Thread.currentThread().interrupt();
                break;
            } finally {
                this.reentrantLock.unlock();
            }
        }
        Float progress = this.getPercentage();
        String bar = "#".repeat((int) (progress / 2)) + "-".repeat((int) (50 - (progress / 2)));
        System.out.print("\r[" + bar + "] " + progress + "% Time Spent: " + (System.currentTimeMillis() - startTimestamp.getTime()) / 1000 + "s\n");
        Timestamp endTimestamp = new Timestamp(System.currentTimeMillis());
        System.out.println(String.format("Time taken to download the file: %.2f seconds", (endTimestamp.getTime() - startTimestamp.getTime()) / 1000f));
    }

    public boolean pause() {
        this.reentrantLock.lock();
        try {
            System.out.println("Pausing the Completion Tracker Thread");
            this.isPaused = true;
        } finally {
            this.reentrantLock.unlock();
        }
        return true;
    }

    public boolean resume() {
        try {
            this.reentrantLock.lock();
            System.out.println("Resuming the Completion Tracker Thread");
            this.isPaused = false;
            this.condition.signal();
        } finally {
            this.reentrantLock.unlock();
        }
        return true;
    }

}
