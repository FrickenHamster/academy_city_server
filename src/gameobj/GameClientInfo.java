package gameobj;



/**
 * Created with IntelliJ IDEA.
 * User: Fricken Hamster
 * Date: 5/31/12
 * Time: 3:50 AM
 * To change this template use File | Settings | File Templates.
 */
public class GameClientInfo
{
	
	private String userName;
	private boolean playerSpawned;
	private int locX;
	private int locY;
	
	private ObjStranded stranded;
	
	public GameClientInfo()
	{
		playerSpawned = false;
		stranded = new ObjStranded();
	}
	
	public  void setUserName( String nn )
	{
		userName = nn;
	}
	public String getUserName()
	{
		return userName;
	}
	
	public void spawnStranded( int sx , int sy )
	{
		playerSpawned = true;
		stranded.spawn( sx , sy );
	}
	
	public void stepFunction( double delta )
	{
		stranded.stepFunction( delta );
	}
	
	public void setLocX( int xx )
	{
		locX = xx;
	}
	public void setLocY( int yy )
	{
		locY = yy;
	}
	
	
	public ObjStranded getStranded()
	{
		return stranded;
	}
	public int getLocX()
	{
		return stranded.getX();
	}
	public int getLocY()
	{
		return stranded.getY();
	}
	
	public boolean isSpawned()
	{
		return playerSpawned;
	}
	
}
