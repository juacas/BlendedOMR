package org.uva.itast;

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
import org.uva.itast.blended.omr.pages.ImageFilePage;
import org.uva.itast.blended.omr.pages.PageImage;
import org.uva.itast.blended.omr.scanners.AlignMarkRodilanaDetector;
@RunWith(Theories.class)
public class TestRodilanaAlignment
{
	/**
	 * Logger for this class
	 */
	private static final Log	logger	=LogFactory.getLog(TestRodilanaAlignment.class);

@Theory
public void testFrameMarksDetection(double value) throws IOException
{
	URL templateUrl=TestRodilanaAlignment.class.getClassLoader().getResource("frame_test.fields");
	String valueString=NumberFormat.getNumberInstance().format(value);
	String fileName="OMR_imagePage850x1170rotated"+valueString+".jpg";

		if (logger.isDebugEnabled())
		{
			logger.debug("Testing a rotation of "+value+" deegrees.");
			logger.debug("testFrameMarksDetection(double) - Scanning filename: - fileName=" + fileName); //$NON-NLS-1$
		}

	URL imageUrl=TestRodilanaAlignment.class.getClassLoader().getResource(fileName);
	
	OMRProcessor omr=new OMRProcessor();
	omr.loadTemplate(templateUrl.getPath());
	AlignMarkRodilanaDetector mark=new AlignMarkRodilanaDetector(omr.getActiveTemplate(),null);
	PageImage pageImage=new ImageFilePage(imageUrl);
	mark.setBufferWidth(8);
	mark.align(pageImage);
	Assert.assertTrue(Math.abs(Math.abs(mark.getAlignmentSlope()*180/Math.PI)-value)<0.1);
}
public static @DataPoints double[] values={0,1.0,1.1,1.2,1.3,1.5,2.0,3.0};

}
