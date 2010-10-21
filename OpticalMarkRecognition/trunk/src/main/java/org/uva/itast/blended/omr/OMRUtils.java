package org.uva.itast.blended.omr;
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
* @author MarÃƒÂ­a JesÃƒÂºs VerdÃƒÂº 
* @author Luisa Regueras 
* @author Elena VerdÃƒÂº
* 
* @license http://www.gnu.org/copyleft/gpl.html GNU Public License
* @package blended
 ***********************************************************************/


import java.awt.Image;
import java.awt.Point;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Map;

import javax.imageio.ImageIO;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.uva.itast.blended.omr.pages.PageImage;
import org.uva.itast.blended.omr.pages.SubImage;
import org.uva.itast.blended.omr.scanners.BarcodeScanner;
import org.uva.itast.blended.omr.scanners.MarkScannerException;
import org.uva.itast.blended.omr.scanners.ScanResult;
import org.uva.itast.blended.omr.scanners.SolidCircleMarkScanner;

import com.sun.pdfview.PDFFile;

public class OMRUtils
{
	/**
	 * Logger for this class
	 */
	private static final Log	logger					= LogFactory
																.getLog(OMRUtils.class);

	public static final String	USERID_FIELDNAME		= "USERID";
	public static final String	TEMPLATEID_FIELDNAME	= "TEMPLATEFIELD";
	public static final String	IMAGE_TYPE				= "png";

	/**
	 * MÃ¯Â¿Â½todo que lee una imagen y la transforma en un objeto de tipo
	 * BufferedImage reescalado
	 * 
	 * @param filename
	 * @param outputdir
	 * @return imagenSalida
	 * @throws IOException
	 */
	
	

	/**
	 * MÃ¯Â¿Â½todo que salva un objeto tipo imagen en un archivo fÃ¯Â¿Â½sico de extensiÃ¯Â¿Â½n
	 * png
	 * 
	 * @param imagen
	 * @param filename
	 * @param imageFormat
	 * @throws IOException
	 */
	public static void salvarImagen(Image imagen, String filename,
			String imageFormat) throws IOException
	{
		File rasterImageFile = new File(filename);
		ImageIO.write((RenderedImage) imagen, imageFormat, rasterImageFile);
	}

	/**
	 * MÃ¯Â¿Â½todo que a partir de un fichero pdf de entrada devuelve el nÃ¯Â¿Â½mero su
	 * pÃ¯Â¿Â½ginas
	 * 
	 * @param inputdir
	 * @return pdffile.getNumpages();
	 * @throws IOException
	 */
	public static int getNumpagesPDF(String inputdir) throws IOException
	{
		RandomAccessFile raf = new RandomAccessFile(inputdir, "r"); // se carga
																	// la imagen
																	// pdf para
																	// leerla
		FileChannel channel = raf.getChannel();
		ByteBuffer buf = channel.map(FileChannel.MapMode.READ_ONLY, 0, channel
				.size());
		PDFFile pdffile = new PDFFile(buf); // se crea un objeto de tipo PDFFile
											// para almacenar las pÃ¯Â¿Â½ginas
		return pdffile.getNumPages(); // se obtiene el nÃ¯Â¿Â½mero de paginas
	}



	/**
	 * MÃ¯Â¿Â½todo que procesa una imÃ¯Â¿Â½gen dada por el inputpath y que llama a los
	 * mÃ¯Â¿Â½todos que harÃ¯Â¿Â½n posible el procesado de los datos que contenga
	 * 
	 * @param inputpath
	 * @param align
	 * @param medianfilter
	 * @param outputdir
	 * @param plantilla
	 * @throws IOException
	 */
	public static void processsPageAnSaveResultsWithLogging(PageImage page, boolean align,
			boolean medianfilter, String outputdir, Map<String,PlantillaOMR> plantillas, String acticode, String userid)
			throws IOException
	{
			long taskStart = System.currentTimeMillis();
			
			processPageAndSaveResults(align, medianfilter,outputdir, plantillas, page, acticode, userid);
			logger.debug("Page  ("+page.getFileName()+") processed in (ms)"+(System.currentTimeMillis()-taskStart)); //$NON-NLS-1$
	}

