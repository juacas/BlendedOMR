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

package org.uva.itast.blended.omr;

import java.awt.Color;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.uva.itast.blended.omr.pages.SubImage;

public class BufferedImageUtil
{

	/**
	 * Logger for this class
	 */
	private static final Log	logger	=LogFactory.getLog(BufferedImageUtil.class);

	/**
	 * 
	 * @param img
	 * @param x
	 * @param y
	 * @param template
	 * @param dump
	 * @return
	 */
	public static double templateXOR(BufferedImage img, int x, int y, BufferedImage template, boolean dump)
	{
		int diff=0, total=0;
		if (y < 0)
		{
			logger.error("coordinate in XOR template should not be <0");
			y=0;
		}
		if (x < 0)
		{
			logger.error("coordinate in XOR template should not be <0");
			x=0;
		}
		int templateHeight=template.getHeight();
		int templateWidth=template.getWidth();
		int imgHeight=img.getHeight();
		int imgWidth=img.getWidth();
		if (dump && logger.isDebugEnabled())
		{
			logger.debug("templateXOR - Testing XOR Width=" + templateWidth + "from upper-left x=" + x + ", y=" + y); //$NON-NLS-1$ //$NON-NLS-2$

			for (int j=y; j < y + templateHeight && j < imgHeight; j++)
			{
				for (int i=x; i < x + templateWidth && i < imgWidth; i++)
				{
					float luminance=getLuminance(img, j, i);
					boolean isblack=(luminance < 0.75 ? true : false);
					boolean tempIsBlack=isBlack(template, j - y, i - x);
					System.out.print(isblack ? "*" : (tempIsBlack ? "O" : "_"));
				}
				System.out.println("<-");
			}
		}// end debug

		for (int j=y; j < y + templateHeight && j < imgHeight; j++)
		{
			for (int i=x; i < x + templateWidth && i < imgWidth; i++)
			{
				float luminance=getLuminance(img, j, i);
				boolean isblack=(luminance < 0.75 ? true : false);
				boolean tempIsWhite=isWhite(template, j - y, i - x);
				boolean tempIsBlack=isBlack(template, j - y, i - x);

				if ((isblack & tempIsWhite) | ((!isblack) & tempIsBlack))
				{
					diff++;
				}
				total++;
			}

		}
		if (dump && logger.isDebugEnabled())
		{
			logger.debug("templateXOR- Diffs=" + diff + " out of " + total); //$NON-NLS-1$ //$NON-NLS-2$
		}
		return ((double) diff) / total;
	}

	/**
	 * Determina si un determinado p�xel es blanco o no
	 * 
	 * @param template
	 * @param j
	 * @param i
	 * @return
	 */
	private static boolean isWhite(BufferedImage template, int j, int i)
	{
		return (template.getRGB(i, j) == Color.WHITE.getRGB() ? true : false);
	}

	/**
	 * Determina si un determinado p�xel es negro o no
	 * 
	 * @param template
	 * @param j
	 * @param i
	 * @return
	 */
	private static boolean isBlack(BufferedImage template, int j, int i)
	{
		return (template.getRGB(i, j) == Color.BLACK.getRGB() ? true : false);
	}

	/**
	 * Estimates the mean luminance of a subimage
	 * 
	 * @param img
	 * @param downsampling
	 * @return array meanluminance,minlum,maxlum
	 */
	public static float[] statsLuminance(SubImage img, int downsampling)
	{
		float luminanceSum=0;
		float min=1;
		float max=0;
		Rectangle captured=img.getCapturedBoundingBox();
		Rectangle represented=img.getBoundingBox();
		Point reference=img.getReference();
		int xstart=Math.max(captured.x - reference.x, 0);
		int ystart=Math.max(captured.y - reference.y, 0);
		int pixels=0;
		float luminance;
		for (int y=ystart; y < ystart + captured.height; y+=downsampling)
			for (int x=xstart; x < xstart + captured.width; x+=downsampling)
			{
				luminance=getLuminance(img, y, x);
				luminanceSum+=luminance;
				min=Math.min(min, luminance);
				min=Math.min(min, luminance);
				max=Math.max(max, luminance);
				pixels++;
			}
		return new float[] { luminanceSum / pixels, min, max };
	}

