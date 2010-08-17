package com.collabnet.svnedge.teamforge;

import javax.servlet.http.HttpServletResponse;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

    /**
     * Encapsulates the knowledge of how to run a CGI script, given the
     * script's desired environment and (optionally) input/output streams
     *
     * <p>
     *
     * Exposes a <code>run</code> method used to actually invoke the
     * CGI.
     *
     * </p>
     * <p>
     *
     * The CGI environment and settings are derived from the information
     * passed to the constuctor.
     *
     * </p>
     * <p>
     *
     * The input and output streams can be set by the <code>setInput</code>
     * and <code>setResponse</code> methods, respectively.
     * </p>
     *
     * @author    Martin Dengler [root@martindengler.com]
     * @version   $Revision$, $Date$
     */

public class CGIRunner {

        private int debug = 1;
    
        /** script/command to be executed */
        private String command = null;

        /** environment used when invoking the cgi script */
        private Map<String, String> env = null;

        /** working directory used when invoking the cgi script */
        private File wd = null;

        /** query parameters to be passed to the invoked script */
        private Map<String, String> params = null;

        /** stdin to be passed to cgi script */
        private InputStream stdin = null;

        /** response object used to set headers & get output stream */
        private HttpServletResponse response = null;

        /** boolean tracking whether this object has enough info to run() */
        private boolean readyToRun = false;




        /**
         *  Creates a CGIRunner and initializes its environment, working
         *  directory, and query parameters.
         *  <BR>
         *  Input/output streams (optional) are set using the
         *  <code>setInput</code> and <code>setResponse</code> methods,
         *  respectively.
         *
         * @param  command  string full path to command to be executed
         * @param  env      Hashtable with the desired script environment
         * @param  wd       File with the script's desired working directory
         * @param  params   Hashtable with the script's query parameters
         */
        protected CGIRunner(String command, Map<String, String> env, File wd,
                            Map<String, String> params) {
            this.command = command;
            this.env = env;
            this.wd = wd;
            this.params = params;
            updateReadyStatus();
        }



        /**
         * Checks & sets ready status
         */
        protected void updateReadyStatus() {
            if (command != null
                && env != null
                && wd != null
                && params != null
                && response != null) {
                readyToRun = true;
            } else {
                readyToRun = false;
            }
        }



        /**
         * Gets ready status
         *
         * @return   false if not ready (<code>run</code> will throw
         *           an exception), true if ready
         */
        protected boolean isReady() {
            return readyToRun;
        }



        /**
         * Sets HttpServletResponse object used to set headers and send
         * output to
         *
         * @param  response   HttpServletResponse to be used
         *
         */
        protected void setResponse(HttpServletResponse response) {
            this.response = response;
            updateReadyStatus();
        }



        /**
         * Sets standard input to be passed on to the invoked cgi script
         *
         * @param  stdin   InputStream to be used
         *
         */
        protected void setInput(InputStream stdin) {
            this.stdin = stdin;
            updateReadyStatus();
        }



        /**
         * Converts a Hashtable to a String array by converting each
         * key/value pair in the Hashtable to a String in the form
         * "key=value" (hashkey + "=" + hash.get(hashkey).toString())
         *
         * @param  h   Hashtable to convert
         *
         * @return     converted string array
         *
         * @exception  NullPointerException   if a hash key has a null value
         *
         */
        protected String[] hashToStringArray(Map<String, String> h) {
            String[] strArr;
            if (h != null && !h.isEmpty()) {
                List<String> v = new ArrayList<String>(h.size());
                Set<Map.Entry<String, String>> paramSet = h.entrySet();
                for (Map.Entry<String, String> e : paramSet) {
                    v.add(e.getKey() + "=" + e.getValue());
                }
                strArr = (String[]) v.toArray(new String[v.size()]);
            } else {
                strArr = new String[0];
            }
            return strArr;
        }



