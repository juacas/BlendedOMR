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
* @license http://www.gnu.org/copyleft/gpl.html GNU Public License
* @package blended
 ***********************************************************************/

 

/***********************************************************************
 * Module developed at the University of Valladolid http://www.eduvalab.uva.es
 * Designed and directed by Juan Pablo de Castro with 
 * the effort of many other students of telecommunciation 
 * engineering this module is provides as-is without any 
 * guarantee. Use it as your own risk.
 *
 * @author Juan Pablo de Castro and Miguel Baraja Campesino and many others.
 * @license http://www.gnu.org/copyleft/gpl.html GNU Public License
 * @package blended
 ***********************************************************************/

package org.uva.itast.blended.omr;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import javax.imageio.ImageIO;

import com.sun.pdfview.PDFFile;
import com.sun.pdfview.PDFPage;
import com.sun.pdfview.PDFRenderer;


public class PDFToTif {
	public PDFToTif() {
	}

	public void convertirPDF(String filename) {
		try {
			System.out.println("Empezando conversi�n PDF a TIF");
			// se carga la imagen pdf para leerla
			RandomAccessFile raf = new RandomAccessFile(filename, "r");
			FileChannel channel = raf.getChannel();
			ByteBuffer buf = channel.map(FileChannel.MapMode.READ_ONLY, 0,
					channel.size());
			// se crea un objeto de tipo PDFFile para almacenar las p�ginas
			PDFFile pdffile = new PDFFile(buf);
			// se cogen la primera p�gina
			PDFPage page = pdffile.getPage(0);
			// se captura la primera p�gina
			BufferedImage imagen_pdf = leerImagenPDF(page);
			// se guarda en el formato tif
			salvarImagen(imagen_pdf, filename + ".tif");
			System.out.println("Finalizando conversi�n PDF a TIF");
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
			// creamos un objeto gr�fico en dos dimensiones
			imagenSalida.createGraphics();
			imagenSalida.createGraphics().drawImage(imagenObjeto, 0, 0,
					resizeWidth, resizeHeight, null);
			// se guarda en el formato tif
			salvarImagen(imagenSalida, filename + ".tif");
			System.out.println("Finalizando reescalado de imagen");
			/*
			 * //Prueba para visualizar la im�gen en una ventana externa JFrame
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
			// se elige el formato en el cual se guardar� la imagen y se escribe
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
			// creamos un objeto gr�fico en dos dimensiones
			Graphics2D g2 = img_pdf.createGraphics();
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
					RenderingHints.VALUE_ANTIALIAS_ON);

			// renderizamos la imgen
			PDFRenderer renderer = new PDFRenderer(page, g2, new Rectangle(0,
					0, 1700, 2339), null, Color.RED);
			page.waitForFinish();
			renderer.run();
			// por �ltimo dibujamos la imagen
			img_pdf.createGraphics().drawImage(img_pdf, 0, 0, resizeWidth,
					resizeHeight, null);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return img_pdf;

	}
}
