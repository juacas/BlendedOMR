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

package org.uva.itast.blended.omr.scanners;

import java.awt.Color;
import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.uva.itast.blended.omr.Field;
import org.uva.itast.blended.omr.OMRProcessor;
import org.uva.itast.blended.omr.pages.PageImage;

import com.google.zxing.ReaderException;

public abstract class MarkScanner
{
	/**
	 * Logger for this class
	 */
	private static final Log	logger			=LogFactory.getLog(MarkScanner.class);

	ScanResult					lastResult;
	PageImage					pageImage;
	boolean						medianfilter	=false;

	protected OMRProcessor		omr;

	/**
	 * Aplica un filtro para reconstruir imagenes de mala calidad, a trav�s del
	 * valor de los p�xeles vecinos
	 * 
	 * @param subimage
	 */
	public static BufferedImage medianFilter(BufferedImage subimage)
	{

		com.jhlabs.image.MedianFilter filter=new com.jhlabs.image.MedianFilter();
		BufferedImage result=filter.createCompatibleDestImage(subimage, subimage.getColorModel());
		filter.filter(subimage, result);

		return result;

	}

	/**
	 * M�todo que lee el valor de un c�digo de barras contenido en un objeto
	 * tipo BufferedImage a partir de los patrones dados en un objeto tipo Campo
	 * 
	 * @param campo
	 * @param imagen
	 * @param medianfilter
	 * @return
	 * @throws ReaderException
	 */
	public ScanResult scanField(Field campo) throws MarkScannerException
	{

		// se leen y almacenan las coordenadas
		Rectangle2D coords=campo.getBBox();
		if (logger.isDebugEnabled())
			logger.debug("Searching mark for field:" + campo);
		ScanResult result;
		try
		{
			result=scanAreaForFieldData(coords);
		}
		catch (MarkScannerException e)
		{
			Rectangle2D expandedBbox=getExpandedArea(coords);
			// Try with a wider area
			logger.debug("Last attempt to read mark was in error: RETRY a wider area...");
			result=scanAreaForFieldData(expandedBbox);

		}
		this.lastResult=result;
		return result;
	}

	protected abstract Rectangle2D getExpandedArea(Rectangle2D coords);

	/**
	 * @param coords
	 * @param doNotAnnotateImage
	 *            TODO
	 * @return
	 */
	public abstract ScanResult scanAreaForFieldData(Rectangle2D coords) throws MarkScannerException;

	/**
	 * Return boundingBox of
	 * 
	 * @param coords
	 * @return
	 */
	private Rectangle getRectArea(double[] coords)
	{
		Point2D coordUpperLeft=pageImage.toPixels(coords[0], coords[1]);
		Point2D coordBottomRight=pageImage.toPixels(coords[0] + coords[2], coords[1] + coords[2]);

		int x=(int) Math.min(coordUpperLeft.getX(), coordBottomRight.getX());
		int y=(int) Math.min(coordUpperLeft.getY(), coordBottomRight.getY());
		int xm=(int) Math.max(coordUpperLeft.getX(), coordBottomRight.getX());
		int ym=(int) Math.max(coordUpperLeft.getY(), coordBottomRight.getY());
		int width=xm - x; // anchura en p�xeles
		int height=ym - y; // altura en p�xeles

		return new Rectangle(x, y, width, height);
	}

	/**
	 * 
	 */
	public MarkScanner()
	{
		super();
	}

	/**
	 * @param omr
	 * @param imagen
	 * @param medianfilter
	 */
	public MarkScanner(OMRProcessor omr, PageImage imagen, boolean medianfilter)
	{
		this.omr=omr;
		this.pageImage=imagen;
		this.medianfilter=medianfilter;
	}

	/**
	 * @param campo
	 * @return
	 * @throws
	 */
	abstract public String getParsedCode(Field campo) throws MarkScannerException;

	/**
	 * Generates an expanded boundingbox in milimeters
	 * 
	 * @see {@link #BARCODE_AREA_PERCENT}
	 * @see {@value #BARCODE_AREA_PERCENT}
	 * @param rect
	 * @param percent TODO
	 * @return milimeteres
	 */
	abstract protected Rectangle2D getExpandedArea(Rectangle2D rect, double percent);

	/**
	 * Create an emphasized indication in the {@link PageImage} reporting image
	 * 
	 * @param pageImage2
	 * @param color TODO
	 */
	abstract public void putEmphasisMarkOnImage(PageImage pageImage2, Color color);

}