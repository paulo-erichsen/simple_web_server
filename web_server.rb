###############################################################################
# Author: Paulo Fagundes
# Date: 1/22/2014
#
# This is a simple ruby implementation of a Web Server.
###############################################################################
require 'socket'

###############################################################################
# This class handles HTTP Requests. It is assumed that all the requests are
# GET requests. It checks if the requested object exists and returns it to the
# client. If the object doesn't exist, it returns a web page w/ error 404.
###############################################################################
class HttpRequest
  CRLF = "\r\n"

  #############################################################################
  # HttpRequest constructor
  #
  # socket must be a TCPSocket object
  #############################################################################
  def initialize(socket)
    @socket = socket
  end

  #############################################################################
  # this method processes the GET request, builds the HTTP response and sends
  # it back through the socket
  #############################################################################
  def process_request
    # get the request line of the HTTP request message
    request_line = @socket.gets
    return unless request_line # don't continue unless there's a request

    # Display the request line
    puts "\n#{request_line}"

    # get and display the header lines
    # NOTE: the check for CRLF is such that it won't be stuck on infinite loop
    while (header_line = @socket.gets()) && header_line != CRLF
      puts header_line
    end

    # extract the filename from the request line
    filename = request_line.split[1]

    # Prepend a "." so that the file request is within the current directory
    filename.prepend('.')

    # open the requested file
    if File.exists?(filename) && !File.directory?(filename)
      file_exists = true
      file = File.open(filename)
    else
      file_exists = false
    end

    # construct the response message
    if file_exists
      status_line = "HTTP/1.0 200 OK#{CRLF}"
      content_type_line = "Content-type: #{get_content_type(filename)}#{CRLF}"
    else
      status_line = "HTTP/1.0 404 Not Found#{CRLF}"
      content_type_line = "Content-type: text/html#{CRLF}"
      entity_body = "<html>\n<head>\n<title>Error 404</title>\n</head>\n"
      entity_body << "<body>\n\"#{filename}\" Not Found</body>\n</html>\n"
    end

    # send the status line
    @socket.print(status_line)
    # send the content type line
    @socket.print(content_type_line)
    # send a blank line to indicate the end of the header lines
    @socket.print(CRLF)

    # send the entity body
    if file_exists
      send_bytes(file)
      file.close
    else
      @socket.print(entity_body)
    end

    # close the socket
    @socket.close
  end

  #############################################################################
  # ///////////////////////////////////////////////////////////////////////////
  # /////////////////////////// PRIVATE METHODS ///////////////////////////////
  # ///////////////////////////////////////////////////////////////////////////
  #############################################################################

  private

  #############################################################################
  # reads 1024 bytes from the file at a time and sends them to the socket
  #############################################################################
  def send_bytes(file)
    until file.eof?
      buffer = file.read(1024)
      @socket.write(buffer)
    end
  end

  #############################################################################
  # returns the appropriate content type for the given filename
  #############################################################################
  def get_content_type(filename)
    ext = File.extname(filename) # get the extension
    type = 'application/octed-stream'
    type = 'text/html'  if ext == '.html' || ext == '.htm'
    type = 'text/plain' if ext == '.txt'  || ext == '.rb'
    type = 'text/css'   if ext == '.css'
    type = 'image/jpeg' if ext == '.jpeg' || ext == '.jpg'
    type = 'image/gif'  if ext == '.gif'
    type = 'image/png'  if ext == '.png'
    type = 'image/bmp'  if ext == '.bmp'
    type = 'text/xml'   if ext == '.xml' || ext == '.xsl'
    type # same as: return type
  end
end

#############################################################################
# the "main" execution of the program
#############################################################################
port = ARGV.first ? ARGV.first : 6789 # port number optionally set with ARGV
server = TCPServer.new(port) # the "welcome socket"

# listens for TCP connection requests
while (socket = server.accept) # socket is a TCPSocket -> the session socket
  # creates an object to process the HTTP request message
  # creates a new thread to process the request
  Thread.new { HttpRequest.new(socket).process_request }
end
