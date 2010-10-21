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

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.uva.itast.blended.omr.OMRProcessor;

import com.sun.pdfview.PDFFile;
import com.sun.pdfview.PDFPage;
import com.sun.pdfview.PDFRenderer;

public class PDFPageImage extends PageImage
{
	/**
	 * 
	 */
	private static final int	PREFERRED_PIXELS_WIDTH_A4	= OMRProcessor._PAGE_WIDTH_PIXELS;

	/**
	 * 
	 */
	private static final int	PREFERRED_PIXELS_HEIGHT_A4	= OMRProcessor._PAGE_HEIGHT_PIXELS;

	/**
	 * Logger for this class
	 */
	private static final Log	logger	= LogFactory.getLog(PDFPageImage.class);

	private static final int	RENDER_PAGE_BEFORE	= 1;

	private int	pageNumber;
	private PDFFile	pdfFile;

	private File	filePath;

	private PDFPage	page;

	private int	extractionMethod;

	/**
	 * @param pdffile
	 * @param page page number starting in 0
	 * @throws IOException 
	 */
	public PDFPageImage(File path,PDFFile pdffile, int page) throws IOException
	{
		this.filePath=path;
		if (pdfFile==null)
			this.pdfFile= loadPDFFile(filePath);
		else
			this.pdfFile = pdffile;
		this.extractionMethod=PDFPageImage.RENDER_PAGE_BEFORE;
		this.pageNumber=page;
		resetAlignmentInfo();
	}

	/**
	 * @param filePath2
	 * @throws IOException 
	 */
	public static PDFFile loadPDFFile(File inputpath) throws IOException
	{
		RandomAccessFile raf = new RandomAccessFile(inputpath, "r");	//se carga la imagen pdf para leerla
		FileChannel channel = raf.getChannel();
		ByteBuffer buf = channel.map(FileChannel.MapMode.READ_ONLY, 0, channel.size());
		PDFFile pdffile = new PDFFile(buf); 
		return pdffile;
	}

	/* (non-Javadoc)
	 * @see org.uva.itast.blended.omr.PageImage#getImagen()
	 */
	@Override
	public BufferedImage createImage()
	{
		long start=System.currentTimeMillis();
		getPage();
		
		try
		{
			BufferedImage imagen = renderFullPageImage(page);
			setImagen(imagen);
		}
		catch (InterruptedException e)
		{
			throw new RuntimeException("UNEXPECTED: PDF conversion interrumped.",e);
		}		
		
		logger.debug("PDF page "+pageNumber+" converted in (ms)"+(System.currentTimeMillis()-start)); //$NON-NLS-1$
		
		return super.getImagen();
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		return filePath.getName()+" page "+pageNumber;
	}

	/* (non-Javadoc)
	 * @see org.uva.itast.blended.omr.PageImage#getType()
	 */
	@Override
	public String getType()
	{
		
		return "PDF";
	}

	/* (non-Javadoc)
	 * @see org.uva.itast.blended.omr.PageImage#getFileName()
	 */
	@Override
	public String getName()
	{
		return "Page "+pageNumber+" of "+filePath.getAbsolutePath();
	}
	/**
	 * M�todo que lee una imagen pdf y la transforma en un objeto de tipo
	 * BufferedImage reescalado
	 * 
	 * @param page
	 * @return img_pdf
	 * @throws InterruptedException
	 * @throws Exception
	 */
	private  BufferedImage renderFullPageImage(PDFPage page)
			throws InterruptedException
	{
		BufferedImage img_pdf = null; // bufferedimage para almacenar la imagen

		// se elige el ancho y el alto de la imagen
		int resizeWidth = OMRProcessor._PAGE_WIDTH_PIXELS;
		int resizeHeight = OMRProcessor._PAGE_HEIGHT_PIXELS;

		img_pdf = new BufferedImage(resizeWidth, resizeHeight,
				BufferedImage.TYPE_INT_RGB); // se configura la imagen con las
												// medidas especificas y en
												// escala de grises
		Graphics2D g2 = img_pdf.createGraphics(); // se crea un objeto gr�fico
													// en dos dimensiones
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);

