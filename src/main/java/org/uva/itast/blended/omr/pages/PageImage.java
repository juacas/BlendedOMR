/**
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
* @author MarÃ­a JesÃºs VerdÃº 
* @author Luisa Regueras 
* @author Elena VerdÃº
* 
* @license http://www.gnu.org/copyleft/gpl.html GNU Public License
* @package blended
 ***********************************************************************/

 

package org.uva.itast.blended.omr.pages;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Hashtable;
import java.util.Vector;

import javax.imageio.ImageIO;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.uva.itast.blended.omr.BufferedImageUtil;
import org.uva.itast.blended.omr.Field;
import org.uva.itast.blended.omr.OMRTemplate;

public abstract class PageImage
{
	/**
	 * Logger for this class
	 */
	private static final Log	logger	= LogFactory.getLog(PageImage.class);
	private static final int	REPORTING_WIDTH	= 1024;
	public static double		a4width		= 210;										// mm
	public static double		a4height	= 290;										// mm
	
	/**
	 * @return the imagen
	 */
	public BufferedImage getImagen()
	{
		if (image==null)
		{
			setImagen(createImage());
		}
		return image;
	}

	/**
	 * @return
	 */
	abstract BufferedImage createImage();

	private BufferedImage	image;
	private BufferedImage	reportImage;
	private AffineTransform	alignmentTransform;
	private double alignmentSlope;
	
	
	/**
	 * @param imagen the imagen to set
	 */
	protected void setImagen(BufferedImage imagen)
	{
		this.image = imagen;
		// configure alignment information
		resetAlignmentInfo();
	}

	/**
	 * @param image
	 */
	public void resetAlignmentInfo()
	{
		AffineTransform tr=new AffineTransform();
		
		double horizRatio = getPreferredHorizontalResolution();
		double vertRatio = getPreferredVerticalResolution();
		
		// Do not assume square pixels
		tr.setToScale(horizRatio,vertRatio);
		setAlignmentInfo(tr);
	}
	
	
	public double getAlignmentSlope() {
		return alignmentSlope;
	}

