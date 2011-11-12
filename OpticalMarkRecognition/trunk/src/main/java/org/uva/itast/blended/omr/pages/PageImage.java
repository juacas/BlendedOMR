/**
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
* @author MarÃ­a JesÃºs VerdÃº 
* @author Luisa Regueras 
* @author Elena VerdÃº
* 
* @license http://www.gnu.org/copyleft/gpl.html GNU Public License
* @package blended
 ***********************************************************************/

 

package org.uva.itast.blended.omr.pages;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.Dimension2D;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Date;

import javax.imageio.ImageIO;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.uva.itast.blended.omr.BufferedImageUtil;
import org.uva.itast.blended.omr.scanners.Size;

public abstract class PageImage
{
	/**
	 * Logger for this class
	 */
	static final Log	logger	= LogFactory.getLog(PageImage.class);
	private static final int	REPORTING_WIDTH	= 1024;
	public static double		a4width		= 210;										// mm
	public static double		a4height	= 297;										// mm
	private BufferedImage	image;
	private BufferedImage	reportImage;
	/**
	 * Sets the transformation data needed for aligning the Page
	 * contains information about the traslation and rotation of the page in pixels units.
	 * Need to consider image scale to convert from milimeters
	 *
	 **/
	private AffineTransform	alignmentTransform;
	
	/**
	 * Calculated resolution
	 */
	double vertResolution=Double.NaN;
	double horizResolution=Double.NaN;
	/**
	 * @return the scanned image
	 */
	public BufferedImage getImage()
	{
		if (image==null)
		{
			setImage(createImage());
		}
		return image;
	}

	/**
	 * @return
	 */
	abstract BufferedImage createImage();


	/**
	 * stores the {@link BufferedImage} of the page. It can be in portrait or in landscape proportions (90 degrees clockwise rotation).
	 * @param imagen the imagen to set
	 * The image is handled in portrait format.
	 * If the Image has landscape proportions it is assumed to be clockwise rotated and this method rotates it.
	 * 
	 */
	protected void setImage(BufferedImage imagen)
	{
		//
		if (imagen!=null && imagen.getHeight()<imagen.getWidth())
		{
		logger.debug("Rotating image. Original size was:"+imagen.getWidth()+"x"+imagen.getWidth()+" pixeles.");
		this.image= BufferedImageUtil.rotateImage(imagen,-90);
		}
		else
		{
		this.image=imagen;
		}
		// configure alignment information
		resetAlignmentInfo();
	}

	/**
	 * @param data.getImage()
	 */
	public void resetAlignmentInfo()
	{
		AffineTransform tr=new AffineTransform();
		
		double horizRatio = getHorizontalResolution();
		double vertRatio = getVerticalResolution();
		
		// Do not assume square pixels
		tr.setToScale(horizRatio,vertRatio);
		setAlignmentInfo(tr);
	}
	

	
	/**
	 * Sets the transformation data needed for aligning the Page
	 * contains information about the traslation and rotation of the page in pixels units.
	 * Need to consider image scale to convert from milimeters
	 * @param transform
	 * @see AffineTransform
	 * @see getVerticalRatio
	 * @see getHorizontalRatio
	 */
	public void setAlignmentInfo(AffineTransform transform)
	{
		this.alignmentTransform=transform;
	}
	
	public AffineTransform getAllignmentInfo()
	{
		if (this.alignmentTransform==null)
			resetAlignmentInfo();
		return this.alignmentTransform;
		}
	/**
	 * Try to detect alignment marks and create the spatial transformation info
	 * @see java.awt.geom.AffineTransform 
	 */
	public void alignImage()
	{
		long funcStart = System.currentTimeMillis();
		
		AffineTransform transform=getAllignmentInfo();
		
		// obtain rotation and traslation
		transform.translate(0, 0); //pixels
		
		setAlignmentInfo(transform);
		
		logger.debug("Page aligned in (ms)" + (System.currentTimeMillis() - funcStart)); //$NON-NLS-1$

	}
	/**
	 * Mark the results in a thumbnail of the page intended for reporting
	 */
	public void labelPageAsProcessed()
	{
		
		BufferedImage imagen = getReportingImage();
		Graphics2D g=imagen.createGraphics();
		g.setColor(Color.RED);
		g.drawString("Page Processed at:"+new Date()+" W:"+imagen.getWidth()+" H:"+imagen.getHeight(), 10, 10);
		
	}

	/**
	 * Creates an small-resolution image for reporting purposes
	 * @return
	 */
	public BufferedImage getReportingImage()
	{
		if (this.reportImage==null)
		{
			// Create a fixed (smaller) resolution image
			int w=Math.min(REPORTING_WIDTH,getImage().getWidth());
			//int w=getImage().getWidth(); // scale 1
			int h=getImage().getHeight()*w/getImage().getWidth();
			
			this.reportImage=new BufferedImage(w, h,BufferedImage.TYPE_INT_RGB);
			Image scaledImage=getImage().getScaledInstance(w, h, Image.SCALE_DEFAULT);
			this.reportImage.createGraphics().drawImage(scaledImage, 0, 0, null);
		}
	
		return this.reportImage;
	}

