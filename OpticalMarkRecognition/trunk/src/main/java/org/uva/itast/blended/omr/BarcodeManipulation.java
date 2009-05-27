/*
 * BarcodeManipulation.java
 *
 * Creado en Abril-Mayo de 2009
 *
 */

package org.uva.itast.blended.omr;

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
import org.uva.itast.blended.omr.pages.PageImage;

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
public final class BarcodeManipulation
{
	/**
	 * Logger for this class
	 */
	private static final Log	logger	= LogFactory.getLog(BarcodeManipulation.class);

	private static final double	BARCODE_AREA_PERCENT	= 0.5d;

	private Result	lastResult;
	private PageImage	pageImage;
	private boolean	medianfilter;

	private BufferedImage	subimage;

	public BarcodeManipulation(PageImage imagen, boolean medianfilter)
	{
		this.pageImage=imagen;
		this.medianfilter=false;//medianfilter;
	}
	
	/**
	 * Método que lee el valor de un código de barras contenido en un objeto tipo BufferedImage a partir de los patrones dados en un objeto tipo Campo
	 * @param campo
	 * @param imagen
	 * @param medianfilter 
	 * @return
	 * @throws ReaderException
	 */
	public Result scanField(Campo campo) throws ReaderException  {
		 
		  
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
	//TODO sustituir por subimage de pageimage	  
		 BufferedImage subimage = pageImage.getSubimage(rect, BufferedImage.TYPE_BYTE_BINARY);		//se coge la subimagen, x,y,w,h (en píxeles)
		 //BufferedImage subImage=new BufferedImage(subImage1.getWidth(),subImage1.getHeight(),BufferedImage.TYPE_BYTE_GRAY);
		 //subImage.getGraphics().drawImage(subImage, 0, 0, null);
		  if (subimage == null)
			{
			  logger.error("leerBarcode(Campo) - " + pageImage.toString() + ": No es posible cargar la imagen", null); //$NON-NLS-1$ //$NON-NLS-2$
			  //TODO: Lanzar Excepcion
			  throw new RuntimeException("Can't extract subimage from page.");
			}
		  
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
				 BufferedImage medianed= medianFilter(subimage);
				
				 source = new BufferedImageMonochromeBitmapSource(medianed);
				 result = new MultiFormatReader().decode(source, null);
				 Graphics g2=medianed.createGraphics();
				 g2.setColor(Color.BLACK);
				 g2.drawString("MEDIAN",medianed.getWidth()/2-20, medianed.getHeight()/2);
				 subimage=medianed; // store medianed image for reporting
			 }
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
	public String getParsedCode(Campo campo) throws ReaderException
	{
		return getParsedCode(scanField(campo));
	}

	/**
	 * @param campo
	 */
	public void markBarcode(Campo campo)
	{
		//get bbox in pixels
		Rectangle rect=pageImage.toPixels(campo.getBBox());
		// expand the area for some tolerance
		Rectangle2D expandedArea = getExpandedArea(campo.getBBox());
		Rectangle expandedRect = pageImage.toPixels(expandedArea);
		
		Graphics2D g = pageImage.getReportingGraphics();
//	if (logger.isDebugEnabled())
//		{
//			//filtered image is on subimage
//			g.drawImage(subimage,rect.x,rect.y,null);
//		}
		g.setStroke(new BasicStroke(3));
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
