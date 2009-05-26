/**
 * 
 */
package org.uva.itast.blended.omr.pages;

//import omrproj.ConcentricCircle;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.uva.itast.blended.omr.OMRProcessor;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

/**
 * @author juacas
 *
 */
public class ImageFilePage extends PageImage
{
	/**
	 * Logger for this class
	 */
	private static final Log	logger	= LogFactory.getLog(ImageFilePage.class);

	private File	filePath;

	/**
	 * @param imagen
	 * @param align
	 */
	public ImageFilePage(File path)
	{
	this.filePath=path;
	}

	/* (non-Javadoc)
	 * @see org.uva.itast.blended.omr.PageImage#getImagen()
	 */
	@Override
	public BufferedImage createImage()
	{
		BufferedImage image;
		try
		{
			image = loadImageFile();
		}
		catch (IOException e)
		{
			throw new RuntimeException("Can't load image "+filePath,e);
		}
		setImagen(image);
		return super.getImagen();
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		return filePath.getName();
	}

	/**
	 * @return
	 * @throws IOException 
	 */
	private BufferedImage loadImageFile() throws IOException
	{
		BufferedImage imagen;
		
		long start=System.currentTimeMillis();
		imagen = ImageIO.read(filePath);
		if(false)
			imagen = reescalar();	
		logger.debug("Image page ("+filePath.getName()+") converted in (ms)"+(System.currentTimeMillis()-start)); //$NON-NLS-1$
		

		return imagen;
	}
	public  BufferedImage reescalar() throws IOException
	{
		Image imagenObjeto = null;
		BufferedImage imagenSalida = null; // se crea la imagen de salida

		// se elige el ancho y el alto de la nueva imagen
		int resizeWidth = OMRProcessor._IMAGEWIDTHPIXEL;
		int resizeHeight = OMRProcessor._IMAGEHEIGTHPIXEL;

		
		

		imagenSalida = new BufferedImage(resizeWidth, resizeHeight,
				BufferedImage.TYPE_INT_RGB); // se configura la imagen con las
												// medidas especificas 
		//Image scaled=imagenObjeto.getScaledInstance(resizeWidth, resizeHeight, Image.SCALE_FAST);
		
		imagenSalida.createGraphics().drawImage(imagenObjeto, 0, 0,
				resizeWidth, resizeHeight, null); // se crea un objeto gráfico
													// en dos dimensiones

		return imagenSalida;
	}

	/* (non-Javadoc)
	 * @see org.uva.itast.blended.omr.PageImage#getType()
	 */
	@Override
	public String getType()
	{
		return "1-PAGE IMAGE FILE";
	}

	/* (non-Javadoc)
	 * @see org.uva.itast.blended.omr.PageImage#getFileName()
	 */
	@Override
	public String getFileName()
	{
		
		return filePath.getAbsolutePath();
	}

	/* (non-Javadoc)
	 * @see org.uva.itast.blended.omr.PageImage#getHorizontalRatio()
	 */
	@Override
	public double getHorizontalRatio()
	{
		return getImagen().getWidth()/a4width;
	}

	/* (non-Javadoc)
	 * @see org.uva.itast.blended.omr.PageImage#getVerticalRatio()
	 */
	@Override
	public double getVerticalRatio()
	{
		return getImagen().getHeight()/a4height;
	}

	

}
