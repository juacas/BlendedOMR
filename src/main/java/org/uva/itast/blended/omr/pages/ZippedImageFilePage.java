package org.uva.itast.blended.omr.pages;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.imageio.ImageIO;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ZippedImageFilePage extends ImageFilePage
{
	/* (non-Javadoc)
	 * @see org.uva.itast.blended.omr.pages.ImageFilePage#getFileName()
	 */
	@Override
	public String getFileName()
	{
		return zipFile.getName()+"->"+entry.getName();
	}

	/**
	 * Logger for this class
	 */
	private static final Log	logger	=LogFactory.getLog(ZippedImageFilePage.class);

	private ZipFile	zipFile;// reference to avoid unexpected closing of stream
	private ZipEntry	entry;

	public ZippedImageFilePage(ZipFile zip, ZipEntry entry)
	{
		super();
		this.zipFile=zip;
		this.entry=entry;
	}

	/* (non-Javadoc)
	 * @see org.uva.itast.blended.omr.pages.ImageFilePage#loadImageFile()
	 */
	@Override
	protected BufferedImage loadImageFile() throws IOException
	{
BufferedImage imagen;
		
		long start=System.currentTimeMillis();
		
		imagen = ImageIO.read(zipFile.getInputStream(entry));
		if (imagen ==null)
			throw new IOException("File "+zipFile+"->"+entry.getName()+" do not contain a valid image");
//		if(false)
//			imagen = reescalar();	
		logger.debug("Image page ("+zipFile.getName()+"->"+entry.getName()+") converted in (ms)"+(System.currentTimeMillis()-start)); //$NON-NLS-1$
		

		return imagen;
	}

	

}
