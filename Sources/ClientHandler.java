import java.io.*;
import java.net.Socket;
import java.net.URLDecoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.nio.charset.StandardCharsets;


/*
The ClientHandler class in your Java code is a server-side component that handles client requests.
It implements the Runnable interface, allowing it to be used in a multithreaded environment.
The class supports handling of GET, HEAD, POST, and TRACE HTTP methods.
It reads client requests, processes them based on the HTTP method, and sends back appropriate responses.
The class also includes error handling for file not found and input/output exceptions.
 */

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
            if (requestLine == null) {
                return;
            }
            System.out.println(requestLine);
            System.out.println("\nClient request at time: " + java.time.LocalTime.now());

            
            // // add 10 seconds delay - TESTING
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
            Map<String, String> parameters;
            String method = requestParsed[0];
            String uri = requestParsed[1];
            String sanitize_uri = sanitizeUri(uri);
            String httpVersion = requestParsed[2];
            System.out.println("method: " + method + " uri: " + sanitize_uri + " httpVersion: " + httpVersion);

            if (uri.contains("?")) {
                parameters = getParamMap(sanitize_uri);
                System.out.println("parameters: " + parameters);
                uri = uri.substring(0, uri.indexOf("?"));
            } else {
                parameters = new HashMap<>();
            }

            switch (method) {
                case "GET":
                    handleGetRequest(uri, out);
                    break;
                case "HEAD":
                    handleHeadRequest(uri, out);
                    break;
                case "POST":
                    handlePostRequest(uri, parameters, in, out);
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

    public String sanitizeUri(String uri) { // Pattern matching
        // Pattern to match extraneous characters before the intended path
        String pattern = "/+\\.*/+";
    
        // Replace matched patterns with a single "/"
        String sanitizedUri = uri.replaceAll(pattern, "/");
    
        return sanitizedUri;
    }

    private String getContentType(String contentType) { // Content type
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

    // Sanitize the path string to prevent directory traversal attacks
    private Path getSanitizedPathString(String uri, OutputStream out) throws IOException {
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


    public void handleGetRequest(String uri, OutputStream out) throws IOException {
        Path filePath = getSanitizedPathString(uri, out);
        System.out.println("File Path: " + filePath);
        if (filePath == null) { return; }
        File file = filePath.toFile();
        String contentType = Files.probeContentType(filePath);
        contentType = getContentType(contentType);
        ResponseUtil.sendSuccessResponse(file, contentType, out);

    }

    public void handleHeadRequest(String uri, OutputStream out) throws IOException {
        System.out.println("Handling HEAD request for URI: " + uri);
        String path = uri.split("\\?")[0];  // Use regex "\\?" to split since "?" is a special character in regex

        Path filePath = getSanitizedPathString(path, out);
        if (filePath == null) { return; }

        File file = filePath.toFile();
        if (!file.exists() || !file.isFile()) {
            // Handle the case where the file does not exist or is not a file (e.g., send a 404 response)
            Errors.sendErrorResponse(out, 404);
            return;
        }
        String contentType = Files.probeContentType(filePath);
        contentType = getContentType(contentType);
        ResponseUtil.sendHEADResponse(file, contentType, out);
    }

    public void handlePostRequest(String uri,Map<String, String> params_in_head ,BufferedReader in, OutputStream out) throws IOException {
        
        if (uri.equals("params_info.html")){

            System.out.println("Handling POST request for URI: " + uri);
            int contentLength = -1;

            // Read and process all headers
            String headerLine;
            while (!(headerLine = in.readLine()).isEmpty()) {
                System.out.println(headerLine); 
                if (headerLine.startsWith("Content-Length:")) {
                    contentLength = Integer.parseInt(headerLine.substring("Content-Length:".length()).trim());
                }
            }

            if (contentLength == -1) {
                Errors.sendErrorResponse(out, 411); // Erro for Length
                return;
            }

            // Read the body of the request based on Content-Length
            char[] bodyChars = new char[contentLength];
            int bytesRead = in.read(bodyChars, 0, contentLength);
            if (bytesRead != contentLength) {
                Errors.sendErrorResponse(out, 400); // Bad Request
                return;
            }
            String body = new String(bodyChars);
            System.out.println("Body: " + body);
            
            Map<String, String> params = parseFormData(body);
            params.putAll(params_in_head);
            
            // Generate dynamic HTML based on params
            String responseHtml = generateDynamicHtml(params);
        
            // Send response
            PrintWriter writer = new PrintWriter(out, true);
            writer.println("HTTP/1.1 200 OK");
            writer.println("Content-Type: text/html");
            writer.println("Content-Length: " + responseHtml.getBytes().length);
            writer.println();
            writer.println(responseHtml);
            writer.flush();
        } else {
            Errors.sendErrorResponse(out, 404); // Not Found
       }
}

    private Map<String, String> parseFormData(String formData) {
        Map<String, String> params = new HashMap<>();
        String[] pairs = formData.split("&");
        for (String pair : pairs) {
            String[] keyValue = pair.split("=");
            if (keyValue.length == 2) {
                String key = URLDecoder.decode(keyValue[0], StandardCharsets.UTF_8);
                String value = URLDecoder.decode(keyValue[1], StandardCharsets.UTF_8);
                params.put(key, value);
            }
        }
        return params;
    }

    private String generateDynamicHtml(Map<String, String> params) {
        StringBuilder htmlBuilder = new StringBuilder("<!DOCTYPE html><html><body>");
        htmlBuilder.append("<h2>Form Submission Details</h2>");
        for (Map.Entry<String, String> entry : params.entrySet()) {
            htmlBuilder.append("<p>").append(entry.getKey()).append(": ").append(entry.getValue()).append("</p>");
        }
        htmlBuilder.append("</body></html>");
        return htmlBuilder.toString();
    }


    public void handleTraceRequest(String requestLine, BufferedReader in, OutputStream out) throws IOException {
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
