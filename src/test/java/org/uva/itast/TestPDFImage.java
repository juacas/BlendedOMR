/**
 * 
 */
package org.uva.itast;

import static org.junit.Assert.*;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

import org.junit.Test;
import org.uva.itast.blended.omr.BarcodeManipulation;
import org.uva.itast.blended.omr.UtilidadesFicheros;
import org.uva.itast.blended.omr.pages.PDFPageImage;
import org.uva.itast.blended.omr.pages.PageImage;
import org.uva.itast.blended.omr.pages.SubImage;

/**
 * @author juacas
 *
 */
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
			
			
//			sub=page.getSubimage(0, 0, 100, 100);
//			UtilidadesFicheros.salvarImagen(sub, "target/test-classes/output/debug0-100.png", "png");
//			 sub=page.getSubimage(0, 0, 150, 150);
//			UtilidadesFicheros.salvarImagen(sub, "target/test-classes/output/debug0-150.png", "png");
//			 sub=page.getSubimage(0, 0, 200, 200);
//			UtilidadesFicheros.salvarImagen(sub, "target/test-classes/output/debug0-200.png", "png");
//			 sub=page.getSubimage(100, 100, 200, 200);
//				UtilidadesFicheros.salvarImagen(sub, "target/test-classes/output/debug100-200.png", "png");
//				 sub=page.getSubimage(200, 200, 200, 200);
//					UtilidadesFicheros.salvarImagen(sub, "target/test-classes/output/debug200-200.png", "png");
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
