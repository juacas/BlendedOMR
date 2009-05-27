/**
 * 
 */
package org.uva.itast;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Vector;

import org.uva.itast.blended.omr.OMRProcessor;
import org.uva.itast.blended.omr.pages.PageImage;


import junit.framework.TestCase;


/**
 * @author juacas
 *
 */
public class TestOMRProcessor extends TestCase
{

	private OMRProcessor	processor;

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	@Override
	protected void setUp() throws Exception
	{
		// TODO Auto-generated method stub
		super.setUp();
		  
	}

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#tearDown()
	 */
	@Override
	protected void tearDown() throws Exception
	{
		// TODO Auto-generated method stub
		super.tearDown();
	}

	/**
	 * Test method for {@link org.uva.itast.blended.omr.OMRProcessor#readCommandLine(java.lang.String[])}.
	 */
	public void testLeerLineaComandos()
	{
		//fail("Not yet implemented");
	}

	/**
	 * Test method for {@link org.uva.itast.blended.omr.OMRProcessor#loadTemplate(java.lang.String)}.
	 */
	public void testLeerDefinitionfile()
	{
		//fail("Not yet implemented");
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
	public void testProcessPath()
	{
		try
		{
		URL url=getClass().getClassLoader().getResource("Doc1.pdf");
		File testPath=new File(url.toURI());
		prepareConfig(testPath);

		Vector<PageImage> errores;
		// detección de errores
		errores=processor.processPath("nonexistentfile.png");
		assertTrue("Errors not detected ",errores.size()==1);
		
		processor.setMedianFilter(true);
		errores= processor.processPath(testPath.getAbsolutePath());        		//se leen las páginas escaneadas
		assertTrue("Errors encountered."+errores,errores.isEmpty());
	
		
		url=getClass().getClassLoader().getResource("OMR_imagePage850x1170.png");
		testPath=new File(url.toURI());

		processor.setMedianFilter(true);
		errores= processor.processPath(testPath.getAbsolutePath());        		//se leen las páginas escaneadas
		assertTrue("Errors encountered",errores.isEmpty());
		
		url=getClass().getClassLoader().getResource("Doc2.pdf");
		testPath=new File(url.toURI());

		processor.setMedianFilter(true);
		errores= processor.processPath(testPath.getAbsolutePath());        		//se leen las páginas escaneadas
		assertTrue("Errors encountered",errores.isEmpty());
		
		}
		catch (Exception e)
		{
			e.printStackTrace();
			fail("Can't configure test case."+e);
		}
	}
	public void testProcessMultiPagePDF()
	{
	try
		{
		  if (false) 
	    	   return;
       
		URL url=getClass().getClassLoader().getResource("56605.pdf");
		File testPath=new File(url.toURI());
		prepareConfig(testPath);
		Vector<PageImage> errores;
		// detección de errores
	processor.setMedianFilter(true);
		errores= processor.processPath(testPath.getAbsolutePath());        		//se leen las páginas escaneadas
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