	public void setAlignmentSlope(double slope) {
		this.alignmentSlope = slope;
	}
	
	
	/**
	 * MÃ©todo que devolverï¿½ la posiciï¿½n de los cuatro puntos de alineaciï¿½n
	 * 
	 * @param plantilla
	 * @param pageImage 
	 * @return marcasalign (array de 4 Point2D con la posiciï¿½n de las cuatro marcas de alineaciï¿½n)
	 * @throws IOException 
	 */
		public Point2D[] align(OMRTemplate plantilla, PageImage pageImage)
		{
			long taskStart = System.currentTimeMillis();
			AffineTransform transform=new AffineTransform();
			
			Point2D[] marcasalign = new Point2D[4];
			
			double dist_original_x;
			double dist_imagen_x;
			double dist_original_y;
			double dist_imagen_y;
			double escala_x = 1;
			double escala_y = 1;
			//angulo de rotacion
			double angulo_rotacion = 0;
			//centro de rotacion, por defecto el centro de la pagina
			double center_x = pageImage.a4width*pageImage.getPreferredHorizontalResolution()/2;
			double center_y = pageImage.a4height*pageImage.getPreferredVerticalResolution()/2;
			
			Point2D etopleft = new Point();			//expected top left point
			Point2D etopright = new Point();		//expected top right point
			Point2D ebottomleft = new Point();		//expected bottom left point
			Point2D ebottomright = new Point();		//expected bottom right point
			
			
			Point punto_translado = new Point();
			
			
			for (int i = 0; i < plantilla.getNumPaginas(); i++)
			{
				// Hastable para almacenar los campos que leemos del fichero de definiciï¿½n de marcas
				Hashtable<String, Field> campos = plantilla.getPage(i + 1).getFields();
				Collection<Field> campos_val = campos.values();
				for (Field campo : campos_val)
				{
					int tipo = campo.getTipo();
					
					if (tipo == Field.FRAME)	//FRAME = 2
						{
						//Se guarda la esquina superior izquierda
						Rectangle2D coords = campo.getBBox();
						
						etopleft.setLocation(coords.getX(), coords.getY());
						etopright.setLocation(coords.getX()+coords.getWidth(), coords.getY());
						ebottomleft.setLocation(coords.getX(), coords.getY()+coords.getHeight());
						ebottomright.setLocation(coords.getX()+coords.getWidth(), coords.getY()+coords.getHeight());
						
						//Se buscan las marcas de alineaciï¿½n
						marcasalign = searchAlignMarks(campo, pageImage);
						
						//Se calcula la escala (distancia entre dos puntos es igual a la raiz cuadrada de: (x2-x1)^2+(y2-y1)^2
						dist_original_x = 
							Math.sqrt(((etopright.getX()-etopleft.getX())*(etopright.getX()-etopleft.getX()))
									 +(((etopright.getY()-etopleft.getY())*(etopright.getY()-etopleft.getY()))));
						dist_imagen_x = 
							Math.sqrt(((marcasalign[1].getX()-marcasalign[0].getX())*(marcasalign[1].getX()-marcasalign[0].getX()))
									 +((marcasalign[1].getY()-marcasalign[0].getY())*(marcasalign[1].getY()-marcasalign[0].getY())));
						dist_original_y = 
							Math.sqrt(((etopleft.getX()-ebottomleft.getX())*(etopleft.getX()-ebottomleft.getX()))
									 +((etopleft.getY()-ebottomleft.getY())*(etopleft.getY()-ebottomleft.getY())));
						dist_imagen_y = 
							Math.sqrt(((marcasalign[2].getX()-marcasalign[0].getX())*(marcasalign[2].getX()-marcasalign[0].getX()))
									 +((marcasalign[2].getY()-marcasalign[0].getY())*(marcasalign[2].getY()-marcasalign[0].getY())));
						
						escala_x = dist_original_x/dist_imagen_x;
						escala_y = dist_original_y/dist_imagen_y;
						
						//margen aceptable en el escalado
						if(Math.abs(1-escala_x)<0.01)
							escala_x=1;
						if(Math.abs(1-escala_y)<0.01)
							escala_y=1;
						
						
						//System.out.println("PENDIENTE: "+getPendiente_alineacion());
						
						//obtencion del centro de rotacion, se supone el centro del marco de alineacion
						center_x = (marcasalign[0].getX()+marcasalign[3].getX())/2*pageImage.getPreferredHorizontalResolution();
						center_y = (marcasalign[0].getY()+marcasalign[3].getY())/2*pageImage.getPreferredVerticalResolution();
						angulo_rotacion =  Math.atan(this.alignmentSlope);
						}
				}
				//pageImage.labelPageAsProcessed();
			}
			
			//Rotaciï¿½n
		if (logger.isDebugEnabled())
		{
			logger.debug("align(OMRTemplate, PageImage) - ï¿½ngulo de rotaciï¿½n: " + angulo_rotacion); //$NON-NLS-1$
		}
			transform.rotate(angulo_rotacion, center_x, center_y);
			
			//se transladan las coordenadas (con esto se solucionan los posibles movimientos de la pï¿½gina
			punto_translado.setLocation(etopleft.getX()-marcasalign[0].getX(), etopleft.getY()-marcasalign[0].getY());//en milï¿½metros
			
			punto_translado = toPixels(-punto_translado.getX(),-punto_translado.getY());
			

		if (logger.isDebugEnabled())
		{
			logger.debug("align(OMRTemplate, PageImage) - Traslado de x: " + punto_translado.getX()); //$NON-NLS-1$
			logger.debug("align(OMRTemplate, PageImage) - Traslado de y: " + punto_translado.getY()); //$NON-NLS-1$
		}
			transform.translate(punto_translado.getX(), punto_translado.getY());
			
			//se escala la transformada
		if (logger.isDebugEnabled())
		{
			logger.debug("align(OMRTemplate, PageImage) - Escala de x: " + escala_x); //$NON-NLS-1$
			logger.debug("align(OMRTemplate, PageImage) - Escala de y: " + escala_y); //$NON-NLS-1$
		}
			transform.scale(alignmentTransform.getScaleX()/escala_x, alignmentTransform.getScaleY()/escala_y);
			setAlignmentInfo(transform);
			//logger.debug("\tImage alligned in (ms)" + (System.currentTimeMillis() - taskStart)); //$NON-NLS-1$
			return marcasalign;
		}
		
		
	/**
	 * Mï¿½todo que recoge la posiciï¿½n de las cuatro marcas de alineaciï¿½n
	 * 
	 * @param campo
	 * @param pageImage 
	 * @return realpoint (array de 4 Point2D con la posiciï¿½n de las cuatro marcas de alineaciï¿½n)
	 * @throws IOException 
	 */
		private Point2D[] searchAlignMarks(Field campo, PageImage pageImage) 
		{
			Rectangle2D coords = campo.getBBox();
			
			//puntos de alineamiento esperados
			Point2D etopleft = new Point();			//expected top left point
			Point2D etopright = new Point();		//expected top right point
			Point2D ebottomleft = new Point();		//expected bottom left point
			Point2D ebottomright = new Point();		//expected bottom right point
			
			//inicializamos los puntos esperados
			etopleft.setLocation(coords.getX(), coords.getY());
			etopright.setLocation(coords.getX()+coords.getWidth(), coords.getY());
			ebottomleft.setLocation(coords.getX(), coords.getY()+coords.getHeight());
			ebottomright.setLocation(coords.getX()+coords.getWidth(), coords.getY()+coords.getHeight());
			
			//array de puntos en el que se pondrï¿½n la posiciï¿½n real de las marcas de alineamiento
			Point2D[] realpoint = new Point2D[4];
			
			realpoint[0] = pointPosition(etopleft);
			realpoint[1] = pointPosition(etopright);
			realpoint[2] = pointPosition(ebottomleft);
			realpoint[3] = pointPosition(ebottomright);
			
			/*se va a calcular la pendiente entre las esquinas superiores y entre las esquinas inferiores
			 * y se tomarï¿½ la media para calcular el ï¿½ngulo de rotaciï¿½n
			 */
			
			double pend_align_1 = (realpoint[1].getY()-realpoint[0].getY())/(realpoint[1].getX()-realpoint[0].getX());
			double pend_align_2 = (realpoint[3].getY()-realpoint[2].getY())/(realpoint[3].getX()-realpoint[2].getX());;
			setAlignmentSlope((pend_align_1+pend_align_2)/2);
			
			
			return realpoint;
		}

