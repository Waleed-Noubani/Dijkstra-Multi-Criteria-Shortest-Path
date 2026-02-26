// Graph  --> تمثيل الجراف (nodes + edges)
// Node → عنصر داخل الـ PriorityQueue
package application;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.Scanner;

public class Graph {
	
	private LinkedList<Edge>[] adjacencyList;  // كل index يمثل عقدة (node)،,,,,  والـ LinkedList فيها Edges الخارجة من هذه العقدة.
	private String[] nodeNames; // 3050 , 120 ...
	private int numNodes;  // num of vertex
	private int maxNodes;
	
	private String sourceNode;
	private String destinationNode;
	private int optimizationType;
	//private int[] nodeIds; // نفس العقد لكن كـ int ومرتبة. (binary search )


	public Graph() {
		this.maxNodes = 10000;
		this.adjacencyList = (LinkedList<Edge>[]) new LinkedList[maxNodes];
		this.nodeNames = new String[maxNodes];
		this.numNodes = 0;
		//this.nodeIds= null ;
		
		for (int i = 0; i < maxNodes; i++) {
			adjacencyList[i] = new LinkedList<>();
		}
	}

	
	//  Binary search على nodeNames (لأنها sorted)
    public int getNodeIndex(String nodeName) {
        if (nodeName == null || numNodes == 0) return -1;
        int pos = Arrays.binarySearch(nodeNames, 0, numNodes, nodeName.trim());
        return (pos >= 0) ? pos : -1;
    }
    
//	public int getNodeIndex(String nodeName) {  // binary search
//	    if (nodeIds == null || numNodes == 0) return -1;
//
//	    int id;
//	    try {
//	        id = Integer.parseInt(nodeName.trim());  // "12" --> 12
//	    } catch (NumberFormatException e) {
//	        return -1;
//	    }
//
//	    int pos = Arrays.binarySearch(nodeIds, id);
//	    return (pos >= 0) ? pos : -1;
//	}

	
	public String getNodeName(int index) {
		if (index >= 0 && index < numNodes) {
			return nodeNames[index];
		}
		return null;
	}
	 

	//  O(E log V)
	
	public void readFromFile(String filename) throws FileNotFoundException {

	    File file = new File(filename);
	    Scanner sc1 = new Scanner(file);

	    if (sc1.hasNextLine()) {
	        String[] firstLine = sc1.nextLine().trim().split("\\s+");
	        sourceNode = firstLine[0];
	        destinationNode = firstLine[1];
	        optimizationType = Integer.parseInt(firstLine[2]);
	    }

	    int edgeCount = 0;
	    while (sc1.hasNextLine()) {
	        String line = sc1.nextLine().trim();
	        if (line.isEmpty()) continue;

	        String[] parts = line.split("\\s+");
	        if (parts.length != 4) continue;

	        edgeCount++;
	    }
	    sc1.close();

	    if (edgeCount == 0) {
	        for (int i = 0; i < numNodes; i++) adjacencyList[i].clear();
	        numNodes = 0;
	        return;
	    }

	    String[] fromArr = new String[edgeCount]; // الهدف من التخزين المؤقت: أبني vertices أولًا ثم أبني adjacencyList. لانه اليجز بحتوي على 4 بيانات
	    String[] toArr   = new String[edgeCount];
	    double[] distArr = new double[edgeCount];
	    double[] timeArr = new double[edgeCount];

	    // لكل edge في from + to
	    String[] nodesRaw = new String[edgeCount * 2];
	    int rawCount = 0;

	    
	    
	    
	    Scanner sc2 = new Scanner(file);

	    
	    if (sc2.hasNextLine()) sc2.nextLine(); // skip line 1
	   // قراءة الـ edges وتعبئة arrays + جمع vertices
	    int e = 0;
	    while (sc2.hasNextLine()) {
	        String line = sc2.nextLine().trim();
	        if (line.isEmpty()) continue;

	        String[] parts = line.split("\\s+");
	        if (parts.length != 4) continue;

	        String from = parts[0].trim();
	        String to   = parts[1].trim();
	        double distance = Double.parseDouble(parts[2]);
	        double time = Double.parseDouble(parts[3]);

	        fromArr[e] = from;
	        toArr[e] = to;
	        distArr[e] = distance;
	        timeArr[e] = time;

	        nodesRaw[rawCount++] = from;
	        nodesRaw[rawCount++] = to;

	        e++;
	    }
	    sc2.close();

	    nodesRaw = Arrays.copyOf(nodesRaw, rawCount); 	    // sort + unique 

	   // mergeSort(nodesRaw, 0, nodesRaw.length - 1);
	    Arrays.sort(nodesRaw);

	    int uniqueCount = 1;
	    for (int i = 1; i < nodesRaw.length; i++) {
	        if (!nodesRaw[i].equals(nodesRaw[i - 1])) uniqueCount++;
	    }

	    if (uniqueCount > maxNodes) {
	        throw new RuntimeException("Maximum nodes exceeded!");
	    }

	    String[] vertices = new String[uniqueCount];
	    vertices[0] = nodesRaw[0];
	    int idx = 1;
	    for (int i = 1; i < nodesRaw.length; i++) {
	        if (!nodesRaw[i].equals(nodesRaw[i - 1])) {
	            vertices[idx++] = nodesRaw[i];
	        }
	    }

	   // تنظيف adjacencyList من قراءة قديمة + تحديث numNodes
	    int oldNumNodes = this.numNodes;
	    this.numNodes = uniqueCount;

	    int clearCount = Math.max(oldNumNodes, this.numNodes);
	    clearCount = Math.min(clearCount, maxNodes);
	    for (int i = 0; i < clearCount; i++) {
	        adjacencyList[i].clear();
	    }

	    for (int i = 0; i < uniqueCount; i++) {
	        nodeNames[i] = vertices[i];
	    }

	    //  بناء adjacencyList 
	    for (int i = 0; i < edgeCount; i++) {
	        String from = fromArr[i];

		     // int pos= binarySearch(vertices, from);
	        int pos = Arrays.binarySearch(vertices, from);

	        if (pos >= 0) {
	            adjacencyList[pos].add(new Edge(toArr[i], distArr[i], timeArr[i]));
	        }
	    }
	}





