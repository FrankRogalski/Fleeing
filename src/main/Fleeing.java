package main;

import java.util.ArrayList;
import java.util.Random;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Duration;

public class Fleeing extends Application{
	private Canvas can;
	private GraphicsContext gc;
	
	private Timeline tl_draw;
	private ArrayList<Viecher> viechzeugs = new ArrayList<Viecher>();
	private final Random r = new Random();
	
	private boolean paused = false;
	
	public static void main(String[] args) {
		launch(args);
	}
	
	@Override
	public void init() throws Exception {
		tl_draw = new Timeline(new KeyFrame(Duration.millis(1000/60), e -> {
			draw();
		}));
		tl_draw.setCycleCount(Timeline.INDEFINITE);
		tl_draw.play();
	}
	
	@Override
	public void start(Stage stage) throws Exception {
		Pane root = new Pane();
		Scene scene = new Scene(root, 700, 400);
		
		stage.setTitle("Sample Text");
		
		can = new Canvas(scene.getWidth(), scene.getHeight());
		gc = can.getGraphicsContext2D();
		
		root.getChildren().add(can);
//		root.setStyle("-fx-background-color: #000000");
		
		scene.setOnMouseClicked(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent e) {
				final double size = r.nextInt(76) + 25;
				
				if (MouseButton.PRIMARY == e.getButton()) {
					viechzeugs.add(new Viecher(gc, e.getX(), e.getY(), size, "prey"));
				} else if (MouseButton.SECONDARY == e.getButton()) {
					viechzeugs.add(new Viecher(gc, e.getX(), e.getY(), size, "hunter"));
				}
			}
		});
		
		scene.addEventFilter(KeyEvent.KEY_PRESSED, e -> {
			if(e.getCode() == KeyCode.P) {
				if(paused) {
					tl_draw.play();
				} else {
					tl_draw.stop();
				}
				paused = !paused;
			}
		});
		
		scene.widthProperty().addListener((obsv, oldVal, newVal) -> {
		   can.setWidth(newVal.doubleValue());
		});
		
		scene.heightProperty().addListener((obsv, oldVal, newVal) -> {
			can.setHeight(newVal.doubleValue());
		});
		
		stage.setScene(scene);
		stage.show();
		
		//setup
	}
	
	private void draw() {
		gc.clearRect(0, 0, can.getWidth(), can.getHeight());
		drawLines();
		
		for (int i = viechzeugs.size() - 1;i >= 0;i--) {
			if (viechzeugs.get(i).lookAround(viechzeugs)) {
				viechzeugs.get(i).update();
				viechzeugs.get(i).show();
				Viecher kind = viechzeugs.get(i).getKinder();
				if (kind != null) {
					viechzeugs.add(kind);
				}
			} else {
				viechzeugs.remove(i);
			}
		}
	}
	
	public void drawLines() {
		gc.setFill(Color.BLACK);
		gc.strokeLine(can.getWidth() / 10, can.getHeight() / 10, can.getWidth() / 10 * 9, can.getHeight() / 10); //oben
		gc.strokeLine(can.getWidth() / 10 * 9, can.getHeight() / 10, can.getWidth() / 10 * 9, can.getHeight() / 10 * 9); //rechts
		gc.strokeLine(can.getWidth() / 10 * 9, can.getHeight() / 10 * 9, can.getWidth() / 10, can.getHeight() / 10 * 9); //unten
		gc.strokeLine(can.getWidth() / 10, can.getHeight() / 10 * 9, can.getWidth() / 10, can.getHeight() / 10); //links
	}
}