/**
 * 
 */
package org.uva.itast.blended.omr.scanners;


/**
 * @author juacas
 *
 */
public class ScanResult
{

	private String	scanner;
	private Object	result;

	/**
	 * @param string
	 * @param result
	 */
	public ScanResult(String scanner, Object result)
	{
		this.scanner=scanner;
		this.result=result;
	}

	/**
	 * @return the scanner
	 */
	public String getScanner()
	{
		return scanner;
	}

	/**
	 * @return the resulr
	 */
	public Object getResult()
	{
		return result;
	}

}
