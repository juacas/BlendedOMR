/*
* ====================================================================
*
* License:        GNU General Public License
*
* Note: Original work copyright to respective authors
*
* This file is part of Blended (c) 2009-2010 University of Valladolid..
*
* Blended is free software; you can redistribute it and/or
* modify it under the terms of the GNU General Public License
* as published by the Free Software Foundation; either version 2
* of the License, or (at your option) any later version.
*
* Blended is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
*
*
* Module developed at the University of Valladolid http://www.eduvalab.uva.es
*
* http://www.itnt.uva.es , http://www.eduvalab.uva.es
*
* Designed and directed by Juan Pablo de Castro with 
* the effort of many other students of telecommunication 
* engineering.
* This module is provides as-is without any 
* guarantee. Use it as your own risk.
*
* @author Juan Pablo de Castro
* @author Jesus Rodilana
* @author María Jesús Verdú 
* @author Luisa Regueras 
* @author Elena Verdú
* 
* @license http://www.gnu.org/copyleft/gpl.html GNU Public License
* @package blended
 ***********************************************************************/

 

/***********************************************************************
 * Module developed at the University of Valladolid http://www.eduvalab.uva.es
 * Designed and directed by Juan Pablo de Castro with 
 * the effort of many other students of telecommunciation 
 * engineering this module is provides as-is without any 
 * guarantee. Use it as your own risk.
 *
 * @author Juan Pablo de Castro and Miguel Baraja Campesino and many others.
 * @license http://www.gnu.org/copyleft/gpl.html GNU Public License
 * @package blended
 ***********************************************************************/

package org.uva.itast.blended.omr.pages;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.sun.pdfview.PDFFile;

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
		
		PDFFile pdffile = PDFPageImage.loadPDFFile(inputpath); //se crea un objeto de tipo PDFFile que almacena las p�ginas
		int numPagesPDF = pdffile.getNumPages();
		logger.debug("PDF readed in (ms)"+(System.currentTimeMillis()-start)); //$NON-NLS-1$
		for (int i=0;i<numPagesPDF;i++)
			{
			PageImage page=new PDFPageImage(inputpath,pdffile,i);
			addPage(page);
			}
		
		}
	else
		{
		pages.add(new ImageFilePage(inputpath));
		
		}
		
	}

	/**
	 * @param page
	 */
	public void addPage(PageImage page)
	{
		pages.add(page);
	}

	/* (non-Javadoc)
	 * @see java.lang.Iterable#iterator()
	 */
	public Iterator<PageImage> iterator()
	{
		return pages.iterator();
	}

}
