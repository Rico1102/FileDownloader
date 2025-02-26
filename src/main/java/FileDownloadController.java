public class FileDownloadController {

    public static void main(String[] args) {
//        String fileUrl = "https://www.learningcontainer.com/wp-content/uploads/2020/05/sample-mp4-file.mp4";
        String downloadDir = "E:\\Playground\\FileDownloader";
        String fileUrl = "https://drive.usercontent.google.com/download?id=1_sXdYflb3z9TgV7A_R9m9kdtRao4M6-8&confirm=t";
        FileDownloader fileDownloader = new FileDownloader(fileUrl, downloadDir, "IMG_3317.MOV");
        fileDownloader.download();
    }

}