	/**
	 * @param inputpath
	 * @param align
	 * @param medianfilter
	 * @param outputdir
	 * @param plantilla
	 * @param pageImage
	 * @throws FileNotFoundException
	 */
	public static void processPageAndSaveResults(boolean align,
			boolean medianfilter, String outputdir, Map<String,PlantillaOMR> plantillas,
			PageImage pageImage, String acticode, String userid) throws FileNotFoundException
	{
		PlantillaOMR plantilla=findBestSuitedTemplate(pageImage, plantillas, medianfilter);
		
		processPage(pageImage, align, medianfilter, outputdir, plantilla); // se
																			// procesa
																			// la
																			// pÃ¯Â¿Â½gina
		saveOMRResults(pageImage.getFileName(), outputdir, plantilla, acticode, userid);// se salvan
																	// los
																	// resultados
																	// en
																	// archivo
	}

	/**
	 * MÃ¯Â¿Â½todo que procesa una pÃ¯Â¿Â½gina a partir de un BufferedImage invocando a
	 * los mÃ¯Â¿Â½todos que buscarÃ¯Â¿Â½n las marcas.
	 * All templates passed as argument must share the location of the TemplateIdentification
	 * in the screen 
	 * TODO refactor this. Not to be static.
	 * @param pageImage
	 * @param align
	 * @param outputdir
	 * @param plantillas usado como in/out devuelve los valores reconocidos
	 * @throws FileNotFoundException
	 */
	public static void processPage(PageImage pageImage, boolean align,
			boolean medianfilter, String outputdir, PlantillaOMR plantilla)
			throws FileNotFoundException
	{
		
		
		if (align)
			{
			//pageImage.align(); //encapsula procesamiento y representaciÃ¯Â¿Â½n
			pageImage.align(plantilla, pageImage);
			}
		
		long taskStart = System.currentTimeMillis();
		
		searchMarks(outputdir, plantilla, pageImage, medianfilter); // se
																				// buscan
																				// las
																				// marcas
		logger.debug("\tMarks scanned in (ms)" + (System.currentTimeMillis() - taskStart)); //$NON-NLS-1$
		
	}
	public static void processPage(PageImage pageImage, boolean align,
		boolean medianfilter, String outputdir, Map<String,PlantillaOMR> plantillas)
		throws FileNotFoundException
{
		PlantillaOMR plantilla=findBestSuitedTemplate(pageImage, plantillas, medianfilter);
		processPage(pageImage, align, medianfilter, outputdir, plantilla);
}
	/**
	 * Scan the page searching the {@value #TEMPLATEID_FIELDNAME} tag and select the proper
	 * template in the map.
	 * If no valid TemplateId code is found the first item in the map is selected.
	 * @param pageImage
	 * @param plantillas
	 * @param medianfilter
	 * @return
	 */
	public static PlantillaOMR findBestSuitedTemplate(PageImage pageImage, Map<String, PlantillaOMR> plantillas, boolean medianfilter)
	{
		/**
		 * Get any (first) template with information for recognizing the TemplateID.
		 */
		
		for (PlantillaOMR aTemplate : plantillas.values())
		{
			PageTemplate firstPage=aTemplate.getPagina(1);
			Field campo=firstPage.getCampos().get(TEMPLATEID_FIELDNAME);
			if (campo!=null)
				{
				
				scanField(pageImage, campo, medianfilter);
				String templateId=campo.getValue();
				// extract the page number
				if (templateId != null)
				{
					templateId=templateId.substring(0, templateId.length() - 1);
				}
				/**
				 * get the actual template
				 */
				PlantillaOMR plantilla=plantillas.get(templateId);
				if (plantilla != null)
				{
					return plantilla;
				}
				else // return current template instead
				{
					logger.warn("findBestSuitedTemplate: Using a default template! May render unexpected results if documents have different structure!!", null); //$NON-NLS-1$
					return aTemplate;
				}
				}

			logger.warn("findBestSuitedTemplate- Template do not have a "+TEMPLATEID_FIELDNAME+" field. Try the next.", null); //$NON-NLS-1$
		}
		throw new RuntimeException("No "+TEMPLATEID_FIELDNAME+" field found! in the templates in use.");
		
	
	}
	
