/**
 * 
 */
package org.uva.itast.blended.omr.scanners;

import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.uva.itast.blended.omr.Field;
import org.uva.itast.blended.omr.pages.PageImage;

import com.google.zxing.ReaderException;

/*
 *
 * @author Juan Pablo de Castro
 * @author juacas
 */
public abstract class MarkScanner
{
	/**
	 * Logger for this class
	 */
	private static final Log	logger	= LogFactory.getLog(MarkScanner.class);

	ScanResult	lastResult;
	PageImage	pageImage;
	boolean medianfilter=false;
	/**
	 * Aplica un filtro para reconstruir imagenes de mala calidad, a través del valor de los píxeles vecinos
	 * @param subimage
	 */
	public static BufferedImage medianFilter(BufferedImage subimage)
	{
		
			com.jhlabs.image.MedianFilter filter = new com.jhlabs.image.MedianFilter();
			BufferedImage result=filter.createCompatibleDestImage(subimage,subimage.getColorModel());
			filter.filter(subimage, result);
			
			return result;
	
	}

	/**
	 * Método que lee el valor de un código de barras contenido en un objeto tipo BufferedImage a partir de los patrones dados en un objeto tipo Campo
	 * @param campo
	 * @param imagen
	 * @param medianfilter 
	 * @return
	 * @throws ReaderException
	 */
	public ScanResult scanField(Field campo) throws MarkScannerException
	{// TODO cambiar Object por un objeto mejor diseñado
			 
			  //se leen y almacenan las coordenadas
			Rectangle2D coords = campo.getBBox();
			Rectangle2D expandedBbox = getExpandedArea(coords);
	//		Rectangle area=pageImage.toPixels(coords);
	//		Rectangle expandedArea=pageImage.toPixels(expandedBbox);
			
		    ScanResult result;
			try
			{
				result = scanAreaForFieldData(coords);
			}
			catch (MarkScannerException e)
			{
				//Try with a wider area
			
				result =  scanAreaForFieldData(expandedBbox);
				
			}
		      this.lastResult=result;
		      return result;
		  }

	
	/**
	 * @param coords
	 * @return
	 */
	public abstract ScanResult scanAreaForFieldData(Rectangle2D coords) throws MarkScannerException;
	/**
	 * Return boundingBox of 
	 * @param coords
	 * @return
	 */
	private Rectangle getRectArea(double[] coords)
	{
		Point2D coordUpperLeft=	pageImage.toPixels(coords[0], coords[1]);
		Point2D coordBottomRight= pageImage.toPixels(coords[0]+coords[2], coords[1]+coords[2]);
		
		int x=(int) Math.min(coordUpperLeft.getX(), coordBottomRight.getX());
		int y=(int) Math.min(coordUpperLeft.getY(), coordBottomRight.getY());
		int xm=(int) Math.max(coordUpperLeft.getX(), coordBottomRight.getX());
		int ym=(int) Math.max(coordUpperLeft.getY(), coordBottomRight.getY());
		int width = xm-x;	//anchura en píxeles
		int height = ym-y;	//altura en píxeles
		
		return new Rectangle(x,y,width,height);
	}

	/**
	 * 
	 */
	public MarkScanner()
	{
		super();
	}

	/**
	 * @param imagen
	 * @param medianfilter
	 */
	public MarkScanner(PageImage imagen, boolean medianfilter)
	{
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
	 * @return milimeteres
	 */
	abstract protected Rectangle2D getExpandedArea(Rectangle2D rect);
	

}