	public LinkedList<Edge> getAdjecant(String nodeName) {
		int index = getNodeIndex(nodeName);
		if (index == -1) {
			return new LinkedList<>();
		}
		return adjacencyList[index];
	}

	public String[] getAllNodes() {
		String[] result = new String[numNodes];
		for (int i = 0; i < numNodes; i++) {
			result[i] = nodeNames[i];
		}
		return result;
	}


	public boolean hasNode(String nodeName) {
		return getNodeIndex(nodeName) != -1;
	}

	public String getSourceNode() {
		return sourceNode;
	}

	public String getDestinationNode() {
		return destinationNode;
	}

	public int getOptimizationType() {
		return optimizationType;
	}
	
	public int getNumNodes() {
		return numNodes;
	}
	
	private void mergeSort(String[] arr, int left, int right) {
	    if (left >= right) return;

	    int mid = (left + right) / 2;
	    mergeSort(arr, left, mid);
	    mergeSort(arr, mid + 1, right);
	    merge(arr, left, mid, right);
	}

	private void merge(String[] arr, int left, int mid, int right) {
	    int n1 = mid - left + 1;
	    int n2 = right - mid;

	    String[] L = new String[n1];
	    String[] R = new String[n2];

	    for (int i = 0; i < n1; i++)
	        L[i] = arr[left + i];

	    for (int j = 0; j < n2; j++)
	        R[j] = arr[mid + 1 + j];

	    int i = 0, j = 0, k = left;

	    while (i < n1 && j < n2) {
	        if (L[i].compareTo(R[j]) <= 0) {
	            arr[k++] = L[i++];
	        } else {
	            arr[k++] = R[j++];
	        }
	    }

	    while (i < n1) arr[k++] = L[i++];
	    while (j < n2) arr[k++] = R[j++];
	}



	public static int binarySearch(String[] a, String key) {
	    return binarySearch0(a, 0, a.length, key);
	}

	private static int binarySearch0(String[] a, int fromIndex, int toIndex, String key) {
	    int low = fromIndex;
	    int high = toIndex - 1;

	    while (low <= high) {
	        int mid = (low + high) >>> 1;
	        String midVal = a[mid];

	        int cmp = midVal.compareTo(key);

	        if (cmp < 0)
	            low = mid + 1;
	        else if (cmp > 0)
	            high = mid - 1;
	        else
	            return mid; // key found
	    }
	    return -(low + 1); // key not found
	}

	

}

