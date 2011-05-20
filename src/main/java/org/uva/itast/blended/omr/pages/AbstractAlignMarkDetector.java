package org.uva.itast.blended.omr.pages;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Hashtable;

import javax.imageio.ImageIO;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.uva.itast.blended.omr.Field;
import org.uva.itast.blended.omr.OMRProcessor;
import org.uva.itast.blended.omr.OMRTemplate;

public abstract class AbstractAlignMarkDetector implements AlignMarkDetector
{
	/**
	 * Logger for this class
	 */
	private static final Log	logger		=LogFactory.getLog(AbstractAlignMarkDetector.class);

	private double				alignmentSlope;
	protected OMRTemplate		template;
	private int					bufferWidth	=9 * 2;

	private OMRProcessor		omrProcessor;

	public AbstractAlignMarkDetector(OMRTemplate template, OMRProcessor omrProcessor)
	{
		this.template=template;
		this.omrProcessor=omrProcessor;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.uva.itast.blended.omr.pages.AlignMarkDetector#getBufferWidth()
	 */
	public int getBufferWidth()
	{
		return bufferWidth;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.uva.itast.blended.omr.pages.AlignMarkDetector#setBufferWidth(int)
	 */
	public void setBufferWidth(int bufferWidth)
	{
		this.bufferWidth=bufferWidth;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.uva.itast.blended.omr.pages.AlignMarkDetector#getAlignmentSlope()
	 */
	public double getAlignmentSlope()
	{
		return alignmentSlope;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.uva.itast.blended.omr.pages.AlignMarkDetector#setAlignmentSlope(double
	 * )
	 */
	public void setAlignmentSlope(double alignmentSlope)
	{
		this.alignmentSlope=alignmentSlope;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.uva.itast.blended.omr.pages.AlignMarkDetector#align(org.uva.itast
	 * .blended.omr.pages.PageImage)
	 */
	public Point2D[] align(PageImage pageImage)
	{
		long taskStart=System.currentTimeMillis();
		AffineTransform transform=new AffineTransform();

		Point2D[] marcasalign=new Point2D[4];

		double dist_original_x;
		double dist_imagen_x;
		double dist_original_y;
		double dist_imagen_y;
		double escala_x=1;
		double escala_y=1;
		// angulo de rotacion
		double angulo_rotacion=0;
		// centro de rotacion, por defecto el centro de la pagina
		double center_x=pageImage.a4width * pageImage.getPreferredHorizontalResolution() / 2;
		double center_y=pageImage.a4height * pageImage.getPreferredVerticalResolution() / 2;

		Point2D etopleft=new Point(); // expected top left point
		Point2D etopright=new Point(); // expected top right point
		Point2D ebottomleft=new Point(); // expected bottom left point
		Point2D ebottomright=new Point(); // expected bottom right point

		Point punto_translado=new Point();

		// TODO: check what to do with multipage documents
		// for (int i = 1; i <= template.getNumPaginas(); i++)
		// for now, use the first page for the frame
		int i=1;
		{
			// Hastable para almacenar los campos que leemos del fichero de
			// definiciï¿½n de marcas
			Hashtable<String, Field> fields=template.getPage(i).getFields();
			// Collection<Field> campos_val = campos.values();
			// for (Field campo : campos_val)
			// use only the field "align"
			Field campo=fields.get("Align");
			{
				int tipo=campo.getTipo();

				if (tipo == Field.FRAME) // FRAME = 2
				{
					// Se guarda la esquina superior izquierda
					Rectangle2D coords=campo.getBBox();

					etopleft.setLocation(coords.getX(), coords.getY());
					etopright.setLocation(coords.getX() + coords.getWidth(), coords.getY());
					ebottomleft.setLocation(coords.getX(), coords.getY() + coords.getHeight());
					ebottomright.setLocation(coords.getX() + coords.getWidth(), coords.getY() + coords.getHeight());

					// Se buscan las marcas de alineaciï¿½n
					marcasalign=searchAlignMarks(campo, pageImage);
					/*
					 * se va a calcular la pendiente entre las esquinas
					 * superiores y entre las esquinas inferiores y se tomarï¿½
					 * la media para calcular el ï¿½ngulo de rotaciï¿½n
					 */
					Point2D dtopleft=marcasalign[0];
					Point2D dtopright=marcasalign[1];
					Point2D dbottomleft=marcasalign[2];
					Point2D dbottomright=marcasalign[3];
					
					Graphics g=pageImage.getReportingImage().getGraphics();
					g.setColor(Color.GREEN);
					g.drawRect((int)dtopleft.getX(), (int)dtopleft.getY(), (int)(dbottomright.getX()-dtopleft.getX()),(int) (dbottomright.getY()-dtopleft.getY()));
					
					double pend_align_1=(dtopright != null && dtopleft != null) ? (dtopright.getY() - dtopleft.getY())
						/ (dtopright.getX() - dtopleft.getX()) : Double.NaN;
					double pend_align_2=(dbottomleft != null && dbottomright != null) ? (dbottomright.getY() - dbottomleft.getY())
						/ (dbottomright.getX() - dbottomleft.getX()) : Double.NaN;

					if (pend_align_1 != Double.NaN && pend_align_2 != Double.NaN)
					{
						setAlignmentSlope((pend_align_1 + pend_align_2) / 2);
					}
					else if (pend_align_1 == Double.NaN && pend_align_2 != Double.NaN)
					{
						setAlignmentSlope(pend_align_2);
					}
					else if (pend_align_1 != Double.NaN && pend_align_2 == Double.NaN)
					{
						setAlignmentSlope(pend_align_1);
					}

					// Se calcula la escala
					dist_original_x=Math.sqrt(((etopright.getX() - etopleft.getX()) * (etopright.getX() - etopleft.getX()))
						+ (((etopright.getY() - etopleft.getY()) * (etopright.getY() - etopleft.getY()))));
					if (dtopleft != null && dtopright != null)
					{
						dist_imagen_x=Math.sqrt(((dtopright.getX() - dtopleft.getX()) * (dtopright.getX() - dtopleft.getX()))
							+ ((dtopright.getY() - dtopleft.getY()) * (dtopright.getY() - dtopleft.getY())));
					}
					else
					{
						dist_imagen_x=Math.sqrt(((dbottomright.getX() - dbottomleft.getX()) * (dbottomright.getX() - dbottomleft.getX()))
							+ ((dbottomright.getY() - dbottomleft.getY()) * (dbottomright.getY() - dbottomleft.getY())));
					}

					dist_original_y=Math.sqrt(((etopleft.getX() - ebottomleft.getX()) * (etopleft.getX() - ebottomleft.getX()))
						+ ((etopleft.getY() - ebottomleft.getY()) * (etopleft.getY() - ebottomleft.getY())));

					if (dtopleft != null && dbottomleft != null)
					{
						dist_imagen_y=Math.sqrt(((dbottomleft.getX() - dtopleft.getX()) * (dbottomleft.getX() - dtopleft.getX()))
							+ ((dbottomleft.getY() - dtopleft.getY()) * (dbottomleft.getY() - dtopleft.getY())));
					}
					else
					{
						dist_imagen_y=Math.sqrt(((dbottomright.getX() - dtopright.getX()) * (dbottomright.getX() - dtopright.getX()))
							+ ((dbottomright.getY() - dtopright.getY()) * (dbottomright.getY() - dtopright.getY())));
					}

					escala_x=dist_original_x / dist_imagen_x;
					escala_y=dist_original_y / dist_imagen_y;

					// margen aceptable en el escalado
					if (Math.abs(1 - escala_x) < 0.02)
						escala_x=1;
					if (Math.abs(1 - escala_y) < 0.02)
						escala_y=1;

					// System.out.println("PENDIENTE: "+getPendiente_alineacion());

					// obtencion del centro de rotacion, se supone el centro del
					// marco de alineacion
					center_x=(marcasalign[0].getX() + marcasalign[3].getX()) / 2 * pageImage.getPreferredHorizontalResolution();
					center_y=(marcasalign[0].getY() + marcasalign[3].getY()) / 2 * pageImage.getPreferredVerticalResolution();
					angulo_rotacion=Math.atan(getAlignmentSlope());

				}
			}
			// pageImage.labelPageAsProcessed();
		}

		// Rotaciï¿½n
		if (logger.isDebugEnabled())
		{
			logger.debug("align(OMRTemplate, PageImage) - ï¿½ngulo de rotaciï¿½n: " + angulo_rotacion * 180 / Math.PI + " degrees."); //$NON-NLS-1$
		}
		transform.rotate(angulo_rotacion, center_x, center_y);

		// se transladan las coordenadas (con esto se solucionan los posibles
		// movimientos de la pï¿½gina
		punto_translado.setLocation(etopleft.getX() - marcasalign[0].getX(), etopleft.getY() - marcasalign[0].getY());// en
																														// milï¿½metros

		punto_translado=pageImage.toPixels(-punto_translado.getX(), -punto_translado.getY());

		if (logger.isDebugEnabled())
		{
			logger.debug("align(OMRTemplate, PageImage) - Traslado de x: " + punto_translado.getX()); //$NON-NLS-1$
			logger.debug("align(OMRTemplate, PageImage) - Traslado de y: " + punto_translado.getY()); //$NON-NLS-1$
		}
		transform.translate(punto_translado.getX(), punto_translado.getY());

		// se escala la transformada
		if (logger.isDebugEnabled())
		{
			logger.debug("align(OMRTemplate, PageImage) - Escala de x: " + escala_x); //$NON-NLS-1$
			logger.debug("align(OMRTemplate, PageImage) - Escala de y: " + escala_y); //$NON-NLS-1$
		}
		// TODO check this way of applying the transformation. Shoul reuse and
		// modify original transform.
		transform.scale(pageImage.getAllignmentInfo().getScaleX() / escala_x, pageImage.getAllignmentInfo().getScaleY() / escala_y);
		pageImage.setAlignmentInfo(transform);
		logger.debug("\tImage alligned in (ms)" + (System.currentTimeMillis() - taskStart)); //$NON-NLS-1$
		return marcasalign;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.uva.itast.blended.omr.pages.AlignMarkDetector#searchAlignMarks(org
	 * .uva.itast.blended.omr.Field, org.uva.itast.blended.omr.pages.PageImage)
	 */
	public Point2D[] searchAlignMarks(Field campo, PageImage pageImage)
	{
		Rectangle2D coords=campo.getBBox();

		// puntos de alineamiento esperados
		Point2D etopleft=new Point(); // expected top left point
		Point2D etopright=new Point(); // expected top right point
		Point2D ebottomleft=new Point(); // expected bottom left point
		Point2D ebottomright=new Point(); // expected bottom right point

		// inicializamos los puntos esperados
		etopleft.setLocation(coords.getX(), coords.getY());
		etopright.setLocation(coords.getX() + coords.getWidth(), coords.getY());
		ebottomleft.setLocation(coords.getX(), coords.getY() + coords.getHeight());
		ebottomright.setLocation(coords.getX() + coords.getWidth(), coords.getY() + coords.getHeight());

		Point2D dtopleft=pointPosition(pageImage, etopleft); // detected top
																// left point
		Point2D dtopright=pointPosition(pageImage, etopright); // detected top
																// right point
		Point2D dbottomleft=pointPosition(pageImage, ebottomleft); // detected
																	// bottom
																	// left
																	// point
		Point2D dbottomright=pointPosition(pageImage, ebottomright); // detected
																		// bottom
																		// right
																		// point

		Point2D[] realpoint=new Point2D[] { dtopleft, dtopright, dbottomleft, dbottomright };
		return realpoint;
	}

	public abstract Point2D pointPosition(PageImage pageImage, Point2D expectedPoint);

	/**
	 * @param pageImage
	 * @param expectedRect
	 * @return
	 */
	public SubImage extractSubimage(PageImage pageImage, Rectangle2D expectedRect)
	{
		// se coge la subimage
		SubImage subimage=pageImage.getSubimage(expectedRect, BufferedImage.TYPE_BYTE_GRAY);
		if (subimage == null)
		{
			throw new RuntimeException("Can't extract subimage from page.");
		}
		return subimage;
	}

	/**
	 * @param subimage
	 */
	protected void logImage(BufferedImage subimage)
	{
		if (logger.isDebugEnabled())
		{
			try
			{
				File rasterImageFile=omrProcessor != null ? File.createTempFile("debugCorners", ".png", new File(omrProcessor.getOutputdir())) : File
					.createTempFile("debugCorners", ".png");
				// rasterImageFile.deleteOnExit();
				logger.debug("Debug output to " + rasterImageFile); //$NON-NLS-1$
				ImageIO.write(subimage, "png", rasterImageFile);

			}
			catch (IOException e)
			{
				// TODO Auto-generated catch block
				logger.error("Debug image error: pointPosition(Point2D)", e); //$NON-NLS-1$
			}
		}
	}

	/**
	 * @param expectedPoint
	 * @return
	 */
	public Rectangle2D getExpectedRect(Point2D expectedPoint)
	{
		Rectangle2D expectedRect=new Rectangle();

		expectedRect.setFrame(expectedPoint.getX() - getBufferWidth(), expectedPoint.getY() - getBufferWidth(), getBufferWidth() * 2,
			getBufferWidth() * 2); // medidas en mm
		return expectedRect;
	}

}