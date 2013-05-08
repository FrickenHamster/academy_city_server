package aserver;

/**
 * Created with IntelliJ IDEA.
 * User: Fricken Hamster
 * Date: 6/1/12
 * Time: 4:04 AM
 * Fricken Hamster's server for academy city game.
 * May this project succeed
 */
import gameobj.GameClientInfo;

import java.io.*;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.Timer;
import java.util.TimerTask;

public class ChatServerConnection extends Thread
{
	private Socket socket;
	private ChatServer server;
	private DataInputStream inputStream;
	private DataOutputStream outputStream;

	private int clientID;
	private ChatClientInfo chatInfo;
	private boolean chatReady;
	
	private Timer heartBeatTimer;
	private Timer connectSend;
	private boolean heartResponse;
	private long pingPrev;
	private int pingInterval;
	private int timeoutCount;
	
	public static final int CONNECTID = 2;
	public static final int CONNECTEDPACKAGE = 3;
	public static final int NEWCONNECTTION = 4;
	public static final int SETNAME = 5;
	public static final int CLIENTDISCONNECT = 9;
	public static final int CHATMESSAGE = 10;
	public static final int GAMEOPENPACKAGE = 20;
	public static final int NEWGAMEOPEN = 21;
	public static final int GAMESERVERREQUEST = 24;
	
	public static final int HEARTBEAT = 101;
	public static final int PING = 102;

	public ChatServerConnection(Socket socket, ChatServer server , int id)
	{
		this.socket = socket;
		this.server = server;

		clientID = id;
		chatInfo = new ChatClientInfo();
		chatReady = false;
		
		pingInterval = 2000;
		heartBeatTimer = new Timer( );
		
		heartBeatTimer.schedule( new ChatHeartBeat(this ) , 2000 , pingInterval );
		pingPrev = -1;
		heartResponse = true;
		timeoutCount = 0;
		
		connectSend = new Timer();
		connectSend.schedule( new connectSend(this) , 500 );
		
	}
	
	class connectSend extends TimerTask
	{
		ChatServerConnection connection;
		public connectSend( ChatServerConnection cc )
		{
			connection = cc;
		}
		public void run()
		{
			//sendConnect();
			//sendConnectedPlayers();
		}
	}
	
	class ChatHeartBeat extends TimerTask
	{
		ChatServerConnection connection;
		public ChatHeartBeat( ChatServerConnection cc )
		{
			connection = cc;
		}
		
		public void run()
		{
			if ( heartResponse == false )
			{
				debug( "Timeout Count:" + timeoutCount );
				if ( timeoutCount > 3 )
				{
					
					closeConnection();
				}
				timeoutCount ++;
			}
			connection.sendHeartBeat();
			heartResponse = false;
			
			pingPrev = System.nanoTime();
		}
	}
	
	public void sendConnectedPlayers( )
	{
		try
		{
			ByteArrayOutputStream bout = new ByteArrayOutputStream();
			int numw = 0;
			for ( int i = 0; i < server.clientNum(); i ++ )
			{
				if ( i != this.clientID )
				{
	
					ChatServerConnection cc = server.getConnection( i );
					if ( cc == null )
					{
						debug("sconnected skip:" + i );
						continue;
					}
					numw++;
				}
			}
			bout.write(CONNECTEDPACKAGE);
			bout.write( numw );
			for ( int i = 0; i < server.clientNum(); i ++ )
			{
				if ( i != this.clientID )
				{
					
					ChatServerConnection cc = server.getConnection( i );
					if ( cc == null )
					{
						continue;
					}
					bout.write( i );
					if (cc.getChatInfo().getUserName() != null )
					{
						bout.write(1);
						String nn = cc.getChatInfo().getUserName();//.getBytes( "UTF-8" );
						byte bn[] = nn.getBytes(  );
						bout.write( bn.length );
						bout.write( bn );
					}
					else
					{
						bout.write( 0 );
					}
				}	
			}
			byte ba[] = bout.toByteArray();
			debug( "sent connected size:" + ba.length );
			sendByteArray(ba);
		}
		catch ( Exception e)
		{
			debug( "error writing connected:" + e.getMessage() );
		}
	}

	public void sendOpenGames( )
	{
		try
		{
			ByteArrayOutputStream bout = new ByteArrayOutputStream();
			DataOutputStream dout = new DataOutputStream( bout );
			int numw = 0;
			for ( int i = 0; i < server.getGameNum(); i ++ )
			{
				GameServer gg = server.getGameserver( i );
				if ( gg == null )
				{
					continue;
				}
				numw++;
			}
			debug( "gamesnum:" + numw );
			dout.writeByte(GAMEOPENPACKAGE);
			dout.writeByte(numw);
			for ( int i = 0; i < server.getGameNum(); i ++ )
			{
				GameServer gg = server.getGameserver( i );
				if ( gg == null )
				{
					continue;
				}
				dout.writeByte( i );
				dout.writeShort( gg.getPort() );
			}
			byte ba[] = bout.toByteArray();
			debug( "sent game size:" + ba.length );
			sendByteArray(ba);
			
		
		}
		catch ( IOException e)
		{
			debug( "error writing connected:" + e.getMessage() );
		}
	}
	
