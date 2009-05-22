/*
 * UtilidadesFicheros.java
 *
 * Creado en Febrero-Mayo de 2009
 *
 */

package org.uva.itast.blended.omr;

import java.awt.Image;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Collection;
import java.util.Hashtable;

import javax.imageio.ImageIO;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.uva.itast.blended.omr.pages.PageImage;

import com.google.zxing.ReaderException;
import com.sun.pdfview.PDFFile;

/**
 * @author Jesús Rodilana
 */
public class UtilidadesFicheros
{
	/**
	 * Logger for this class
	 */
	private static final Log	logger					= LogFactory
																.getLog(UtilidadesFicheros.class);

	public static final String	USERID_FIELDNAME		= "USERID";
	public static final String	ACTIVITYCODE_FIELDNAME	= "ACTIVITYCODE";
	public static final String	IMAGE_TYPE				= "png";

	/**
	 * Método que lee una imagen y la transforma en un objeto de tipo
	 * BufferedImage reescalado
	 * 
	 * @param filename
	 * @param outputdir
	 * @return imagenSalida
	 * @throws IOException
	 */
	
	

	/**
	 * Método que salva un objeto tipo imagen en un archivo físico de extensión
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
	 * Método que a partir de un fichero pdf de entrada devuelve el número su
	 * páginas
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
											// para almacenar las páginas
		return pdffile.getNumPages(); // se obtiene el número de paginas
	}



	/**
	 * Método que procesa una imágen dada por el inputpath y que llama a los
	 * métodos que harán posible el procesado de los datos que contenga
	 * 
	 * @param inputpath
	 * @param align
	 * @param medianfilter
	 * @param outputdir
	 * @param plantilla
	 * @throws IOException
	 */
	public static void procesarImagenes(PageImage page, boolean align,
			boolean medianfilter, String outputdir, PlantillaOMR plantilla, String acticode, String userid)
			throws IOException
	{
		

			long taskStart = System.currentTimeMillis();
			
			
			processPageAndSaveResults(align, medianfilter,outputdir, plantilla, page, acticode, userid);
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
			boolean medianfilter, String outputdir, PlantillaOMR plantilla,
			PageImage pageImage, String acticode, String userid) throws FileNotFoundException
	{
		
		procesarPagina(pageImage, align, medianfilter, outputdir, plantilla); // se
																			// procesa
																			// la
																			// página
		
		saveOMRResults(pageImage.getFileName(), outputdir, plantilla, acticode, userid);// se salvan
																	// los
																	// resultados
																	// en
																	// archivo
	}

	/**
	 * Método que procesa una página a partir de un BufferedImage invocando a
	 * los métodos que buscarán las marcas
	 * 
	 * @param pageImage
	 * @param align
	 * @param outputdir
	 * @param plantilla in/out devuelve los valores reconocidos
	 * @throws FileNotFoundException
	 */
	public static void procesarPagina(PageImage pageImage, boolean align,
			boolean medianfilter, String outputdir, PlantillaOMR plantilla)
			throws FileNotFoundException
	{
		
		if (align)
			{
			
			pageImage.align(); //encapsula procesamiento y representación
			
			}
		if (medianfilter)
		{
			
			pageImage.medianFilter(); //encapsula procesamiento y representación
		
		}
		
		long taskStart = System.currentTimeMillis();
		
		buscarMarcas(outputdir, plantilla, pageImage, medianfilter); // se
																				// buscan
																				// las
																				// marcas
		logger.debug("\tMarks scanned in (ms)" + (System.currentTimeMillis() - taskStart)); //$NON-NLS-1$
		
	}

	/**
	 * @param grayimage
	 */
	/* TODO: Implementar alineamiento con BufferedImage
	 * 
	private static void alignImage(Gray8Image grayimage)
	{
		ImageManipulation imageManipulator = new ImageManipulation(grayimage); // se
																				// crea
																				// un
																				// objeto
																				// image
																				// que
																				// nos
																				// permita
																				// trabajar
																				// con
																				// la
																				// imagen
		imageManipulator.locateConcentricCircles(); // se alinea si esta marcada
													// la bandera de alineación
	}
*/
	/**
	 * Método para buscar las marcas dentro de un objeto tipo Gray8Image
	 * 
	 * @param outputdir
	 * @param plantilla
	 * @param pageImage 
	 * @param imagen
	 * @param medianfilter
	 * @return plantilla
	 * @throws FileNotFoundException
	 */
	public static PlantillaOMR buscarMarcas(String outputdir,
			PlantillaOMR plantilla,  PageImage pageImage, 
			boolean medianfilter) throws FileNotFoundException
	{
		

		for (int i = 0; i < plantilla.getNumPaginas(); i++)
		{
			// se recorren todas las marcas de una página determinada
			Hashtable<String, Campo> campos = plantilla.getPagina(i + 1)
					.getCampos(); // Hastable para almacenar los campos que
									// leemos del fichero de definición de
									// marcas
			Collection<Campo> campos_val = campos.values();
			for (Campo campo : campos_val)
			{
				// vamos a buscar en los campos leídos, en marcas[] están
				// almacenadas las keys
				int tipo = campo.getTipo(); // se almacena el tipo para separar
											// entre si es un barcode o un
											// circle
				if (tipo == Campo.CIRCLE)
					buscarMarcaCircle(i, pageImage , campo, medianfilter);
				else if (tipo == Campo.CODEBAR)
					{
					buscarMarcaCodebar(pageImage, campo, medianfilter);
					
					}
			}
			
			pageImage.labelPageAsProcessed();
 			
		}
		return plantilla;
	}