		/**
		 * Mï¿½todo que nos calcula la posiciï¿½n de un punto de alineaciï¿½n
		 * 
		 * @param punto
		 * @return realpoint (Point2D con la posiciï¿½n de una marca de alineaciï¿½n)
		 * @throws IOException 
		 */
		private Point2D pointPosition(Point2D punto) 
		{
			
			Point2D realpoint = new Point();
			
			Rectangle rect = new Rectangle();
			
			//se define un rectï¿½ngulo de sintonï¿½a donde hay mï¿½s posibilidades de
			//encontrar las marcas de alineamiento, el centro serï¿½n las coordenadas
			//del punto y tendrï¿½ 18x18 mm de lado
			rect.setFrame(punto.getX()-9, punto.getY()-9, 18, 18);	//medidas en mm
			
			//pasamos las coordenadas a pï¿½xeles
			rect = toPixels(rect);
			
			//se coge la subimage
			BufferedImage subimage = getImagen().getSubimage(rect.x,rect.y,rect.width,rect.height);
			if (logger.isDebugEnabled())
			{
			try
			{
				File rasterImageFile = new File("debug_corner"+System.currentTimeMillis()+".png");
				ImageIO.write(subimage,"png", rasterImageFile);
				
			}
			catch (IOException e)
			{
				// TODO Auto-generated catch block
			logger.error("Debug image error: pointPosition(Point2D)", e); //$NON-NLS-1$
			}
			}
			if (subimage == null)
			{
				throw new RuntimeException("Can't extract subimage from page.");
				}
			
			Vector<Point2D> pointsfound = new Vector<Point2D>();	//aquï¿½ almacenaremos los puntos que encontremos
			Vector<Point2D> pointshor = new Vector<Point2D>();		//aquï¿½ almacenaremos los puntos de la teï¿½rica recta horizontal
			Vector<Point2D> pointsver = new Vector<Point2D>();		//aquï¿½ almacenaremos los puntos de la teï¿½rica recta vertical
			Vector<Double> pendientehor = new Vector<Double>();		//vector para almacenar las pendientes horizontales
			Vector<Double> pendientever = new Vector<Double>();		//vector para almacenar las pendientes verticales
			
			//Se buscan los puntos de las marcas de alineaciï¿½n
			calculatePointsAlign(subimage, pointsfound);
			
			//Se calculan los puntos que pertenecen a cada pendiente (vertical y horizontal)
			calculateSlope(pointsfound, pointshor, pointsver, pendientehor,pendientever);
			
			//Se ordenan los elementos de las pendientes
			Collections.sort(pendientehor);
			Collections.sort(pendientever);
			
			//Se calculan las medianas de las pendientes (NO SIRVE)
			//calculateMedianSlope(pendientehor, pendientever);
			
			//Calculo de las rectas mediante regresiï¿½n lineal
			double sumahorX = 0;
			double sumahorY = 0;
			double sumaverX = 0;
			double sumaverY = 0;
			double mediahorX = 0;
			double mediahorY = 0;
			double mediaverX = 0;
			double mediaverY = 0;
			double sumaprodhorXX = 0;
			//double sumaprodhorYY = 0;
			double sumaprodverXX = 0;
			//double sumaprodverYY = 0;
			double prodrhor = 0;
			double prodrver = 0;
			double a_hor = 0;
			double b_hor = 0;
			double a_ver = 0;
			double b_ver = 0;
			
			Vector<Double> deshor = new Vector<Double>();		//aquï¿½ almacenaremos las desviaciones
			Vector<Double> desver = new Vector<Double>();		//aquï¿½ almacenaremos las desviaciones
			
			Vector<Double> prodhor = new Vector<Double>();		//aquï¿½ se almacenara el producto de (x-xi)*(y-yi)
			Vector<Double> prodver = new Vector<Double>();		//aquï¿½ se almacenara el producto de (x-xi)*(y-yi)
			
			//Se hace la suma de los datos de x e y
			for (Point2D markhor : pointshor) {
				sumahorX = calculateSumX(sumahorX, markhor);
				sumahorY = calculateSumY(sumahorY, markhor);
			}
			
			for (Point2D markver : pointsver) {
				sumaverX = calculateSumX(sumaverX, markver);
				sumaverY = calculateSumY(sumaverY, markver);
			}
			
			//Se calculan las medias
			mediahorX = calculateMedia(pointshor, sumahorX);
			mediahorY = calculateMedia(pointshor, sumahorY);
			
			mediaverX = calculateMedia(pointsver, sumaverX);
			mediaverY = calculateMedia(pointsver, sumaverY);
			
			//Se almacenan las desviaciones al cuadrado y el producto de (x-xi)*(y-yi)
			calculateDesvAndProd(pointshor, mediahorX, mediahorY, deshor, prodhor);
			calculateDesvAndProd(pointsver, mediaverX, mediaverY, desver, prodver);
			
			for (Double des : deshor) {
				sumaprodhorXX = calculateProd(sumaprodhorXX,des);
			}
			
			for (Double double1 : prodhor) {
				prodrhor = calculateProd(prodrhor, double1);
			}
			
			for (Double des : desver) {
				sumaprodverXX = calculateProd(sumaprodverXX, des);
			}
			
			for (Double double1 : prodver) {
				prodrver = calculateProd(prodrver, double1);
			}
			
			//por ï¿½ltimo se calculan los coeficientes a y b de ambas rectas
			b_hor = calculateBFactor(sumaprodhorXX, prodrhor);
			a_hor = calculateAFactor(mediahorX, mediahorY, b_hor);
			
			
			b_ver = calculateBFactor(sumaprodverXX, prodrver);
			a_ver = calculateAFactor(mediaverX, mediaverY, b_ver);
			
			//igualando las dos ecuaciones se obtiene que el punto de corte es el siguiente
			double x = ((a_hor-a_ver)/(b_ver-b_hor));
			double y = (a_ver + (b_ver*x));
			
			//pasamos a mm
			realpoint=toMilimeters((int)x, (int)y);
			//sumamos la posiciï¿½n del cuadro
			realpoint.setLocation(realpoint.getX()+punto.getX()-9, realpoint.getY()+punto.getY()-9);
			

		if (logger.isDebugEnabled())
		{
			logger.debug("pointPosition(Point2D) - posicion del punto: " + realpoint); //$NON-NLS-1$
		}
			
			return realpoint;
		}

