/*
 *
 */

package org.uva.itast.blended.omr;


import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.uva.itast.blended.omr.pages.PageImage;
import org.uva.itast.blended.omr.pages.SubImage;


/**
 * @author Aaditeshwar Seth
 */
public class SolidCircleMark {
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
	private static final Log logger = LogFactory.getLog(SolidCircleMark.class);

	
	public double markWidth;
	public double markHeight;
	double approxXscale;

	double	approxYscale;
	// Gray8Image template;
	BufferedImage template;

	
	double maxsim;
	int maxsimX, maxsimY;

	private PageImage pageImage;

	private double autoSimilarity;

	/**
	 * 
	 * @param grayimage
	 * @param markWidth          in milimeters
	 * @param markHeight         in milimeters
	 * @param approxXscale
	 * @param approxYscale
	 */
	public SolidCircleMark(PageImage pageimage, double markWidth, double markHeight) {
		

		this.pageImage = pageimage;

		this.approxXscale = pageimage.getPreferredHorizontalResolution();
		this.approxYscale = pageimage.getPreferredVerticalResolution();
		
		Point dims= pageimage.toPixels(markWidth, markHeight);
		
		template = new BufferedImage((
				int) (dims.x * 1.15) + 1,
				(int) (dims.y * 1.15) + 1,
				BufferedImage.TYPE_BYTE_BINARY);
		
		fillTemplate(template, dims.x / 2, approxXscale / approxYscale);
		
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
	 * 
	 * @param x
	 *            coords of the CENTER of the mark
	 * @param y
	 * @param dump
	 *            enable the dumping of pattern comparisons
	 * @return
	 */
//	public boolean isMark(int x, int y, boolean dump) 
//	{
//		maxsim = -1;
//		maxsimX = 0;
//		maxsimY = 0;
//		
//		
//		// [JPC] this loop was refactored to start the analysis from the center
//		int maxDeltaX = (int) (markWidth * SCAN_PERCENT);
//		int maxDeltaY = (int) (markHeight * SCAN_PERCENT);
//		int deltaXY = Math.max(1, markWidth / SCAN_DELTA_DIVISOR);
//		
//		//Gets a subimage from  x-maxDeltaX-template.getWidth(),y-maxDeltaY ->  x+maxDeltaY,y+maxDeltaY
//		// stores the offsetX and offsetY to use original images's coordinates
//		
//		int templateWidth = template.getWidth();
//		int templateHeight = template.getHeight();
//		int offsetX=x-maxDeltaX-templateWidth;
//		int offsetY=y-maxDeltaY-templateHeight;
//		
//		SubImage subImage=this.pageImage.getSubimage(offsetX, offsetY, maxDeltaX*2, maxDeltaY*2, BufferedImage.TYPE_BYTE_GRAY);
//		
//		boolean markpoint = true;// for debugging the position of the templates.
//		for (int xTemplate = x; xTemplate <= x + maxDeltaX; xTemplate += deltaXY)
//		{
//
//			for (int yTemplate = y; yTemplate <= y + maxDeltaY; yTemplate += deltaXY)
//			{
//	
//			double similarity = 1.0 - 
//				BufferedImageUtil.templateXOR(subImage, 
//						xTemplate  - templateWidth / 2 -offsetX, 
//						yTemplate  - templateHeight / 2 -offsetY, 
//						template, dump);
//				if (markpoint)
//					markPointInImage(xTemplate, yTemplate);
//				
//				if (maxsim == -1 || maxsim < similarity)
//				{
//					maxsim = similarity;
//					maxsimX = xTemplate;
//					maxsimY = yTemplate;// + markradY * 2; // XXX
//				}
//				// similarity = 1.0 - ConcentricCircle.templateXOR(grayimage,
//				similarity = 1.0 - 
//					BufferedImageUtil.templateXOR(subImage, 
//						2 * x - xTemplate - templateWidth / 2 -offsetX, 
//						2 * y - yTemplate - templateHeight / 2 -offsetY,
//						template, dump);
//				if (markpoint)
//					markPointInImage(2 * x - xTemplate, 2 * y - yTemplate);
//				if (maxsim == -1 || maxsim < similarity)
//				{
//					maxsim = similarity;
//					maxsimX = 2 * x - xTemplate;
//					maxsimY = 2 * y - yTemplate;// + markradY * 2; // XXX
//				}
//
//				// similarity = 1.0 - ConcentricCircle.templateXOR(grayimage,
//				similarity = 1.0 - 
//					BufferedImageUtil.templateXOR(subImage, 
//							xTemplate - templateWidth / 2 -offsetX,
//							2 * y - yTemplate- templateHeight / 2-offsetY,
//							template, dump);
//				if (markpoint)
//					markPointInImage(xTemplate, 2 * y - yTemplate);
//				if (maxsim == -1 || maxsim < similarity)
//				{
//					maxsim = similarity;
//					maxsimX = xTemplate;
//					maxsimY = 2 * y - yTemplate;// + markradY * 2; // XXX
//				}
//
//				// similarity = 1.0 - ConcentricCircle.templateXOR(grayimage,
//				similarity = 1.0 - BufferedImageUtil.templateXOR(subImage, 
//						2 * x - xTemplate - templateWidth / 2 -offsetX,
//						yTemplate - templateHeight/ 2-offsetY,
//						template, dump);
//
//				if (markpoint)
//					markPointInImage(2 * x - xTemplate, yTemplate);
//				if (maxsim == -1 || maxsim < similarity)
//				{
//					maxsim = similarity;
//					maxsimX = 2 * x - xTemplate;
//					maxsimY = yTemplate;
//				}
//			}
//		}
//		double threshold = getAutoSimilarity() * (1 + SIMILARITY_PERCENT);
//		if (logger.isDebugEnabled())
//		{
//			logger.debug("isMark(int, int) - --" + maxsim + " (threshold)" + threshold + ":" + maxsimX + "," + maxsimY + "->" + x + ":" + y); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
//		}
//		if (maxsim > threshold)
//			return true;
//		else
//			return false;
//
//	}

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
		Graphics g = pageImage.getReportingGraphics();
		g.setColor(Color.WHITE);
		g.fillOval(x - 1, y - 1, 2, 2);
		// g.drawRect(i-w/2-1, j-h/2-1, w, h);
		g.setColor(Color.BLACK);
		g.drawOval(x - 1, y - 1, 2, 2);
		// g.drawRect(i-w/2, j-h/2, w, h);

	}

