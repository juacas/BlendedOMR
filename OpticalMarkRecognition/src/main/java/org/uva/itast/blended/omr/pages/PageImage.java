/**
 * 
 */
package org.uva.itast.blended.omr.pages;

import omrproj.ImageManipulation;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.util.Date;

import javax.imageio.ImageIO;

import net.sourceforge.jiu.color.reduction.RGBToGrayConversion;
import net.sourceforge.jiu.data.ByteChannelImage;
import net.sourceforge.jiu.data.Gray8Image;
import net.sourceforge.jiu.data.PixelImage;
import net.sourceforge.jiu.data.RGB24Image;
import net.sourceforge.jiu.filters.MedianFilter;
import net.sourceforge.jiu.gui.awt.BufferedRGB24Image;
import net.sourceforge.jiu.gui.awt.ImageCreator;
import net.sourceforge.jiu.ops.MissingParameterException;
import net.sourceforge.jiu.ops.WrongParameterException;

/**
 * @author juacas
 */
public abstract class PageImage
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
			BufferedImage imagen=getImagen();
			long taskStart = System.currentTimeMillis();
		//	WritableRaster raster = imagen.copyData( null );
		//	BufferedImage copy = new BufferedImage( imagen.getColorModel(), raster, imagen.isAlphaPremultiplied(), null );
		//	logger.debug("\tOriginal image copied in (ms)" + (System.currentTimeMillis() - taskStart)); //$NON-NLS-1$
			//It's not necessary to keep a copy. concersion to Gray8Image do not touch original.
			 taskStart = System.currentTimeMillis();
			
			grayimage = convertToGrayImage(imagen); // se transforma el
														// BufferedImage en
														// Gray8Image
			
			logger.debug("\tImage converted to GrayImage in (ms)" + (System.currentTimeMillis() - taskStart)); //$NON-NLS-1$
			//setImagen(copy); // preserves original BufferedImage
		}
	
		return grayimage;
	}

	/**
	 * @return the imagen
	 */
	public BufferedImage getImagen()
	{
		if (imagen==null)
		{
			setImagen(createImage());
		}
		return imagen;
	}

	/**
	 * @return
	 */
	abstract BufferedImage createImage();

	private BufferedImage	imagen;

	

	/**
	 * @param imagen the imagen to set
	 */
	protected void setImagen(BufferedImage imagen)
	{
		this.imagen = imagen;
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
	 * Método que a partir de un objeto tipo BufferedImage lo transforma en uno
	 * de tipo Gray8Image
	 * 
	 * @param imagen
	 * @return grayimage
	 */
	Gray8Image convertToGrayImage(BufferedImage imagen)
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
public void medianFilter()
	{
		try
		{
			long funcStart = System.currentTimeMillis();
			MedianFilter filter = new MedianFilter();
			Gray8Image grayimage = getGrayImage();
			
//			filter.setArea((int) ((grayimage.getWidth()/ OMRProcessor._IMAGEWIDTHPIXEL * 25) / 2) * 2 + 1,
//					(int) (grayimage.getHeight()/ OMRProcessor._IMAGEHEIGTHPIXEL * 25 / 2) * 2 + 1);
			filter.setArea(3,3);
			filter.setInputImage(grayimage);

			filter.process();
			
			logger.debug("Page filtered (Median) in (ms)" + (System.currentTimeMillis() - funcStart)); //$NON-NLS-1$

		}
		catch (MissingParameterException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (WrongParameterException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// Gray8Image medianimage = (Gray8Image)(filter.getOutputImage());
}
	private static void alignImage(Gray8Image grayimage)
	{
		long funcStart = System.currentTimeMillis();
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
		logger.debug("Page aligned in (ms)" + (System.currentTimeMillis() - funcStart)); //$NON-NLS-1$

	}

	/**
	 * 
	 */
	public void labelPageAsProcessed()
	{
		BufferedImage imagen = getImagen();
		Gray8Image grayImg = getGrayImage();
		Graphics2D g=imagen.createGraphics();
		g.setColor(Color.RED);
		g.drawString("Page Processed at:"+new Date()+" W:"+imagen.getWidth()+" H:"+imagen.getHeight()+"  workcopy W: "+grayImg.getWidth()+" H:"+grayImg.getHeight(), 10, 10);
		
		
	}

	/**
	 * @return
	 */
	public abstract String getType();

	/**
	 * @return
	 */
	public abstract String getFileName();

	/**
	 * Free up memory resources
	 */
	public void freeMemory()
	{
		long freeMem=Runtime.getRuntime().freeMemory();
		long availMem=Runtime.getRuntime().totalMemory();
		grayimage=null;
		
		imagen.flush();
		setImagen(null);
		System.gc();

		if (logger.isDebugEnabled())
		{
			logger.debug("endUse() -TotalMem"+availMem/1024/1024+" MB freeMem Before=" + freeMem/1024/1024 + ", freeMem After=" + Runtime.getRuntime().freeMemory()/1024/1024); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}

	/**
	 * @param outputdir
	 * @throws IOException 
	 */
	public void outputMarkedPage(String outputdir) throws IOException
	{
		File debugImagePath;
							
			debugImagePath = File.createTempFile("OMR_original_marked", ".jpg", new File(outputdir));
			ImageIO.write(getImagen(), "JPG", debugImagePath);
		
	}

	/**
	 * @param outputdir
	 * @throws IOException 
	 */
	public void outputWorkingPage(String outputdir) throws IOException
	{
		File debugImagePath;
		
		debugImagePath = File.createTempFile("OMR_working_debug", ".jpg", new File(outputdir));
		Gray8Image grayImage=getGrayImage();
		
		Image img=ImageCreator.convertToAwtImage(grayImage,255);
		BufferedImage out=new BufferedImage(grayImage.getWidth(),grayImage.getHeight(),BufferedImage.TYPE_INT_RGB);
		out.createGraphics().drawImage(img, 0, 0, null);
		
		ImageIO.write(out, "JPG", debugImagePath);
	
	}

	/**
	 * @return
	 */
	public abstract double getHorizontalRatio();
	public abstract double getVerticalRatio();

	/**
	 * @param x
	 * @return
	 */
	public int toPixelsX(double x)
	{
		return (int) (x * this.getHorizontalRatio());
	}
	public int toPixelsY(double y)
	{
		return (int) (y * this.getVerticalRatio());
	}
}
