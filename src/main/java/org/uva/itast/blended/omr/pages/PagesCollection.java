/**
 * 
 */
package org.uva.itast.blended.omr.pages;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.sun.pdfview.PDFFile;

/**
 * @author juacas
 *
 */
public class PagesCollection implements Iterable<PageImage>
{
	/**
	 * Logger for this class
	 */
	private static final Log	logger	= LogFactory.getLog(PagesCollection.class);

	
	
	ArrayList<PageImage> pages=new ArrayList<PageImage>();

	/**
	 * @param inputpath
	 * @throws IOException 
	 */
	public PagesCollection(File inputpath) throws IOException
	{
		
		addFile(inputpath);
	}

	/**
	 * 
	 */
	public PagesCollection()
	{
		// TODO Auto-generated constructor stub
	}

	/**
	 * @return
	 */
	public int getNumPages()
	{
		return pages.size();
	}

	/**
	 * @param i index of page starting with 0
	 * @return
	 */
	public PageImage getPageImage(int i)
	{
		
			
		PageImage pageImage=pages.get(i);
		return pageImage;
	}

	/**
	 * @param i
	 * @return
	 */
	private String getPageType(int i)
	{
		return pages.get(i).getType();
	}

	/**
	 * @param file
	 * @throws IOException 
	 */
	public void addFile(File inputpath) throws IOException
	{
	if(inputpath.getName().toLowerCase().endsWith(".pdf"))
		{	//si se trata de un fichero PDF, hay que convertir a BufferedImage
		long start=System.currentTimeMillis();
		
		PDFFile pdffile = PDFPageImage.loadPDFFile(inputpath); //se crea un objeto de tipo PDFFile que almacena las páginas
		int numPagesPDF = pdffile.getNumPages();
		logger.debug("PDF readed in (ms)"+(System.currentTimeMillis()-start)); //$NON-NLS-1$
		for (int i=0;i<numPagesPDF;i++)
			{
			PageImage page=new PDFPageImage(inputpath,pdffile,i);
			pages.add(page);
			}
		
		}
	else
		{
		pages.add(new ImageFilePage(inputpath));
		
		}
		
	}

	/* (non-Javadoc)
	 * @see java.lang.Iterable#iterator()
	 */
	public Iterator<PageImage> iterator()
	{
		return pages.iterator();
	}

}
