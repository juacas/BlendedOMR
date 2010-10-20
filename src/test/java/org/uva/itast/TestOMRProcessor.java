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

 

package org.uva.itast;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import org.junit.Test;
import org.uva.itast.blended.omr.OMRProcessor;
import org.uva.itast.blended.omr.PlantillaOMR;
import org.uva.itast.blended.omr.pages.PageImage;

public class TestOMRProcessor
{

	private OMRProcessor	processor;

	/**
	 * Test method for {@link org.uva.itast.blended.omr.OMRProcessor#readCommandLine(java.lang.String[])}.
	 * @throws URISyntaxException 
	 */
	@Test
	public void testLeerLineaComandos() throws URISyntaxException
	{
		processor = new OMRProcessor();
		String [] args = new String[12];
		int ai=0;
		args[ai++]="commandName";
		args[ai++]="-i"; 
		URL url_1 = getClass().getClassLoader().getResource("p2.pdf");
		File testPath_1 = new File(url_1.toURI());
		args[ai++] = testPath_1.toString();
		args[ai++]="-a";
		args[ai++]="-o";
		URL url_4 = getClass().getClassLoader().getResource("");
		File testPath_4 = new File(url_4.toURI());
		args[ai++] = testPath_4.toString();
		args[ai++]="-id1";
		args[ai++]="USERID";
		args[ai++]="-id2";
		args[ai++]="ACTIVITYCODE";
		args[ai++]="-d";
		URL url = getClass().getClassLoader().getResource("prac_test_lab.fields");
		File testPath = new File(url.toURI());
		args[ai++] = testPath.toString();
		
		processor.readCommandLine(args);
		assertTrue(processor.getInputPath().equals(testPath_1.getPath()));
		//TODO Place asserts and failure cases
		
		try
		{
			args[3]="+a";
			processor.readCommandLine(args);
			fail("Bad command line not detected.");
		}
		catch (IllegalArgumentException e)
		{
			// Error detected
		}
		
		
	}

	/**
	 * Test method for {@link org.uva.itast.blended.omr.OMRProcessor#loadTemplate(java.lang.String)}.
	 * @throws URISyntaxException 
	 * @throws IOException 
	 */
	@Test
	public void testLeerDefinitionfile() throws URISyntaxException, IOException
	{
		OMRProcessor processor = new OMRProcessor();
		String [] args = new String[11];
		args[0]="-i"; 
		URL url_1 = getClass().getClassLoader().getResource("p1.pdf");
		File testPath_1 = new File(url_1.toURI());
		args[1] = testPath_1.toString();
		args[2]="-a";
		args[3]="-o";
		URL url_4 = getClass().getClassLoader().getResource("");
		File testPath_4 = new File(url_4.toURI());
		args[4] = testPath_4.toString();
		args[5]="-id1";
		args[6]="USERID";
		args[7]="-id2";
		args[8]="ACTIVITYCODE";
		args[9]="-d";
		URL url = getClass().getClassLoader().getResource("prac_test_lab.fields");
		File testPath = new File(url.toURI());
		args[10] = testPath.toString();
		
		processor.readCommandLine(args);
		processor.loadTemplate(processor.getDefinitionfile());
		
		Map<String,PlantillaOMR> templates= processor.getTemplates();
		
		assertTrue(templates.size()==1); // 
		assertTrue(templates.containsKey("000071949057"));
		//TODO make asserts and failure cases
		
	}

	/**
	 * Test method for {@link org.uva.itast.blended.omr.OMRProcessor#escribirValoresCampo(java.lang.String)}.
	 */
	public void testEscribirValoresCampo()
	{
		//fail("Not yet implemented");
	}

