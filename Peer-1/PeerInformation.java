import java.util.ArrayList;

/**
 * Class to manage peer information, both local and destination.
 */
public class PeerInformation {
    // Store local information
    public static class Local {
        public static int serverPort = 9010;
        public static int clientPort = 8010;
        public static int downloadPort = 10010;
        public static String IP = "";
        public static String name = "";
        public static String ID = "";
        public static String path = "./Look";
        public static ArrayList<String> fileList = new ArrayList<>();
    }

    // Store destination information
    public static class Dest {
        public static ArrayList<String> destList = new ArrayList<>();
        public static String destination = "127.0.0.1:8010";
        public static ArrayList<String> destPath = new ArrayList<>();
        public static String path = "./Look";
    }

    /**
     * Initialize destination properties to their default values.
     */
    public void init() {
        PeerInformation.Dest.destination = "";
        PeerInformation.Dest.destList = new ArrayList<>();
        PeerInformation.Dest.destPath = new ArrayList<>();
    }
}
