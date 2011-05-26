/*
* ====================================================================
*
* License:        GNU General Public License
*
* Note: Original work copyright to respective authors
*
* This file is part of Blended (c) 2009-2010 University of Valladolid..
*
* Blended is free software; you can redistribute it and/or
* modify it under the terms of the GNU General Public License
* as published by the Free Software Foundation; either version 2
* of the License, or (at your option) any later version.
*
* Blended is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
*
*
* Module developed at the University of Valladolid http://www.eduvalab.uva.es
*
* http://www.itnt.uva.es , http://www.eduvalab.uva.es
*
* Designed and directed by Juan Pablo de Castro with 
* the effort of many other students of telecommunication 
* engineering.
* This module is provides as-is without any 
* guarantee. Use it as your own risk.
*
* @author Juan Pablo de Castro
* @author Jesus Rodilana
* @author María Jesús Verdú 
* @author Luisa Regueras 
* @author Elena Verdú
* 
* @license http://www.gnu.org/copyleft/gpl.html GNU Public License
* @package blended
 ***********************************************************************/

 

/***********************************************************************
 * Module developed at the University of Valladolid http://www.eduvalab.uva.es
 * Designed and directed by Juan Pablo de Castro with 
 * the effort of many other students of telecommunciation 
 * engineering this module is provides as-is without any 
 * guarantee. Use it as your own risk.
 *
 * @author Juan Pablo de Castro and Miguel Baraja Campesino and many others.
 * @license http://www.gnu.org/copyleft/gpl.html GNU Public License
 * @package blended
 ***********************************************************************/

package org.uva.itast.blended.omr;

import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import java.util.StringTokenizer;

public class Field {
	
	
    /* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		return this.nombre+" at ("+bbox.getX()+", "+bbox.getY()+")"+getValue()==null?"":"="+getValue();
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
    public static int CIRCLE=0;	
    public static int SQUARE=1;
    public static int CODEBAR=2;
    public static int FRAME=3;
	private boolean valid=true;
	
	/**
	 * Constructor de la clase Campo sin par�metros
	 *
	 */
	public Field(){
		 this.valor = "";
	}
	
	/**
	 * Constructor de la clase Campo, almacena los datos contenidos en line, seg�n sean de cada tipo de los que almacena Campo
	 * @param line
	 * @throws NullPointerException
	 */
    public Field(String line) throws NullPointerException{
        StringTokenizer st = new StringTokenizer(line, "[");
        nombre = st.nextToken();
        String sig = st.nextToken();
        StringTokenizer st2 = new StringTokenizer(sig, "[]=");
        String tipos = st2.nextToken();
        String coord = st2.nextToken();
        coords=coord.split(",");
        double coordenadas[]=new double[4];
        for(int i=0; i<coords.length; i++) coordenadas[i]=Double.parseDouble(coords[i]);
        Rectangle2D bbox=new Rectangle2D.Double(coordenadas[0],coordenadas[1],coordenadas[2],coordenadas[3]);
        setBBox(bbox);
        
        setValue("");
        
        if(tipos.equalsIgnoreCase("CIRCLE")) {
            this.tipo = CIRCLE;
        } else  if(tipos.equalsIgnoreCase("SQUARE")) {
            this.tipo = SQUARE;
        }else if(tipos.equalsIgnoreCase("CODEBAR")) {
            this.tipo = CODEBAR;
        }else if(tipos.equalsIgnoreCase("FRAME")) {
            this.tipo = FRAME;
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
	public String getName() {
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
