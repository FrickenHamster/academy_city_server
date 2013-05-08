package formulas;

/**
 * Created with IntelliJ IDEA.
 * User: Fricken Hamster
 * Date: 7/10/12
 * Time: 6:20 PM
 * To change this template use File | Settings | File Templates.
 */
public class Formula
{

	public Formula()
	{

	}
	
	public static double getDir(double x1 , double y1 , double x2 , double y2)
	{
		return ( Math.atan2( (y2 - y1),( x2 - x1 ) )* (180/Math.PI) );
	}
	
	public static double compareDist( double x1 , double y1 , double x2 , double y2 )
	{
		return ( Math.pow( y2 - y1 , 2) + Math.pow( x2 - x1 , 2) ) ;
	}
	
	public static double getDist( double x1 , double y1 , double x2 , double y2 )
	{
		return ( Math.sqrt(  Math.pow( y2 - y1 , 2) + Math.pow( x2 - x1 , 2) ) );
	}

	public static double dirToRad( double dir )
	{
		return ( dir * Math.PI / 180 );
	}
}
