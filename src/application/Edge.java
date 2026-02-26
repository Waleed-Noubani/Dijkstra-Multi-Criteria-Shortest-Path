package application;

public class Edge {
	private String destination;
	private double distance;
	private double time;

	public Edge(String destination, double distance, double time) {
		this.destination = destination;
		this.distance = distance;
		this.time = time;

	}

	public String getDestination() {
		return destination;
	}

	public void setDestination(String destination) {
		this.destination = destination;
	}

	public double getDistance() {
		return distance;
	}

	public void setDistance(double distance) {
		this.distance = distance;
	}

	public double getTime() {
		return time;
	}

	public void setTime(double time) {
		this.time = time;
	}

	@Override
	public String toString() {
		return "Edge [destination=" + destination + ", distance=" + distance + ", time=" + time + "]";
	}

}