		/**
		 * Calcula el factor 'a' de la regresiï¿½n lineal
		 * @param mediahorX
		 * @param mediahorY
		 * @param b_hor
		 * @return
		 */
		private double calculateAFactor(double mediaX, double mediaY,
				double b_hor) {
			double a_hor;
			a_hor = mediaY - (b_hor*mediaX);
			return a_hor;
		}

		/**
		 * Calcula el factor 'b' de la regresiï¿½n lineal
		 * 
		 * @param sumaprodhorXX
		 * @param prodrhor
		 * @return
		 */
		private double calculateBFactor(double sumaprodXX, double prodr) {
			double b_hor=new Double(prodr/sumaprodXX);
			b_hor = prodr/sumaprodXX;
			
			
			if (Double.isNaN(b_hor))//se pone un valor elevado ya que serï¿½a infinito
				return 1000000000;
			else
				return b_hor;
		}

		/**
		 * Almacena las desviaciones al cuadrado y el producto de (x-xi)*(y-yi)
		 * 
		 * @param pointsver
		 * @param mediaverX
		 * @param mediaverY
		 * @param desver
		 * @param prodver
		 */
		private void calculateDesvAndProd(Vector<Point2D> points,
				double mediaX, double mediaY, Vector<Double> des,
				Vector<Double> prodver) {
			for (Point2D markver : points) {
				double prod_des = (markver.getX()-mediaX)*(markver.getX()-mediaX);
				des.add(prod_des);
				double prod = (markver.getX()-mediaX)*(markver.getY()-mediaY);
				
				prodver.add(prod);
			}
		}

