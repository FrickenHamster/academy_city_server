package gameobj;


import aserver.GameServer;
import aserver.Main;

/**
 * Created with IntelliJ IDEA.
 * User: Fricken Hamster
 * Date: 7/4/12
 * Time: 11:54 PM
 * To change this template use File | Settings | File Templates.
 */
public class GameField extends Thread
{
	private final long OPTIMALINTERVAL = 1000000000 / 30;

	private boolean runLoop;
	private long prevTime;
	
	GameServer gameServer;
	private int inGameTime;
	
	public GameField( GameServer ss )
	{
		gameServer = ss;
		inGameTime = 0;
	}
	
	public void gameStep( double delta)
	{
		gameServer.stepFunction( delta );
		inGameTime ++;
	}
	
	public void run()
	{
		runLoop = true;
		while ( runLoop )
		{
			long curTime = System.nanoTime();
			long changeTime = curTime - prevTime;
			double delta = changeTime / OPTIMALINTERVAL;

			prevTime = curTime;
			
			gameStep( delta );
			
			try
			{
				Thread.sleep( (prevTime - System.nanoTime())/1000000 + OPTIMALINTERVAL/1000000);
			}
			catch ( Exception e)
			{
				
			}
		}
	}
	
	public int getInGameTime()
	{
		return inGameTime;
	}
	
	public void printInGameTime()
	{
		consoleText( "Time is:" + inGameTime );
	}
	
	
	private void consoleText( String msg )
	{
		Main.consoleText( "GameField" , msg );
	}

}