	/**
	 * MÃ¯Â¿Â½todo para buscar las marcas dentro de un objeto tipo Gray8Image
	 * 
	 * @param outputdir
	 * @param plantilla
	 * @param pageImage 
	 * @param imagen
	 * @param medianfilter
	 * @return plantilla
	 * @throws FileNotFoundException
	 */
	public static PlantillaOMR searchMarks(String outputdir,
			PlantillaOMR plantilla,  PageImage pageImage, 
			boolean medianfilter) throws FileNotFoundException
	{
		
		for (int i = 0; i < plantilla.getNumPaginas(); i++)
		{
			// se recorren todas las marcas de una pÃ¯Â¿Â½gina determinada
			Hashtable<String, Field> campos = plantilla.getPagina(i + 1)
					.getCampos(); // Hastable para almacenar los campos que
									// leemos del fichero de definiciÃ¯Â¿Â½n de
									// marcas
			Collection<Field> campos_val = campos.values();
					
			for (Field campo : campos_val)
			{
				// vamos a buscar en los campos leÃ¯Â¿Â½dos, en marcas[] estÃ¯Â¿Â½n
				// almacenadas las keys
				scanField(pageImage, campo, medianfilter);
			}
			
			pageImage.labelPageAsProcessed();
 			
		}
		return plantilla;
	}

	/**
	 * @param pageImage
	 * @param campo
	 * @param i
	 * @param medianfilter
	 */
	private static void scanField(PageImage pageImage, Field campo, boolean medianfilter)
	{
		int tipo = campo.getTipo(); // se almacena el tipo para separar
									// entre si es un barcode o un
									// circle
		if (tipo == Field.CIRCLE)
			buscarMarcaCircle(pageImage , campo, medianfilter);
		else if (tipo == Field.CODEBAR)
			{
			buscarMarcaCodebar(pageImage, campo, medianfilter);
			}
	}


	/**
	 * MÃ¯Â¿Â½todo que busca marcas de tipo codebar en un objeto tipo BufferedImage
	 * 
	 * @param pageImage
	 * @param campo
	 * @param medianfilter
	 */
	private static void buscarMarcaCodebar(PageImage pageImage, Field campo,boolean medianFilter)
	{

	
		BarcodeScanner barcodeScanner=new BarcodeScanner(pageImage,medianFilter);
		try
		{
			campo.setValue( barcodeScanner.getParsedCode(campo) );
			barcodeScanner.markBarcode(campo);
		}
		catch (MarkScannerException e)
		{
			campo.setValue(null);
			campo.setValid(false);
			if (logger.isDebugEnabled())
				barcodeScanner.markBarcode(campo);
		}
		
		//barcodeManipulator.markBarcode(campo);
	}

	/**
	 * MÃ¯Â¿Â½todo que busca marcas de tipo circle en un objeto tipo Gray8Image
	 * 
	 * @param i
	 *
	 * @param campo 
	 * @param markedImage 
	 * @param mark
	 * @param markedImage
	 * @param campo
	 * @param medianfilter
	 */
	private static void buscarMarcaCircle( 
			PageImage pageImage, Field campo, boolean medianfilter)
	{
		Rectangle2D bbox=campo.getBBox();//milimeters
//		Rectangle bboxPx = pageImage.toPixels(bbox);//pÃ¯Â¿Â½xeles
		// center of the mark
		Point2D center=new Point();
		center.setLocation(bbox.getCenterX(),bbox.getCenterY()); //nos da el centro geomÃ¯Â¿Â½trico del rectÃ¯Â¿Â½ngulo
		
		
		// leemos la anchura de las marcas en milÃ¯Â¿Â½metros
		double markWidth = Math.max(1, bbox.getWidth());
		double markHeight = Math.max(1,bbox.getHeight());
		SolidCircleMarkScanner markScanner = new SolidCircleMarkScanner(pageImage,markWidth,markHeight,medianfilter);

		if (logger.isDebugEnabled())
		{
			logger.debug("buscarMarcaCircle - campo=" + campo); //$NON-NLS-1$
		}
		try
		{
			ScanResult res=markScanner.scanField(campo);
			
			if ( (Boolean)res.getResult() ) // se busca la marca que se desea encontrar
			{
				if (logger.isDebugEnabled())
				{
					logger.debug("buscarMarcaCircle - >>>>>>>Found mark at " + bbox + " (mm) :" + campo); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
				}
				campo.setValue("true");
													// si se ha encontrado la marca
				markScanner.putCircleMarkOnImage(pageImage);
			} else
			{
				campo.setValue("false");
			}
			campo.setValid(true);
		}
		catch (MarkScannerException e)
		{
			campo.setValid(false);
		}
	}

