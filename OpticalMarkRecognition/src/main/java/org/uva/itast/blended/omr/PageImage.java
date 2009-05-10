/**
 * 
 */
package org.uva.itast.blended.omr;

import omrproj.ImageManipulation;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.awt.image.BufferedImage;

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
		this.imagen = imagen;
		long taskStart = System.currentTimeMillis();
		grayimage = convertirAGrayImage(imagen); // se transforma el
													// BufferedImage en
													// Gray8Image
		logger
				.debug("\tImage converted to GrayImage in (ms)" + (System.currentTimeMillis() - taskStart)); //$NON-NLS-1$

		if (align == true)
		{
			taskStart = System.currentTimeMillis();
			alignImage(grayimage);
			logger
					.debug("\tImage alligned in (ms)" + (System.currentTimeMillis() - taskStart)); //$NON-NLS-1$
		}
		
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
}
