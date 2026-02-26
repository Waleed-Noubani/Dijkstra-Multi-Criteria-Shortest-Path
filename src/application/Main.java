package application;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.Path;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.animation.Animation;
import javafx.animation.FadeTransition;
import javafx.animation.PathTransition;
import javafx.animation.SequentialTransition;
import java.util.List;

public class Main extends Application {

	private Graph graph;
	private Dijkstra dijkstra;

	private ComboBox<String> sourceCombo; // input
	private ComboBox<String> destCombo;

	private RadioButton distanceRadio; // type
	private RadioButton timeRadio;
	private RadioButton bothRadio;
	
	private RadioButton selDistancePath;  // type2
	private RadioButton selTimePath;

	private Label distLabel; // result
	private Label timeLabel;

	private Pane pathPane;
	private Animation currentAnim;
	private ScrollPane pathScroll;
	private Dijkstra.TwoResults currentTwoResults;

	@Override
	public void start(Stage stage) {
		try {
			graph = new Graph();
			graph.readFromFile("graph_data.txt");
			dijkstra = new Dijkstra();

			VBox root = new VBox(20);
			root.setPadding(new Insets(30));
			root.setAlignment(Pos.TOP_CENTER);
			root.getStyleClass().add("root");

			Label titleLabel = new Label(" Dijkstra Shortest Path Finder");
			titleLabel.getStyleClass().add("app-title");

			VBox inputPanel = createInputPanel();
			HBox optionsPanel = createOptionsPanel();
			HBox buttonsPanel = createButtonsPanel();
			VBox resultsPanel = createResultsPanel();

			Separator sep1 = new Separator();
			Separator sep2 = new Separator();
			Separator sep3 = new Separator();

			root.getChildren().addAll(titleLabel, sep1, inputPanel, sep2, optionsPanel, buttonsPanel, sep3,
					resultsPanel);

			Scene scene = new Scene(root, 900, 750);
			scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());

			stage.setTitle("Shortest Path");
			stage.setScene(scene);
			stage.show();

		} catch (Exception e) {
			showError("Error", e.getMessage());
			e.printStackTrace();
		}
	}

	private VBox createInputPanel() {
		VBox box = new VBox(15);
		box.setAlignment(Pos.CENTER);
		box.setPrefWidth(700);

		HBox sourceBox = new HBox(15);
		sourceBox.setAlignment(Pos.CENTER);

		Label srcLabel = new Label(" Source Node:");
		srcLabel.getStyleClass().add("field-label");
		srcLabel.setPrefWidth(180);

		sourceCombo = new ComboBox<>();
		sourceCombo.setEditable(true);
		sourceCombo.setPromptText("Select source node");
		sourceCombo.getItems().addAll(graph.getAllNodes());
		sourceCombo.setPrefWidth(450);

		if (graph.getSourceNode() != null) {
			sourceCombo.setValue(graph.getSourceNode());
		}
		sourceBox.getChildren().addAll(srcLabel, sourceCombo);

		///////////////////////////

		HBox destBox = new HBox(15);
		destBox.setAlignment(Pos.CENTER);

		Label destLabel = new Label(" Destination Node:");
		destLabel.getStyleClass().add("field-label");
		destLabel.setPrefWidth(180);

		destCombo = new ComboBox<>();
		destCombo.setEditable(true);
		destCombo.setPromptText("Select destination node");
		destCombo.getItems().addAll(graph.getAllNodes());
		destCombo.setPrefWidth(450);

		if (graph.getDestinationNode() != null) {
			destCombo.setValue(graph.getDestinationNode());
		}

		destBox.getChildren().addAll(destLabel, destCombo);
		box.getChildren().addAll(sourceBox, destBox);
		return box;
	}

	private HBox createButtonsPanel() {
		HBox box = new HBox(20);
		box.setAlignment(Pos.CENTER);
		box.setPrefWidth(700);

		Button findBtn = new Button(" Find Shortest Path");
		Button clearBtn = new Button(" Clear All");

		findBtn.getStyleClass().add("find-btn");
		clearBtn.getStyleClass().add("clear-btn");
		findBtn.setPrefWidth(300);
		clearBtn.setPrefWidth(300);
		findBtn.setOnAction(e -> findPath());
		clearBtn.setOnAction(e -> clear());

		box.getChildren().addAll(findBtn, clearBtn);
		return box;
	}

	private HBox createOptionsPanel() {
		HBox box = new HBox(12);
		box.setAlignment(Pos.CENTER);

		Label optLabel = new Label(" Optimization Type:");
		optLabel.getStyleClass().add("field-label");

		ToggleGroup group = new ToggleGroup();

		distanceRadio = new RadioButton(" Shortest Distance");
		timeRadio = new RadioButton(" Minimum Time");
		bothRadio = new RadioButton(" Both (Compare)");

		distanceRadio.setToggleGroup(group);
		timeRadio.setToggleGroup(group);
		bothRadio.setToggleGroup(group);

		int opt = graph.getOptimizationType();
		if (opt == 1)
			distanceRadio.setSelected(true);
		else if (opt == 2)
			timeRadio.setSelected(true);
		else
			bothRadio.setSelected(true);

		box.getChildren().addAll(optLabel, distanceRadio, timeRadio, bothRadio);
		return box;

	}

	private HBox pathSelectBox;

	private VBox createResultsPanel() {
		VBox box = new VBox(15);
		box.setAlignment(Pos.CENTER);
		box.setPrefWidth(800);

		Label resLabel = new Label(" Results:");
		resLabel.getStyleClass().add("results-title");

		pathPane = new Pane();
		pathPane.setMinHeight(100);
		pathPane.setPrefHeight(100);
		pathPane.getStyleClass().add("path-pane");

		pathScroll = new ScrollPane(pathPane);
		pathScroll.setPrefViewportWidth(800);
		pathScroll.setPrefViewportHeight(170);
		pathScroll.setFitToHeight(true);
		pathScroll.setFitToWidth(false);
		pathScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
		pathScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
		pathScroll.getStyleClass().add("path-scroll");

		ToggleGroup selPathG = new ToggleGroup();

		selDistancePath = new RadioButton(" Distance Path");
		selTimePath = new RadioButton(" Time Path");

		selDistancePath.setSelected(true);

		selDistancePath.setToggleGroup(selPathG);
		selTimePath.setToggleGroup(selPathG);

		selDistancePath.setOnAction(e -> {
			if (currentTwoResults != null) {
				PathResult byDist = currentTwoResults.getByDistance();
				if (!byDist.getPath().isEmpty()) {
					visualizePath(byDist.getPath(), byDist);
				}
			}
		});

		selTimePath.setOnAction(e -> {
			if (currentTwoResults != null) {
				PathResult byTime = currentTwoResults.getByTime();
				if (!byTime.getPath().isEmpty()) {
					visualizePath(byTime.getPath(), byTime);
				}
			}
		});

		pathSelectBox = new HBox(20);
		pathSelectBox.setAlignment(Pos.CENTER);
		pathSelectBox.getChildren().addAll(selDistancePath, selTimePath);
		pathSelectBox.setVisible(false);  

		
		HBox statsBox = new HBox(50);
		statsBox.setAlignment(Pos.CENTER);

		distLabel = new Label(" Total Distance: ... ");
		distLabel.getStyleClass().add("dist-label");
		timeLabel = new Label(" Total Time: ... ");
		timeLabel.getStyleClass().add("time-label");

		statsBox.getChildren().addAll(distLabel, timeLabel);

		box.getChildren().addAll(resLabel, pathScroll, pathSelectBox, statsBox);
		return box;
	}

	private void findPath() {
		String source = sourceCombo.getValue();
		String dest = destCombo.getValue();

		if (dest == null || dest.trim().isEmpty() || source.equals(dest)) {
			showWarning("⚠️ Please select a destination node and source node but not the same");
			return;
		}

		if (!graph.hasNode(source) || !graph.hasNode(dest)) {
			showWarning("⚠️ Source or destination node not found in graph");
			return;
		}

		int type = 1;
		if (distanceRadio.isSelected())
			type = 1;
		else if (timeRadio.isSelected())
			type = 2;
		else if (bothRadio.isSelected())
			type = 3;

		try {
			Object result = dijkstra.findShortestPath(graph, source, dest, type);

			if (type == 3) {
				Dijkstra.TwoResults two = (Dijkstra.TwoResults) result;
				displayTwoResults(two);
			} else {
				currentTwoResults = null;
				pathSelectBox.setVisible(false);
				PathResult path = (PathResult) result;
				displayOneResult(path, type);
			}
		} catch (Exception e) {
			showError("Error", "Failed to find path: " + e.getMessage());
			e.printStackTrace();
		}
	}

	private void displayOneResult(PathResult result, int type) {
		pathSelectBox.setVisible(false);
		visualizePath(result.getPath(), result);

		distLabel.setText(String.format(" Total Distance: %.2f km", result.getTotalDistance()));
		timeLabel.setText(String.format(" Total Time: %.2f min", result.getTotalTime()));
	}

	
	private void displayTwoResults(Dijkstra.TwoResults two) {
		currentTwoResults = two;
		pathSelectBox.setVisible(true);
		PathResult byDist = two.getByDistance();
		PathResult byTime = two.getByTime();

		if (!byDist.getPath().isEmpty() && !byTime.getPath().isEmpty()) {
			distLabel.setText(String.format(" Dist Optimization: %.2f km", byDist.getTotalDistance()));
			timeLabel.setText(String.format(" Time Optimization: %.2f min", byTime.getTotalTime()));
		}

		if (!byDist.getPath().isEmpty() && selDistancePath.isSelected()) {
			visualizePath(byDist.getPath(), byDist);
		} else if (!byTime.getPath().isEmpty() && selTimePath.isSelected()) {
			visualizePath(byTime.getPath(), byTime);
		} else {
			stopAndClearPathView();
		}
	}
	

	//  PATH 5s ANIMATION
	private void visualizePath(List<String> path, PathResult result) {
		if (pathPane == null)
			return;

		if (currentAnim != null) // إذا في أنيميشن شغال من قبل، وقفه
			currentAnim.stop();
		
		pathPane.getChildren().clear();

		if (path == null || path.size() < 2)
			return;

		// توزيع أفقي (سطر واحد)
		double startX = 40;
		double y = 50;
		double stepX = 140;  // المسافة  بين كل عقدة والتي بعدها
		double radius = 17; // نصف قطر دائرة العقدة

		pathPane.setPrefWidth(1600);
		pathPane.setPrefHeight(200); // ارتفاع ثابت للوحة الرسم

		double[] cx = new double[path.size()];
		double[] cy = new double[path.size()];

		// رسم الفيرتكس
		for (int i = 0; i < path.size(); i++) {
			double x = startX + i * stepX;

			cx[i] = x;
			cy[i] = y;

			Circle node = new Circle(x, y, radius);
			node.getStyleClass().add("path-node");

			Text label = new Text(path.get(i));
			label.getStyleClass().add("path-node-label");
			//label.setX(x - Math.min(14, path.get(i).length() * 3));
			label.setX(x - 14);
			label.setY(y + 38);  // setY: ينزل تحت الدائرة بـ 38

			pathPane.getChildren().addAll(node, label);
		}

		// رسم edges + بياناتها
		Line[] lines = new Line[path.size() - 1];
		for (int i = 0; i < path.size() - 1; i++) {

			Line line = new Line(cx[i] + radius, cy[i], cx[i + 1] - radius, cy[i]); //ارسم خط أفقي بين العقدتين
			line.getStyleClass().add("path-line");
			line.setOpacity(0.0); 
			lines[i] = line; // خزنته
			pathPane.getChildren().add(0, line);

			Edge e = null;
			LinkedList<Edge> adj = graph.getAdjecant(path.get(i)); // جبنا ال ادجيسانت
			LinkedList<Edge>.Iterator<Edge> it = adj.iterator();

			while (it.hasNext()) {
				Edge ed = it.next();
				if (ed.getDestination().equals(path.get(i + 1))) { // أول edge وجهته تساوي العقدة التالية في المسار --> هذا هو الـ edge الصحيح.
					e = ed;
					break;
				}
			}

			if (e != null) {
				double midX = (cx[i] + cx[i + 1]) / 2;   // منتصف الخط (X) عشان نحط النص بالنص
				double midY = cy[i];
				

				Text distText = new Text(String.format("%.1f km", e.getDistance()));
				distText.getStyleClass().add("edge-distance");
				distText.setX(midX - 22);
				distText.setY(midY - 8);

				Text timeText = new Text(String.format("%.1f min", e.getTime()));
				timeText.getStyleClass().add("edge-time");
				timeText.setX(midX - 22);
				timeText.setY(midY + 18);

				pathPane.getChildren().addAll(distText, timeText);
			}
		}

//		// marker
//		Circle marker = new Circle(cx[0], cy[0], 6);
//		marker.getStyleClass().add("path-marker");
//		pathPane.getChildren().add(marker);

		// Path للحركة
		Path fxPath = new Path();
		fxPath.getElements().add(new MoveTo(cx[0], cy[0]));
		for (int i = 1; i < path.size(); i++) {
			fxPath.getElements().add(new LineTo(cx[i], cy[i]));
		}

		SequentialTransition reveal = new SequentialTransition(); // أنيميشن متسلسل: خطوة بعد خطوة
		for (Line l : lines) {
			FadeTransition ft = new FadeTransition(Duration.millis(320), l); // اعمل ‏تلاشي‏ لمدة 120 من مخفي إلى ظاهر  تنضاف وحدة وحدة بالترتيب.
			ft.setFromValue(0.0);
			ft.setToValue(1.0);
			reveal.getChildren().add(ft);
		}

		//PathTransition move = new PathTransition(Duration.seconds(5), fxPath, marker);

		SequentialTransition all = new SequentialTransition(reveal);
		currentAnim = all;
		all.play();
	}

	private void clear() {
		sourceCombo.setValue(null);
		destCombo.setValue(null);

		distanceRadio.setSelected(true);
		selDistancePath.setSelected(true);

		currentTwoResults = null;
		pathSelectBox.setVisible(false);

		distLabel.setText(" Total Distance: ...");
		timeLabel.setText(" Total Time: ...");

		stopAndClearPathView();
	}

	private void stopAndClearPathView() {
		if (currentAnim != null)
			currentAnim.stop();
		if (pathPane != null)
			pathPane.getChildren().clear();
	}

	private void showWarning(String message) {
		Alert alert = new Alert(Alert.AlertType.WARNING);
		alert.setTitle("Warning");
		alert.setHeaderText(null);
		alert.setContentText(message);
		styleAlert(alert);
		alert.showAndWait();
	}

	private void showError(String title, String message) {
		Alert alert = new Alert(Alert.AlertType.ERROR);
		alert.setTitle(title);
		alert.setHeaderText(null);
		alert.setContentText(message);
		styleAlert(alert);
		alert.showAndWait();
	}

	private void styleAlert(Alert alert) {
		DialogPane dialogPane = alert.getDialogPane();
		dialogPane.setStyle("-fx-background-color: #2d3142; -fx-font-size: 14px;");
		dialogPane.lookupAll(".content").forEach(node -> node.setStyle("-fx-text-fill: white;"));
	}

	public static void main(String[] args) {
		launch(args);
	}
}

