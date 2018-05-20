package s0553576;

import lenz.htw.ai4g.ai.*;
import static org.lwjgl.opengl.GL11.*;

import java.awt.Polygon;

public class SmartCar extends AI 
{
	
	private final float MAX_ACCELERATION;
	private final float MAX_VELOCITY;
	private final float MAX_ANGULAR_ACCELERATION;
	private final float MAX_ANGULAR_VELOCITY;
	
	private static final float TARGET_RADIUS = 1;
	private static final float BREAK_RADIUS = 20;
	private static final float BREAK_ANGLE = (float)Math.toRadians(15);
	
	private Vector[] obstaclesCenter;
	private float[] obstaclesRadius;
	
	public SmartCar(Info info) 
	{
		super(info);
		//this.enlistForDevelopment();
		this.enlistForTournament(553576, 554133);
		
		MAX_ACCELERATION = info.getMaxAcceleration();
		MAX_VELOCITY = info.getMaxVelocity();
		MAX_ANGULAR_ACCELERATION = info.getMaxAngularAcceleration();
		MAX_ANGULAR_VELOCITY = info.getMaxAngularVelocity();
		
		Polygon[] obstacles = info.getTrack().getObstacles();
		int numObstacles = obstacles.length;
		
		obstaclesCenter = new Vector[numObstacles];
		obstaclesRadius = new float[numObstacles];
		for(int i = 0; i < numObstacles; ++i)
		{
			Vector[] points = createPointList(obstacles[i]);
			obstaclesCenter[i] = getObstacleCenter(points);
			obstaclesRadius[i] = getObstacleRadius(points);
		}
		
	}

	@Override
	public String getName() 
	{
		return "Fraylin Boons";
	}

	@Override
	public DriverAction update(boolean wasResetAfterCollision) 
	{
		Vector carPosition = new Vector(info.getX(), info.getY());
		float carOrientation = info.getOrientation();
		
		Vector targetPosition = new Vector(info.getCurrentCheckpoint().x, info.getCurrentCheckpoint().y);
		float targetOrientation = Vector.sub(targetPosition, carPosition).angle();
		
		float currentVelocity = new Vector(info.getVelocity()).length();
		float acceleration = arrive(carPosition, targetPosition, currentVelocity);
		
		float angAcceleration = align(carOrientation, targetOrientation, info.getAngularVelocity());
		
		return new DriverAction(acceleration, angAcceleration);
	}
	
	private float arrive(Vector source, Vector target, float currentVel)
	{
		float distance = Vector.sub(source, target).length();
		float velocity = 0;
		float acceleration = 0;
		
		float wunschZeit = 1;
		
		if(distance <= TARGET_RADIUS)
		{
			//already on target
			return 0;
		}
		
		if(distance <= BREAK_RADIUS)
		{
			float factor = MAX_VELOCITY / BREAK_RADIUS;
			float len = Vector.sub(target, source).length();
			velocity = len * factor;
		}
		else
		{
			velocity = MAX_VELOCITY;
		}
		
		acceleration = (velocity - currentVel) / wunschZeit;
		
		return acceleration;
	}
	
	private float align(float sourceOri, float targetOri, float currentAngularVel)
	{
		float angle = targetOri - sourceOri;
		angle = mapRotation(angle);
		float absAng = Math.abs(angle);
		
		float angularVel = 0;
		float angularAcc = 0;
		
		float wunschZeit = 1f;
		
		if(absAng < Math.toRadians(1))
		{
			return 0;
		}
		
		if(absAng <= BREAK_ANGLE)
		{
			float factor = MAX_ANGULAR_VELOCITY / BREAK_ANGLE;
			angularVel = absAng * factor;
		}
		else
		{
			angularVel = MAX_ANGULAR_VELOCITY;
		}
		
		angularVel *= angle / absAng; 
		
		angularAcc = (angularVel - currentAngularVel) / wunschZeit;
		
		return angularAcc;
	}
	
	private Vector getObstacleCenter(Vector[] points)
	{
		Vector center = Vector.ZERO;
		for(int i = 0; i < points.length; ++i)
		{
			center = Vector.add(center, points[i]);
		}
		return Vector.scale(center, 1.0f / points.length);
	}
	
	private float getObstacleRadius(Vector[] points)
	{
		Vector min = new Vector(Float.MAX_VALUE, Float.MAX_VALUE);
		Vector max = new Vector(Float.MIN_VALUE, Float.MIN_VALUE);
		for(int i = 0; i < points.length; ++i)
		{
			Vector point = points[i];
			if(point.x < min.x)
				min.x = point.x;
			else if(point.x > max.x)
				max.x = point.x;
			
			if(point.y < min.y)
				min.y = point.y;
			else if(point.y > max.y)
				max.y = point.y;
		}
		
		Vector diagonal = Vector.sub(min, max);
		return diagonal.length() / 2.0f;
	}
	
	private Vector[] createPointList(Polygon poly)
	{
		Vector[] points = new Vector[poly.npoints];
		for(int i = 0; i < poly.npoints; ++i)
		{
			points[i] = new Vector(poly.xpoints[i], poly.ypoints[i]);
		}
		return points;
	}
	
	private float mapRotation(float rot)
	{
		return Vector.constrainAngle(rot);
	}
	
	@Override
	public String getTextureResourceName()
	{
		return "/s0553576/car.png";
	}
	
	@Override
	public void doDebugStuff() 
	{
		float posX = info.getX();
		float posY = info.getY();
		
		float velX = info.getVelocity().x;
		float velY = info.getVelocity().y;
		
		float oriX = (float)Math.cos(info.getOrientation());
		float oriY = (float)Math.sin(info.getOrientation());
		
		float tarX = info.getCurrentCheckpoint().x;
		float tarY = info.getCurrentCheckpoint().y;
		
		glLineWidth(2.5f);
		
		glBegin(GL_LINES);
		glColor3f(1, 0, 0);
		glVertex2f(posX, posY);
		glVertex2f(posX + 20 * oriX, posY + 20 * oriY);
		glEnd();
		
		glBegin(GL_LINES);
		glColor3f(0, 1, 0);
		glVertex2f(posX, posY);
		glVertex2f(posX + velX, posY + velY);
		glEnd();
		
		glBegin(GL_LINES);
		glColor3f(0, 0, 1);
		glVertex2f(posX, posY);
		glVertex2f(tarX, tarY);
		glEnd();
	}
}
