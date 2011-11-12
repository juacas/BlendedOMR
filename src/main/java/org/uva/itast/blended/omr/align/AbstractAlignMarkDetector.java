package org.uva.itast.blended.omr.align;

import java.awt.Color;
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
import org.uva.itast.blended.omr.OMRUtils;
import org.uva.itast.blended.omr.align.AlignmentResult.AlignmentPosition;
import org.uva.itast.blended.omr.pages.PageImage;
import org.uva.itast.blended.omr.pages.PagePoint;
import org.uva.itast.blended.omr.pages.SubImage;

public abstract class AbstractAlignMarkDetector implements AlignMarkDetector
{
	private static final int	DELTA_FOR_OUTSIDING	=5;

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
		int d_center_px_x=(int) (PageImage.a4width * pageImage.getHorizontalResolution() / 2);
		int d_center_px_y=(int) (PageImage.a4height * pageImage.getVerticalResolution() / 2);

		
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
			
			// Use for calculations only  detected corners that were squared
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
			else // some heuristics for last chance. Maybe opposite point is displaced!
			if (topLeftSquared)
			{
				useForCalculations1=dtopleft;
				useForCalculations2=dbottomleft;
				dbottomright= calculateThirdCorner(dbottomleft,dtopleft,dtopright);
			}
			else
			if (bottomLeftSquared)
			{
				useForCalculations1=dbottomleft;
				useForCalculations2=dbottomright;
				dtopright=calculateThirdCorner(dbottomright, dbottomleft, dtopleft);
			}
			else
			if (bottomRightSquared)
			{
				useForCalculations1=dbottomleft;
				useForCalculations2=dbottomright;
				dtopleft=calculateThirdCorner(dtopright, dbottomright, dbottomright);
			}
			else
			if (topRightSquared)
			{
				useForCalculations1=dtopleft;
				useForCalculations2=dtopright;
				dbottomleft=calculateThirdCorner(dtopleft, dtopright, dbottomright);
			}
			else
			{
				logger.warn("Not squared frame detected. Alignment may be wrong!");
				// use full image as frame. Some scanners crop the images this way
//				dtopleft=new PagePoint(pageImage, (int)5,(int)5);
//				dtopright=new PagePoint(pageImage, (int)pageImage.getImage().getWidth()-5,(int)5);
//				dbottomleft=new PagePoint(pageImage, (int)5,(int)pageImage.getImage().getHeight()-5);
//				dbottomright=new PagePoint(pageImage, (int)pageImage.getImage().getWidth()-5,(int)pageImage.getImage().getHeight()-5);
				
				// assume image is aligned
				dtopleft=etopleft;
				dtopright=etopright;
				dbottomleft=ebottomleft;
				dbottomright=ebottomright;
				
				useForCalculations1=dtopleft;
				useForCalculations2=dtopright;
			}
			
			/**
			 * Calculate the slope
			 */
			double pend_align=calculateSlope(useForCalculations1, useForCalculations2);

			
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
			
			
			escala_x=((double)dist_original_px_x) / dist_imagen_px_x;
			escala_y=((double)dist_original_px_y) / dist_imagen_px_y;
			
			/**
			 * Adjust the actual resolution
			 * NOTE that some values (i.e. detected points in milimeters are no longer valid) 
			 */
			double horizRes=dist_imagen_px_x / dist_original_x;
			double vertRes=dist_imagen_px_y / dist_original_y;
			marcasalign.setHorizontalResolution(horizRes);
			marcasalign.setVerticalResolution(vertRes);
			pageImage.setHorizontalResolution(horizRes);
			pageImage.setVerticalResolution(vertRes);

			// obtencion del centro de rotacion, se supone el centro del
			// marco de alineacion

			// diagonal
			if (dtopleft != null && dbottomright != null)
				{
//				d_center_xmm=dtopleft.getX() + (dbottomright.getX() - dtopleft.getX()) / 2;
//				d_center_ymm=dtopleft.getY() + (dbottomright.getY() - dtopleft.getY()) / 2;
				d_center_px_x=dtopleft.getXpx() + (dbottomright.getXpx() - dtopleft.getXpx()) / 2;
				d_center_px_y=dtopleft.getYpx() + (dbottomright.getYpx() - dtopleft.getYpx()) / 2;
				}
			else
			// other diagonal
			if (dbottomleft != null && dtopright != null)
				{
//				d_center_xmm=dbottomleft.getX() + (dtopright.getX() - dtopleft.getX()) / 2;
//				d_center_ymm=dbottomleft.getY() + (dtopright.getY() - dbottomleft.getY()) / 2;
				d_center_px_x=dbottomleft.getXpx() + (dtopright.getXpx() - dtopleft.getXpx()) / 2;
				d_center_px_y=dbottomleft.getYpx() + (dtopright.getYpx() - dbottomleft.getYpx()) / 2;
				}
			else
			{
				throw new RuntimeException("FATAL: Not enough information available to calculate the center's X coordinate.");
			}

