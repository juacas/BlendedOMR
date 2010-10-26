package org.uva.itast.blended.omr.scanners;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.util.Collections;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.uva.itast.blended.omr.BufferedImageUtil;
import org.uva.itast.blended.omr.OMRTemplate;
import org.uva.itast.blended.omr.pages.AbstractAlignMarkDetector;
import org.uva.itast.blended.omr.pages.PageImage;
import org.uva.itast.blended.omr.pages.SubImage;

public class AlignMarkRodilanaDetector extends AbstractAlignMarkDetector
{
	/**
	 * Logger for this class
	 */
	public static final Log	logger	=LogFactory.getLog(AlignMarkRodilanaDetector.class);

	public AlignMarkRodilanaDetector(OMRTemplate template)
	{
		super(template);
	}

	/**
	 * Mï¿½todo que nos calcula la posiciï¿½n de un punto de alineaciï¿½n
	 * 
	 * @param pageImage
	 *            TODO
	 * @param expectedPoint
	 * @return realpoint (Point2D con la posiciï¿½n de una marca de
	 *         alineaciï¿½n)
	 * @throws IOException
	 */
	public Point2D pointPosition(PageImage pageImage, Point2D expectedPoint)
	{

		Rectangle2D expectedRect=getExpectedRect(expectedPoint);

		SubImage subimage=extractSubimage(pageImage, expectedRect);
		logImage(subimage);
		Vector<Point2D> pointshor=new Vector<Point2D>(); // aquï¿½ almacenaremos
															// los puntos de la
															// teï¿½rica recta
															// horizontal
		Vector<Point2D> pointsver=new Vector<Point2D>(); // aquï¿½ almacenaremos
															// los puntos de la
															// teï¿½rica recta
															// vertical
		Vector<Double> pendientehor=new Vector<Double>(); // vector para
															// almacenar las
															// pendientes
															// horizontales
		Vector<Double> pendientever=new Vector<Double>(); // vector para
															// almacenar las
															// pendientes
															// verticales

		// Se buscan los puntos de las marcas de alineaciï¿½n
		Vector<Point2D> pointsfound=calculatePointsAlign(subimage);
		; // aquï¿½ almacenaremos los puntos que encontremos

		// Se calculan los puntos que pertenecen a cada pendiente (vertical y
		// horizontal)
		calculateSlope(pointsfound, pointshor, pointsver, pendientehor, pendientever);

		// Se ordenan los elementos de las pendientes
		Collections.sort(pendientehor);
		Collections.sort(pendientever);

		// Se calculan las medianas de las pendientes (NO SIRVE)
		// calculateMedianSlope(pendientehor, pendientever);

		// Calculo de las rectas mediante regresiï¿½n lineal
		double sumahorX=0;
		double sumahorY=0;
		double sumaverX=0;
		double sumaverY=0;
		double mediahorX=0;
		double mediahorY=0;
		double mediaverX=0;
		double mediaverY=0;
		double sumaprodhorXX=0;
		// double sumaprodhorYY = 0;
		double sumaprodverXX=0;
		// double sumaprodverYY = 0;
		double prodrhor=0;
		double prodrver=0;
		double a_hor=0;
		double b_hor=0;
		double a_ver=0;
		double b_ver=0;

		Vector<Double> deshor=new Vector<Double>(); // aquï¿½ almacenaremos las
													// desviaciones
		Vector<Double> desver=new Vector<Double>(); // aquï¿½ almacenaremos las
													// desviaciones

		Vector<Double> prodhor=new Vector<Double>(); // aquï¿½ se almacenara el
														// producto de
														// (x-xi)*(y-yi)
		Vector<Double> prodver=new Vector<Double>(); // aquï¿½ se almacenara el
														// producto de
														// (x-xi)*(y-yi)

		// Se hace la suma de los datos de x e y
		for (Point2D markhor : pointshor)
		{
			sumahorX=calculateSumX(sumahorX, markhor);
			sumahorY=calculateSumY(sumahorY, markhor);
		}

		for (Point2D markver : pointsver)
		{
			sumaverX=calculateSumX(sumaverX, markver);
			sumaverY=calculateSumY(sumaverY, markver);
		}

		// Se calculan las medias
		mediahorX=calculateMedia(pointshor, sumahorX);
		mediahorY=calculateMedia(pointshor, sumahorY);

		mediaverX=calculateMedia(pointsver, sumaverX);
		mediaverY=calculateMedia(pointsver, sumaverY);

		// Se almacenan las desviaciones al cuadrado y el producto de
		// (x-xi)*(y-yi)
		calculateDesvAndProd(pointshor, mediahorX, mediahorY, deshor, prodhor);
		calculateDesvAndProd(pointsver, mediaverX, mediaverY, desver, prodver);

		for (Double des : deshor)
		{
			sumaprodhorXX=calculateProd(sumaprodhorXX, des);
		}

		for (Double double1 : prodhor)
		{
			prodrhor=calculateProd(prodrhor, double1);
		}

		for (Double des : desver)
		{
			sumaprodverXX=calculateProd(sumaprodverXX, des);
		}

		for (Double double1 : prodver)
		{
			prodrver=calculateProd(prodrver, double1);
		}

		// por ï¿½ltimo se calculan los coeficientes a y b de ambas rectas
		b_hor=calculateBFactor(sumaprodhorXX, prodrhor);
		a_hor=calculateAFactor(mediahorX, mediahorY, b_hor);

		b_ver=calculateBFactor(sumaprodverXX, prodrver);
		a_ver=calculateAFactor(mediaverX, mediaverY, b_ver);

		// igualando las dos ecuaciones se obtiene que el punto de corte es el
		// siguiente
		double x=((a_hor - a_ver) / (b_ver - b_hor));
		double y=(a_ver + (b_ver * x));

		// pasamos a mm
		Point2D realpoint=pageImage.toMilimeters((int) x, (int) y);
		// sumamos la posiciï¿½n del cuadro
		realpoint.setLocation(realpoint.getX() + expectedRect.getX(), realpoint.getY() + expectedRect.getY());

		if (logger.isDebugEnabled())
		{
			logger.debug("pointPosition(Point2D) - posicion del punto: " + realpoint); //$NON-NLS-1$
		}

		return realpoint;
	}

