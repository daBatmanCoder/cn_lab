import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class ConfigLoader {
    private int port;
    private String root;
    private String defaultPage;
    private int maxThreads;

    // The constructor loads the configuration file and sets the properties
    public ConfigLoader(String filePath) {
        Properties config = new Properties();
        try (FileInputStream fis = new FileInputStream(filePath)) {
            config.load(fis);
            // Defaulting if no specified
            this.defaultPage = config.getProperty("defaultPage", "index.html"); 
            this.maxThreads = Integer.parseInt(config.getProperty("maxThreads", "10"));
            this.port = Integer.parseInt(config.getProperty("port", "8080")); 
            this.root = config.getProperty("root", "~/www/lab/html/").replace("~", System.getProperty("user.home")); 

        } catch (IOException e) {
            System.err.println("Failed to load configuration: " + e.getMessage());
            System.exit(1);
        }
    }

    // Getters for configuration properties
    public String getDefaultPage() {
        return this.defaultPage;
    }

    public int getMaxThreads() {
        return this.maxThreads;
    }

    public int getPort() {
        return this.port;
    }

    public String getRoot() {
        return this.root;
    }
  
    // Test the class
    public static void main(String[] args) {
        ConfigLoader config = new ConfigLoader("config_file.ini");
        System.out.println("Default Page: " + config.getDefaultPage());
        System.out.println("Max Threads: " + config.getMaxThreads());
        System.out.println("Port: " + config.getPort());
        System.out.println("Root: " + config.getRoot());
    }
}
