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
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Vector;

public class OMRTemplate {
	
	// to identificate and index the templates with it
	String templateID;
	
	private Vector<PageTemplate> paginas=new Vector<PageTemplate>();	//instanciamos el vector paginas;

	private int selectedPageNumber;
	//cada elemento almacena un n�mero de p�gina y el contenido de dicha p�gina
	
	/**
	 * Constructor de la clase OMRTemplate, crea una plantilla a partir
	 * del n�mero de p�ginas y el definitionfile, adem�s crea una PaginaDefinicionMarcas
	 * por cada p�gina del definitonfile y las almacena en la plantilla
	 * @param definitionfile
	 * @throws IOException
	 */
	public OMRTemplate(String definitionfile) throws IOException
	{	
		FileInputStream inputStream=new FileInputStream(definitionfile);
		load(inputStream);
	}
	public OMRTemplate(InputStream inputStream) throws IOException
	{	
		load(inputStream);
	}
	/**
	 * @param inputStream
	 * @throws IOException
	 */
	private void load(InputStream inputStream) throws IOException
	{
		String line;
		BufferedReader in = new BufferedReader(new InputStreamReader(inputStream));
		while((line = in.readLine()) != null && !line.equals("")) {
				if (line.startsWith("TemplateId="))
				{
					String[] parts=line.split("=");
					this.templateID=parts[1];
				}
				else
            	if(line.startsWith("[Page"))            			//se identifica la p�gina
            	{
            		String num=line.substring(5,line.length()-1);	//Obtener i de [Pagei]
            		int numpag=Integer.parseInt(num);
            		PageTemplate pagina = new PageTemplate(numpag);		//se crea una nueva p�gina, par�metros: definitionfile e i, este �ltimo indica el n�mero de p�gina
        			pagina.leerMarcas(in);							//se leen las marcas
        			paginas.add(numpag-1, pagina);					//se guardan a partir del elemento 0 (numpag-1)
            	}
            }
		in.close();
	}
	
	/**
	 *  Devuelve una p�gina determinada, dada por i
	 *  @param pagina se numera empezando en 1
	 *  @return pagina.elementAt(pagina-1)
	 */
	public PageTemplate getPage(int pagina) {
		return paginas.elementAt(pagina-1);
	}
	PageTemplate getSelectedPage()
	{
		return getPage(getSelectedPageNumber());
	}
	/**
	 * Devuelve el vector paginas donde est�n almacenadas todas las p�ginas de una plantilla
	 * @return paginas
	 */
	public Vector<PageTemplate> getPaginas() {
		return paginas;
	}
	
	/**
	 * Devuelve el n�mero de paginas que tiene la plantilla
	 * @return paginas.size()
	 */
	public int getNumPaginas() {
		return paginas.size();
	}
	/**
	 * @return the templateID
	 */
	public String getTemplateID()
	{
		return templateID;
	}
	public void setSelectedPage(int pageNumber) 
	{
	this.selectedPageNumber=pageNumber;
	}
	public int getSelectedPageNumber()
	{
		return selectedPageNumber;
	}

	
}
