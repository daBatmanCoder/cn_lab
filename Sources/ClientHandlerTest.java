import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.util.*;

/**
 * This class tests the ClientHandler class.
 * It tests the handleGetRequest, handleHeadRequest, handlePostRequest, and handleTraceRequest methods.
 */

public class ClientHandlerTest {

    public static void main(String[] args) {
        testHandleGetRequest();
        testHandleHeadRequest();
        testHandlePostRequest();
        testHandleTraceRequest();
    }

    public static void testHandleGetRequest() {
        try {
            // Create a temporary file for testing
            Path tempFile = Files.createTempFile("test", ".txt");
            String rootDirectory = tempFile.getParent().toString();
            String defaultPage = "index.html";
            Socket socket = new Socket();
            OutputStream outputStream = new ByteArrayOutputStream();
            ClientHandler clientHandler = new ClientHandler(socket, rootDirectory, defaultPage);

            // Test case 1: Valid GET request
            String uri1 = "file.txt";
            clientHandler.handleGetRequest(uri1, outputStream);
            // Assert the response

            // Test case 2: Invalid URI
            String uri2 = "nonexistent.txt";
            clientHandler.handleGetRequest(uri2, outputStream);
            // Assert the response

            // Clean up the temporary file
            Files.deleteIfExists(tempFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void testHandleHeadRequest() {
        try {
            // Create a temporary file for testing
            Path tempFile = Files.createTempFile("test", ".txt");
            String rootDirectory = tempFile.getParent().toString();
            String defaultPage = "index.html";
            Socket socket = new Socket();
            OutputStream outputStream = new ByteArrayOutputStream();
            ClientHandler clientHandler = new ClientHandler(socket, rootDirectory, defaultPage);

            // Test case 1: Valid HEAD request
            String uri1 = "file.txt";
            clientHandler.handleHeadRequest(uri1, outputStream);
            // Assert the response

            // Test case 2: Invalid URI
            String uri2 = "nonexistent.txt";
            clientHandler.handleHeadRequest(uri2, outputStream);
            // Assert the response

            // Clean up the temporary file
            Files.deleteIfExists(tempFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void testHandlePostRequest() {
        try {
            // Create a temporary file for testing
            Path tempFile = Files.createTempFile("test", ".txt");
            String rootDirectory = tempFile.getParent().toString();
            String defaultPage = "index.html";
            Socket socket = new Socket();
            Map<String, String> params = new HashMap<>();
            OutputStream outputStream = new ByteArrayOutputStream();
            ClientHandler clientHandler = new ClientHandler(socket, rootDirectory, defaultPage);

            // Test case 1: Valid POST request
            String uri1 = "file.txt";
            BufferedReader in = new BufferedReader(new StringReader("POST data"));
            clientHandler.handlePostRequest(uri1, params, in, outputStream);
            // Assert the response

            // Test case 2: Invalid URI
            String uri2 = "nonexistent.txt";
            BufferedReader in2 = new BufferedReader(new StringReader("POST data"));
            clientHandler.handlePostRequest(uri2,params, in2, outputStream);
            // Assert the response

            // Clean up the temporary file
            Files.deleteIfExists(tempFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void testHandleTraceRequest() {
        try {
            Socket socket = new Socket();
            OutputStream outputStream = new ByteArrayOutputStream();
            ClientHandler clientHandler = new ClientHandler(socket, "", "");

            // Test case 1: Valid TRACE request
            String requestLine1 = "TRACE / HTTP/1.1";
            BufferedReader in1 = new BufferedReader(new StringReader(""));
            clientHandler.handleTraceRequest(requestLine1, in1, outputStream);
            // Assert the response

            // Test case 2: Valid TRACE request with headers
            String requestLine2 = "TRACE / HTTP/1.1";
            BufferedReader in2 = new BufferedReader(new StringReader("Header1: Value1\r\nHeader2: Value2\r\n"));
            clientHandler.handleTraceRequest(requestLine2, in2, outputStream);
            // Assert the response
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}