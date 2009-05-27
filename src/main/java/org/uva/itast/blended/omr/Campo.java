/*
 * Campo.java
 *
 * Creado en Abril-Mayo de 2009
 *
 */

package org.uva.itast.blended.omr;

import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import java.util.StringTokenizer;

/**
 * Almacena los distintos campos de un Campo de formulario
 * @author Jes�s Rodilana
 *
 */
public class Campo {
	
	
    /* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		
		return this.nombre+" at ("+bbox.getX()+", "+bbox.getY()+")";
	}

	private String nombre;
    private int tipo, numeroPagina;
    private String[] coords;
    private Rectangle2D bbox; //BoundingBox in milimeters
    
    /**
     * BoundingBox in milimeters
	 * @return the bbox
	 */
	public Rectangle2D getBBox()
	{
		return bbox;
	}

	private String valor;
    public static int CIRCLE=0;	//nos puede resultar m�s c�modo tratarlos como dos enteros, incluso podr�amos valernos de boolean, aunque lo dejaremos as� porque es m�s escalable
	public static int CODEBAR=1;
	private boolean valid=true;
	
	/**
	 * Constructor de la clase Campo sin par�metros
	 *
	 */
	public Campo(){
		 this.valor = "";
	}
	
	/**
	 * Constructor de la clase Campo, almacena los datos contenidos en line, seg�n sean de cada tipo de los que almacena Campo
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
        double coordenadas[]=new double[4];
        for(int i=0; i<coords.length; i++) coordenadas[i]=Double.parseDouble(coords[i]);
        Rectangle2D bbox=new Rectangle();
        bbox.setFrame(coordenadas[0],coordenadas[1],coordenadas[2],coordenadas[3]);
        setBBox(bbox);
        
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
     * BoundingBox in milimeters
	 * @param bbox
	 */
	public void setBBox(Rectangle2D bbox)
	{
		this.bbox=bbox;
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
	 * Devuelve el numero de p�gina a la que pertenece el campo
	 * @return numeroPagina
	 */
	public int getNumPag() {
        return numeroPagina;
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
