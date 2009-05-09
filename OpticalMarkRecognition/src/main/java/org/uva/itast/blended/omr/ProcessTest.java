/*
 * ProcessTest.java
 *
 * Creado en Marzo-Mayo de 2009
 *
 */

package org.uva.itast.blended.omr;

/**
 * 
 * @author Jesús Rodilana
 *
 */
public class ProcessTest {
    
	/**
	 * Método main del programa
	 * 
	 * @param args
	 * @throws Exception
	 */
    public static void main(String[] args) throws Exception {
        TestManipulation procesartest = new TestManipulation();    			//se crea un objeto tipo TestManipulation para todo lo que tenga que ver con sus métodos
        procesartest.leerLineaComandos(args);        						//se lee la línea de comandos
        procesartest.leerDefinitionfile(procesartest.getDefinitionfile());	//se lee el fichero con la descripción de las marcas
        procesartest.leerPaginas(procesartest.getInputPath());        		//se leen las páginas escaneadas
    }
}
