package org.uva.itast.blended.omr.pages;

import java.io.IOException;

import org.uva.itast.blended.omr.Field;

public interface AlignMarkDetector
{

	/**
	 * @return the bufferWidth
	 */
	public abstract int getBufferWidth();

	/**
	 * @param bufferWidth the bufferWidth to set
	 */
	public abstract void setBufferWidth(int bufferWidth);
/**
 * Slope detected measured as the ratio between delta x and delta y reduced to the first and fourth quadrants.
 * @return
 */
	public abstract double getAlignmentSlope();

	public abstract void setAlignmentSlope(double alignmentSlope);

	/**
	 * MÃ©todo que devolverï¿½ la posiciï¿½n de los cuatro puntos de alineaciï¿½n
	 * Uses the 
	 * @param pageImage 
	 * @return marcasalign (array de 4 Point2D con la posiciï¿½n de las cuatro marcas de alineaciï¿½n)
	 * @throws IOException 
	 */
	public abstract AlignmentResult align(PageImage pageImage);

	/**
	 * Calculates the detected position of the four corners of the alignment frame.
	 * @param campo
	 * @param pageImage
	 * @return bean with results of the alignment process {@link AlignmentResult}
	 */
	public abstract AlignmentResult searchAlignMarks(Field campo, PageImage pageImage);

}