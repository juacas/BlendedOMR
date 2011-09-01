/*
* ====================================================================
*
* License:        GNU General Public License
*
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
* @author María Jesús Verdú 
* @author Luisa Regueras 
* @author Elena Verdú
* 
* @license http://www.gnu.org/copyleft/gpl.html GNU Public License
* @package blended
 ***********************************************************************/

 
package org.uva.itast.blended.omr.scanners;

import java.awt.geom.Rectangle2D;

public class ScanResult
{

	private String	scanner;
	private Object	result;
	private Rectangle2D location=null;
	/**
	 * @return the location
	 */
	public Rectangle2D getLocation()
	{
		return location;
	}

	/**
	 * @param location the location to set
	 */
	public void setLocation(Rectangle2D location)
	{
		this.location=location;
	}

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
