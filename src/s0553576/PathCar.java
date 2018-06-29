package s0553576;

import lenz.htw.ai4g.ai.*;
import static org.lwjgl.opengl.GL11.*;

public class PathCar extends AI 
{
	
	private final float MAX_ACCELERATION;
	private final float MAX_VELOCITY;
	private final float MAX_ANGULAR_ACCELERATION;
	private final float MAX_ANGULAR_VELOCITY;
	
	private static final float TARGET_RADIUS = 1;
	private static final float BREAK_RADIUS = 30;
	private static final float BREAK_ANGLE = (float)Math.toRadians(15);
	
	private int trackWidth;
	private int trackHeight;
	
	private Vector[] path;
	private int currentNode;
	
	public PathCar(Info info) 
	{
		super(info);
		this.enlistForTournament(553576, 554133);
		
		trackWidth = info.getTrack().getWidth();
		trackHeight = info.getTrack().getHeight();
		
		MAX_ACCELERATION = info.getMaxAcceleration();
		MAX_VELOCITY = info.getMaxVelocity();
		MAX_ANGULAR_ACCELERATION = info.getMaxAngularAcceleration();
		MAX_ANGULAR_VELOCITY = info.getMaxAngularVelocity();
		
		path = new Vector[6];
		
		path[0] = new Vector(700, 700);
		path[1] = new Vector(700, 300);
		path[2] = new Vector(500, 500);
		path[3] = new Vector(300, 300);
		path[4] = new Vector(300, 700);
		path[5] = new Vector(500, 500);
		
		currentNode = 0;
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
		
		float dist = Vector.sub(carPosition, path[currentNode]).length();
		if(dist <= 10)
			currentNode = (currentNode + 1) % path.length;
		
		//Vector targetPosition = new Vector(info.getCurrentCheckpoint().x, info.getCurrentCheckpoint().y);
		Vector targetPosition = path[currentNode];
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
		
		Vector dir = Vector.normalize(new Vector(info.getVelocity()));
		
		Vector perpDir = Vector.perp(dir);
		
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
		
		glBegin(GL_LINES);
		glColor3f(1, 1, 0);
		glVertex2f(0, 0);
		glColor3f(1, 0, 0);
		glVertex2f(trackWidth, trackHeight);
		glEnd();
		
		glBegin(GL_LINES);
		glColor3f(0.5f, 0, 0.5f);
		for(int i = 0; i < path.length; ++i)
		{
			Vector a = path[i];
			Vector b = path[(i + 1) % path.length];
			glVertex2f(a.x, a.y);
			glVertex2f(b.x, b.y);
		}
		glEnd();
		
		drawGLCircle(new Vector(posX, posY), 10, 0, 1, 1);
	}
	
	private void drawGLCircle(Vector position, float radius, float r, float g, float b)
	{
		glBegin(GL_LINE_LOOP);
		glColor3f(r, g, b);
		for (int i = 0; i < 360; i++)
		{
			float degInRad = (float)Math.toRadians(i);
			glVertex2f(position.x + (float)Math.cos(degInRad) * radius, position.y + (float)Math.sin(degInRad) * radius);
		}
		glEnd();
	}
}
