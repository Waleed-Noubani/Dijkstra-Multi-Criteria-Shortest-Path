package application;

import java.util.ArrayList;
import java.util.List;

public class PathResult {
	private List<String> listPath;
	private double totalDistance;
	private double totalTime;
	private int optimizationType;

	public PathResult() {
		this.listPath = new ArrayList<String>(listPath);
		this.totalDistance = 0.0;
		this.totalTime = 0.0;
		this.optimizationType = 1;
	}

	public PathResult(List<String> listPath, double totalDistance, double totalTime, int optimizationType) {
		this.listPath = new ArrayList<String>(listPath);
		this.totalDistance = totalDistance;
		this.totalTime = totalTime;
		this.optimizationType = optimizationType;
	}

	public List<String> getPath() {
        return new ArrayList<>(listPath);
    }


	public double getTotalDistance() {
		return totalDistance;
	}

	public void setTotalDistance(double totalDistance) {
		this.totalDistance = totalDistance;
	}

	public double getTotalTime() {
		return totalTime;
	}

	public void setTotalTime(double totalTime) {
		this.totalTime = totalTime;
	}

	public int getOptimizationType() {
		return optimizationType;
	}

	public void setOptimizationType(int optimizationType) {
		this.optimizationType = optimizationType;
	}

	public boolean hasPath() {
		return listPath != null && !listPath.isEmpty();
	}

	@Override
	public String toString() {
		if (!hasPath()) {
			return "No path found!";
		}

		StringBuilder sb = new StringBuilder();
		sb.append("=== Path Result (").append(optimizationType).append(") ===\n");
		sb.append("Path: ");

		// طباعة المسار
		for (int i = 0; i < listPath.size(); i++) {
			sb.append(listPath.get(i));
			if (i < listPath.size() - 1) {
				sb.append(" → ");
			}
		}
		sb.append("\n");

		// طباعة المسافة والوقت
		sb.append(String.format("Total Distance: %.2f km\n", totalDistance));
		sb.append(String.format("Total Time: %.2f minutes\n", totalTime));

		return sb.toString();
	}

}
