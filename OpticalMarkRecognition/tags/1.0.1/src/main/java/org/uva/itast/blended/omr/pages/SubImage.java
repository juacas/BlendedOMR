/*
* ====================================================================
*
* License:        GNU General Public License
*
* Note: Original work copyright to respective authors
*
* This file is part of Blended (c) 2009-2010 University of Valladolid..
*
* Blended is free software; you can redistribute it and/or
* modify it under the terms of the GNU General Public License
* as published by the Free Software Foundation; either version 2
* of the License, or (at your option) any later version.
*
* Blended is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
*
*
* Module developed at the University of Valladolid http://www.eduvalab.uva.es
*
* http://www.itnt.uva.es , http://www.eduvalab.uva.es
*
* Designed and directed by Juan Pablo de Castro with 
* the effort of many other students of telecommunication 
* engineering.
* This module is provides as-is without any 
* guarantee. Use it as your own risk.
*
* @author Juan Pablo de Castro
* @author Jesus Rodilana
* @author María Jesús Verdú 
* @author Luisa Regueras 
* @author Elena Verdú
* 
* @license http://www.gnu.org/copyleft/gpl.html GNU Public License
* @package blended
 ***********************************************************************/

 

/***********************************************************************
 * Module developed at the University of Valladolid http://www.eduvalab.uva.es
 * Designed and directed by Juan Pablo de Castro with 
 * the effort of many other students of telecommunciation 
 * engineering this module is provides as-is without any 
 * guarantee. Use it as your own risk.
 *
 * @author Juan Pablo de Castro and Miguel Baraja Campesino and many others.
 * @license http://www.gnu.org/copyleft/gpl.html GNU Public License
 * @package blended
 ***********************************************************************/

package org.uva.itast.blended.omr.pages;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;

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
