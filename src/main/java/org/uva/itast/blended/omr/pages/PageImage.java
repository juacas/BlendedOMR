/**
 * 
 */
package org.uva.itast.blended.omr.pages;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Date;

import javax.imageio.ImageIO;

/*import net.sourceforge.jiu.color.reduction.RGBToGrayConversion;
import net.sourceforge.jiu.data.Gray8Image;
import net.sourceforge.jiu.data.PixelImage;
import net.sourceforge.jiu.data.RGB24Image;
import net.sourceforge.jiu.filters.MedianFilter;
import net.sourceforge.jiu.gui.awt.BufferedRGB24Image;
import net.sourceforge.jiu.gui.awt.ImageCreator;
import net.sourceforge.jiu.ops.MissingParameterException;
import net.sourceforge.jiu.ops.WrongParameterException;
import omrproj.ImageManipulation;*/

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * @author juacas
 */
public abstract class PageImage {
	/**
	 * Logger for this class
	 */
	private static final Log logger = LogFactory.getLog(PageImage.class);
	
	public static double		a4width		= 210;										// mm
	public static double		a4height	= 290;										// mm

	//private Gray8Image grayimage;//XXX grayimage==>workimage

	/**
	 * @return the grayimage
	 */
	/*public Gray8Image getGrayImage() {

		if (grayimage == null) {
			BufferedImage imagen = getImagen();
			long taskStart = System.currentTimeMillis();
			// WritableRaster raster = imagen.copyData( null );
			// BufferedImage copy = new BufferedImage( imagen.getColorModel(),
			// raster, imagen.isAlphaPremultiplied(), null );
			//	logger.debug("\tOriginal image copied in (ms)" + (System.currentTimeMillis() - taskStart)); //$NON-NLS-1$
			// It's not necessary to keep a copy. concersion to Gray8Image do
			// not touch original.
			taskStart = System.currentTimeMillis();

			// se transforma el BufferedImage en Gray8Image
			grayimage = convertToGrayImage(imagen);

			logger
					.debug("\tImage converted to GrayImage in (ms)" + (System.currentTimeMillis() - taskStart)); //$NON-NLS-1$
			// setImagen(copy); // preserves original BufferedImage
		}

		return grayimage;
	}*/
	
	public BufferedImage getWorkImage() {

		if (workimage == null) {
			workimage = getImagen();
			long taskStart = System.currentTimeMillis();
			// WritableRaster raster = imagen.copyData( null );
			// BufferedImage copy = new BufferedImage( imagen.getColorModel(),
			// raster, imagen.isAlphaPremultiplied(), null );
			//	logger.debug("\tOriginal image copied in (ms)" + (System.currentTimeMillis() - taskStart)); //$NON-NLS-1$
			// It's not necessary to keep a copy. concersion to Gray8Image do
			// not touch original.
			taskStart = System.currentTimeMillis();

			logger
					.debug("\tImage converted to GrayImage in (ms)" + (System.currentTimeMillis() - taskStart)); //$NON-NLS-1$
			// setImagen(copy); // preserves original BufferedImage
		}

		return workimage;
	}
	
	/**
	 * @return the imagen
	 */
	public BufferedImage getImagen() {
		if (imagen == null) {
			setImagen(createImage());
		}
		return imagen;
	}

	/**
	 * @return
	 */
	abstract BufferedImage createImage();
	
	private BufferedImage imagen;
	private BufferedImage workimage;

	/**
	 * @param imagen
	 *            the imagen to set
	 */
	protected void setImagen(BufferedImage imagen) {
		this.imagen = imagen;
	}

	/**
	 * 
	 */
	public void align() {
		long taskStart = System.currentTimeMillis();
		//alignImage(getGrayImage());
		alignImage(getWorkImage());
		logger
				.debug("\tImage alligned in (ms)" + (System.currentTimeMillis() - taskStart)); //$NON-NLS-1$
	}

	/**
	 * Método que a partir de un objeto tipo BufferedImage lo transforma en uno
	 * de tipo Gray8Image
	 * 
	 * @param imagen
	 * @return grayimage
	 */
	/*Gray8Image convertToGrayImage(BufferedImage imagen) {
		Gray8Image grayimage = null;
		RGB24Image redimage = null;
		try {
			PixelImage image = new BufferedRGB24Image(imagen);
			if (image.getImageType().toString().indexOf("RGB") != -1) {
				redimage = (RGB24Image) image;
				RGBToGrayConversion rgbtogray = new RGBToGrayConversion();
				rgbtogray.setInputImage(redimage);
				rgbtogray.process();
				grayimage = (Gray8Image) (rgbtogray.getOutputImage());
			} else if (image.getImageType().toString().indexOf("Gray") != -1) {
				grayimage = (Gray8Image) (image);
			} else {
				grayimage = null;
			}
		} catch (Exception e) {
			throw new RuntimeException("Can't convert image.");
		}
		return grayimage;
	}*/

