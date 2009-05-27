/*
 * BuffereImageImageUtil.java
 * 
 * Creado en Mayo de 2009
 *
 */

package org.uva.itast.blended.omr;

import java.awt.Color;
import java.awt.image.BufferedImage;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 *
 * @author Jesús Rodilana
 */
public class BufferedImageUtil {
	
	/**
	 * Logger for this class
	 */
	private static final Log logger = LogFactory.getLog(BufferedImageUtil.class);
	
	/**
	 * 
	 * @param img
	 * @param x
	 * @param y
	 * @param template
	 * @param dump
	 * @return
	 */
	public static double templateXOR(BufferedImage img, int x, int y,
			BufferedImage template, boolean dump) {
		int diff = 0, total = 0;

		int templateHeight = template.getHeight();
		int templateWidth = template.getWidth();
		int imgHeight = img.getHeight();
		int imgWidth = img.getWidth();
		if (dump && logger.isDebugEnabled()) 
		{
			logger
					.debug("templateXOR - Testing XOR Width=" + templateWidth + "from upper-left x=" + x + ", y=" + y); //$NON-NLS-1$ //$NON-NLS-2$

			for (int j = y; j < y + templateHeight && j < imgHeight; j++) {
				for (int i = x; i < x + templateWidth && i < imgWidth; i++) 
				{
					float luminance = getLuminance(img, j, i);
					boolean isblack = (luminance < 0.75 ? true : false);
					boolean tempIsBlack = isBlack(template, j - y, i - x);
					System.out.print(isblack ? "*" : (tempIsBlack ? "O" : "_"));
				}
				System.out.println("<-");
			}
		}//  end debug
		
		for (int j = y; j < y + templateHeight && j < imgHeight; j++) {
			for (int i = x; i < x + templateWidth && i < imgWidth; i++) {
				float luminance = getLuminance(img, j, i);
				boolean isblack = (luminance < 0.75 ? true : false);
				boolean tempIsWhite = isWhite(template, j - y, i - x);
				boolean tempIsBlack = isBlack(template, j - y, i - x);

				if ((isblack & tempIsWhite) | ((!isblack) & tempIsBlack)) {
					diff++;
				}
				total++;
			}

		}
		if (dump && logger.isDebugEnabled()) {
			logger.debug("templateXOR- Diffs=" + diff + " out of " + total); //$NON-NLS-1$ //$NON-NLS-2$
		}
		return ((double) diff) / total;
	}

	/**
	 * Determina si un determinado píxel es blanco o no
	 * @param template
	 * @param j
	 * @param i
	 * @return
	 */
	private static boolean isWhite(BufferedImage template, int j, int i) {
		return (template.getRGB(i, j) == Color.WHITE.getRGB() ? true : false);
	}

	/**
	 * Determina si un determinado píxel es negro o no
	 * @param template
	 * @param j
	 * @param i
	 * @return
	 */
	private static boolean isBlack(BufferedImage template, int j, int i) {
		return (template.getRGB(i, j) == Color.BLACK.getRGB() ? true : false);
	}

	/**
	 * Obtiene la luminancia de un pixel
	 * @param img
	 * @param j
	 * @param i
	 * @return
	 */
	private static float getLuminance(BufferedImage img, int j, int i) {
		int rgb = img.getRGB(i, j);
		float red = ((rgb & 0xff0000) >> 16) / 255.f;
		float green = ((rgb & 0xff00) >> 8) / 255.f;
		float blue = (rgb & 0xff) / 255.f;
		float luminance = (float) ((.299 * red) + (.587 * green) + (.114 * blue));
		return luminance;
	}

	/**
	 * Dibuja de una marca de color blanco o negro en un BufferedImage
	 * @param img
	 * @param x
	 * @param y
	 * @param color
	 */
	public static void putMarkBufferedImage(BufferedImage img, int x, int y, boolean color) {
        if(color) {
        	img.setRGB(x, y, Color.BLACK.getRGB());
        	img.setRGB(x + 1, y + 1, Color.BLACK.getRGB());
        	img.setRGB(x - 1, y - 1, Color.BLACK.getRGB());
        	img.setRGB(x + 1, y, Color.BLACK.getRGB());
        	img.setRGB(x - 1, y, Color.BLACK.getRGB());
        	img.setRGB(x, y + 1, Color.BLACK.getRGB());
        	img.setRGB(x, y - 1, Color.BLACK.getRGB());
        	img.setRGB(x + 1, y - 1, Color.BLACK.getRGB());
        	img.setRGB(x - 1, y + 1, Color.BLACK.getRGB());        
        	img.setRGB(x - 1, y - 1, Color.BLACK.getRGB());
        } else {
        	img.setRGB(x, y, Color.WHITE.getRGB());
        	img.setRGB(x + 1, y + 1, Color.WHITE.getRGB());
        	img.setRGB(x - 1, y - 1, Color.WHITE.getRGB());
        	img.setRGB(x + 1, y, Color.WHITE.getRGB());
        	img.setRGB(x - 1, y, Color.WHITE.getRGB());
        	img.setRGB(x, y + 1, Color.WHITE.getRGB());
        	img.setRGB(x, y - 1, Color.WHITE.getRGB());
        	img.setRGB(x + 1, y - 1, Color.WHITE.getRGB());
        	img.setRGB(x - 1, y + 1, Color.WHITE.getRGB());        
        	img.setRGB(x - 1, y - 1, Color.WHITE.getRGB());
        }
		
	}
}
