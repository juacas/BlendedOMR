/*
 * TestManipulation.java
 *
 * Creado en Marzo-Mayo de 2009
 *
 */

package org.uva.itast.blended.omr;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Hashtable;
import java.util.Vector;

/**
 * @author Jes�s Rodilana
 */
public class TestManipulation
{
	/**
	 * Logger for this class
	 */
	private static final Log	logger				= LogFactory.getLog(TestManipulation.class);

	public static final int	_IMAGEHEIGTHPIXEL	= 2339/2;	// valor en p�xeles
															// de la altura de
															// la im�gen con la
															// que se trabaja en
															// el programa
	public static final int	_IMAGEWIDTHPIXEL	= 1700/2;	// valor en p�xeles
															// de la anchura de
															// la im�gen con la
															// que se trabaja en
															// el programa

	private String			arg;
	private char			flag;
	private boolean			vflag				= false;
	private String			inputPath;						// fichero o
															// directorio a
															// procesar
	private String			outputdir;						// directorio donde
															// se alojaran los
															// resultados
	private String			userid;						// identificador que
															// se utilizar� para
															// marcar los
															// ficheros
	private String			activitycode;					// identificador que
															// contiene el
															// n�mero de
															// documento que
															// corresponde con
															// una actividad o
															// cuestionario de
															// Moodle. El �ltimo
															// car�cter recoge
															// el n�mero de
															// p�gina en caso de
															// haber m�s de una,
															// p�gina
	private String			definitionfile;				// fichero con la
															// descripci�n de
															// las marcas
	private boolean			autoalign			= false;	// bandera para la
															// opci�n de
															// alineado
	private boolean			medianfilter		= false;	// bandera para la
															// opci�n de
															// alineado
	private boolean			dflag				= false;	// marcador para el
															// campo obligatorio
															// -d
	private int				numeropaginas		= 0;		// el n�mero de
															// p�gina por
															// defecto ser� 0
	PlantillaOMR			plantilla;						// plantilla para
															// almacenar las
															// p�ginas y los
															// campos de
															// definition file

	/**
	 * Constructor TestManipulation sin par�metros.
	 */
	public TestManipulation()
	{
	}

	/**
	 * M�todo que lee la l�nea de comandos. Identifica que las opciones y
	 * par�metros sean correctos y los almacena. uso: blended_omr [-i inputdir]
	 * [-o outputdir] [-id1 USERID] [-id2 ACTIVITYCODE] [-a] [-f] -d
	 * definitionfiles -a indica que hay que alinear la p�gina -f indica que hay
	 * que filtrar los campos (para im�genes de mala calidad)
	 */
	public void leerLineaComandos(String[] args)
	{
		int i = 0, j;

		// detectamos todas las opciones (s�mbolo "-" delante)
		while (i < args.length && args[i].startsWith("-"))
		{
			vflag = true;
			arg = args[i++];

			// opciones que requieren argumentos
			// opci�n -i
			if (arg.equals("-i"))
			{
				if (i < args.length)
					setInputPath(args[i++]);
				else
					System.err.println("-i requiere un path");
				if (vflag)
					;
				// System.out.println("inputdir = " + inputdir);
			}
			// opci�n -o
			else if (arg.equals("-o"))
			{
				if (i < args.length)
					setOutputdir(args[i++]);
				else
					System.err.println("-o requiere un path");
				if (vflag)
					;
				// System.out.println("outputdir = " + outputdir);
			}
			// opci�n -id1
			else if (arg.equals("-id1"))
			{
				if (i < args.length)
					setUserid(args[i++]);
				else
					System.err.println("-id1 requiere un USERID");
				if (vflag)
					;
				// System.out.println("USERID = " + userid);
			}
			// opci�n -id2
			else if (arg.equals("-id2"))
			{
				if (i < args.length)
					setActivitycode(args[i++]);
				else
					System.err.println("-id2 requiere un ACTIVITYCODE");
				if (vflag)
					;
				// System.out.println("ACTIVITYCODE = " + activitycode);
			}
			// opci�n -d
			else if (arg.equals("-d"))
			{
				if (i < args.length)
					setDefinitionfile(args[i++]);
				else
					System.err.println("-d requiere un definitionfile");
				if (vflag)
				{
					// System.out.println("DefinitionFile = " + definitionfile);
					dflag = true;
				}
			}
			// opciones que no requieren argumentos (flags)
			else
			{
				for (j = 1; j < arg.length(); j++)
				{
					flag = arg.charAt(j);
					switch (flag)
					{
					case 'a':
						if (vflag) // System.out.println("Opci�n autoalign");
							setAutoalign(true);
						break;
					case 'f':
						if (vflag) // System.out.println("Opci�n medianfilter");
							setMedianFilter(true);
						break;
					default:
						System.err
								.println("Revise la l�nea de comandos: opci�n inv�lida "
										+ flag);
						break;
					}
				}
			}
		}
		// si hay m�s par�metros mostraremos un texto de error
		if (i < args.length || dflag == false)
			System.err
					.println("uso: blended_omr [-i inputdir] [-o outputdir] [-id1 USERID] [-id2 ACTIVITYCODE] [-a] -d definitionfile");
		else
		{
			System.out.println("�L�nea de comandos correcta!");
		}
	}