        /**
         * Executes a CGI script with the desired environment, current working
         * directory, and input/output streams
         *
         * <p>
         * This implements the following CGI specification recommedations:
         * <UL>
         * <LI> Servers SHOULD provide the "<code>query</code>" component of
         *      the script-URI as command-line arguments to scripts if it
         *      does not contain any unencoded "=" characters and the
         *      command-line arguments can be generated in an unambiguous
         *      manner.
         * <LI> Servers SHOULD set the AUTH_TYPE metavariable to the value
         *      of the "<code>auth-scheme</code>" token of the
         *      "<code>Authorization</code>" if it was supplied as part of the
         *      request header.  See <code>getCGIEnvironment</code> method.
         * <LI> Where applicable, servers SHOULD set the current working
         *      directory to the directory in which the script is located
         *      before invoking it.
         * <LI> Server implementations SHOULD define their behavior for the
         *      following cases:
         *     <ul>
         *     <LI> <u>Allowed characters in pathInfo</u>:  This implementation
         *             does not allow ASCII NUL nor any character which cannot
         *             be URL-encoded according to internet standards;
         *     <LI> <u>Allowed characters in path segments</u>: This
         *             implementation does not allow non-terminal NULL
         *             segments in the the path -- IOExceptions may be thrown;
         *     <LI> <u>"<code>.</code>" and "<code>..</code>" path
         *             segments</u>:
         *             This implementation does not allow "<code>.</code>" and
         *             "<code>..</code>" in the the path, and such characters
         *             will result in an IOException being thrown;
         *     <LI> <u>Implementation limitations</u>: This implementation
         *             does not impose any limitations except as documented
         *             above.  This implementation may be limited by the
         *             servlet container used to house this implementation.
         *             In particular, all the primary CGI variable values
         *             are derived either directly or indirectly from the
         *             container's implementation of the Servlet API methods.
         *     </ul>
         * </UL>
         * </p>
         *
         * @exception IOException if problems during reading/writing occur
         *
         * @see    java.lang.Runtime#exec(String command, String[] envp,
         *                                File dir)
         */
    protected void run() throws IOException {

            /*
             * REMIND:  this method feels too big; should it be re-written?
             */

            if (!isReady()) {
                throw new IOException(this.getClass().getName()
                                      + ": not ready to run.");
            }

            if (debug >= 1 ) {
                log("runCGI(envp=[" + env + "], command=" + command + ")");
            }

            if ((command.indexOf(File.separator + "." + File.separator) >= 0)
                || (command.indexOf(File.separator + "..") >= 0)
                || (command.indexOf(".." + File.separator) >= 0)) {
                throw new IOException(this.getClass().getName()
                                      + "Illegal Character in CGI command "
                                      + "path ('.' or '..') detected.  Not "
                                      + "running CGI [" + command + "].");
            }

            /* original content/structure of this section taken from
             * http://developer.java.sun.com/developer/
             *                               bugParade/bugs/4216884.html
             * with major modifications by Martin Dengler
             */
            Runtime rt = null;
            InputStream commandsStdOut = null;
            BufferedReader commandsStdErr = null;
            BufferedOutputStream commandsStdIn = null;
            Process proc = null;
            int bufRead = -1;

            StringBuilder cmdAndArgs = new StringBuilder("python ");
            cmdAndArgs.append(command);
            //create query arguments
            Set<Map.Entry<String, String>> paramSet = params.entrySet();
            if (paramSet != null && !paramSet.isEmpty()) {
                cmdAndArgs.append(" ");
                for (Map.Entry<String, String> e : paramSet) {
                    String k = e.getKey();
                    String v = e.getValue();
                    if ((k.indexOf("=") < 0) && (v.indexOf("=") < 0)) {
                        cmdAndArgs.append(k);
                        cmdAndArgs.append("=");
                        v = java.net.URLEncoder.encode(v, "UTF-8");
                        cmdAndArgs.append(v);
                        cmdAndArgs.append(" ");
                    }
                }
            }

            rt = Runtime.getRuntime();
            proc = rt.exec(cmdAndArgs.toString(), hashToStringArray(env), wd);

      String sContentLength = (String) env.get("CONTENT_LENGTH");
      if(!"".equals(sContentLength)) {
	  commandsStdIn = new BufferedOutputStream(proc.getOutputStream());
	  try {
	      byte[] content = new byte[Integer.parseInt(sContentLength)];

	      int lenRead = stdin.read(content);

	      if ("POST".equals(env.get("REQUEST_METHOD"))) {
		  String paramStr = getPostInput(params);
		  if (paramStr != null) {
		      byte[] paramBytes = paramStr.getBytes();
		      commandsStdIn.write(paramBytes);

		      int contentLength = paramBytes.length;
		      if (lenRead > 0) {
			  String lineSep = System.getProperty("line.separator");

			  commandsStdIn.write(lineSep.getBytes());

			  contentLength = lineSep.length() + lenRead;
		      }

		      env.put("CONTENT_LENGTH", String.valueOf(contentLength));
		  }
	      }

	      if (lenRead > 0) {
		  commandsStdIn.write(content, 0, lenRead);
	      }


	      commandsStdIn.flush();
	  } finally {
	      commandsStdIn.close();
	  }
      }

            /* we want to wait for the process to exit,  Process.waitFor()
             * is useless in our situation; see
             * http://developer.java.sun.com/developer/
             *                               bugParade/bugs/4223650.html
             */

            boolean isRunning = true;
            commandsStdOut = new BufferedInputStream(proc.getInputStream());
            commandsStdErr = new BufferedReader
                (new InputStreamReader(proc.getErrorStream()));
            OutputStream servletContainerStdout = null;

	    try {

		try {
		    if (response.getOutputStream() != null) {
			servletContainerStdout =
				new BufferedOutputStream(response.getOutputStream());
		    }
		} catch (IOException ignored) {
		    //NOOP: no output will be written
		}
		final BufferedReader stdErrRdr = commandsStdErr;

		new Thread() {
		    public void run() {
			sendToLog(stdErrRdr);
		    } ;
		}.start();


		while (isRunning) {

		    try {

			//set headers
			String line = null;
			while (((line = readline(commandsStdOut)) != null)
				&& !("".equals(line))) {
			    if (debug >= 2) {
				log("runCGI: addHeader(\"" + line + "\")");
			    }
			    if (line.startsWith("HTTP") || line.startsWith("HTTPS")) {
				//TODO: should set status codes (NPH support)
				/*
				 * response.setStatus(getStatusCode(line));
				 */
			    } else if (line.indexOf(":") >= 0) {
				if (line.startsWith("Status:")) {
				    String status = line.substring(line.indexOf(":") + 1).trim();
				    status = status.substring(0, status.indexOf(" "));
				    response.setStatus(Integer.parseInt(status));
				}
				response.addHeader
					(line.substring(0, line.indexOf(":")).trim(),
						line.substring(line.indexOf(":") + 1).trim());
			    } else {
				log("runCGI: bad header line \"" + line + "\"");
			    }
			}

			//write output
			byte[] cBuf = new byte[1024];
			while ((bufRead = commandsStdOut.read(cBuf)) != -1) {
			    if (servletContainerStdout != null) {
				if (debug >= 4) {
				    log("runCGI: write(\"" + new String(cBuf, 0, bufRead) + "\")");
				}
				servletContainerStdout.write(cBuf, 0, bufRead);
			    }
			}

			if (servletContainerStdout != null) {
			    servletContainerStdout.flush();
			}

			proc.exitValue(); // Throws exception if alive

			isRunning = false;

		    } catch (IllegalThreadStateException e) {
			try {
			    Thread.sleep(500);
			} catch (InterruptedException ignored) {
			}
		    }
		} //replacement for Process.waitFor()
	    } finally {
		try { commandsStdOut.close(); } catch (Throwable t) { /* ignore */ }
		try { commandsStdErr.close(); } catch (Throwable t) { /* ignore */ }
		if (servletContainerStdout != null) {
		    try { servletContainerStdout.close(); } catch (Throwable t) { /* ignore */ }
		}
	    }
        }

