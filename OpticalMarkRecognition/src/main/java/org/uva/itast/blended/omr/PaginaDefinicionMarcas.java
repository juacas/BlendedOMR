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
 * @author Jes�s Rodilana
 *
 */
public class PaginaDefinicionMarcas {
	
	private int numPagina;					//p�gina sobre la cu�l versa la informaci�n
	private Hashtable<String,Campo> campos= new Hashtable<String, Campo>();	//Hastable para almacenar los campos que leemos del fichero de definici�n de marcas
	private Vector<String> marcas= new Vector<String>();		//vector para buscar marcas, aqu� est�n almacenadas las keys
	
	/**
	 * Constructor de la clase PaginaDefinicionMarcas
	 * @param numerodepagina
	 */
	public PaginaDefinicionMarcas(int numerodepagina){
		this.numPagina = numerodepagina;
	}
	
	/**
	 * M�todo que lee las marcas de un objeto BufferedReader y las almacena en un objeto tipo Campo
	 * @param in
	 */
	public void leerMarcas(BufferedReader in){
		
		String line;
		try {
        	in.mark(20);		//marcamos para recordar la posici�n anterior donde termino la lectura de in
            while((line = in.readLine()) != null && !line.equals("") ) {
            	if(line.startsWith("[Page"))			//etiqueta de principio de p�gina
            	{
            		System.out.println("P�gina siguiente");
            		in.reset();
            		return;
            	}
            	else									//lectura de campos de una l�nea
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
	public Hashtable<String,Campo> getCampos() {
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
