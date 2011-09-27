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
* @license http://www.gnu.org/copyleft/gpl.html GNU Public License
* @package blended
 ***********************************************************************/

 

/***********************************************************************
 * Module developed at the University of Valladolid http://www.eduvalab.uva.es
 * Designed and directed by Juan Pablo de Castro with 
 * the effort of many other students of telecommunciation 
 * engineering this module is provides as-is without any 
 * guarantee. Use it as your own risk.
 *
 * @author Juan Pablo de Castro and Miguel Baraja Campesino and many others.
 * @license http://www.gnu.org/copyleft/gpl.html GNU Public License
 * @package blended
 ***********************************************************************/

package org.uva.itast.blended.omr;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class PageTemplate {
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		return "PageTemplate [numPagina=" + numPagina + ", fields=" + fields + "]";
	}

	/**
	 * Logger for this class
	 */
	private static final Log			logger	= LogFactory.getLog(PageTemplate.class);
	
	private int numPagina;					//p�gina sobre la cu�l versa la informaci�n
	private Hashtable<String,Field> fields= new Hashtable<String, Field>();	//Hastable para almacenar los fields que leemos del fichero de definici�n de marcas
	private Vector<String> marcas= new Vector<String>();		//vector para buscar marcas, aqu� est�n almacenadas las keys
	
	/**
	 * Constructor de la clase PaginaDefinicionMarcas
	 * @param numerodepagina
	 */
	public PageTemplate(int numerodepagina){
		this.numPagina = numerodepagina;
	}
	
	/**
	 * M�todo que lee las marcas de un objeto BufferedReader y las almacena en un objeto tipo Campo
	 * @param in
	 * @throws IOException 
	 */
	public void leerMarcas(BufferedReader in) throws IOException{
		
		String line;
		
        	in.mark(20);		//marcamos para recordar la posici�n anterior donde termino la lectura de in
            while((line = in.readLine()) != null && !line.equals("") ) {
            	if(line.startsWith("[Page"))			//etiqueta de principio de p�gina
            	{
				if (logger.isDebugEnabled())
				{
					logger.debug("leerMarcas(BufferedReader) - P�gina siguiente"); //$NON-NLS-1$
				}
            		in.reset();
            		return;
            	}
            	else									//lectura de fields de una l�nea
            	{
            		Field campo = new Field(line);
                    fields.put(campo.getName(), campo);
                	marcas.add(campo.getName());		//almacenamos en el array marcas[] la clave
            	}
            	in.mark(20);
            }
       
	}

	/**
	 * M�todo que devuelve el vector marcas, que contiene las claves de los fields
	 * @return marcas
	 */
	public Vector<String> getMarks() {
		return marcas;
	}

	/**
	 * M�todo que devuelve el Hastable fields, que contiene los fields
	 * @return fields
	 */
	public Hashtable<String,Field> getFields() {
		return fields;
	}
	
	/**
	 * M�todo para extraer el n�mero de una p�gina determinada de la plantilla
	 * @return numPagina
	 */
	public int getPageNumber() {
		return numPagina;
	}
}
