/*
 *
 */

package org.uva.itast.blended.omr.scanners;


import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.uva.itast.blended.omr.BufferedImageUtil;
import org.uva.itast.blended.omr.Field;
import org.uva.itast.blended.omr.UtilidadesFicheros;
import org.uva.itast.blended.omr.pages.PageImage;
import org.uva.itast.blended.omr.pages.SubImage;


/**
 * @author Juan Pablo de Castro
 */
public class SolidCircleMarkScanner extends MarkScanner{
	/**
	 * 
	 */
	private static final double SIMILARITY_PERCENT = 0.5d;

	/**
	 * 
	 */
	private static final int SCAN_DELTA_DIVISOR = 4;
	/**
	 *  porcentaje del radio para el barrido que se da para buscar la marca
	 */
	private static final double SCAN_PERCENT = 0.2d;
	/**
	 * Logger for this class
	 */
	private static final Log logger = LogFactory.getLog(SolidCircleMarkScanner.class);

	
	public double markWidth;
	public double markHeight;
	double approxXscale;

	double	approxYscale;
	// Gray8Image template;
	BufferedImage template;

	
	double maxsim;
	int maxsimX, maxsimY;

	

	private double autoSimilarity;

	private boolean	dump=false;

	/**
	 * 
	 * @param grayimage
	 * @param markWidth          in milimeters
	 * @param markHeight         in milimeters
	 * @param approxXscale
	 * @param approxYscale
	 */
	public SolidCircleMarkScanner(PageImage pageimage, double markWidth, double markHeight, boolean medianfilter) 
	{
		super(pageimage,medianfilter);

		
		this.approxXscale = pageimage.getPreferredHorizontalResolution();
		this.approxYscale = pageimage.getPreferredVerticalResolution();
		Rectangle2D area=new Rectangle();
		area.setFrame(0, 0, markWidth, markHeight);
		Rectangle dims= pageimage.toPixels(area);
		
		template = new BufferedImage((
				int) (dims.width * 1.15) + 1,
				(int) (dims.height * 1.15) + 1,
				BufferedImage.TYPE_BYTE_BINARY);
		
		fillTemplate(template, dims.width / 2, approxXscale / approxYscale);
		
		this.markHeight = markWidth;
		this.markWidth = markHeight;
	}

	// private void fillTemplate(Gray8Image templateimg, int markradX,
	// double aspect)
	private void fillTemplate(BufferedImage templateimg, int markradX,
			double aspect) {
		double centerX = templateimg.getWidth() / 2;
		double centerY = templateimg.getHeight() / 2;
		int whites = 0;
		for (int i = 0; i < templateimg.getWidth(); i++) {
			for (int j = 0; j < templateimg.getHeight(); j++) {
				double dist = Math.sqrt((i - centerX) * (i - centerX)
						+ (j - centerY) / aspect * (j - centerY) / aspect);
				if (dist <= markradX) {
					// templateimg.putBlack(i, j);
					templateimg.setRGB(i, j, Color.BLACK.getRGB());

				} else {
					// templateimg.putWhite(i, j);
					templateimg.setRGB(i, j, Color.WHITE.getRGB());
					whites++;
				}
			}
		}

		// compute autoSimilarity
		autoSimilarity = ((float) whites)
				/ (templateimg.getHeight() * templateimg.getWidth());
	}



	/**
	 * When comparing with an empty space there are a minimum similarity due to
	 * empty space around the mark
	 * 
	 * @return
	 */
	private double getAutoSimilarity() {

		return autoSimilarity;
	}

