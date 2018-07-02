package s0553576;

import lenz.htw.ai4g.ai.*;
import static org.lwjgl.opengl.GL11.*;

import java.awt.Polygon;

public class PathCar extends AI 
{
	private final float MAX_VELOCITY;
	private final float MAX_ANGULAR_VELOCITY;
	
	private static final float TARGET_RADIUS = 1;
	private static final float BREAK_RADIUS = 30;
	private static final float BREAK_ANGLE = (float)Math.toRadians(15);
	
	private static final int DIVISIONS = 30;
	
	private AStar astar;
	
	private Vector[] path;
	private int currentNode;
	
	public PathCar(Info info) 
	{
		super(info);
		this.enlistForTournament(553576, 554133);
		
		int trackWidth = info.getTrack().getWidth();
		int trackHeight = info.getTrack().getHeight();
		
		Polygon[] obstacles = info.getTrack().getObstacles();
		Polygon[] boosts = info.getTrack().getFastZones();
		Polygon[] slows = info.getTrack().getSlowZones();
		
		astar = new AStar();
		astar.createGraph(obstacles, boosts, slows, DIVISIONS, trackWidth, trackHeight);
		
		MAX_VELOCITY = info.getMaxVelocity();
		MAX_ANGULAR_VELOCITY = info.getMaxAngularVelocity();
		
		currentNode = 0;
	}

	@Override
	public String getName() 
	{
		return "Fraylin Boons";
	}

	boolean calcPath = true;
	Vector lastGoal = null;
	boolean goToGoal = false;
	
	@Override
	public DriverAction update(boolean wasResetAfterCollision) 
	{
		Vector carPosition = new Vector(info.getX(), info.getY());
		float carOrientation = info.getOrientation();
		
		Vector goalPosition = new Vector(info.getCurrentCheckpoint().x, info.getCurrentCheckpoint().y);
		if(!goalPosition.equals(lastGoal))
			calcPath = true;
		lastGoal = goalPosition;

		if(wasResetAfterCollision || calcPath)
		{
			path = astar.getPath(carPosition, goalPosition);
			calcPath = false;
			goToGoal = false;
			currentNode = 0;
		}
		
		if(!goToGoal)
		{
			float dist = Vector.sub(carPosition, path[currentNode]).length();
			if(dist <= astar.getCellWidth())
				currentNode = (currentNode + 1);
		}
		
		Vector targetPosition = null;
		if(currentNode == path.length)
		{
			targetPosition = goalPosition;
			goToGoal = true;
		}
		else
			targetPosition = path[currentNode];
		
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
		
		drawGLCircle(new Vector(posX, posY), 10, 0, 1, 1);
		
		float cellWidth = astar.getCellWidth();
		float cellHeight = astar.getCellHeight();
		
		int[][] graph = astar.getGraphArray();
		
		
		//glBegin(GL_LINES);
		for(int y = 0; y < DIVISIONS; ++y)
		{
			for(int x = 0; x < DIVISIONS; ++x)
			{
				float xPos = x * cellWidth;
				float yPos = y * cellHeight;
				
				if(graph[x][y] == AStar.OBSTACLE_COST)
				{
					glBegin(GL_TRIANGLES);
					glColor3f(1, 0, 0);
					glVertex2f(xPos, yPos);
					glVertex2f(xPos + cellWidth, yPos);
					glVertex2f(xPos, yPos + cellHeight);
					
					glVertex2f(xPos + cellWidth, yPos + cellHeight);
					glVertex2f(xPos, yPos + cellHeight);
					glVertex2f(xPos + cellWidth, yPos);
					glEnd();
				}
				else if(graph[x][y] == AStar.BOOST_COST)
				{
					glBegin(GL_TRIANGLES);
					glColor3f(0, 0, 1);
					glVertex2f(xPos, yPos);
					glVertex2f(xPos + cellWidth, yPos);
					glVertex2f(xPos, yPos + cellHeight);
					
					glVertex2f(xPos + cellWidth, yPos + cellHeight);
					glVertex2f(xPos, yPos + cellHeight);
					glVertex2f(xPos + cellWidth, yPos);
					glEnd();
				}
				else if(graph[x][y] == AStar.SLOW_COST)
				{
					glBegin(GL_TRIANGLES);
					glColor3f(1, 0.3f, 0.3f);
					glVertex2f(xPos, yPos);
					glVertex2f(xPos + cellWidth, yPos);
					glVertex2f(xPos, yPos + cellHeight);
					
					glVertex2f(xPos + cellWidth, yPos + cellHeight);
					glVertex2f(xPos, yPos + cellHeight);
					glVertex2f(xPos + cellWidth, yPos);
					glEnd();
				}
				
				glBegin(GL_LINES);
				glColor3f(0.5f, 1f, 0.5f);
				glVertex2f(xPos, yPos);
				glVertex2f(xPos + cellWidth, yPos);
				glVertex2f(xPos, yPos);
				glVertex2f(xPos, yPos + cellHeight);
				glEnd();
			}
		}
		//glEnd();
		
		glBegin(GL_LINES);
		glColor3f(0.5f, 0, 0.5f);
		for(int i = 0; i < path.length-1; ++i)
		{
			Vector a = path[i];
			Vector b = path[(i + 1)];
			glVertex2f(a.x, a.y);
			glVertex2f(b.x, b.y);
		}
		glEnd();
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
