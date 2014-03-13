This is a simple implementation of a Web Server that I've implemented in Java, Ruby and Python. It assumes that all requests are GET requests. If a file that is requested exists, then it returns the contents of that file. Else, it returns Error 404.

How to run:

    ./server PORT_NUMBER # note that "./server" depends on which language is being used

Example:

    # Python:
    python web_server.py 6789
    # then on the browser, if I go to http://127.0.0.1:6789/ then I should see the server's response.
    # Requesting an existing file:
    # on the browser: http://127.0.0.1:6789/web_server.py

Fell free to test this by adding images / other types of files to the folder where your server is being ran and then requesting it through the browser.

Note: I've tried to keep all three implementations the same, but the Python implementation (my most recent) has one extra feature that it displays a list of files if no filenames are given in the browser.
