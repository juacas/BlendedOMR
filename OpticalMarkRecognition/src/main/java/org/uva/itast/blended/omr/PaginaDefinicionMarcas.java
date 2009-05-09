/*
 * Plantilla.java
 *
 * Creado en Abril-Mayo de 2009
 *
 */

package org.uva.itast.blended.omr;

import java.io.BufferedReader;
import java.util.Hashtable;
import java.util.Vector;

/**
 * 
 * @author Jesús Rodilana
 *
 */
public class PaginaDefinicionMarcas {
	
	private int numPagina;					//página sobre la cuál versa la información
	private Hashtable<String,Campo> campos= new Hashtable<String, Campo>();	//Hastable para almacenar los campos que leemos del fichero de definición de marcas
	private Vector<String> marcas= new Vector<String>();		//vector para buscar marcas, aquí están almacenadas las keys
	
	/**
	 * Constructor de la clase PaginaDefinicionMarcas
	 * @param numerodepagina
	 */
	public PaginaDefinicionMarcas(int numerodepagina){
		this.numPagina = numerodepagina;
	}
	
	/**
	 * Método que lee las marcas de un objeto BufferedReader y las almacena en un objeto tipo Campo
	 * @param in
	 */
	public void leerMarcas(BufferedReader in){
		
		String line;
		try {
        	in.mark(20);		//marcamos para recordar la posición anterior donde termino la lectura de in
            while((line = in.readLine()) != null && !line.equals("") ) {
            	if(line.startsWith("[Page"))			//etiqueta de principio de página
            	{
            		System.out.println("Página siguiente");
            		in.reset();
            		return;
            	}
            	else									//lectura de campos de una línea
            	{
            		Campo campo = new Campo(line);
                    campos.put(campo.getNombre(), campo);
                	marcas.add(campo.getNombre());		//almacenamos en el array marcas[] la clave
            	}
            	in.mark(20);
            }
        } catch(Exception ex) {
            ex.printStackTrace(System.out);
        }
	}

	/**
	 * Método que devuelve el vector marcas, que contiene las claves de los campos
	 * @return marcas
	 */
	public Vector<String> getMarcas() {
		return marcas;
	}

	/**
	 * Método que devuelve el Hastable campos, que contiene los campos
	 * @return campos
	 */
	public Hashtable<String,Campo> getCampos() {
		return campos;
	}
	
	/**
	 * Método para extraer el número de una página determinada de la plantilla
	 * @return numPagina
	 */
	public int getNumPagina() {
		return numPagina;
	}
}
