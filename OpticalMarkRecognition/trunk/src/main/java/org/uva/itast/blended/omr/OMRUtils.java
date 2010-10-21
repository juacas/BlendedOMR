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
			boolean medianfilter, String outputdir, Map<String,OMRTemplate> plantillas, String acticode, String userid)
			throws IOException
	{
			long taskStart = System.currentTimeMillis();
			
			processPageAndSaveResults(align, medianfilter,outputdir, plantillas, page, acticode, userid);
			logger.debug("Page  ("+page.getName()+") processed in (ms)"+(System.currentTimeMillis()-taskStart)); //$NON-NLS-1$
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
			boolean medianfilter, String outputdir, Map<String,OMRTemplate> plantillas,
			PageImage pageImage, String acticode, String userid) throws FileNotFoundException
	{
		OMRTemplate plantilla=findBestSuitedTemplate(pageImage, plantillas, medianfilter);
		
		processPage(pageImage, align, medianfilter, outputdir, plantilla); // se
																			// procesa
																			// la
																			// pÃ¯Â¿Â½gina
		saveOMRResults(pageImage.getName(), outputdir, plantilla, acticode, userid);// se salvan
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
			boolean medianfilter, String outputdir, OMRTemplate plantilla)
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
		boolean medianfilter, String outputdir, Map<String,OMRTemplate> plantillas)
		throws FileNotFoundException
{
		OMRTemplate plantilla=findBestSuitedTemplate(pageImage, plantillas, medianfilter);
		processPage(pageImage, align, medianfilter, outputdir, plantilla);
}
	/**
	 * Scan the page searching the {@value #TEMPLATEID_FIELDNAME} tag and select the proper
	 * template in the map.
	 * If no valid TemplateId code is found the first item in the map is selected.
	 * @param pageImage
	 * @param templates
	 * @param medianfilter
	 * @return
	 */
	public static OMRTemplate findBestSuitedTemplate(PageImage pageImage, Map<String, OMRTemplate> templates, boolean medianfilter)
	{
		/**
		 * Get any (first) template with information for recognizing the TemplateID.
		 */
		
		for (OMRTemplate aTemplate : templates.values())
		{
			PageTemplate firstPage=aTemplate.getPage(1);
			Field field=firstPage.getFields().get(TEMPLATEID_FIELDNAME);
			if (field!=null)
				{
				
				scanField(pageImage, field, medianfilter);
				String templateId=field.getValue();
				// extract the page number
				if (templateId != null)
				{
					templateId=templateId.substring(0, templateId.length() - 1);
				}
				/**
				 * get the actual template
				 */
				OMRTemplate plantilla=templates.get(templateId);
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

			logger.debug("findBestSuitedTemplate- Template do not have a "+TEMPLATEID_FIELDNAME+" field. Try the next.", null); //$NON-NLS-1$
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
	public static OMRTemplate searchMarks(String outputdir,
			OMRTemplate plantilla,  PageImage pageImage, 
			boolean medianfilter) throws FileNotFoundException
	{
		
		for (int i = 0; i < plantilla.getNumPaginas(); i++)
		{
			// se recorren todas las marcas de una pÃ¯Â¿Â½gina determinada
			Hashtable<String, Field> campos = plantilla.getPage(i + 1)
					.getFields(); // Hastable para almacenar los campos que
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
			searchBarcodeMark(pageImage, campo, medianfilter);
			}
	}


	/**
	 * MÃ¯Â¿Â½todo que busca marcas de tipo codebar en un objeto tipo BufferedImage
	 * 
	 * @param pageImage
	 * @param field
	 * @param medianfilter
	 */
	private static void searchBarcodeMark(PageImage pageImage, Field field,boolean medianFilter)
	{

	
		BarcodeScanner barcodeScanner=new BarcodeScanner(pageImage,medianFilter);
		try
		{
			field.setValue( barcodeScanner.getParsedCode(field) );
			barcodeScanner.markBarcode(field);
		}
		catch (MarkScannerException e)
		{
			field.setValue(null);
			field.setValid(false);
			if (logger.isDebugEnabled())
				barcodeScanner.markBarcode(field);
		}
		
		//barcodeManipulator.markBarcode(campo);
	}

	/**
	 * MÃ¯Â¿Â½todo que busca marcas de tipo circle en un objeto tipo Gray8Image
	 * 
	 * @param i
	 *
	 * @param field 
	 * @param markedImage 
	 * @param mark
	 * @param markedImage
	 * @param field
	 * @param medianfilter
	 */
	private static void buscarMarcaCircle(PageImage pageImage, Field field, boolean medianfilter)
	{
		Rectangle2D bbox=field.getBBox();//milimeters
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
			logger.debug("buscarMarcaCircle - campo=" + field); //$NON-NLS-1$
		}
		try
		{
			ScanResult res=markScanner.scanField(field);
			
			if ( (Boolean)res.getResult() ) // se busca la marca que se desea encontrar
			{
				if (logger.isDebugEnabled())
				{
					logger.debug("buscarMarcaCircle - >>>>>>>Found mark at " + bbox + " (mm) :" + field); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
				}
				field.setValue("true");
													// si se ha encontrado la marca
				markScanner.putCircleMarkOnImage(pageImage);
			} else
			{
				field.setValue("false");
			}
			field.setValid(true);
		}
		catch (MarkScannerException e)
		{
			field.setValid(false);
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
			OMRTemplate plantilla, String templateIdName, String userIdName) throws FileNotFoundException
	{
		Hashtable<String, Field> fields = plantilla.getPage(1).getFields();
		Field templateIdField = fields.get(templateIdName);
		Field useridField = fields.get(userIdName);
		try
		{
			if (useridField==null || templateIdField==null) // simulate a NumberFormat.
				throw new NumberFormatException("There is no "+TEMPLATEID_FIELDNAME+" field defined in the template!!");
			
			/**
			 * Force to cast to integer to avoid the injection of paths in the Ids.
			 */
			int useridInt = Integer.parseInt(useridField.getValue()); 
			int templateIdInt = Integer.parseInt(templateIdField.getValue()); 																	

			File dir = new File(outputdir); // que venga de parametro
			File outputFile = new File(dir, "omr_result_" + useridInt + "_"
					+ templateIdInt + ".txt");

			PrintWriter out = new PrintWriter(new FileOutputStream(outputFile,true));
			for (int i = 0; i < plantilla.getNumPaginas(); i++)
			{
				out.println("Filename=" + inputpath);
				PageTemplate page=plantilla.getPage(i + 1);
				out.println("[Page" + page.getPageNumber()+ "]");
				for (int k = 0; k < page.getMarks().size(); k++)
				{
					Field field = fields.get(page.getMarks().elementAt(k));
					out.println(field.getName() + "=" + field.getValue());
				}
			}
			out.close();
			
		}
		catch (NumberFormatException e)
		{
			// TODO Auto-generated catch block
			logger.error("saveOMRResults: Report can't be written. Both ids are not available: "+templateIdName+"="+templateIdField+" and "+userIdName+"="+useridField+"."); //$NON-NLS-1$
			
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
