package org.uva.itast.blended.omr.align;

import java.awt.geom.AffineTransform;
import java.util.HashMap;
import java.util.Map;

import org.uva.itast.blended.omr.pages.PagePoint;

public class AlignmentResult
{
	public static  enum AlignmentPosition{TOPLEFT,TOPRIGHT,BOTTOMLEFT,BOTTOMRIGHT};
	protected Map<AlignmentPosition,PagePoint> expected=new HashMap<AlignmentPosition, PagePoint>();
	protected Map<AlignmentPosition,PagePoint> detected=new HashMap<AlignmentPosition, PagePoint>();
	protected Map<PagePoint,PagePoint> pointsMap=new HashMap<PagePoint, PagePoint>();
	private double	slope;
	private PagePoint	detectedCenter;
	private AffineTransform	transform;
	private PagePoint	expectedCenter;
	private double	horizRes;
	private double	vertRes;
	private PagePoint	delta;
	
	public void addResult(AlignmentPosition type, PagePoint etopleft, PagePoint dtopleft)
	{
		expected.put(type,etopleft);
		detected.put(type,dtopleft);
		pointsMap.put(etopleft,dtopleft);
	}

	/**
	 * @return the expected
	 */
	public Map<AlignmentPosition, PagePoint> getExpected()
	{
		return expected;
	}

	/**
	 * @return the detected
	 */
	public Map<AlignmentPosition, PagePoint> getDetected()
	{
		return detected;
	}

	/**
	 * @return the paired
	 */
	public Map<PagePoint, PagePoint> getPointPairs()
	{
		return pointsMap;
	}

	
	/**
	 * Slope should be near 0 for normal operation
	 * @param pend_align  Horizontal slope of the detected alignment procedure
	 */
	public void setAlignmentSlope(double pend_align)
	{
		this.slope=pend_align;
	}

	/**
	 * Detected center of rotation
	 * @param detectedCenter
	 */
	public void setDetectedCenter(PagePoint detectedCenter)
	{
		this.detectedCenter=detectedCenter;
	}

	/**
	 * Difference between the expected center and the detected center in milimeters
	 * @param transX
	 * @param transY
	 */
	public void setDisplacementDelta(PagePoint delta)
	{
		this.delta=delta;
	}

	/**
	 * @return the slope
	 */
	public double getAlignmentSlope()
	{
		return slope;
	}

	/**
	 * @return the detectedCenter
	 */
	public PagePoint getDetectedCenter()
	{
		return detectedCenter;
	}


	public void setAlignmentTransform(AffineTransform transform)
	{
		this.transform=transform;
	}
	public AffineTransform getAlignmentTransform()
	{
		return transform;
	}

	public void setExpectedCenter(PagePoint expectedCenter)
	{
		this.expectedCenter=expectedCenter;
	}
	public PagePoint getExpectedCenter()
	{
		return expectedCenter;
	}

	public double getHorizontalResolution()
	{
		return horizRes;
	}

	public double getVerticalResolution()
	{
		return vertRes;
	}

	public void setHorizontalResolution(double horizRes2)
	{
		this.horizRes=horizRes2;
	}

	public void setVerticalResolution(double vertRes2)
	{
		this.vertRes=vertRes2;
	}

}
