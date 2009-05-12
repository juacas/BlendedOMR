/**
 * 
 */
package org.uva.itast.blended.omr;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.awt.image.BufferedImage;
import java.io.File;

import com.sun.pdfview.PDFFile;
import com.sun.pdfview.PDFPage;

/**
 * @author juacas
 *
 */
public class PDFPageImage extends PageImage
{
	/**
	 * Logger for this class
	 */
	private static final Log	logger	= LogFactory.getLog(PDFPageImage.class);

	private int	pageNumber;
	private PDFFile	pdfFile;

	private File	filePath;

	/**
	 * @param pdffile
	 * @param page
	 */
	public PDFPageImage(File path,PDFFile pdffile, int page)
	{
		this.filePath=path;
		this.pdfFile=pdffile;
		this.pageNumber=page;
	}

	/* (non-Javadoc)
	 * @see org.uva.itast.blended.omr.PageImage#getImagen()
	 */
	@Override
	public BufferedImage createImage()
	{
		long start=System.currentTimeMillis();
		PDFPage page = pdfFile.getPage(pageNumber);        				//se coge la  página
		try
		{
			BufferedImage imagen = UtilidadesFicheros.leerImagenPDF(page);
			setImagen(imagen);
		}
		catch (InterruptedException e)
		{
			throw new RuntimeException("UNEXPECTED: PDF conversion interrumped.",e);
		}		
		
		logger.debug("PDF page "+pageNumber+" converted in (ms)"+(System.currentTimeMillis()-start)); //$NON-NLS-1$
		
		return super.getImagen();
	}

	/* (non-Javadoc)
	 * @see org.uva.itast.blended.omr.PageImage#getType()
	 */
	@Override
	public String getType()
	{
		
		return "PDF";
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
