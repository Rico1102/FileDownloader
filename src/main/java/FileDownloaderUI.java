import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;

public class FileDownloaderUI extends Application {

    private static final List<FileRequest> ongoingDownloads = new ArrayList<>();
    private static final FileDownloadController fileDownloadController = new FileDownloadController();
    // Observable list to track download requests
    private final ObservableList<FileRequest> downloadQueue = FXCollections.observableArrayList();

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Multi-threaded File Downloader");

        // Input Fields
        TextField fileUrlField = new TextField();
        fileUrlField.setPromptText("File URL");
        fileUrlField.setText("https://drive.usercontent.google.com/download?confirm=t&id=1-ck_USSw4YvVY3GsDMvryoN8eGRb5mDN");

        TextField locationField = new TextField();
        locationField.setPromptText("Save Location");
        locationField.setText("E:\\Playground\\FileDownloader");

        TextField fileNameField = new TextField();
        fileNameField.setPromptText("File Name");
        fileNameField.setText("1_27_gb.mov");

        // Add Button
        Button addButton = new Button("Add to Queue");

        // Input Section Layout
        VBox inputSection = new VBox(10, fileUrlField, locationField, fileNameField, addButton);

        // Container to display queued items with scroll support
        VBox queueContainer = new VBox(10);
        queueContainer.setPadding(new Insets(10));
        queueContainer.setStyle("-fx-padding: 10; -fx-background-color: #ffffff;");

        ScrollPane scrollPane = new ScrollPane(queueContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(300); // Auto-adjust to maximize height
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER); // Hide scrollbar when empty

        addButton.setOnAction(e -> {
            String fileUrl = fileUrlField.getText().trim();
            String location = locationField.getText().trim();
            String fileName = fileNameField.getText().trim();

            if (fileUrl.isEmpty() || location.isEmpty() || fileName.isEmpty()) {
                showAlert("Error", "All fields are required.");
                return;
            }

            FileRequest fileRequest = new FileRequest(fileName, location, fileUrl, "0%");
            downloadQueue.add(fileRequest);
            ongoingDownloads.add(fileRequest);

            VBox fileEntry = new VBox(5);
            fileEntry.setStyle("-fx-border-color: #4CAF50; -fx-border-radius: 8; -fx-padding: 10; -fx-background-color: #f0f0f0; -fx-pref-width: 100%; -fx-min-height: 120px; -fx-max-height: 100px;");

            Label fileDetails = new Label(
                    "Downloading: " + fileRequest.getUrl() + "\n↓\n"
                            + "Saving to: " + fileRequest.getLocation() + "/" + fileRequest.getFileName() + "\n"
            );
            fileDetails.setStyle("-fx-font-family: 'Arial'; -fx-font-size: 14; -fx-font-weight: bold;");

            ProgressBar progressBar = new ProgressBar(0);
            progressBar.setPrefWidth(Double.MAX_VALUE);
            Label statusLabel = new Label("Queued");

            fileEntry.getChildren().addAll(fileDetails, progressBar, statusLabel);
            queueContainer.getChildren().add(fileEntry);

            // Enable scrollbar only if content exceeds container size
            if (queueContainer.getChildren().size() > 4) {
                scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
            }


            submitRequest(fileRequest);
            fileRequest.setStatus("Downloading");


            Task<Void> task = new Task<Void>() {
                @Override
                protected Void call() throws Exception {
                    while (true) {
                        float progress = fileDownloadController.getDownloadProgress(fileRequest.getId());
                        String downloadSpeed = fileDownloadController.getDownloadSpeed(fileRequest.getId());
                        System.out.println("Progress: " + progress);
                        Platform.runLater(() -> {
                            progressBar.setProgress(progress / 100);
                            statusLabel.setText("Downloading  | " + downloadSpeed);
                            fileRequest.setProgress(progress + "%");
                            fileRequest.setSpeed(downloadSpeed);
                        });
                        if (progress == 100.0) {
                            ongoingDownloads.remove(fileRequest);
                            Platform.runLater(() -> {
                                statusLabel.setText("Completed");
                            });

                            fileRequest.setSpeed("0 Kb/s");
                            fileRequest.setStatus("Completed");
                            break;
                        }
                        Thread.sleep(1000);
                    }
                    this.cancel();
                    return null;
                }
            };
            new Thread(task).start();

            // Clear fields after adding
            fileUrlField.clear();
            locationField.clear();
            fileNameField.clear();
        });

        // Layout
        VBox layout = new VBox(10);
        layout.setPadding(new Insets(15));
        layout.setStyle("-fx-font-family: 'Arial'; -fx-font-size: 14; -fx-background-color: #f9f9f9;");
        layout.getChildren().addAll(
                inputSection,
                new Label("Download Queue:"),
                scrollPane
        );

        Scene scene = new Scene(layout, 600, 400);
        scene.widthProperty().addListener((obs, oldVal, newVal) -> {
            scrollPane.setPrefWidth(newVal.doubleValue());
        });
        scene.heightProperty().addListener((obs, oldVal, newVal) -> {
            scrollPane.setPrefHeight(newVal.doubleValue() - 200); // Adjust height proportionally
        });

        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void submitRequest(FileRequest fileRequest) {
        fileDownloadController.submitDownloadRequest(fileRequest.getUrl(), fileRequest.getLocation(), fileRequest.getFileName(), fileRequest.getId());
    }

    // Helper method for alerts
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
