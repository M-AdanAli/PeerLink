package peerlink.handler;

import java.io.*;
import java.net.Socket;

public class FileSendingHandler implements Runnable{
    private final Socket clientSocket;
    private final File file;

    public FileSendingHandler(Socket clientSocket, File file){
        if (clientSocket == null || file == null){
            throw new IllegalArgumentException("Please pass valid Arguments.");
        }
        this.clientSocket = clientSocket;
        this.file = file;
    }

    @Override
    public void run() {
        try (FileInputStream inputStream = new FileInputStream(file);
                OutputStream outputStream = clientSocket.getOutputStream()) {
            String header = "Filename: "+file.getName()+"\n";
            outputStream.write(header.getBytes());
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ( (bytesRead = inputStream.read(buffer)) != -1 ){
                outputStream.write(buffer,0,bytesRead);
            }

            System.out.println("File, "+file.getName()+", sent to "+ clientSocket.getInetAddress() );
        } catch (IOException e) {
            System.err.println(e);
        }finally {
            try {
                clientSocket.close();
                file.delete();
            }catch (IOException e){
                System.err.println(e);
            }
        }
    }
}