	// public void putMarkOnImage(Gray8Image markedImage)
	public void putMarkOnImage(BufferedImage markedImage) {
		BufferedImageUtil.putMarkBufferedImage(markedImage, maxsimX, maxsimY, true);
		BufferedImageUtil.putMarkBufferedImage(markedImage, maxsimX + 3, maxsimY + 3,
				true);
		BufferedImageUtil.putMarkBufferedImage(markedImage, maxsimX - 3, maxsimY + 3,
				true);
		BufferedImageUtil.putMarkBufferedImage(markedImage, maxsimX + 3, maxsimY - 3,
				true);
		BufferedImageUtil.putMarkBufferedImage(markedImage, maxsimX - 3, maxsimY - 3,
				true);
	}

	/**
	 * @param pageImage
	 */
	public void putCircleMarkOnImage(PageImage pageImage) {
		
		Graphics g = pageImage.getReportingGraphics();
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
		g.drawOval(
				maxsimX - markWidth / 2 - 1,
				maxsimY - markHeight / 2 - 1,
				markWidth + 1, markHeight + 1);
		g.drawLine(maxsimX, maxsimY - markHeight / 2 - 1, maxsimX, maxsimY
				- markHeight / 2 - 20);
		Polygon arrowHead = new Polygon();
		arrowHead.addPoint(maxsimX, maxsimY - markHeight / 2 - 1);
		arrowHead.addPoint(maxsimX - 6, maxsimY - markHeight / 2 - 10);
		arrowHead.addPoint(maxsimX + 6, maxsimY - markHeight / 2 - 10);
		g.fillPolygon(arrowHead);

	}