//package application;
//
//import javafx.application.Application;
//import javafx.geometry.Insets;
//import javafx.geometry.Pos;
//import javafx.scene.Scene;
//import javafx.scene.control.*;
//import javafx.scene.layout.*;
//import javafx.stage.Stage;
//import java.util.List;
//
//public class Main extends Application {
//
//	private Graph graph;
//	private Dijkstra dijkstra;
//	private ComboBox<String> sourceCombo;
//	private ComboBox<String> destCombo;
//	private RadioButton distanceRadio;
//	private RadioButton timeRadio;
//	private RadioButton bothRadio;
//	private TextArea resultArea;
//	private Label distLabel;
//	private Label timeLabel;
//
//	@Override
//	public void start(Stage stage) {
//		try {
//			// تحميل الملف
//			graph = new Graph();
//			graph.readFromFile("graph_data.txt");
//			dijkstra = new Dijkstra();
//
//			// البناء الرئيسي
//			VBox root = new VBox(20);
//			root.setPadding(new Insets(30));
//			root.setAlignment(Pos.TOP_CENTER);
//			root.setStyle("-fx-background-color: #1a1d29;");
//
//			// العنوان
//			Label titleLabel = new Label("🗺️ Dijkstra Shortest Path Finder");
//			titleLabel.setStyle("-fx-text-fill: #00b4d8; " + "-fx-font-size: 28px; " + "-fx-font-weight: bold;");
//
//			// Panel الإدخال
//			VBox inputPanel = createInputPanel();
//
//			// Panel الخيارات
//			VBox optionsPanel = createOptionsPanel();
//
//			// الأزرار
//			HBox buttonsPanel = createButtonsPanel();
//
//			// النتائج
//			VBox resultsPanel = createResultsPanel();
//
//			Separator sep1 = new Separator();
//			Separator sep2 = new Separator();
//			Separator sep3 = new Separator();
//
//			root.getChildren().addAll(titleLabel, sep1, inputPanel, sep2, optionsPanel, buttonsPanel, sep3,
//					resultsPanel);
//
//			Scene scene = new Scene(root, 900, 750);
//			scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
//
//			stage.setTitle("Dijkstra Algorithm - Shortest Path");
//			stage.setScene(scene);
//			stage.setResizable(false);
//			stage.show();
//
//		} catch (Exception e) {
//			showError("Error", e.getMessage());
//			e.printStackTrace();
//		}
//	}
//
//	private VBox createInputPanel() {
//		VBox box = new VBox(15);
//		box.setAlignment(Pos.CENTER);
//		box.setPrefWidth(700);
//
//		// Source
//		HBox sourceBox = new HBox(15);
//		sourceBox.setAlignment(Pos.CENTER);
//
//		Label srcLabel = new Label("📍 Source Node:");
//		srcLabel.setStyle("-fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold;");
//		srcLabel.setPrefWidth(180);
//
//		sourceCombo = new ComboBox<>();
//		sourceCombo.setEditable(true);
//		sourceCombo.setPromptText("Select or type source node");
//		
//		sourceCombo.getItems().addAll(graph.getAllNodes());
//		sourceCombo.setPrefWidth(450);
//
//		if (graph.getSourceNode() != null) {
//			sourceCombo.setValue(graph.getSourceNode());
//		}
//
//		sourceBox.getChildren().addAll(srcLabel, sourceCombo);
//
//		// Destination
//		HBox destBox = new HBox(15);
//		destBox.setAlignment(Pos.CENTER);
//
//		Label destLabel = new Label("🎯 Destination Node:");
//		destLabel.setStyle("-fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold;");
//		destLabel.setPrefWidth(180);
//
//		destCombo = new ComboBox<>();
//		destCombo.setEditable(true);
//		destCombo.setPromptText("Select or type destination node");
//		destCombo.getItems().addAll(graph.getAllNodes());
//		destCombo.setPrefWidth(450);
//
//		if (graph.getDestinationNode() != null) {
//			destCombo.setValue(graph.getDestinationNode());
//		}
//
//		destBox.getChildren().addAll(destLabel, destCombo);
//
//		box.getChildren().addAll(sourceBox, destBox);
//		return box;
//	}
//
//	private VBox createOptionsPanel() {
//		VBox box = new VBox(12);
//		box.setAlignment(Pos.CENTER);
//
//		Label optLabel = new Label("⚙️ Optimization Type:");
//		optLabel.setStyle("-fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold;");
//
//		ToggleGroup group = new ToggleGroup();
//
//		distanceRadio = new RadioButton(" Shortest Distance");
//		timeRadio = new RadioButton(" Minimum Time");
//		bothRadio = new RadioButton(" Both (Compare)");
//
//		distanceRadio.setToggleGroup(group);
//		timeRadio.setToggleGroup(group);
//		bothRadio.setToggleGroup(group);
//
//		int opt = graph.getOptimizationType();
//		if (opt == 1)
//			distanceRadio.setSelected(true);
//		else if (opt == 2)
//			timeRadio.setSelected(true);
//		else
//			bothRadio.setSelected(true);
//
//		HBox radioBox = new HBox(30);
//		radioBox.setAlignment(Pos.CENTER);
//		radioBox.getChildren().addAll(distanceRadio, timeRadio, bothRadio);
//
//		box.getChildren().addAll(optLabel, radioBox);
//		return box;
//	}
//
//	private HBox createButtonsPanel() {
//		HBox box = new HBox(20);
//		box.setAlignment(Pos.CENTER);
//		box.setPrefWidth(700);
//
//		Button findBtn = new Button("🔍 Find Shortest Path");
//		Button clearBtn = new Button("🔄 Clear All");
//
//		findBtn.getStyleClass().add("find-btn");
//		clearBtn.getStyleClass().add("clear-btn");
//
//		findBtn.setPrefWidth(300);
//		clearBtn.setPrefWidth(300);
//
//		findBtn.setOnAction(e -> findPath());
//		clearBtn.setOnAction(e -> clear());
//
//		box.getChildren().addAll(findBtn, clearBtn);
//		return box;
//	}
//
//	private VBox createResultsPanel() {
//		VBox box = new VBox(15);
//		box.setAlignment(Pos.CENTER);
//		box.setPrefWidth(800);
//
//		Label resLabel = new Label("📊 Results:");
//		resLabel.setStyle("-fx-text-fill: white; -fx-font-size: 18px; -fx-font-weight: bold;");
//
//		resultArea = new TextArea();
//		resultArea.setPromptText("Results will show here...");
//		resultArea.setEditable(false);
//		resultArea.setWrapText(true);
//		resultArea.setPrefRowCount(12);
//		resultArea.setStyle(
//				"-fx-control-inner-background: #1a1d29; -fx-text-fill: white; -fx-font-size: 15px; -fx-font-weight: bold; -fx-font-family: 'Consolas', 'Monaco', monospace; -fx-border-color: #4f5d75; -fx-border-width: 2px;");
//		resultArea.setPrefWidth(800);
//		resultArea.getStyleClass().add("result-area");
//
//		HBox statsBox = new HBox(50);
//		statsBox.setAlignment(Pos.CENTER);
//
//		distLabel = new Label("📏 Total Distance: --");
//		distLabel.setStyle("-fx-text-fill: #00b4d8; -fx-font-size: 16px; -fx-font-weight: bold;");
//
//		timeLabel = new Label("⏱️ Total Time: --");
//		timeLabel.setStyle("-fx-text-fill: #ffd60a; -fx-font-size: 16px; -fx-font-weight: bold;");
//
//		statsBox.getChildren().addAll(distLabel, timeLabel);
//
//		box.getChildren().addAll(resLabel, resultArea, statsBox);
//		return box;
//	}
//
//	private void findPath() {
//		String source = sourceCombo.getValue();
//		String dest = destCombo.getValue();
//
//		// Validation
//		if (source == null || source.trim().isEmpty()) {
//			showWarning("⚠️ Please select or enter a source node");
//			return;
//		}
//
//		if (dest == null || dest.trim().isEmpty()) {
//			showWarning("⚠️ Please select or enter a destination node");
//			return;
//		}
//
//		if (!graph.hasNode(source)) {
//			showWarning("⚠️ Source node '" + source + "' not found in graph");
//			return;
//		}
//
//		if (!graph.hasNode(dest)) {
//			showWarning("⚠️ Destination node '" + dest + "' not found in graph");
//			return;
//		}
//
//		if (source.equals(dest)) {
//			showWarning("⚠️ Source and destination cannot be the same");
//			return;
//		}
//
//		// Get optimization type
//		int type = 1;
//		if (distanceRadio.isSelected())
//			type = 1;
//		else if (timeRadio.isSelected())
//			type = 2;
//		else if (bothRadio.isSelected())
//			type = 3;
//
//		// Find path
//		try {
//			Object result = dijkstra.findShortestPath(graph, source, dest, type);
//
//			if (type == 3) {
//				Dijkstra.TwoResults two = (Dijkstra.TwoResults) result;
//				displayTwoResults(two);
//			} else {
//				PathResult path = (PathResult) result;
//				displayOneResult(path, type);
//			}
//		} catch (Exception e) {
//			showError("Error", "Failed to find path: " + e.getMessage());
//			e.printStackTrace();
//		}
//	}
//
//	private void displayOneResult(PathResult result, int type) {
//		if (result.getPath().isEmpty()) {
//			resultArea.setText("❌ NO PATH FOUND!\n\n" + "The destination is not reachable from the source node.\n"
//					+ "There is no connection between these two nodes in the graph.");
//			distLabel.setText("📏 Total Distance: ∞");
//			timeLabel.setText("⏱️ Total Time: ∞");
//			return;
//		}
//
//		StringBuilder sb = new StringBuilder();
//		sb.append("✅ PATH FOUND SUCCESSFULLY!\n");
//		sb.append("═".repeat(70)).append("\n\n");
//
//		String optType = (type == 1) ? "SHORTEST DISTANCE" : "MINIMUM TIME";
//		sb.append("🎯 Optimization: ").append(optType).append("\n\n");
//
//		List<String> path = result.getPath();
//		sb.append("📍 Path (").append(path.size()).append(" nodes):\n\n");
//
//		// عرض المسار بشكل جميل
//		sb.append("   ");
//		for (int i = 0; i < path.size(); i++) {
//			sb.append(path.get(i));
//			if (i < path.size() - 1) {
//				sb.append("  →  ");
//				// سطر جديد كل 5 nodes
//				if ((i + 1) % 5 == 0) {
//					sb.append("\n   ");
//				}
//			}
//		}
//
//		sb.append("\n\n");
//		sb.append("═".repeat(70)).append("\n\n");
//		sb.append(String.format("📏 Total Distance: %.2f km\n", result.getTotalDistance()));
//		sb.append(String.format("⏱️  Total Time: %.2f minutes\n", result.getTotalTime()));
//
//		resultArea.setText(sb.toString());
//		distLabel.setText(String.format("📏 Total Distance: %.2f km", result.getTotalDistance()));
//		timeLabel.setText(String.format("⏱️ Total Time: %.2f min", result.getTotalTime()));
//	}
//
//	private void displayTwoResults(Dijkstra.TwoResults two) {
//		PathResult byDist = two.getByDistance();
//		PathResult byTime = two.getByTime();
//
//		StringBuilder sb = new StringBuilder();
//		sb.append("🔀 COMPARISON MODE - BOTH OPTIMIZATIONS\n");
//		sb.append("═".repeat(70)).append("\n\n");
//
//		// Distance Optimization
//		sb.append("📏 OPTIMIZATION BY DISTANCE:\n");
//		sb.append("─".repeat(70)).append("\n");
//
//		if (!byDist.getPath().isEmpty()) {
//			sb.append("Path: ");
//			List<String> distPath = byDist.getPath();
//			for (int i = 0; i < distPath.size(); i++) {
//				sb.append(distPath.get(i));
//				if (i < distPath.size() - 1) {
//					sb.append(" → ");
//					if ((i + 1) % 6 == 0)
//						sb.append("\n      ");
//				}
//			}
//			sb.append("\n");
//			sb.append(String.format("Total Distance: %.2f km\n", byDist.getTotalDistance()));
//			sb.append(String.format("Total Time: %.2f minutes\n", byDist.getTotalTime()));
//		} else {
//			sb.append("❌ No path found\n");
//		}
//
//		sb.append("\n").append("═".repeat(70)).append("\n\n");
//
//		// Time Optimization
//		sb.append("⏱️  OPTIMIZATION BY TIME:\n");
//		sb.append("─".repeat(70)).append("\n");
//
//		if (!byTime.getPath().isEmpty()) {
//			sb.append("Path: ");
//			List<String> timePath = byTime.getPath();
//			for (int i = 0; i < timePath.size(); i++) {
//				sb.append(timePath.get(i));
//				if (i < timePath.size() - 1) {
//					sb.append(" → ");
//					if ((i + 1) % 6 == 0)
//						sb.append("\n      ");
//				}
//			}
//			sb.append("\n");
//			sb.append(String.format("Total Distance: %.2f km\n", byTime.getTotalDistance()));
//			sb.append(String.format("Total Time: %.2f minutes\n", byTime.getTotalTime()));
//		} else {
//			sb.append("❌ No path found\n");
//		}
//
//		sb.append("\n").append("═".repeat(70)).append("\n\n");
//
//		// Comparison
//		if (!byDist.getPath().isEmpty() && !byTime.getPath().isEmpty()) {
//			double distDiff = Math.abs(byDist.getTotalDistance() - byTime.getTotalDistance());
//			double timeDiff = Math.abs(byDist.getTotalTime() - byTime.getTotalTime());
//
//			sb.append("📊 COMPARISON:\n");
//			sb.append(String.format("   Distance Difference: %.2f km\n", distDiff));
//			sb.append(String.format("   Time Difference: %.2f minutes\n", timeDiff));
//
//			distLabel.setText(String.format("📏 Dist Optimization: %.2f km", byDist.getTotalDistance()));
//			timeLabel.setText(String.format("⏱️ Time Optimization: %.2f min", byTime.getTotalTime()));
//		}
//
//		resultArea.setText(sb.toString());
//	}
//
//	private void clear() {
//		sourceCombo.setValue(null);
//		destCombo.setValue(null);
//		distanceRadio.setSelected(true);
//		resultArea.clear();
//		distLabel.setText("📏 Total Distance: --");
//		timeLabel.setText("⏱️ Total Time: --");
//	}
//
//	private void showWarning(String message) {
//		Alert alert = new Alert(Alert.AlertType.WARNING);
//		alert.setTitle("Warning");
//		alert.setHeaderText(null);
//		alert.setContentText(message);
//		styleAlert(alert);
//		alert.showAndWait();
//	}
//
//	private void showError(String title, String message) {
//		Alert alert = new Alert(Alert.AlertType.ERROR);
//		alert.setTitle(title);
//		alert.setHeaderText(null);
//		alert.setContentText(message);
//		styleAlert(alert);
//		alert.showAndWait();
//	}
//
//	private void styleAlert(Alert alert) {
//		DialogPane dialogPane = alert.getDialogPane();
//		dialogPane.setStyle("-fx-background-color: #2d3142; " + "-fx-font-size: 14px;");
//		dialogPane.lookupAll(".content").forEach(node -> node.setStyle("-fx-text-fill: white;"));
//	}
//
//	public static void main(String[] args) {
//		launch(args);
//	}
//}

