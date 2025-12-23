

# PeerLink

PeerLink is a peer-to-peer file sharing app for quickly sending files between devices. 🚀

This project is built as a learning exercise to understand HTTP servers and socket programming in **plain Java**, without frameworks.

## What it does (File Sharing) 🔄 

PeerLink runs a small HTTP server that:

- Accepts a file upload at `POST /upload`
- Returns a **port code**
- Lets you download the file via `GET /download/{port}`

Under the hood, the uploaded file is served over a short-lived TCP socket bound to that port, then cleaned up after the transfer.

## Tech stack 🛠️

- **Java 21**
- **Maven**
- JDK built-in HTTP server: `com.sun.net.httpserver.HttpServer`
- Virtual threads: `Executors.newVirtualThreadPerTaskExecutor()` 

## Project structure 🏛️

- [peerlink.app.App](cci:2://file:///E:/IdeaProjects/PeerLink/src/main/java/peerlink/app/App.java:6:0-29:1)  
  Starts the server on port `8080`.

- [peerlink.controller.FileController](cci:2://file:///E:/IdeaProjects/PeerLink/src/main/java/peerlink/controller/FileController.java:14:0-58:1)  
  Registers HTTP routes and basic CORS handling.

- [peerlink.handler.UploadHandler](cci:2://file:///E:/IdeaProjects/PeerLink/src/main/java/peerlink/handler/UploadHandler.java:15:0-91:1)  
  Handles multipart file upload, stores the file temporarily, returns a port code.

- `peerlink.service.FileSharingService`  
  Generates an available port and listens for a one-time TCP connection to send the file.

- [peerlink.handler.FileSendingHandler](cci:2://file:///E:/IdeaProjects/PeerLink/src/main/java/peerlink/handler/FileSendingHandler.java:5:0-41:1)  
  Streams file bytes to the TCP client and deletes the temp file afterward.

- [peerlink.handler.DownloadHandler](cci:2://file:///E:/IdeaProjects/PeerLink/src/main/java/peerlink/handler/DownloadHandler.java:9:0-76:1)  
  Bridges the TCP download back to an HTTP response with `Content-Disposition: attachment`.

- [peerlink.utils.HttpRequestBodyParser](cci:2://file:///E:/IdeaProjects/PeerLink/src/main/java/peerlink/utils/HttpRequestBodyParser.java:4:0-89:1)  
  Custom minimal multipart/form-data parsing for a single uploaded file.

## Requirements ✅

- Java **21** (or newer)
- Maven **3.x**

## Run locally 🏃

From the project root:

```bash
mvn clean compile
java -cp target/classes peerlink.app.App
```

The server starts on:

- `http://localhost:8080`

Stop it by pressing **Enter** in the console.

## API 🌐

### Upload a file

- **Endpoint:** `POST /upload`
- **Content-Type:** `multipart/form-data`
- **Response:** JSON containing a port code (example: `{"port": 54321}`)

Example using `curl`:

```bash
curl -X POST http://localhost:8080/upload ^
  -F "file=@path/to/your-file.zip"
```

### Download a file

- **Endpoint:** `GET /download/{port}`
- **Response:** A file download (`application/octet-stream`)

Example:

```bash
curl -L -o downloaded.file http://localhost:8080/download/54321
```

Or open in a browser:

- `http://localhost:8080/download/54321`

## Limitations ⚠️

- **No authentication** (intentionally simple for learning).
- **CORS is permissive** (`Access-Control-Allow-Origin: *`).
- The download bridge currently connects to **`localhost:{port}`**, so the current flow is mainly oriented around local testing.
- Files are stored in a temp folder and deleted after being sent.

## Planned improvements 🧭

- Frontend UI
- Deployment (download from another machine, not just localhost)
- Better multipart parsing + validations
- Security hardening (auth, encryption, rate limiting)