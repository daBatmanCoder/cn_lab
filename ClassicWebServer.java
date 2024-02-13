
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


/**
 * The ClassicWebServer class represents a simple web server that handles incoming client requests.
 * It listens on a specified port, accepts client connections, and delegates the handling of each
 * client connection to a separate thread from a thread pool. The server is configured with a root
 * directory, default page, maximum number of threads, and port number.
 */

public class ClassicWebServer {

    private int port;
    private String rootDirectory;
    private String defaultPage;
    private int maxThreads;

    // Constructor for the server
    public ClassicWebServer(String defaultPage, int maxThreads, int port, String rootDirectory ) {
        this.port = port;
        this.rootDirectory = rootDirectory;
        this.defaultPage = defaultPage;
        this.maxThreads = maxThreads;
    }

    // Start the server and handle every request coming in
    public void start() {

        // Create a thread pool with a fixed number of threads (for our server - 10)
        ExecutorService threadPool = Executors.newFixedThreadPool(maxThreads);

        try {
            try (ServerSocket serverSocket = new ServerSocket(port)) {
                System.out.println("Web server is listening on port " + port+ "...\n");
 
                while (true) {
                    try {
                        Socket socket = serverSocket.accept();
                        // Handle the client connection using a separate thread from the thread pool
                        threadPool.execute(new ClientHandler(socket, rootDirectory, defaultPage));
                    } catch (IOException e) {
                        System.out.println("Server exception: " + e.getMessage());
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("Could not start server on port " + port + ": " + e.getMessage());
            System.exit(1);
        }
    }

    public static void main(String[] args) {
        try{
            ConfigLoader config = new ConfigLoader("config_file.ini");
            ClassicWebServer server = new ClassicWebServer( config.getDefaultPage(), config.getMaxThreads(), config.getPort(), config.getRoot());
            server.start();
        }
        catch(Exception e){
            System.out.println("Error: " + e.getMessage());
        }
    }
}

