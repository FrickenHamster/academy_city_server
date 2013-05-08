package aserver;

import gameobj.GameClientInfo;
import gameobj.GameField;

import java.net.*;
import java.io.*;
import java.util.*;

public class GameServer extends Thread
{

	private ServerSocket socketServer;
	private int port;
	private boolean listening;
	private Vector<GameServerConnection> clientConnections;
	private int maxPlayers;
	
	private GameField gameField;
	
	public GameServer( int pp , int maxP )
	{
		 port = pp;
		 clientConnections = new Vector<GameServerConnection>();
		 listening = false;
		 maxPlayers = maxP;
	}

	public void run()
	{
		try
		{
			gameField = new GameField( this );
			gameField.start();
			socketServer = new ServerSocket(this.port);
			listening = true;
			debug( "listening");
			while ( listening )
			{
				Socket socket = socketServer.accept();
				consoleText( "client connection from " + socket.getRemoteSocketAddress());
				GameServerConnection socketConnection = new GameServerConnection( socket , this , 0 );
				socketConnection.start();
				socket.setTcpNoDelay( true );
				addListConnection(socketConnection);
				
			};
		}
		catch( Exception e)
		{
			debug( e.getMessage() );
		}
	}
	
	private int addListConnection( GameServerConnection connection)
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
		consoleText( "Removed connection " + id );
	}
	
	public void stepFunction( double delta )
	{
		for (int i = 0; i < this.clientConnections.size(); i++)
		{
			GameServerConnection client = this.clientConnections.get(i);
			if ( client != null )
			{
				client.getGameInfo().stepFunction( delta );
			}
		}
	}
	
	public void printPositions()
	{
		for (int i = 0; i < this.clientConnections.size(); i++)
		{
			GameServerConnection client = this.clientConnections.get(i);
			if ( client != null )
			{
				GameClientInfo ginfo = client.getGameInfo();
				consoleText( ginfo.getUserName() + " position is :" + ginfo.getLocX() + "," + ginfo.getLocY( ) );
			}
		}
	}
	
	public void sendByteArrayToAll( byte[] ba )
	{
		for (int i = 0; i < this.clientConnections.size(); i++)
		{
			
			GameServerConnection client = this.clientConnections.get(i);
			if ( client != null )
			{
				client.sendByteArray( ba );
			}
		}
	}

	public void sendByteArrayToAllExcept( byte[] ba , int skipid )
	{

		for (int i = 0; i < this.clientConnections.size(); i++)
		{
			if ( i == skipid )
			{
				continue;
			}
			GameServerConnection cc = this.clientConnections.get(i);
			if ( cc != null )
			{
				cc.sendByteArray( ba );
			}
		}
	}

	public GameServerConnection getConnection( int id )
	{
		return clientConnections.get( id );
	}
	
	public int getPort()
	{
		return port;
	}

	public int clientNum()
	{
		return this.clientConnections.size();
	}
	
	private void consoleText( String msg )
	{
		Main.consoleText( "GameServer[" + port + "]" , msg );
	}

	private void debug( String msg )
	{
		Main.debug( "GameServer[" + port + "]" , msg );
	}

}
