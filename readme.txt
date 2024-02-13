Computer networks- February 2024 LAB :


ConfigLoader Class:       The ConfigLoader class is responsible for loading the server's configuration settings from an external file (config.ini). 
                          It reads various configuration properties, including the server's listening port, root directory for served files, 
                          default page to be served when no specific file is requested, and the maximum number of threads in the thread pool. 
                          This class enables the server's behavior to be easily customized without changing the code.

ClassicWebServer Class:   The ClassicWebServer class represents a simple web server that handles incoming client requests.
                          It listens on a specified port, accepts client connections, and delegates the handling of each
                          client connection to a separate thread from a thread pool. The server is configured with a root
                          directory, default page, maximum number of threads, and port number.

ClientHandler Class:      The ClientHandler class in your Java code is a server-side component that handles client requests.
                          It implements the Runnable interface, allowing it to be used in a multithreaded environment.
                          The class supports handling of GET, HEAD, POST, and TRACE HTTP methods.
                          It reads client requests, processes them based on the HTTP method, and sends back appropriate responses.
                          The class also includes error handling for file not found and input/output exceptions.

                          A notable feature of the ClientHandler class is the sanitizeUri method, 
                          which processes the request URI to remove potentially malicious elements (like /../) that could lead to directory traversal attacks.
                          This ensures that all file accesses are safely contained within the server's root directory.


ResponseUtil Class:       The ResponseUtil class provides methods for sending HTTP responses to the client.

Errors Class:             The Errors class provides a method for sending an HTTP error response to the client.

ClientHandlerTest:         This ClientHandlerTest tests the ClientHandler class.
                          It tests the handleGetRequest, handleHeadRequest, handlePostRequest, and handleTraceRequest methods.



Design Philosophy,

The Server class listens for incoming connections on a specific port and spawns a new thread for each client connection with a maximum number of connection.
This allows the server to handle multiple client requests concurrently without blocking other connections. 
The ClientHandler class takes care of processing each client request independently, ensuring efficient and responsive communication between the server and clients.
And the responses- when a good answer is marked then the ResponseUtil is triggered and when we have an error then the Error class is then called, 
keeping it simple, organize and easy to access and modify if necessary.

The 3 principles we've decided to take in mind when implementing were: simplicity, modularity, and extensibility. 
(in Extend)

Simplicity:    The server handles basic web server functionality without unnecessary complexity, making it easy to understand and maintain.
Modularity:    Each class has a clear, distinct responsibility, allowing individual components (such as configuration loading or request handling) to be modified or extended independently.
Extensibility: The server's use of a configuration file and the separation of request handling into its own class allows for easy customization and extension to support additional functionality or HTTP methods in the future.