//public void readFromFil(String filename) throws FileNotFoundException {
//File file = new File(filename);
//Scanner scanner = new Scanner(file);
//
//if (scanner.hasNextLine()) {                        	                 //قراءة أول سطر   
//    String[] firstLine = scanner.nextLine().trim().split("\\s+");
//    sourceNode = firstLine[0];
//    destinationNode = firstLine[1];
//    optimizationType = Integer.parseInt(firstLine[2]);
//}
//
//
//int edgesCap = 1024; // initial     //   ليش 4؟ (عشان كل (edge عنده هدول الأربع قيم.
////int[] fromArr = new int[edgesCap];
////int[] toArr   = new int[edgesCap];
//String[] fromArr = new String[edgesCap];
//String[] toArr   = new String[edgesCap];
//double[] distArr = new double[edgesCap];
//double[] timeArr = new double[edgesCap];
//int edgCount = 0;
//
//int nodesCap = 2048;
//// int[] nodesRaw = new int[nodesCap];  // store nods from + to
//String[] nodesRaw = new String[nodesCap];
//int nodeCount = 0;
//
//while (scanner.hasNextLine()) {                 // read lines reminder
//	
//    String line = scanner.nextLine().trim();
//    if (line.isEmpty()) continue;
//
//    String[] parts = line.split("\\s+");
//    if (parts.length != 4) continue;
//
////    int from = Integer.parseInt(parts[0]);
////    int to = Integer.parseInt(parts[1]);
//    String from = parts[0].trim();
//    String to   = parts[1].trim();
//    double distance = Double.parseDouble(parts[2]);
//    double time = Double.parseDouble(parts[3]);
//
//    // resize :  كبّر مصفوفات edges عند الحاجة
//    if (edgCount == edgesCap) {
//        edgesCap *= 2;
//        fromArr = Arrays.copyOf(fromArr, edgesCap);
//        toArr   = Arrays.copyOf(toArr, edgesCap);
//        distArr = Arrays.copyOf(distArr, edgesCap);
//        timeArr = Arrays.copyOf(timeArr, edgesCap);
//    }
//
//    fromArr[edgCount] = from;  
//    toArr[edgCount] = to;
//    distArr[edgCount] = distance;
//    timeArr[edgCount] = time;
//    edgCount++;
//
//    // resize
//    if (nodeCount + 2 > nodesCap) {
//        nodesCap *= 2;
//        nodesRaw = Arrays.copyOf(nodesRaw, nodesCap);
//    }
//    nodesRaw[nodeCount++] = from;
//    nodesRaw[nodeCount++] = to;
//}
//scanner.close();             // finish read to file
//
//
//// لو ما في edges
//if (edgCount == 0) {
//    for (int i = 0; i < numNodes; i++) adjacencyList[i].clear();
//    numNodes = 0;
//    return;
//}
//
//// sort + delete dublicete
//nodesRaw = Arrays.copyOf(nodesRaw, nodeCount);
//Arrays.sort(nodesRaw);
////mergeSort(nodesRaw, 0, nodesRaw.length - 1);
//
//
//int uniqueCount = 1;
//for (int i = 1; i < nodesRaw.length; i++) {
//   // if (nodesRaw[i] != nodesRaw[i - 1]) uniqueCount++;
//    if (!nodesRaw[i].equals(nodesRaw[i - 1])) uniqueCount++;
//}
//
//if (uniqueCount > maxNodes) {
//    throw new RuntimeException("Maximum nodes exceeded!");
//}
//
////int[] vertices = new int[uniqueCount];  // = nodeIds []
//String[] vertices = new String[uniqueCount];
//vertices[0] = nodesRaw[0];
//int idx = 1;
//for (int i = 1; i < nodesRaw.length; i++) {
//    //if (nodesRaw[i] != nodesRaw[i - 1]) {
//    if (!nodesRaw[i].equals(nodesRaw[i - 1])) {
//        vertices[idx++] = nodesRaw[i];
//    }
//}
////    this.nodeIds = vertices;         // نظييفه ومرتبة
//
////  حدّث  وبنفس الوقت نظّف 
//int oldNumNodes = this.numNodes;
//this.numNodes = uniqueCount;
//
//int clearCount = Math.max(oldNumNodes, this.numNodes); // تنظيف القوائم القديمة حتى ما تضل edges من قراءة سابقة.
//clearCount = Math.min(clearCount, maxNodes);
//for (int i = 0; i < clearCount; i++) {
//    adjacencyList[i].clear();
//}
//
//for (int i = 0; i < uniqueCount; i++) {
////    nodeNames[i] = String.valueOf(vertices[i]); // بعبي nodeName [] 
//    nodeNames[i] = vertices[i];
//}
//
//// بناء adjacencyList ( باستخدام binarySearch )
//for (int i = 0; i < edgCount; i++) {
//    String from = fromArr[i];
//   // int pos binarySearch(vertices, from);
//
//    int pos = Arrays.binarySearch(vertices, from); // (array , key )  ret index
//    if (pos >= 0)         
//     adjacencyList[pos].add(new Edge(String.valueOf(toArr[i]), distArr[i], timeArr[i]));
//}
//
////System.out.println("Graph loaded success ");
////System.out.println("Total nodes: " + numNodes);
////System.out.println("Total edges: " + getTotalEdges());
//}
//private int getOrAddNodeIndex(String nodeName) { // search or add
//for (int i = 0; i < numNodes; i++) {
//	if (nodeNames[i].equals(nodeName)) {
//		return i;
//	}
//}
//
//if (numNodes >= maxNodes) {
//	throw new RuntimeException("Maximum nodes exceeded!");
//}
//nodeNames[numNodes] = nodeName;
//return numNodes++;
//}

