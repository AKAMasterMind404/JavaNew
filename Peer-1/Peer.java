import java.io.*;
import java.net.*;
import java.util.Timer;
import java.util.TimerTask;

public class Peer {
    /*
     * and register all files in the local file list.
     */
    public static void monitorFile(String path, Handlers peerfunction) {
        File file = new File(path);
        String test[];
        test = file.list();
        if (test.length != 0) {
            for (int i = 0; i < test.length; i++) {
                threadForRegister(test[i], peerfunction);
            }
        }
        // Use WrThread to auto update register file
        new WrThread(path, peerfunction);
    }

    /*
     * Register the file to the index server
     */
    public static void threadForRegister(String fileName, Handlers peerfunction) {
        Socket socket = null;
        StringBuffer sb = new StringBuffer("register ");
        try {
            PeerSocket peersocket = new PeerSocket();
            socket = peersocket.socket;
            PrintWriter pw = peersocket.getWriter(socket);
            // register to the local file
            peerfunction.register(PeerInformation.Local.ID, fileName);
            // register to the server end
            // Send register message
            sb.append(PeerInformation.Local.ID);
            sb.append(" " + fileName);

            pw.println(sb.toString());
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (socket != null) {
                    socket.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void startFunction(Handlers peerfunction) throws IOException {
        boolean exit = false;
        // Store file name
        String fileName = null;
        BufferedReader localReader = new BufferedReader(new InputStreamReader(System.in));

        // Usage Interface
        while (!exit) {
            System.out.println("\n1 Register all file\n2 Search a file\n3 Exit");
            Integer i1 = Integer.parseInt(localReader.readLine());
            if (i1 == 1) {
                monitorFile(PeerInformation.Local.path, peerfunction);
                break;
            } else if (i1 == 2) {
                boolean find;
                System.out.println("Enter the file name:");
                fileName = localReader.readLine();
                // Search file through index server
                find = searchThread(fileName, peerfunction);
                if (find) {
                    System.out.println("\n1 Download the file\n2 Cancel and back");
                    Integer inputInteger = Integer.parseInt(localReader.readLine());
                    if (inputInteger == 1) {
                        download(fileName, peerfunction);
                    }
                }
            } else if (i1 == 3) {
                exit = true;
                System.exit(0);
                break;
            }
        }
    }

    public static void unregisterThread(String fileName, Handlers peerfunction) {
        Socket socket = null;
        System.out.println("in");
        StringBuffer sb = new StringBuffer("unregister ");
        try {
            PeerSocket peersocket = new PeerSocket();
            socket = peersocket.socket;
            PrintWriter pw = peersocket.getWriter(socket);
            // Unregister to the local file

            peerfunction.unRegister(PeerInformation.Local.ID, fileName);
            // Unregister to the server end
            // Send unregister message
            sb.append(PeerInformation.Local.ID);
            sb.append(" " + fileName);

            pw.println(sb.toString());

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (socket != null) {
                    socket.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public boolean searchThread(String fileName, Handlers peerfunction) {
        PeerInformation peerinfo = new PeerInformation();
        peerinfo.init();
        Socket socket = null;
        StringBuffer sb = new StringBuffer("search ");
        boolean find = false;
        // Store file ID
        String findID = null;

        try {
            PeerSocket peersocket = new PeerSocket();
            socket = peersocket.socket;
            BufferedReader br = peersocket.getReader(socket);
            PrintWriter pw = peersocket.getWriter(socket);

            // Send search message
            sb.append(PeerInformation.Local.ID);
            sb.append(" " + fileName);
            pw.println(sb.toString());

            // Get peer list
            while (!("bye".equals(findID = br.readLine()))) {
                PeerInformation.Dest.destList.add(findID);
            }

            // If find file in some peers, output their address
            if ((find = peerfunction.search(fileName)) == true) {
                for (int i = 0; i < PeerInformation.Dest.destPath.size(); i++) {
                    System.out.println(fileName + " is found on " + PeerInformation.Dest.destPath.get(i));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (socket != null) {
                    socket.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return find;
    }

    /*
     * Used to download file from other clients
     */
    public void download(String fileName, Handlers peerfunction) {
        String IP = null;
        int port = 0;
        int serverPort = 0;

        String address = PeerInformation.Dest.destPath.get(0);

        String[] info = address.split("\\:");
        IP = info[0];
        port = Integer.parseInt(info[1]);
        /*
         * Set up serverPort
         */
        serverPort = PeerInformation.Local.downloadPort;
        // Set up a server socket to receive file
        new DThread(serverPort, fileName);

        // Set up a socket connection to the peer destination
        Socket socket = null;

        StringBuffer sb = new StringBuffer("download ");
        try {
            PeerSocket peersocket = new PeerSocket(IP, port);
            socket = peersocket.socket;
            PrintWriter pw = peersocket.getWriter(socket);

            // Send download message
            sb.append(fileName);
            sb.append(" " + serverPort);
            sb.append(" " + PeerInformation.Local.IP);
            pw.println(sb.toString());
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (socket != null) {
                    socket.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String args[]) throws IOException {
        Handlers peerfunction = new Handlers();
        peerfunction.init();
        // Monitor_file(PeerInformation.Local.path,peerfunction);
        ServerSocket server = null;
        try {
            server = new ServerSocket(PeerInformation.Local.serverPort);
            System.out.println("\n Peer  started!");
            // System.out.println(server);
            new PThread(server);
        } catch (IOException e) {
            e.printStackTrace();
        }

        Peer peer = new Peer();
        peer.startFunction(peerfunction);
    }
}

/*
 * Used to receive file from file client
 * Step 1. Set up a server socket
 * Step 2. Waiting for input data
 */
class DThread extends Thread {
    int port;
    String fileName;

    public DThread(int port, String fileName) {
        this.port = port;
        this.fileName = fileName;
        start();
    }

    public void run() {
        try {
            ServerSocket server = new ServerSocket(port);
            // while(true){
            Socket socket = server.accept();
            receiveFile(socket, fileName);
            socket.close();
            server.close();
            // }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void receiveFile(Socket socket, String fileName) throws IOException {
        byte[] inputByte = null;
        int length = 0;
        DataInputStream dis = null;
        FileOutputStream fos = null;
        String filePath = "./Look/" + fileName;
        try {
            try {
                dis = new DataInputStream(socket.getInputStream());
                File f = new File("./Look");
                if (!f.exists()) {
                    f.mkdir();
                }

                fos = new FileOutputStream(new File(filePath));
                inputByte = new byte[1024];
                System.out.println("\nStart receiving...");
                System.out.println("display file " + fileName);
                while ((length = dis.read(inputByte, 0, inputByte.length)) > 0) {
                    fos.write(inputByte, 0, length);
                    fos.flush();
                }
                System.out.println("Finish receive:" + filePath);
            } finally {
                if (fos != null)
                    fos.close();
                if (dis != null)
                    dis.close();
                if (socket != null)
                    socket.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

/*
 * Watch file
 * Listening to the local file folder
 * When there is a change, register or
 * unregister file in the local list.
 */
class WrThread extends Thread {
    String path = null;
    Handlers peerfunction = null;

    public WrThread(String path, Handlers peerfunction) {
        this.path = path;
        this.peerfunction = peerfunction;
        start();
    }

    public void run() {
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {

            @Override
            public void run() {
                if (PeerInformation.Local.fileList.size() != 0) {
                    for (int i = 0; i < PeerInformation.Local.fileList.size(); i++) {
                        File file = new File(path + File.separator +
                                PeerInformation.Local.fileList.get(i));
                        if (!file.exists()) {
                            System.out.println(PeerInformation.Local.fileList.get(i) + " was removed!");
                            Peer.unregisterThread(PeerInformation.Local.fileList.get(i), peerfunction);

                        }
                    }
                }
            }

        }, 1000, 100);

    }
}

class PeerSocket {
    public Socket socket = null;
    private Handlers pf = new Handlers();

    public PeerSocket() throws IOException {
        pf.init();
        socket = new Socket(PeerInformation.Local.IP, PeerInformation.Local.clientPort);
    }

    public PeerSocket(String IP, int port) throws IOException {
        pf.init();
        PeerInformation.Local.clientPort = port;
        socket = new Socket(IP, PeerInformation.Local.clientPort);
    }

    public PrintWriter getWriter(Socket socket) throws IOException {
        OutputStream socketOut = socket.getOutputStream();
        return new PrintWriter(socketOut, true);
    }

    public BufferedReader getReader(Socket socket) throws IOException {
        InputStream socketIn = socket.getInputStream();
        return new BufferedReader(new InputStreamReader(socketIn));
    }
}

class peerServer {
    public ServerSocket serversocket;
    public int port;

    public peerServer() throws IOException {
        port = PeerInformation.Local.serverPort;
        serversocket = new ServerSocket(port);
    }

    public peerServer(int port) throws IOException {
        this.port = port;
        serversocket = new ServerSocket(port);
    }

    public PrintWriter getWriter(Socket socket) throws IOException {
        OutputStream socketOut = socket.getOutputStream();
        return new PrintWriter(socketOut, true);

    }

    public BufferedReader getReader(Socket socket) throws IOException {
        InputStream socketIn = socket.getInputStream();
        return new BufferedReader(new InputStreamReader(socketIn));

    }
}