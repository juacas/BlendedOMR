package org.uva.itast.blended.omr.scanners;

import java.awt.geom.Dimension2D;

public class Size extends Dimension2D
{
private double	width;
private double	height;

public Size(double width, double height)
{
	super();
	setSize(width, height);
}

@Override
public double getWidth()
{
	return width;
}

@Override
public double getHeight()
{
	return height;
}

@Override
public void setSize(double width, double height)
{
	this.width=width;
	this.height=height;
}
}
