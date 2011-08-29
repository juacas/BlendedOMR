package org.uva.itast.blended.omr.pages;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Map;
import java.util.Map.Entry;

import javax.imageio.ImageIO;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.uva.itast.blended.omr.Field;
import org.uva.itast.blended.omr.OMRProcessor;
import org.uva.itast.blended.omr.OMRTemplate;
import org.uva.itast.blended.omr.pages.AlignmentResult.AlignmentPosition;

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
	public AlignmentResult align(PageImage pageImage)
	{
		long taskStart=System.currentTimeMillis();

		double escala_x=1;
		double escala_y=1;
		// angulo de rotacion
		double angulo_rotacion=0;
		// centro de rotacion, por defecto el centro de la pagina
		int d_center_xpx=(int) (pageImage.a4width * pageImage.getPreferredHorizontalResolution() / 2);
		int d_center_ypx=(int) (pageImage.a4height * pageImage.getPreferredVerticalResolution() / 2);

		double d_center_ymm;
		double d_center_xmm;
		// TODO: check what to do with multipage documents
		// for (int i = 1; i <= template.getNumPaginas(); i++)
		// for now, use the first page for the frame
		int i=1;

		// Hastable para almacenar los campos que leemos del fichero de
		// definiciï¿½n de marcas
		Hashtable<String, Field> fields=template.getPage(i).getFields();
		// Collection<Field> campos_val = campos.values();
		// for (Field campo : campos_val)
		// use only the field "align"
		Field campo=fields.get("Align");

		int tipo=campo.getTipo();

		if (tipo == Field.FRAME) // FRAME = 2
		{
			// Se guarda las esquinas
			Rectangle2D coords=campo.getBBox();
			// PagePoint etopleft=new PagePoint(pageImage,coords.getX(),
			// coords.getY()); // expected top left point
			// PagePoint etopright=new PagePoint(pageImage,coords.getX() +
			// coords.getWidth(), coords.getY()); // expected top right point
			// PagePoint ebottomleft=new PagePoint(pageImage,coords.getX(),
			// coords.getY() + coords.getHeight()); // expected bottom left
			// point
			// PagePoint ebottomright=new PagePoint(pageImage,coords.getX() +
			// coords.getWidth(), coords.getY() + coords.getHeight()); //
			// expected bottom right point

			// Se buscan las marcas de alineaciï¿½n
			AlignmentResult marcasalign=searchAlignMarks(campo, pageImage);

			Map<AlignmentPosition, PagePoint> detectedpoints=marcasalign.getDetected();
			Map<PagePoint, PagePoint> points=marcasalign.getPointPairs();
			Map<AlignmentPosition, PagePoint> expectedpoints=marcasalign.getExpected();
			/*
			 * se va a calcular la pendiente entre las esquinas superiores y
			 * entre las esquinas inferiores y se tomarï¿½ la media para
			 * calcular el ï¿½ngulo de rotaciï¿½n
			 */
			PagePoint dtopleft=detectedpoints.get(AlignmentPosition.TOPLEFT);
			PagePoint dtopright=detectedpoints.get(AlignmentPosition.TOPRIGHT);
			PagePoint dbottomleft=detectedpoints.get(AlignmentPosition.BOTTOMLEFT);
			PagePoint dbottomright=detectedpoints.get(AlignmentPosition.BOTTOMRIGHT);

			PagePoint etopleft=expectedpoints.get(AlignmentPosition.TOPLEFT);
			PagePoint etopright=expectedpoints.get(AlignmentPosition.TOPRIGHT);
			PagePoint ebottomleft=expectedpoints.get(AlignmentPosition.BOTTOMLEFT);
			PagePoint ebottomright=expectedpoints.get(AlignmentPosition.BOTTOMRIGHT);

			if (logger.isDebugEnabled())
			{
				for (Entry<PagePoint, PagePoint> pointpair : points.entrySet())
				{
					logger.debug("Alignment result. Expected:" + pointpair.getKey());
					logger.debug("                  detected:" + pointpair.getValue());
				}
			}

			
			// check corner angles to eliminate possible bad detections
			boolean topRightSquared=isSquared(dtopleft, dtopright, dbottomright);
			boolean topLeftSquared=isSquared(dbottomleft, dtopleft, dtopright);
			boolean bottomLeftSquared=isSquared(dtopleft, dbottomleft, dbottomright);
			boolean bottomRightSquared=isSquared(dbottomleft, dbottomright, dtopright);
			
			if (!topRightSquared) // 90degrees?
			{
				logger.debug("TopRight corner is not squared!");
				// calculate fourth
			}
			if (!topLeftSquared) // 90degrees?
			{
				logger.debug("TopLeft corner is not squared!");
				// calculate fourth
			}
			if (!bottomLeftSquared) // 90degrees?
			{
				logger.debug("BottomLeft corner is not squared!");
				// calculate fourth
			}
			if (!bottomRightSquared) // 90degrees?
			{
				logger.debug("BottomRight corner is not squared!");
				// calculate fourth using
			}
			
			// Use for calculations only  detected corners thar were squared
			PagePoint useForCalculations1, useForCalculations2; // first point upper-leftmost
			
			if (topLeftSquared && topRightSquared)
			{
				useForCalculations1=dtopleft;
				useForCalculations2=dtopright;
			}
			else
			if (topRightSquared && bottomRightSquared)
			{
				useForCalculations1=dtopright;
				useForCalculations2=dbottomright;
			} 
			else
			if (bottomRightSquared && bottomLeftSquared)
			{
				useForCalculations1=dbottomleft;
				useForCalculations2=dbottomright;
			}
			else
			if (topLeftSquared && bottomLeftSquared)
			{
				useForCalculations1=dtopleft;
				useForCalculations2=dbottomleft;
			}
			else
			{
				logger.warn("Not squared frame detected. Alignment may be wrong!");
				useForCalculations1=dtopleft;
				useForCalculations2=dtopright;
			}
			
			int deltaXpx=useForCalculations1.getYpx()-useForCalculations2.getYpx();
			int deltaYpx=useForCalculations1.getXpx()-useForCalculations2.getXpx();
			
			double pend_align;
			
			if (Math.abs(deltaYpx)>Math.abs(deltaXpx)) // vertical line
				{
				pend_align = ((double)deltaXpx)/deltaYpx;
				}
			else		// horizontal line
				{
				pend_align= ((double)deltaYpx)/deltaXpx;
				}
//			// angles
//			double pend_align_1=(dtopright != null && dtopleft != null) ? (dtopright.getY() - dtopleft.getY()) / (dtopright.getX() - dtopleft.getX())
//				: Double.NaN;
//			double pend_align_2=(dbottomleft != null && dbottomright != null) ? (dbottomright.getY() - dbottomleft.getY())
//				/ (dbottomright.getX() - dbottomleft.getX()) : Double.NaN;
//
//			if (pend_align_1 != Double.NaN && pend_align_2 != Double.NaN)
//			{
//				setAlignmentSlope((pend_align_1 + pend_align_2) / 2);
//			}
//			else if (pend_align_1 == Double.NaN && pend_align_2 != Double.NaN)
//			{
//				setAlignmentSlope(pend_align_2);
//			}
//			else if (pend_align_1 != Double.NaN && pend_align_2 == Double.NaN)
//			{
//				setAlignmentSlope(pend_align_1);
//			}
			
			setAlignmentSlope(pend_align);
			marcasalign.setAlignmentSlope(pend_align);
			// Se calcula la escala
			double dist_original_x;
			double dist_imagen_x;
			int dist_imagen_px_x;
			int dist_imagen_px_y;
			double dist_original_y;
			double dist_imagen_y;
			
			dist_original_x=etopright.distance(etopleft);
			dist_original_y=etopleft.distance(ebottomleft);
			int dist_original_px_y=etopleft.distancePx(ebottomleft);
			int dist_original_px_x=etopright.distancePx(etopleft);
			
			if (dtopleft != null && dtopright != null)
			{
				dist_imagen_x=dtopright.distance(dtopleft);
				dist_imagen_px_x=dtopright.distancePx(dtopleft);
			}
			else if (dbottomright != null && dbottomleft != null)
			{
				dist_imagen_x=dbottomright.distance(dbottomleft);
				dist_imagen_px_x=dbottomright.distancePx(dbottomleft);
			}
			else
			{
				logger.debug("Can't calculate new horizontal scale: TL=" + dtopleft + ", TR=" + dtopright + " , BL=" + dbottomleft + " , BR="
					+ dbottomright);
				dist_imagen_x=dist_original_x; // patch TODO
				dist_imagen_px_x=dist_original_px_x;
			}


			if (dtopleft != null && dbottomleft != null)
			{
				dist_imagen_y=dbottomleft.distance(dtopleft);
				dist_imagen_px_y=dbottomleft.distancePx(dtopleft);
			}
			else if (dbottomright != null && dtopright != null)
			{
				dist_imagen_y=dbottomright.distance(dtopright);
				dist_imagen_px_y = dbottomright.distancePx(dtopright);
			}
			else
			{
				logger.debug("Can't calculate new vertical scale: TL=" + dtopleft + ", TR=" + dtopright + " , BL=" + dbottomleft + " , BR="
					+ dbottomright);
				dist_imagen_y=dist_original_y; // patch TODO
				dist_imagen_px_y=dist_original_px_y;
			}
			// TODO calcular con pixeles observados es lo más realista.
			escala_x=((double)dist_original_px_x) / dist_imagen_px_x;
			escala_y=((double)dist_original_px_y) / dist_imagen_px_y;

//			// margen aceptable en el escalado
//			if (Math.abs(1 - escala_x) < 0.02)
//				escala_x=1;
//			if (Math.abs(1 - escala_y) < 0.02)
//				escala_y=1;

			/**
			 * Adjust the actual resolution
			 */
			double horizRes=dist_imagen_px_x / dist_imagen_x;
			double vertRes=dist_imagen_px_y / dist_imagen_y;
			marcasalign.setHorizontalResolution(horizRes);
			marcasalign.setVerticalResolution(vertRes);
			pageImage.setHorizontalResolution(horizRes);
			pageImage.setVerticalResolution(vertRes);

			// obtencion del centro de rotacion, se supone el centro del
			// marco de alineacion

			// diagonal
			if (dtopleft != null && dbottomright != null)
				{
				d_center_xmm=dtopleft.getX() + (dbottomright.getX() - dtopleft.getX()) / 2;
				d_center_ymm=dtopleft.getY() + (dbottomright.getY() - dtopleft.getY()) / 2;
				d_center_xpx=dtopleft.getXpx() + (dbottomright.getXpx() - dtopleft.getXpx()) / 2;
				d_center_ypx=dtopleft.getYpx() + (dbottomright.getYpx() - dtopleft.getYpx()) / 2;
				}
			else
			// other diagonal
			if (dbottomleft != null && dtopright != null)
				{
				d_center_xmm=dbottomleft.getX() + (dtopright.getX() - dtopleft.getX()) / 2;
				d_center_ymm=dbottomleft.getY() + (dtopright.getY() - dbottomleft.getY()) / 2;
				d_center_xpx=dbottomleft.getXpx() + (dtopright.getXpx() - dtopleft.getXpx()) / 2;
				d_center_ypx=dbottomleft.getYpx() + (dtopright.getYpx() - dbottomleft.getYpx()) / 2;
				}
			// else
			// // bottom side
			// if (dbottomleft!=null && dbottomright!=null)
			// center_xmm = dbottomleft.getX() +
			// (dbottomright.getX()-dbottomleft.getX())/2;
			// else
			// // top side
			// if (dtopleft!=null && dtopright!=null)
			// center_xmm = dtopleft.getX() +
			// (dtopright.getX()-dtopleft.getX())/2;
			else
			{
				throw new RuntimeException("FATAL: Not enough information available to calculate the center's X coordinate.");
			}

			angulo_rotacion=Math.atan(getAlignmentSlope());
			// translation of frame center
			double e_center_xmm=(etopright.getX() + etopleft.getX()) / 2;
			double e_center_ymm=(etopleft.getY() + ebottomleft.getY()) / 2;
			double e_center_xpx=e_center_xmm * pageImage.getHorizontalResolution();
			double e_center_ypx=e_center_ymm * pageImage.getVerticalResolution();

			double transX=e_center_xmm - d_center_xmm;
			double transY=e_center_ymm - d_center_ymm;
			PagePoint detectedCenter=new PagePoint(pageImage,  d_center_xpx, d_center_ypx);
			PagePoint expectedCenter=new PagePoint(pageImage,  e_center_xmm, e_center_ymm);
			marcasalign.setDetectedCenter(detectedCenter);
			marcasalign.setExpectedCenter(expectedCenter);
			marcasalign.setDisplacementDeltas(transX,transY);


			/**
			 * Log detected frame
			 */
			debugAlignMarkFrame(pageImage, dtopleft, dtopright, dbottomleft, dbottomright, Color.GREEN);
			debugAlignMarkFrame(pageImage, etopleft, etopright, ebottomleft, ebottomright, Color.BLUE);
			/**
			 * End Log
			 */
			// Rotaciï¿½n
			if (logger.isDebugEnabled())
			{
				logger.debug("align(OMRTemplate, PageImage)  - Detected rotation: " + angulo_rotacion * 180 / Math.PI + " degrees."
					+ "\n                               - Frame center expected:"+e_center_xpx+","+e_center_ypx+" px"
					+ "\n                               - Frame center detected:"+d_center_xpx+","+d_center_ypx+" px"
					+ "\n                               - Frame center delta x: " + transX + " mm."
					+ "\n                               - Frame center delta y: " + transY + " mm." 
					+ "\n                               - Scale x: "+ escala_x + " expected/actual" 
					+ "\n                               - Scale y: "+ escala_y + " expected/actual"
					+ "\n                               - Resolutions: " + horizRes + ", " + vertRes + " px/mm"); //$NON-NLS-1$
			}
			// TODO check this way of applying the transformation. Should reuse
			// and
			// modify original transform.
			// transform.scale(pageImage.getAllignmentInfo().getScaleX() /
			// escala_x, pageImage.getAllignmentInfo().getScaleY() / escala_y);
			AffineTransform transform=createTransform( marcasalign);

			marcasalign.setAlignmentTransform(transform);
			// transform.scale(escala_x, escala_y);

			pageImage.setAlignmentInfo(transform);
			

			logger.debug("\tImage alligned in (ms)" + (System.currentTimeMillis() - taskStart)); //$NON-NLS-1$
			return marcasalign;
		}
		else
			return null;
	}

	private AffineTransform createTransform( AlignmentResult marcasalign)
	{
		double angulo_rotacion=Math.atan(marcasalign.getAlignmentSlope());
		PagePoint detectedCenter=marcasalign.getDetectedCenter();
		PagePoint expectedCenter=marcasalign.getExpectedCenter();
		AffineTransform transform=new AffineTransform();
		


		transform.scale(marcasalign.getHorizontalResolution(), marcasalign.getVerticalResolution()); // to convert from mm to px
		transform.translate(detectedCenter.getX(), detectedCenter.getY());
		transform.rotate(angulo_rotacion);
		transform.translate(-expectedCenter.getX(), -expectedCenter.getY());

//		TODO move to test code
//		Point2D traslatedCenter=new Point.Double();
//		transform.transform(expectedCenter, traslatedCenter);
//		
//		Point2D traslated=new Point.Double();
//		PagePoint expectedTopLeft=marcasalign.getExpected().get(AlignmentPosition.TOPLEFT);
//		PagePoint detectedTopLeft=marcasalign.getDetected().get(AlignmentPosition.TOPLEFT);
//
//		transform.transform(expectedTopLeft, traslated);
		return transform;
	}

	/**
	 * @param point1
	 * @param corner
	 * @param point2
	 * @return
	 */
	private boolean isSquared(PagePoint point1, PagePoint corner, PagePoint point2)
	{
		if (point1==null || corner==null || point2==null)
			return false;
		double orthogonality=Math.abs(proyection(point1, corner, point2));
		return orthogonality < 0.02;
	}

	/**
	 * calcs orthogonality via scalar product.
	 * 
	 * @param point1
	 * @param corner
	 * @param point2
	 * @return
	 */
	private double proyection(PagePoint point1, PagePoint corner, PagePoint point2)
	{
		double v1x=point1.x - corner.x;
		double v1y=point1.y - corner.y;
		double v2x=point2.x - corner.x;
		double v2y=point2.y - corner.y;
		return (v1x * v2x + v1y * v2y)/Math.sqrt((v1x*v1x+v1y*v1y)*(v1x*v1x+v1y*v1y));
	}

	protected void debugAlignMarkFrame(PageImage pageImage, PagePoint topleft, PagePoint topright, PagePoint bottomleft, PagePoint bottomright, Color color)
	{
		if (topleft == null || topright == null || bottomleft == null || bottomright == null)
			return;
		Graphics g=pageImage.getReportingGraphics();
		g.setColor(color);

//		Point framePxUL=pageImage.toPixels(topleft.getX(), topleft.getY());
//		Point framePxUR=pageImage.toPixels(topright.getX(), topright.getY());
//		Point framePxBL=pageImage.toPixels(bottomleft.getX(), bottomleft.getY());
//		Point framePxBR=pageImage.toPixels(bottomright.getX(), bottomright.getY());

		g.drawLine(topleft.getXpx(), topleft.getYpx(), topright.getXpx(), topright.getYpx());
		g.drawLine(topleft.getXpx(), topleft.getYpx(), bottomleft.getXpx(), bottomleft.getXpx());
		g.drawLine(topright.getXpx(), topright.getYpx(), bottomright.getXpx(), bottomright.getYpx());
		g.drawLine(bottomleft.getXpx(), bottomleft.getYpx(), bottomright.getXpx(), bottomright.getYpx());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.uva.itast.blended.omr.pages.AlignMarkDetector#searchAlignMarks(org
	 * .uva.itast.blended.omr.Field, org.uva.itast.blended.omr.pages.PageImage)
	 */
	public AlignmentResult searchAlignMarks(Field campo, PageImage pageImage)
	{
		Rectangle2D coords=campo.getBBox();

		// puntos de alineamiento esperados
		PagePoint etopleft=new PagePoint(pageImage, coords.getX(), coords.getY()); // expected
																					// top
																					// left
																					// point
		PagePoint etopright=new PagePoint(pageImage, coords.getX() + coords.getWidth(), coords.getY()); // expected
																										// top
																										// right
																										// point
		PagePoint ebottomleft=new PagePoint(pageImage, coords.getX(), coords.getY() + coords.getHeight()); // expected
																											// bottom
																											// left
																											// point
		PagePoint ebottomright=new PagePoint(pageImage, coords.getX() + coords.getWidth(), coords.getY() + coords.getHeight()); // expected
																																// bottom
																																// right
																																// point

		PagePoint dtopleft=pointPosition(etopleft); // detected topleft point
		logger.debug("Point Top-Left supposed to be at: " + etopleft + "\n                        found at: " + dtopleft);

		PagePoint dtopright=pointPosition(etopright); // detected top
														// right point
		logger.debug("Point Top-Right supposed to be at: " + etopright + "\n                        found at: " + dtopright);

		PagePoint dbottomleft=pointPosition(ebottomleft); // detected
															// bottom
															// left
															// point
		PagePoint dbottomright=pointPosition(ebottomright); // detected
															// bottom
															// right
															// point

		AlignmentResult result=new AlignmentResult();
		result.addResult(AlignmentPosition.TOPLEFT, etopleft, dtopleft);
		result.addResult(AlignmentPosition.TOPRIGHT, etopright, dtopright);
		result.addResult(AlignmentPosition.BOTTOMLEFT, ebottomleft, dbottomleft);
		result.addResult(AlignmentPosition.BOTTOMRIGHT, ebottomright, dbottomright);
		return result;
	}

	/**
	 * 
	 * @param pageImage
	 * @param expectedPoint
	 *            aproximate position in milimeters (assumes that the scale is
	 *            correct)
	 * @return detected point in pixels Use
	 *         {@link PageImage#toMilimeters(int, int)} to obtain equivalence in
	 *         milimeters
	 */
	public abstract PagePoint pointPosition(PagePoint expectedPoint);

	/**
	 * @param pageImage
	 * @param expectedRect
	 *            bounding box in milimeters
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
				File outputDebug=new File(omrProcessor.getOutputdir() + "/output");
				if (!outputDebug.exists())
					outputDebug.mkdirs();
				File rasterImageFile=omrProcessor != null ? File.createTempFile("debugCorners", ".png", outputDebug) : File.createTempFile(
					"debugCorners", ".png");
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
	public Rectangle2D getExpectedRect(PagePoint expectedPoint)
	{
		Rectangle2D expectedRect=new Rectangle();

		expectedRect.setFrame(expectedPoint.getX() - getBufferWidth(), expectedPoint.getY() - getBufferWidth(), getBufferWidth() * 2,
			getBufferWidth() * 2); // medidas en mm
		return expectedRect;
	}

}