//public void addEdge(String from, String to, double distance, double time) {
//int fromIndex = getOrAddNodeIndex(from);
//adjacencyList[fromIndex].add(new Edge(to, distance, time));
//}
//

//public int getTotalEdges() {
//int count = 0;
//for (int i = 0; i < numNodes; i++) {
//	count += adjacencyList[i].size();
//}
//return count;
//}





//
//
//
//
//
//
//
//
//public void readFromFile(String filename) throws FileNotFoundException {
//File file = new File(filename);
//Scanner scanner = new Scanner(file);
//
//if (scanner.hasNextLine()) {
//	String[] firstLine = scanner.nextLine().trim().split("\\s+");
//	sourceNode = firstLine[0];
//	destinationNode = firstLine[1];
//	optimizationType = Integer.parseInt(firstLine[2]);
//
//	System.out.println("Source: " + sourceNode);
//	System.out.println("Destination: " + destinationNode);
//	System.out.println("Optimization Type: " + optimizationType);
//}
//
//while (scanner.hasNextLine()) {
//	String line = scanner.nextLine().trim();
//	if (line.isEmpty()) continue;
//
//	String[] parts = line.split("\\s+");
//	if (parts.length == 4) {
//		String from = parts[0];
//		String to = parts[1];
//		double distance = Double.parseDouble(parts[2]);
//		double time = Double.parseDouble(parts[3]);
//		
//		addEdge(from, to, distance, time);
//	}
//}
//scanner.close();
//
//System.out.println("Graph loaded successfully!");
//System.out.println("Total nodes: " + numNodes);
//System.out.println("Total edges: " + getTotalEdges());
//}