		PDFRenderer renderer = new PDFRenderer(page, g2, new Rectangle(0, 0,
				OMRProcessor._PAGE_WIDTH_PIXELS,
				OMRProcessor._PAGE_HEIGHT_PIXELS), null, Color.RED); // se
																		// renderiza
																		// la
																		// imgen
		page.waitForFinish();
		renderer.run();
		img_pdf.createGraphics().drawImage(img_pdf, 0, 0, resizeWidth,
				resizeHeight, null); // por �ltimo se dibuja la imagen

		return img_pdf;
	}

	/* (non-Javadoc)
	 * @see org.uva.itast.blended.omr.PageImage#getHorizontalRatio()
	 */
	@Override
	public double getPreferredHorizontalResolution()
	{
		return PREFERRED_PIXELS_WIDTH_A4/PageImage.a4width;
	}
	/* (non-Javadoc)
	 * @see org.uva.itast.blended.omr.PageImage#getVerticalRatio()
	 */
	@Override
	public double getPreferredVerticalResolution()
	{	
		return PREFERRED_PIXELS_HEIGHT_A4/PageImage.a4height;
	}
	private Rectangle2D toPDFUnits(Rectangle2D rect)
	{
		float ratioHeight=PREFERRED_PIXELS_HEIGHT_A4/getPage().getHeight();
		float ratioWidth=PREFERRED_PIXELS_WIDTH_A4/getPage().getWidth();
		Rectangle2D rectNew=new Rectangle();
		rectNew.setFrame(rect.getX()/ratioWidth, rect.getY()/ratioHeight,rect.getWidth()/ratioWidth,rect.getHeight()/ratioHeight);
		return rectNew;
	}
	/* (non-Javadoc)
	 * @see org.uva.itast.blended.omr.pages.PageImage#getSubimage(int, int, int, int)
	 */
	@Override
	public SubImage getSubimage(Rectangle2D rect, int imageType)
	{
		if (this.extractionMethod==PDFPageImage.RENDER_PAGE_BEFORE)
		{
			return super.getSubimage(rect, imageType);
		}
		else
		{
		return getSubimageWithPartialRendering(rect, imageType);
		}
	}

	/**
	 * @param x
	 * @param y
	 * @param w
	 * @param h
	 * @return
	 */
	private SubImage getSubimageWithPartialRendering(Rectangle2D rect,int imageType)
	{
		double pageHeight=getPage().getHeight();
		// Area in pixels according to preferred resolution
		Point upperLeft = toPixels(rect.getX(), rect.getY());
		
		Rectangle imageBBox=this.toPixels(rect); // subImage Bounding Box in pixels		
		Rectangle2D pdfAreaBBox=toPDFUnits(imageBBox); // subImage Bounding Box in PDF units
		
		Rectangle imageSize= new Rectangle(imageBBox.width,imageBBox.height); // subImage Size in pixels
		
		
		Rectangle2D clippingArea=new Rectangle(); // area of interest in the PDF
		clippingArea.setFrame(pdfAreaBBox.getX(), 
							  pageHeight-pdfAreaBBox.getY() - pdfAreaBBox.getHeight(), //PDF-Page coordinate space counts from bottomleft
							  pdfAreaBBox.getWidth(), pdfAreaBBox.getHeight());
		
		SubImage img_pdf = new SubImage(imageSize.width,imageSize.height,imageType); 
		// se configura la imagen con las medidas necesarias
		
Graphics2D g2 = img_pdf.createGraphics(); // se crea un objeto gr�fico en dos dimensiones
g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_OFF); // prefer to get sharp edges
		
		PDFRenderer renderer = new PDFRenderer(getPage(), g2,
												imageSize,
												clippingArea, 
												Color.RED); // se renderiza la imgen 
		try
		{
			getPage().waitForFinish();
		}
		catch (InterruptedException e)
		{
			e.printStackTrace();
			throw new RuntimeException(e);
		}
		renderer.run();
		
		img_pdf.setReference(upperLeft);
		img_pdf.setBoundingBox(imageBBox);
		
		return img_pdf;
	}

	/**
	 * @return
	 */
	private PDFPage getPage()
	{
		if (page==null)
			this.page = pdfFile.getPage(pageNumber);   //se coge la  p�gina
			
		return this.page;
	}
}