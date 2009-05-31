/**
 * 
 */
package org.uva.itast.blended.omr.scanners;

import com.google.zxing.ReaderException;

/**
 * @author juacas
 *
 */
public class MarkScannerException extends Exception
{

	/**
	 * @param e
	 */
	public MarkScannerException(ReaderException e)
	{
		super(e);
	}

}
