package org.uva.itast.blended.omr.align;

import java.awt.Color;
import java.awt.Point;
import java.awt.geom.Rectangle2D;
import java.awt.image.PixelGrabber;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.uva.itast.blended.omr.BufferedImageUtil;
import org.uva.itast.blended.omr.OMRProcessor;
import org.uva.itast.blended.omr.OMRTemplate;
import org.uva.itast.blended.omr.OMRUtils;
import org.uva.itast.blended.omr.pages.PagePoint;
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
	public PagePoint pointPosition(PagePoint expectedPoint)
	{
		//Try different buffers
		// TODO try different offsets scanning from the corners to the expected point
		setBufferWidth(5);
		PagePoint observedPoint=pointPositionInternal(expectedPoint);
		debugAlignDetectionArea(expectedPoint);

		if (observedPoint==null)
		{
			setBufferWidth(10);
			observedPoint=pointPositionInternal(expectedPoint);
			debugAlignDetectionArea(expectedPoint);

		}
		if (observedPoint==null)
		{
			setBufferWidth(15);
			observedPoint=pointPositionInternal(expectedPoint);
			debugAlignDetectionArea(expectedPoint);

		}
		if (observedPoint==null)
		{
			setBufferWidth(20);
			observedPoint=pointPositionInternal( expectedPoint);
			debugAlignDetectionArea(expectedPoint);
		}
		return observedPoint;
		
	}

	private void debugAlignDetectionArea(PagePoint expectedPoint)
	{
		if (logger.isDebugEnabled())
		{
		PagePoint topleft= new PagePoint(expectedPoint.getPageImage(), expectedPoint.getX()-getBufferWidth(), expectedPoint.getY()-getBufferWidth());
		PagePoint topright= new PagePoint(expectedPoint.getPageImage(),expectedPoint.getX()+getBufferWidth(), expectedPoint.getY()-getBufferWidth());
		PagePoint bottomleft= new PagePoint(expectedPoint.getPageImage(),expectedPoint.getX()-getBufferWidth(), expectedPoint.getY()+getBufferWidth());
		PagePoint bottomright= new PagePoint(expectedPoint.getPageImage(),expectedPoint.getX()+getBufferWidth(), expectedPoint.getY()+getBufferWidth());
		
		OMRUtils.debugFrame(expectedPoint.getPageImage(), topleft, topright,bottomleft,bottomright,Color.BLUE,null);
		}
		return;
	}

	/**
	 * @param pageImage
	 * @param expectedPoint in milimeters
	 * @return
	 */
	private PagePoint pointPositionInternal(PagePoint expectedPoint)
	{
		Rectangle2D expectedRect=getExpectedRect(expectedPoint);
		SubImage subimage=extractSubimage(expectedPoint.getPageImage(), expectedRect);
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
			logger.debug("Ignoring probably empty area with luminance stats max:"+maxLumin+" min:"+minLumin+" med:"+medLumin);
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
		int valueMaxHoriz=0;
		int valueMaxVert=0;
		for (HoughResult houghResult : res)
		{
			if (houghResult.degrees < 45 && houghResult.degrees > -45) // horiz
			{
			if (valueMaxHoriz<houghResult.value)
				valueMaxHoriz=houghResult.value;
			}
			else if (houghResult.degrees > 45 && houghResult.degrees < 135) // vert
			{
				if (valueMaxVert<houghResult.value)
					valueMaxVert=houghResult.value;	
			}
		}
		// discard lines that are not enough clear
		if (valueMaxHoriz< valueMaxVert*0.5 || valueMaxVert< valueMaxHoriz*0.5)
			{
			logger.debug("Lines are not clear. Ignoring area.");
			return null;
			}
		int thresholdVert= (int) (valueMaxVert*0.9);
		int thresholdHoriz= (int) (valueMaxHoriz*0.9);
		// TODO do not average all values. Take the highest values <10%.
		for (HoughResult houghResult : res)
		{
			
			if (houghResult.degrees < 45 && houghResult.degrees > -45) // horiz
			{
				if (houghResult.value<thresholdHoriz)
					continue;
				horizDegrees+=houghResult.degrees;
				horizRho+=houghResult.rho;
				horizCount++;
			}
			else if (houghResult.degrees > 45 && houghResult.degrees < 135) // vert
			{
				if (houghResult.value<thresholdVert)
					continue;
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
		
		PagePoint observedPoint=new PagePoint(expectedPoint.getPageImage(),reference.x+horizRho,reference.y+vertRho);
		return observedPoint;
	}

	

}
