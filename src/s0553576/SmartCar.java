package s0553576;

import lenz.htw.ai4g.ai.*;
import static org.lwjgl.opengl.GL11.*;

public class SmartCar extends AI 
{
	
	private final float MAX_ACCELERATION;
	private final float MAX_VELOCITY;
	private final float MAX_ANGULAR_ACCELERATION;
	private final float MAX_ANGULAR_VELOCITY;
	
	private static final float TARGET_RADIUS = 1;
	private static final float BREAK_RADIUS = 20;
	private static final float BREAK_ANGLE = (float)Math.toRadians(30);
	
	public SmartCar(Info info) 
	{
		super(info);
		//this.enlistForDevelopment();
		this.enlistForTournament(553576, 554133);
		
		MAX_ACCELERATION = info.getMaxAcceleration();
		MAX_VELOCITY = info.getMaxVelocity();
		MAX_ANGULAR_ACCELERATION = info.getMaxAngularAcceleration();
		MAX_ANGULAR_VELOCITY = info.getMaxAngularVelocity();
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
		if(angle > Math.PI)
			angle = ((float)Math.PI * 2) - angle;
		if(angle < -Math.PI)
			angle = ((float)Math.PI * 2) + angle;
		
		float angularVel = 0;
		float angularAcc = 0;
		
		float wunschZeit = 1f;
		
		if(Math.abs(angle) < 0.01f)
		{
			return 0;
		}
		
		if(Math.abs(angle) <= BREAK_ANGLE)
		{
			float factor = MAX_ANGULAR_VELOCITY / BREAK_ANGLE;
			angularVel = angle * factor;
		}
		else
		{
			// preserve the direction of rotation by taking the sign of the angle
			angularVel = Math.signum(angle) * MAX_ANGULAR_VELOCITY;
		}
		
		angularAcc = (angularVel - currentAngularVel) / wunschZeit;
		
		return angularAcc;
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
