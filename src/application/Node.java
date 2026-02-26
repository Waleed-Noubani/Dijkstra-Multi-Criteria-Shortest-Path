package application;

public class Node  {
	private String name;
	private double cost;

	public Node(String name, double cost) {
		this.name = name;
		this.cost = cost;
	}

	public String getName() {
		return name;
	}

	public double getCost() {
		return cost;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setCost(double cost) {
		this.cost = cost;
	}

	@Override
	public String toString() {
		return "Node [" + name + ", cost=" + cost + "]";
	}
}