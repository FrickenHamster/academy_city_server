package gameobj;

import aserver.Main;
import formulas.Formula;

/**
 * Created with IntelliJ IDEA.
 * User: Fricken Hamster
 * Date: 7/10/12
 * Time: 5:06 PM
 * To change this template use File | Settings | File Templates.
 */
public class ObjStranded
{
	
	private int moveSpeed;
	private double moveXSpeed;
	private double moveYSpeed;
	private int targetMoveX;
	private int targetMoveY;
	private double rotation;
	
	private int state;
	private boolean spawned;
	private double x;
	private double y;
	
	private static final int NOTSPAWNED = -1;
	private static final int IDLE = 0;
	private static final int MOVING = 1;
	
	public ObjStranded(  )
	{
		moveSpeed = 6;
		
		x = 0;
		y = 0;
		moveXSpeed = 0;
		moveYSpeed = 0;
		targetMoveX = 0;
		targetMoveY = 0;
		rotation = 0;
		
		state = NOTSPAWNED;
		
	}
	
	public void spawn( int sx , int sy )
	{
		x = sx;
		y = sy;
		state = IDLE;
	}
	
	public void moveTarget( int tx , int ty )
	{
		state = MOVING;
		targetMoveX = tx;
		targetMoveY = ty;
		double dd = Formula.getDir(x ,y , tx , ty );
		rotation = dd;
		double currad = Formula.dirToRad( dd );
		moveXSpeed = Math.cos( currad ) * moveSpeed;
		moveYSpeed = Math.sin( currad ) * moveSpeed;
	}
	
	
	public void stepFunction( double delta )
	{
		if ( state == MOVING )
		{
			//consoleText("far:" +Formula.compareDist( x , y , targetMoveX , targetMoveY ) + ":" + moveXSpeed +"," + moveYSpeed);
			if (Formula.compareDist( x , y , targetMoveX , targetMoveY ) < ( moveSpeed * moveSpeed ) )
			{
				x = targetMoveX;
				y = targetMoveY;
				state = IDLE;
			}
			else
			{
				x += moveXSpeed;
				y += moveYSpeed;
			}
		}
	}
	
	public void setPosition( int xx , int yy )
	{
		x = xx;
		y = yy;
	}
	public int getX()
	{
		return (int)x;
	}
	public int getY()
	{
		return (int)y;
	}
	
	private void consoleText( String msg )
	{
		Main.consoleText("ObjStranded" , msg );
	}

	public String removeLetters(String ss)
	{
		String newString = new String();
		int ilen = ss.length();
		for (int i = 0; i < ilen; i++)
		{
			char cc = ss.charAt(i);
			if (Character.isLetter(cc)) // or check if the value is between 65 and 90 or 97 and 122
			{
				newString += cc;
			}
		}
		return newString;
	}

}
