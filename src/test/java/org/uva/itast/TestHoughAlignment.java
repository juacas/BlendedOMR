package org.uva.itast;

import java.awt.Point;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.text.NumberFormat;

import junit.framework.Assert;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;
import org.uva.itast.blended.omr.OMRProcessor;
import org.uva.itast.blended.omr.pages.AlignMarkDetector;
import org.uva.itast.blended.omr.pages.AlignmentResult;
import org.uva.itast.blended.omr.pages.AlignmentResult.AlignmentPosition;
import org.uva.itast.blended.omr.pages.ImageFilePage;
import org.uva.itast.blended.omr.pages.PageImage;
import org.uva.itast.blended.omr.pages.PagePoint;
import org.uva.itast.blended.omr.scanners.AlignMarkHoughDetector;
@RunWith(Theories.class)
public class TestHoughAlignment
{
	private static final int	TRANSFORMATION_TOLERANCE	=10;
	/**
	 * Logger for this class
	 */
	private static final Log	logger	=LogFactory.getLog(TestHoughAlignment.class);

@Theory
public void testFrameMarksDetection(double value) throws IOException
{
	URL templateUrl=TestHoughAlignment.class.getClassLoader().getResource("frame_test.fields");
	String valueString=NumberFormat.getNumberInstance().format(value);
	String fileName="OMR_imagePage850x1170rotated"+valueString+".jpg";

		if (logger.isDebugEnabled())
		{
			logger.debug("Testing a rotation of "+value+" deegrees.");
			logger.debug("testFrameMarksDetection(double) - Scanning filename: - fileName=" + fileName); //$NON-NLS-1$
		}

	URL imageUrl=TestHoughAlignment.class.getClassLoader().getResource(fileName);
	
	OMRProcessor omr=new OMRProcessor();
	omr.loadTemplate(templateUrl.getPath());
	File dir=new File(new File(templateUrl.getPath()).getParentFile(),"output");
	omr.setOutputdir(dir.getAbsolutePath());	
	AlignMarkDetector detector=new AlignMarkHoughDetector(omr.getActiveTemplate(), omr);
	detector.setBufferWidth(10);
	
	PageImage pageImage=new ImageFilePage(imageUrl);

	AlignmentResult detectedAlignmentInfo=detector.align(pageImage);
	pageImage.outputMarkedPage(dir.getAbsolutePath());
	Assert.assertEquals(value,detectedAlignmentInfo.getAlignmentSlope()*180/Math.PI,0.5);
	
	AffineTransform transform=detectedAlignmentInfo.getAlignmentTransform();
	
	// check the correct translation of the center
	PagePoint dcenter=detectedAlignmentInfo.getDetectedCenter();
	PagePoint e_center=detectedAlignmentInfo.getExpectedCenter();
	Point2D trans=new Point();
//	transform it to px
	transform.transform(e_center, trans);
	Assert.assertEquals(trans.getX(), dcenter.getXpx(), TRANSFORMATION_TOLERANCE/2);
	Assert.assertEquals(trans.getY(), dcenter.getYpx(), TRANSFORMATION_TOLERANCE/2);	
	// check correspondences
	// Compare TOPLEFT detected to synthetic generated point
	
	PagePoint dTopLeft=detectedAlignmentInfo.getDetected().get(AlignmentPosition.TOPLEFT);
	PagePoint eTopLeft=detectedAlignmentInfo.getExpected().get(AlignmentPosition.TOPLEFT);
	
	// expected mm coords should be traslated to detected px coords
	
	Point2D ptDst = new Point();
	transform.transform(eTopLeft, ptDst);
	Assert.assertEquals(ptDst.getX(), dTopLeft.getXpx(), TRANSFORMATION_TOLERANCE);
	Assert.assertEquals(ptDst.getY(), dTopLeft.getYpx(), TRANSFORMATION_TOLERANCE);
	// test corners
	Point2D ptDstBottomLeft = new Point();
	PagePoint dBottomLeft=detectedAlignmentInfo.getDetected().get(AlignmentPosition.BOTTOMLEFT);
	PagePoint eBottomLeft=detectedAlignmentInfo.getExpected().get(AlignmentPosition.BOTTOMLEFT);
	transform.transform(eBottomLeft, ptDstBottomLeft);
	Assert.assertEquals(ptDstBottomLeft.getX(), dBottomLeft.getXpx(), TRANSFORMATION_TOLERANCE);
	Assert.assertEquals(ptDstBottomLeft.getY(), dBottomLeft.getYpx(), TRANSFORMATION_TOLERANCE);
	
	Point2D ptDstTopRight = new Point();
	PagePoint dTopRight=detectedAlignmentInfo.getDetected().get(AlignmentPosition.TOPRIGHT);
	PagePoint eTopRight=detectedAlignmentInfo.getExpected().get(AlignmentPosition.TOPRIGHT);
	transform.transform(eTopRight, ptDstTopRight);
	Assert.assertEquals(ptDstTopRight.getX(), dTopRight.getXpx(), TRANSFORMATION_TOLERANCE);
	Assert.assertEquals(ptDstTopRight.getY(), dTopRight.getYpx(), TRANSFORMATION_TOLERANCE);

}
public static @DataPoints double[] values={0,1.0,1.1,1.2,1.3,1.5,2.0,3.0};

}
