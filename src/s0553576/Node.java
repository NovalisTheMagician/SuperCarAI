package s0553576;

public class Node 
{
	public int x;
	public int y;
	
	public Node(int x, int y)
	{
		this.x = x;
		this.y = y;
	}
	
	@Override
	public int hashCode()
	{
		return x * 1000 + y;
	}
	
	@Override
	public boolean equals(Object other)
	{
		if(other == null)
			return false;
		
		Node otherNode = (Node)other;
		return this.x == otherNode.x && this.y == otherNode.y;
	}
}
