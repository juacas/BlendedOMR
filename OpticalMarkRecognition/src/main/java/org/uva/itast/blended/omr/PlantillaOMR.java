/*
 * PlantillaOMR.java
 *
 * Creado en Abril-Mayo de 2009
 *
 */

package org.uva.itast.blended.omr;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Vector;

/**
 * 
 * @author Jes�s Rodilana
 *
 */
public class PlantillaOMR {
	
	
	private Vector<PaginaDefinicionMarcas> paginas;		//cada elemento almacena un n�mero de p�gina y el contenido de dicha p�gina
	
	/**
	 * Constructor de la clase PlantillaOMR, crea una plantilla a partir
	 * del n�mero de p�ginas y el definitionfile, adem�s crea una PaginaDefinicionMarcas
	 * por cada p�gina del definitonfile y las almacena en la plantilla
	 * @param definitionfile
	 * @throws IOException
	 */
	public PlantillaOMR(String definitionfile) throws IOException
	{
		
		paginas = new Vector<PaginaDefinicionMarcas>();	//instanciamos el vector paginas
		String line;
		
		BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(definitionfile)));
		while((line = in.readLine()) != null && !line.equals("")) {
            	if(line.startsWith("[Page"))            			//se identifica la p�gina
            	{
            		String num=line.substring(5,line.length()-1);	//Obtener i de [Pagei]
            		int numpag=Integer.parseInt(num);
            		PaginaDefinicionMarcas pagina = new PaginaDefinicionMarcas(numpag);		//se crea una nueva p�gina, par�metros: definitionfile e i, este �ltimo indica el n�mero de p�gina
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
	public PaginaDefinicionMarcas getPagina(int pagina) {
		return paginas.elementAt(pagina-1);
	}
	
	/**
	 * Devuelve el vector paginas donde est�n almacenadas todas las p�ginas de una plantilla
	 * @return paginas
	 */
	public Vector<PaginaDefinicionMarcas> getPaginas() {
		return paginas;
	}
	
	/**
	 * Devuelve el n�mero de paginas que tiene la plantilla
	 * @return paginas.size()
	 */
	public int getNumPaginas() {
		return paginas.size();
	}
}
