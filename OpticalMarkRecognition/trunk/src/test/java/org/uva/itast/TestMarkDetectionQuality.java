/**
 * 
 */
package org.uva.itast;

import java.awt.geom.AffineTransform;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Vector;

import junit.framework.TestCase;

import org.uva.itast.blended.omr.OMRProcessor;
import org.uva.itast.blended.omr.pages.PageImage;
import org.uva.itast.blended.omr.pages.PagesCollection;


/**
 * @author juacas
 *
 */
public class TestMarkDetectionQuality extends TestCase
{
	private OMRProcessor	processor;
	
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
		align.translate(5, -10);
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
		errores= processor.processPath(testPath.getAbsolutePath());        		//se leen las páginas escaneadas
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
