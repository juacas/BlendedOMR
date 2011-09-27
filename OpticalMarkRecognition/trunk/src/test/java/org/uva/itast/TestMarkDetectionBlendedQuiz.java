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

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Vector;

import org.junit.Test;
import org.uva.itast.blended.omr.OMRProcessor;
import org.uva.itast.blended.omr.pages.PageImage;
import org.uva.itast.blended.omr.pages.PagesCollection;

public class TestMarkDetectionBlendedQuiz
{
	private OMRProcessor	processor;
	
	@Test
	public void testProcessMediumResImage()
	{
		try
		{
		URL url=getClass().getClassLoader().getResource("CuestionarioBlended.pdf");
		testFile(url);
		
		}
		catch (Exception e)
		{
			e.printStackTrace();
			fail("Can't configure test case."+e);
		}
	}
	@Test
	public void testBlendedMarked()
	{
		try
		{
		URL url=getClass().getClassLoader().getResource("CuestionarioBlendedMarked.png");
		testFile(url);
		
		}
		catch (Exception e)
		{
			e.printStackTrace();
			fail("Can't configure test case."+e);
		}
	}
	@Test
	public void testBlendedMarkedRotated()
	{
		try
		{
		URL url=getClass().getClassLoader().getResource("CuestionarioBlendedMarkedRotated.png");
		testFile(url);
		
		}
		catch (Exception e)
		{
			e.printStackTrace();
			fail("Can't configure test case."+e);
		}
	}

	@Test
	public void testBlendedMarkedScanned()
	{
		try
		{
		URL url=getClass().getClassLoader().getResource("Escaneo1_Cuestionario_Previo_a_Practica_1_de_Telematica-2011-06-28-12-49-44-6-PDF-Ab_Pagina1.jpg");
		testFile(url);
		
		}
		catch (Exception e)
		{
			e.printStackTrace();
			fail("Can't configure test case."+e);
		}
	}
	@Test
	public void testBlendedMarkedBorderlessScanned()
	{
		try
		{
		URL url=getClass().getClassLoader().getResource("scan3621.jpg");
		testFile(url);
		
		}
		catch (Exception e)
		{
			e.printStackTrace();
			fail("Can't configure test case."+e);
		}
	}
	@Test
	public void testBlendedMarkedGeographyScanned()
	{
		try
		{
		URL url=getClass().getClassLoader().getResource("EscaneoBlended_2011-09-20_Page_1.jpg");
		testFile(url);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			fail("Can't configure test case."+e);
		}
	}
	
	/**
	 * @param url
	 * @throws URISyntaxException
	 * @throws IOException
	 */
	private void testFile(URL url) throws URISyntaxException, IOException
	{
		File testPath=new File(url.toURI());
		prepareConfig(testPath);

		Vector<PageImage> errores;
		// detección de errores
		
		processor.setMedianFilter(true);
		
	
		testPath=new File(url.toURI());

		processor.setMedianFilter(false);
		
		PagesCollection pages = new PagesCollection(testPath);
		// procesar ficheros
		PageImage page=pages.getPageImage(0);
		
//AffineTransform align= page.getAllignmentInfo();
//		align.translate(5, -10);
//	page.setAlignmentInfo(align);
		
		errores= processor.processPages(pages);     		
		assertTrue("Errors encountered",errores.isEmpty());
	}

	
	private String[] prepareConfig(File testPath) throws IOException
	{
		processor = new OMRProcessor();
	      
		String argLine="-i /var/moodledata/blended/123/ -o /tmp/blended -id1 USERID -id2 ACTIVITYCODE -d /tmp/formato2313.txt -a";
		String[] args= argLine.split(" ");
		
		File outputDir=new File(testPath.getParentFile(),"output");
		if (!outputDir.exists())
			outputDir.mkdir();
		args[3]=outputDir.getAbsolutePath();
		args[9]=new  File (testPath.getParentFile(),"fieldset_OMR[1].fields").getAbsolutePath();
		processor.readCommandLine(args);        						//se lee la línea de comandos
        processor.loadTemplateCollection(testPath.getParentFile().getAbsolutePath());	//se lee los ficheros con las descripciones de las marcas
		return args;
	}
}
