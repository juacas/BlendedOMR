/**
 * 
 */
package org.uva.itast.blended.omr;

import omrproj.ImageManipulation;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.util.Date;

import net.sourceforge.jiu.color.reduction.RGBToGrayConversion;
import net.sourceforge.jiu.data.Gray8Image;
import net.sourceforge.jiu.data.PixelImage;
import net.sourceforge.jiu.data.RGB24Image;
import net.sourceforge.jiu.gui.awt.BufferedRGB24Image;

/**
 * @author juacas
 */
public class PageImage
{
	/**
	 * Logger for this class
	 */
	private static final Log	logger	= LogFactory.getLog(PageImage.class);

	private Gray8Image			grayimage;

	/**
	 * @return the grayimage
	 */
	public Gray8Image getGrayImage()
	{
		
	
		if (grayimage==null)
		{
			long taskStart = System.currentTimeMillis();
			WritableRaster raster = imagen.copyData( null );
			BufferedImage copy = new BufferedImage( imagen.getColorModel(), raster, imagen.isAlphaPremultiplied(), null );
			logger.debug("\tOriginal image copied in (ms)" + (System.currentTimeMillis() - taskStart)); //$NON-NLS-1$
			
			 taskStart = System.currentTimeMillis();
			
			grayimage = convertirAGrayImage(imagen); // se transforma el
														// BufferedImage en
														// Gray8Image
			logger.debug("\tImage converted to GrayImage in (ms)" + (System.currentTimeMillis() - taskStart)); //$NON-NLS-1$
			this.imagen=copy; // preserves original BufferedImage
		}
	
		return grayimage;
	}

	/**
	 * @return the imagen
	 */
	public BufferedImage getImagen()
	{
		return imagen;
	}

	private BufferedImage	imagen;

	/**
	 * 
	 * @param imagen
	 * @param align
	 */
	public PageImage(BufferedImage imagen, boolean align)
	{
		
		this(imagen);
		
		if (align == true)
		{
			align();
		}
		
	}

	/**
	 * 
	 */
	public void align()
	{
		long taskStart = System.currentTimeMillis();
		alignImage(getGrayImage());
		logger.debug("\tImage alligned in (ms)" + (System.currentTimeMillis() - taskStart)); //$NON-NLS-1$
	}

	/**
	 * @param imagen2
	 */
	public PageImage(BufferedImage imagen)
	{
		this.imagen=imagen;
		
	}

	/**
	 * Método que a partir de un objeto tipo BufferedImage lo transforma en uno
	 * de tipo Gray8Image
	 * 
	 * @param imagen
	 * @return grayimage
	 */
	Gray8Image convertirAGrayImage(BufferedImage imagen)
	{
		Gray8Image grayimage = null;
		RGB24Image redimage = null;
		try
		{
			PixelImage image = new BufferedRGB24Image(imagen);
			if (image.getImageType().toString().indexOf("RGB") != -1)
			{
				redimage = (RGB24Image) image;
				RGBToGrayConversion rgbtogray = new RGBToGrayConversion();
				rgbtogray.setInputImage(redimage);
				rgbtogray.process();
				grayimage = (Gray8Image) (rgbtogray.getOutputImage());
			} else if (image.getImageType().toString().indexOf("Gray") != -1)
			{
				grayimage = (Gray8Image) (image);
			} else
			{
				grayimage = null;
			}
		}
		catch (Exception e)
		{
			throw new RuntimeException("Can't convert image.");
		}
		return grayimage;
	}

	private static void alignImage(Gray8Image grayimage)
	{
		ImageManipulation imageManipulator = new ImageManipulation(grayimage); // se
		// crea
		// un
		// objeto
		// image
		// que
		// nos
		// permita
		// trabajar
		// con
		// la
		// imagen
		imageManipulator.locateConcentricCircles(); // se alinea si esta marcada
		// la bandera de alineación
	}

	/**
	 * 
	 */
	public void markProcessing()
	{
		BufferedImage imagen = getImagen();
		Gray8Image grayImg = getGrayImage();
		Graphics2D g=imagen.createGraphics();
		g.setColor(Color.RED);
		g.drawString("Page Processed at:"+new Date()+" W:"+imagen.getWidth()+" H:"+imagen.getHeight()+"  workcopy W: "+grayImg.getWidth()+" H:"+grayImg.getHeight(), 10, 10);
		
		
	}
}
