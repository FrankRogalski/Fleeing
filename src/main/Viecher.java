package main;

import java.util.ArrayList;
import java.util.Random;

import javafx.geometry.Point2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class Viecher {
	private Physics phys;
	private GraphicsContext gc;
	private final Color color;
	private String kind;
	private int lifespan;
	
	private double size, maxSight, maxViewAngle, foresight, maxLifespan = 60 * 60;
	
	/**
	 * Zu Lösende Probleme
	 * 1. Perlin noise
	 * 3. Anderen Weg finden, wenn zu lange in einer Ecke
	 * */
	
	public Viecher(GraphicsContext gc, double x, double y, double size, String kind) {
		
		phys = new Physics(x, y, map(size, 25, 100, 4, 1), map(size, 25, 100, 0.05, 0.005));
		
		if (kind == "hunter") {
			color = Color.RED;
			lifespan = (int) map(size, 25, 100, 0, maxLifespan);
		} else {
			color = Color.GREEN;
			lifespan = -1;
		}
		
		maxSight = map(size, 25, 100, 750, 250);
		maxViewAngle = map(size, 25, 100, 60, 20);
		foresight = map(size, 25, 100, 50, 200);
		
		this.gc = gc;
		this.size = size;
		this.kind = kind;
		
		phys.seek(gc.getCanvas().getWidth(), gc.getCanvas().getHeight());
	}
	
	private double sign (Point2D p1, Point2D p2, Point2D p3) {
	    return (p1.getX() - p3.getX()) * (p2.getY() - p3.getY()) - (p2.getX() - p3.getX()) * (p1.getY() - p3.getY());
	}

	private boolean PointInTriangle (Point2D pt, Point2D v1, Point2D v2, Point2D v3) {
	    boolean b1, b2, b3;

	    b1 = sign(pt, v1, v2) < 0.0;
	    b2 = sign(pt, v2, v3) < 0.0;
	    b3 = sign(pt, v3, v1) < 0.0;

	    return ((b1 == b2) && (b2 == b3));
	}
	
	private boolean isVisible(Point2D loc) {
		if (kind == "hunter") {
			final Point2D vel = phys.getVelocity();
			Point2D vel2 = new Point2D(vel.getX(), vel.getY());
			
			Point2D ecke1 = Physics.rotatePoint(0, 0, maxViewAngle, vel2);
			
			ecke1 = ecke1.normalize();
			ecke1 = ecke1.multiply(maxSight);
			ecke1 = phys.getLocation().add(ecke1);
			
			Point2D ecke2 = Physics.rotatePoint(0, 0, -maxViewAngle, vel);
			
			ecke2 = ecke2.normalize();
			ecke2 = ecke2.multiply(maxSight);
			ecke2 = phys.getLocation().add(ecke2);
			
			gc.strokeLine(phys.getLocation().getX(), 
					phys.getLocation().getY(), 
					ecke1.getX(), 
					ecke1.getY());
			
			gc.strokeLine(ecke1.getX(), 
					ecke1.getY(), 
					ecke2.getX(), 
					ecke2.getY());
			
			gc.strokeLine(ecke2.getX(), 
					ecke2.getY(), 
					phys.getLocation().getX(), 
					phys.getLocation().getY());
			
			return PointInTriangle(loc, phys.getLocation(), ecke1, ecke2);
		}
		
		return (phys.distance(loc) < maxSight);
	}
	
	public boolean lookAround(ArrayList<Viecher> andere) {
		if (lifespan > 0) {
			lifespan--;
		}
		if (lifespan == 0) {
			return false;
		}
		
		double dist = Double.MAX_VALUE;
		Point2D loc = null, vel = null;
		
		for (Viecher vieh : andere) {
			if (this != vieh) {
				if(isVisible(vieh.getLocation())) {
					if(kind == "prey") {
						if (vieh.kind == "hunter") {
							if (phys.distance(vieh.getLocation()) < dist) {
								dist = phys.distance(vieh.getLocation());
								loc = vieh.getLocation();
								vel = vieh.getVelocity();
							}
						}
					} else {
						if (vieh.kind == "prey") {
							if (phys.distance(vieh.getLocation()) - vieh.getSize() < dist) {
								dist = phys.distance(vieh.getLocation()) - vieh.getSize();
								loc = vieh.getLocation();
								vel = vieh.getVelocity();
							}
						}
					}
				}
				
				if(touching(vieh)) {
					if(kind == "prey") {
						if (vieh.kind == "hunter") {
							vieh.addLifespan((int) map(size, 25, 100, maxLifespan / 100 * 15, maxLifespan / 100 * 30));
							return false;
						} else {
							phys.avoid(vieh.getLocation(), 3);
						}
					} else if (vieh.kind == "hunter"){
						phys.avoid(vieh.getLocation(), 3);
					}
				}
			}
		}
		
		if (loc != null) {
			vel = vel.normalize();
			vel = vel.multiply(foresight);
			if (kind == "prey") {
				phys.avoid(loc.add(vel));
			} else {
				phys.seek(loc.add(vel));
			}
		}
		return true;
	}
	
	public void addLifespan(int lifespan) {
		if( this.lifespan + lifespan < maxLifespan) {
			this.lifespan += lifespan;
		} else {
			this.lifespan = (int) (maxLifespan);
		}
	}
	
	public void update() {
		final double weight = 2;
		if (phys.getX() > gc.getCanvas().getWidth() / 10 * 9) {
			phys.seek(new Point2D(0, phys.getY()), weight);
		} else if (phys.getX() < gc.getCanvas().getWidth() / 10) {
			phys.seek(new Point2D(gc.getCanvas().getWidth(), phys.getY()), weight);
		} else if (phys.getY() > gc.getCanvas().getHeight() / 10 * 9) {
			phys.seek(new Point2D(phys.getX(), 0), weight);
		} else if (phys.getY() < gc.getCanvas().getHeight() / 10) {
			phys.seek(new Point2D(phys.getX(), gc.getCanvas().getHeight()), weight);
		}
		
		if (lifespan > 0) {
			size = map(lifespan, 0, maxLifespan, 25, 100);
			phys.setMaxSpeed(map(size, 25, 100, 4, 1));
			phys.setMaxForce(map(size, 25, 100, 0.05, 0.005));
			maxSight = map(size, 25, 100, 750, 250);
			maxViewAngle = map(size, 25, 100, 60, 20);
			foresight = map(size, 25, 100, 50, 200);
		}
		
		phys.updatePosition();
	}
	
	public Viecher getKinder() {
		if (new Random().nextDouble() < 0.0001) {
			if (kind == "hunter") {
				return new Viecher(gc, phys.getX(), phys.getY(), 100, kind);
			} else {
				return new Viecher(gc, phys.getX(), phys.getY(), size, kind);
			}
		}
		return null;
	}
	
	public String getKind() {
		return kind;
	}
	
	//Seek section
	public void seek(Point2D target) {
		phys.seek(target);
	}
	
	public void seek(Point2D target, double weight) {
		phys.seek(target, weight);
	}
	
	//flee section
	public void flee(Point2D target) {
		phys.avoid(target);
	}
	
	public void flee(Point2D target, double weight) {
		phys.avoid(target, weight);
	}
	
	public void show() {
		gc.setFill(color);
		gc.fillOval(phys.getX() - size / 2, phys.getY() - size / 2, size, size);
		
		Point2D line = phys.getVelocity();
		line = line.normalize();
		line = line.multiply(foresight);
		line = phys.getLocation().add(line);
		gc.strokeLine(phys.getX(), phys.getY(), line.getX(), line.getY());
		
		if (lifespan > 0) {
			gc.setFill(Color.BLACK);
			gc.fillText(lifespan + "", phys.getX(), phys.getY());
		}
	}
	
	public Point2D getLocation() {
		return phys.getLocation();
	}
	
	public Point2D getVelocity() {
		return phys.getVelocity();
	}
	
	public double getSize() {
		return size;
	}
	
	public boolean touching(Viecher ding) {
		return (phys.distance(ding.getLocation()) < size / 2 + ding.getSize() / 2);
	}
	
	public static double map(double value, double min, double max, double nMin, double nMax) {
		return ((value - min) / (max - min)) * (nMax - nMin) + nMin;
	}
}