	/**
	 * @param markCenter position in milimeters relative to actual page
	 * @param b
	 * @return
	 */
	public boolean isMark(Point2D markCenter, boolean dump)
	{
		maxsim = -1;
		maxsimX = 0;
		maxsimY = 0;
		
		
		// [JPC] this loop was refactored to start the analysis from the center
		
		double maxDeltaX = markWidth * SCAN_PERCENT;
		double maxDeltaY = markHeight * SCAN_PERCENT;
		
		
		//Gets a subimage from  x-maxDeltaX-template.getWidth(),y-maxDeltaY ->  x+maxDeltaY,y+maxDeltaY
		// stores the offsetX and offsetY to use original images's coordinates
		
		Point2D templateDim = pageImage.toMilimeters(template.getWidth(),  template.getHeight());
		
		double marginX = maxDeltaX+templateDim.getX();
		double marginY = maxDeltaY+templateDim.getY();
		double startX = markCenter.getX()- marginX;
		double startY = markCenter.getY()- marginY;
		
		Rectangle2D markArea=new Rectangle();
		markArea.setFrame(startX, startY, marginX*2, marginY*2);
		long start=System.currentTimeMillis();
		SubImage subImage=this.pageImage.getSubimage(markArea, BufferedImage.TYPE_INT_RGB);
		logger.debug("isMark(Point2D, boolean) - Subimage extracted in - (ms)=" + (System.currentTimeMillis()-start)); //$NON-NLS-1$
		
		if (logger.isDebugEnabled()&&false)
			UtilidadesFicheros.logSubImage(subImage);
		
		// Start processing in pixels
		
		int templateWidth=template.getWidth();
		int templateHeight=template.getHeight();
		Point markCenterPx=pageImage.toPixels(markCenter);
		Rectangle markAreaPx=pageImage.toPixels(markArea);
		int offsetX=markAreaPx.x;
		int offsetY=markAreaPx.y;
		
		Point maxDelta=pageImage.toPixels(maxDeltaX,maxDeltaY);
		int maxDeltaXpx = maxDelta.x;
		int maxDeltaYpx = maxDelta.y;
		int deltaXYpx = pageImage.toPixels(Math.max(1, markWidth / SCAN_DELTA_DIVISOR), 0).x;
		
		boolean markpoint = true;// for debugging the position of the templates.
		start=System.currentTimeMillis();
		for (int xTemplate = markCenterPx.x; xTemplate <= markCenterPx.x + maxDeltaXpx; xTemplate += deltaXYpx)
		{

			for (int yTemplate = markCenterPx.y; yTemplate <= markCenterPx.y + maxDeltaYpx; yTemplate += deltaXYpx)
			{
	
			double similarity = 1.0 - 
				BufferedImageUtil.templateXOR(
						subImage, 
						(xTemplate  - templateWidth / 2 -offsetX), 
						(yTemplate  - templateHeight / 2 -offsetY), 
						template, dump);
				if (markpoint)
					markPointInImage(xTemplate, yTemplate);
				
				if (maxsim == -1 || maxsim < similarity)
				{
					maxsim = similarity;
					maxsimX = xTemplate;
					maxsimY = yTemplate;// + markradY * 2; // XXX
				}
				// similarity = 1.0 - ConcentricCircle.templateXOR(grayimage,
				similarity = 1.0 - 
					BufferedImageUtil.templateXOR(subImage, 
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
					maxsimY = 2 *  markCenterPx.y - yTemplate;// + markradY * 2; // XXX
				}

				// similarity = 1.0 - ConcentricCircle.templateXOR(grayimage,
				similarity = 1.0 - 
					BufferedImageUtil.templateXOR(subImage, 
							xTemplate - templateWidth / 2 -offsetX,
							2 *  markCenterPx.y - yTemplate- templateHeight / 2-offsetY,
							template, dump);
				if (markpoint)
					markPointInImage(xTemplate, 2 *  markCenterPx.y - yTemplate);
				if (maxsim == -1 || maxsim < similarity)
				{
					maxsim = similarity;
					maxsimX = xTemplate;
					maxsimY = 2 *  markCenterPx.y - yTemplate;// + markradY * 2; // XXX
				}

				// similarity = 1.0 - ConcentricCircle.templateXOR(grayimage,
				similarity = 1.0 - BufferedImageUtil.templateXOR(subImage, 
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

	
}