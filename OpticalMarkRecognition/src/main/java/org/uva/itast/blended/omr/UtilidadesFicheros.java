/*
 * UtilidadesFicheros.java
 *
 * Creado en Febrero-Mayo de 2009
 *
 */

package org.uva.itast.blended.omr;

import omrproj.ConcentricCircle;
import omrproj.ImageManipulation;
import omrproj.ImageUtil;
import omrproj.SolidMark;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
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

import net.sourceforge.jiu.color.reduction.RGBToGrayConversion;
import net.sourceforge.jiu.data.Gray8Image;
import net.sourceforge.jiu.data.PixelImage;
import net.sourceforge.jiu.data.RGB24Image;
import net.sourceforge.jiu.gui.awt.BufferedRGB24Image;

//import org.apache.commons.logging.Log;
//import org.apache.commons.logging.LogFactory;

import com.google.zxing.ReaderException;
import com.sun.pdfview.PDFFile;
import com.sun.pdfview.PDFPage;
import com.sun.pdfview.PDFRenderer;

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
	public static BufferedImage reescalar(File imagenLeer) throws IOException
	{
		Image imagenObjeto = null;
		BufferedImage imagenSalida = null; // se crea la imagen de salida

		// se elige el ancho y el alto de la nueva imagen
		int resizeWidth = TestManipulation._IMAGEWIDTHPIXEL;
		int resizeHeight = TestManipulation._IMAGEHEIGTHPIXEL;

		
		imagenObjeto = ImageIO.read(imagenLeer);

		imagenSalida = new BufferedImage(resizeWidth, resizeHeight,
				BufferedImage.TYPE_BYTE_GRAY); // se configura la imagen con las
												// medidas especificas y en
												// escala de grises
		//Image scaled=imagenObjeto.getScaledInstance(resizeWidth, resizeHeight, Image.SCALE_FAST);
		
		imagenSalida.createGraphics().drawImage(imagenObjeto, 0, 0,
				resizeWidth, resizeHeight, null); // se crea un objeto gráfico
													// en dos dimensiones

		return imagenSalida;
	}

	/**
	 * Método que lee una imagen pdf y la transforma en un objeto de tipo
	 * BufferedImage reescalado
	 * 
	 * @param page
	 * @return img_pdf
	 * @throws InterruptedException
	 * @throws Exception
	 */
	public static BufferedImage leerImagenPDF(PDFPage page)
			throws InterruptedException
	{
		BufferedImage img_pdf = null; // se crea un bufferedImge para almacenar
										// la imagen

		// se elige el ancho y el alto de la imagen
		int resizeWidth = TestManipulation._IMAGEWIDTHPIXEL;
		int resizeHeight = TestManipulation._IMAGEHEIGTHPIXEL;

		img_pdf = new BufferedImage(resizeWidth, resizeHeight,
				BufferedImage.TYPE_BYTE_GRAY); // se configura la imagen con las
												// medidas especificas y en
												// escala de grises
		Graphics2D g2 = img_pdf.createGraphics(); // se crea un objeto gráfico
													// en dos dimensiones
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);

		PDFRenderer renderer = new PDFRenderer(page, g2, new Rectangle(0, 0,
				TestManipulation._IMAGEWIDTHPIXEL,
				TestManipulation._IMAGEHEIGTHPIXEL), null, Color.RED); // se
																		// renderiza
																		// la
																		// imgen
		page.waitForFinish();
		renderer.run();
		img_pdf.createGraphics().drawImage(img_pdf, 0, 0, resizeWidth,
				resizeHeight, null); // por último se dibuja la imagen

		return img_pdf;
	}

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
	 * Método que a partir de un objeto tipo BufferedImage lo transforma en uno
	 * de tipo Gray8Image
	 * 
	 * @param imagen
	 * @return grayimage
	 */
	public static Gray8Image convertirAGrayImage(BufferedImage imagen)
	{
		Gray8Image grayimage = null;
		RGB24Image redimage = null;
		try
		{
			PixelImage image = new BufferedRGB24Image(imagen);
			if (image.getImageType().toString().indexOf("RGB") != -1)
			{
				redimage = (RGB24Image) image;
				RGBToGrayConversion rgbtogray = new RGBToGrayConversion();
				rgbtogray.setInputImage(redimage);
				rgbtogray.process();
				grayimage = (Gray8Image) (rgbtogray.getOutputImage());
			} else if (image.getImageType().toString().indexOf("Gray") != -1)
			{
				grayimage = (Gray8Image) (image);
			} else
			{
				grayimage = null;
			}
		}
		catch (Exception e)
		{
			throw new RuntimeException("Can't convert image.");
		}
		return grayimage;
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
	public static void procesarImagenes(File inputpath, boolean align,
			boolean medianfilter, String outputdir, PlantillaOMR plantilla)
			throws IOException
	{
		BufferedImage imagen;
		PagesCollection col = new PagesCollection(inputpath);
		int numpages = col.getNumPages();

		for (int i = 0; i < numpages; i++)
		{
			imagen = col.getPageImage(i);
			
			processPageAndSaveResults(inputpath, align, medianfilter,
					outputdir, plantilla, imagen);
		}

	}

	/**
	 * @param inputpath
	 * @param align
	 * @param medianfilter
	 * @param outputdir
	 * @param plantilla
	 * @param imagen
	 * @throws FileNotFoundException
	 */
	public static void processPageAndSaveResults(File inputpath, boolean align,
			boolean medianfilter, String outputdir, PlantillaOMR plantilla,
			BufferedImage imagen) throws FileNotFoundException
	{
		procesarPagina(imagen, align, medianfilter, outputdir, plantilla); // se
																			// procesa
																			// la
																			// página
		saveOMRResults(inputpath.getName(), outputdir, plantilla); // se salvan
																	// los
																	// resultados
																	// en
																	// archivo
	}

	/**
	 * Método que procesa una página a partir de un BufferedImage invocando a
	 * los métodos que buscarán las marcas
	 * 
	 * @param imagen
	 * @param align
	 * @param outputdir
	 * @param plantilla devuelve los valores reconocidos
	 * @throws FileNotFoundException
	 */
	public static void procesarPagina(BufferedImage imagen, boolean align,
			boolean medianfilter, String outputdir, PlantillaOMR plantilla)
			throws FileNotFoundException
	{
		long taskStart = System.currentTimeMillis();
		long funcStart = taskStart;
		PageImage pageImage=new PageImage(imagen,align); //encapsula procesamiento y representación
		
		
		
		taskStart = System.currentTimeMillis();
		buscarMarcas(outputdir, plantilla, pageImage, medianfilter); // se
																				// buscan
																				// las
																				// marcas
		logger
				.debug("\tMarks scanned in (ms)" + (System.currentTimeMillis() - taskStart)); //$NON-NLS-1$
		logger
				.debug("Page processed in (ms)" + (System.currentTimeMillis() - funcStart)); //$NON-NLS-1$

	}

	/**
	 * @param grayimage
	 */
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
		// Campo campo = new Campo();
		Gray8Image grayimage=pageImage.getGrayImage();
		
		 
	Gray8Image markedImage = (Gray8Image) (grayimage.createCopy());

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
					buscarMarcaCircle(i, pageImage ,markedImage, campo, medianfilter);
				else if (tipo == Campo.CODEBAR)
					buscarMarcaCodebar(pageImage, campo, medianfilter);
			}
			if (logger.isDebugEnabled())
			{
				File debugImagePath;
				try
				{
					debugImagePath = File.createTempFile("OMR_marksfound", ".png", new File(outputdir));
					ImageUtil.saveImage(markedImage,debugImagePath.getAbsolutePath());
					
					debugImagePath = File.createTempFile("OMR_original_marked", ".jpg", new File(outputdir));
					ImageIO.write(pageImage.getImagen(), "JPG", debugImagePath);
				}
				catch (IOException e)
				{
					logger.error("buscarMarcas(String, PlantillaOMR, Gray8Image, BufferedImage, boolean)", e); //$NON-NLS-1$
				}
				
			}
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
	private static void buscarMarcaCodebar(PageImage pageImage, Campo campo,
			boolean medianfilter)
	{

		String acticode;

		String userid;
		try
		{
			campo.setValue(BarcodeManipulation.leerBarcode(campo, pageImage.getImagen(),
					medianfilter));
		}
		catch (ReaderException e)
		{
			campo.setValue(null);
			campo.setValid(false);
		}
		if (campo.getNombre() == ACTIVITYCODE_FIELDNAME)
			acticode = campo.getValue();
		if (campo.getNombre() == USERID_FIELDNAME)
			userid = campo.getValue();
	}

	/**
	 * Método que busca marcas de tipo circle en un objeto tipo Gray8Image
	 * 
	 * @param i
	 * @param medianfilter 
	 * @param campo 
	 * @param markedImage 
	 * @param mark
	 * @param markedImage
	 * @param campo
	 * @param medianfilter
	 */
	private static void buscarMarcaCircle(int i, 
			PageImage pageImage, Gray8Image markedImage, Campo campo, boolean medianfilter)
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
		xpixel = (int) (x * TestManipulation._IMAGEWIDTHPIXEL / ConcentricCircle.a4width);
		ypixel = (int) (y * TestManipulation._IMAGEHEIGTHPIXEL / ConcentricCircle.a4height);

		// leemos la anchura de las marcas y las pasamos a píxeles
		markradXmm = coords[2];
		markradYmm = coords[3];
		int markWidth = Math.max(5, (int) (markradXmm * TestManipulation._IMAGEWIDTHPIXEL / ConcentricCircle.a4width));
		int markHeight = Math.max(5,(int) (markradYmm * TestManipulation._IMAGEHEIGTHPIXEL / ConcentricCircle.a4height));
		SolidCircleMark mark = new SolidCircleMark(pageImage,
				markWidth,
				markHeight, TestManipulation._IMAGEWIDTHPIXEL / ConcentricCircle.a4width, TestManipulation._IMAGEHEIGTHPIXEL / ConcentricCircle.a4height);

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
			mark.putMarkOnImage(markedImage); // se hace una cruz en markedImage
												// si se ha encontrado la marca
			mark.putMarkOnImage(pageImage);
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
			PlantillaOMR plantilla) throws FileNotFoundException
	{

		Hashtable<String, Campo> campos = plantilla.getPagina(1).getCampos();

		Campo acticodeField = campos.get(ACTIVITYCODE_FIELDNAME);
		Campo useridField = campos.get(USERID_FIELDNAME);

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

		PrintWriter out = new PrintWriter(new FileOutputStream(outputFile));
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
		return;
	}
}
