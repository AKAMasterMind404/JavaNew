import java.io.*;
import java.net.*;

class IndexServer {
    public ServerSocket serversocket;
    public int port;

    public IndexServer() throws IOException {
        port = 8010;
        serversocket = new ServerSocket(port);
        System.out.println("Server Start ... ENJOY");
    }

    public IndexServer(int port) throws IOException {
        this.port = port;
        serversocket = new ServerSocket(port);
        System.out.println("Server Start ... ENJOY");
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

public class Server {
    public static void main(String[] args) throws IOException {
        new Server().service();
    }

    public void service() throws IOException {
        method serverfunction = new method();
        ServerSocket serverSocket;
        IndexServer indexserver = new IndexServer();
        serverSocket = indexserver.serversocket;
        try {
            Socket socket = null;
            while (true) {
                socket = serverSocket.accept();
                System.out.println("New connection accepted! ");
                new SThread(socket, serverfunction);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}