		private double calculateProd(double prod, Double double1) {
			
			prod = prod + double1;
			return prod;
		}

		private double calculateSumY(double suma, Point2D mark) {
			
			suma = suma + mark.getY();
			return suma;
		}

		private double calculateSumX(double suma, Point2D mark) {
			suma = suma + mark.getX();
			
			return suma;
		}

		/**
		 * Calcula la media de los puntos de un objecto tipo Vector
		 * 
		 * @param pointshor
		 * @param suma
		 * @return media
		 */
		private double calculateMedia(Vector<Point2D> points, double suma) {
			double media;
			
			media=suma/points.size();
			return media;
		}

		/**
		 * Busca los puntos de las marcas de alineaciï¿½n
		 * 
		 * @param subimage
		 * @param pointsfound
		 */
		private void calculatePointsAlign(BufferedImage subimage,
				Vector<Point2D> pointsfound) {
			//se fija la coordenada "y" y se hacen barridos sobre la "x" cada 3 px
			for (int y = 1; y < subimage.getHeight(); y += 2)
			{
				for (int x = 1; x < subimage.getWidth(); x += 2)
				{
					float luminance = BufferedImageUtil.getLuminance(subimage, y, x);
					boolean isblack = (luminance < 0.5 ? true : false);

					if (isblack) {
						Point2D point = new Point();
						point.setLocation(x, y);
						pointsfound.add(point);
					}
				}
			}
		}

