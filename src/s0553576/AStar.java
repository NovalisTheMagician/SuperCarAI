package s0553576;

import java.awt.Polygon;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class AStar 
{
	public static final int EMPTY_COST = 20;
	public static final int OBSTACLE_COST = Integer.MAX_VALUE;
	public static final int BOOST_COST = 10;
	public static final int SLOW_COST = 50;
	
	private int[][] graphArray;
	private int numDivisions;
	private float cellWidth;
	private float cellHeight;
	
	public AStar()
	{
		
	}
	
	public void createGraph(Polygon[] obstacles, Polygon[] boosts, Polygon[] slows, int divisions, float levelWidth, float levelHeight)
	{
		numDivisions = divisions;
		
		graphArray = new int[numDivisions][];
		for(int i = 0; i < numDivisions; ++i)
		{
			graphArray[i] = new int[numDivisions];
			Arrays.fill(graphArray[i], EMPTY_COST);
		}
		
		cellWidth = levelWidth / numDivisions;
		cellHeight = levelHeight / numDivisions;
		
		checkObstacles(obstacles, boosts, slows);
	}
	
	public int[][] getGraphArray()
	{
		return graphArray;
	}
	
	public float getCellWidth()
	{
		return cellWidth;
	}
	
	public float getCellHeight()
	{
		return cellHeight;
	}
	
	public Vector[] getPath(Vector from, Vector to)
	{
		Node start = new Node((int)(from.x / cellWidth), (int)(from.y / cellHeight));
		Node end = new Node((int)(to.x / cellWidth), (int)(to.y / cellHeight));
		
		Set<Node> closedSet = new HashSet<>();
		Set<Node> openSet = new HashSet<>();
		
		openSet.add(start);
		
		Map<Node, Node> cameFrom = new HashMap<>();
		
		Map<Node, Float> gScore = new HashMap<>();
		gScore.put(start, 0.0f);
		
		Map<Node, Float> fScore = new HashMap<>();
		fScore.put(start, getHeuristic(start, end));
		
		Node current = null;
		
		while(!openSet.isEmpty())
		{
			current = getLowestScore(openSet, fScore);
			if(current.equals(end))
				break;
			
			openSet.remove(current);
			closedSet.add(current);
			
			List<Node> neighbors = getNeighbors(current);
			for(Node neighbor : neighbors)
			{
				if(closedSet.contains(neighbor))
				{
					continue;
				}
				
				if(!openSet.contains(neighbor))
				{
					openSet.add(neighbor);
				}
				
				float tentativeGScore = gScore.get(current) + getCostOf(neighbor);
				if(tentativeGScore >= gScore.getOrDefault(neighbor, Float.POSITIVE_INFINITY))
					continue;
					
				cameFrom.put(neighbor, current);
				gScore.put(neighbor, tentativeGScore);
				fScore.put(neighbor, gScore.get(neighbor) + getHeuristic(neighbor, end));
			}
		}
		
		return smoothPath(reconstructPath(cameFrom, current));
	}
	
	private float getCostOf(Node n)
	{
		return graphArray[n.x][n.y];
	}
	
	private Node getLowestScore(Set<Node> nodeSet, Map<Node, Float> scoreMap)
	{
		Node n = null;
		float lowestScore = Float.POSITIVE_INFINITY;
		for(Node node : nodeSet)
		{
			float score = scoreMap.getOrDefault(node, Float.POSITIVE_INFINITY);
			if(score < lowestScore)
			{
				n = node;
				lowestScore = score;
			}
		}
		
		return n;
	}
	
	private List<Node> getNeighbors(Node current)
	{
		int x = current.x;
		int y = current.y;
		
		List<Node> connections = new ArrayList<>();
		
		if(x+1 < numDivisions)
			connections.add(new Node(x+1, y));
		if(x-1 >= 0)
			connections.add(new Node(x-1, y));
		if(y+1 < numDivisions)
			connections.add(new Node(x, y+1));
		if(y-1 >= 0)
			connections.add(new Node(x, y-1));
		
		return connections;
	}
	
	private Node[] reconstructPath(Map<Node, Node> cameFrom, Node current)
	{
		ArrayList<Node> totalPath = new ArrayList<>();
		totalPath.add(current);
		while(cameFrom.containsKey(current))
		{
			current = cameFrom.get(current);
			totalPath.add(current);
		}
		Node[] nodeArray = new Node[totalPath.size()];
		
		return totalPath.toArray(nodeArray);
	}
	
	private Vector[] smoothPath(Node[] path)
	{
		Vector[] vecPath = new Vector[path.length];
		for(int i = 0; i < path.length; ++i)
		{
			float x = path[i].x * cellWidth + (cellWidth / 2.0f);
			float y = path[i].y * cellHeight + (cellHeight / 2.0f);
			vecPath[i] = new Vector(x, y);
		}
		reverse(vecPath);
		return vecPath;
	}
	
	private void reverse(Vector[] array)
	{
		for(int i = 0; i < array.length / 2; i++)
		{
		    Vector temp = array[i];
		    array[i] = array[array.length - i - 1];
		    array[array.length - i - 1] = temp;
		}
	}
	
	private float getHeuristic(Node from, Node to)
	{
		int x = to.x - from.x;
		int y = to.y - from.y;
		return (float)Math.sqrt(x*x+y*y);
	}
	
	private void checkObstacles(Polygon[] obstacles, Polygon[] boosts, Polygon[] slows) {
		for (int y = 0; y < numDivisions; y++) {
			for (int x = 0; x < numDivisions; x++) {
				
				float xPos = x * cellWidth;
				float yPos = y * cellHeight;
				
				Rectangle2D rectangle = new Rectangle2D.Float(xPos, yPos, cellWidth, cellHeight);
				for (int i = 0; i < obstacles.length; i++) {
					if (obstacles[i].intersects(rectangle)) {
						graphArray[x][y] = OBSTACLE_COST;
						break;
					}
				}
				
				for (int i = 0; i < boosts.length; i++) {
					if (boosts[i].intersects(rectangle)) {
						graphArray[x][y] = BOOST_COST;
						break;
					}
				}
				
				for (int i = 0; i < slows.length; i++) {
					if (slows[i].intersects(rectangle)) {
						graphArray[x][y] = SLOW_COST;
						break;
					}
				}
			}
		}
	}
}
