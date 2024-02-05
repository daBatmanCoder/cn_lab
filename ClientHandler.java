import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ClientHandler implements Runnable {

    private final Socket socket;
    private final String rootDirectory;
    private final String defaultPage;

    public ClientHandler(Socket socket, String rootDirectory, String defaultPage) {
        this.socket = socket;
        this.rootDirectory = rootDirectory;
        this.defaultPage = defaultPage;
    }


    @Override
    public void run() {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                OutputStream out = socket.getOutputStream()) {

            String requestLine = in.readLine();
            String[] requestParsed = parseHTTPRequest(requestLine);
            if (requestParsed == null || requestParsed.length == 0) {
                Errors.sendErrorResponse(out, 400); // Bad Request
                return;
            }
            System.out.println("Client request at time: " + java.time.LocalTime.now());
            System.out.println(requestLine);

            // System.out.println(requestLine);
            // String[] requestParts = requestLine.split(" ");
            if (requestParsed.length < 3) {
                Errors.sendErrorResponse(out, 400); // Bad Request
                return;
            }

            String method = requestParsed[0];
            String uri = requestParsed[1];
            // System.out.println("requestParts[0] "+requestParts[0]);
            // System.out.println("uri "+ uri);
            // System.out.println("out "+out);

            if (uri.contains("?")) {
                uri = uri.substring(uri.indexOf("?") + 1);
            }
            if (uri.charAt(0)=='/') {
                uri = uri.substring(1);
            }

            switch (method) {
                case "GET":
                    handleGetRequest(uri, out);
                    break;
                case "HEAD":
                    handleHeadRequest(uri, out);
                    break;
                case "POST":
                    handlePostRequest(uri, in, out);
                    break;
                case "TRACE":
                    handleTraceRequest(requestLine, in, out);
                    break;
                default:
                    Errors.sendErrorResponse(out, 501);

            }
        } catch (FileNotFoundException e) {

            try {

                Errors.sendErrorResponse(socket.getOutputStream(), 404); // Not Found
            } catch (IOException ex) {

                ex.printStackTrace();
            }
        } catch (IOException e) {

            e.printStackTrace();

            if (!socket.isClosed()) {
                try {
                    OutputStream out = socket.getOutputStream();
                    Errors.sendErrorResponse(out, 500); // Send a 500 Internal Server Error response
                } catch (IOException ex) {
                    ex.printStackTrace(); // Log this exception as well, in case sending the error response fails
                }
            }
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace(); // Log exception
            }
        }
    }
    private String getContentType(String contentType) {
        switch (contentType) {
            case "image/jpeg":
            case "image/png":
            case "image/gif":
            case "image/bmp":
                return "image";
            case "image/x-icon":
                return "icon";
            case "text/html":
                return "text/html";
            default:
                return "application/octet-stream";
        }
    }

    public static String[] parseHTTPRequest(String requestLine) {
        if (requestLine == null || requestLine.isEmpty()) {
            return null;
        }

        // System.out.println("Client request at time: " + java.time.LocalTime.now());
        // System.out.println(requestLine);

        String[] requestParts = requestLine.split(" ");
        if (requestParts.length < 3) {
            return null;
        }

        String method = requestParts[0];
        String uri = requestParts[1].equals("/") ? "defaultPage" : requestParts[1];
        String httpVersion = requestParts[2];

        if (uri.contains("?")) {
            uri = uri.substring(uri.indexOf("?") + 1);
        }
        if (uri.charAt(0) == '/') {
            uri = uri.substring(1);
        }

        // Return the parsed method, URI, and arguments as an array
        return new String[]{method, uri, httpVersion};
    }

    private void handleGetRequest(String uri, OutputStream out) throws IOException {

        Path filePath = Paths.get(rootDirectory).resolve(uri.substring(0)).normalize();
        System.out.println("file path obtain is: " + filePath+"\n");

        File file = filePath.toFile();

        if (!file.exists()) {
            Errors.sendErrorResponse(out, 404); // Not Found
            return;
        }

        if (!file.getCanonicalPath().startsWith(new File(rootDirectory).getCanonicalPath())) {
            Errors.sendErrorResponse(out, 403); // Forbidden
            return;
        }

        String contentType = Files.probeContentType(filePath);
        contentType = getContentType(contentType);
        ResponseUtil.sendSuccessResponse(file, contentType, out);
        
    }

    private void handleHeadRequest(String uri, OutputStream out) throws IOException {

        Path filePath = Paths.get(rootDirectory).resolve(uri.substring(0)).normalize();
        System.out.println("file path obtain is: " + filePath+"\n");

        File file = filePath.toFile();

        if (!file.exists() || !file.getCanonicalPath().startsWith(new File(rootDirectory).getCanonicalPath())) {
            Errors.sendErrorResponse(out, 404); // Not Found or Forbidden
            return;
        }

        String contentType = Files.probeContentType(filePath);
        contentType = getContentType(contentType);
        ResponseUtil.sendHeadResponse(file, contentType, out);
    }

    private void handlePostRequest(String uri, BufferedReader in, OutputStream out) throws IOException {
        String response = "HTTP/1.1 200 OK\r\nContent-Type: text/plain\r\n\rPOST request processed.";
        out.write(response.getBytes());
        out.flush();
    }

    private void handleTraceRequest(String requestLine, BufferedReader in, OutputStream out) throws IOException {
        StringBuilder response = new StringBuilder("HTTP/1.1 200 OK\r\nContent-Type: message/http\r\n");
        response.append(requestLine).append("\r\n");
        String headerLine;
        while ((headerLine = in.readLine()) != null && !headerLine.isEmpty()) {
            response.append(headerLine).append("\r\n");
        }
        out.write(response.toString().getBytes());
        out.flush();
    }
}
