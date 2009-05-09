/**
 * 
 */
package org.uva.itast.blended.omr;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import com.sun.pdfview.PDFFile;
import com.sun.pdfview.PDFPage;

/**
 * @author juacas
 *
 */
public class PagesCollection
{
	/**
	 * Logger for this class
	 */
	private static final Log	logger	= LogFactory.getLog(PagesCollection.class);

	private PDFFile	pdffile; // for PDF
	private int	numPagesPDF;

	private BufferedImage	imagen; //last rendered image

	private File	inputPath;

	/**
	 * @param inputpath
	 * @throws IOException 
	 */
	public PagesCollection(File inputpath) throws IOException
	{
		this.inputPath=inputpath;
		if(inputpath.getName().toLowerCase().endsWith(".pdf"))
			{	//si se trata de un fichero PDF, hay que convertir a BufferedImage
    		long start=System.currentTimeMillis();
			RandomAccessFile raf = new RandomAccessFile(inputpath, "r");	//se carga la imagen pdf para leerla
			FileChannel channel = raf.getChannel();
			ByteBuffer buf = channel.map(FileChannel.MapMode.READ_ONLY, 0, channel.size());
			pdffile = new PDFFile(buf);    							//se crea un objeto de tipo PDFFile para almacenar las páginas
			numPagesPDF = pdffile.getNumPages();
			logger.debug("PDF readed in (ms)"+(System.currentTimeMillis()-start)); //$NON-NLS-1$
			}
		else
			{
			numPagesPDF=1;
			
			}
	}

	/**
	 * @return
	 */
	public int getNumPages()
	{
		return numPagesPDF;
	}

	/**
	 * @param i index of page starting with 0
	 * @return
	 */
	public BufferedImage getPageImage(int i)
	{
		try
		{
			long start=System.currentTimeMillis();
			if("PDF".equals(getPageType(i)))
			{
			PDFPage page = pdffile.getPage(i+1);        				//se coge la primera página
			imagen = UtilidadesFicheros.leerImagenPDF(page);		//reescalamos la imagen para que tenga la resolución que deseamos
			logger.debug("PDF page "+i+" ("+inputPath.getName()+") converted in (ms)"+(System.currentTimeMillis()-start)); //$NON-NLS-1$
			return imagen;
			}
			else
			{
				imagen = UtilidadesFicheros.reescalar(inputPath);	
				logger.debug("Image page "+i+" ("+inputPath.getName()+") converted in (ms)"+(System.currentTimeMillis()-start)); //$NON-NLS-1$
				
			}
		}
		catch (InterruptedException e)
		{
			
			throw new RuntimeException("Page "+i+" ("+getPageType(i)+") unavailable.",e);
		}
		catch (IOException e)
		{
			
			throw new RuntimeException("Page "+i+" ("+getPageType(i)+") unavailable.",e);
		}
		return imagen;
	}

	/**
	 * @param i
	 * @return
	 */
	private String getPageType(int i)
	{
		if (pdffile!=null)
			return "PDF";
		else
			return "Non-PDF";
	}

}
