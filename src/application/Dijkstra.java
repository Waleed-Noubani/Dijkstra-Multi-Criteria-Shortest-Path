package application;

import java.util.Collections;
import java.util.List;
import java.util.ArrayList;

public class Dijkstra {
	private PriorityQueue priorityQueue; // يخزن (node, cost) وأقل cost يطلع أول.

	private double[] cost;
	private int[] previous; // previous[i] = index العقدة اللي جينا منها للعقدة i (عشان نرجع المسار).
	private Edge[] prevEdge; // عشان نجمع distance/time لاحقًا .edge اللي جينا منها
	private boolean[] visited;

	private Graph graph;
	private int numNodes;

	public Dijkstra() {
		this.priorityQueue = new PriorityQueue();
	}

	public Object findShortestPath(Graph graph, String source, String destination, int type) {
		this.graph = graph;
		this.numNodes = graph.getNumNodes();

		this.cost = new double[numNodes];
		this.previous = new int[numNodes];
		this.prevEdge = new Edge[numNodes];
		this.visited = new boolean[numNodes];

		if (type == 3) {
			PathResult byDistance = findPathDijxtra(source, destination, 1);
			PathResult byTime = findPathDijxtra(source, destination, 2);
			return new TwoResults(byDistance, byTime);
		}

		if (type != 1 && type != 2) {
			throw new IllegalArgumentException("type must be 1, 2, or 3");
		}

		return findPathDijxtra(source, destination, type);
	}

	// O((V + E) log V)
	private PathResult findPathDijxtra(String source, String destination, int optType) {

		int sourceIndex = graph.getNodeIndex(source);
		int destIndex = graph.getNodeIndex(destination);

		if (sourceIndex == -1 || destIndex == -1) {
			return new PathResult(new ArrayList<>(), Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, optType);
		}

		initialize(sourceIndex);

		while (!priorityQueue.isEmpty()) {
			Node current = priorityQueue.poll();  // أقل cost node .
			String currName = current.getName();
			int currIndex = graph.getNodeIndex(currName); 

			if (current.getCost() > cost[currIndex]) {    // PriorityQueue قد تحتوي نسخ قديمة من نفس العقدة هذا الشرط يتجاهل أي نسخة كلفتها أكبر من الكلفة الحالية المسجلة.
				continue;
			}

			if (visited[currIndex]) {
				continue;
			}
			visited[currIndex] = true;

			if (currIndex == destIndex) {
				break;
			}

			LinkedList<Edge> adjecants = graph.getAdjecant(currName); 

			LinkedList<Edge>.Iterator<Edge> iterator = adjecants.iterator();
			while (iterator.hasNext()) {
				Edge edge = iterator.next();

				String adjName = edge.getDestination();
				int adjIndex = graph.getNodeIndex(adjName); 

				if (adjIndex == -1 || visited[adjIndex]) {
					continue;
				}

				double newCost = cost[currIndex] + getWeight(edge, optType);

				if (newCost < cost[adjIndex]) {
					cost[adjIndex] = newCost;
					previous[adjIndex] = currIndex;
					prevEdge[adjIndex] = edge;
					priorityQueue.add(new Node(adjName, newCost));
				}
			}
		}

		PathResult result = buildResult(destIndex, optType);
		clear();

		return result;
	}

	private void initialize(int sourceIndex) {
		for (int i = 0; i < numNodes; i++) {
			cost[i] = Double.MAX_VALUE;
			previous[i] = -1;
			prevEdge[i] = null;
			visited[i] = false;
		}
		cost[sourceIndex] = 0.0; 
		priorityQueue.add(new Node(graph.getNodeName(sourceIndex), 0.0));   // ابدأ بالـ PQ: (source, 0)
	}

	private double getWeight(Edge edge, int optType) {
		return (optType == 1) ? edge.getDistance() : edge.getTime();
	}

