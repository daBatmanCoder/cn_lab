import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.file.Files;

public class ResponseUtil {
    
     // Request line:
    //      GET /index.html HTTP/1.1

     // Headers: 
            // Host: localhost:8080
            // User-Agent: Chrome/90.0.4430.212
            // Content-Type: text/html
            
    // Body: 
    //      <html><body><h1>404 Not Found</h1></body></html>

    public static void sendSuccessResponse(File file, String contentType, OutputStream out) throws IOException {
        byte[] content = Files.readAllBytes(file.toPath());

        PrintWriter writer = new PrintWriter(out, true);
        // Response example: 
        // HTTP/1.1 200 OK[CRLF] 
        // content-type: text/html[CRLF] 
        // content-length: <page/file size>[CRLF] 
        // [CRLF] 
        // <content of page/file> 
        writer.println("HTTP/1.1 200 OK");
        writer.println("Content-Type: " + contentType);
        writer.println("Content-Length: " + content.length);
        writer.println(); // Blank line between headers and content
        writer.flush();
        System.out.println("Server Response at time: " + java.time.LocalTime.now());
        System.out.println("HTTP/1.1 200 OK");
        System.out.println("Content-Type: " + contentType);
        System.out.println("Content-Length: " + content.length);
        System.out.println();
        out.write(content);
        out.flush();
    }

    // No content sent for HEAD request 
    public static void sendHEADResponse(File file, String contentType, OutputStream out) throws IOException {
        PrintWriter writer = new PrintWriter(out, true);
        writer.println("HTTP/1.1 200 OK");
        writer.println("Content-Type: " + contentType);
        writer.println("Content-Length: " + file.length());
        writer.println(); // No body sent for HEAD request
        writer.flush();

        // print the header to the console
        System.out.println("Server Response at time: " + java.time.LocalTime.now());
        System.out.println("HTTP/1.1 200 OK");
        System.out.println("Content-Type: " + contentType);
        System.out.println("Content-Length: " + file.length());
        System.out.println();

    }

}
