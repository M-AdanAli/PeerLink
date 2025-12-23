package peerlink.app;

import peerlink.controller.FileController;

import java.io.IOException;

public class App {
    public static void main(String[] args) {
        try {
            FileController fileController = new FileController(8080);
            fileController.start();
            System.out.println("PeerLink started on port 8080");

            Runtime.getRuntime().addShutdownHook(new Thread(
                    () -> {
                        fileController.stop();
                        System.out.println("PeerLink stopped.");
                    }
            ));

            System.out.println("Press \"Enter\" to stop the server");
            System.in.read();
            System.exit(0);
        }catch (IOException e){
            System.err.println("Failed to start Server at port 8080");
            e.printStackTrace();
        }

    }
}
