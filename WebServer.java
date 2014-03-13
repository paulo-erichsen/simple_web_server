/******************************************************************************
* Program:
*    Lab Webserver, Computer Communication and Networking
* Author:
*    Paulo Fagundes
* Date:
*    1/22/2014
* Summary:
*    This is a simple java implementation of a Web Server.
******************************************************************************/
import java.io.*;
import java.net.*;
import java.util.*;

/**
 * This class starts and runs the WebServer. Default port: 6789.
 */
public final class WebServer
{
   /**
    *
    *
    */
   public static void main(String argv[])
      throws Exception
   {
      // sets the port number to be used (default: 6789; optional: argv[0])
      int port = argv.length > 0 ? Integer.parseInt(argv[0]) : 6789;

      // establish the listen socket
      ServerSocket welcomeSocket = new ServerSocket(port);

      // process HTTP service requests in an infinite loop
      while(true)
      {
         // listen for a TCP connection request
         Socket socket = welcomeSocket.accept();

         // construct an object to process the HTTP request message
         HttpRequest request = new HttpRequest(socket);

         // create a new thread to process the request
         Thread thread = new Thread(request);

         // start the thread
         thread.start();
      }
   }
}

/**
 * This class handles HTTP Requests. It is assumed that all the requests are
 * GET requests. It checks if the requested object exists and returns it to the
 * client. If the object doesn't exist, it returns a web page w/ error 404.
 */
final class HttpRequest
   implements Runnable
{
   final static String CRLF = "\r\n";
   Socket socket;

   /**
    * HttpRequest public constructor
    */
   public HttpRequest(Socket socket)
      throws Exception
   {
      this.socket = socket;
   }

   /**
    * overriding run() such that Thread.start() will work as intended
    */
   // Implement the run() method of the Runnable interface.
   public void run()
   {
      try
      {
         processRequest();
      }
      catch (Exception e)
      {
         System.out.println(e);
      }

   }

   /**
    * this method processes the GET request, builds the HTTP response and sends
    * it back to the client through the socket
    */
   private void processRequest()
      throws Exception
   {
      // Get a reference to the socket's input and output streams.
      InputStream is = socket.getInputStream();
      DataOutputStream os = new DataOutputStream(socket.getOutputStream());

      // Set up input stream filters.
      BufferedReader br = new BufferedReader(new InputStreamReader(is));

      // get the request line of the HTTP request message
      String requestLine = br.readLine();

      // Display the request line
      System.out.println();
      System.out.println(requestLine);

      // Get and display the header lines.
      String headerLine = null;
      while ((headerLine = br.readLine()).length() != 0)
      {
         System.out.println(headerLine);
      }

      //  Extract the filename from the request line.
      StringTokenizer tokens = new StringTokenizer(requestLine);
      tokens.nextToken(); // skip over the method, which should be "GET"
      String fileName = tokens.nextToken();
      // Prepend a "." so that the file request is within the current directory
      fileName = "." + fileName;

      // Open the requested file.
      FileInputStream fis = null;
      boolean fileExists = true;
      try
      {
         fis = new FileInputStream(fileName);
      }
      catch (FileNotFoundException e)
      {
         fileExists = false;
      }

      // Construct the response message.
      String statusLine = null;
      String contentTypeLine = null;
      String entityBody = null;
      if (fileExists)
      {
         statusLine = "HTTP/1.0 200 OK" + CRLF;
         contentTypeLine = "Content-type: " + contentType(fileName) + CRLF;
      }
      else
      {
         statusLine = "HTTP/1.0 404 Not Found" + CRLF;
         contentTypeLine = "Content-type: text/html" + CRLF;
         entityBody = "<html>" +
            "<head><title>Error 404</title></head>" +
            "<body>Not Found</body></html>";
      }

      // Send the status line.
      os.writeBytes(statusLine);
      // Send the content type line.
      os.writeBytes(contentTypeLine);
      // Send a blank line to indicate the end of the header lines.
      os.writeBytes(CRLF);

      // Send the entity body.
      if (fileExists)
      {
         sendBytes(fis, os);
         fis.close();
      }
      else
      {
         os.writeBytes(entityBody);
      }

      // Close streams and socket.
      os.close();
      br.close();
      socket.close();
   }

   /**
    * Reads the contents of the opened file and writes them to the socket's
    * output stream.
    *
    * @param fis - the InputStream of the opened file
    * @param os - the stream where the contents of the file will be sent. This
    * should be the socket output stream.
    *
    */
   private static void sendBytes(FileInputStream fis, OutputStream os)
      throws Exception
   {
      // Construct a 1K buffer to hold bytes on their way to the socket.
      byte[] buffer = new byte[1024];
      int bytes = 0;
      // Copy requested file into the socket's output stream.
      while((bytes = fis.read(buffer)) != -1 )
      {
         os.write(buffer, 0, bytes);
      }
   }

   /**
    * Returns the appropriate content type for the given filename.
    *
    * Note: could download the following mime types list, store it, make a
    * map out of it and return the type using that map.
    * http://svn.apache.org/viewvc/httpd/httpd/trunk/docs/conf/mime.types?revision=1506674
    *
    * @param fileName - the name of the file requested by the client
    *
    * @return the String with appropriate content type
    */
   private static String contentType(String fileName)
   {
      String type = "application/octed-stream";

      if (fileName.endsWith(".htm") || fileName.endsWith(".html"))
         type = "text/html";
      else if (fileName.endsWith(".txt") || fileName.endsWith(".java"))
         type = "text/plain";
      else if (fileName.endsWith(".gif"))
         type = "image/gif";
      else if (fileName.endsWith(".jpeg"))
         type = "image/jpeg";
      else if (fileName.endsWith(".png"))
         type = "image/png";
      else if (fileName.endsWith(".mp3"))
         type = "audio/mpeg";
      return type;
   }
}
