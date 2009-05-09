/*
 * ProcessTest.java
 *
 * Creado en Marzo-Mayo de 2009
 *
 */

package org.uva.itast.blended.omr;

/**
 * 
 * @author Jes�s Rodilana
 *
 */
public class ProcessTest {
    
	/**
	 * M�todo main del programa
	 * 
	 * @param args
	 * @throws Exception
	 */
    public static void main(String[] args) throws Exception {
        TestManipulation procesartest = new TestManipulation();    			//se crea un objeto tipo TestManipulation para todo lo que tenga que ver con sus m�todos
        procesartest.leerLineaComandos(args);        						//se lee la l�nea de comandos
        procesartest.leerDefinitionfile(procesartest.getDefinitionfile());	//se lee el fichero con la descripci�n de las marcas
        procesartest.leerPaginas(procesartest.getInputPath());        		//se leen las p�ginas escaneadas
    }
}
