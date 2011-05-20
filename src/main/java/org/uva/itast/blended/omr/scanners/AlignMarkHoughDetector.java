package org.uva.itast.blended.omr.scanners;

import java.awt.Point;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.PixelGrabber;

import org.uva.itast.blended.omr.BufferedImageUtil;
import org.uva.itast.blended.omr.OMRProcessor;
import org.uva.itast.blended.omr.OMRTemplate;
import org.uva.itast.blended.omr.pages.AbstractAlignMarkDetector;
import org.uva.itast.blended.omr.pages.PageImage;
import org.uva.itast.blended.omr.pages.SubImage;

public class AlignMarkHoughDetector extends AbstractAlignMarkDetector
{

	

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
		if (observedPoint==null)
		{
			setBufferWidth(10);
			observedPoint=pointPositionInternal(pageImage, expectedPoint);
		}
		if (observedPoint==null)
		{
			setBufferWidth(15);
			observedPoint=pointPositionInternal(pageImage, expectedPoint);
		}
		return observedPoint;
		
	}

	/**
	 * @param pageImage
	 * @param expectedPoint
	 * @return
	 */
	private Point2D pointPositionInternal(PageImage pageImage, Point2D expectedPoint)
	{
		Rectangle2D expectedRect=getExpectedRect(expectedPoint);
		SubImage subimage=extractSubimage(pageImage, expectedRect);
		Point reference=subimage.getReference();
		BufferedImage report=pageImage.getReportingImage();
//		Graphics g= report.getGraphics();
//		g.setColor(Color.BLUE);
//		g.drawRect(expectedRect.getMinX(), expectedRect.getMinY(), expectedRect.getWidth(), expectedRect.getWidth());
		
		logImage(subimage);
		int width=subimage.getWidth();
		int height=subimage.getHeight();
		int[] orig=new int[width*height];
		float medLum[]=BufferedImageUtil.statsLuminance(subimage, 2);
//		BufferedImageUtil.scale(subimage, 1/medLum[1]);
//		BufferedImageUtil.invert(subimage);
//		BufferedImageUtil.thresholdAndInvert(subimage, medLum[0]);
		float maxLumin=medLum[2];
		float minLumin=medLum[1];
		float medLumin=medLum[0];
		BufferedImageUtil.thresholdAndInvert(subimage, minLumin+(maxLumin-minLumin)/2);// TODO discard single pixel minimuns
		
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
				System.out.println("descarted deegree:" + houghResult.degrees);
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
