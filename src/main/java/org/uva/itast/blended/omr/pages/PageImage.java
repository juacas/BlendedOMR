/**
 * 
 */
package org.uva.itast.blended.omr.pages;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
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


/**
 * @author juacas
 */
public abstract class PageImage
{
	/**
	 * Logger for this class
	 */
	private static final Log	logger	= LogFactory.getLog(PageImage.class);
	private static final int	REPORTING_WIDTH	= 1024;
	public static double		a4width		= 210;										// mm
	public static double		a4height	= 290;										// mm





	/**
	 * @return the imagen
	 */
	public BufferedImage getImagen()
	{
		if (imagen==null)
		{
			setImagen(createImage());
		}
		return imagen;
	}

	/**
	 * @return
	 */
	abstract BufferedImage createImage();

	private BufferedImage	imagen;
	private BufferedImage	reportImage;
	private AffineTransform	alignmentTransform;

	

	/**
	 * @param imagen the imagen to set
	 */
	protected void setImagen(BufferedImage imagen)
	{
		this.imagen = imagen;
		// configure alignment information
		resetAlignmentInfo();
	}

	/**
	 * @param imagen
	 */
	public void resetAlignmentInfo()
	{
		AffineTransform tr=new AffineTransform();
		
		double horizRatio = getPreferredHorizontalResolution();
		double vertRatio = getPreferredVerticalResolution();
		
		// Do not assume square pixels
		tr.setToScale(horizRatio,vertRatio);
		setAlignmentInfo(tr);
	}

	/**
	 * 
	 */
	public void align()
	{
		long taskStart = System.currentTimeMillis();
		AffineTransform transform=new AffineTransform();
		
// TODO: Process page for aligning
		setAlignmentInfo(transform);
		logger.debug("\tImage alligned in (ms)" + (System.currentTimeMillis() - taskStart)); //$NON-NLS-1$
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
	return alignmentTransform;
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
		if (reportImage==null)
		{
			// Create a fixed (smaller) resolution image
			int w=Math.min(REPORTING_WIDTH,getImagen().getWidth());
			int h=getImagen().getHeight()*w/getImagen().getWidth();
			
			reportImage=new BufferedImage(w, h,BufferedImage.TYPE_INT_RGB);
			Image scaledImage=getImagen().getScaledInstance(w, h, Image.SCALE_DEFAULT);
			reportImage.createGraphics().drawImage(scaledImage, 0, 0, null);
		}
	
		return reportImage;
	}

	/**
	 * @return
	 */
	public abstract String getType();

	/**
	 * @return
	 */
	public abstract String getFileName();

	/**
	 * Free up memory resources
	 */
	public void freeMemory()
	{
		long freeMem=Runtime.getRuntime().freeMemory();
		long availMem=Runtime.getRuntime().totalMemory();
		reportImage.flush();
		reportImage=null;
		
		imagen.flush();
		setImagen(null);
		System.gc();

		if (logger.isDebugEnabled())
		{
			logger.debug("endUse() -TotalMem"+availMem/1024/1024+" MB freeMem Before=" + freeMem/1024/1024 + ", freeMem After=" + Runtime.getRuntime().freeMemory()/1024/1024); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}

	/**
	 * @param outputdir
	 * @throws IOException 
	 */
	public void outputMarkedPage(String outputdir) throws IOException
	{
		File debugImagePath;
							
			debugImagePath = File.createTempFile("OMR_original_marked", ".jpg", new File(outputdir));
			ImageIO.write(getReportingImage(), "JPG", debugImagePath);
		
	}

	/**
	 * @param outputdir
	 * @throws IOException 
	 */
	public void outputWorkingPage(String outputdir) throws IOException
	{
		File debugImagePath;
		
		debugImagePath = File.createTempFile("OMR_working_debug", ".jpg", new File(outputdir));		
		ImageIO.write(getImagen(), "JPG", debugImagePath);
	
	}

	/**
	 * Returns the ratio between pixels and milimeters
	 * default implementation getImagen().getWidth()/PageImage.a4width
	 * @return resolution in pixels/mm
	 */
	public abstract double getPreferredHorizontalResolution();
	/**
	 * Returns the ratio between pixels and milimeters
	 * default implementation getImagen().getHeight()/PageImage.a4height
	 * @return resolution in pixels/mm
	 */
	public abstract double getPreferredVerticalResolution();

	/**
	 * Use alignment information to transform from milimeters to pixel coordinates at the preferred resolution for this page
	 * @param x
	 * @return
	 */
	public Point toPixels(double x,double y)
	{
		AffineTransform alignment=getAllignmentInfo();
		Point2D coord=new Point();
		coord.setLocation(x, y);
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
	 * @param imageType TODO
	 * @return SubImage
	 * @see SubImage
	 */
	public SubImage getSubimage(double x, double y, double w, double h, int imageType)
	{
	Rectangle2D rectMM=new Rectangle();
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
			Point2D dest=null;
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
	 * @return
	 */
	public Graphics2D getReportingGraphics()
	{
		BufferedImage reportingImage = this.getReportingImage();
		Graphics2D g=reportingImage.createGraphics();
		AffineTransform trans= g.getTransform();
		trans.scale(reportingImage.getWidth()/(getPreferredHorizontalResolution()*PageImage.a4width),
				reportingImage.getHeight()/(getPreferredVerticalResolution()*PageImage.a4height));
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
		Point upperLeft=this.toPixels(rectMM.getX(), rectMM.getY());
		
		//TODO: incluir la resoluci�n preferida ahora asume la nativa de la imagen
		Point reference=upperLeft;
		
		BufferedImage originalSubImage=getImagen().getSubimage(rect.x,rect.y, rect.width, rect.height);
		
		//rotate image
		SubImage subimage=new SubImage(rect.width,rect.height,imageType);
		subimage.setReference(reference);
		subimage.setBoundingBox(rect);
		
		
		Graphics2D g=subimage.createGraphics();
		g.drawImage(originalSubImage, 0,0, null);
		
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
	
}