	/**
	 * Obtiene la luminancia de un pixel
	 * 
	 * @param img
	 * @param j
	 * @param i
	 * @return
	 */
	public static float getLuminance(BufferedImage img, int j, int i)
	{

		int rgb=img.getRGB(i, j);
		float red=((rgb & 0xff0000) >> 16) / 255.f;
		float green=((rgb & 0xff00) >> 8) / 255.f;
		float blue=(rgb & 0xff) / 255.f;
		float luminance=(float) ((.299 * red) + (.587 * green) + (.114 * blue));
		return luminance;

	}

	/**
	 * Dibuja de una marca de color blanco o negro en un BufferedImage
	 * 
	 * @param img
	 * @param x
	 * @param y
	 * @param color
	 */
	public static void putMarkBufferedImage(BufferedImage img, int x, int y, boolean color)
	{
		if (color)
		{
			img.setRGB(x, y, Color.BLACK.getRGB());
			img.setRGB(x + 1, y + 1, Color.BLACK.getRGB());
			img.setRGB(x - 1, y - 1, Color.BLACK.getRGB());
			img.setRGB(x + 1, y, Color.BLACK.getRGB());
			img.setRGB(x - 1, y, Color.BLACK.getRGB());
			img.setRGB(x, y + 1, Color.BLACK.getRGB());
			img.setRGB(x, y - 1, Color.BLACK.getRGB());
			img.setRGB(x + 1, y - 1, Color.BLACK.getRGB());
			img.setRGB(x - 1, y + 1, Color.BLACK.getRGB());
			img.setRGB(x - 1, y - 1, Color.BLACK.getRGB());
		}
		else
		{
			img.setRGB(x, y, Color.WHITE.getRGB());
			img.setRGB(x + 1, y + 1, Color.WHITE.getRGB());
			img.setRGB(x - 1, y - 1, Color.WHITE.getRGB());
			img.setRGB(x + 1, y, Color.WHITE.getRGB());
			img.setRGB(x - 1, y, Color.WHITE.getRGB());
			img.setRGB(x, y + 1, Color.WHITE.getRGB());
			img.setRGB(x, y - 1, Color.WHITE.getRGB());
			img.setRGB(x + 1, y - 1, Color.WHITE.getRGB());
			img.setRGB(x - 1, y + 1, Color.WHITE.getRGB());
			img.setRGB(x - 1, y - 1, Color.WHITE.getRGB());
		}

	}

	public static void thresholdAndInvert(SubImage img, float f)
	{
		Rectangle captured=img.getCapturedBoundingBox();
		Point reference=img.getReference();
		int xstart=Math.max(captured.x - reference.x, 0);
		int ystart=Math.max(captured.y - reference.y, 0);

		float luminance;
		for (int y=ystart; y < ystart + captured.height; y++)
			for (int x=xstart; x < xstart + captured.width; x++)
			{
				luminance=getLuminance(img, y, x);
				if (luminance < f)
					img.setRGB(x, y, 0xffffff);
				else
					img.setRGB(x, y, 0x000000);
			}
	}
	public static void invert(SubImage img)
	{
		Rectangle captured=img.getCapturedBoundingBox();
		Point reference=img.getReference();
		int xstart=Math.max(captured.x - reference.x, 0);
		int ystart=Math.max(captured.y - reference.y, 0);

		
		for (int y=ystart; y < ystart + captured.height; y++)
			for (int x=xstart; x < xstart + captured.width; x++)
			{
				img.setRGB(x, y, 0xffffff-img.getRGB(x, y));
			}
	}
	public static void scale(SubImage img, float ratio)
	{
		Rectangle captured=img.getCapturedBoundingBox();
		Point reference=img.getReference();
		int xstart=Math.max(captured.x - reference.x, 0);
		int ystart=Math.max(captured.y - reference.y, 0);

		
		for (int y=ystart; y < ystart + captured.height; y++)
			for (int x=xstart; x < xstart + captured.width; x++)
			{
				img.setRGB(x, y, (int) (ratio*img.getRGB(x, y)));
			}
	}
	
}
