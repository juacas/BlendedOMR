/*
 * PDFToTif.java
 *
 * Creado en Febrero-Marzo de 2009
 *
 */

package org.uva.itast.blended.omr;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import javax.imageio.ImageIO;

import com.sun.pdfview.*;

/**
 * 
 * @author Jesús Rodilana
 */

public class PDFToTif {
	public PDFToTif() {
	}

	public void convertirPDF(String filename) {
		try {
			System.out.println("Empezando conversión PDF a TIF");
			// cargamos la imagen pdf para leerla
			RandomAccessFile raf = new RandomAccessFile(filename, "r");
			FileChannel channel = raf.getChannel();
			ByteBuffer buf = channel.map(FileChannel.MapMode.READ_ONLY, 0,
					channel.size());
			// creamos un objeto de tipo PDFFile para almacenar las páginas
			PDFFile pdffile = new PDFFile(buf);
			// cogemos la primera página
			PDFPage page = pdffile.getPage(0);
			// se captura la primera página
			BufferedImage imagen_pdf = leerImagenPDF(page);
			// se guarda en el formato tif
			salvarImagen(imagen_pdf, filename + ".tif");
			System.out.println("Finalizando conversión PDF a TIF");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void reescalar(String filename) {
		Image imagenObjeto = null;

		try {
			System.out.println("Empezando reescalado de imagen");
			// elegimos el ancho y el alto de la nueva imagen
			int resizeWidth = 1700;
			int resizeHeight = 2339;
			// leemos la imagen como un archivo y la almacenamos en un objeto
			// imagen
			File imagenLeer = new File(filename);
			imagenObjeto = ImageIO.read(imagenLeer);
			// creeamos la imagen de salida
			BufferedImage imagenSalida = null;
			// configuramos la imagen con las medidas especificas y en escala de
			// grises
			imagenSalida = new BufferedImage(resizeWidth, resizeHeight,
					BufferedImage.TYPE_BYTE_GRAY);
			// creamos un objeto gráfico en dos dimensiones
			imagenSalida.createGraphics();
			imagenSalida.createGraphics().drawImage(imagenObjeto, 0, 0,
					resizeWidth, resizeHeight, null);
			// se guarda en el formato tif
			salvarImagen(imagenSalida, filename + ".tif");
			System.out.println("Finalizando reescalado de imagen");
			/*
			 * //Prueba para visualizar la imágen en una ventana externa JFrame
			 * frame = new JFrame(); JLabel label = new JLabel(new
			 * ImageIcon(imagenSalida)); frame.getContentPane().add(label,
			 * BorderLayout.CENTER); frame.pack(); frame.setVisible(true);
			 */
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void salvarImagen(Image imagen, String filename) {
		try {
			File imagen_tif = new File(filename);
			// se elige el formato en el cual se guardará la imagen y se escribe
			// en un fichero
			ImageIO.write((RenderedImage) imagen, "tif", imagen_tif);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static BufferedImage leerImagenPDF(PDFPage page) {
		// creamos un bufferedImge para almacenar la imagen
		BufferedImage img_pdf = null;
		try {

			// elegimos el ancho y el alto de la imagen
			int resizeWidth = 1700;
			int resizeHeight = 2339;

			// configuramos la imagen con las medidas especificas y en escala de
			// grises
			img_pdf = new BufferedImage(resizeWidth, resizeHeight,
					BufferedImage.TYPE_BYTE_GRAY);
			// creamos un objeto gráfico en dos dimensiones
			Graphics2D g2 = img_pdf.createGraphics();
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
					RenderingHints.VALUE_ANTIALIAS_ON);

			// renderizamos la imgen
			PDFRenderer renderer = new PDFRenderer(page, g2, new Rectangle(0,
					0, 1700, 2339), null, Color.RED);
			page.waitForFinish();
			renderer.run();
			// por último dibujamos la imagen
			img_pdf.createGraphics().drawImage(img_pdf, 0, 0, resizeWidth,
					resizeHeight, null);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return img_pdf;

	}
}
