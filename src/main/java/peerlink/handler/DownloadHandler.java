package peerlink.handler;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.*;
import java.net.Socket;

public class DownloadHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        Headers responseHeaders = exchange.getResponseHeaders();
        responseHeaders.add("Access-Control-Allow-Origin","*");

        if (!exchange.getRequestMethod().equalsIgnoreCase("GET")){
            String response = "Method not allowed";
            exchange.sendResponseHeaders(405, response.getBytes().length);
            try(OutputStream outputStream = exchange.getResponseBody()){
                outputStream.write(response.getBytes());
            }
            return;
        }

        String path = exchange.getRequestURI().getPath();
        String portString = path.substring(path.lastIndexOf('/')+1);
        try {
            int port = Integer.parseInt(portString);

            try(Socket socket = new Socket("localhost",port);
                InputStream inputStream = socket.getInputStream()){

                File tempFile = File.createTempFile("download-",".tmp");
                String filename = "downloaded-file";
                try (FileOutputStream fileOutputStream = new FileOutputStream(tempFile)){
                    byte[] buffer = new byte[4096];
                    StringBuilder header = new StringBuilder();
                    int b;
                    while ((b=inputStream.read())!=-1){
                        if ( b =='\n') break;
                        header.append((char) b);
                    }
                    if (header.toString().startsWith("Filename: ")){
                        filename = header.substring("Filename: ".length());
                    }

                    int bytesRead;
                    while ((bytesRead = inputStream.read(buffer)) != -1){
                        fileOutputStream.write(buffer,0,bytesRead);
                    }

                }
                responseHeaders.add("Content-Disposition", "attachment; filename=\"" + filename + "\"");
                responseHeaders.add("Content-Type", "application/octet-stream");
                exchange.sendResponseHeaders(200, tempFile.length());
                try (OutputStream outputStream = exchange.getResponseBody();
                     FileInputStream fileInputStream = new FileInputStream(tempFile)){
                    byte[] buffer = new byte[4096];
                    int bytesRead;
                    while ( ( bytesRead = fileInputStream.read(buffer) ) !=  -1){
                        outputStream.write(buffer,0,bytesRead);
                    }
                }finally {
                    tempFile.delete();
                }
            }
        }catch (Exception e){
            System.err.println("Not able to download the file: "+e.getMessage());
            String response = "Error occurred while downloading the file"+e.getMessage();
            responseHeaders.add("Content-Type","Text/plain");
            exchange.sendResponseHeaders(400,response.getBytes().length);
            try (OutputStream outputStream = exchange.getResponseBody()){
                outputStream.write(response.getBytes());
            }
        }
    }
}
