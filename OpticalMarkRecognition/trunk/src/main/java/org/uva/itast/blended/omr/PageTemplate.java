/*
 * PaginaDefinicionMarcas.java
 *
 * Creado en Abril-Mayo de 2009
 *
 */

package org.uva.itast.blended.omr;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 
 * @author Juan Pablo de Castro
 * @author Jes�s Rodilana
 *
 */
public class PageTemplate {
	/**
	 * Logger for this class
	 */
	private static final Log			logger	= LogFactory.getLog(PageTemplate.class);
	
	private int numPagina;					//p�gina sobre la cu�l versa la informaci�n
	private Hashtable<String,Field> campos= new Hashtable<String, Field>();	//Hastable para almacenar los campos que leemos del fichero de definici�n de marcas
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
            	else									//lectura de campos de una l�nea
            	{
            		Field campo = new Field(line);
                    campos.put(campo.getNombre(), campo);
                	marcas.add(campo.getNombre());		//almacenamos en el array marcas[] la clave
            	}
            	in.mark(20);
            }
       
	}

	/**
	 * M�todo que devuelve el vector marcas, que contiene las claves de los campos
	 * @return marcas
	 */
	public Vector<String> getMarcas() {
		return marcas;
	}

	/**
	 * M�todo que devuelve el Hastable campos, que contiene los campos
	 * @return campos
	 */
	public Hashtable<String,Field> getCampos() {
		return campos;
	}
	
	/**
	 * M�todo para extraer el n�mero de una p�gina determinada de la plantilla
	 * @return numPagina
	 */
	public int getNumPagina() {
		return numPagina;
	}
}