        private void sendToLog(BufferedReader rdr) {
            String line = null;
            int lineCount = 0 ;
            try {
                while ((line = rdr.readLine()) != null) {
                    log("runCGI (stderr):" +  line) ;
                }
                lineCount++ ;
            } catch (IOException e) {
                log("sendToLog error", e) ;
            } finally {
                try {
                    rdr.close() ;
                } catch (IOException ce) {
                    log("sendToLog error", ce) ;
                } ;
            } ;
            if ( lineCount > 0 && debug > 2) {
                log("runCGI: " + lineCount + " lines received on stderr") ;
            } ;
        }


        /**
         * Gets a string for input to a POST cgi script
         *
         * @param  params   Hashtable of query parameters to be passed to
         *                  the CGI script
         * @return          for use as input to the CGI script
         */

        protected String getPostInput(Map<String, String> params) {
            String lineSeparator = System.getProperty("line.separator");
            StringBuilder postInput = new StringBuilder();
            StringBuilder qs = new StringBuilder();
            Set<Map.Entry<String, String>> paramSet = params.entrySet();
            if (paramSet != null && !paramSet.isEmpty()) {
                for (Map.Entry<String, String> e : paramSet) {
                    String k = e.getKey();
                    String v = e.getValue();
                    if ((k.indexOf("=") < 0) && (v.indexOf("=") < 0)) {
                        postInput.append(k);
                        qs.append(k);
                        postInput.append("=");
                        qs.append("=");
                        postInput.append(v);
                        qs.append(v);
                        postInput.append(lineSeparator);
                        qs.append("&");
                    }
                }
            }
            qs.append(lineSeparator);
            return qs.append(postInput.toString()).toString();
        }

    private static String readline(InputStream s) throws IOException {
        StringBuffer result = new StringBuffer();
	boolean isEOF = false;
        while (true) {
            int b = s.read();
            if (b == '\r') {
                b = s.read();
            }
            if (b == '\n') {
                break;
            } else if (b == -1) {
		isEOF = true;
		break;
	    }
            result.append((char)b);
        }
	if (result.length() == 0 && isEOF) {
	    return null;
	}
        return result.toString();
    }
    
    private void log(String s) {
        System.out.println(s);
    }

    private void log(String s, Exception e) {
        System.out.println(s);
        e.printStackTrace();
    }
}
