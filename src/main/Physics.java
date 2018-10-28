package main;

import javafx.geometry.Point2D;

public class Physics {
	private Point2D location, acceleration, velocity;

	private double maxSpeed, maxForce;

	public Physics(double x, double y, double maxSpeed, double maxForce) {
		location = new Point2D(x, y);
		velocity = new Point2D(0, 0);
		acceleration = new Point2D(0, 0);

		this.maxForce = maxForce;
		this.maxSpeed = maxSpeed;
	}

	public void seek(double x, double y) {
		move(new Point2D(x, y), 1, true);
	}

	public void seek(double x, double y, double weight) {
		move(new Point2D(x, y), weight, true);
	}

	public void seek(Point2D point) {
		move(point, 1, true);
	}

	public void seek(Point2D point, double weight) {
		move(point, weight, true);
	}

	public void seek(Physics point) {
		move(point.getLocation(), 1, true);
	}

	public void seek(Physics point, double weight) {
		move(point.getLocation(), weight, true);
	}

	public void avoid(double x, double y) {
		move(new Point2D(x, y), 1, false);
	}

	public void avoid(double x, double y, double weight) {
		move(new Point2D(x, y), weight, false);
	}

	public void avoid(Point2D point) {
		move(point, 1, false);
	}

	public void avoid(Point2D point, double weight) {
		move(point, weight, false);
	}

	public void avoid(Physics point) {
		move(point.getLocation(), 1, false);
	}

	public void avoid(Physics point, double weight) {
		move(point.getLocation(), weight, false);
	}

	private void move(Point2D point, double weight, boolean isSeeking) {
		// gewünschte Richtung ermitteln
		Point2D desired = new Point2D(point.getX(), point.getY()).subtract(location);
		desired = desired.normalize();
		desired = desired.multiply(maxSpeed);

		// Kraft in der gelenkt wird ermitteln
		Point2D steer = new Point2D(desired.getX(), desired.getY()).subtract(velocity);
		// steer.limit(maxForce);
		if (steer.magnitude() > maxForce) {
			steer = steer.normalize();
			steer = steer.multiply(maxForce);
		}
		steer = steer.multiply(weight);

		// falls es sich von dem Punkt entfernen soll, bewegrichtung umkehren
		if (!isSeeking) {
			steer = steer.multiply(-1);
		}

		// kraft anwenden
		applyForce(steer);
	}

	private void applyForce(Point2D force) {
		acceleration = acceleration.add(force);
	}

	public void updatePosition() {
		velocity = velocity.add(acceleration);
		// velocity.limit(maxSpeed);
		if (velocity.magnitude() > maxSpeed) {
			velocity = velocity.normalize();
			velocity = velocity.multiply(maxSpeed);
		}
		location = location.add(velocity);
		acceleration = acceleration.multiply(0);
	}

	public void setLocation(double x, double y) {
		location = new Point2D(x, y);
	}

	public void setLocation(Point2D point) {
		location = new Point2D(point.getX(), point.getY());
	}

	public void setLocation(Physics point) {
		location = new Point2D(point.getX(), point.getY());
	}

	public Point2D getLocation() {
		return location;
	}

	public void setX(double x) {
		location = new Point2D(x, location.getY());
	}

	public double getX() {
		return location.getX();
	}

	public void setY(double y) {
		location = new Point2D(location.getX(), y);
	}

	public double getY() {
		return location.getY();
	}

	public double distance(double x, double y) {
		return location.distance(new Point2D(x, y));
	}

	public double distance(Point2D point) {
		return location.distance(point);
	}

	public double distance(Physics point) {
		return location.distance(point.getLocation());
	}

	public Point2D getVelocity() {
		return velocity;
	}

	public void setMaxSpeed(double maxSpeed) {
		this.maxSpeed = maxSpeed;
	}

	public double getMaxSpeed() {
		return maxSpeed;
	}

	public void setMaxForce(double maxForce) {
		this.maxForce = maxForce;
	}

	public double getMaxForce() {
		return maxForce;
	}

	public static Point2D rotatePoint(double cx, double cy, double angle, Point2D p) {
		double s = Math.sin(Math.toRadians(angle));
		double c = Math.cos(Math.toRadians(angle));

		// translate point back to origin:
		p = new Point2D(p.getX() - cx, p.getY() - cy);

		// rotate point
		double xnew = p.getX() * c - p.getY() * s;
		double ynew = p.getX() * s + p.getY() * c;

		// translate point back:
		p = new Point2D(xnew + cx, ynew + cy);
		return p;
	}
}