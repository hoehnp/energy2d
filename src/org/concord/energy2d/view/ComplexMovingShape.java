/*
 *   Copyright (C) 2012  The Concord Consortium, Inc.,
 *   25 Love Lane, Concord, MA 01742
 */

package org.concord.energy2d.view;

import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Shape;
import java.awt.geom.Area;

/**
 * Complex shapes take longer time to construct. So this class uses a location point to avoid recalculating the area.
 * 
 * @author Charles Xie
 * 
 */
abstract class ComplexMovingShape implements MovingShape {

	Area area;
	Point location = new Point();

	public void setLocation(int x, int y) {
		location.setLocation(x, y);
	}

	public Point getLocation() {
		return location;
	}

	public int getX() {
		return location.x;
	}

	public int getY() {
		return location.y;
	}

	public Shape getShape() {
		return area;
	}

	public void render(Graphics2D g) {
		g.translate(location.x, location.y);
		g.draw(area);
		g.translate(-location.x, -location.y);
	}

}