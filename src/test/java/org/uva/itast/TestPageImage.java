/**
 * 
 */
package org.uva.itast;

import static org.junit.Assert.*;

import java.awt.Point;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;

import org.junit.Test;
import org.uva.itast.blended.omr.pages.ImageFilePage;
import org.uva.itast.blended.omr.pages.PageImage;

/**
 * @author juacas
 *
 */
public class TestPageImage
{

	/**
	 * Test method for {@link org.uva.itast.blended.omr.pages.PageImage#alignImage()}.
	 */
	@Test
	public void testAlignImage()
	{
	
	}

	/**
	 * Test method for {@link org.uva.itast.blended.omr.pages.PageImage#toPixels(double, double)}.
	 */
	@Test
	public void testToPixels()
	{
		URL url = getClass().getClassLoader().getResource(
		"OMR_imagePage850x1170.png");
File file;
try
{
	file = new File(url.toURI());

	PageImage page = new ImageFilePage(file);

	// Page without alignment
	Point2D pixels;
	Point2D objectivePoint = new Point();

	page.resetAlignmentInfo();
	pixels = page.toPixels(0, 0);
	objectivePoint.setLocation(0, 0);

	assertTrue("Identity transform do not work.", pixels
			.equals(objectivePoint));

	pixels = page.toPixels(PageImage.a4width, PageImage.a4height);
	objectivePoint.setLocation(850, 1174);

	assertTrue("Identity transform do not work BottomLeft corner.", pixels
			.equals(objectivePoint));
	
// transform offset
	page.resetAlignmentInfo();
	AffineTransform tr=page.getAllignmentInfo();
	tr.translate(1, 1);// milimeters
	page.setAlignmentInfo(tr);
		
	pixels = page.toPixels(0, 0); //milimeters
	objectivePoint.setLocation(4, 4); //pixels

	assertTrue("Offset transform do not work.", pixels.equals(objectivePoint));
	pixels = page.toPixels(PageImage.a4width, PageImage.a4height);
	objectivePoint.setLocation(850+4, 1174+4); //pixels

	assertTrue("Offset transform do not work.", pixels.equals(objectivePoint));
	
	// transform 90 degrees
	page.resetAlignmentInfo();
	tr=page.getAllignmentInfo();
	// page rotated 90 degrees and t
	
	tr.translate(PageImage.a4height,0); // places origin at bottom-left NOTE:transforms acts in inverse order
	tr.rotate(0.5*Math.PI);
	
	
	page.setAlignmentInfo(tr);

	pixels = page.toPixels(PageImage.a4width, PageImage.a4height);
	objectivePoint.setLocation(0, 850);

	assertTrue("90 deegree transform do not work.", pixels.equals(objectivePoint));

	pixels = page.toPixels(0,0);
	objectivePoint.setLocation(1174, 0);

	assertTrue("90 deegree transform do not work.", pixels.equals(objectivePoint));
	
	pixels = page.toPixels(0, PageImage.a4height);
	objectivePoint.setLocation(0, 0);
	assertTrue("90 deegree transform do not work.", pixels.equals(objectivePoint));
}
catch (URISyntaxException e)
{
	fail("Test configuration." + e.getMessage());
}

	}
	
	/**
	 * Test method for {@link org.uva.itast.blended.omr.pages.PageImage#toMilimeters(int, int)}.
	 */
	@Test
	public void testToMilimeters()
	{
		URL url = getClass().getClassLoader().getResource(
		"OMR_imagePage850x1170.png");
File file;
try
{
	file = new File(url.toURI());

	PageImage page = new ImageFilePage(file);

	// Page without alignment
	Point2D pixels;
	Point2D objectivePoint = new Point();

	page.resetAlignmentInfo();
	pixels = page.toMilimeters(0, 0);
	objectivePoint.setLocation(0, 0);

	assertTrue("Identity transform do not work.",  pixels.distance(objectivePoint)<0.5);

	pixels = page.toMilimeters(850, 1174);
	objectivePoint.setLocation(PageImage.a4width, PageImage.a4height);

	assertTrue("Identity transform do not work BottomLeft corner.", pixels.distance(objectivePoint)<0.5);
	
// transform offset
	page.resetAlignmentInfo();
	AffineTransform tr=page.getAllignmentInfo();
	tr.translate(1, 1);// milimeters
	page.setAlignmentInfo(tr);
		
	pixels = page.toMilimeters(4, 4); //pixels on image
	objectivePoint.setLocation(0, 0); //millimeters on paper

	assertTrue("Offset transform do not work.", pixels.distance(objectivePoint)<0.1);
	pixels = page.toMilimeters(850+4, 1174+4);
	objectivePoint.setLocation(PageImage.a4width, PageImage.a4height); //pixels

	assertTrue("Offset transform do not work.", pixels.distance(objectivePoint)<0.1);
	
	// transform 90 degrees
	page.resetAlignmentInfo();
	tr=page.getAllignmentInfo();
	// page rotated 90 degrees and t
	
	tr.translate(PageImage.a4height,0); // places origin at bottom-left NOTE:transforms acts in inverse order
	tr.rotate(0.5*Math.PI);
	
	
	page.setAlignmentInfo(tr);

	pixels = page.toMilimeters(0, 850);
	objectivePoint.setLocation(PageImage.a4width, PageImage.a4height);

	assertTrue("90 deegree transform do not work.", pixels.distance(objectivePoint)<0.1);

	pixels = page.toMilimeters(1174,0);
	objectivePoint.setLocation(0, 0);

	assertTrue("90 deegree transform do not work.", pixels.distance(objectivePoint)<0.1);
	
	pixels = page.toMilimeters(0, 0);
	objectivePoint.setLocation(0, PageImage.a4height);
	assertTrue("90 deegree transform do not work.", pixels.distance(objectivePoint)<0.1);
}
catch (URISyntaxException e)
{
	fail("Test configuration." + e.getMessage());
}

	}

}
