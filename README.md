# FileDownloader

**FileDownloader** is a multi-threaded file downloading application designed to efficiently download large files from remote locations with support for pausing, resuming, and cancellation.

## Features
- 🚀 **Multi-threaded Downloads** for improved performance
- ⏸️ **Pause & Resume Support** for flexibility
- ❌ **Cancel Download** with cleanup
- 📈 **Real-time Progress Tracking** with speed details
- 🖥️ **User Interface** powered by JavaFX

## Installation
### Prerequisites
- **Java 17+**
- **JavaFX** (for UI version)

### Build Instructions
```bash
# Clone the repository
git clone https://github.com/Rico1102/FileDownloader.git
cd FileDownloader

# Compile the project
javac *.java

# Run the UI version
java FileDownloaderUI

# Run the CLI version -- still in development
java FileDownloadController
```

## Usage
### Command Line Interface (CLI) --- **(In Development)**
```bash
java FileDownloadController [options] <URL>
```
**Options:**
- `-o <outputDir>` : Specify output directory
- `-t <threads>` : Number of threads for concurrent download
- `-p` : Pause active download
- `-r` : Resume paused download
- `-c` : Cancel active download

**Example:**
```bash
java FileDownloadController -o "~/Downloads" -t 4 https://example.com/largefile.zip
```

### User Interface (UI)
1. Enter the **URL**, **Download Location**, and **File Name**.
2. Click **Add to Queue**.
3. Use the **Pause**, **Resume**, or **Cancel** buttons for active downloads.

## Project Structure
```
src/
├── CompletionTracker.java
├── FileDownloadController.java
├── FileDownloader.java
├── FileDownloaderUI.java
├── FileRequest.java
└── WorkerNodes.java
```

## Known Issues & Improvements
- Add retry logic for network interruptions.
- Implement more dynamic thread scaling.
- Improve UI responsiveness during large file downloads.

## Contributing
1. Fork the repository.
2. Create a new branch (`git checkout -b feature-name`).
3. Commit your changes (`git commit -m 'Add feature'`).
4. Push the branch (`git push origin feature-name`).
5. Submit a Pull Request.

## License
This project is licensed under the **MIT License**. See the `LICENSE` file for details.

