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


package org.uva.itast.blended.omr;

public class OmrCommand {

	/**
	 * M�todo main del programa
	 * Process the command line as described in {@link OMRProcessor#readCommandLine(String[])}
	 * The arguments are: 
	 * {@value OMRProcessor#CMD_USAGE}
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		
		// se crea un objeto tipo TestManipulation para todo lo que tenga que ver con sus m�todos
		OMRProcessor processor = new OMRProcessor();
		// se lee la l�nea de comandos
		try
		{
			processor.readCommandLine(args);
			// se lee el fichero con la descripci�n de las marcas
			processor.loadTemplate(processor.getDefinitionfile());
			// se leen las p�ginas escaneadas
			processor.processPath(processor.getInputPath());
		}
		catch (IllegalArgumentException e)
		{
			System.out.println("OmrCommand bad arguments: "+e.getMessage());
			System.out.println(OMRProcessor.CMD_USAGE);
		}
	}
}