		/**
		 * Calcula la posiciï¿½n de los puntos que pertenecen a las rectas horizontal y vertical
		 * 
		 * @param pointsfound
		 * @param pointshor
		 * @param pointsver
		 * @param pendientehor
		 * @param pendientever
		 */
		private void calculateSlope(Vector<Point2D> pointsfound,
				Vector<Point2D> pointshor, Vector<Point2D> pointsver,
				Vector<Double> pendientehor, Vector<Double> pendientever) {
			
			//recta horizontal
			int i=1;
			for (Point2D markpoint : pointsfound) {
				if(i < pointsfound.size()){					
					double newpendiente1 = 
						(markpoint.getY()-pointsfound.elementAt(i).getY())/(markpoint.getX()-pointsfound.elementAt(i).getX());
					//para la teï¿½rica recta horizontal
					if (Math.abs(newpendiente1)<0.5){
						pendientehor.add(newpendiente1);
						//introducimos los dos puntos
						pointshor.add(pointsfound.elementAt(i-1));
						pointshor.add(pointsfound.elementAt(i));
					}
				}
				i++;
			}
			
			//recta vertical
			i=1;
			for (Point2D markpoint : pointsfound) {
				if(i < pointsfound.size()){
					/**System.out.println("y:   "+markpoint.getY());
					System.out.println("y-1: "+pointsfound.elementAt(i).getY());
					System.out.println("x:   "+markpoint.getX());
					System.out.println("x-1: "+pointsfound.elementAt(i).getX());
					System.out.println("------------------------------------------------");*/
					
					double newpendiente2 = 
						(markpoint.getY()-pointsfound.elementAt(i).getY())/(markpoint.getX()-pointsfound.elementAt(i).getX());
					//para la teï¿½rica recta vertical
					if (Math.abs(newpendiente2)>=0.5){
						pendientever.add(newpendiente2);
						//introducimos el elemento
						pointsver.add(pointsfound.elementAt(i-1));
						pointsver.add(pointsfound.elementAt(i));
					}
				}
				i++;
			}
		}
		
	/**
	 * Sets the transformation data needed for aligning the Page
	 * contains information about the traslation and rotation of the page in pixels units.
	 * Need to consider image scale to convert from milimeters
	 * @param transform
	 * @see AffineTransform
	 * @see getVerticalRatio
	 * @see getHorizontalRatio
	 */
	public void setAlignmentInfo(AffineTransform transform)
	{
		this.alignmentTransform=transform;
	}
	
	public AffineTransform getAllignmentInfo()
	{
		if (this.alignmentTransform==null)
			resetAlignmentInfo();
		return alignmentTransform;
		}
	/**
	 * Try to detect alignment marks and create the spatial transformation info
	 * @see java.awt.geom.AffineTransform 
	 */
	public void alignImage()
	{
		long funcStart = System.currentTimeMillis();
		
		AffineTransform transform=getAllignmentInfo();
		
		// obtain rotation and traslation
		transform.translate(0, 0); //pixels
		
		setAlignmentInfo(transform);
		
		logger.debug("Page aligned in (ms)" + (System.currentTimeMillis() - funcStart)); //$NON-NLS-1$

	}
	/**
	 * Mark the results in a thumbnail of the page intended for reporting
	 */
	public void labelPageAsProcessed()
	{
		
		BufferedImage imagen = getReportingImage();
		Graphics2D g=imagen.createGraphics();
		g.setColor(Color.RED);
		g.drawString("Page Processed at:"+new Date()+" W:"+imagen.getWidth()+" H:"+imagen.getHeight(), 10, 10);
		
	}

