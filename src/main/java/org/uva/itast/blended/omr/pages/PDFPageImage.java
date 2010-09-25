/**
 * 
 */
package org.uva.itast.blended.omr.pages;

//import omrproj.ConcentricCircle;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.uva.itast.blended.omr.OMRProcessor;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;

import com.sun.pdfview.PDFFile;
import com.sun.pdfview.PDFPage;
import com.sun.pdfview.PDFRenderer;

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
			BufferedImage imagen = leerImagenPDF(page);
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
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		return filePath.getName()+" page "+pageNumber;
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
	/**
	 * Método que lee una imagen pdf y la transforma en un objeto de tipo
	 * BufferedImage reescalado
	 * 
	 * @param page
	 * @return img_pdf
	 * @throws InterruptedException
	 * @throws Exception
	 */
	public  BufferedImage leerImagenPDF(PDFPage page)
			throws InterruptedException
	{
		BufferedImage img_pdf = null; // se crea un bufferedImge para almacenar
										// la imagen

		// se elige el ancho y el alto de la imagen
		int resizeWidth = OMRProcessor._IMAGEWIDTHPIXEL;
		int resizeHeight = OMRProcessor._IMAGEHEIGTHPIXEL;

		img_pdf = new BufferedImage(resizeWidth, resizeHeight,
				BufferedImage.TYPE_INT_RGB); // se configura la imagen con las
												// medidas especificas y en
												// escala de grises
		Graphics2D g2 = img_pdf.createGraphics(); // se crea un objeto gráfico
													// en dos dimensiones
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);

		PDFRenderer renderer = new PDFRenderer(page, g2, new Rectangle(0, 0,
				OMRProcessor._IMAGEWIDTHPIXEL,
				OMRProcessor._IMAGEHEIGTHPIXEL), null, Color.RED); // se
																		// renderiza
																		// la
																		// imgen
		page.waitForFinish();
		renderer.run();
		img_pdf.createGraphics().drawImage(img_pdf, 0, 0, resizeWidth,
				resizeHeight, null); // por último se dibuja la imagen

		return img_pdf;
	}

	/* (non-Javadoc)
	 * @see org.uva.itast.blended.omr.PageImage#getHorizontalRatio()
	 */
	@Override
	public double getHorizontalRatio()
	{
		return OMRProcessor._IMAGEWIDTHPIXEL/a4width;
	}
	/* (non-Javadoc)
	 * @see org.uva.itast.blended.omr.PageImage#getVerticalRatio()
	 */
	@Override
	public double getVerticalRatio()
	{
		return OMRProcessor._IMAGEHEIGTHPIXEL/a4height;
	}
}