//package application;
//
//import javafx.geometry.Point2D;
//import java.io.File;
//import java.io.FileNotFoundException;
//import java.util.*;
//
//public class Graph {
//	private Map<String, List<Edge>> adjacencyList; // الشبكة
//	//private Map<String, Point2D> nodePositions; // استخدمنا Point2D لتمثيل موقع كل node على الشاشة باستخدام إحداثيات
//												// (X,Y)،
//												// ودالة generateCircularLayout : تقوم بتوزيع الـ nodes بشكل دائري حول
//												// مركز محدد باستخدام دوال sin و cos.
//	private Set<String> allNodes; // كل الـ nodes
//
//	// معلومات من أول سطر في الملف
//	private String sourceNode;
//	private String destinationNode;
//	private int optimizationType;
//
//	public Graph() {
//		adjacencyList = new HashMap<>();
//		//nodePositions = new HashMap<>();
//		allNodes = new HashSet<>();
//	}
//
//	public void addEdge(String from, String to, double distance, double time) {
//		adjacencyList.putIfAbsent(from, new ArrayList<>()); // إذا (from) مش موجودة كمفتاح في الـ adjacencyList،|| بعمل
//															// إلها قائمة جيران جديدة.
//		adjacencyList.get(from).add(new Edge(to, distance, time)); // بضيف طريق جديد من from إلى to، ومعه distance و
//																	// time.
//
//		allNodes.add(from); // بسجّل العقدتين ضمن مجموعة كل العقد.
//		allNodes.add(to);
//	}
//
////	// إضافة edge في الاتجاهين (Undirectional)
////	public void addBidirectionalEdge(String from, String to, double distance, double time) {
////		addEdge(from, to, distance, time);
////		addEdge(to, from, distance, time);
////	}
//
//	public void readFromFile(String filename) throws FileNotFoundException {
//		File file = new File(filename);
//		Scanner scanner = new Scanner(file);
//
//		// source * destination * optimizationType
//		if (scanner.hasNextLine()) {
//			String[] firstLine = scanner.nextLine().trim().split("\\s+"); // أي عدد من المسافات
//			sourceNode = firstLine[0];
//			destinationNode = firstLine[1];
//			optimizationType = Integer.parseInt(firstLine[2]);
//
//			System.out.println("Source: " + sourceNode);
//			System.out.println("Destination: " + destinationNode);
//			System.out.println("Optimization Type: " + optimizationType);
//		}
//
//		// قراءة باقي الأسطر
//		while (scanner.hasNextLine()) {
//			String line = scanner.nextLine().trim();
//			if (line.isEmpty())
//				continue;
//
//			String[] parts = line.split("\\s+");
//			if (parts.length == 4) {
//				String from = parts[0];
//				String to = parts[1];
//				double distance = Double.parseDouble(parts[2]);
//				double time = Double.parseDouble(parts[3]);
//				// أضف edge في الاتجاهين
//				addEdge(from, to, distance, time);
//			}
//		}
//		scanner.close();
//
//		System.out.println("Graph loaded successfully!");
//		System.out.println("Total nodes: " + allNodes.size());
//		System.out.println("Total edges: " + getTotalEdges());
//
//		// ولّد مواقع الـ nodes للرسم
//	//	generateCircularLayout();
//	}
//
////	// توليد مواقع الـ nodes على شكل دائرة
////	public void generateCircularLayout() {
////		List<String> nodeList = new ArrayList<>(allNodes); // بحوّل الـ Set لقائمة عشان يمشي عليها بالترتيب.
////		int numNodes = nodeList.size();
////
////		 // بيحدد مركز دائرة (400,400) ونص قطر 350.
////		double centerX = 500;
////		double centerY = 400;
////		double radius = 280;
////
////		// رتب الـ nodes على الدائرة
////		for (int i = 0; i < numNodes; i++) {
////			double angle = 2 * Math.PI * i / numNodes; // لكل node: بحسب زاوية مختلفة
////			double x = centerX + radius * Math.cos(angle);
////			double y = centerY + radius * Math.sin(angle);
////
////			nodePositions.put(nodeList.get(i), new Point2D(x, y)); // وبخزنهم في nodePositions // add --> put in hashMap
////		}
////	}
//
//	public List<Edge> getAdjecant(String node) { // إذا node موجودة برجع قائمة الـ edges || إذا مش موجودة برجع قائمة
//													// فاضية بدل null (ممتاز لتفادي NullPointerException)
//		return adjacencyList.getOrDefault(node, new ArrayList<>());
//	}
//
//	public Set<String> getAllNodes() {
//		return allNodes;
//	}
//
////	public Point2D getNodePosition(String node) {
////		return nodePositions.get(node);
////	}
//
//	public int getTotalEdges() {
//		int count = 0;
//		for (List<Edge> edges : adjacencyList.values()) {
//			count += edges.size();
//		}
//		return count ;
//	}
//
//	public String getSourceNode() {
//		return sourceNode;
//	}
//
//	public String getDestinationNode() {
//		return destinationNode;
//	}
//
//	public int getOptimizationType() {
//		return optimizationType;
//	}
//
//	public boolean hasNode(String node) {
//		return allNodes.contains(node);
//	}
//
////	// toString للطباعة (للتجربة)
////	@Override
////	public String toString() {
////		StringBuilder sb = new StringBuilder();
////		sb.append("Graph:\n");
////		sb.append("Nodes: ").append(allNodes.size()).append("\n");
////		sb.append("Edges: ").append(getTotalEdges()).append("\n");
////		return sb.toString();
////	}
//}
//
////شو الفرق بين Map و Set و List؟
//
////1️⃣ List (قائمة)
////List<String> names = new ArrayList<>();
////names.add("A");
////names.add("B");
////names.add("A");
////
////
////✔ تقبل التكرار
////✔ فيها ترتيب
////✔ نضيف بـ add
////
////2️⃣ Set (مجموعة)
////Set<String> nodes = new HashSet<>();
////nodes.add("A");
////nodes.add("B");
////nodes.add("A"); // ❌ ما تنضاف
////
////
////✔ بدون تكرار
////❌ ما يهم الترتيب
////✔ نضيف بـ add
////
////📌 عشان هيك استخدمنا Set لـ allNodes
////
////3️⃣ Map (قاموس)
////Map<String, Integer> ages = new HashMap<>();
////ages.put("Ali", 20);
////ages.put("Sara", 22);
////ages.put("Ali", 25); // يستبدل القيمة
////
////
////✔ كل مفتاح فريد
////✔ قيمة لكل مفتاح
////❌ ما فيها add
////✔ نضيف بـ put