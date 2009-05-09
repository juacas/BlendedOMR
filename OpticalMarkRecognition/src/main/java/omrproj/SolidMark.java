/*
 * Mark.java
 *
 * Created on June 30, 2007, 5:56 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package omrproj;

//import net.sourceforge.jiu.codecs.*;
import net.sourceforge.jiu.data.*;

//import net.sourceforge.jiu.color.reduction.*;
//import net.sourceforge.jiu.filters.*;
//import net.sourceforge.jiu.geometry.*;

/**
 * @author Aaditeshwar Seth
 */
public class SolidMark
{
	/**
	 * 
	 */
	private static final double	SCAN_PERCENT	= 0.5;	// porcentaje del radio
														// para el barrido que
														// se da para buscar la
														// marca
	Gray8Image					grayimage;
	public int					markradX;
	public int							markradY;
	double						approxXscale, approxYscale;
	Gray8Image					template;

	int							x, y;
	double						maxsim;
	int							maxsimX, maxsimY;

	public SolidMark(Gray8Image grayimage, double approxXscale,
			double approxYscale)
	{
		this.grayimage = grayimage;
		this.approxXscale = approxXscale;
		this.approxYscale = approxYscale;
		// markradX = (int)(ConcentricCircle.markDiam / 2 * approxXscale);
		// markradY = (int)(ConcentricCircle.markDiam / 2 * approxYscale);

		// TODO: Estaba usando 0!!
		markradX = 5;// XXX utilizar los leídos en el archivo
		markradY = 5;

		template = new MemoryGray8Image((int) (markradX * 2 * 1.15) + 1,
				(int) (markradY * 2 * 1.15) + 1);
		fillTemplate(template, markradX, approxXscale / approxYscale);
		ImageUtil.saveImage(template, "c:\\temp\\sys\\marktemplate.png");
	}

	private void fillTemplate(Gray8Image templateimg, int markradX,
			double aspect)
	{
		double centerX = templateimg.getWidth() / 2;
		double centerY = templateimg.getHeight() / 2;
		for (int i = 0; i < templateimg.getWidth(); i++)
		{
			for (int j = 0; j < templateimg.getHeight(); j++)
			{
				double dist = Math.sqrt((i - centerX) * (i - centerX)
						+ (j - centerY) / aspect * (j - centerY) / aspect);
				if (dist <= markradX)
				{
					templateimg.putBlack(i, j);
				} else
				{
					templateimg.putWhite(i, j);
				}
			}
		}
	}

	public boolean isMark(int x, int y)
	{
		maxsim = -1;
		maxsimX = 0;
		maxsimY = 0;
		if (false) // do not scan
		{
			double similarity = 1.0 - ConcentricCircle.templateXOR(grayimage,
					x, y, template, false);
			System.out.println("--" + similarity + "->" + x + ":" + y);
			if (similarity > 0.55)
			{
				return true;
			}
			return false;
		} else
		{
			for (int i = x - (int) (markradX * SCAN_PERCENT); i <= x
					+ (int) (markradX * SCAN_PERCENT); i += Math.max(1,
					markradX / 5))
			{
				for (int j = y - (int) (markradY * SCAN_PERCENT); j <= y
						+ (int) (markradY * SCAN_PERCENT); j += Math.max(1,
						markradX / 5))
				{
					double similarity = 1.0 - ConcentricCircle
							.templateXOR(grayimage,
									i - template.getWidth() / 2, j
											- template.getHeight() / 2,
									template, false);
					if (maxsim == -1 || maxsim < similarity)
					{
						maxsim = similarity;
						maxsimX = i;
						maxsimY = j;// + markradY * 2; // XXX
					}
				}
			}

			System.out.println("--" + maxsim + ":" + maxsimX + "," + maxsimY
					+ "->" + x + ":" + y);
			if (maxsim > 0.55)
				return true;
			else
				return false;
		}
	}

	public void putMarkOnImage(Gray8Image markedImage)
	{
		ImageUtil.putMark(markedImage, maxsimX, maxsimY, true);
		ImageUtil.putMark(markedImage, maxsimX + 3, maxsimY + 3, true);
		ImageUtil.putMark(markedImage, maxsimX - 3, maxsimY + 3, true);
		ImageUtil.putMark(markedImage, maxsimX + 3, maxsimY - 3, true);
		ImageUtil.putMark(markedImage, maxsimX - 3, maxsimY - 3, true);
	}

}
