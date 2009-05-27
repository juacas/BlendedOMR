/*
 * ProcessTemplate.java
 *
 * Created on June 30, 2007, 12:12 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package omrproj;

//import java.awt.Image;
//import java.awt.image.RenderedImage;
//import java.io.File;

//import javax.imageio.ImageIO;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import sun.awt.image.ImageFormatException;

import net.sourceforge.jiu.data.*;

/**
 * @author Aaditeshwar Seth
 */
public class ProcessTemplate
{
	/**
	 * Logger for this class
	 */
	private static final Log	logger			= LogFactory
														.getLog(ProcessTemplate.class);
	public static final String	IMAGE_FORMAT	= "png";

	public static void main(String args[])
	{
		String filename = args[0];
		// String filename="2circle-org-colored-whole.tif";
		if (logger.isDebugEnabled())
		{
			logger.debug("main(String[]) - " + filename); //$NON-NLS-1$
		}
		// //convertimos la imagen a tif
		// //PDFToRaster tif_imagen = new PDFToRaster();
		// try
		// {
		// //tif_imagen.convertirPDF(filename,IMAGE_FORMAT, filename); XXX hay
		// que cambiarlo
		// }
		// catch (Exception e)
		// {
		//			logger.error("main(String[]) - filename=" + filename , e); //$NON-NLS-1$ //$NON-NLS-2$
		// System.exit(-1);
		// }

		// ahora la imagen a cambiado de extensión y por consiguiente
		// trabajaremos con la otra imagen
		// en vez de imprimir la imagen a archivo podríamos trabajar
		// directamente con el objeto tipo imagen
		// de momento lo dejaremos así puesto que habría que hacer
		// modificaciones en el resto de métodos
		filename = filename + "." + IMAGE_FORMAT;

		long tstart = System.currentTimeMillis();
		Gray8Image grayimage;
		try
		{
			grayimage = ImageUtil.readImage(filename);
			logger
					.debug("main(String[]) - Raster template readed:  - filename=" + filename + "- tdelta(ms)=" + (System.currentTimeMillis() - tstart)); //$NON-NLS-

			// Gray8Image grayimage =
			// ImageUtil.readImage("../../2circle-org-colored-whole.tif");
			// Gray8Image grayimage =
			// ImageUtil.readImage("2circle-org-colored-whole.tif");

			ImageManipulation image = new ImageManipulation(grayimage);
			logger.debug("main(String[]) - Searching reference circles."); //$NON-NLS-

			tstart = System.currentTimeMillis();
			image.locateConcentricCircles();
			logger.debug("main(String[]) - Reference circles located:  - tdelta(ms)=" + (System.currentTimeMillis() - tstart)); //$NON-NLS-
			logger.debug("main(String[]) - Searching Marks."); //$NON-NLS-
			tstart = System.currentTimeMillis();
			image.locateMarks();
			logger.debug("main(String[]) - Marks located:  - tdelta(ms)=" + (System.currentTimeMillis() - tstart)); //$NON-NLS-

			image.writeAscTemplate(filename + ".asc");
			image.writeConfig(filename + ".config");
		}
		catch (IOException e)
		{

			logger.error(
					"main(String[]) Can't access/read image:" + filename, e); //$NON-NLS-1$
		}
		catch (ImageFormatException e)
		{
			logger.error("main(String[]) Can't decode image:" + filename, e); //$NON-NLS-1$
		}

	}

}