	/**
	 * Devuelve el path de entrada
	 * 
	 * @return inputdir
	 */
	public String getInputPath()
	{
		return inputPath;
	}

	/**
	 * Marca el valor del path de entrada
	 * 
	 * @param inputdir
	 */
	private void setInputPath(String inputdir)
	{
		this.inputPath = inputdir;
	}

	/**
	 * Devuelve el path de salida
	 * 
	 * @return outputdir
	 */
	public String getOutputdir()
	{
		return outputdir;
	}

	/**
	 * Marca el valor del path de salida
	 * 
	 * @param outputdir
	 */
	private void setOutputdir(String outputdir)
	{
		this.outputdir = outputdir;
	}

	/**
	 * Devuelve el UserID
	 * 
	 * @return userid
	 */
	public String getUserid()
	{
		return userid;
	}

	/**
	 * Marca el valor del UserID
	 * 
	 * @param userid
	 */
	private void setUserid(String userid)
	{
		this.userid = userid;
	}

	/**
	 * Devuelve el ActivityCode
	 * 
	 * @return
	 */
	public String getActivitycode()
	{
		return activitycode;
	}

	/**
	 * Marca el valor del ActivityCode
	 * 
	 * @param activitycode
	 */
	private void setActivitycode(String activitycode)
	{
		this.activitycode = activitycode;
	}

	/**
	 * Devuelve el nombre del archivo de definici�n de marcas
	 * 
	 * @return definitionfile
	 */
	public String getDefinitionfile()
	{
		return definitionfile;
	}

	/**
	 * Marca el nombre del archivo de definici�n de marcas
	 * 
	 * @param definitionfile
	 */
	private void setDefinitionfile(String definitionfile)
	{
		this.definitionfile = definitionfile;
	}

	/**
	 * Devuelve true o false en funci�n de si la opci�n autoalign esta activada
	 * o no
	 * 
	 * @return autoalign
	 */
	public boolean isAutoalign()
	{
		return autoalign;
	}

	/**
	 * Marca true o false en funci�n de si la opci�n autoalign esta activada o
	 * no
	 * 
	 * @param autoalign
	 */
	private void setAutoalign(boolean autoalign)
	{
		this.autoalign = autoalign;
	}

	/**
	 * Devuelve true o false en funci�n de si la opci�n medianfilter esta
	 * activada o no
	 * 
	 * @return medianfilter
	 */
	public boolean isMedianFilter()
	{
		return medianfilter;
	}

