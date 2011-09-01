package org.uva.itast.blended.omr.pages;

import java.awt.Point;
import java.awt.geom.Point2D;

/**
 * Stores a {@link PageImage} reference with a position in milimeters
 * 
 * @author juacas
 */
public class PagePoint extends Point2D.Double
{

	/**
	 * 
	 */
	private static final long	serialVersionUID	=7176698354883895171L;
	private PageImage			pageImage;
	/**
	 * 
	 * @param page
	 * @param x
	 *            milimeters
	 * @param y
	 *            milimeters
	 */
	private Point				pixelsPoint;
/**
 * initializes the point in the page with milimeters.
 * @param page
 * @param x
 * @param y
 */
	public PagePoint(PageImage page, double x, double y)
	{

		this.pageImage=page;
		this.setLocation(x, y);
	}
	/* (non-Javadoc)
 * @see java.awt.geom.Point2D.Double#toString()
 */
@Override
public String toString()
{
	return "("+this.x+","+this.y+ ") (mm) ("+ this.pixelsPoint.x+","+this.pixelsPoint.y+") (px)";
}
	/**
	 * initializes the point from observed pixels
	 * @param page
	 * @param x
	 * @param y
	 */
	public PagePoint(PageImage page, int x, int y)
	{

		this.pageImage=page;
		this.setLocation(x, y);
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.geom.Point2D.Double#setLocation(double, double)
	 */
	@Override
	public void setLocation(double x, double y)
	{
		super.setLocation(x, y);
		this.pixelsPoint=pageImage.toPixels(x, y);
	}
	/**
	 * set a location in pixels
	 * @param x
	 * @param y
	 */
	public void setLocation(int x,int y)
	{
		this.pixelsPoint=new Point(x, y);
		this.setLocation(pageImage.toMilimeters(x, y));
	}
	/**
	 * Returns the position on page in milimeters
	 * @return
	 */
	public Point getPixelsPoint()
	{
		return this.pixelsPoint;
	}

	public PageImage getPageImage()
	{
		return pageImage;
	}
	public int getYpx()
	{
		return pixelsPoint.y;
	}
	public int getXpx()
	{
		return pixelsPoint.x;
	}
	public int distancePx(PagePoint dtopleft)
	{
		return (int) pixelsPoint.distance(dtopleft.getPixelsPoint());
	}
}