	/**
	 * Método para filtrar toda la página
	 */
	public void medianFilter() {
		
		long funcStart = System.currentTimeMillis();
		
		com.jhlabs.image.MedianFilter filter = new com.jhlabs.image.MedianFilter();
		workimage = getWorkImage();
		
		BufferedImage result=filter.createCompatibleDestImage(workimage,workimage.getColorModel());
		filter.filter(workimage, result);
		workimage=result;
		
		logger.debug("Page filtered (Median) in (ms)" + (System.currentTimeMillis() - funcStart)); //$NON-NLS-1$
		
	}

	//private static void alignImage(Gray8Image grayimage) {
	private static void alignImage(BufferedImage image) {//XXX FALTA
		long funcStart = System.currentTimeMillis();
		//ImageManipulation imageManipulator = new ImageManipulation(grayimage); // se
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
		//imageManipulator.locateConcentricCircles(); // se alinea si esta marcada
		// la bandera de alineación
		logger
				.debug("Page aligned in (ms)" + (System.currentTimeMillis() - funcStart)); //$NON-NLS-1$

	}

	/**
	 * 
	 */
	public void labelPageAsProcessed() {
		BufferedImage imagen = getImagen();
		//Gray8Image grayImg = getGrayImage();
		Graphics2D g = imagen.createGraphics();
		g.setColor(Color.RED);
		//g.drawString("Page Processed at:" + new Date() + " W:"
			//	+ imagen.getWidth() + " H:" + imagen.getHeight()
			//	+ "  workcopy W: " + grayImg.getWidth() + " H:"
			//	+ grayImg.getHeight(), 10, 10);

		g.drawString("Page Processed at:" + new Date() + " W:"
				+ imagen.getWidth() + " H:" + imagen.getHeight()
				+ "  workcopy W: " + workimage.getWidth() + " H:"
				+ workimage.getHeight(), 10, 10);

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
	public void freeMemory() {
		long freeMem = Runtime.getRuntime().freeMemory();
		long availMem = Runtime.getRuntime().totalMemory();
		//grayimage = null;
		workimage = null;

		imagen.flush();
		setImagen(null);
		System.gc();

		if (logger.isDebugEnabled()) {
			logger
					.debug("endUse() -TotalMem" + availMem / 1024 / 1024 + " MB freeMem Before=" + freeMem / 1024 / 1024 + ", freeMem After=" + Runtime.getRuntime().freeMemory() / 1024 / 1024); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}

	/**
	 * @param outputdir
	 * @throws IOException
	 */
	public void outputMarkedPage(String outputdir) throws IOException {
		File debugImagePath;

		debugImagePath = File.createTempFile("OMR_original_marked", ".jpg",
				new File(outputdir));
		ImageIO.write(getImagen(), "JPG", debugImagePath);

	}

	/**
	 * @param outputdir
	 * @throws IOException
	 */
	public void outputWorkingPage(String outputdir) throws IOException {
		File debugImagePath;

		debugImagePath = File.createTempFile("OMR_working_debug", ".jpg",
				new File(outputdir));
		//Gray8Image grayImage = getGrayImage();
		BufferedImage workImage = getWorkImage();

		//Image img = ImageCreator.convertToAwtImage(grayImage, 255);
		//BufferedImage out = new BufferedImage(grayImage.getWidth(), grayImage
			//	.getHeight(), BufferedImage.TYPE_INT_RGB);
		
		BufferedImage out = new BufferedImage(workImage.getWidth(), workImage
				.getHeight(), BufferedImage.TYPE_INT_RGB);
		
		//out.createGraphics().drawImage(img, 0, 0, null);
		
		out.createGraphics().drawImage(workImage, 0, 0, null);

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
	public int toPixelsX(double x) {
		return (int) (x * this.getHorizontalRatio());
	}

	public int toPixelsY(double y) {
		return (int) (y * this.getVerticalRatio());
	}
}
