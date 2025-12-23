package peerlink.utils;

import java.util.concurrent.ThreadLocalRandom;

public class Utility {
    public static int generateCode(){
        final int DYNAMIC_STARTING_PORT = 49152;
        final int DYNAMIC_ENDING_PORT = 65535;
        return ThreadLocalRandom.current().nextInt(DYNAMIC_STARTING_PORT,DYNAMIC_ENDING_PORT+1);    
    }
}
