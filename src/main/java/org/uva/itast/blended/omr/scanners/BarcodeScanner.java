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
 ***********************************************************************/

package org.uva.itast.blended.omr.scanners;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.RasterFormatException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.uva.itast.blended.omr.Field;
import org.uva.itast.blended.omr.OMRProcessor;
import org.uva.itast.blended.omr.OMRUtils;
import org.uva.itast.blended.omr.pages.PageImage;
import org.uva.itast.blended.omr.pages.SubImage;

import com.google.zxing.MonochromeBitmapSource;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.ReaderException;
import com.google.zxing.Result;
import com.google.zxing.client.j2se.BufferedImageMonochromeBitmapSource;
import com.google.zxing.client.result.ParsedResult;
import com.google.zxing.client.result.ResultParser;

public final class BarcodeScanner extends MarkScanner
{
	/**
	 * Logger for this class
	 */
	static final Log	logger	= LogFactory.getLog(BarcodeScanner.class);

	static final double	BARCODE_AREA_PERCENT	= 0.5d;

	private BufferedImage	subimage;

	public BarcodeScanner(OMRProcessor omr, PageImage imagen, boolean medianfilter)
	{
		super(omr,imagen,medianfilter);
	}
	/**
	 * 
	 */
	public String getParsedCode(Field campo) throws MarkScannerException
	{
		return getParsedCode(scanField(campo));
	}
	
	/**
	 * @param result
	 * @return
	 */
	public String getParsedCode(ScanResult result)
	{
		String barcode;
		if (result!=null)
		{
		ParsedResult parsedResult = ResultParser.parseResult((Result) result.getResult());
	      
	      //System.out.println(imagen.toString() + " (format: " + result.getBarcodeFormat() +", type: " + parsedResult.getType() + "):\nRaw result:\n" + result.getText() +"\nParsed result:\n" + parsedResult.getDisplayResult());
	    barcode = parsedResult.getDisplayResult();
		}
		else
		barcode=null;
		
		return barcode;
	}
	/**
	 * Generates an expanded boundingbox in milimeters
	 * 
	 * @see {@link #BARCODE_AREA_PERCENT}
	 * @see {@value #BARCODE_AREA_PERCENT}
	 * @param rect
	 * @return milimeteres
	 */
	protected Rectangle2D getExpandedArea(Rectangle2D rect)
	{
		Rectangle expandedRect=new Rectangle();
		expandedRect.setFrame((rect.getX()-rect.getWidth()*(BARCODE_AREA_PERCENT)/2),
						(rect.getY()-rect.getHeight()*(BARCODE_AREA_PERCENT)/2),
						(rect.getWidth()*(1+BARCODE_AREA_PERCENT)),
						(rect.getHeight()*(1+BARCODE_AREA_PERCENT)));
		return expandedRect;
	}
	/**
	 * @param rect area in milimeters to be scanned
	 * @return
	 * @throws ReaderException
	 */
	public ScanResult scanAreaForFieldData(Rectangle2D rect)	throws MarkScannerException
	{
		//[JPC] Need to be TYPE_BYTE_GRAY 
		  // BufferedImageMonochromeBitmapSource seems to work bad with TYPE_BYTERGB
		  
		 SubImage subimage;
		try
		{
			subimage=pageImage.getSubimage(rect, BufferedImage.TYPE_BYTE_GRAY);
		}
		catch (RasterFormatException e2)
		{
			logger.error(e2);
			throw new MarkScannerException(e2);
		}
		
		if (subimage == null)
			{
			  logger.error("leerBarcode(Campo) - " + pageImage.toString() + ": No es posible cargar la imagen", null); //$NON-NLS-1$ //$NON-NLS-2$
			  //Lanzar otra Excepcion
			  throw new RuntimeException("Can't extract subimage from page.");
			}
		if (logger.isDebugEnabled())
			OMRUtils.logSubImage(omr, "codebar2D",subimage);  
		
	    MonochromeBitmapSource source = new BufferedImageMonochromeBitmapSource(subimage);
	    Result result=null;
		try
		{
			result = new MultiFormatReader().decode(source,null);
		}
		catch (ReaderException e)
		{
			//retry after filtering
		if(medianfilter == true)
			 {
				if (logger.isDebugEnabled())
					OMRUtils.logSubImage(omr,subimage);
	
				long start=System.currentTimeMillis();
				BufferedImage medianed= medianFilter(subimage);
				logger.debug("scanAreaForBarcode(MedianFilter area=" + subimage.getWidth()+"x"+subimage.getHeight() + ") In (ms) "+(System.currentTimeMillis()-start)); //$NON-NLS-1$ //$NON-NLS-2$
				 
				 if (logger.isDebugEnabled())
					 OMRUtils.logSubImage(omr,"debug_median",medianed);
				 
				 source = new BufferedImageMonochromeBitmapSource(medianed);
				 try
				{
					result = new MultiFormatReader().decode(source, null);
				}
				catch (ReaderException e1)
				{
					 if (logger.isErrorEnabled())
						 {
						 logger.error("Can't recognize any code in the field located at: "+rect+"(see debug output image)",e1);
						 OMRUtils.logSubImage(this.omr,"debug_monochrome_barcode",subimage);
						 }
					throw new MarkScannerException(e1);
				}
				
				 //subimage=medianed; // store medianed image for reporting
				
			 }
		else
		{
				if (logger.isErrorEnabled()) {
					logger.error(
							"Can't recognize any code in the field located at: "
									+ rect + "(see debug output image)", e);
					OMRUtils.logSubImage(this.omr,"debug_monochrome_barcode", subimage);
				}

				throw new MarkScannerException(e); // re-throw exception to
													// notify caller
			}
		}
		this.lastResult=new ScanResult("Barcode",result);
		lastResult.setLocation(rect);
		return lastResult;
	}

	/**
	 * @param campo
	 */
	public void markBarcode(Field campo)
	{
		try
		{
			//get bbox in pixels
			Rectangle rect=pageImage.toPixels(campo.getBBox());
			// expand the area for some tolerance
			Rectangle2D expandedArea = getExpandedArea(campo.getBBox());
			Rectangle expandedRect = pageImage.toPixels(expandedArea);
			
			Graphics2D g = pageImage.getReportingGraphics();
			AffineTransform t=g.getTransform();
			g.setStroke(new BasicStroke(1,BasicStroke.CAP_SQUARE,BasicStroke.JOIN_ROUND,1,new float[]{(float) (3/t.getScaleX()),(float) (6/t.getScaleY())},0));
			if (lastResult!=null)
				g.setColor(Color.BLUE);
			else 
				g.setColor(Color.RED);
			
			
			g.drawRoundRect(rect.x, rect.y, rect.width, rect.height, 3, 3);
			g.drawRoundRect(expandedRect.x, expandedRect.y, expandedRect.width, expandedRect.height, 3, 3);
			
			
			//g.setFont(new Font("Arial",Font.PLAIN,(int) (12/t.getScaleX())));
			if (lastResult!=null)
				g.drawString(((Result)lastResult.getResult()).getBarcodeFormat().toString()+"="+getParsedCode(lastResult), rect.x, rect.y);
		}
		catch (Exception e)
		{
			logger.error("Unexpected errr while logging the image:",e);
		}
		
	}
	@Override
	public void putEmphasisMarkOnImage(PageImage pageImage2)
	{
		if (lastResult!=null)
		{
			OMRUtils.logFrame(pageImage2, lastResult.getLocation(), Color.RED, "BarCodeDetected");
		}
		
	}
}