			angulo_rotacion=Math.atan(getAlignmentSlope());
			// translation of frame center
			double e_center_x=etopleft.getX()+ (ebottomright.getX() - etopleft.getX()) / 2;
			double e_center_y=etopleft.getY()+ (ebottomright.getY() - etopleft.getY()) / 2;
			
//			double e_center_xpx=e_center_xmm * pageImage.getHorizontalResolution();
//			double e_center_ypx=e_center_ymm * pageImage.getVerticalResolution();

			
			PagePoint detectedCenter=new PagePoint(pageImage,  d_center_px_x, d_center_px_y);
			PagePoint expectedCenter=new PagePoint(pageImage,  e_center_x, e_center_y);
			
			marcasalign.setDetectedCenter(detectedCenter);
			marcasalign.setExpectedCenter(expectedCenter);
			int transXpx=detectedCenter.getXpx()-expectedCenter.getXpx();
			int transYpx=detectedCenter.getYpx()-expectedCenter.getYpx();
			
			PagePoint delta=new PagePoint(pageImage, (int)transXpx, (int)transYpx);
			marcasalign.setDisplacementDelta(delta);


			/**
			 * Log detected frame
			 */
			if (logger.isDebugEnabled())
			{
				OMRUtils.logFrame(pageImage, dtopleft, dtopright, dbottomleft, dbottomright, Color.GREEN, "Detected");
				OMRUtils.logFrame(pageImage, etopleft, etopright, ebottomleft, ebottomright, Color.BLUE, "Expected");
			}
			/**
			 * End Log
			 */
			// Rotaciï¿½n
			if (logger.isInfoEnabled())
			{
				logger.info("align(OMRTemplate, PageImage)  - Detected rotation: " + angulo_rotacion * 180 / Math.PI + " degrees."
					+ "\n                               - Frame topleft expected:"+etopleft
					+ "\n                               - Frame topleft detected:"+dtopleft
					+ "\n                               - Frame bottomright expected:"+ebottomright
					+ "\n                               - Frame bottomright detected:"+dbottomright
					+ "\n                               - Frame bottomleft expected:"+ebottomleft
					+ "\n                               - Frame bottomleft detected:"+dbottomleft
					+ "\n                               - Frame center expected:"+expectedCenter
					+ "\n                               - Frame center detected:"+detectedCenter
					+ "\n                               - Frame center delta:"+delta
					+ "\n                               - Frame center delta x: " + transXpx + "px."
					+ "\n                               - Frame center delta y: " + transYpx + "px." 
					+ "\n                               - Scale x: "+ escala_x + " expected/actual ("+dist_original_px_x+"/"+dist_imagen_px_x+")" 
					+ "\n                               - Scale y: "+ escala_y + " expected/actual ("+dist_original_px_y+"/"+dist_imagen_px_y+")"
					+ "\n                               - Resolutions Horiz: " + horizRes + "px/mm ("+ dist_imagen_px_x+"/"+ dist_original_x+")"
					+ "\n                               - Resolutions  Vert: " + vertRes + "px/mm ("+ dist_imagen_px_y+"/"+ dist_original_y+")");
			
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
			
			// debug transformation
			if (logger.isDebugEnabled())
					{
					logTransformedFrame(pageImage,marcasalign);
					
					PagePoint transCenter=new PagePoint(pageImage, expectedCenter.x, expectedCenter.y);
					logger.debug("Detected center:"+detectedCenter);
					logger.debug("Transformed center:"+transCenter);
					}
			if(logger.isInfoEnabled())
				{
				logger.info("\tImage alligned in (ms)" + (System.currentTimeMillis() - taskStart)); //$NON-NLS-1$
				}

			
			return marcasalign;
		}
		else
			return null;
	}
	
public static void logTransformedFrame(PageImage pageImage, AlignmentResult marcasalign)
	{
	PagePoint etopleft=marcasalign.getExpected().get(AlignmentPosition.TOPLEFT);
	PagePoint etopright=marcasalign.getExpected().get(AlignmentPosition.TOPRIGHT);
	PagePoint ebottomleft=marcasalign.getExpected().get(AlignmentPosition.BOTTOMLEFT);
	PagePoint ebottomright=marcasalign.getExpected().get(AlignmentPosition.BOTTOMRIGHT);
	PagePoint topLeft=new PagePoint(pageImage, etopleft.x, etopleft.y);
	PagePoint topRight=new PagePoint(pageImage, etopright.x, etopright.y);
	PagePoint bottomLeft=new PagePoint(pageImage, ebottomleft.x, ebottomleft.y);
	PagePoint bottomRight=new PagePoint(pageImage, ebottomright.x, ebottomright.y);
	
	OMRUtils.logFrame(pageImage, topLeft, topRight, bottomLeft, bottomRight, Color.RED,"Transformed Frame");
	}