	/**
	 * Calcula el factor 'a' de la regresiï¿½n lineal
	 * 
	 * @param mediahorX
	 * @param mediahorY
	 * @param mediaX
	 *            TODO
	 * @param mediaY
	 *            TODO
	 * @param b_hor
	 * @return
	 */
	public double calculateAFactor(double mediaX, double mediaY, double b_hor)
	{
		double a_hor;
		a_hor=mediaY - (b_hor * mediaX);
		return a_hor;
	}

	/**
	 * Calcula el factor 'b' de la regresiï¿½n lineal
	 * 
	 * @param sumaprodhorXX
	 * @param prodrhor
	 * @param sumaprodXX
	 *            TODO
	 * @param prodr
	 *            TODO
	 * @return
	 */
	public double calculateBFactor(double sumaprodXX, double prodr)
	{
		double b_hor=new Double(prodr / sumaprodXX);
		b_hor=prodr / sumaprodXX;

		if (Double.isNaN(b_hor))// se pone un valor elevado ya que serï¿½a
								// infinito
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
	 * @param points
	 *            TODO
	 * @param mediaX
	 *            TODO
	 * @param mediaY
	 *            TODO
	 * @param des
	 *            TODO
	 * @param prodver
	 */
	public void calculateDesvAndProd(Vector<Point2D> points, double mediaX, double mediaY, Vector<Double> des, Vector<Double> prodver)
	{
		for (Point2D markver : points)
		{
			double prod_des=(markver.getX() - mediaX) * (markver.getX() - mediaX);
			des.add(prod_des);
			double prod=(markver.getX() - mediaX) * (markver.getY() - mediaY);

			prodver.add(prod);
		}
	}

	public double calculateProd(double prod, Double double1)
	{

		prod=prod + double1;
		return prod;
	}

	public double calculateSumY(double suma, Point2D mark)
	{

		suma=suma + mark.getY();
		return suma;
	}

	public double calculateSumX(double suma, Point2D mark)
	{
		suma=suma + mark.getX();

		return suma;
	}

	/**
	 * Calcula la media de los puntos de un objecto tipo Vector
	 * 
	 * @param pointshor
	 * @param points
	 *            TODO
	 * @param suma
	 * @return media
	 */
	public double calculateMedia(Vector<Point2D> points, double suma)
	{
		double media;

		media=suma / points.size();
		return media;
	}

	/**
	 * Busca los puntos de las marcas de alineaciï¿½n
	 * 
	 * @param subimage
	 * @param pointsfound
	 * @return
	 */
	public Vector<Point2D> calculatePointsAlign(SubImage subimage)
	{
		Vector<Point2D> pointsfound=new Vector<Point2D>(); // aquï¿½
															// almacenaremos los
															// puntos que
															// encontremos
		Point geomReference=subimage.getReference(); // the subimage is
														// extracted from this
														// coordinate.
		Rectangle captured=subimage.getCapturedBoundingBox();
		Rectangle represented=subimage.getBoundingBox();
		Point reference=subimage.getReference();
		int xstart=Math.max(captured.x - reference.x, 0);
		int ystart=Math.max(captured.y - reference.y, 0);
		// se fija la coordenada "y" y se hacen barridos sobre la "x" cada 3 px
		float[] statsLum=BufferedImageUtil.statsLuminance(subimage, 1);
		float threshold=(statsLum[2] + statsLum[1]) / 2;
		for (int y=ystart; y < ystart + captured.height; y+=2)
		{
			for (int x=xstart; x < xstart + captured.width; x+=2)
			{
				float luminance=BufferedImageUtil.getLuminance(subimage, y, x);
				boolean isblack=(luminance < threshold ? true : false);

				if (isblack)
				{
					Point2D point=new Point(x + geomReference.x, y + geomReference.y);
					pointsfound.add(point);
				}
			}
		}
		return pointsfound;
	}

	/**
	 * Calcula la posiciï¿½n de los puntos que pertenecen a las rectas
	 * horizontal y vertical
	 * 
	 * @param pointsfound
	 * @param pointshor
	 * @param pointsver
	 * @param pendientehor
	 * @param pendientever
	 */
	public void calculateSlope(Vector<Point2D> pointsfound, Vector<Point2D> pointshor, Vector<Point2D> pointsver, Vector<Double> pendientehor,
		Vector<Double> pendientever)
	{

		// recta horizontal
		int i=1;
		for (Point2D markpoint : pointsfound) // TODO: Esto no funciona!! FIXME
												// No tiene sentido este bucle
												// mixto iterador/contador!!
		{
			if (i < pointsfound.size())
			{
				double newpendiente=(markpoint.getY() - pointsfound.elementAt(i).getY()) / (markpoint.getX() - pointsfound.elementAt(i).getX());
				// para la teï¿½rica recta horizontal
				if (Math.abs(newpendiente) < 0.5)
				{
					pendientehor.add(newpendiente);
					// introducimos los dos puntos
					pointshor.add(pointsfound.elementAt(i - 1));
					pointshor.add(pointsfound.elementAt(i));
				}
				else
				{
					pendientever.add(newpendiente);
					// introducimos el elemento
					pointsver.add(pointsfound.elementAt(i - 1));
					pointsver.add(pointsfound.elementAt(i));
				}
			}
			i++;
		}

	}
}