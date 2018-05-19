package s0553576;

import org.lwjgl.util.vector.Vector2f;

public class Vector
{
	public static final Vector ZERO = new Vector(0, 0);
	
	public float x;
	public float y;
	
	public Vector(float x, float y)
	{
		this.x = x;
		this.y = y;
	}
	
	public Vector(Vector2f vector)
	{
		x = vector.x;
		y = vector.y;
	}
	
	public float length()
	{
		return (float)Math.sqrt(x*x + y*y);
	}
	
	public float angle()
	{
		return constrainAngle((float)Math.atan2(y, x));
	}
	
	public static Vector add(Vector lhs, Vector rhs)
	{
		return new Vector(lhs.x + rhs.x, lhs.y + rhs.y);
	}
	
	public static Vector sub(Vector lhs, Vector rhs)
	{
		return new Vector(lhs.x - rhs.x, lhs.y - rhs.y);
	}
	
	public static Vector scale(Vector vector, float factor)
	{
		return new Vector(vector.x * factor, vector.y * factor);
	}
	
	public static Vector normalize(Vector vector)
	{
		float l = vector.length();
		return new Vector(vector.x / l, vector.y / l);
	}
	
	public static float dot(Vector lhs, Vector rhs)
	{
		return lhs.x*rhs.x + lhs.y*rhs.y;
	}
	
	public static float angle(Vector lhs, Vector rhs)
	{
		float angle = (float)Math.cos((Vector.dot(lhs, rhs)) / (lhs.length() * rhs.length()));
		return constrainAngle(angle);
	}
	
	public static float signedAngle(Vector lhs, Vector rhs)
	{
		//signed_angle = atan2(b.y,b.x) - atan2(a.y,a.x)
		float angle = (float)Math.atan2(rhs.y, rhs.x) - (float)Math.atan2(lhs.y, lhs.x);
		return constrainAngle(angle);
	}
	
	public static float constrainAngle(float angle)
    {
        while (angle >= Math.PI)
            angle -= (float)Math.PI * 2;
        while (angle <= -Math.PI)
            angle += (float)Math.PI * 2;

        return angle;
    }
}
