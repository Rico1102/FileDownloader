import java.sql.Timestamp;

public class CompletionTracker {
    private final Long chunks;
    private Long chunksDownloaded = 0L;

    public CompletionTracker(Long chunks) {
        if (chunks == 0) {
            throw new IllegalArgumentException("Chunks cannot be zero");
        }
        this.chunks = chunks;
    }

    public synchronized void markChunkCompleted() {
        this.chunksDownloaded += 1L;
    }

    public synchronized Float getPercentage() {
        return (this.chunksDownloaded.floatValue() / this.chunks.floatValue()) * 100.0f;
    }

    public void showProgress() {
        Timestamp startTimestamp = new Timestamp(System.currentTimeMillis());
        while (!this.chunksDownloaded.equals(this.chunks)) {
            Float progress = this.getPercentage();
            String bar = "#".repeat((int) (progress / 2)) + "-".repeat((int) (50 - (progress / 2)));
            System.out.print("\r[" + bar + "] " + progress + "%");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        Float progress = this.getPercentage();
        String bar = "#".repeat((int) (progress / 2)) + "-".repeat((int) (50 - (progress / 2)));
        System.out.print("\r[" + bar + "] " + progress + "%\n");
        Timestamp endTimestamp = new Timestamp(System.currentTimeMillis());
        System.out.println(String.format("Time taken to download the file: %.2f seconds", (endTimestamp.getTime() - startTimestamp.getTime())/1000f));
    }

}
