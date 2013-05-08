package aserver;

import gameobj.GameClientInfo;

import java.io.*;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.Timer;
import java.util.TimerTask;

public class GameServerConnection extends Thread
{
	private Socket socket;
	private GameServer server;
	private DataInputStream inputStream;
	private DataOutputStream outputStream;

	private Timer heartBeatTimer;
	private Timer connectSend;
	private boolean heartResponse;
	private long pingPrev;
	private int pingInterval;
	private int timeoutCount;
	
	private int clientID;
	private GameClientInfo gameInfo;

	public static final int CONNECTID = 2;
	public static final int CONNECTEDPACKAGE = 3;
	public static final int NEWCONNECTTION = 4;
	public static final int SETNAME = 5;
	public static final int CLIENTDISCONNECT = 9;
	public static final int CHATMESSAGE = 10;
	public static final int REQUESTSPAWN = 30;
	public static final int SPAWNPLAYER = 31;
	public static final int POSITIONUPDATE = 40;
	public static final int TARGETMOVEPOSITION = 41;

	public static final int HEARTBEAT = 101;
	public static final int PING = 102;

	public GameServerConnection(Socket socket, GameServer server , int id)
	{
		this.socket = socket;
		this.server = server;

		pingInterval = 500;
		heartBeatTimer = new Timer( );

		heartBeatTimer.schedule( new ChatHeartBeat(this ) , 2000 , pingInterval );
		pingPrev = -1;
		heartResponse = true;
		timeoutCount = 0;
		
		clientID = id;
		gameInfo = new GameClientInfo();
	}

	class ChatHeartBeat extends TimerTask
	{
		GameServerConnection connection;
		public ChatHeartBeat( GameServerConnection cc )
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

					//closeConnection();
				}
				timeoutCount ++;
			}
			connection.sendHeartBeat();
			heartResponse = false;

			pingPrev = System.nanoTime();
		}
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
			consoleText( e.getMessage() );
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
				int cx;
				int cy;
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
						gameInfo.setUserName( nn );
						debug("name set to:" + nn);

						bout.write( SETNAME );
						bout.write( clientID );
						bout.write( nn.getBytes( "UTF-8" ));

						ba = bout.toByteArray();
						server.sendByteArrayToAll( ba );
						break;
					
					case REQUESTSPAWN:
						if ( !gameInfo.isSpawned())
						{
							sendSpawn( 200 , 200 );
							gameInfo.spawnStranded( 200 , 200 );
						}
						break;
						
					case POSITIONUPDATE:
						cx = packetStream.readShort();
						cy = packetStream.readShort();
						gameInfo.getStranded().setPosition( cx , cy );
						databout.writeByte( POSITIONUPDATE );
						databout.writeByte( clientID );
						databout.writeShort( cx );
						databout.writeShort( cy );
						ba = bout.toByteArray();
						server.sendByteArrayToAllExcept( ba , clientID );
						break;
					case TARGETMOVEPOSITION:
						cx = packetStream.readShort();
						cy = packetStream.readShort();
						gameInfo.getStranded().moveTarget( cx , cy );
						databout.writeByte( TARGETMOVEPOSITION );
						databout.writeByte( clientID );
						databout.writeShort( cx );
						databout.writeShort( cy );
						ba = bout.toByteArray();
						server.sendByteArrayToAll( ba  );
					
				}
			}
		} catch (Exception e)
		{
			consoleText(e.getMessage());
		}

	}

	public void sendConnectedPlayers( )
	{
		try
		{
			ByteArrayOutputStream bout = new ByteArrayOutputStream();
			DataOutputStream dout = new DataOutputStream( bout );
			int numw = 0;
			for ( int i = 0; i < server.clientNum(); i ++ )
			{
				if ( i != this.clientID )
				{
					GameServerConnection cc = server.getConnection( i );
					if ( cc == null )
					{
						continue;
					}
					numw++;
				}
			}
			dout.writeByte(CONNECTEDPACKAGE);
			dout.writeByte(numw);
			for ( int i = 0; i < server.clientNum(); i ++ )
			{
				if ( i != this.clientID )
				{

					GameServerConnection cc = server.getConnection( i );
					if ( cc == null )
					{
						continue;
					}
					dout.writeByte(i);
					GameClientInfo gg = cc.getGameInfo();
					if (gg.getUserName() != null )
					{
						bout.write(1);
						String nn = gg.getUserName();//.getBytes( "UTF-8" );
						byte bn[] = nn.getBytes(  );
						dout.write( bn.length );
						dout.write( bn );
					}
					else
					{
						dout.writeByte(0);
					}
					if (gg.isSpawned() )
					{
						dout.writeByte(1);
						dout.writeShort(gg.getLocX());
						dout.writeShort(gg.getLocY());
						consoleText( "spawned connected:" + gg.getLocX() + " , " + gg.getLocY() );
					}
					else
					{
						dout.writeByte(0);
					}
				}
			}
			consoleText("sent player package of:" + numw );
			byte ba[] = bout.toByteArray();
			sendByteArray(ba);
		}
		catch ( Exception e)
		{
			consoleText( "error writing connected:" + e.getMessage() );
		}
	}
	
	public void sendSpawn( int sx , int sy)
	{
		try 
		{
			ByteArrayOutputStream bout = new ByteArrayOutputStream();
			DataOutputStream dout = new DataOutputStream(bout);
			dout.writeByte( SPAWNPLAYER );
			dout.writeByte( clientID );
			dout.writeShort( sx );
			dout.writeShort( sy );
			byte ba[] = bout.toByteArray();
			server.sendByteArrayToAll( ba );
		}
		catch ( Exception e  )
		{
			consoleText( "error sending spawn:" + e.getMessage());
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
	
	public GameClientInfo getGameInfo()
	{
		return gameInfo;
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

	private void debug(String msg)
	{
		Main.debug("GameConnection[" + getRemoteAddress() + "]", msg);
	}
	private void consoleText( String msg )
	{
		Main.consoleText( "GameConnection[" + getRemoteAddress() +"," + clientID + "]" , msg );
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
			consoleText("Exception (closing): " + e.getMessage());
		}
	}
}
