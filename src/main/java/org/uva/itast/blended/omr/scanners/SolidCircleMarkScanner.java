/************************************************
 *  Note: Original work copyright to respective authors
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
* @author MarÃ­a JesÃºs VerdÃº 
* @author Luisa Regueras 
* @author Elena VerdÃº
* 
* @license http://www.gnu.org/copyleft/gpl.html GNU Public License
* @package blended
 ***********************************************************************/

 

package org.uva.itast.blended.omr.scanners;


import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.geom.AffineTransform;
import java.awt.geom.Dimension2D;
import java.awt.image.BufferedImage;

import org.uva.itast.blended.omr.OMRProcessor;
import org.uva.itast.blended.omr.pages.PageImage;

public class SolidCircleMarkScanner extends TemplateMarkScanner{
	/**
	 * 
	 * @param grayimage
	 * @param markWidth          in milimeters
	 * @param markHeight         in milimeters
	 * @param approxXscale
	 * @param approxYscale
	 */
	public SolidCircleMarkScanner(OMRProcessor omr,PageImage pageimage, double markWidth, double markHeight, boolean medianfilter) 
	{
		super(omr,pageimage,markWidth,markHeight,medianfilter);
	}
	
	@Override
	protected double fillTemplate(BufferedImage templateimg, int width,int height)
	{
		double centerX = templateimg.getWidth() / 2;
		double centerY = templateimg.getHeight() / 2;
		double aspect=((double)width)/height;
		int ellipseRadius=width/2; // in fact it is the minor axis
		int whites = 0;
		for (int i = 0; i < templateimg.getWidth(); i++) {
			for (int j = 0; j < templateimg.getHeight(); j++) {
				double dist = Math.sqrt((i - centerX) * (i - centerX)
						+ (j - centerY) / aspect * (j - centerY) / aspect);
				if (dist <= ellipseRadius) {
					// templateimg.putBlack(i, j);
					templateimg.setRGB(i, j, Color.BLACK.getRGB());

				} else {
					// templateimg.putWhite(i, j);
					templateimg.setRGB(i, j, Color.WHITE.getRGB());
					whites++;
				}
			}
		}
		// compute autoSimilarity
		return ((double) whites)	/ (templateimg.getHeight() * templateimg.getWidth());
	}
	/**
	 * @param pageImage
	 */
	public void putEmphasisMarkOnImage(PageImage pageImage)
	{
		
		Graphics2D g = pageImage.getReportingGraphics();
		// int centerColor=imagen.getRGB(maxsimX, maxsimY);
		// g.setXORMode(new Color(centerColor));
		// g.setColor(Color.RED);
		// g.fillOval(maxsimX - markWidth/2, maxsimY - markHeight/2, markWidth,
		// markHeight);
		// g.setPaintMode();
		Dimension2D markDimsPx=pageImage.sizeInPixels(new Size(markWidth,markHeight));
		int markWidth=(int) markDimsPx.getWidth();
		int markHeight=(int) markDimsPx.getHeight();
		g.setColor(Color.RED);
		AffineTransform t=g.getTransform();
		g.drawLine(maxsimX, maxsimY - markHeight / 2 - 1, maxsimX, maxsimY
				- markHeight / 2 - (int)(20/t.getScaleY()));
		Polygon arrowHead = new Polygon();
		arrowHead.addPoint(maxsimX, (int) (maxsimY - markHeight / 2 - 1/t.getScaleY()));
		arrowHead.addPoint((int)(maxsimX - 6/t.getScaleX()),(int)( maxsimY - markHeight / 2 - 6/t.getScaleY()));
		arrowHead.addPoint((int)(maxsimX + 6/t.getScaleX()), (int)(maxsimY - markHeight / 2 - 6/t.getScaleY()));
		g.fillPolygon(arrowHead);
		
		g.setStroke(new BasicStroke(2,BasicStroke.CAP_SQUARE,BasicStroke.JOIN_ROUND,1,new float[]{(float) (3/t.getScaleX()),(float) (3/t.getScaleY())},0));
		g.drawOval(maxsimX - markWidth / 2 - 1,
				maxsimY - markHeight / 2 - 1,
				markWidth + 1, markHeight + 1);
	
	}

	@Override
	public String getType()
	{
		return "SolidCircle";
	}
	
}