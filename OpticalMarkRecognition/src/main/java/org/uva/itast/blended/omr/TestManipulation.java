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
 * @author Jesús Rodilana
 */
public class TestManipulation
{
	/**
	 * Logger for this class
	 */
	private static final Log	logger				= LogFactory.getLog(TestManipulation.class);

	public static final int	_IMAGEHEIGTHPIXEL	= 2339/2;	// valor en píxeles
															// de la altura de
															// la imágen con la
															// que se trabaja en
															// el programa
	public static final int	_IMAGEWIDTHPIXEL	= 1700/2;	// valor en píxeles
															// de la anchura de
															// la imágen con la
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
															// se utilizará para
															// marcar los
															// ficheros
	private String			activitycode;					// identificador que
															// contiene el
															// número de
															// documento que
															// corresponde con
															// una actividad o
															// cuestionario de
															// Moodle. El último
															// carácter recoge
															// el número de
															// página en caso de
															// haber más de una,
															// página
	private String			definitionfile;				// fichero con la
															// descripción de
															// las marcas
	private boolean			autoalign			= false;	// bandera para la
															// opción de
															// alineado
	private boolean			medianfilter		= false;	// bandera para la
															// opción de
															// alineado
	private boolean			dflag				= false;	// marcador para el
															// campo obligatorio
															// -d
	private int				numeropaginas		= 0;		// el número de
															// página por
															// defecto será 0
	PlantillaOMR			plantilla;						// plantilla para
															// almacenar las
															// páginas y los
															// campos de
															// definition file

	/**
	 * Constructor TestManipulation sin parámetros.
	 */
	public TestManipulation()
	{
	}

	/**
	 * Método que lee la línea de comandos. Identifica que las opciones y
	 * parámetros sean correctos y los almacena. uso: blended_omr [-i inputdir]
	 * [-o outputdir] [-id1 USERID] [-id2 ACTIVITYCODE] [-a] [-f] -d
	 * definitionfiles -a indica que hay que alinear la página -f indica que hay
	 * que filtrar los campos (para imágenes de mala calidad)
	 */
	public void leerLineaComandos(String[] args)
	{
		int i = 0, j;

		// detectamos todas las opciones (símbolo "-" delante)
		while (i < args.length && args[i].startsWith("-"))
		{
			vflag = true;
			arg = args[i++];

			// opciones que requieren argumentos
			// opción -i
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
			// opción -o
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
			// opción -id1
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
			// opción -id2
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
			// opción -d
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
						if (vflag) // System.out.println("Opción autoalign");
							setAutoalign(true);
						break;
					case 'f':
						if (vflag) // System.out.println("Opción medianfilter");
							setMedianFilter(true);
						break;
					default:
						System.err
								.println("Revise la línea de comandos: opción inválida "
										+ flag);
						break;
					}
				}
			}
		}
		// si hay más parámetros mostraremos un texto de error
		if (i < args.length || dflag == false)
			System.err
					.println("uso: blended_omr [-i inputdir] [-o outputdir] [-id1 USERID] [-id2 ACTIVITYCODE] [-a] -d definitionfile");
		else
		{
			System.out.println("¡Línea de comandos correcta!");
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
	 * Devuelve el nombre del archivo de definición de marcas
	 * 
	 * @return definitionfile
	 */
	public String getDefinitionfile()
	{
		return definitionfile;
	}

	/**
	 * Marca el nombre del archivo de definición de marcas
	 * 
	 * @param definitionfile
	 */
	private void setDefinitionfile(String definitionfile)
	{
		this.definitionfile = definitionfile;
	}

	/**
	 * Devuelve true o false en función de si la opción autoalign esta activada
	 * o no
	 * 
	 * @return autoalign
	 */
	public boolean isAutoalign()
	{
		return autoalign;
	}

	/**
	 * Marca true o false en función de si la opción autoalign esta activada o
	 * no
	 * 
	 * @param autoalign
	 */
	private void setAutoalign(boolean autoalign)
	{
		this.autoalign = autoalign;
	}

	/**
	 * Devuelve true o false en función de si la opción medianfilter esta
	 * activada o no
	 * 
	 * @return medianfilter
	 */
	public boolean isMedianFilter()
	{
		return medianfilter;
	}

	/**
	 * Marca true o false en función de si la opción medianfilter esta activada
	 * o no
	 * 
	 * @param medianfilter
	 */
	private void setMedianFilter(boolean medianfilter)
	{
		this.medianfilter = medianfilter;
	}

	/**
	 * Método para leer los campos del archivo de marcas
	 * 
	 * @param filename
	 * @throws IOException
	 */
	public void leerDefinitionfile(String filename) throws IOException
	{
		String line;

		// //primero se leen el número de páginas que define el archivo
		// BufferedReader entrada = new BufferedReader(new InputStreamReader(new
		// FileInputStream(filename)));
		// while((line = entrada.readLine()) != null && !line.equals(""))
		// {
		// if(line.startsWith("[Page")) numeropaginas++; //si identificamos una
		// página incrementamos el número de página
		// }
		// if (numeropaginas == 0) numeropaginas=1; //si no se encuentra la
		// etiqueta solamente habrá una página
		// entrada.close();

		plantilla = new PlantillaOMR(filename); // se crea la plantilla según el
												// número de páginas, dentro de
												// esta clase se leerán las
												// posiciones de las marcas

	}

	/**
	 * Método para escribir todos los valores de un campo, el parámetro key
	 * indicara el nombre del campo
	 * 
	 * @param key
	 */
	public void escribirValoresCampo(String key)
	{
		Hashtable<String, Campo> campos = plantilla.getPagina(1).getCampos();
		Campo campo = (Campo) campos.get(key);
		System.out.println("Nombre : " + campo.getNombre());
		System.out.println("Numero de Página : " + campo.getNumPag());
		System.out.println("Tipo : " + campo.getTipo());
		double[] coords = campo.getCoordenadas();
		for (int i = 0; i < coords.length; i++)
			System.out.println("Coordenadas : " + coords[i]);
	}

	/**
	 * Método para leer todas las páginas que haya en inputpath
	 * 
	 * @param inputPath
	 * @return {@link Vector} with {@link File} that was not processed (with errors)
	 */
	public Vector<File> leerPaginas(String inputPath)
	{
		File dir = new File(inputPath);
		File[] files = obtainFileList(dir); // obteneción de la lista de
											// ficheros a procesar
		return processFileList(files); // procesar ficheros
	}

	/**
	 * Método para procesar la lista de ficheros
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
	 * Método que obtiene toda la lista de ficheros dada por dir, sí dir es un
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
					name.toLowerCase(); // se convierte el nombre a minúsculas
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