	/**
	 * Marca true o false en funci�n de si la opci�n medianfilter esta activada
	 * o no
	 * 
	 * @param medianfilter
	 */
	private void setMedianFilter(boolean medianfilter)
	{
		this.medianfilter = medianfilter;
	}

	/**
	 * M�todo para leer los campos del archivo de marcas
	 * 
	 * @param filename
	 * @throws IOException
	 */
	public void leerDefinitionfile(String filename) throws IOException
	{
		String line;

		// //primero se leen el n�mero de p�ginas que define el archivo
		// BufferedReader entrada = new BufferedReader(new InputStreamReader(new
		// FileInputStream(filename)));
		// while((line = entrada.readLine()) != null && !line.equals(""))
		// {
		// if(line.startsWith("[Page")) numeropaginas++; //si identificamos una
		// p�gina incrementamos el n�mero de p�gina
		// }
		// if (numeropaginas == 0) numeropaginas=1; //si no se encuentra la
		// etiqueta solamente habr� una p�gina
		// entrada.close();

		plantilla = new PlantillaOMR(filename); // se crea la plantilla seg�n el
												// n�mero de p�ginas, dentro de
												// esta clase se leer�n las
												// posiciones de las marcas

	}

	/**
	 * M�todo para escribir todos los valores de un campo, el par�metro key
	 * indicara el nombre del campo
	 * 
	 * @param key
	 */
	public void escribirValoresCampo(String key)
	{
		Hashtable<String, Campo> campos = plantilla.getPagina(1).getCampos();
		Campo campo = (Campo) campos.get(key);
		System.out.println("Nombre : " + campo.getNombre());
		System.out.println("Numero de P�gina : " + campo.getNumPag());
		System.out.println("Tipo : " + campo.getTipo());
		double[] coords = campo.getCoordenadas();
		for (int i = 0; i < coords.length; i++)
			System.out.println("Coordenadas : " + coords[i]);
	}

	/**
	 * M�todo para leer todas las p�ginas que haya en inputpath
	 * 
	 * @param inputPath
	 * @return {@link Vector} with {@link File} that was not processed (with errors)
	 */
	public Vector<File> leerPaginas(String inputPath)
	{
		File dir = new File(inputPath);
		File[] files = obtainFileList(dir); // obteneci�n de la lista de
											// ficheros a procesar
		return processFileList(files); // procesar ficheros
	}

	/**
	 * M�todo para procesar la lista de ficheros
	 * 
	 * @param files
	 * @throws IOException
	 * @return {@link Vector} with Files not processed
	 */
	private Vector<File> processFileList(File[] files)
	{
		Vector<File> errors = new Vector<File>();
		for (int i = 0; i < files.length; i++)
		{
			try
			{
				UtilidadesFicheros.procesarImagenes(files[i], isAutoalign(),
						isMedianFilter(), outputdir, plantilla);
			}
			catch (Exception e)
			{
				// report files with errors

				if (logger.isDebugEnabled())
				{
					logger.debug("processFileList(File[]) - Can't process file  - inputPath=" + inputPath + ", file=" + files[i] + ", e=" + e); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				}
				errors.add(files[i]);
			}
		}
		return errors;
	}

	/**
	 * M�todo que obtiene toda la lista de ficheros dada por dir, s� dir es un
	 * fichero obtiene dicho fichero
	 * 
	 * @param path
	 * @return
	 */
	private File[] obtainFileList(File path)
	{
		File[] files; // almacenamos en un array de File[] los path de los
						// ficheros
		if (path.isDirectory())
		{
			files = path.listFiles(new FilenameFilter()
			{
				public boolean accept(File dir, String name)
				{
					name.toLowerCase(); // se convierte el nombre a min�sculas
					return name.endsWith(".jpg") || name.endsWith(".png")
							|| name.endsWith(".pdf");
				}
			});
		} else
		{
			files = new File[] { path };
		}
		return files;
	}
}
