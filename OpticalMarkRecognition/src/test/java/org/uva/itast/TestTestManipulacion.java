/**
 * 
 */
package org.uva.itast;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Vector;

import org.uva.itast.blended.omr.PageImage;
import org.uva.itast.blended.omr.TestManipulation;


import junit.framework.TestCase;


/**
 * @author juacas
 *
 */
public class TestTestManipulacion extends TestCase
{

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
	 * Test method for {@link org.uva.itast.blended.omr.TestManipulation#leerLineaComandos(java.lang.String[])}.
	 */
	public void testLeerLineaComandos()
	{
		//fail("Not yet implemented");
	}

	/**
	 * Test method for {@link org.uva.itast.blended.omr.TestManipulation#leerDefinitionfile(java.lang.String)}.
	 */
	public void testLeerDefinitionfile()
	{
		//fail("Not yet implemented");
	}

	/**
	 * Test method for {@link org.uva.itast.blended.omr.TestManipulation#escribirValoresCampo(java.lang.String)}.
	 */
	public void testEscribirValoresCampo()
	{
		//fail("Not yet implemented");
	}

	/**
	 * Test method for {@link org.uva.itast.blended.omr.TestManipulation#leerPaginas(java.lang.String)}.
	 */
	public void testLeerPagina()
	{
		TestManipulation procesartest = new TestManipulation();    			//se crea un objeto tipo TestManipulation para todo lo que tenga que ver con sus métodos
	      
		String argLine="-i /var/moodledata/blended/123/ -o /tmp/blended -id1 USERID -id2 ACTIVITYCODE -d /tmp/formato2313.txt";
		String[] args= argLine.split(" ");
		try
		{
		URL url=getClass().getClassLoader().getResource("Doc1.pdf");
		

		File testPath=new File(url.toURI());
		
		args[1]=testPath.getAbsolutePath();
		
		File outputDir=new File(testPath.getParentFile(),"output");
		if (!outputDir.exists())
			outputDir.mkdir();
		
		args[3]=outputDir.getAbsolutePath();
		
		args[9]=new  File (testPath.getParentFile(),"prac_test_lab.fields").getAbsolutePath();
		
		procesartest.leerLineaComandos(args);        						//se lee la línea de comandos
        procesartest.leerDefinitionfile(procesartest.getDefinitionfile());	//se lee el fichero con la descripción de las marcas
       
		}
		catch (Exception e)
		{
			fail("Can't configure test case."+e);
		}
		Vector<PageImage> errores;
		// detección de errores
		errores=procesartest.leerPaginas("nonexistentfile.png");
		assertTrue("Errors not detected ",errores.size()==1);
		
		errores= procesartest.leerPaginas(procesartest.getInputPath());        		//se leen las páginas escaneadas
		assertTrue("Errors encountered",errores.isEmpty());
	
		try
		{
			URL url=getClass().getClassLoader().getResource("OMR_imagePage850x1170.png");
			File testPath=new File(url.toURI());
			
			args[1]=testPath.getAbsolutePath();
		}
		catch (URISyntaxException e)
		{
			fail("Can't configure test case."+e.getMessage());
		}
		
		       						//se lee la línea de comandos
		errores= procesartest.leerPaginas(args[1]);        		//se leen las páginas escaneadas
		assertTrue("Errors encountered",errores.isEmpty());
		
	}

}