	// XXX pasar medianfilter

	/**
	 * Método que busca marcas de tipo codebar en un objeto tipo BufferedImage
	 * 
	 * @param pageImage
	 * @param campo
	 * @param medianfilter
	 */
	private static void buscarMarcaCodebar(PageImage pageImage, Campo campo,boolean medianFilter)
	{

	
		BarcodeManipulation barcodeManipulator=new BarcodeManipulation(pageImage,medianFilter);
		try
		{
			campo.setValue( barcodeManipulator.getParsedCode(campo) );
		}
		catch (ReaderException e)
		{
			campo.setValue(null);
			campo.setValid(false);
		}
		
		barcodeManipulator.markBarcode(campo);
	}

	/**
	 * Método que busca marcas de tipo circle en un objeto tipo Gray8Image
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
	private static void buscarMarcaCircle(int i, 
			PageImage pageImage, Campo campo, boolean medianfilter)
	{
		double x;
		double y;
		int xpixel;
		int ypixel;
		double markradXmm; // se trabajara con la posición de las marcas en
							// milímetros
		double markradYmm;
		double[] coords = campo.getCoordenadas();
		// "x" será la primera coordenada, "y" la segunda
		//centra las coordenadas
		x = coords[0]+coords[2]/2;
		y = coords[1]+coords[3]/2;
		// Pasamos el tamaño a píxeles _IMAGEWIDTHPIXELx_IMAGEHEIGTHPIXEL
		xpixel = pageImage.toPixelsX(x);//(int) (x * pageImage.getHorizontalRatio());
		ypixel = pageImage.toPixelsY(y);

		// leemos la anchura de las marcas y las pasamos a píxeles
		markradXmm = coords[2];
		markradYmm = coords[3];
		int markWidth = Math.max(5, pageImage.toPixelsX(markradXmm));
		int markHeight = Math.max(5,pageImage.toPixelsY(markradYmm));
		SolidCircleMark mark = new SolidCircleMark(pageImage,markWidth,	markHeight);

		if (logger.isDebugEnabled())
		{
			logger.debug("buscarMarcaCircle - campo=" + campo); //$NON-NLS-1$
		}

		if (mark.isMark(xpixel, ypixel,false)) // se busca la marca que se desea
											// encontrar
		{
			if (logger.isDebugEnabled())
			{
				logger.debug("buscarMarcaCircle - >>>>>>>Found mark at " + x + "," + y + ":" + campo); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
			}
			campo.setValue("true");
												// si se ha encontrado la marca
			mark.putCircleMarkOnImage(pageImage);
		} else
		{
			campo.setValue("false");
		}
	}

	/**
	 * Método para guardar los resultados del proceso de reconocimiento de
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

		try
		{
			Hashtable<String, Campo> campos = plantilla.getPagina(1).getCampos();

			//TODO: usar los nombres pasados en -id1 -id2
			Campo acticodeField = campos.get(acticode);
			Campo useridField = campos.get(userid);
			
			int useridInt = Integer.parseInt(useridField.getValue()); // evita
																		// inyección
																		// de path
																		// en el
																		// código
			int acticodeInt = Integer.parseInt(acticodeField.getValue()); // evita
																			// inyección
																			// de
																			// path
																			// en el
																			// código

			File dir = new File(outputdir); // que venga de parametro
			File outputFile = new File(dir, "omr_result_" + useridInt + "_"
					+ acticodeInt + ".txt");

			PrintWriter out = new PrintWriter(new FileOutputStream(outputFile,true));
			for (int i = 0; i < plantilla.getNumPaginas(); i++)
			{
				out.println("Filename=" + inputpath);
				out.println("[Page" + plantilla.getPagina(i + 1).getNumPagina()
						+ "]");
				for (int k = 0; k < plantilla.getPagina(i + 1).getMarcas().size(); k++)
				{
					Campo campo2 = campos.get(plantilla.getPagina(i + 1)
							.getMarcas().elementAt(k));
					out.println(campo2.getNombre() + "=" + campo2.getValue());
				}
			}
			out.close();
			
		}
		catch (NumberFormatException e)
		{
			// TODO Auto-generated catch block
			logger.error("saveOMRResults:  Can't obtain ID1 and ID2 for outputting report."); //$NON-NLS-1$
		}
		return;
	}
}
