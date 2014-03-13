#!/usr/bin/env python

"""web_server.py: This is a simple perl implementation of a Web Server"""
__author__ = "Paulo Fagundes"
__date__   = "3/12/2014"

import sys
import thread
import socket
import os.path

CRLF = "\r\n"
DEFAULT_PORT = 6789

###############################################################################
# This class handles HTTP Requests. It is assumed that all the requests are
# GET requests. It checks if the requested object exists and returns it to the
# client. If the object doesn't exist, it returns a web page w/ error 404.
###############################################################################
class WebServer:

    ###########################################################################
    # WebServer constructor ~ opens the welcome socket for TCP connections
    ###########################################################################
    def __init__(self, port):
        self.s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        self.s.bind(('', port))
        self.s.listen(1)

    ###########################################################################
    # WebServer destructor ~ NOTE: note sure if necessary, since ^C
    ###########################################################################
    def __del__(self):
        self.s.close()

    ###########################################################################
    # listen for TCP requests, starting a new thread to process each request
    ###########################################################################
    def run(self):
        while 1:
            thread.start_new_thread(self.process_request, self.s.accept())

    ###########################################################################
    # reads from the client socket data, processes the GET request and sends
    # back the response to the client
    ###########################################################################
    def process_request(self, conn, addr):
        # get the data from the client (browser)
        data = conn.recv(1024)
        if not data:
            conn.close()
            return

        # get the filename from the GET request (assuming it's a GET)
        request_line = data.split(CRLF)[0]
        filename = "." + request_line.split()[1]

        # BUILD the status_line, content_type_line (and body)
        # if no filename given, display the files in the current directory
        if filename == "./":
            file_exists = False
            status_line = "HTTP/1.0 200 OK#{CRLF}"
            content_type_line = "Content-type: text/html" + CRLF
            entity_body = self.build_index()
        # if the filename was given, get the right content type
        elif os.path.isfile(filename):
            file_exists = True
            status_line = "HTTP/1.0 200 OK#{CRLF}"
            content_type_line = "Content-type: " + \
                                 self.get_content_type(filename) + \
                                 CRLF
        # else: file not found!
        else :
            file_exists = False
            status_line = "HTTP/1.0 404 Not Found" + CRLF
            content_type_line = "Content-type: text/html" + CRLF
            entity_body = "<html>\n<head>\n<title>Error 404</title>\n</head>\n"
            entity_body += "<body>\n\"" + filename + \
                                    "\" Not Found</body>\n</html>\n"

        # send the status line and content_type lines
        conn.sendall(status_line)
        conn.sendall(content_type_line)
        conn.sendall(CRLF)

        # if the file exists, send the contents of the file
        if file_exists :
            with open(filename, 'r') as f:
                output = f.read()
            conn.sendall(output)
        # else, send what was set for the body of the message
        else :
            conn.sendall(entity_body)

        conn.close() # close the connection / socket

    ###########################################################################
    # gets a list of files in this directory and builds an HTML page w/ it
    ###########################################################################
    def build_index(self):
        # make a list of the current files, and link to them
        body = "<html>\n<head>\n<title>Index</title></head>\n"
        body += "<ul>\n"
        for filename in os.listdir("."):
            body += "\t<li><a href=\"" + filename + "\">" + \
                    filename + "</a></li>\n"
        body += "</ul>\n"
        return body

    ###########################################################################
    # returns the appropriate content type for the given filename
    ###########################################################################
    def get_content_type(self, filename):
        ext = os.path.splitext(filename)[1] # get the extension
        c_type = 'application/octed-stream'
        if ext == '.html' or ext == '.htm' : c_type = 'text/html'
        if ext == '.txt'  or ext == '.py'  : c_type = 'text/plain'
        if ext == '.css' :                   c_type = 'text/css'
        if ext == '.jpeg' or ext == '.jpg' : c_type = 'image/jpeg'
        if ext == '.gif' :                   c_type = 'image/gif'
        if ext == '.png' :                   c_type = 'image/png'
        if ext == '.bmp' :                   c_type = 'image/bmp'
        if ext == '.xml' or ext == '.xsl'  : c_type = 'text/xml'
        return c_type

#############################################################################
# the "main" execution of the program
#############################################################################
port = int(sys.argv[1]) if len(sys.argv) > 1 else DEFAULT_PORT
server = WebServer(port)
server.run()