	/**
	 * MÃ¯Â¿Â½todo para guardar los resultados del proceso de reconocimiento de
	 * marcas
	 * 
	 * @param outputdir
	 * @param inputpath
	 * @param plantilla
	 * @throws FileNotFoundException
	 */
	public static void saveOMRResults(String inputpath, String outputdir,
			PlantillaOMR plantilla, String acticode, String userid) throws FileNotFoundException
	{
		Hashtable<String, Field> campos = plantilla.getPagina(1).getCampos();
		Field acticodeField = campos.get(acticode);
		Field useridField = campos.get(userid);
		try
		{
			
			int useridInt = Integer.parseInt(useridField.getValue()); // evita
																		// inyecciÃ¯Â¿Â½n
																		// de path
																		// en el
																		// cÃ¯Â¿Â½digo
			int acticodeInt = Integer.parseInt(acticodeField.getValue()); // evita
																			// inyecciÃ¯Â¿Â½n
																			// de
																			// path
																			// en el
																			// cÃ¯Â¿Â½digo

			File dir = new File(outputdir); // que venga de parametro
			File outputFile = new File(dir, "omr_result_" + useridInt + "_"
					+ acticodeInt + ".txt");

			PrintWriter out = new PrintWriter(new FileOutputStream(outputFile,true));
			for (int i = 0; i < plantilla.getNumPaginas(); i++)
			{
				out.println("Filename=" + inputpath);
				PageTemplate pagina=plantilla.getPagina(i + 1);
				out.println("[Page" + pagina.getNumPagina()
						+ "]");
				for (int k = 0; k < pagina.getMarcas().size(); k++)
				{
					Field campo2 = campos.get(pagina
							.getMarcas().elementAt(k));
					out.println(campo2.getNombre() + "=" + campo2.getValue());
				}
			}
			out.close();
			
		}
		catch (NumberFormatException e)
		{
			// TODO Auto-generated catch block
			logger.error("saveOMRResults:  Can't obtain "+acticode+"="+acticodeField+" and "+userid+"="+useridField+" for outputting report."); //$NON-NLS-1$
			
		}
		return;
	}

	/**
	 * @param subImage
	 */
	public static void logSubImage(String textId, BufferedImage subImage)
	{
		if (logger.isDebugEnabled()&& true)
		{
			long start=System.currentTimeMillis();
			
			
			
			try
			{
				URL url=OMRUtils.class.getClassLoader().getResource("Doc1.pdf");
				File testPath=new File(new File(url.toURI()).getParentFile(),"output");
				File imgFile=new File(testPath,"debug_"+textId+System.currentTimeMillis()+".png");
				OMRUtils.salvarImagen(subImage, imgFile.getAbsolutePath(), "PNG");
				logger.debug("Dumped "+textId+" in (ms) :"+(System.currentTimeMillis()-start));
			}
			catch (IOException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			catch (URISyntaxException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	
	}
	public static void logSubImage(SubImage subImage)
	{
		
		logSubImage("subimage", subImage);
	}

	/**
	 * @param prefix
	 * @param medianed
	 */
	public static void logSubImage(String prefix, SubImage subImage)
	{
		Rectangle2D markArea=subImage.getBoundingBox();
		logger.debug("Dumped subimage  "+markArea);
		logSubImage(prefix,(BufferedImage)subImage);
		
	}
}