	/**
	 * @param x in pixels
	 * @param y in pixels
	 */
	private void markPointInImage(int x, int y) {
		Graphics2D g = pageImage.getReportingGraphics();
		AffineTransform t=g.getTransform();
		g.setColor(Color.WHITE);
		g.fillOval((int)(x - 1/t.getScaleX()),(int)( y - 1/t.getScaleY()), (int)(2/t.getScaleX()), (int)(2/t.getScaleY()));
		// g.drawRect(i-w/2-1, j-h/2-1, w, h);
		g.setColor(Color.BLACK);
		g.drawOval((int)(x - 1/t.getScaleX()), (int)(y - 1/t.getScaleY()), (int)(2/t.getScaleX()), (int)(2/t.getScaleY()));
		// g.drawRect(i-w/2, j-h/2, w, h);

	}

	
	/**
	 * @param pageImage
	 */
	public void putCircleMarkOnImage(PageImage pageImage) {
		
		Graphics2D g = pageImage.getReportingGraphics();
		// int centerColor=imagen.getRGB(maxsimX, maxsimY);
		// g.setXORMode(new Color(centerColor));
		// g.setColor(Color.RED);
		// g.fillOval(maxsimX - markWidth/2, maxsimY - markHeight/2, markWidth,
		// markHeight);
		// g.setPaintMode();
		Point markDimsPx=pageImage.toPixels(markWidth,markHeight);
		int markWidth=markDimsPx.x;
		int markHeight=markDimsPx.y;
		g.setColor(Color.RED);
		AffineTransform t=g.getTransform();
		g.drawLine(maxsimX, maxsimY - markHeight / 2 - 1, maxsimX, maxsimY
				- markHeight / 2 - (int)(20/t.getScaleY()));
		Polygon arrowHead = new Polygon();
		arrowHead.addPoint(maxsimX, (int) (maxsimY - markHeight / 2 - 1/t.getScaleY()));
		arrowHead.addPoint((int)(maxsimX - 6/t.getScaleX()),(int)( maxsimY - markHeight / 2 - 10/t.getScaleY()));
		arrowHead.addPoint((int)(maxsimX + 6/t.getScaleX()), (int)(maxsimY - markHeight / 2 - 10/t.getScaleY()));
		g.fillPolygon(arrowHead);
		
		g.setStroke(new BasicStroke(2,BasicStroke.CAP_SQUARE,BasicStroke.JOIN_ROUND,1,new float[]{(float) (3/t.getScaleX()),(float) (4/t.getScaleY())},0));
		g.drawOval(
				maxsimX - markWidth / 2 - 1,
				maxsimY - markHeight / 2 - 1,
				markWidth + 1, markHeight + 1);

	}

