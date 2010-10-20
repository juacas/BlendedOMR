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
* @author Juan Pablo de Castro
* @author Jesus Rodilana
* @author María Jesús Verdú 
* @author Luisa Regueras 
* @author Elena Verdú
* 
* @license http://www.gnu.org/copyleft/gpl.html GNU Public License
* @package blended
 ***********************************************************************/

 
package org.uva.itast;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.awt.geom.AffineTransform;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Vector;

import org.junit.Test;
import org.uva.itast.blended.omr.OMRProcessor;
import org.uva.itast.blended.omr.pages.PageImage;
import org.uva.itast.blended.omr.pages.PagesCollection;

public class TestMarkDetectionQuality
{
	private OMRProcessor	processor;
	
	@Test
	public void testProcessMediumResImage()
	{
		try
		{
		URL url=getClass().getClassLoader().getResource("page-0007.jpg");
		File testPath=new File(url.toURI());
		prepareConfig(testPath);

		Vector<PageImage> errores;
		// detección de errores
		
		processor.setMedianFilter(true);
		
		url=getClass().getClassLoader().getResource("page-0007.jpg");
		testPath=new File(url.toURI());

		processor.setMedianFilter(false);
		
		PagesCollection pages = new PagesCollection(testPath);
		// procesar ficheros
		PageImage page=pages.getPageImage(0);
		
		AffineTransform align= page.getAllignmentInfo();
//		align.translate(5, -10);
		page.setAlignmentInfo(align);
		
		errores= processor.processPages(pages);     		
		assertTrue("Errors encountered",errores.isEmpty());
		
		
		}
		catch (Exception e)
		{
			e.printStackTrace();
			fail("Can't configure test case."+e);
		}
	}
	@Test
	public void testProcessLowResImage()
	{
		try
		{
		URL url=getClass().getClassLoader().getResource("OMR_imagePage850x1170.png");
		File testPath=new File(url.toURI());
		prepareConfig(testPath);

		Vector<PageImage> errores;
		// detección de errores
		
		processor.setMedianFilter(true);
		
		url=getClass().getClassLoader().getResource("OMR_imagePage850x1170.png");
		testPath=new File(url.toURI());

		processor.setMedianFilter(true);
		errores= processor.processPath(testPath.getAbsolutePath());        		//se leen las p�ginas escaneadas
		assertTrue("Errors encountered",errores.isEmpty());
		
		
		}
		catch (Exception e)
		{
			e.printStackTrace();
			fail("Can't configure test case."+e);
		}
	}
	@Test
	public void testProcessDitheredImage()
	{
		try
		{
		String fileName = "test_page_dithered.png";
		URL url=getClass().getClassLoader().getResource(fileName);
		File testPath=new File(url.toURI());
		prepareConfig(testPath);

		Vector<PageImage> errores;
		// detección de errores
		
		processor.setMedianFilter(true);
		
		url=getClass().getClassLoader().getResource(fileName);
		testPath=new File(url.toURI());

		processor.setMedianFilter(true);
		errores= processor.processPath(testPath.getAbsolutePath());        		//se leen las páginas escaneadas
		assertTrue("Errors encountered",errores.isEmpty());
		assertTrue("Activity code not detected readed.",processor.getFieldValue("ACTIVITYCODE")!=null);
		
		}
		catch (Exception e)
		{
			e.printStackTrace();
			fail("Can't configure test case."+e);
		}
	}
	
	private String[] prepareConfig(File testPath) throws IOException
	{
		processor = new OMRProcessor();
	      
		String argLine="-i /var/moodledata/blended/123/ -o /tmp/blended -id1 USERID -id2 ACTIVITYCODE -d /tmp/formato2313.txt";
		String[] args= argLine.split(" ");
		
		File outputDir=new File(testPath.getParentFile(),"output");
		if (!outputDir.exists())
			outputDir.mkdir();
		args[3]=outputDir.getAbsolutePath();
		args[9]=new  File (testPath.getParentFile(),"prac_test_lab.fields").getAbsolutePath();
		processor.readCommandLine(args);        						//se lee la línea de comandos
        processor.loadTemplate(processor.getDefinitionfile());	//se lee el fichero con la descripción de las marcas
		return args;
	}
}
