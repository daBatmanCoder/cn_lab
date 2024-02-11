import java.io.*;
import java.net.Socket;
import java.net.URLDecoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

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
            System.out.println("\nClient request at time: " + java.time.LocalTime.now());
            System.out.println(requestLine);
            
            
            // // add 10 seconds delay
            // try {
            //     Thread.sleep(5000);
            // } catch (InterruptedException e) {
            //     e.printStackTrace();
            // }


            String[] requestParsed = parseHTTPRequest(requestLine);
            if (requestParsed == null) {
                Errors.sendErrorResponse(out, 400); // Bad Request
                return;
            }

            String method = requestParsed[0];
            String uri = requestParsed[1];
            String httpVersion = requestParsed[2];
            System.out.println("method: " + method + " uri: " + uri + " httpVersion: " + httpVersion);

            if (uri.contains("?")) {
                Map<String, String> parameters = getParamMap(uri);
                System.out.println("parameters: " + parameters);
                uri = uri.substring(0, uri.indexOf("?"));
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

    public String[] parseHTTPRequest(String requestLine) {
        if (requestLine == null || requestLine.isEmpty()) { return null; }

        String[] requestParts = requestLine.split(" ");
        if (requestParts.length < 3) { return null; }

        String method = requestParts[0];
        String uri = requestParts[1];
        String httpVersion = requestParts[2];
        // if (!httpVersion.equals("HTTP/1.1")) { return null; }

        if (uri.charAt(0) == '/') {
            uri = uri.substring(1);
        }

        if (uri.isEmpty()) {
            uri = defaultPage;
        }
        if (uri.contains("?") && !uri.contains(defaultPage)) {
            uri = defaultPage + uri;
        }

        // if (requestLine.contains("chunked: yes")) {
        //     System.out.println("Chunked transfer encoding is not supported.");
        //     return null;
        // }


        // Return the parsed method, URI (with params if exists), and arguments as an array
        return new String[] { method, uri, httpVersion };
    }


    private Map<String, String> getParamMap(String uri) {
        Map<String, String> parameters = new HashMap<>();
        if (uri.contains("?")) {
            String uri_params = uri.substring(uri.indexOf("?") + 1);
            String[] pairs = uri_params.split("&");
            for (String pair : pairs) {
                String[] keyValue = pair.split("=");
                if (keyValue.length == 2) {
                    try {
                        String key = URLDecoder.decode(keyValue[0], "UTF-8");
                        String value = URLDecoder.decode(keyValue[1], "UTF-8");
                        parameters.put(key, value);
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return parameters;
    }

    private Path getSanitizedPathString(String uri, OutputStream out) throws IOException {
        // Path filePath = Paths.get(rootDirectory).resolve(uri.substring(0)).normalize();
        Path filePath = Paths.get(rootDirectory).resolve(uri.substring(0));
        File file = filePath.toFile();
        if (!file.getCanonicalPath().startsWith(new File(rootDirectory).getCanonicalPath())) {
            Errors.sendErrorResponse(out, 403); // Forbidden
            return null;
        }
        if (!file.exists()) {
            Errors.sendErrorResponse(out, 404); // Not Found
            return null;
        }
        return filePath;
    }


    private void handleGetRequest(String uri, OutputStream out) throws IOException {
        Path filePath = getSanitizedPathString(uri, out);
        if (filePath == null) { return; }
        File file = filePath.toFile();
        String contentType = Files.probeContentType(filePath);
        contentType = getContentType(contentType);
        ResponseUtil.sendSuccessResponse(file, contentType, out);

    }

    private void handleHeadRequest(String uri, OutputStream out) throws IOException {
        Path filePath = getSanitizedPathString(uri, out);
        if (filePath == null) { return; }
        File file = filePath.toFile();
        String contentType = Files.probeContentType(filePath);
        contentType = getContentType(contentType);
        ResponseUtil.sendHEADResponse(file, contentType, out);
    }

    private void handlePostRequest(String uri, BufferedReader in, OutputStream out) throws IOException {

        Path filePath = getSanitizedPathString(uri, out);
        if (filePath == null) { return; }
        File file = filePath.toFile();
        String contentType = Files.probeContentType(filePath);
        contentType = getContentType(contentType);
        // String response = "HTTP/1.1 200 OK\r\nContent-Type: text/plain\r\n\rPOST request processed.\r\n";
        // out.write(response.getBytes());
        ResponseUtil.sendSuccessResponse(file, contentType, out);
    }

    // private void handleTraceRequest(String requestLine, BufferedReader in, OutputStream out) throws IOException {
    //     StringBuilder response = new StringBuilder("HTTP/1.1 200 OK\r\nContent-Type: message/http\r\n");
    //     response.append(requestLine).append("\r\n");
    //     String headerLine;
    //     while ((headerLine = in.readLine()) != null && !headerLine.isEmpty()) {
    //         response.append(headerLine).append("\r\n");
    //     }
    //     out.write(response.toString().getBytes());
    //     out.flush();
    // }

    private void handleTraceRequest(String requestLine, BufferedReader in, OutputStream out) throws IOException {
        PrintWriter writer = new PrintWriter(out, true);
        writer.println("HTTP/1.1 200 OK");
        writer.println("Content-Type: message/http");
        writer.println();
        writer.println(requestLine);
        String headerLine;
        while ((headerLine = in.readLine()) != null && !headerLine.isEmpty()) {
            writer.println(headerLine);
        }
        writer.flush();
    }
}
