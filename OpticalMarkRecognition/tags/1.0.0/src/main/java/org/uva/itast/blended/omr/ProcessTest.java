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
		// se crea un objeto tipo TestManipulation para todo lo que tenga que ver con sus métodos
		OMRProcessor processor = new OMRProcessor();
		// se lee la línea de comandos
		processor.readCommandLine(args);
		// se lee el fichero con la descripción de las marcas
		processor.loadTemplate(processor.getDefinitionfile());
		// se leen las páginas escaneadas
		processor.processPath(processor.getInputPath());
	}
}
