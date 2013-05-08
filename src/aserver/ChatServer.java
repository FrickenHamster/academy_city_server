package aserver;

/**
 * Created with IntelliJ IDEA.
 * User: Fricken Hamster
 * Date: 6/1/12
 * Time: 3:59 AM
 * Fricken Hamster's server for academy city game.
 * May this project succeed
 */

import java.net.*;
import java.io.*;
import java.util.*;

public class ChatServer extends Thread
{

	private ServerSocket socketServer;
	private int port;
	private boolean listening;
	private Vector<ChatServerConnection> clientConnections;
	private int connectionNum;
	
	private Vector<GameServer> gameServers;
	private final int gameServerMax = 6;
	private int gameServerNum;
	
	public ChatServer( int pp )
	{
		port = pp;
		clientConnections = new Vector<ChatServerConnection>();
		listening = false;
		
		gameServerNum = 0;
		gameServers = new Vector<GameServer>();
		
		startGameServer( 8 );

	}

	public void startGameServer( int maxP )
	{
		boolean ppf = false;
		int nn = 0;
		for ( int i = 0; i < gameServerNum; i ++ )
		{
			if ( gameServers.get( i ) == null )
			{
				nn = i;
				ppf = true;
				break;
			}
		}
		if ( !ppf )
		{
			nn = gameServerNum;
		}
		try 
		{
			int pp = Main.gamePortStart + nn;
			GameServer gg = new GameServer( pp , maxP );
			if ( ppf )
			{
				gameServers.set( nn , gg );
			}
			else
			{
				gameServers.add( gg );
			}
			gg.start();
			consoleText("Game Server started on port " + pp);
			ByteArrayOutputStream bout = new ByteArrayOutputStream();
			DataOutputStream dout = new DataOutputStream( bout );
			dout.writeByte( ChatServerConnection.NEWGAMEOPEN );
			dout.writeByte( nn );
			dout.writeShort( pp );
			byte[] ba = bout.toByteArray();
			sendByteArrayToAll( ba );
		}
		catch ( IOException e )
		{
			debug( e.getMessage() );
		}
		gameServerNum++;
	}
	
	private int addListConnection( ChatServerConnection connection)
	{
		for ( int i = 0; i < clientConnections.size() ; i++ )
		{
			if ( clientConnections.get( i ) == null )
			{
				clientConnections.set( i , connection );
				connection.setClientID( i );
				debug( "connection added at index:" + i );
				return i;
			}
		}
		int ii = clientConnections.size();
		clientConnections.add( connection );
		connection.setClientID(ii);
		debug( "connection added at index:" + ii );
		return ( ii ) ;
	}
	
	
	public void removeConnection( int id )
	{
		clientConnections.set(id , null);
		debug( "Removed connection " + id );
	}

	public void run()
	{
		try
		{
			socketServer = new ServerSocket(this.port);
			listening = true;
			debug( "listening");
			while ( listening )
			{
				Socket socket = socketServer.accept();
				consoleText( "client connection from " + socket.getRemoteSocketAddress());
				ChatServerConnection socketConnection = new ChatServerConnection( socket , this , 0 );
				socketConnection.start();
				int id = addListConnection(socketConnection);
				socket.setTcpNoDelay( true );
			};
		}
		catch( Exception e)
		{
			debug( e.getMessage() );
		}
	}


	public void sendByteArrayToAll( byte[] ba )
	{
		
		for (int i = 0; i < this.clientConnections.size(); i++)
		{
			ChatServerConnection cc = this.clientConnections.get(i);
			if ( cc != null )
			{
				//ChatServerConnection client = this.clientConnections.get(i);
				cc.sendByteArray( ba );
				
			}
		}
	}

	public void sendByteArrayToAllExcept( byte[] ba , int dontid )
	{
		for (int i = 0; i < this.clientConnections.size(); i++)
		{
			if ( i == dontid )
			{
				continue;
			}
			ChatServerConnection cc = this.clientConnections.get(i);
			if ( cc != null )
			{
				//ChatServerConnection client = this.clientConnections.get(i);
				cc.sendByteArray( ba );
			}
		}
	}

	public int getPort()
	{
		return port;
	}

	public int clientNum()
	{
		return this.clientConnections.size();
	}
	
	public int getGameNum()
	{
		return gameServers.size();
	}
	public GameServer getGameserver( int id )
	{
		return gameServers.get( id );
	}
	
	public ChatServerConnection getConnection( int id )
	{
		return clientConnections.get( id );
	}

	private void debug( String msg )
	{
		Main.debug( "ChatServer[" + port + "]" , msg );
	}
	private void consoleText( String msg )
	{
		Main.consoleText( "ChatServer[" + port + "]" , msg );
	}

}
