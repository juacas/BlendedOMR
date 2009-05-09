/*
 * ProcessImage.java
 *
 * Created on June 29, 2007, 10:11 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package omrproj;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.uva.itast.blended.omr.ImageManipulation;

import java.io.IOException;
import java.util.StringTokenizer;

import sun.awt.image.ImageFormatException;

import net.sourceforge.jiu.data.Gray8Image;

//import net.sourceforge.jiu.codecs.*;
//import net.sourceforge.jiu.data.*;
//import net.sourceforge.jiu.color.reduction.*;
//import net.sourceforge.jiu.filters.*;

/**
 *
 * @author Aaditeshwar Seth
 */
public class ProcessForm {
	/**
	 * Logger for this class
	 */
	private static final Log	logger	= LogFactory.getLog(ProcessForm.class);
    
    public static void main(String[] args) {
        String imgfilename = args[0];
        String templatefilename = args[1];
        
        //reescalamos la imagen para que tenga la resoluci�n que deseamos
        //PDFToRaster imagenreescalada = new PDFToRaster();
        //XXX hace falta poner un directorio de salida
        //imagenreescalada.reescalar(imgfilename, ProcessTemplate.IMAGE_FORMAT, DIRECTORIO DE SALIDA);
        
        //al reescalar la imagen la hemos guardado con otro nombre
        //en vez de imprimir la imagen a archivo podr�amos trabajar directamente con el objeto tipo imagen
        //de momento lo dejaremos as� puesto que habr�a que hacer modificaciones en el resto de m�todos
        imgfilename=imgfilename + "."+ProcessTemplate.IMAGE_FORMAT;
		if (logger.isDebugEnabled())
		{
			logger.debug("main(String[]) - " + imgfilename); //$NON-NLS-1$
		}
        
        Gray8Image grayimage;
		try
		{
			grayimage = ImageUtil.readImage(imgfilename);
//	        Gray8Image grayimage = ImageUtil.readImage("../../2circle-4.tif");

	        ImageManipulation image = new ImageManipulation(grayimage);
	        image.locateConcentricCircles();

	        image.readConfig(templatefilename + ".config");
	        image.readFields(templatefilename + ".fields");
	        image.readAscTemplate(templatefilename + ".asc");
	        image.searchMarks();
	        image.saveData(imgfilename + ".dat");
//	        image.readConfig("2circle-org-colored-whole.config");
//	        image.readFields("2circle-org-colored-whole.fields");
//	        image.readAscTemplate("2circle-org-colored-whole2.tif.asc");
//	        image.searchMarks();
//	        image.saveData("2circle-4.dat");
		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			logger.error("main(String[])", e); //$NON-NLS-1$
		}
		catch (ImageFormatException e)
		{
			// TODO Auto-generated catch block
			logger.error("main(String[])", e); //$NON-NLS-1$
		}

    }

}