	/**
	 * @param markCenter position in milimeters relative to actual page
	 * @param b
	 * @return
	 */
	protected boolean isMark(Rectangle2D markArea, boolean dump)
	{
		maxsim = -1;
		maxsimX = 0;
		maxsimY = 0;
		
		
		// [JPC] this loop was refactored to start the analysis from the center
		
	
		//Gets a subimage from  x-maxDeltaX-template.getWidth(),y-maxDeltaY ->  x+maxDeltaY,y+maxDeltaY
		// stores the offsetX and offsetY to use original images's coordinates
		Rectangle2D unexpandedArea=markArea;
		markArea=getExpandedArea(markArea);
		
		long start=System.currentTimeMillis();
		SubImage subImage=this.pageImage.getSubimage(markArea, BufferedImage.TYPE_INT_RGB);
		logger.debug("isMark(Point2D, boolean) - Subimage extracted in - (ms)=" + (System.currentTimeMillis()-start)); //$NON-NLS-1$
		BufferedImage img=subImage;
		
		if (logger.isDebugEnabled())
			UtilidadesFicheros.logSubImage(subImage);
		
		if(medianfilter == true)
		 {
			start=System.currentTimeMillis();
			img= medianFilter(subImage);
			logger.debug("scanAreaForBarcode(MedianFilter area=" + subImage.getWidth()+"x"+subImage.getHeight() + ") In (ms) "+(System.currentTimeMillis()-start)); //$NON-NLS-1$ //$NON-NLS-2$
			 
			 if (logger.isDebugEnabled())
				 UtilidadesFicheros.logSubImage("debug_median",img);
		 }
		
		
		
		// Start processing in pixels
		Point2D markCenter=new Point();
		markCenter.setLocation(markArea.getCenterX(),markArea.getCenterY());
		
		int templateWidth=template.getWidth();
		int templateHeight=template.getHeight();
		Point markCenterPx=pageImage.toPixels(markCenter);
		Rectangle markAreaPx=pageImage.toPixels(markArea);
		Rectangle markUnexpandedAreaPx=pageImage.toPixels(unexpandedArea);
		int offsetX=markAreaPx.x;
		int offsetY=markAreaPx.y;
		
		double maxDeltaX = markWidth * SCAN_PERCENT;
		double maxDeltaY = markHeight * SCAN_PERCENT;
				
		Point maxDelta=pageImage.toPixels(maxDeltaX,maxDeltaY);
//		int maxDeltaXpx = maxDelta.x;
//		int maxDeltaYpx = maxDelta.y;
		int maxDeltaXpx = (int) (markAreaPx.width*SCAN_PERCENT);
		int maxDeltaYpx = (int) (markAreaPx.height*SCAN_PERCENT);
		
		int deltaXYpx = Math.max(1, markUnexpandedAreaPx.width/SCAN_DELTA_DIVISOR);
		
		boolean markpoint = true;// for debugging the position of the templates.
		start=System.currentTimeMillis();
		for (int xTemplate = markCenterPx.x; xTemplate <= markCenterPx.x + maxDeltaXpx; xTemplate += deltaXYpx)
		{

			for (int yTemplate = markCenterPx.y; yTemplate <= markCenterPx.y + maxDeltaYpx; yTemplate += deltaXYpx)
			{
	
			double similarity = 1.0 - 
				BufferedImageUtil.templateXOR(
						img, 
						(xTemplate  - templateWidth / 2 -offsetX), 
						(yTemplate  - templateHeight / 2 -offsetY), 
						template, dump);
				if (markpoint)
					markPointInImage(xTemplate, yTemplate);
				
				if (maxsim == -1 || maxsim < similarity)
				{
					maxsim = similarity;
					maxsimX = xTemplate;
					maxsimY = yTemplate;
				}
			
				similarity = 1.0 - 
					BufferedImageUtil.templateXOR(
						img, 
						2 *  markCenterPx.x - xTemplate - templateWidth / 2 -offsetX, 
						2 *  markCenterPx.y - yTemplate - templateHeight / 2 -offsetY,
						template, dump);
				if (markpoint)
					markPointInImage(2 *  markCenterPx.x - xTemplate,
									2 *  markCenterPx.y - yTemplate);
				if (maxsim == -1 || maxsim < similarity)
				{
					maxsim = similarity;
					maxsimX = 2 *  markCenterPx.x - xTemplate;
					maxsimY = 2 *  markCenterPx.y - yTemplate;
				}

			
				similarity = 1.0 - 
					BufferedImageUtil.templateXOR(
							img, 
							xTemplate - templateWidth / 2 -offsetX,
							2 *  markCenterPx.y - yTemplate- templateHeight / 2-offsetY,
							template, dump);
				if (markpoint)
					markPointInImage(xTemplate, 2 *  markCenterPx.y - yTemplate);
				if (maxsim == -1 || maxsim < similarity)
				{
					maxsim = similarity;
					maxsimX = xTemplate;
					maxsimY = 2 *  markCenterPx.y - yTemplate;
				}

				
				similarity = 1.0 - BufferedImageUtil.templateXOR(
						img, 
						2 *  markCenterPx.x - xTemplate - templateWidth / 2 -offsetX,
						yTemplate - templateHeight/ 2-offsetY,
						template, dump);

				if (markpoint)
					markPointInImage(2 *  markCenterPx.x - xTemplate, yTemplate);
				if (maxsim == -1 || maxsim < similarity)
				{
					maxsim = similarity;
					maxsimX = 2 *  markCenterPx.x - xTemplate;
					maxsimY = yTemplate;
				}
			}
		}
		double threshold = getAutoSimilarity() * (1 + SIMILARITY_PERCENT);
		if (logger.isDebugEnabled())
		{
			logger.debug("isMark(int, int)-->(ms)"+(System.currentTimeMillis()-start)+" Simil:" + maxsim + " (threshold)" + threshold + ":" + maxsimX + "," + maxsimY + " supposed to be at->" + markCenterPx ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
		}
		if (maxsim > threshold)
			return true;
		else
			return false;

	}

	/* (non-Javadoc)
	 * @see org.uva.itast.blended.omr.scanners.MarkScanner#getExpandedArea(java.awt.geom.Rectangle2D)
	 */
	@Override
	protected Rectangle2D getExpandedArea(Rectangle2D rect)
	{
		Rectangle templateRectPx=new Rectangle(0,0,template.getWidth(),template.getHeight());
		Rectangle2D templateArea = pageImage.toMilimeters(templateRectPx);
		
		double maxDeltaX = markWidth * SCAN_PERCENT;
		double maxDeltaY = markHeight * SCAN_PERCENT;
		double marginX = maxDeltaX+templateArea.getWidth();
		double marginY = maxDeltaY+templateArea.getHeight();
		double startX = rect.getCenterX()- marginX;
		double startY = rect.getCenterY()- marginY;
		
		Rectangle2D markArea=new Rectangle();
		markArea.setFrame(startX, startY, marginX*2, marginY*2);
		return markArea;
	}

	/* (non-Javadoc)
	 * @see org.uva.itast.blended.omr.scanners.MarkScanner#getParsedCode(org.uva.itast.blended.omr.Field)
	 */
	@Override
	public String getParsedCode(Field campo) throws MarkScannerException
	{
		
		return ((Boolean)scanField(campo).getResult()).toString();
	}

	/* (non-Javadoc)
	 * @see org.uva.itast.blended.omr.scanners.MarkScanner#scanAreaForFieldData(java.awt.geom.Rectangle2D)
	 */
	@Override
	public ScanResult scanAreaForFieldData(Rectangle2D coords)
			throws MarkScannerException
	{
		boolean result=isMark(coords, dump);
		ScanResult res=new ScanResult("SolidCircle",new Boolean(result));
		return res;
	}

	
}