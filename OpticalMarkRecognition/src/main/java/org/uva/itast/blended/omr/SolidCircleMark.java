/*
 *
 */

package org.uva.itast.blended.omr;


import java.awt.Color;
import java.awt.Graphics;
import java.awt.Polygon;
import java.awt.image.BufferedImage;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.uva.itast.blended.omr.pages.PageImage;


/**
 * @author Aaditeshwar Seth
 */
public class SolidCircleMark {
	/**
	 * 
	 */
	private static final double SIMILARITY_PERCENT = 0.5d;

	/**
	 * 
	 */
	private static final int SCAN_DELTA_DIVISOR = 4;
	/**
	 *  porcentaje del radio para el barrido que se da para buscar la marca
	 */
	private static final double SCAN_PERCENT = 0.5d;
	/**
	 * Logger for this class
	 */
	private static final Log logger = LogFactory.getLog(SolidCircleMark.class);

	// Gray8Image grayimage;
	BufferedImage image;
	public int markWidth;
	public int markHeight;
	double approxXscale, approxYscale;
	// Gray8Image template;
	BufferedImage template;

	int x, y;
	double maxsim;
	int maxsimX, maxsimY;

	private PageImage pageImage;

	private double autoSimilarity;

	/**
	 * 
	 * @param grayimage
	 * @param markWidth
	 *            in pixels
	 * @param markHeight
	 *            in pixels
	 * @param approxXscale
	 * @param approxYscale
	 */
	public SolidCircleMark(PageImage pageimage, int markWidth, int markHeight) {
		// this.grayimage = pageimage.getGrayImage();

		this.image = pageimage.getImagen();

		this.pageImage = pageimage;
		this.approxXscale = pageimage.getHorizontalRatio();
		this.approxYscale = pageimage.getVerticalRatio();
		this.markHeight = markHeight;
		this.markWidth = markWidth;

		template = new BufferedImage((int) (markWidth * 1.15) + 1,
				(int) (markHeight * 1.15) + 1, 1); // 10 = TYPE_BYTE_GRAY, 1 =
													// TYPE_INT_RGB
		fillTemplate(template, markWidth / 2, approxXscale / approxYscale);

		// template = new MemoryGray8Image((int) (markWidth * 1.15) + 1,(int)
		// (markHeight * 1.15) + 1);
		// fillTemplate(template, markWidth/2, approxXscale / approxYscale);

	}

	// private void fillTemplate(Gray8Image templateimg, int markradX,
	// double aspect)
	private void fillTemplate(BufferedImage templateimg, int markradX,
			double aspect) {
		double centerX = templateimg.getWidth() / 2;
		double centerY = templateimg.getHeight() / 2;
		int whites = 0;
		for (int i = 0; i < templateimg.getWidth(); i++) {
			for (int j = 0; j < templateimg.getHeight(); j++) {
				double dist = Math.sqrt((i - centerX) * (i - centerX)
						+ (j - centerY) / aspect * (j - centerY) / aspect);
				if (dist <= markradX) {
					// templateimg.putBlack(i, j);
					templateimg.setRGB(i, j, Color.BLACK.getRGB());

				} else {
					// templateimg.putWhite(i, j);
					templateimg.setRGB(i, j, Color.WHITE.getRGB());
					whites++;
				}
			}
		}

		// compute autoSimilarity
		autoSimilarity = ((double) whites)
				/ (templateimg.getHeight() * templateimg.getWidth());
	}

