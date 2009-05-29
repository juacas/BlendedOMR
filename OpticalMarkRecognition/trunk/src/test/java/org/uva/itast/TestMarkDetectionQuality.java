/**
 * 
 */
package org.uva.itast;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Vector;

import junit.framework.TestCase;

import org.uva.itast.blended.omr.OMRProcessor;
import org.uva.itast.blended.omr.pages.PageImage;


/**
 * @author juacas
 *
 */
public class TestMarkDetectionQuality extends TestCase
{
	private OMRProcessor	processor;
	public void testProcessLowResImage()
	{
		try
		{
		URL url=getClass().getClassLoader().getResource("OMR_imagePage850x1170.png");
		File testPath=new File(url.toURI());
		prepareConfig(testPath);

		Vector<PageImage> errores;
		// detecci�n de errores
		
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
	public void testProcessDitheredImage()
	{
		try
		{
		String fileName = "test_page_dithered.png";
		URL url=getClass().getClassLoader().getResource(fileName);
		File testPath=new File(url.toURI());
		prepareConfig(testPath);

		Vector<PageImage> errores;
		// detecci�n de errores
		
		processor.setMedianFilter(true);
		
		url=getClass().getClassLoader().getResource(fileName);
		testPath=new File(url.toURI());

		processor.setMedianFilter(true);
		errores= processor.processPath(testPath.getAbsolutePath());        		//se leen las p�ginas escaneadas
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
		processor.readCommandLine(args);        						//se lee la l�nea de comandos
        processor.loadTemplate(processor.getDefinitionfile());	//se lee el fichero con la descripci�n de las marcas
		return args;
	}
}