/**
 * calculates the third corner of the rectangle in pixels
 * @param dbottomleft
 * @param dtopleft
 * @param dtopright
 * @return
 */
	private PagePoint calculateThirdCorner(PagePoint dbottomleft, PagePoint dtopleft, PagePoint dtopright)
	{
		int x;
		int y;
		x= dbottomleft.getXpx() + dtopright.getXpx()-dtopleft.getXpx();
		y= dbottomleft.getYpx() + dtopright.getYpx()-dtopleft.getYpx();
		return new PagePoint(dtopleft.getPageImage(),(int)x,(int)y);
	}

	/**
	 * @param useForCalculations1
	 * @param useForCalculations2
	 * @return ratio between increments in x coords and y coords, reduced to first quadrant
	 */
	private double calculateSlope(PagePoint useForCalculations1, PagePoint useForCalculations2)
	{
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
		return pend_align;
	}

	private AffineTransform createTransform( AlignmentResult marcasalign)
	{
		double angulo_rotacion=Math.atan(marcasalign.getAlignmentSlope());
		PagePoint detectedCenter=marcasalign.getDetectedCenter();
		PagePoint expectedCenter=marcasalign.getExpectedCenter();
		AffineTransform transform=new AffineTransform();
		
		transform.translate(detectedCenter.getXpx(), detectedCenter.getYpx());
		transform.scale(marcasalign.getHorizontalResolution(), marcasalign.getVerticalResolution()); // to convert from mm to px
		transform.rotate(angulo_rotacion);
		transform.translate(-expectedCenter.getX(), -expectedCenter.getY());

//		TODO move to test code
//		Point2D traslatedCenter=new Point2D.Double();
//		transform.transform(expectedCenter, traslatedCenter);
//		
//		Point2D traslated=new Point2D.Double();
//		PagePoint expectedTopLeft=marcasalign.getExpected().get(AlignmentPosition.TOPLEFT);
//		PagePoint detectedTopLeft=marcasalign.getDetected().get(AlignmentPosition.TOPLEFT);
//
//		transform.transform(expectedTopLeft, traslated);
//		
//		PagePoint dTopLeft=marcasalign.getDetected().get(AlignmentPosition.TOPLEFT);
//		PagePoint eTopLeft=marcasalign.getExpected().get(AlignmentPosition.TOPLEFT);
//		
//		// expected mm coords should be traslated to detected px coords
//		
//		Point2D ptDst = new Point2D.Double();
//		transform.transform(eTopLeft, ptDst);
//		
//		Point2D res2=new Point2D.Double();
//		Point2D origin = new Point2D.Double(0,0);
//		transform.transform(origin, res2);
		
	
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
		double orthogonality=Math.abs(vectorProjection(point1, corner, point2));
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
	private double vectorProjection(PagePoint point1, PagePoint corner, PagePoint point2)
	{
		double v1x=point1.x - corner.x;
		double v1y=point1.y - corner.y;
		double v2x=point2.x - corner.x;
		double v2y=point2.y - corner.y;
		return (v1x * v2x + v1y * v2y)/Math.sqrt((v1x*v1x+v1y*v1y)*(v1x*v1x+v1y*v1y));
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

		// puntos de alineamiento esperados expected topleft point
		PagePoint etopleft=new PagePoint(pageImage, coords.getX(), coords.getY());
		PagePoint etopright=new PagePoint(pageImage, coords.getX() + coords.getWidth(), coords.getY()); // expected  top  right point
		PagePoint ebottomleft=new PagePoint(pageImage, coords.getX(), coords.getY() + coords.getHeight()); // expected  bottom left  point
		PagePoint ebottomright=new PagePoint(pageImage, coords.getX() + coords.getWidth(), coords.getY() + coords.getHeight()); // expected  bottom right  point
		PagePoint dtopleft=null,dtopright=null,dbottomleft=null,dbottomright=null;
		
		
		
		// search in the corner
//		PagePoint searchIn=new PagePoint(pageImage,	etopleft.getXpx()-DELTA_FOR_OUTSIDING,	etopleft.getYpx()-DELTA_FOR_OUTSIDING);
//		dtopleft=pointPosition(searchIn); // detected topleft point
		if (dtopleft==null)
			dtopleft=pointPosition(etopleft); // detected topleft point

//		searchIn=new PagePoint(pageImage,etopright.getXpx()+DELTA_FOR_OUTSIDING,etopright.getYpx()-DELTA_FOR_OUTSIDING);
//		dtopright=pointPosition(searchIn);
		if (dtopright==null)
		 dtopright=pointPosition(etopright); // detected top right point
		
//		searchIn=new PagePoint(pageImage,ebottomleft.getXpx()-DELTA_FOR_OUTSIDING,ebottomleft.getYpx()-DELTA_FOR_OUTSIDING);
//		dbottomleft=pointPosition(searchIn);
		if (dbottomleft==null)
		 dbottomleft=pointPosition(ebottomleft); // detected  bottom  left point
//		searchIn=new PagePoint(pageImage,ebottomright.getXpx()+DELTA_FOR_OUTSIDING,ebottomright.getYpx()+DELTA_FOR_OUTSIDING);
//		dbottomright=pointPosition(searchIn);
		if (dbottomright==null)
		 dbottomright=pointPosition(ebottomright); // detected bottom  right  point

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
	 * @return detected {@link PagePoint} initialized in pixels Use
	 *         {@link PagePoint#getX()} to obtain equivalence in
	 *         milimeters and {@link PagePoint#getXpx()} in pixels
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