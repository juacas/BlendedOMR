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
	public Result leerBarcode(Campo campo) throws ReaderException  {
		 
		  
		  
		  //se leen y almacenan las coordenadas
		  double[] coords = campo.getCoordenadas();
			Rectangle expandedArea = getExpandedArea(coords);
			Rectangle area=getRectArea(coords);
	    Result result;
		try
		{
			result = scanAreaForBarcode(area);
		}
		catch (ReaderException e)
		{
			//Try with a wider area
		
			result = scanAreaForBarcode(expandedArea);
			//for report
			subimage=subimage.getSubimage(area.x-expandedArea.x, area.y-expandedArea.y, area.width, area.height);
		}
	      this.lastResult=result;
	      return result;
	  }

	/**
	 * @param rect
	 * @return
	 * @throws ReaderException
	 */
	private Result scanAreaForBarcode(Rectangle rect) throws ReaderException
	{
		//[JPC] Need to be TYPE_BYTE_GRAY 
		  // BufferedImageMonochromeBitmapSource seems to work bad with TYPE_BYTERGB
		  
		  BufferedImage barcodeArea = pageImage.getImagen().getSubimage(rect.x,rect.y,rect.width,rect.height);		//se coge la subimagen, x,y,w,h (en píxeles)
		  if (barcodeArea == null)
			{
			  logger.error("leerBarcode(Campo) - " + pageImage.getImagen().toString() + ": No es posible cargar la imagen", null); //$NON-NLS-1$ //$NON-NLS-2$
			  //TODO: Lanzar Excepcion
			}
		  
		  subimage=new BufferedImage(rect.width,rect.height,BufferedImage.TYPE_BYTE_GRAY);
		  Graphics g=subimage.createGraphics();
		  g.drawImage(barcodeArea,0,0,null);
		  
		
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
		ParsedResult parsedResult = ResultParser.parseResult(result);
	      
	      //System.out.println(imagen.toString() + " (format: " + result.getBarcodeFormat() +", type: " + parsedResult.getType() + "):\nRaw result:\n" + result.getText() +"\nParsed result:\n" + parsedResult.getDisplayResult());
	      barcode = parsedResult.getDisplayResult();
		return barcode;
	}

	/**
	 * @param coords
	 * @return
	 */
	private Rectangle getRectArea(double[] coords)
	{
		int x = this.pageImage.toPixelsX(coords[0]);		//concentriccirclewidth//posición de la x
		  int y = this.pageImage.toPixelsY(coords[1]);		//posición de la y
		  int width = this.pageImage.toPixelsX(coords[2]);	//anchura en píxeles
		  int height = this.pageImage.toPixelsY(coords[3]);	//altura en píxeles
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
		subimage=result;
		return subimage;

}

	/**
	 * @param campo
	 * @return
	 * @throws ReaderException 
	 */
	public String getParsedCode(Campo campo) throws ReaderException
	{
		return getParsedCode(leerBarcode(campo));
	}

	/**
	 * @param campo
	 */
	public void markBarcode(Campo campo)
	{
		Rectangle rect=getRectArea(campo.getCoordenadas());
		// expand the area for some tolerance
		Rectangle expandedRect = getExpandedArea(campo.getCoordenadas());
		Graphics2D g=this.pageImage.getImagen().createGraphics();
	
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
	 * @param rect
	 * @return
	 */
	private Rectangle getExpandedArea(double coords[])
	{
		Rectangle rect=getRectArea(coords);
		
		
		Rectangle expandedRect=new Rectangle((int)(rect.x-rect.width*(BARCODE_AREA_PERCENT)/2),
						(int)(rect.y-rect.height*(BARCODE_AREA_PERCENT)/2),
						(int)(rect.width*(1+BARCODE_AREA_PERCENT)),
						(int)(rect.height*(1+BARCODE_AREA_PERCENT)));
		return expandedRect;
	}
}