	/**
	 * Creates an small-resolution image for reporting purposes
	 * @return
	 */
	public BufferedImage getReportingImage()
	{
		if (reportImage==null)
		{
			// Create a fixed (smaller) resolution image
			int w=Math.min(REPORTING_WIDTH,getImagen().getWidth());
			int h=getImagen().getHeight()*w/getImagen().getWidth();
			
			reportImage=new BufferedImage(w, h,BufferedImage.TYPE_INT_RGB);
			Image scaledImage=getImagen().getScaledInstance(w, h, Image.SCALE_DEFAULT);
			reportImage.createGraphics().drawImage(scaledImage, 0, 0, null);
		}
	
		return reportImage;
	}

	/**
	 * @return
	 */
	public abstract String getType();

	/**
	 * @return
	 */
	public abstract String getName();

	/**
	 * Free up memory resources
	 */
	public void freeMemory()
	{
		long tstart=System.currentTimeMillis();
		long freeMem=Runtime.getRuntime().freeMemory();
		long availMem=Runtime.getRuntime().totalMemory();
		reportImage.flush();
		reportImage=null;
		
		image.flush();
		setImagen(null);
		System.gc();

		if (logger.isDebugEnabled())
		{
			logger.debug("endUse()-Free Memory in "+(System.currentTimeMillis()-tstart)+" ms TotalMem:"+availMem/1024/1024+" MB freeMem Before:" + freeMem/1024/1024 + ", freeMem After:" + Runtime.getRuntime().freeMemory()/1024/1024); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}

	/**
	 * @param outputdir
	 * @throws IOException 
	 */
	public void outputMarkedPage(String outputdir) throws IOException
	{
		File debugImagePath;
							
			debugImagePath = File.createTempFile("OMR_original_marked", ".jpg", new File(outputdir));
			ImageIO.write(getReportingImage(), "JPG", debugImagePath);
		
	}

	/**
	 * @param outputdir
	 * @throws IOException 
	 */
	public void outputWorkingPage(String outputdir) throws IOException
	{
		File debugImagePath;
		
		debugImagePath = File.createTempFile("OMR_working_debug", ".jpg", new File(outputdir));		
		ImageIO.write(getImagen(), "JPG", debugImagePath);
	
	}

	/**
	 * Returns the ratio between pixels and milimeters
	 * default implementation getImagen().getWidth()/PageImage.a4width
	 * @return resolution in pixels/mm
	 */
	public abstract double getPreferredHorizontalResolution();
	/**
	 * Returns the ratio between pixels and milimeters
	 * default implementation getImagen().getHeight()/PageImage.a4height
	 * @return resolution in pixels/mm
	 */
	public abstract double getPreferredVerticalResolution();

	/**
	 * Use alignment information to transform from milimeters to pixel coordinates at the preferred resolution for this page
	 * @param x
	 * @return
	 */
	public Point toPixels(double x,double y)
	{
		AffineTransform alignment=getAllignmentInfo();
		Point2D coord=new Point();
		coord.setLocation(x, y);
		Point coordTransformed=new Point();
		alignment.transform(coord, coordTransformed);
		
		return coordTransformed;
	}
	
	public Point toPixelsPoint(double x,double y)
	{
		AffineTransform alignment=getAllignmentInfo();
		alignment.setToScale(getPreferredHorizontalResolution(), getPreferredVerticalResolution());
		
		Point2D coord=new Point();
		coord.setLocation(x, y);
		Point coordTransformed=new Point();
		alignment.transform(coord, coordTransformed);
		
		return coordTransformed;
	}

	/**
	 * Obtain a subimage from the pageimage (in milimeters and related to actual paper)
	 * Place to make optimizations when rendering high resolution files.
	 * It takes into account the traslation and rotation of the physical page.
	 * 
	 * Default implementation uses getImage() which should decode entire image.
	 * @param x mm
	 * @param y mm
	 * @param w mm
	 * @param h mm
	 * @param imageType
	 * @return SubImage
	 * @see SubImage
	 */
	public SubImage getSubimage(double x, double y, double w, double h, int imageType)
	{
	Rectangle2D rectMM=new Rectangle();
	rectMM.setFrame(x,y,w,h);
	return getSubimage(rectMM, imageType);
	}

	/**
	 * Convert from  pixel-space to paper-space.
	 * Paper-space refers to logical area of the paper in the image.
	 * Pixel-space refers to entire area of the image that contains the image of the paper. (Maybe with offset and rotation)
	 * @param i
	 * @param j
	 * @return
	 * @throws NoninvertibleTransformException 
	 */
	public Point2D toMilimeters(int i, int j) 
	{

		try
		{
			AffineTransform tr = getAllignmentInfo();
			AffineTransform inv;
			inv = tr.createInverse();
			Point2D pixeles = new Point(i, j);
			Point2D dest=null;
			return inv.transform(pixeles, dest);
		}
		catch (NoninvertibleTransformException e)
		{
			throw new RuntimeException("error page definition.",e);
		}

	}

	/**
	 * Convert box from Milimeters to Pixels relative to the PageImage in the preferred resolution
	 * in case of alignment rotation the returned Rectangle is the minimum bounding box that contains
	 * the original corners transformed.
	 * @param box in milimeters related to actual page
	 * @return minimum bounding box in pixels related to image representation
	 */
	public Rectangle toPixels(Rectangle2D box)
	{
		
		Point p1=toPixels(box.getX(),box.getY());
		Point p2=toPixels(box.getMaxX(),box.getMaxY());
		Rectangle bboxPx=new Rectangle(p1);
		bboxPx.add(p2);
		return bboxPx;
		
	}

	/**
	 * Scaled graphics for drawing on a small version of the page.
	 * @return
	 */
	public Graphics2D getReportingGraphics()
	{
		BufferedImage reportingImage = this.getReportingImage();
		Graphics2D g=reportingImage.createGraphics();
		AffineTransform trans= g.getTransform();
		trans.scale(reportingImage.getWidth()/(getPreferredHorizontalResolution()*PageImage.a4width),
				reportingImage.getHeight()/(getPreferredVerticalResolution()*PageImage.a4height));
		g.setTransform(trans);
		return g;
	}

	/**
	 * @param rectMM bounding box in milimeters
	 * @see #getSubimage(double, double, double, double, int)
	 * @return
	 */
	public SubImage getSubimage(Rectangle2D rectMM, int imageType)
	{
		
		Rectangle rect=this.toPixels(rectMM);
		Point upperLeft=this.toPixels(rectMM.getX(), rectMM.getY());
		
		//TODO: incluir la resoluciï¿½n preferida ahora asume la nativa de la imagen
		Point reference=upperLeft;
		
		BufferedImage originalSubImage=getImagen().getSubimage(rect.x,rect.y, rect.width, rect.height);
		
		//rotate image
		SubImage subimage=new SubImage(rect.width,rect.height,imageType);
		subimage.setReference(reference);
		subimage.setBoundingBox(rect);
		
		
		Graphics2D g=subimage.createGraphics();
		g.drawImage(originalSubImage, 0,0, null);
		
		return subimage;
	}

	/**
	 * @param pointMM
	 * @return
	 */
	public Point toPixels(Point2D pointMM)
	{
		
		return toPixels(pointMM.getX(), pointMM.getY());
	}

	/**
	 * @param templateRectPx
	 * @return
	 */
	public Rectangle2D toMilimeters(Rectangle boxPx)
	{
		Point2D p1=toMilimeters(boxPx.x,boxPx.y);
		Point2D p2=toMilimeters((int)boxPx.getMaxX(),(int)boxPx.getMaxY());
		Rectangle2D bbox=new Rectangle();
		bbox.setFrameFromDiagonal(p1, p2);
		
		return bbox;
	}
	
}