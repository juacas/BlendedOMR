/*
 * BarcodeManipulation.java
 *
 * Creado en Abril-Mayo de 2009
 *
 */

package org.uva.itast.blended.omr.scanners;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.uva.itast.blended.omr.Field;
import org.uva.itast.blended.omr.UtilidadesFicheros;
import org.uva.itast.blended.omr.pages.PageImage;
import org.uva.itast.blended.omr.pages.SubImage;

import com.google.zxing.MonochromeBitmapSource;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.ReaderException;
import com.google.zxing.Result;
import com.google.zxing.client.j2se.BufferedImageMonochromeBitmapSource;
import com.google.zxing.client.result.ParsedResult;
import com.google.zxing.client.result.ResultParser;

/**
 * 
 * @author Jesús Rodilana
 *
 */
public final class BarcodeScanner
{
	/**
	 * Logger for this class
	 */
	private static final Log	logger	= LogFactory.getLog(BarcodeScanner.class);

	private static final double	BARCODE_AREA_PERCENT	= 0.5d;

	private Result	lastResult;
	private PageImage	pageImage;
	private boolean	medianfilter;

	private BufferedImage	subimage;

	public BarcodeScanner(PageImage imagen, boolean medianfilter)
	{
		this.pageImage=imagen;
		this.medianfilter=medianfilter;
	}
	
	/**
	 * Método que lee el valor de un código de barras contenido en un objeto tipo BufferedImage a partir de los patrones dados en un objeto tipo Campo
	 * @param campo
	 * @param imagen
	 * @param medianfilter 
	 * @return
	 * @throws ReaderException
	 */
	public Result scanField(Field campo) throws ReaderException  {
		 
		  
		  //se leen y almacenan las coordenadas
		Rectangle2D coords = campo.getBBox();
		Rectangle2D expandedBbox = getExpandedArea(coords);
//		Rectangle area=pageImage.toPixels(coords);
//		Rectangle expandedArea=pageImage.toPixels(expandedBbox);
		
	    Result result;
		try
		{
			result = scanAreaForBarcode(coords);
		}
		catch (ReaderException e)
		{
			//Try with a wider area
		
			result = scanAreaForBarcode(expandedBbox);
			
		}
	      this.lastResult=result;
	      return result;
	  }

	/**
	 * @param rect area in milimeters to be scanned //TODO
	 * @return
	 * @throws ReaderException
	 */
	private Result scanAreaForBarcode(Rectangle2D rect) throws ReaderException
	{
		//[JPC] Need to be TYPE_BYTE_GRAY 
		  // BufferedImageMonochromeBitmapSource seems to work bad with TYPE_BYTERGB
		  
		 SubImage subimage = pageImage.getSubimage(rect, BufferedImage.TYPE_BYTE_GRAY);		//se coge la subimagen, x,y,w,h (en píxeles)
	
		  if (subimage == null)
			{
			  logger.error("leerBarcode(Campo) - " + pageImage.toString() + ": No es posible cargar la imagen", null); //$NON-NLS-1$ //$NON-NLS-2$
			  //TODO: Lanzar Excepcion
			  throw new RuntimeException("Can't extract subimage from page.");
			}
		if (logger.isDebugEnabled())
			UtilidadesFicheros.logSubImage(subimage);  
		
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
				 UtilidadesFicheros.logSubImage(subimage);

				long start=System.currentTimeMillis();
				BufferedImage medianed= medianFilter(subimage);
				logger.debug("scanAreaForBarcode(MedianFilter area=" + subimage.getWidth()+"x"+subimage.getHeight() + ") In (ms) "+(System.currentTimeMillis()-start)); //$NON-NLS-1$ //$NON-NLS-2$
				 
				 if (logger.isDebugEnabled())
					 UtilidadesFicheros.logSubImage("debug_median",medianed);
				 
				 source = new BufferedImageMonochromeBitmapSource(medianed);
				 result = new MultiFormatReader().decode(source, null);
				
				 //subimage=medianed; // store medianed image for reporting
				
			 }
		else
			throw e; // re-throw exception to notify caller 
		}
		return result;
	}

	/**
	 * @param result
	 * @return
	 */
	public String getParsedCode(Result result)
	{
		String barcode;
		if (result!=null)
		{
		ParsedResult parsedResult = ResultParser.parseResult(result);
	      
	      //System.out.println(imagen.toString() + " (format: " + result.getBarcodeFormat() +", type: " + parsedResult.getType() + "):\nRaw result:\n" + result.getText() +"\nParsed result:\n" + parsedResult.getDisplayResult());
	    barcode = parsedResult.getDisplayResult();
		}
		else
		barcode=null;
		
		return barcode;
	}

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
	 * Aplica un filtro para reconstruir imagenes de mala calidad, a través del valor de los píxeles vecinos
	 * @param subimage
	 */
private static BufferedImage medianFilter(BufferedImage subimage) {
	
		com.jhlabs.image.MedianFilter filter = new com.jhlabs.image.MedianFilter();
		BufferedImage result=filter.createCompatibleDestImage(subimage,subimage.getColorModel());
		filter.filter(subimage, result);
		
		return result;

}

	/**
	 * @param campo
	 * @return
	 * @throws ReaderException 
	 */
	public String getParsedCode(Field campo) throws ReaderException
	{
		return getParsedCode(scanField(campo));
	}

	/**
	 * @param campo
	 */
	public void markBarcode(Field campo)
	{
		//get bbox in pixels
		Rectangle rect=pageImage.toPixels(campo.getBBox());
		// expand the area for some tolerance
		Rectangle2D expandedArea = getExpandedArea(campo.getBBox());
		Rectangle expandedRect = pageImage.toPixels(expandedArea);
		
		Graphics2D g = pageImage.getReportingGraphics();

		g.setStroke(new BasicStroke(1,BasicStroke.CAP_SQUARE,BasicStroke.JOIN_ROUND,1,new float[]{3,6},0));
		if (lastResult!=null)
			g.setColor(Color.BLUE);
		else 
			g.setColor(Color.RED);
		
		
		g.drawRoundRect(rect.x, rect.y, rect.width, rect.height, 3, 3);
		g.drawRoundRect(expandedRect.x, expandedRect.y, expandedRect.width, expandedRect.height, 3, 3);
		
		if (lastResult!=null)
			g.drawString(lastResult.getBarcodeFormat().toString()+"="+getParsedCode(lastResult), rect.x, rect.y);
		
	}

	

	/**
	 * Generates an expanded boundingbox in milimeters
	 * 
	 * @see {@link #BARCODE_AREA_PERCENT}
	 * @see {@value #BARCODE_AREA_PERCENT}
	 * @param rect
	 * @return milimeteres
	 */
	private Rectangle2D getExpandedArea(Rectangle2D rect)
	{
		Rectangle expandedRect=new Rectangle();
		expandedRect.setFrame((rect.getX()-rect.getWidth()*(BARCODE_AREA_PERCENT)/2),
						(rect.getY()-rect.getHeight()*(BARCODE_AREA_PERCENT)/2),
						(rect.getWidth()*(1+BARCODE_AREA_PERCENT)),
						(rect.getHeight()*(1+BARCODE_AREA_PERCENT)));
		return expandedRect;
	}
}
