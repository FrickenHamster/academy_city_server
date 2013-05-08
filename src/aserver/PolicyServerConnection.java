package aserver;

import java.io.*;
import java.net.*;

public class PolicyServerConnection extends Thread
{
	
	private Socket socket;
	private BufferedReader socketIn;
	private PrintWriter socketOut;
	
	public PolicyServerConnection( Socket ss)
	{
		socket = ss;
	}
	
	
	
	public void run()
	{
		try
		{
			socketIn = new BufferedReader( new InputStreamReader( socket.getInputStream()));
			socketOut = new PrintWriter( this.socket.getOutputStream() , true);
			readPolicyRequest();
		}
		catch ( Exception e)
    	{
    		debug( e.getMessage());
    	}
	}
	
	private void readPolicyRequest()
	{
		try
		{
			String request = read();
			consoleText( "recieved:" + request);
			
			if ( request.equals(PolicyServer.POLICY_REQUEST))
			{
				writePolicy();
			}
			
		}
		catch ( Exception e)
    	{
    		debug( e.getMessage());
    	}
	}
	
	protected void writePolicy() 
	{
        try 
        {
            this.socketOut.write(PolicyServer.POLICY_XML + "\u0000");
            this.socketOut.close();
            debug("policy sent to client");
        }
        catch (Exception e) {
            debug("Exception (writePolicy): " + e.getMessage());
        }
    }
	
	private String read() 
	{
        StringBuffer buffer = new StringBuffer();
        int codePoint;
        boolean zeroByteRead = false;
        
        try {
            do {
                codePoint = this.socketIn.read();

                if (codePoint == 0) {
                    zeroByteRead = true;
                }
                else if (Character.isValidCodePoint(codePoint)) {
                    buffer.appendCodePoint(codePoint);
                }
            }
            while (!zeroByteRead && buffer.length() < 200);
        }
        catch (Exception e) {
            debug("Exception (read): " + e.getMessage());
        }
        
        return buffer.toString();
    }
	
	protected void finalize()
	{
		try {
            this.socketIn.close(); 
            this.socketOut.close();
            this.socket.close();
            debug("connection closed");
        }
        catch (Exception e) {
            debug("Exception (finalize): " + e.getMessage());
        }
	}
	
	private void debug( String str )
	{
		Main.debug("PolicyConnection" , str);
	}
	private void consoleText( String msg )
	{
		Main.consoleText( "PolicyConnection:" , msg );
	}

}
