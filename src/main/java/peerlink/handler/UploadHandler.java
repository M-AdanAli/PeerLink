package peerlink.handler;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import peerlink.service.FileSharingService;
import peerlink.utils.HttpRequestBodyParser;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Optional;
import java.util.UUID;

public class UploadHandler implements HttpHandler {
    private final File tempDirectory;

    public UploadHandler(){
        this.tempDirectory = new File(System.getProperty("java.io.tmpdir") + File.separator + "peerlink-uploads");
        if (!tempDirectory.exists()){
            tempDirectory.mkdir();
        }
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        Headers responseHeaders = exchange.getResponseHeaders();
        responseHeaders.add("Access-Control-Allow-Origin","*");

        if (exchange.getRequestMethod().equalsIgnoreCase("POST")){

            Headers requestHeaders = exchange.getRequestHeaders();
            String contentType = requestHeaders.getFirst("Content-Type");

            if (contentType != null && contentType.startsWith("multipart/form-data")){
                try {
                    String boundary = contentType.substring(contentType.indexOf("boundary=")+9);
                    byte[] requestData = exchange.getRequestBody().readAllBytes();
                    Optional<HttpRequestBodyParser.ParsedData> parsedData = HttpRequestBodyParser.parse(requestData, boundary);
                    if (parsedData.isEmpty()){
                        String response = "Bad Request: Could not parse file content.";
                        exchange.sendResponseHeaders(400,response.getBytes().length);
                        try (OutputStream outputStream = exchange.getResponseBody()) {
                            outputStream.write(response.getBytes());
                        }
                        return;
                    }
                    String fileName = parsedData.get().getFileName();
                    if (fileName.trim().isEmpty()){
                        fileName = "unnamed-file";
                    }
                    String uniqueFileName = UUID.randomUUID() + "_" + fileName;
                    File filePath = new File(tempDirectory + File.separator + uniqueFileName);
                    try (FileOutputStream fileOutputStream = new FileOutputStream(filePath)) {
                        fileOutputStream.write(parsedData.get().getFileContent());
                    }
                    int port = FileSharingService.INSTANCE.offerFile(filePath);
                    Thread.startVirtualThread(()->{
                       FileSharingService.INSTANCE.startListeningForFile(port);
                    });
                    String jsonResponse = "{\"port\": "+port+"}";
                    responseHeaders.add("Content-Type","application/json");
                    exchange.sendResponseHeaders(200,jsonResponse.getBytes().length);
                    try (OutputStream outputStream = exchange.getResponseBody()) {
                        outputStream.write(jsonResponse.getBytes());
                    }
                }catch (Exception e){
                    System.err.println("Error Processing file upload : "+e.getMessage());
                    String response = "Server Error : "+e.getMessage();
                    exchange.sendResponseHeaders(505, response.getBytes().length);
                    try (OutputStream outputStream = exchange.getResponseBody()) {
                        outputStream.write(response.getBytes());
                    }
                }
            }else {
                String response = "Bad Request: Content-Type must be multipart/form-data";
                exchange.sendResponseHeaders(400,response.getBytes().length);
                try (OutputStream outputStream = exchange.getResponseBody()) {
                    outputStream.write(response.getBytes());
                }
            }
            return;
        }

        String response = "Method not allowed";
        exchange.sendResponseHeaders(405,response.getBytes().length);
        try (OutputStream outputStream = exchange.getResponseBody()) {
            outputStream.write(response.getBytes());
        }
    }
}
