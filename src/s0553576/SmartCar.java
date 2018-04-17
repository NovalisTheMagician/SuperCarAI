package s0553576;

import lenz.htw.ai4g.ai.*;
import static org.lwjgl.opengl.GL11.*;

public class SmartCar extends AI {

	private static class Vector
	{
		public float x;
		public float y;
		
		public Vector(float x, float y)
		{
			this.x = x;
			this.y = y;
		}
		
		public float length()
		{
			return (float)Math.sqrt(x*x + y*y);
		}
		
		public float angle()
		{
			return (float)Math.atan2(y, x);
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
	
	public SmartCar(Info info) {
		super(info);
		//this.enlistForDevelopment();
		this.enlistForTournament(553576, 554133);
	}

	@Override
	public String getName() {
		return "Fraylin Boons";
	}

	@Override
	public DriverAction update(boolean wasResetAfterCollision) {
		Vector pos = new Vector(info.getX(), info.getY());
		
		float curOrient = info.getOrientation();
		
		Vector tar = new Vector(info.getCurrentCheckpoint().x, info.getCurrentCheckpoint().y);
		
		Vector dir = Vector.normalize(Vector.sub(tar, pos));
		
		float tarOrient = dir.angle();
		
		if(tarOrient > Math.PI)
			tarOrient -= (float)Math.PI * 2;
		
		float orient = (tarOrient - curOrient);
		
		if(Math.abs(orient) < 0.001f)
			orient = 0;
		
		return new DriverAction(dir.length(), orient);
	}
	
	@Override
	public String getTextureResourceName() {
		return "/s0553576/car.png";
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
		
		glBegin(GL_LINES);
		glColor3f(0, 0, 1);
		glVertex2f(posX, posY);
		glVertex2f(tarX, tarY);
		glEnd();
	}
}
