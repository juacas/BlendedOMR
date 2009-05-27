/**
 * 
 */
package org.uva.itast.blended.omr.pages;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;

/**
 * @author juacas
 *
 */
public class SubImage extends BufferedImage
{

	private Rectangle	boundingBox;
	/**
	 * @return the boundingBox
	 */
	public Rectangle getBoundingBox()
	{
		return boundingBox;
	}

	/**
	 * @return the reference
	 */
	public Point getReference()
	{
		return reference;
	}

	private Point	reference;

	/**
	 * @param width
	 * @param height
	 * @param imageType
	 * @param cm
	 */
	public SubImage(int width, int height, int imageType)
	{
		super(width, height, imageType);
		// TODO Auto-generated constructor stub
	}

	/**
	 * Point used as upper-left in the subimage extraction
	 * @see this{@link #setBoundingBox(Rectangle)}
	 * @param reference
	 */
	public void setReference(Point reference)
	{
		this.reference=reference;
		
	}

	/**
	 * Actual boundingbox representing the subimage.
	 * Reference Point is somewhere inside the bbox but not necessary at the upper-left pixel.
	 * @see {@link #setReference(Point)}
	 * @param rect
	 */
	public void setBoundingBox(Rectangle rect)
	{
		this.boundingBox=rect;
		
	}
	

}
