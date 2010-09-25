/*
* ====================================================================
*
* License:        GNU General Public License
*
* Note: Original work copyright to respective authors
*
* This file is part of Blended (c) 2009-2010 University of Valladolid..
*
* Blended is free software; you can redistribute it and/or
* modify it under the terms of the GNU General Public License
* as published by the Free Software Foundation; either version 2
* of the License, or (at your option) any later version.
*
* Blended is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
*
*
* Module developed at the University of Valladolid http://www.eduvalab.uva.es
*
* http://www.itnt.uva.es , http://www.eduvalab.uva.es
*
* Designed and directed by Juan Pablo de Castro with 
* the effort of many other students of telecommunication 
* engineering.
* This module is provides as-is without any 
* guarantee. Use it as your own risk.
*
* @author Juan Pablo de Castro
* @author Jesus Rodilana
* @author María Jesús Verdú 
* @author Luisa Regueras 
* @author Elena Verdú
* 
* @license http://www.gnu.org/copyleft/gpl.html GNU Public License
* @package blended
 ***********************************************************************/

package org.uva.itast;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.awt.Point;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

import org.junit.Test;
import org.uva.itast.blended.omr.OMRProcessor;
import org.uva.itast.blended.omr.pages.ImageFilePage;
import org.uva.itast.blended.omr.pages.PageImage;

public class TestPageImage
{

	/**
	 * Test method for {@link org.uva.itast.blended.omr.pages.PageImage#alignImage()}.
	 * @throws IOException 
	 */
	@Test
	public void testAlignImage() throws IOException
	{
		URL url = getClass().getClassLoader().getResource("p1.pdf");
		File file;
		try
		{
			file = new File(url.toURI());
			PageImage page = new ImageFilePage(file);
			
			OMRProcessor processor = new OMRProcessor();
			String [] args = new String[11];
			args[0]="-i"; 
			URL url_1 = getClass().getClassLoader().getResource("p1.pdf");
			File testPath_1 = new File(url_1.toURI());
			args[1] = testPath_1.toString();
			args[2]="-a";
			args[3]="-o";
			URL url_4 = getClass().getClassLoader().getResource("");
			File testPath_4 = new File(url_4.toURI());
			args[4] = testPath_4.toString();
			args[5]="-id1";
			args[6]="USERID";
			args[7]="-id2";
			args[8]="ACTIVITYCODE";
			args[9]="-d";
			URL url1 = getClass().getClassLoader().getResource("prac_test_lab.fields");
			File testPath = new File(url1.toURI());
			args[10] = testPath.toString();
			
			processor.readCommandLine(args);
			processor.loadTemplate(processor.getDefinitionfile());
			processor.processPath(processor.getInputPath());
		}
		catch (URISyntaxException e)
		{
			fail("Test configuration." + e.getMessage());
		}
	}

	/**
	 * Test method for {@link org.uva.itast.blended.omr.pages.PageImage#toPixels(double, double)}.
	 */
	@Test
	public void testToPixels()
	{
		URL url = getClass().getClassLoader().getResource("OMR_imagePage850x1170.png");
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

	assertTrue("Identity transform do not work.", pixels.equals(objectivePoint));

	pixels = page.toPixels(PageImage.a4width, PageImage.a4height);
	objectivePoint.setLocation(850, 1170);

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
	objectivePoint.setLocation(850+4, 1170+4); //pixels

	assertTrue("Offset transform do not work.", pixels.equals(objectivePoint));
	
	// transform 90 degrees
	
	page.resetAlignmentInfo();
	tr=page.getAllignmentInfo();
	// page rotated 90 degrees and t
	
	tr.translate(PageImage.a4height,0); // places origin at bottom-left NOTE:transforms acts in inverse order
	tr.rotate(0.5*Math.PI);
	
	page.setAlignmentInfo(tr);

	pixels = page.toPixels(PageImage.a4width, PageImage.a4height);
	
	objectivePoint.setLocation(0, 850); // TODO investigate this rounding error of 3 pixels

	assertTrue("90 deegree transform do not work.", pixels.distance(objectivePoint)<5);

	pixels = page.toPixels(0,0);
	
	objectivePoint.setLocation(1170, 0);// TODO investigate this rounding error of 4 pixels

	assertTrue("90 deegree transform do not work.", pixels.distance(objectivePoint)<5);
	
	pixels = page.toPixels(0, PageImage.a4height);
	objectivePoint.setLocation(0, 0);
	assertTrue("90 deegree transform do not work.", pixels.distance(objectivePoint)<5);
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

	pixels = page.toMilimeters(850, 1170);
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
	pixels = page.toMilimeters(850+4, 1170+4);
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

	assertTrue("90 deegree transform do not work.", pixels.distance(objectivePoint)<0.9);

	pixels = page.toMilimeters(1170,0);
	objectivePoint.setLocation(0, 0); //TODO: investigate the error of 0.94 milimeters

	assertTrue("90 deegree transform do not work.", pixels.distance(objectivePoint)<1);
	
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
