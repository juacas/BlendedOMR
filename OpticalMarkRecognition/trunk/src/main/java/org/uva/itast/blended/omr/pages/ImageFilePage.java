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

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.uva.itast.blended.omr.OMRProcessor;

public class ImageFilePage extends PageImage
{
	/**
	 * Logger for this class
	 */
	private static final Log	logger	= LogFactory.getLog(ImageFilePage.class);

	private File	filePath;

	/**
	 * @param imagen
	 * @param align
	 */
	public ImageFilePage(File path)
	{
	this.filePath=path;
	
	}

	public ImageFilePage()
	{
	}

	/* (non-Javadoc)
	 * @see org.uva.itast.blended.omr.PageImage#getImagen()
	 */
	@Override
	public BufferedImage createImage()
	{
		BufferedImage image;
		try
		{
			image = loadImageFile();
		}
		catch (IOException e)
		{
			throw new RuntimeException("Can't load image "+filePath,e);
		}
		setImagen(image);
		return super.getImagen();
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		return getFileName();
	}

	/**
	 * @return
	 * @throws IOException 
	 */
	protected BufferedImage loadImageFile() throws IOException
	{
		BufferedImage imagen;
		
		long start=System.currentTimeMillis();
		imagen = ImageIO.read(filePath);
		if (imagen ==null)
			throw new IOException("File "+filePath+"do not contain a valid image");
//		if(false)
//			imagen = reescalar();	
		logger.debug("Image page ("+filePath.getName()+") converted in (ms)"+(System.currentTimeMillis()-start)); //$NON-NLS-1$
		

		return imagen;
	}
	public  BufferedImage reescalar() throws IOException
	{
		Image imagenObjeto = null;
		BufferedImage imagenSalida = null; // se crea la imagen de salida

		// se elige el ancho y el alto de la nueva imagen
		int resizeWidth = OMRProcessor._PAGE_WIDTH_PIXELS;
		int resizeHeight = OMRProcessor._PAGE_HEIGHT_PIXELS;

		imagenSalida = new BufferedImage(resizeWidth, resizeHeight,
				BufferedImage.TYPE_INT_RGB); // se configura la imagen con las
												// medidas especificas 
		
		imagenSalida.createGraphics().drawImage(imagenObjeto, 0, 0,
				resizeWidth, resizeHeight, null); // se crea un objeto gr�fico
													// en dos dimensiones

		return imagenSalida;
	}

	/* (non-Javadoc)
	 * @see org.uva.itast.blended.omr.PageImage#getType()
	 */
	@Override
	public String getType()
	{
		return "1-PAGE IMAGE FILE";
	}

	/* (non-Javadoc)
	 * @see org.uva.itast.blended.omr.PageImage#getFileName()
	 */
	@Override
	public String getFileName()
	{
		
		return filePath.getAbsolutePath();
	}

	/* (non-Javadoc)
	 * @see org.uva.itast.blended.omr.PageImage#getHorizontalRatio()
	 */
	@Override
	public double getPreferredHorizontalResolution()
	{
		return getImagen().getWidth()/PageImage.a4width;
	}

	/* (non-Javadoc)
	 * @see org.uva.itast.blended.omr.PageImage#getVerticalRatio()
	 */
	@Override
	public double getPreferredVerticalResolution()
	{
		return getImagen().getHeight()/PageImage.a4height;
	}

	
	

	

}