	public void sendConnectID(  )
	{
		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		bout.write( CONNECTID );
		bout.write( clientID );
		byte[] ba = bout.toByteArray();
		sendByteArray( ba );
	}
	
	public void sendHeartBeat()
	{
		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		bout.write( HEARTBEAT );
		byte ba[];
		ba = bout.toByteArray();
		debug( "sent Heart Beat" );
		sendByteArray(ba);
	}

	public void sendByteArray( byte[] ba )
	{
		try
		{
			ByteArrayOutputStream bout = new ByteArrayOutputStream();
			DataOutputStream dd = new DataOutputStream( bout );
			dd.writeShort( ba.length );
			dd.write( ba );
			byte[] bb = bout.toByteArray();
			
			outputStream.write(bb);
			outputStream.flush();
		}
		catch( Exception e )
		{
			debug( e.getMessage() );
			closeConnection();
		}
	}


	public void run()
	{
		try
		{
			inputStream = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
			outputStream = new DataOutputStream( socket.getOutputStream( ));
			
			while ( inputStream.available() != -1 )
			{
				int packetSize = inputStream.readShort() ;
				debug( "read bytearray size:" + packetSize );
				byte packetBuffer[] = new byte[packetSize];
				int byteTrans = 0;
				while ( byteTrans < packetSize )
				{
					inputStream.read( packetBuffer , byteTrans , 1 );
					byteTrans++;
				}
				ByteArrayInputStream bin = new ByteArrayInputStream( packetBuffer );
				DataInputStream packetStream = new DataInputStream(bin);
				int id = packetStream.readByte();
				debug("read id:" + id);
				String nn;
				byte ba[];
				
				ByteArrayOutputStream bout = new ByteArrayOutputStream();
				DataOutputStream databout = new DataOutputStream( bout );
				switch (id)
				{
					
					case CONNECTID:
						sendConnectID();
						break;
					
					case CONNECTEDPACKAGE:
						sendConnectedPlayers();
						break;
					
					case SETNAME:
						nn = packetStream.readUTF();
						chatInfo.setUserName( nn );
						debug("name set to:" + nn);
						
						bout.write( SETNAME );
						bout.write( clientID );
						bout.write( nn.getBytes( "UTF-8" ));

						ba = bout.toByteArray();
						server.sendByteArrayToAll( ba );
						break;

					case CHATMESSAGE:
						nn = packetStream.readUTF();
						debug( "message set to:" + nn );
						bout.write( CHATMESSAGE );
						bout.write( clientID );
						bout.write( nn.getBytes( "UTF-8" ));

						ba = bout.toByteArray();
						server.sendByteArrayToAll( ba );
						break;
					
					case GAMEOPENPACKAGE:
						sendOpenGames();
						break;
					
					case GAMESERVERREQUEST:
						ba = bout.toByteArray();
						sendByteArray( ba );
						break;
					
					case HEARTBEAT:
						heartResponse = true;
						timeoutCount = 0;
						
						short pp =(short) ((System.nanoTime() - (pingPrev))/1000000);
						
						databout.writeByte( PING );
						databout.writeByte( clientID );
						databout.writeShort(pp);
						//bout.write( 102);
						//bout.write( clientID );
						//bout.write( pp);
						
						ba = bout.toByteArray();
						server.sendByteArrayToAll( ba );
						if ( pingPrev != -1 )
							debug( "ping:" + pp );
						
						break;
				}
				
				
			}
		} catch (Exception e)
		{
			debug(e.getMessage());
		}

	}


	public SocketAddress getRemoteAddress()
	{
		return socket.getRemoteSocketAddress();
	}
	
	public void setClientID( int id )
	{
		clientID = id;
	}
	public int getClientID()
	{
		return clientID;
	}
	
	public ChatClientInfo getChatInfo()
	{
		return chatInfo;
	}
	


	private void debug(String msg)
	{
		Main.debug("ChatConnection[" + getRemoteAddress() + "]", msg);
	}

	private void consoleText( String msg )
	{
		Main.consoleText("ChatConnection[" + getRemoteAddress() + "]" , msg );
	}

	protected void closeConnection()
	{
		try {
			ByteArrayOutputStream bout = new ByteArrayOutputStream();
			bout.write(CLIENTDISCONNECT);
			bout.write(clientID);
			byte ba[] = bout.toByteArray();
			server.sendByteArrayToAllExcept(ba , clientID);
			server.removeConnection( clientID );
			heartBeatTimer.cancel();
			this.outputStream.close();
			this.inputStream.close();
			this.socket.close();
			consoleText("connection closed");
		}
		catch (Exception e) {
			debug("Exception (closing): " + e.getMessage());
		}
	}
}
