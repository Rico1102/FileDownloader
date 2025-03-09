import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.util.UUID;


public class FileRequest {

    private final StringProperty fileName;
    private final String id;
    private final StringProperty location;
    private final StringProperty url;
    private final StringProperty progress;

    private final StringProperty status = new SimpleStringProperty("Queued");

    private final StringProperty speed = new SimpleStringProperty("0 bytes/s");

    public FileRequest(String fileName, String location, String url, String progress) {
        this.id = UUID.randomUUID().toString();
        this.fileName = new SimpleStringProperty(fileName);
        this.location = new SimpleStringProperty(location);
        this.url = new SimpleStringProperty(url);
        this.progress = new SimpleStringProperty(progress);
    }


    public String getFileName() {
        return fileName.get();
    }

    public String getId() {
        return id;
    }

    public String getLocation() {
        return location.get();
    }

    public String getUrl() {
        return url.get();
    }

    public String getProgress() {
        return progress.get();
    }

    public void setProgress(String s) {
        progress.set(s);
    }

    public String getStatus() {
        return status.get();
    }

    public void setStatus(String s) {
        status.set(s);
    }

    public String getSpeed() {
        return speed.get();
    }

    public void setSpeed(String s) {
        speed.set(s);
    }
}
