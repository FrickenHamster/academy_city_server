package aserver;

import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 * Created with IntelliJ IDEA.
 * User: Fricken Hamster
 * Date: 7/9/12
 * Time: 8:44 PM
 * To change this template use File | Settings | File Templates.
 */
public class ServerInput extends Thread
{
	
	private boolean gettingInput;
	private ChatServer chatServer;
	
	public ServerInput( ChatServer cc )
	{
		gettingInput = true;
		chatServer = cc;
	}
	
	public void run()
	{
		
		try
		{
			BufferedReader br = new BufferedReader( new InputStreamReader( System.in )  );
			while ( gettingInput )
			{
				String input = br.readLine();
				//System.out.println( input );
				
				if ( input.length() > 0 )
				{
					String[] parse = input.split( " " );
					if ( parse[0].equals( "killserver" ))
					{
						System.exit(1);
					}
					else if ( parse[0].equals( "position"))
					{
						int nn = 0;
						if ( parse.length > 1 )
						{
							nn = Integer.parseInt(parse[1] );
						}
						GameServer gs = chatServer.getGameserver( nn );
						if ( gs != null)
						{
							gs.printPositions();
						}
					}
					else
					{
						consoleText("Unknown command :" + parse[0]);
					}
					
				}
			}
		}
		catch ( Exception e)
		{
			
		}
	}
	
	private void consoleText( String msg )
	{
		Main.consoleText( "" , msg );
	}

}
