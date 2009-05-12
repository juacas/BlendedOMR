/**
 * 
 */
package org.uva.itast.blended.omr;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

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

	/**
	 * @return
	 * @throws IOException 
	 */
	private BufferedImage loadImageFile() throws IOException
	{
		BufferedImage imagen;
		
		long start=System.currentTimeMillis();
		imagen = UtilidadesFicheros.reescalar(filePath);	
		logger.debug("Image page ("+filePath.getName()+") converted in (ms)"+(System.currentTimeMillis()-start)); //$NON-NLS-1$
		

		return imagen;
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

	
	

}
