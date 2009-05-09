/*
 * Plantilla.java
 *
 * Creado en Abril-Mayo de 2009
 *
 */

package org.uva.itast.blended.omr;

import java.util.StringTokenizer;

/**
 * Almacena los distintos campos de un Campo de formulario
 * @author Jesús Rodilana
 *
 */
public class Campo {
	
	//XXX dos posibilidades
	//1 herencia que herede para ser barcode o circle
	//2 único campo de valor
	
    private String nombre;
    private int tipo, numeroPagina;
    private String[] coords;
    private double[] coordenadas=new double[4];
    private String valor;
    public static int CIRCLE=0;	//nos puede resultar más cómodo tratarlos como dos enteros, incluso podríamos valernos de boolean, aunque lo dejaremos así porque es más escalable
	public static int CODEBAR=1;
	private boolean valid=true;
	
	/**
	 * Constructor de la clase Campo sin parámetros
	 *
	 */
	public Campo(){
		 this.valor = "";
	}
	
	/**
	 * Constructor de la clase Campo, almacena los datos contenidos en line, según sean de cada tipo de los que almacena Campo
	 * @param line
	 * @throws NullPointerException
	 */
    public Campo(String line) throws NullPointerException{
        StringTokenizer st = new StringTokenizer(line, "[");
        nombre = st.nextToken();
        String sig = st.nextToken();
        StringTokenizer st2 = new StringTokenizer(sig, "[]=");
        String tipos = st2.nextToken();
        String coord = st2.nextToken();
        coords=coord.split(",");
        
        for(int i=0; i<coords.length; i++) coordenadas[i]=Double.parseDouble(coords[i]);
        
        setValue("");
        
        if(tipos.equalsIgnoreCase("CIRCLE")) {
            this.tipo = CIRCLE;
        } else if(tipos.equalsIgnoreCase("CODEBAR")) {
            this.tipo = CODEBAR;
        }
        else
        	throw new IllegalArgumentException("Field type unsupported in:\""+ line+"\"");
    }
    
    /**
     * Devuelve el nombre del campo
     * @return nombre
     */
	public String getNombre() {
        return nombre;
    }
    
	/**
	 * Devuelve el tipo del campo
	 * @return tipo
	 */
	public int getTipo() {
        return tipo;
    }
	
	/**
	 * Devuelve el numero de página a la que pertenece el campo
	 * @return numeroPagina
	 */
	public int getNumPag() {
        return numeroPagina;
    }
    
	/**
	 * Devuelve las coordenadas del campo
	 * @return coordenadas
	 */
    public double[] getCoordenadas() {
        return coordenadas;
    }
    
	/**
	 * Devuelve el valor de barcode
	 * @return barcode
	 */
	public String getValue() {
		return valor;
	}

	/**
	 * Selecciona el valor de barcode
	 * @param barcode
	 */
	public void setValue(String value) {
		this.valor = value;
	}

	/**
	 * Devuelve el valor de valid
	 * @return valid
	 */
	public boolean isValid() {
		return valid;
	}

	/**
	 * Selecciona el valor de valid
	 * @param valid
	 */
	public void setValid(boolean valid) {
		this.valid = valid;
	}
}
