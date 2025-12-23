package peerlink.service;

import peerlink.utils.Utility;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import peerlink.handler.FileSendingHandler;

public enum FileSharingService {
    INSTANCE;

    private Map<Integer, File> filesToShare;

    FileSharingService(){
        filesToShare = new ConcurrentHashMap<>();
    }

    public int offerFile(File file){
        int port = getUniqueCode();
        filesToShare.put(port,file);
        return port;
    }

    public void startListeningForFile(int port){
        File file = filesToShare.get(port);
        if( file == null || !file.isFile() ) {
            System.out.println("No File associated with the port : " + port);
            return;
        }

        try (ServerSocket socket = new ServerSocket(port)) {
            System.out.println("Serving file, "+file.getName()+", on the port: "+port);
            Socket clientSocket = socket.accept();
            System.out.println("Client Connected: "+ clientSocket.getInetAddress());
            new Thread(new FileSendingHandler(clientSocket, file)).start();
        }catch (IOException e){
            System.err.println(e);
        }
    }

    private int getUniqueCode(){
        while (true){
            int generatedPort = Utility.generateCode();
            if (! filesToShare.containsKey( generatedPort ) && isPortAvailable(generatedPort)) {
                return generatedPort;
            }
        }
    }

    private boolean isPortAvailable(int port) {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            serverSocket.setReuseAddress(true);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

}