	private PathResult buildResult(int destIndex, int optType) {
		List<String> path = reconstructPath(destIndex);

		if (path.isEmpty()) {
			return new PathResult(path, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, optType);
		}

		double totalDistance = 0.0;
		double totalTime = 0.0;

		for (int i = 1; i < path.size(); i++) {
			int nodeIndex = graph.getNodeIndex(path.get(i)); 
		    if (nodeIndex == -1) continue;    
			Edge e = prevEdge[nodeIndex];
			if (e != null) {
				totalDistance += e.getDistance();
				totalTime += e.getTime();
			}
		}

		return new PathResult(path, totalDistance, totalTime, optType);
	}

	private List<String> reconstructPath(int destIndex) { // تتبع الباث رجوع بعده بعكسه
		List<String> path = new ArrayList<>();

		if (cost[destIndex] == Double.MAX_VALUE) {
			return path;
		}

		int current = destIndex;
		while (current != -1) {
			path.add(graph.getNodeName(current));
			current = previous[current];
		}

		Collections.reverse(path);
		return path;
	}

	private void clear() {
		priorityQueue.clear();
	}

	public static class TwoResults {
		private final PathResult byDistance;
		private final PathResult byTime;

		public TwoResults(PathResult byDistance, PathResult byTime) {
			this.byDistance = byDistance;
			this.byTime = byTime;
		}

		public PathResult getByDistance() {
			return byDistance;
		}

		public PathResult getByTime() {
			return byTime;
		}
	}
}