	/**
	 * @return
	 */
	public abstract String getType();

	/**
	 * @return
	 */
	public abstract String getName();

	/**
	 * Free up memory resources
	 */
	public void freeMemory()
	{
		long tstart=System.currentTimeMillis();
		long freeMem=Runtime.getRuntime().freeMemory();
		long availMem=Runtime.getRuntime().totalMemory();
		this.reportImage.flush();
		this.reportImage=null;
		
		this.image.flush();
		setImage(null);
		System.gc();

		if (logger.isDebugEnabled())
		{
			logger.debug("endUse()-Free Memory in "+(System.currentTimeMillis()-tstart)+" ms TotalMem:"+availMem/1024/1024+" MB freeMem Before:" + freeMem/1024/1024 + ", freeMem After:" + Runtime.getRuntime().freeMemory()/1024/1024); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}

	/**
	 * @param outputdir
	 * @throws IOException 
	 */
	public File outputMarkedPage(String outputdir) throws IOException
	{
		File debugImagePath;
							
			debugImagePath = File.createTempFile("OMR_original_marked", ".jpg", new File(outputdir));
			ImageIO.write(getReportingImage(), "JPG", debugImagePath);
		return debugImagePath;
	}

	/**
	 * @param outputdir
	 * @throws IOException 
	 */
	public void outputWorkingPage(String outputdir) throws IOException
	{
		File debugImagePath;
		
		debugImagePath = File.createTempFile("OMR_working_debug", ".jpg", new File(outputdir));		
		ImageIO.write(getImage(), "JPG", debugImagePath);
	
	}

	/**
	 * Returns the ratio between pixels and milimeters
	 * default implementation getImagen().getWidth()/PageImage.a4width 
	 * @see #getPreferredHorizontalResolution()
	 * @return resolution in pixels/mm
	 */
	public double getHorizontalResolution()
	{
		if (!Double.isNaN(horizResolution))
			return horizResolution;
		else
			return getPreferredHorizontalResolution();
	}
	protected abstract double getPreferredHorizontalResolution();
	/**
	 * Returns the ratio between pixels and milimeters
	 * default implementation getImagen().getHeight()/PageImage.a4height
	 * @see #getPreferredVerticalResolution()
	 * @return resolution in pixels/mm
	 */
	public double getVerticalResolution()
	{
		if (!Double.isNaN(vertResolution))
			return vertResolution;
		else
			return getPreferredVerticalResolution();
	}
	
	protected abstract double getPreferredVerticalResolution();
	/**
	 * Sets a new vertical resolution if calculated
	 * @param res resolution in pixels/mm
	 */
	public void setVerticalResolution(double res)
	{
		this.vertResolution=res;
		resetAlignmentInfo();
	};
	/**
	 * Sets a new vertical resolution if calculated
	 * @param res resolution in pixels/mm
	 */
	public void setHorizontalResolution(double res)
	{
		this.horizResolution=res;
		resetAlignmentInfo();
	};
	/**
	 * Use alignment information to transform from milimeters to pixel coordinates at the preferred resolution for this page
	 * @param x
	 * @return
	 */
	public Point toPixels(double x,double y)
	{
		AffineTransform alignment=getAllignmentInfo();
		Point2D coord=new Point2D.Double(x, y);
		Point coordTransformed=new Point();
		alignment.transform(coord, coordTransformed);
		
		return coordTransformed;
	}
	
	public Point toPixelsPoint(double x,double y)
	{
		AffineTransform alignment=getAllignmentInfo();
		alignment.setToScale(getPreferredHorizontalResolution(), getPreferredVerticalResolution());
		
		Point2D coord=new Point2D.Double(x, y);
		Point coordTransformed=new Point();
		alignment.transform(coord, coordTransformed);
		
		return coordTransformed;
	}

	/**
	 * Obtain a subimage from the pageimage (in milimeters and related to actual paper)
	 * Place to make optimizations when rendering high resolution files.
	 * It takes into account the traslation and rotation of the physical page.
	 * 
	 * Default implementation uses getImage() which should decode entire image.
	 * @param x mm
	 * @param y mm
	 * @param w mm
	 * @param h mm
	 * @param imageType
	 * @return SubImage
	 * @see SubImage
	 */
	public SubImage getSubimage(double x, double y, double w, double h, int imageType)
	{
	Rectangle2D rectMM=new Rectangle2D.Double();
	rectMM.setFrame(x,y,w,h);
	return getSubimage(rectMM, imageType);
	}

	/**
	 * Convert from  pixel-space to paper-space.
	 * Paper-space refers to logical area of the paper in the image.
	 * Pixel-space refers to entire area of the image that contains the image of the paper. (Maybe with offset and rotation)
	 * @param i
	 * @param j
	 * @return
	 * @throws NoninvertibleTransformException 
	 */
	public Point2D toMilimeters(int i, int j) 
	{

		try
		{
			AffineTransform tr = getAllignmentInfo();
			AffineTransform inv;
			inv = tr.createInverse();
			Point2D pixeles = new Point(i, j);
			Point2D dest=new Point2D.Double();
			return inv.transform(pixeles, dest);
		}
		catch (NoninvertibleTransformException e)
		{
			throw new RuntimeException("error page definition.",e);
		}

	}

	/**
	 * Convert box from Milimeters to Pixels relative to the PageImage in the preferred resolution
	 * in case of alignment rotation the returned Rectangle is the minimum bounding box that contains
	 * the original corners transformed.
	 * @param box in milimeters related to actual page
	 * @return minimum bounding box in pixels related to image representation
	 */
	public Rectangle toPixels(Rectangle2D box)
	{
		
		Point p1=toPixels(box.getX(),box.getY());
		Point p2=toPixels(box.getMaxX(),box.getMaxY());
		Rectangle bboxPx=new Rectangle(p1);
		bboxPx.add(p2);
		return bboxPx;
		
	}

	/**
	 * Scaled graphics for drawing on a small version of the page.
	 * Transform is set for drawing in pixels refered to the original scanned image
	 * @return
	 */
	public Graphics2D getReportingGraphics()
	{
		// TODO: Crear una transformacion equivalente a la de la página para la imagen reducida
		BufferedImage reportingImage = this.getReportingImage();
		Graphics2D g=reportingImage.createGraphics();
		
//		AffineTransform origTransf=getAllignmentInfo();
//
//		AffineTransform trans=(AffineTransform) origTransf.clone();
//		trans.scale(reportingImage.getWidth()/(getImage().getWidth()),
//			reportingImage.getHeight()/(getImage().getHeight()));
//		AffineTransform trans=g.getTransform();
//		trans.scale(reportingImage.getWidth()/(getPreferredHorizontalResolution()*PageImage.a4width),
//				reportingImage.getHeight()/(getPreferredVerticalResolution()*PageImage.a4height));
		double scaleX=((double)reportingImage.getWidth())/(getImage().getWidth());
		double scaleY=((double)reportingImage.getHeight())/(getImage().getHeight());
		AffineTransform trans = g.getTransform();
		trans.scale(scaleX,scaleY);
		g.setTransform(trans);
		return g;
	}

	/**
	 * @param rectMM bounding box in milimeters
	 * @see #getSubimage(double, double, double, double, int)
	 * @return
	 */
	public SubImage getSubimage(Rectangle2D rectMM, int imageType)
	{
		
		Rectangle rect=this.toPixels(rectMM);
		if (logger.isDebugEnabled())
		{
			logger.debug("Subimage "+ rectMM+" mm "+rect+" px.");
		}
		PagePoint upperLeft=new PagePoint(this, rectMM.getX(), rectMM.getY());
		
		Point reference=upperLeft.getPixelsPoint();
		
		BufferedImage originalImage=getImage();
		Rectangle originalRect= new Rectangle(originalImage.getWidth(), originalImage.getHeight());
		// copy what is available from image
		Rectangle available= rect.intersection(originalRect);
		
		BufferedImage originalSubImage=originalImage.getSubimage(available.x,available.y,available.width,available.height);
		
		//rotate image
		SubImage subimage=new SubImage(rect.width,rect.height,imageType);
		subimage.setReference(reference);
		subimage.setBoundingBox(rect);
		subimage.setCapturedBoundingBox(available);
		
		
		Graphics2D g=subimage.createGraphics();
		g.drawImage(originalSubImage, available.x-rect.x,available.y-rect.y, null);
		
		return subimage;
	}

	/**
	 * @param pointMM
	 * @return
	 */
	public Point toPixels(Point2D pointMM)
	{
		return toPixels(pointMM.getX(), pointMM.getY());
	}

	/**
	 * @param templateRectPx
	 * @return
	 */
	public Rectangle2D toMilimeters(Rectangle boxPx)
	{
		Point2D p1=toMilimeters(boxPx.x,boxPx.y);
		Point2D p2=toMilimeters((int)boxPx.getMaxX(),(int)boxPx.getMaxY());
		Rectangle2D bbox=new Rectangle();
		bbox.setFrameFromDiagonal(p1, p2);
		
		return bbox;
	}

	public Dimension2D sizeInPixels(Size size)
	{ 
		Dimension2D dim= new Dimension();
		dim.setSize(getAllignmentInfo().getScaleX()*size.getWidth(),getAllignmentInfo().getScaleY()*size.getHeight());
		return dim;
	}

	public Dimension2D sizeToMilimeters(Size size)
	{
		Dimension2D dim= new Dimension();
		dim.setSize(size.getWidth()/getAllignmentInfo().getScaleX(),size.getHeight()/getAllignmentInfo().getScaleY());
		return dim;
	}

}