package peerlink.controller;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import peerlink.handler.DownloadHandler;
import peerlink.handler.UploadHandler;

import java.io.*;
import java.net.InetSocketAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FileController {
    private final HttpServer httpServer;
    private final ExecutorService executorService;

    public FileController(int port) throws IOException {
        this.httpServer = HttpServer.create(new InetSocketAddress(port),0);
        this.executorService = Executors.newVirtualThreadPerTaskExecutor();
        httpServer.createContext("/upload",new UploadHandler());
        httpServer.createContext("/download",new DownloadHandler());
        httpServer.createContext("/",new CorsHandler());
        httpServer.setExecutor(executorService);
    }

    public void start(){
        httpServer.start();
        System.out.println("API server started on the port : "+httpServer.getAddress().getPort());
    }

    public void stop(){
        httpServer.stop(0);
        executorService.shutdown();
        System.out.println("API Server stopped.");
    }

    private class CorsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            Headers responseHeaders = exchange.getResponseHeaders();
            responseHeaders.add("Access-Control-Allow-Origin","*");
            responseHeaders.add("Access-Control-Allow-Methods","GET, POST, OPTIONS");
            responseHeaders.add("Access-Control-Allow-Headers","Content-Type, Authorization");

            if (exchange.getRequestMethod().equalsIgnoreCase("OPTIONS")){
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            String response = "NOT FOUND";
            exchange.sendResponseHeaders(404,response.getBytes().length);
            try(OutputStream outputStream = exchange.getResponseBody()) {
                outputStream.write(response.getBytes());
            }
        }
    }
}