	/**
	 * 
	 * @param x
	 *            coords of the CENTER of the mark
	 * @param y
	 * @param dump
	 *            enable the dumping of pattern comparisons
	 * @return
	 */
	public boolean isMark(int x, int y, boolean dump) {
		maxsim = -1;
		maxsimX = 0;
		maxsimY = 0;
		// [JPC] this loop was refactored to start the analysis from the center
		int maxDeltaX = (int) (markWidth * SCAN_PERCENT);
		int maxDeltaY = (int) (markHeight * SCAN_PERCENT);
		int deltaXY = Math.max(1, markWidth / SCAN_DELTA_DIVISOR);
		boolean markpoint = true;// for debugging the position of the templates.
		for (int i = x; i <= x + maxDeltaX; i += deltaXY) {

			for (int j = y; j <= y + maxDeltaY; j += deltaXY) {
				// double similarity = 1.0 - ConcentricCircle.templateXOR(
				double similarity = 1.0 - BufferedImageUtil.templateXOR(image, i
						- template.getWidth() / 2,
						j - template.getHeight() / 2, template, dump);
				if (markpoint)
					markPointInImage(i, j);
				if (maxsim == -1 || maxsim < similarity) {
					maxsim = similarity;
					maxsimX = i;
					maxsimY = j;// + markradY * 2; // XXX
				}
				// similarity = 1.0 - ConcentricCircle.templateXOR(grayimage,
				similarity = 1.0 - BufferedImageUtil.templateXOR(image, 2 * x - i
						- template.getWidth() / 2, 2 * y - j
						- template.getHeight() / 2, template, dump);
				if (markpoint)
					markPointInImage(2 * x - i, 2 * y - j);
				if (maxsim == -1 || maxsim < similarity) {
					maxsim = similarity;
					maxsimX = 2 * x - i;
					maxsimY = 2 * y - j;// + markradY * 2; // XXX
				}

				// similarity = 1.0 - ConcentricCircle.templateXOR(grayimage,
				similarity = 1.0 - BufferedImageUtil.templateXOR(image, i - template.getWidth()
						/ 2, 2 * y - j - template.getHeight() / 2, template,
						dump);
				if (markpoint)
					markPointInImage(i, 2 * y - j);
				if (maxsim == -1 || maxsim < similarity) {
					maxsim = similarity;
					maxsimX = i;
					maxsimY = 2 * y - j;// + markradY * 2; // XXX
				}

				// similarity = 1.0 - ConcentricCircle.templateXOR(grayimage,
				similarity = 1.0 - BufferedImageUtil.templateXOR(image, 2 * x - i
						- template.getWidth() / 2,
						j - template.getHeight() / 2, template, dump);

				if (markpoint)
					markPointInImage(2 * x - i, j);
				if (maxsim == -1 || maxsim < similarity) {
					maxsim = similarity;
					maxsimX = 2 * x - i;
					maxsimY = j;
				}
			}
		}
		double threshold = getAutoSimilarity() * (1 + SIMILARITY_PERCENT);
		if (logger.isDebugEnabled()) {
			logger
					.debug("isMark(int, int) - --" + maxsim + " (threshold)" + threshold + ":" + maxsimX + "," + maxsimY + "->" + x + ":" + y); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
		}
		if (maxsim > threshold)
			return true;
		else
			return false;

	}

	/**
	 * When comparing with an empty space there are a minimum similarity due to
	 * empty space around the mark
	 * 
	 * @return
	 */
	private double getAutoSimilarity() {

		return autoSimilarity;
	}

	/**
	 * @param i
	 * @param j
	 */
	private void markPointInImage(int i, int j) {
		Graphics g = pageImage.getImagen().getGraphics();
		g.setColor(Color.WHITE);
		g.fillOval(i - 1, j - 1, 2, 2);
		// g.drawRect(i-w/2-1, j-h/2-1, w, h);
		g.setColor(Color.BLACK);
		g.drawOval(i - 1, j - 1, 2, 2);
		// g.drawRect(i-w/2, j-h/2, w, h);

	}

	// public void putMarkOnImage(Gray8Image markedImage)
	public void putMarkOnImage(BufferedImage markedImage) {
		BufferedImageUtil.putMarkBufferedImage(markedImage, maxsimX, maxsimY, true);
		BufferedImageUtil.putMarkBufferedImage(markedImage, maxsimX + 3, maxsimY + 3,
				true);
		BufferedImageUtil.putMarkBufferedImage(markedImage, maxsimX - 3, maxsimY + 3,
				true);
		BufferedImageUtil.putMarkBufferedImage(markedImage, maxsimX + 3, maxsimY - 3,
				true);
		BufferedImageUtil.putMarkBufferedImage(markedImage, maxsimX - 3, maxsimY - 3,
				true);
	}

	/**
	 * @param pageImage
	 */
	public void putCircleMarkOnImage(PageImage pageImage) {
		BufferedImage imagen = pageImage.getImagen();

		Graphics g = imagen.createGraphics();
		// int centerColor=imagen.getRGB(maxsimX, maxsimY);
		// g.setXORMode(new Color(centerColor));
		// g.setColor(Color.RED);
		// g.fillOval(maxsimX - markWidth/2, maxsimY - markHeight/2, markWidth,
		// markHeight);
		// g.setPaintMode();
		g.setColor(Color.RED);
		g.drawOval(maxsimX - markWidth / 2 - 1, maxsimY - markHeight / 2 - 1,
				markWidth + 1, markHeight + 1);
		g.drawLine(maxsimX, maxsimY - markHeight / 2 - 1, maxsimX, maxsimY
				- markHeight / 2 - 20);
		Polygon arrowHead = new Polygon();
		arrowHead.addPoint(maxsimX, maxsimY - markHeight / 2 - 1);
		arrowHead.addPoint(maxsimX - 6, maxsimY - markHeight / 2 - 10);
		arrowHead.addPoint(maxsimX + 6, maxsimY - markHeight / 2 - 10);
		g.fillPolygon(arrowHead);

	}
}
