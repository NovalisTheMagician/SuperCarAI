package s0554133;

import lenz.htw.ai4g.ai.AI;
import static org.lwjgl.opengl.GL11.*;

import org.lwjgl.util.vector.Vector2f;

import lenz.htw.ai4g.ai.DriverAction;
import lenz.htw.ai4g.ai.Info;

public class KICar extends AI {
	
	private static class Vector
	{
		public float x;
		public float y;
		
		public Vector(float x, float y)
		{
			this.x = x;
			this.y = y;
		}
		
		public Vector(Vector2f v)
		{
			this.x = v.x;
			this.y = v.y;
		}
		
		public float length()
		{
			return (float)Math.sqrt(x*x + y*y);
		}
		
		public float angle()
		{
			float angle = (float)Math.atan2(y, x);
			if(angle > Math.PI)
				angle -= Math.PI*2;
			
			return angle;
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
			return (float)Math.cos((Vector.dot(lhs, rhs)) / (lhs.length() * rhs.length()));
		}
	}

	public KICar(Info info) {
		super(info);
		enlistForDevelopment();
		//enlistForTournament();
	}

	@Override
	public String getName() {
		return "cat car";
	}

	@Override
	public DriverAction update (boolean wasResetAfterCollision) {
		Vector car = new Vector (info.getX(), info.getY());
		Vector target = new Vector (info.getCurrentCheckpoint().x, info.getCurrentCheckpoint().y);
		
		float targetradius =  1;
		float brakeradius = 5;
		Vector dissV = Vector.normalize(Vector.sub(target, car));
		float distance = Vector.sub(target, car).length();
		
		
		Vector speed;
		
		
		
		if (distance < targetradius) {
			return new DriverAction(0, 0);
		}
		
		if (distance < brakeradius) {
			speed = Vector.scale(Vector.scale(dissV, info.getMaxVelocity()), 1/brakeradius);
		}
		else {
			speed = Vector.scale(dissV, info.getMaxVelocity());
		}
		
		Vector curSpeed = new Vector(info.getVelocity());
		
		float throttle = Vector.scale(Vector.sub(speed, curSpeed), 1 / 1f).length();
		
		
		
		float tolerance = (float)Math.toRadians(1);
		float breakAngle = (float)Math.toRadians(7.5f);
		
		float curOri = info.getOrientation();
		float tarOri = Vector.sub(target, car).angle();
		float diff = tarOri - curOri;
		float reverseDiff = tarOri - curOri;
		
		float weeAngVel = 0;
		
		float steering = 0;
		
		if(diff > Math.PI)
			diff -= Math.PI * 2;
		if(diff < -Math.PI)
			diff += Math.PI * 2;
		
		if(reverseDiff > Math.PI)
			reverseDiff -= Math.PI * 2;
		if(reverseDiff < -Math.PI)
			reverseDiff += Math.PI * 2;
		
		if(Math.abs(diff) < tolerance) {
			return new DriverAction(throttle, 0);
		}
		
		if(Math.abs(diff) < breakAngle) {
			weeAngVel = - reverseDiff * info.getMaxAngularVelocity() / breakAngle; 
		}
		else
			weeAngVel = info.getMaxAngularVelocity();
		
		steering = (weeAngVel - info.getAngularVelocity()) / 1;
				
		
//		//seek beschleunigung
//		Vector acceleration = Vector.scale(Vector.normalize(Vector.sub(target, car)), info.getMaxAcceleration());
//		float throttle = acceleration.length();
//		
//		float brakeangle = (float) Math.toRadians(5);
//		float steering;
//		float tempAngVel;
//		float diff = acceleration.angle() - info.getOrientation();
//		
//		if (diff < brakeangle) {
//			tempAngVel = (acceleration.angle() - info.getOrientation()) * info.getMaxAngularAcceleration() / brakeangle;
//		}
//		else {
//			tempAngVel = (acceleration.angle() - info.getOrientation()) * info.getMaxAngularAcceleration();
//		}			
//			
//
//		steering = tempAngVel;
//		
////		if(steering < - Math.PI)
////			steering += Math.PI*2;
		
		return new DriverAction(throttle, steering);
	}
	

	
	
	@Override
	public String getTextureResourceName() {
		return "/s0554133/car.png";
	}

	
	@Override
	public void doDebugStuff() {
		
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
		
		//
		glBegin(GL_LINES);
		glColor3f(0, 0, 1);
		glVertex2f(posX, posY);
		glVertex2f(tarX, tarY);
		glEnd();
	}
}
