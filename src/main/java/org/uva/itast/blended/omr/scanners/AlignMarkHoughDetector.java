package org.uva.itast.blended.omr.scanners;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.PixelGrabber;

import org.uva.itast.blended.omr.BufferedImageUtil;
import org.uva.itast.blended.omr.OMRTemplate;
import org.uva.itast.blended.omr.pages.AbstractAlignMarkDetector;
import org.uva.itast.blended.omr.pages.PageImage;
import org.uva.itast.blended.omr.pages.SubImage;

public class AlignMarkHoughDetector extends AbstractAlignMarkDetector
{

	public AlignMarkHoughDetector(OMRTemplate template)
	{
		super(template);
	}

	@Override
	public Point2D pointPosition(PageImage pageImage, Point2D expectedPoint)
	{
		Rectangle2D expectedRect=getExpectedRect(expectedPoint);
		SubImage subimage=extractSubimage(pageImage, expectedRect);
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
		BufferedImageUtil.thresholdAndInvert(subimage, minLumin+(maxLumin-minLumin)/5);// TODO discard single pixel minimuns
		
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
		
		// TODO clasificar en verticales y horizontales y hacer medias de posición.
		return null;
		
	}

	

}
