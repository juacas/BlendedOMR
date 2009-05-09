/*
 * TestManipulation.java
 *
 * Creado en Abril-Mayo de 2009
 *
 */

package org.uva.itast.blended.omr;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Hashtable;

import javax.imageio.ImageIO;

import omrproj.ConcentricCircle;

import net.sourceforge.jiu.data.Gray8Image;
import net.sourceforge.jiu.filters.MedianFilter;
import net.sourceforge.jiu.gui.awt.BufferedRGB24Image;
import net.sourceforge.jiu.ops.MissingParameterException;
import net.sourceforge.jiu.ops.WrongParameterException;

import com.google.zxing.DecodeHintType;
import com.google.zxing.MonochromeBitmapSource;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.ReaderException;
import com.google.zxing.Result;
import com.google.zxing.client.j2se.BufferedImageMonochromeBitmapSource;
import com.google.zxing.client.result.ParsedResult;
import com.google.zxing.client.result.ResultParser;

/**
 * 
 * @author Jesús Rodilana
 *
 */
public final class BarcodeManipulation
{
	public BarcodeManipulation() {
	}
	
	/**
	 * Método que lee el valor de un código de barras contenido en un objeto tipo BufferedImage a partir de los patrones dados en un objeto tipo Campo
	 * @param campo
	 * @param imagen
	 * @param medianfilter 
	 * @return
	 * @throws ReaderException
	 */
	public static String leerBarcode(Campo campo, BufferedImage imagen, boolean medianfilter) throws ReaderException  {
		  Hashtable<DecodeHintType, Object> hints = null;
		  String barcode;
		  BufferedImage subimage;
		  Gray8Image medianimage;
		  
		  //se leen y almacenan las coordenadas
		  double[] coords = campo.getCoordenadas();
		  int x = (int) (coords[0]* TestManipulation._IMAGEWIDTHPIXEL/ConcentricCircle.a4width);		//concentriccirclewidth//posición de la x
		  int y = (int) (coords[1]* TestManipulation._IMAGEHEIGTHPIXEL/ConcentricCircle.a4height);		//posición de la y
		  int width = (int) (coords[2]* TestManipulation._IMAGEWIDTHPIXEL/ConcentricCircle.a4width);	//anchura en píxeles
		  int height = (int) (coords[3]* TestManipulation._IMAGEHEIGTHPIXEL/ConcentricCircle.a4height);	//altura en píxeles
		  
		  subimage = imagen.getSubimage(x,y,width,height);		//se coge la subimagen, x,y,w,h (en píxeles)
		  
		  if (subimage == null) System.err.println(imagen.toString() + ": No es posible cargar la imagen");
		  
		  if(medianfilter == true) medianFilter(subimage);		//parametro para desactivar el filtrado 
		  
	      MonochromeBitmapSource source = new BufferedImageMonochromeBitmapSource(subimage);
	      Result result = new MultiFormatReader().decode(source, hints);
	      ParsedResult parsedResult = ResultParser.parseResult(result);
	      
	      //System.out.println(imagen.toString() + " (format: " + result.getBarcodeFormat() +", type: " + parsedResult.getType() + "):\nRaw result:\n" + result.getText() +"\nParsed result:\n" + parsedResult.getDisplayResult());
	      barcode = parsedResult.getDisplayResult();
	      
	      return barcode;
	      
	    //} catch (ReaderException e) {
	    //	System.out.println(imagen.toString() + ": Código de barras no encontrado");
	    //	}
	      
	  }

	/**
	 * Aplica un filtro para reconstruir imagenes de mala calidad, a través del valor de los píxeles vecinos
	 * @param subimage
	 */
private static void medianFilter(BufferedImage subimage) {
	try {
		MedianFilter filter = new MedianFilter();
	    filter.setArea((int)((1700/ 1700 * 15) / 2) * 2 + 1, (int)(2339 / 2339 * 15 / 2) * 2 + 1);
	    filter.setInputImage(new BufferedRGB24Image(subimage));
		filter.process();
		filter.getOutputImage();
		//Gray8Image medianimage = (Gray8Image)(filter.getOutputImage());
		
		//subimage is median-filtered
		
	} catch (MissingParameterException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	} catch (WrongParameterException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
}
}
