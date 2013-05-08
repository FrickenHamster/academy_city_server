package aserver;

import java.io.*;
import java.net.*;

public class PolicyServer extends Thread
{
	public static final String POLICY_REQUEST = "<policy-file-request/>";
    public static final String POLICY_XML =
            "<?xml version=\"1.0\"?>"
            + "<cross-domain-policy>"
            + "<allow-access-from domain=\"*\" to-ports=\"*\" />"
            + "</cross-domain-policy>";
    
    private int port;
    private ServerSocket serverSocket;
    private boolean listening;
    
    
    public PolicyServer( int serverPort )
    {
    	port = serverPort;
    	listening = false;
    }
    
    public void run()
    {
    	try
    	{
    		this.serverSocket = new ServerSocket( this.port);
    		listening = true;
    		debug( "listening");
    		
    		while ( listening)
    		{
    			Socket socket = serverSocket.accept();
    			consoleText( "client connection from " + socket.getRemoteSocketAddress());
    			PolicyServerConnection sCon = new PolicyServerConnection(socket);
    			sCon.start();
    		};
    	}
    	catch ( Exception e)
    	{
    		debug( e.getMessage());
    	}
    }
    
    
   
    
	
    public int getPort()
    {
    	return port;
    }
    
    public boolean getListening()
    {
    	return listening;
    }
    
    protected void finalize() {	 
        try {
            this.serverSocket.close();
            this.listening = false;
            debug("stopped");
        }
        catch (Exception e) {
            debug("Exception (finalize): " + e.getMessage());
        }
    }
    private void debug( String str )
    {
    	Main.debug( "PolicyServer" ,  str);
    }
	private void consoleText( String msg )
	{
		Main.consoleText( "PolicyServer" , msg );
	}
}