//.root {
//    -fx-background-color: #1a1d29;
//}
//
//.combo-box {
//    -fx-background-color: #1a1d29;
//    -fx-border-color: #4f5d75;
//    -fx-border-width: 2px;
//    -fx-border-radius: 5px;
//    -fx-background-radius: 5px;
//}
//
//.combo-box .list-cell {
//    -fx-background-color: #1a1d29;
//    -fx-text-fill: white;
//    -fx-font-size: 14px;
//    -fx-padding: 8px;
//}
//
//.combo-box .list-cell:hover {
//    -fx-background-color: #4f5d75;
//}
//
//.combo-box .list-cell:selected {
//    -fx-background-color: #00b4d8;
//}
//
//.combo-box:focused {
//    -fx-border-color: #00b4d8;
//}
//
//.combo-box .arrow-button {
//    -fx-background-color: #4f5d75;
//}
//
//.combo-box .arrow {
//    -fx-background-color: white;
//}
//
//.combo-box-popup .list-view {
//    -fx-background-color: #1a1d29;
//    -fx-border-color: #4f5d75;
//    -fx-border-width: 2px;
//}
//
//.radio-button {
//    -fx-text-fill: white;
//    -fx-font-size: 14px;
//    -fx-padding: 5px;
//}
//
//.radio-button .radio {
//    -fx-background-color: transparent;
//    -fx-border-color: #00b4d8;
//    -fx-border-width: 2px;
//    -fx-border-radius: 10px;
//    -fx-background-radius: 10px;
//    -fx-padding: 5px;
//}
//
//.radio-button:selected .radio {
//    -fx-background-color: #00b4d8;
//    -fx-border-color: #00b4d8;
//}
//
//.radio-button .dot {
//    -fx-background-color: white;
//    -fx-background-radius: 50%;
//}
//
//.find-btn {
//    -fx-background-color: #00b4d8;
//    -fx-text-fill: white;
//    -fx-font-weight: bold;
//    -fx-font-size: 14px;
//    -fx-padding: 12px 20px;
//    -fx-background-radius: 5px;
//    -fx-cursor: hand;
//}
//
//.find-btn:hover {
//    -fx-background-color: #0096c7;
//}
//
//.find-btn:pressed {
//    -fx-background-color: #023e8a;
//    -fx-scale-x: 0.98;
//    -fx-scale-y: 0.98;
//}
//
//.clear-btn {
//    -fx-background-color: #ef476f;
//    -fx-text-fill: white;
//    -fx-font-weight: bold;
//    -fx-font-size: 14px;
//    -fx-padding: 12px 20px;
//    -fx-background-radius: 5px;
//    -fx-cursor: hand;
//}
//
//.clear-btn:hover {
//    -fx-background-color: #d62828;
//}
//
//.clear-btn:pressed {
//    -fx-background-color: #9d0208;
//    -fx-scale-x: 0.98;
//    -fx-scale-y: 0.98;
//}
//
//.result-area {
//    -fx-control-inner-background: #1a1d29;
//    -fx-border-color: #4f5d75;
//    -fx-border-width: 2px;
//    -fx-border-radius: 5px;
//}
//
//.result-area .content {
//    -fx-background-color: #1a1d29;
//    -fx-text-fill: white !important;
//    -fx-font-size: 14px !important;
//    -fx-font-weight: bold !important;
//}
//
//.result-area .text {
//    -fx-fill: white !important;
//}
//
//.separator .line {
//    -fx-border-color: #4f5d75;
//    -fx-border-width: 1px;
//}
//
//.scroll-bar {
//    -fx-background-color: #2d3142;
//}
//
//.scroll-bar .thumb {
//    -fx-background-color: #4f5d75;
//}
//
//.scroll-bar .thumb:hover {
//    -fx-background-color: #00b4d8;
//}
//
//package application;
//
//import javafx.application.Application;
//import javafx.geometry.Insets;
//import javafx.geometry.Pos;
//import javafx.scene.Scene;
//import javafx.scene.control.*;
//import javafx.scene.layout.*;
//import javafx.scene.shape.Circle;
//import javafx.scene.shape.Line;
//import javafx.stage.Stage;
//import javafx.geometry.Point2D;
//import java.io.FileNotFoundException;
//import java.util.List;
//
//@SuppressWarnings("unused")
//public class Main extends Application {
//
//	private Graph graph;
//	private Dijkstra dijkstra;
//	private ComboBox<String> sourceCombo;
//	private ComboBox<String> destCombo;
//	private RadioButton distanceRadio;
//	private RadioButton timeRadio;
//	private RadioButton bothRadio;
//	private TextArea resultArea;
//	private Label distLabel;
//	private Label timeLabel;
//	private Pane graphPane;
//
//	@Override
//	public void start(Stage stage) {
//		try {
//			// تحميل الملف
//			graph = new Graph();
//			graph.readFromFile("graph_data.txt");
//			dijkstra = new Dijkstra();
//
//			BorderPane root = new BorderPane();
//			root.setStyle("-fx-background-color: #1a1d29;");
//
//			// Panel اليسار
//			VBox leftPanel = makeLeftPanel();
//			root.setLeft(leftPanel);
//
//			// رسم الجراف في الوسط
//			VBox centerPanel = makeGraphPanel();
//			root.setCenter(centerPanel);
//
//			Scene scene = new Scene(root);
//			scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
//
//			stage.setTitle("Dijkstra Shortest Path");
//			stage.setScene(scene);
//			stage.setMaximized(true);
//			stage.show();
//
//		} catch (Exception e) {
//			showError("Error", e.getMessage());
//		}
//	}
//
//	private VBox makeLeftPanel() {
//		VBox vbox = new VBox(15);
//		vbox.setPadding(new Insets(20));
//		vbox.setPrefWidth(380);
//		vbox.setStyle("-fx-background-color: #2d3142;");
//
//		Label title = new Label("Dijkstra Algorithm");
//		title.setStyle("-fx-text-fill: #00b4d8; -fx-font-size: 18px; -fx-font-weight: bold;");
//
//		// Source input
//		Label srcLabel = new Label("Source Node:");
//		srcLabel.setStyle("-fx-text-fill: white; -fx-font-size: 14px;");
//
//		sourceCombo = new ComboBox<>();
//		sourceCombo.setEditable(true);
//		sourceCombo.setPromptText("Select or type source");
//		sourceCombo.getItems().addAll(graph.getAllNodes());
//		sourceCombo.setPrefWidth(Double.MAX_VALUE);
//		if (graph.getSourceNode() != null) {
//			sourceCombo.setValue(graph.getSourceNode());
//		}
//
//		// Destination input
//		Label destLabel = new Label("Destination Node:");
//		destLabel.setStyle("-fx-text-fill: white; -fx-font-size: 14px;");
//
//		destCombo = new ComboBox<>();
//		destCombo.setEditable(true);
//		destCombo.setPromptText("Select or type destination");
//		destCombo.getItems().addAll(graph.getAllNodes());
//		destCombo.setPrefWidth(Double.MAX_VALUE);
//		if (graph.getDestinationNode() != null) {
//			destCombo.setValue(graph.getDestinationNode());
//		}
//
//		Separator sep1 = new Separator();
//
//		// Radio buttons
//		Label optLabel = new Label("Optimization:");
//		optLabel.setStyle("-fx-text-fill: white; -fx-font-size: 14px;");
//
//		ToggleGroup group = new ToggleGroup();
//		distanceRadio = new RadioButton("Distance");
//		timeRadio = new RadioButton("Time");
//		bothRadio = new RadioButton("Both");
//
//		distanceRadio.setToggleGroup(group);
//		timeRadio.setToggleGroup(group);
//		bothRadio.setToggleGroup(group);
//
//		int opt = graph.getOptimizationType();
//		if (opt == 1)
//			distanceRadio.setSelected(true);
//		else if (opt == 2)
//			timeRadio.setSelected(true);
//		else
//			bothRadio.setSelected(true);
//
//		Separator sep2 = new Separator();
//
//		// Buttons
//		HBox btnBox = new HBox(10);
//		Button findBtn = new Button("Find Path");
//		Button clearBtn = new Button("Clear");
//
//		findBtn.getStyleClass().add("find-btn");
//		clearBtn.getStyleClass().add("clear-btn");
//
//		findBtn.setOnAction(e -> findPath());
//		clearBtn.setOnAction(e -> clear());
//
//		findBtn.setMaxWidth(Double.MAX_VALUE);
//		clearBtn.setMaxWidth(Double.MAX_VALUE);
//		HBox.setHgrow(findBtn, Priority.ALWAYS);
//		HBox.setHgrow(clearBtn, Priority.ALWAYS);
//
//		btnBox.getChildren().addAll(findBtn, clearBtn);
//
//		Separator sep3 = new Separator();
//
//		// Results
//		Label resLabel = new Label("Results:");
//		resLabel.setStyle("-fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold;");
//
//		resultArea = new TextArea();
//		resultArea.setPromptText("Results will show here...");
//		resultArea.setEditable(false);
//		resultArea.setWrapText(true);
//		resultArea.setPrefRowCount(12);
//		resultArea.setStyle(
//				"-fx-control-inner-background: #1a1d29; -fx-border-color: #4f5d75; -fx-border-width: 2px;");
//
//		VBox.setVgrow(resultArea, Priority.ALWAYS);
//
//		distLabel = new Label("Total Distance: --");
//		distLabel.setStyle("-fx-text-fill: #00b4d8; -fx-font-size: 13px;");
//
//		timeLabel = new Label("Total Time: --");
//		timeLabel.setStyle("-fx-text-fill: #ffd60a; -fx-font-size: 13px;");
//
//		vbox.getChildren().addAll(title, srcLabel, sourceCombo, destLabel, destCombo, sep1, optLabel, distanceRadio,
//				timeRadio, bothRadio, sep2, btnBox, sep3, resLabel, resultArea, distLabel, timeLabel);
//
//		return vbox;
//	}
//
//	private VBox makeGraphPanel() {
//		VBox vbox = new VBox(10);
//		vbox.setPadding(new Insets(20));
//		vbox.setAlignment(Pos.CENTER);
//
//		Label graphLabel = new Label("Graph Visualization");
//		graphLabel.setStyle("-fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold;");
//
//		ScrollPane scroll = new ScrollPane();
//		scroll.setFitToWidth(true);
//		scroll.setFitToHeight(true);
//
//		graphPane = new Pane();
//		graphPane.setPrefSize(1000, 800);
//		graphPane.setStyle("-fx-background-color: #1a1d29;");
//
//		scroll.setContent(graphPane);
//		VBox.setVgrow(scroll, Priority.ALWAYS);
//
//		drawGraph();
//
//		vbox.getChildren().addAll(graphLabel, scroll);
//		return vbox;
//	}
//
//	private void drawGraph() {
//	    graphPane.getChildren().clear();
//
//	    // رسم الخطوط
//	    for (String node : graph.getAllNodes()) {
//	        Point2D from = graph.getNodePosition(node);
//	        if (from == null) continue;
//
//	        for (Edge edge : graph.getAdjecant(node)) {
//	            String dest = edge.getDestination();
//	            Point2D to = graph.getNodePosition(dest);
//	            if (to == null) continue;
//
//	            // لتقليل تكرار الخطوط (مفيد لو كان عندك اتجاهين)
//	            if (node.compareTo(dest) > 0) continue;
//
//	            Line line = new Line(from.getX(), from.getY(), to.getX(), to.getY());
//	            line.setStroke(javafx.scene.paint.Color.web("#4f5d75"));
//	            line.setStrokeWidth(1.5);
//	            graphPane.getChildren().add(line);
//	        }
//	    }
//
//	    // رسم النقاط والأسماء (بعد الخطوط عشان يظهروا فوق)
//	    for (String nodeName : graph.getAllNodes()) {
//	        Point2D pos = graph.getNodePosition(nodeName);
//	        if (pos == null) continue;
//
//	        Circle circle = new Circle(pos.getX(), pos.getY(), 6);
//	        circle.setFill(javafx.scene.paint.Color.web("#4f5d75"));
//	        circle.setStroke(javafx.scene.paint.Color.web("#00b4d8"));
//	        circle.setStrokeWidth(2);
//
//	        Label label = new Label(nodeName);
//	        label.setLayoutX(pos.getX() - 15);
//	        label.setLayoutY(pos.getY() - 25);
//	        label.setStyle(
//	            "-fx-text-fill: white; -fx-font-size: 11px; -fx-font-weight: bold;" +
//	            " -fx-background-color: rgba(26, 29, 41, 0.9);" +
//	            " -fx-padding: 2px 5px; -fx-background-radius: 3px;"
//	        );
//
//	        circle.setOnMouseClicked(e -> {
//	            if (sourceCombo.getValue() == null || sourceCombo.getValue().isEmpty()) {
//	                sourceCombo.setValue(nodeName);
//	            } else if (destCombo.getValue() == null || destCombo.getValue().isEmpty()) {
//	                destCombo.setValue(nodeName);
//	            }
//	        });
//
//	        graphPane.getChildren().addAll(circle, label);
//	    }
//	}
//
//
//	private void findPath() {
//		String source = sourceCombo.getValue();
//		String dest = destCombo.getValue();
//
//		if (source == null || source.isEmpty() || dest == null || dest.isEmpty()) {
//			showError("Error", "Please select source and destination");
//			return;
//		}
//
//		if (!graph.hasNode(source) || !graph.hasNode(dest)) {
//			showError("Error", "Node not found in graph");
//			return;
//		}
//
//		if (source.equals(dest)) {
//			showError("Error", "Source and destination are same");
//			return;
//		}
//
//		int type = 1;
//		if (distanceRadio.isSelected())
//			type = 1;
//		else if (timeRadio.isSelected())
//			type = 2;
//		else if (bothRadio.isSelected())
//			type = 3;
//
//		Object result = dijkstra.findShortestPath(graph, source, dest, type);
//
//		if (type == 3) {
//			Dijkstra.TwoResults two = (Dijkstra.TwoResults) result;
//			showTwoResults(two);
//		} else {
//			PathResult path = (PathResult) result;
//			showOneResult(path, type);
//			highlightPath(path.getPath());
//		}
//	}
//
//	private void showOneResult(PathResult result, int type) {
//		if (result.getPath().isEmpty()) {
//			resultArea.setText("No path found!");
//			distLabel.setText("Total Distance: infinity");
//			timeLabel.setText("Total Time: infinity");
//			return;
//		}
//
//		StringBuilder sb = new StringBuilder();
//		sb.append("Path Found!\n\n");
//		sb.append("Optimized by: ").append(type == 1 ? "Distance" : "Time").append("\n\n");
//
//		List<String> path = result.getPath();
//		sb.append("Path: ");
//		for (int i = 0; i < path.size(); i++) {
//			sb.append(path.get(i));
//			if (i < path.size() - 1)
//				sb.append(" -> ");
//		}
//
//		resultArea.setText(sb.toString());
//		distLabel.setText(String.format("Total Distance: %.2f km", result.getTotalDistance()));
//		timeLabel.setText(String.format("Total Time: %.2f min", result.getTotalTime()));
//	}
//
//	private void showTwoResults(Dijkstra.TwoResults two) {
//		PathResult byDist = two.getByDistance();
//		PathResult byTime = two.getByTime();
//
//		StringBuilder sb = new StringBuilder();
//		sb.append("Comparison Mode\n\n");
//
//		sb.append("By Distance:\n");
//		if (!byDist.getPath().isEmpty()) {
//			sb.append("Path: ");
//			for (int i = 0; i < byDist.getPath().size(); i++) {
//				sb.append(byDist.getPath().get(i));
//				if (i < byDist.getPath().size() - 1)
//					sb.append(" -> ");
//			}
//			sb.append(String.format("\nDistance: %.2f km\n", byDist.getTotalDistance()));
//			sb.append(String.format("Time: %.2f min\n\n", byDist.getTotalTime()));
//		} else {
//			sb.append("No path\n\n");
//		}
//
//		sb.append("By Time:\n");
//		if (!byTime.getPath().isEmpty()) {
//			sb.append("Path: ");
//			for (int i = 0; i < byTime.getPath().size(); i++) {
//				sb.append(byTime.getPath().get(i));
//				if (i < byTime.getPath().size() - 1)
//					sb.append(" -> ");
//			}
//			sb.append(String.format("\nDistance: %.2f km\n", byTime.getTotalDistance()));
//			sb.append(String.format("Time: %.2f min\n", byTime.getTotalTime()));
//		} else {
//			sb.append("No path\n");
//		}
//
//		resultArea.setText(sb.toString());
//
//		if (!byDist.getPath().isEmpty()) {
//			distLabel.setText(String.format("Distance: %.2f km", byDist.getTotalDistance()));
//			timeLabel.setText(String.format("Time: %.2f min", byDist.getTotalTime()));
//			highlightPath(byDist.getPath());
//		}
//	}
//
//	private void highlightPath(List<String> path) {
//		if (path == null || path.isEmpty())
//			return;
//
//		drawGraph();
//
//		// رسم المسار
//		for (int i = 0; i < path.size() - 1; i++) {
//			String from = path.get(i);
//			String to = path.get(i + 1);
//
//			Point2D fromPos = graph.getNodePosition(from);
//			Point2D toPos = graph.getNodePosition(to);
//
//			if (fromPos != null && toPos != null) {
//				Line line = new Line(fromPos.getX(), fromPos.getY(), toPos.getX(), toPos.getY());
//				line.setStroke(javafx.scene.paint.Color.web("#06ffa5"));
//				line.setStrokeWidth(4);
//				graphPane.getChildren().add(line);
//			}
//		}
//
//		// تلوين النقاط
//		for (int i = 0; i < path.size(); i++) {
//			String nodeName = path.get(i);
//			Point2D pos = graph.getNodePosition(nodeName);
//
//			if (pos != null) {
//				Circle circle = new Circle(pos.getX(), pos.getY(), 8);
//
//				if (i == 0) {
//					circle.setFill(javafx.scene.paint.Color.web("#00b4d8"));
//				} else if (i == path.size() - 1) {
//					circle.setFill(javafx.scene.paint.Color.web("#ef476f"));
//				} else {
//					circle.setFill(javafx.scene.paint.Color.web("#06ffa5"));
//				}
//				circle.setStroke(javafx.scene.paint.Color.WHITE);
//				circle.setStrokeWidth(2);
//
//				Label label = new Label(nodeName);
//				label.setLayoutX(pos.getX() - 10);
//				label.setLayoutY(pos.getY() - 25);
//				label.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 11px;");
//
//				graphPane.getChildren().addAll(circle, label);
//			}
//		}
//	}
//
//	private void clear() {
//		sourceCombo.setValue(null);
//		destCombo.setValue(null);
//		resultArea.clear();
//		distLabel.setText("Total Distance: --");
//		timeLabel.setText("Total Time: --");
//		drawGraph();
//	}
//
//	private void showError(String title, String msg) {
//		Alert alert = new Alert(Alert.AlertType.ERROR);
//		alert.setTitle(title);
//		alert.setContentText(msg);
//		alert.showAndWait();
//	}
//
//	public static void main(String[] args) {
//		launch(args);
//	}
//}
/////////////////////////////////////////////////////////////////////////////////////////////////////////
//package application;
//
//import javafx.application.Application;
//import javafx.geometry.Insets;
//import javafx.geometry.Pos;
//import javafx.scene.Scene;
//import javafx.scene.control.*;
//import javafx.scene.layout.*;
//import javafx.scene.paint.Color;
//import javafx.scene.shape.Circle;
//import javafx.scene.shape.Line;
//import javafx.stage.Stage;
//import javafx.geometry.Point2D;
//import java.io.FileNotFoundException;
//import java.util.List;
//
//public class Main extends Application {
//
//	private Graph graph;
//	private Dijkstra dijkstra;
//
//	// UI Components
//	private TextField sourceField;
//	private TextField destField;
//	private ToggleGroup optimizationGroup;
//	private RadioButton distanceRadio;
//	private RadioButton timeRadio;
//	private RadioButton bothRadio;
//	private TextArea resultArea;
//	private Label distanceLabel;
//	private Label timeLabel;
//
//	private Pane graphPane;
//	private static final double NODE_RADIUS = 5;
//
//	@Override
//	public void start(Stage primaryStage) {
//		try {
//			loadGraph();
//
//			// Single Window
//			BorderPane root = new BorderPane();
//			root.getStyleClass().add("root");
//
//			// Left: Control Panel
//			VBox controlPanel = createControlPanel();
//			root.setLeft(controlPanel);
//
//			// Center: Graph Visualization
//			VBox centerBox = createGraphPanel();
//			root.setCenter(centerBox);
//
//			Scene scene = new Scene(root);
//			scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
//
//			primaryStage.setTitle("🗺️ Dijkstra Algorithm - Shortest Path Finder");
//			primaryStage.setScene(scene);
//			primaryStage.setMaximized(true); // Full Screen
//			primaryStage.show();
//
//		} catch (Exception e) {
//			showError("Error loading application", e.getMessage());
//			e.printStackTrace();
//		}
//	}
//
//	private void loadGraph() throws FileNotFoundException {
//		graph = new Graph();
//		graph.readFromFile("graph_data.txt");
//		dijkstra = new Dijkstra();
//		System.out.println("✅ Graph loaded: " + graph.getAllNodes().size() + " nodes");
//	}
//
//	private VBox createControlPanel() {
//		VBox panel = new VBox(15);
//		panel.setPadding(new Insets(20));
//		panel.setPrefWidth(400);
//		panel.getStyleClass().add("control-panel");
//
//		// Title
//		Label titleLabel = new Label("🗺️ Dijkstra Shortest Path");
//		titleLabel.getStyleClass().add("title-label");
//
//		Label instructionLabel = new Label("💡 Enter nodes or click on graph");
//		instructionLabel.getStyleClass().add("instruction-label");
//
//		Separator sep1 = new Separator();
//
//		// Input Section
//		VBox inputBox = createInputSection();
//
//		Separator sep2 = new Separator();
//
//		// Optimization Section
//		VBox optBox = createOptimizationSection();
//
//		Separator sep3 = new Separator();
//
//		// Buttons
//		HBox buttonBox = createButtonSection();
//
//		Separator sep4 = new Separator();
//
//		// Results
//		VBox resultBox = createResultSection();
//
//		panel.getChildren().addAll(titleLabel, instructionLabel, sep1, inputBox, sep2, optBox, sep3, buttonBox, sep4,
//				resultBox);
//
//		return panel;
//	}
//
//	private VBox createInputSection() {
//		VBox box = new VBox(10);
//
//		// Source
//		Label sourceLabel = new Label("📍 Source Node:");
//		sourceLabel.getStyleClass().add("section-label");
//
//		sourceField = new TextField();
//		sourceField.setPromptText("e.g., A");
//		sourceField.getStyleClass().add("text-field");
//
//		// Read from file
//		if (graph.getSourceNode() != null) {
//			sourceField.setText(graph.getSourceNode());
//		}
//
//		// Destination
//		Label destLabel = new Label("🎯 Destination Node:");
//		destLabel.getStyleClass().add("section-label");
//
//		destField = new TextField();
//		destField.setPromptText("e.g., F");
//		destField.getStyleClass().add("text-field");
//
//		// Read from file
//		if (graph.getDestinationNode() != null) {
//			destField.setText(graph.getDestinationNode());
//		}
//
//		box.getChildren().addAll(sourceLabel, sourceField, destLabel, destField);
//		return box;
//	}
//
//	private VBox createOptimizationSection() {
//		VBox box = new VBox(10);
//
//		Label label = new Label("⚙️ Optimization Type:");
//		label.getStyleClass().add("section-label");
//
//		optimizationGroup = new ToggleGroup();
//
//		distanceRadio = new RadioButton("📏 Shortest Distance");
//		timeRadio = new RadioButton("⏱️ Minimum Time");
//		bothRadio = new RadioButton("🔀 Both (Compare)");
//
//		distanceRadio.setToggleGroup(optimizationGroup);
//		timeRadio.setToggleGroup(optimizationGroup);
//		bothRadio.setToggleGroup(optimizationGroup);
//
//		// Read from file
//		int optType = graph.getOptimizationType();
//		if (optType == 1)
//			distanceRadio.setSelected(true);
//		else if (optType == 2)
//			timeRadio.setSelected(true);
//		else if (optType == 3)
//			bothRadio.setSelected(true);
//		else
//			distanceRadio.setSelected(true);
//
//		box.getChildren().addAll(label, distanceRadio, timeRadio, bothRadio);
//		return box;
//	}
//
//	private HBox createButtonSection() {
//		HBox box = new HBox(10);
//		box.setAlignment(Pos.CENTER);
//
//		Button findBtn = new Button("🔍 Find Path");
//		Button clearBtn = new Button("🔄 Clear");
//
//		findBtn.getStyleClass().add("find-button");
//		clearBtn.getStyleClass().add("clear-button");
//
//		findBtn.setOnAction(e -> handleFindPath());
//		clearBtn.setOnAction(e -> handleClear());
//
//		findBtn.setMaxWidth(Double.MAX_VALUE);
//		clearBtn.setMaxWidth(Double.MAX_VALUE);
//
//		HBox.setHgrow(findBtn, Priority.ALWAYS);
//		HBox.setHgrow(clearBtn, Priority.ALWAYS);
//
//		box.getChildren().addAll(findBtn, clearBtn);
//		return box;
//	}
//
//	private VBox createResultSection() {
//		VBox box = new VBox(10);
//
//		Label label = new Label("📊 Results:");
//		label.getStyleClass().add("result-title-label");
//
//		resultArea = new TextArea();
//		resultArea.setPromptText("Path will appear here...");
//		resultArea.setEditable(false);
//		resultArea.setPrefRowCount(15);
//		resultArea.setWrapText(true);
//		resultArea.setStyle("-fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold;");
//		resultArea.getStyleClass().add("result-area");
//
//		VBox.setVgrow(resultArea, Priority.ALWAYS);
//
//		distanceLabel = new Label("📏 Total Distance: --");
//		timeLabel = new Label("⏱️ Total Time: --");
//
//		distanceLabel.getStyleClass().add("distance-label");
//		timeLabel.getStyleClass().add("time-label");
//
//		box.getChildren().addAll(label, resultArea, distanceLabel, timeLabel);
//		return box;
//	}
//
//	private VBox createGraphPanel() {
//		VBox box = new VBox(10);
//		box.setPadding(new Insets(20));
//		box.setAlignment(Pos.CENTER);
//		box.getStyleClass().add("graph-canvas");
//
//		Label graphLabel = new Label("🎨 Graph Visualization");
//		graphLabel.getStyleClass().add("result-title-label");
//
//		ScrollPane scrollPane = new ScrollPane();
//		scrollPane.setFitToWidth(true);
//		scrollPane.setFitToHeight(true);
//		scrollPane.setPannable(true);
//
//		graphPane = new Pane();
//		graphPane.setPrefSize(1000, 800);
//		graphPane.setMinSize(1000, 800);
//
//		scrollPane.setContent(graphPane);
//		VBox.setVgrow(scrollPane, Priority.ALWAYS);
//
//		drawGraph();
//
//		box.getChildren().addAll(graphLabel, scrollPane);
//		return box;
//	}
//
//	private void drawGraph() {
//		graphPane.getChildren().clear();
//
//		// Draw Edges
//		for (String node : graph.getAllNodes()) {
//			Point2D fromPos = graph.getNodePosition(node);
//			if (fromPos == null)
//				continue;
//
//			for (Edge edge : graph.getAdjecant(node)) {
//				String dest = edge.getDestination();
//				Point2D toPos = graph.getNodePosition(dest);
//				if (toPos == null)
//					continue;
//
//				if (node.compareTo(dest) > 0)
//					continue;
//
//				Line line = new Line(fromPos.getX(), fromPos.getY(), toPos.getX(), toPos.getY());
//				line.getStyleClass().add("graph-edge");
//				graphPane.getChildren().add(line);
//			}
//		}
//
//		// Draw Nodes
//		for (String nodeName : graph.getAllNodes()) {
//			Point2D pos = graph.getNodePosition(nodeName);
//			if (pos == null)
//				continue;
//
//			Circle circle = new Circle(pos.getX(), pos.getY(), NODE_RADIUS);
//			circle.getStyleClass().add("graph-node");
//
//			Label label = new Label(nodeName);
//			label.setLayoutX(pos.getX() - 15);
//			label.setLayoutY(pos.getY() - 20);
//			label.getStyleClass().add("node-label");
//
//			circle.setOnMouseEntered(e -> {
//				circle.getStyleClass().add("graph-node-hover");
//				label.getStyleClass().add("node-label-hover");
//			});
//
//			circle.setOnMouseExited(e -> {
//				circle.getStyleClass().remove("graph-node-hover");
//				label.getStyleClass().remove("node-label-hover");
//			});
//
//			circle.setOnMouseClicked(e -> {
//				if (sourceField.getText().trim().isEmpty()) {
//					sourceField.setText(nodeName);
//				} else if (destField.getText().trim().isEmpty()) {
//					destField.setText(nodeName);
//				}
//			});
//
//			graphPane.getChildren().addAll(circle, label);
//		}
//	}
//
//	private void handleFindPath() {
//		try {
//			String source = sourceField.getText().trim();
//			String dest = destField.getText().trim();
//
//			if (source.isEmpty() || dest.isEmpty()) {
//				showWarning("⚠️ Please enter both source and destination");
//				return;
//			}
//
//			if (!graph.hasNode(source)) {
//				showWarning("⚠️ Source node '" + source + "' not found!");
//				return;
//			}
//
//			if (!graph.hasNode(dest)) {
//				showWarning("⚠️ Destination node '" + dest + "' not found!");
//				return;
//			}
//
//			if (source.equals(dest)) {
//				showWarning("⚠️ Source and destination cannot be the same");
//				return;
//			}
//
//			int optType = getOptimizationType();
//			Object result = dijkstra.findShortestPath(graph, source, dest, optType);
//			displayResult(result, optType);
//
//		} catch (Exception e) {
//			showError("❌ Error finding path", e.getMessage());
//			e.printStackTrace();
//		}
//	}
//
//	private void handleClear() {
//		sourceField.clear();
//		destField.clear();
//		distanceRadio.setSelected(true);
//		resultArea.clear();
//		distanceLabel.setText("📏 Total Distance: --");
//		timeLabel.setText("⏱️ Total Time: --");
//		drawGraph();
//	}
//
//	private int getOptimizationType() {
//		if (distanceRadio.isSelected())
//			return 1;
//		if (timeRadio.isSelected())
//			return 2;
//		if (bothRadio.isSelected())
//			return 3;
//		return 1;
//	}
//
//	private void displayResult(Object result, int optType) {
//		if (optType == 3) {
//			Dijkstra.TwoResults two = (Dijkstra.TwoResults) result;
//			displayBothResults(two);
//		} else {
//			PathResult pathResult = (PathResult) result;
//			displaySingleResult(pathResult, optType == 1 ? "DISTANCE" : "TIME");
//			highlightPath(pathResult.getPath());
//		}
//	}
//
//	private void displaySingleResult(PathResult result, String type) {
//		if (result.getPath().isEmpty()) {
//			resultArea.setText("❌ No path found!\n\nThe destination is not reachable from the source.");
//			distanceLabel.setText("📏 Total Distance: ∞");
//			timeLabel.setText("⏱️ Total Time: ∞");
//			return;
//		}
//
//		StringBuilder sb = new StringBuilder();
//		sb.append("✅ Path Found!\n");
//		sb.append("═".repeat(40)).append("\n\n");
//		sb.append("🎯 Optimized by: ").append(type).append("\n\n");
//		sb.append("📍 Path (").append(result.getPath().size()).append(" nodes):\n");
//
//		List<String> path = result.getPath();
//		for (int i = 0; i < path.size(); i++) {
//			sb.append(path.get(i));
//			if (i < path.size() - 1) {
//				sb.append(" → ");
//				if ((i + 1) % 5 == 0)
//					sb.append("\n");
//			}
//		}
//
//		resultArea.setText(sb.toString());
//		distanceLabel.setText(String.format("📏 Total Distance: %.2f km", result.getTotalDistance()));
//		timeLabel.setText(String.format("⏱️ Total Time: %.2f min", result.getTotalTime()));
//	}
//
//	private void displayBothResults(Dijkstra.TwoResults two) {
//		PathResult byDist = two.getByDistance();
//		PathResult byTime = two.getByTime();
//
//		StringBuilder sb = new StringBuilder();
//		sb.append("🔀 COMPARISON MODE\n");
//		sb.append("═".repeat(40)).append("\n\n");
//
//		sb.append("📏 DISTANCE OPTIMIZATION:\n");
//		if (!byDist.getPath().isEmpty()) {
//			sb.append("Path: ");
//			appendPath(sb, byDist.getPath());
//			sb.append(String.format("Distance: %.2f km\n", byDist.getTotalDistance()));
//			sb.append(String.format("Time: %.2f min\n", byDist.getTotalTime()));
//		} else {
//			sb.append("No path found\n");
//		}
//
//		sb.append("\n").append("─".repeat(40)).append("\n\n");
//
//		sb.append("⏱️ TIME OPTIMIZATION:\n");
//		if (!byTime.getPath().isEmpty()) {
//			sb.append("Path: ");
//			appendPath(sb, byTime.getPath());
//			sb.append(String.format("Distance: %.2f km\n", byTime.getTotalDistance()));
//			sb.append(String.format("Time: %.2f min\n", byTime.getTotalTime()));
//		} else {
//			sb.append("No path found\n");
//		}
//
//		resultArea.setText(sb.toString());
//
//		if (!byDist.getPath().isEmpty() && !byTime.getPath().isEmpty()) {
//			double distDiff = Math.abs(byDist.getTotalDistance() - byTime.getTotalDistance());
//			double timeDiff = Math.abs(byDist.getTotalTime() - byTime.getTotalTime());
//
//			distanceLabel.setText(String.format("📏 Distance diff: %.2f km", distDiff));
//			timeLabel.setText(String.format("⏱️ Time diff: %.2f min", timeDiff));
//
//			highlightPath(byDist.getPath());
//		}
//	}
//
//	private void appendPath(StringBuilder sb, List<String> path) {
//		for (int i = 0; i < path.size(); i++) {
//			sb.append(path.get(i));
//			if (i < path.size() - 1)
//				sb.append(" → ");
//			if ((i + 1) % 6 == 0)
//				sb.append("\n     ");
//		}
//		sb.append("\n");
//	}
//
//	private void highlightPath(List<String> path) {
//		if (path == null || path.isEmpty())
//			return;
//
//		drawGraph();
//
//		// Draw Path Lines
//		for (int i = 0; i < path.size() - 1; i++) {
//			String from = path.get(i);
//			String to = path.get(i + 1);
//
//			Point2D fromPos = graph.getNodePosition(from);
//			Point2D toPos = graph.getNodePosition(to);
//
//			if (fromPos != null && toPos != null) {
//				Line line = new Line(fromPos.getX(), fromPos.getY(), toPos.getX(), toPos.getY());
//				line.getStyleClass().add("path-line");
//				graphPane.getChildren().add(line);
//			}
//		}
//
//		// Highlight Path Nodes
//		for (int i = 0; i < path.size(); i++) {
//			String nodeName = path.get(i);
//			Point2D pos = graph.getNodePosition(nodeName);
//
//			if (pos != null) {
//				Circle circle = new Circle(pos.getX(), pos.getY(), 8);
//
//				if (i == 0) {
//					circle.getStyleClass().add("source-node");
//				} else if (i == path.size() - 1) {
//					circle.getStyleClass().add("destination-node");
//				} else {
//					circle.getStyleClass().add("path-node");
//				}
//
//				Label label = new Label(nodeName);
//				label.setLayoutX(pos.getX() - 15);
//				label.setLayoutY(pos.getY() - 25);
//				label.getStyleClass().add("path-node-label");
//
//				graphPane.getChildren().addAll(circle, label);
//			}
//		}
//	}
//
//	private void showWarning(String message) {
//		Alert alert = new Alert(Alert.AlertType.WARNING);
//		alert.setTitle("Warning");
//		alert.setHeaderText(null);
//		alert.setContentText(message);
//		alert.showAndWait();
//	}
//
//	private void showError(String title, String message) {
//		Alert alert = new Alert(Alert.AlertType.ERROR);
//		alert.setTitle(title);
//		alert.setHeaderText(null);
//		alert.setContentText(message);
//		alert.showAndWait();
//	}
//
//	public static void main(String[] args) {
//		launch(args);
//	}
//}