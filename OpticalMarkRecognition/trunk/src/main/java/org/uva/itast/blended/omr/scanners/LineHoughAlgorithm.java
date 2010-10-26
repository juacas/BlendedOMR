package org.uva.itast.blended.omr.scanners;

import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferInt;
import java.awt.image.Raster;
import java.awt.image.SampleModel;
import java.awt.image.SinglePixelPackedSampleModel;
import java.awt.image.WritableRaster;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.uva.itast.blended.omr.pages.SubImage;

public class LineHoughAlgorithm
{
	/**
	 * Logger for this class
	 */
	private static final Log	logger		=LogFactory.getLog(LineHoughAlgorithm.class);

	int[]						input;

	float[]						template	= { -1, 0, 1, -2, 0, 2, -1, 0, 1 };				;
	double						progress;
	int							width;
	int							height;
	int[]						acc;
	int							accSize		=30;
	int[]						results;

	public void lineHough()
	{
		progress=0;
	}

	public void init(int[] inputIn, int widthIn, int heightIn)
	{
		width=widthIn;
		height=heightIn;
		input=inputIn;
	}

	public void setLines(int lines)
	{
		accSize=lines;
	}

	// hough transform for lines (polar), returns the accumulator array
	public void process()
	{

		// for polar we need accumulator of 180degress * the longest length in
		// the image
		int rmax=(int) Math.sqrt(width * width + height * height);
		acc=new int[rmax * 180];
		int r;
		progress=0;

		for (int x=0; x < width; x++)
		{
			progress+=0.5;
			for (int y=0; y < height; y++)
			{

				if ((input[y * width + x] & 0xff) == 255)
				{

					for (int theta=0; theta < 180; theta++)
					{
						r=(int) (x * Math.cos(((theta) * Math.PI) / 180) + y * Math.sin(((theta) * Math.PI) / 180));
						if ((r > 0) && (r <= rmax))
							acc[r * 180 + theta]=acc[r * 180 + theta] + 1;
					}
				}
			}
		}

		// now normalise to 255 and put in format for a pixel array
		int max=0;

		// Find max acc value
		for (r=0; r < rmax; r++)
		{
			for (int theta=0; theta < 180; theta++)
			{

				if (acc[r * 180 + theta] > max)
				{
					// System.out.println("Value :" + acc[r*180+theta] + " " +
					// theta);
					max=acc[r * 180 + theta];
				}
			}
		}

		// System.out.println("Max :" + max);

		// Normalise all the values
		int value;
		for (r=0; r < rmax; r++)
		{
			for (int theta=0; theta < 180; theta++)
			{

				value=(int) (((double) acc[r * 180 + theta] / (double) max) * 255.0);
				acc[r * 180 + theta]=0xff000000 | (value << 16 | value << 8 | value);
			}
		}

		// accSize=rmax;
		findMaxima();

		if (logger.isDebugEnabled())
		{
			logger.debug("process() - done"); //$NON-NLS-1$
		}
	}

	private int[] findMaxima()
	{

		// for polar we need accumulator of 180degress * the longest length in
		// the image
		int rmax=(int) Math.sqrt(width * width + height * height);
		results=new int[accSize * 3];
		int[] output=new int[width * height];

		for (int r=0; r < rmax; r++)
		{
			for (int theta=0; theta < 180; theta++)
			{
				int value=(acc[r * 180 + theta] & 0xff);

				// if its higher than lowest value add it and then sort
				if (value > results[(accSize - 1) * 3])
				{

					// add to bottom of array
					results[(accSize - 1) * 3]=value;
					results[(accSize - 1) * 3 + 1]=r;
					results[(accSize - 1) * 3 + 2]=theta;

					// shift up until its in right place
					int i=(accSize - 2) * 3;
					while ((i >= 0) && (results[i + 3] > results[i]))
					{
						for (int j=0; j < 3; j++)
						{
							int temp=results[i + j];
							results[i + j]=results[i + 3 + j];
							results[i + 3 + j]=temp;
						}
						i=i - 3;
						if (i < 0)
							break;
					}
				}
			}
		}

		double ratio=(double) (width / 2) / accSize;
		if (logger.isDebugEnabled())
		{
			logger.debug("findMaxima() - top " + accSize + " matches:"); //$NON-NLS-1$ //$NON-NLS-2$
		}

		return output;
	}

	public int[] getAcc()
	{

		return acc;
	}

	public int getProgress()
	{
		return (int) progress;
	}

	/**
	 * 
	 * @return array value, r, theta
	 */
	public HoughResult[] getResults()
	{
		if (logger.isDebugEnabled())
		{
			for (int i=accSize - 1; i >= 0; i--)
			{

				logger.debug("findMaxima() - value: " + results[i * 3] + ", r: " + results[i * 3 + 1] + ", theta: " + results[i * 3 + 2]); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			}

		}
		HoughResult[] resultsRads=new HoughResult[results.length / 3];
		for (int i=0; i < results.length; i+=3)
		{
			resultsRads[i / 3]=new HoughResult(results[i], results[i + 1], results[i + 2] * Math.PI / 180);
		}
		return resultsRads;
	}

	public void labelSubImage(SubImage subimage)
	{
		for (int i=accSize - 1; i >= 0; i--)
		{
			// System.out.println("value: " + results[i*3] + ", r: " +
			// results[i*3+1] + ", theta: " + results[i*3+2]);
			drawPolarLine(results[i * 3], results[i * 3 + 1], results[i * 3 + 2],subimage);
		}

	}
public BufferedImage getAccImage()
{
	int w=180;
	int h=acc.length/w;
	DataBuffer buffer=new DataBufferInt(acc,acc.length);
	int []bitmasks=new int[]{ 0xFF0000,	0xFF00, 0xFF};
	SampleModel sm=new SinglePixelPackedSampleModel(DataBuffer.TYPE_INT,w,h,bitmasks);
 	WritableRaster raster=Raster.createWritableRaster(sm, buffer, null);
   
    BufferedImage bufferedImage=new BufferedImage(w,h,BufferedImage.TYPE_INT_RGB);
    bufferedImage.setData(raster);
	return bufferedImage;
}
	private void drawPolarLine(int value, int r, int theta, SubImage image)
	{
		// draw a line given polar coordinates (and an input image to allow
		// drawing more than one line)

		for (int x=0; x < width; x++)
		{

			for (int y=0; y < height; y++)
			{

				int temp=(int) (x * Math.cos(((theta) * Math.PI) / 180) + y * Math.sin(((theta) * Math.PI) / 180));
				if ((temp - r) == 0)
					image.setRGB(x, y, value<<8|value<<16);
			}
		}

	}

}