//package application;
//
//import java.util.*;
//
//public class Dijkstra {
//	private PriorityQueue priorityQueue;
//
//	// أقل كلفة تم الوصول لها لكل عقدة
//	private Map<String, Double> cost; // مصفوفة تراكمية// cost.get("B") = أقل كلفة وصلنالها لحد الآن للعقدة B
//	private Map<String, String> previous; // هذا عشان نبني المسار بالأخير //previous.get("B") = "A" : يعني: أفضل طريقة
//											// وصلنا فيها لـ B كانت من A.
//	private Map<String, Edge> prevEdge; // هذا عشان نحسب totalDistance و totalTime للمسار النهائي بسهولة//
//										// prevEdge.get("B") = الـ edge اللي استخدمناها للوصول إلى B.
//	private Set<String> visited; // العقدة لما نطلّعها كـأقل كلفة مؤكدة ونخلص منها، ما بدنا نرجع نعالجها. // boolean
//	private Graph graph;
//
//	public Dijkstra() {
//		this.priorityQueue = new PriorityQueue();
//		this.cost = new HashMap<>();
//		this.previous = new HashMap<>();
//		this.prevEdge = new HashMap<>();
//		this.visited = new HashSet<>();
//	}
//
//	// 1 optimize by distance || 2 optimize by time || 3 run both
//	public Object findShortestPath(Graph graph, String source, String destination, int optimizationType) {
//		this.graph = graph;
//
//		if (optimizationType == 3) {
//			PathResult byDistance = findPath(source, destination, 1);
//			PathResult byTime = findPath(source, destination, 2);
//			return new TwoResults(byDistance, byTime);
//		}
//
//		if (optimizationType != 1 && optimizationType != 2) {
//			throw new IllegalArgumentException("optimizationType must be 1, 2, or 3");
//		}
//
//		return findPath(source, destination, optimizationType);
//	}
//
//	///////////////////// خوارزمية Dijkstra /////////////////////////////
//	private PathResult findPath(String source, String destination, int optType) {
//
//		initialize(source); // أول شيء: يجهز القيم (كل المسافات infinity، والمصدر 0).
//
//		// 2) الحلقة الرئيسية
//		while (!priorityQueue.isEmpty()) {
//
//			Node current = priorityQueue.poll(); // طلّع أصغر cost node
//			String currVertix = current.getName();
//
//			if (current.getCost() > cost.get(currVertix)) { // إذا طلعت من الهيب نسخة لعقدة u وكلفتها أكبر من أفضل كلفة مسجلة، فهي نسخة قديمة… تجاهلها.
//				continue;
//			}
//
//			if (visited.contains(currVertix)) {
//				continue;
//			}
//			visited.add(currVertix);
//
//			if (currVertix.equals(destination)) {
//				break;   // إذا وصلنا الهدف نوقف
//			}
//
//			// 3) update adjecant   ويحدّث المسافاات
//			for (Edge edge : graph.getAdjecant(currVertix)) {     
//				String AdjVertex = edge.getDestination();               // v هو اسم العقدة الجار،  E, A , B
//
//				if (visited.contains(AdjVertex))
//					continue;
//
//				double newCost = cost.get(currVertix) + getWeight(edge, optType); //الكلفة الجديدة = الكلفة اللي وصلنا فيها +وزن الطريق
//
//				if (newCost < cost.get(AdjVertex)) { 	  // إذا وجدنا مسار أفضل
//					cost.put(AdjVertex, newCost);
//					previous.put(AdjVertex, currVertix);   // من وين جينا
//					prevEdge.put(AdjVertex, edge);         // أي edge استخدمنا
//					priorityQueue.add(new Node(AdjVertex, newCost));
//				}
//			}
//
//		} // End while
//
//		PathResult result = buildResult(destination, optType);
//		clear();
//
//		return result;
//	}
//
//	// أول شيء: يجهز القيم (كل المسافات infinity، والمصدر 0).
//	private void initialize(String source) {
//		for (String node : graph.getAllNodes()) {
//			cost.put(node, Double.MAX_VALUE);
//			previous.put(node, null);
//		}
//
//		cost.put(source, 0.0);
//		priorityQueue.add(new Node(source, 0.0));
//	}
//
//	// اختيار الوزن حسب نوع التحسين
//	private double getWeight(Edge edge, int optType) {
//		return (optType == 1) ? edge.getDistance() : edge.getTime();
//	}
//
//	// total time and total dictance //بناء PathResult النهائي
//	private PathResult buildResult(String destination, int optType) {
//		
//		List<String> path = tructPath(destination);
//		
//		if (path.isEmpty()) {  // إذا ما في مسار
//			return new PathResult(path, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, optType);
//		}
//		
//		double totalDistance = 0.0;
//		double totalTime = 0.0;
//
//		// جمع distance و time من الـ edges المختارة
//		for (int i = 1; i < path.size(); i++) {
//			Edge e = prevEdge.get(path.get(i));
//			if (e != null) {
//				totalDistance += e.getDistance();
//				totalTime += e.getTime();
//			}
//		}
//		return new PathResult(path, totalDistance, totalTime, optType);
//	}
//
//	private List<String> tructPath(String destination) { // برجعلي الباث
//
//		List<String> path = new ArrayList<>();
//		String current = destination;
//
//		// إذا لم نصل للعقدة
//		if (!cost.containsKey(current) || cost.get(current) == Double.MAX_VALUE) {
//			return path;
//		}
//
//		while (current != null) { 	// الرجوع للخلف باستخدام previous
//			path.add(current);
//			current = previous.get(current);
//		}
//		
//		Collections.reverse(path);
//		return path;
//	}
//
//	private void clear() {
//		priorityQueue.clear();
//		cost.clear();
//		previous.clear();
//		prevEdge.clear();
//		visited.clear();
//	}
//
//	//////////////////////// Inner Class ///////////////////////////////////
//
//	public static class TwoResults {
//
//		private final PathResult byDistance;
//		private final PathResult byTime;
//
//		public TwoResults(PathResult byDistance, PathResult byTime) {
//			this.byDistance = byDistance;
//			this.byTime = byTime;
//		}
//
//		public PathResult getByDistance() {
//			return byDistance;
//		}
//
//		public PathResult getByTime() {
//			return byTime;
//		}
//	}
//}
