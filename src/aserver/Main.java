package aserver;

public class Main
{
	public static final boolean DEBUG = false;
	public static int gamePortStart;
	public static int policyPort;
	public static int chatPort;
	
	
	public static void debug( String label , String str )
	{
		if ( DEBUG )
		{
			System.out.println( label + ":" + str );
		}		
	}
	
	public static void consoleText( String label, String msg )
	{
		System.out.println( label + ":" + msg );
	}

	public static void main( String[] args )
	{
		try
		{
			policyPort = 13425;
			chatPort = 13426;
			gamePortStart = 13427;
			
			for (int i = 0; i < args.length; i++) 
			{
                if (i == 0)
				{
                    chatPort = Integer.parseInt(args[i]);
                }
                
                if (i == 1) {
                    policyPort = Integer.parseInt(args[i]);
                }
            }
			PolicyServer policyServer = new PolicyServer( policyPort );
			policyServer.start();
			consoleText( "Main" , "Policy Server started on port " + policyPort);
			ChatServer chatServer = new ChatServer( chatPort );
			chatServer.start();
			consoleText( "Main" , "Chat Server started on port " + chatPort);
			
			ServerInput consoleInput = new ServerInput( chatServer );
			consoleInput.start();
			
		}
		catch ( Exception e)
		{
			debug("Main" , e.getMessage());
		}
		
	}
	
	
	
	
}
