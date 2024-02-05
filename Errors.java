import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;

public class Errors {

    public static void sendErrorResponse(OutputStream out, int statusCode) throws IOException {
        String statusMessage = getStatusMessage(statusCode);

        String response = "HTTP/1.1 " + statusCode + " " + statusMessage + "\r\n" +
                          "Content-Type: text/html\r\n" +
                          "\r\n" +
                          "<html><body><h1>" + statusCode + " " + statusMessage + "</h1></body></html>";
 
        PrintWriter writer = new PrintWriter(out, true);
        writer.println(response);
        writer.flush();
    }

    private static String getStatusMessage(int statusCode) {
        switch (statusCode) {
            case 200: return "OK";
            case 404: return "Not Found";
            case 501: return "Not Implemented";
            case 400: return "Bad Request";
            case 500: return "Internal Server Error";
            default: return "Unknown Status Code";
        }
    }
}
