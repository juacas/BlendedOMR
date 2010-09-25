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

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

import org.junit.Test;
import org.uva.itast.blended.omr.UtilidadesFicheros;
import org.uva.itast.blended.omr.pages.PDFPageImage;
import org.uva.itast.blended.omr.pages.SubImage;

public class TestPDFImage
{

	/**
	 * Test method for {@link org.uva.itast.blended.omr.pages.PDFPageImage#getSubimage(double, double, double, double, int)}.
	 */
	@Test
	public void testGetSubimage()
	{

		try
		{
			URL url = getClass().getClassLoader().getResource("Doc1.pdf");
			File testPath = new File(url.toURI());
			
			PDFPageImage page=new PDFPageImage(testPath,null,0);
			SubImage sub=null; 
			sub=page.getSubimage(0, 0, 210, 100, BufferedImage.TYPE_INT_RGB);
			UtilidadesFicheros.salvarImagen(sub, "target/test-classes/output/debug-0-0-210-100.png", "png");
			sub=page.getSubimage(31.17d,27.66d, 50.86d, 16.7d,BufferedImage.TYPE_INT_RGB);
			UtilidadesFicheros.salvarImagen(sub, "target/test-classes/output/debug_mark.png", "png");  
			
			
			
			sub=page.getSubimage(82.02d,27.606d, 50.86d, 16.7d, BufferedImage.TYPE_INT_RGB);
			UtilidadesFicheros.salvarImagen(sub, "target/test-classes/output/debug_mark_50_50.png", "png"); 
			
		}
		catch (URISyntaxException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