	/**
	 * Test method for {@link org.uva.itast.blended.omr.OMRProcessor#processPath(java.lang.String)}.
	 */
	@Test
	public void testProcessPath()
	{
		try
		{
		URL url=getClass().getClassLoader().getResource("Doc1.pdf");
		File testPath=new File(url.toURI());
		prepareConfig(testPath);

		Vector<PageImage> errores;
		// detecci�n de errores
		errores=processor.processPath("nonexistentfile.png");
		assertTrue("Errors not detected ",errores.size()==1);
		
		processor.setMedianFilter(true);
		errores= processor.processPath(testPath.getAbsolutePath());        		//se leen las p�ginas escaneadas
		assertTrue("Errors encountered."+errores,errores.isEmpty());
	
		
		url=getClass().getClassLoader().getResource("OMR_imagePage850x1170.png");
		testPath=new File(url.toURI());

		processor.setMedianFilter(true);
		errores= processor.processPath(testPath.getAbsolutePath());        		//se leen las p�ginas escaneadas
		assertTrue("Errors encountered",errores.isEmpty());
		
		url=getClass().getClassLoader().getResource("Doc2.pdf");
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
	public void testProcessMultiPagePDF()
	{
	try
		{
//		  if (false) 
//	    	   return;
       
		URL url=getClass().getClassLoader().getResource("56605.pdf");
		File testPath=new File(url.toURI());
		
		// use a single, common, template definition
		prepareConfig(testPath);
		
		Vector<PageImage> errores;
		// detecci�n de errores
		processor.setMedianFilter(true);
		errores= processor.processPath(testPath.getAbsolutePath());        		//se leen las p�ginas escaneadas
		assertTrue("Errors encountered",errores.isEmpty());
		
		}
		catch (Exception e)
		{
			fail("Can't configure test case."+e);
		}
	}
	@Test
	public void testProcessMultiPagePDFmultiDefinitions()
	{
	try
		{
//		  if (false) 
//	    	   return;
       
		URL url=getClass().getClassLoader().getResource("56605.pdf");
		File testPath=new File(url.toURI());
		
		// use a single, common, template definition
		prepareConfig(testPath);
		
		Vector<PageImage> errores;
		// detecci�n de errores
		// use a collection of templates from a directory
		File dir=testPath.getParentFile();
		processor.getTemplates().clear();
		processor.loadTemplateCollection(dir.getAbsolutePath());
		
		// detecci�n de errores
		processor.setMedianFilter(true);
		errores= processor.processPath(testPath.getAbsolutePath());        		//se leen las p�ginas escaneadas
		assertTrue("Errors encountered",errores.isEmpty());
		
		}
		catch (Exception e)
		{
			fail("Can't configure test case."+e);
		}
	}
	/**
	 * @return
	 * @throws IOException
	 */
	private String[] prepareConfig(File testPath) throws IOException
	{
		processor = new OMRProcessor();
	      
		String argLine="-i /var/moodledata/blended/123/ -o /tmp/blended -id1 USERID -id2 ACTIVITYCODE -d /tmp/formato2313.fields";
		String[] args= argLine.split(" ");
		
		File outputDir=new File(testPath.getParentFile(),"output");
		if (!outputDir.exists())
			outputDir.mkdir();
		args[3]=outputDir.getAbsolutePath();
		args[9]=new  File (testPath.getParentFile(),"prac_test_lab.fields").getAbsolutePath();
		processor.readCommandLine(args);        						//se lee la l�nea de comandos
        processor.loadTemplate(processor.getDefinitionfile());	//se lee el fichero con la descripci�n de las marcas
		return args;
	}
	
	@Test
	public void testFilesFromZip() throws URISyntaxException, ZipException, IOException
	{
		URL url = getClass().getClassLoader().getResource("form_test.zip");
		File testPath = new File(url.toURI());
		ZipFile zip=new ZipFile(testPath);
		List<InputStream> files=OMRProcessor.inputStreamsFromZip(zip,".*");
		assertTrue("ZIP empty", files.size()>0);
		 files=OMRProcessor.inputStreamsFromZip(zip,".*\\.jpg");
		assertTrue("ZIP empty", files.size()==0);
		 files=OMRProcessor.inputStreamsFromZip(zip,".*\\.fields");
		assertTrue("ZIP empty", files.size()>0);
		int abyte=files.get(0).read();
		}

}
