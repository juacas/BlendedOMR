package org.uva.itast.blended.omr.scanners;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.PixelGrabber;

import javax.swing.text.html.InlineView;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.uva.itast.blended.omr.BufferedImageUtil;
import org.uva.itast.blended.omr.OMRProcessor;
import org.uva.itast.blended.omr.OMRTemplate;
import org.uva.itast.blended.omr.pages.AbstractAlignMarkDetector;
import org.uva.itast.blended.omr.pages.PageImage;
import org.uva.itast.blended.omr.pages.SubImage;

public class AlignMarkHoughDetector extends AbstractAlignMarkDetector
{

	
	private static Log logger = LogFactory.getLog(AlignMarkHoughDetector.class);
	
	
	public AlignMarkHoughDetector(OMRTemplate template, OMRProcessor processor)
	{
		super(template,processor);
		setBufferWidth(5);
	}

	@Override
	public Point2D pointPosition(PageImage pageImage, Point2D expectedPoint)
	{
		//Try different buffers
		setBufferWidth(5);
		Point2D observedPoint=pointPositionInternal(pageImage, expectedPoint);
		debugAlignDetectionArea(pageImage,expectedPoint);

		if (observedPoint==null)
		{
			setBufferWidth(10);
			observedPoint=pointPositionInternal(pageImage, expectedPoint);
			debugAlignDetectionArea(pageImage,expectedPoint);

		}
		if (observedPoint==null)
		{
			setBufferWidth(15);
			observedPoint=pointPositionInternal(pageImage, expectedPoint);
			debugAlignDetectionArea(pageImage,expectedPoint);

		}
		if (observedPoint==null)
		{
			setBufferWidth(20);
			observedPoint=pointPositionInternal(pageImage, expectedPoint);
			debugAlignDetectionArea(pageImage,expectedPoint);
		}
		return observedPoint;
		
	}

	private void debugAlignDetectionArea(PageImage pageImage,
			Point2D expectedPoint) {
		if (logger.isDebugEnabled())
		{
		Point2D topleft= new Point();
		topleft.setLocation(expectedPoint.getX()-getBufferWidth(), expectedPoint.getY()-getBufferWidth());
		Point2D topright= new Point();
		topright.setLocation(expectedPoint.getX()+getBufferWidth(), expectedPoint.getY()-getBufferWidth());
		Point2D bottomleft= new Point();
		bottomleft.setLocation(expectedPoint.getX()-getBufferWidth(), expectedPoint.getY()+getBufferWidth());
		Point2D bottomright= new Point();
		bottomright.setLocation(expectedPoint.getX()+getBufferWidth(), expectedPoint.getY()+getBufferWidth());
		
		debugAlignMarkFrame(pageImage, topleft, topright,bottomleft,bottomright,Color.BLUE);
		}
		return;
	}

	/**
	 * @param pageImage
	 * @param expectedPoint in milimeters
	 * @return
	 */
	private Point2D pointPositionInternal(PageImage pageImage, Point2D expectedPoint)
	{
		Rectangle2D expectedRect=getExpectedRect(expectedPoint);
		SubImage subimage=extractSubimage(pageImage, expectedRect);
		Point reference=subimage.getReference();
		
		
//		Graphics g= pageImage.getReportingGraphics();
//		g.setColor(Color.BLUE);
//		g.drawRect((int)expectedRect.getMinX(), (int)expectedRect.getMinY(), (int)expectedRect.getWidth(), (int)expectedRect.getWidth());
		
		logImage(subimage);
		int width=subimage.getWidth();
		int height=subimage.getHeight();
		int[] orig=new int[width*height];
		float medLum[]=BufferedImageUtil.statsLuminance(subimage, 2);
		float maxLumin=medLum[2];
		float minLumin=medLum[1];
		float medLumin=medLum[0];
		BufferedImageUtil.thresholdAndInvert(subimage, minLumin+(maxLumin-minLumin)/2);
		
		// TODO discard single pixel minimuns
		if (maxLumin-minLumin < 0.10) // ignores areas with a very low contrast (probably empty area)
		{
			logger.debug("Ignoring area with luminance stats max:"+maxLumin+" min:"+minLumin+" med:"+medLumin);
			return null;
		}
		logImage(subimage);
		PixelGrabber grabber = new PixelGrabber(subimage, 0, 0, width, height, orig, 0, width);
		try
		{
			grabber.grabPixels();
		}
		catch (InterruptedException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		LineHoughAlgorithm hough=new LineHoughAlgorithm();
		hough.init(orig,width,height);
		hough.setLines(50);
		hough.process();
		HoughResult[] res=hough.getResults();
		hough.labelSubImage(subimage);
		logImage(subimage);
		logImage(hough.getAccImage());
		
		
		double horizDegrees=0;
		double vertDegrees=0;
		int horizRho=0;
		int vertRho=0;
		int horizCount=0;
		int vertCount=0;
		for (HoughResult houghResult : res)
		{
			if (houghResult.degrees < 45 && houghResult.degrees > -45) // horiz
			{
				horizDegrees+=houghResult.degrees;
				horizRho+=houghResult.rho;
				horizCount++;
			}
			else if (houghResult.degrees > 45 && houghResult.degrees < (180 - 45)) // vert
			{
				
				vertDegrees+=houghResult.degrees;
				vertRho+=houghResult.rho;
				vertCount++;
			}
			else
			{
				logger.debug("descarted deegree:" + houghResult.degrees);
			}
			
		}
		if (horizCount==0 || vertCount==0)
		{
			return null;
		}
		horizDegrees=horizDegrees/horizCount;
		vertDegrees=vertDegrees/vertCount;
		horizRho=horizRho/horizCount;
		vertRho=vertRho/vertCount;
		
		Point2D observedPoint=pageImage.toMilimeters(reference.x+horizRho,reference.y+vertRho);
		return observedPoint;
	}

	

}
