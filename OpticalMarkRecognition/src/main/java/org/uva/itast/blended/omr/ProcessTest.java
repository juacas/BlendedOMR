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
        OMRProcessor processor = new OMRProcessor();    			//se crea un objeto tipo TestManipulation para todo lo que tenga que ver con sus m�todos
        processor.readCommandLine(args);        						//se lee la l�nea de comandos
        processor.loadTemplate(processor.getDefinitionfile());	//se lee el fichero con la descripci�n de las marcas
        processor.processPath(processor.getInputPath());        		//se leen las p�ginas escaneadas
    }
}
