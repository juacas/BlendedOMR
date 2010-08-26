/*
 * OMRProcessor.java
 *
 * Creado en Marzo-Mayo de 2009
 *
 */

package org.uva.itast.blended.omr;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.uva.itast.blended.omr.pages.PageImage;
import org.uva.itast.blended.omr.pages.PagesCollection;

/**
 * @author Jes�s Rodilana
 */
public class OMRProcessor {
	/**
	 * Logger for this class
	 */
	private static final Log logger = LogFactory.getLog(OMRProcessor.class);

	// valor en p�xeles de la altura de la im�gen con la que se trabajara
	public static final int _PAGE_HEIGHT_PIXELS = (int) (2339);
	// valor en p�xeles de la anchura de la im�gen con la que se trabajara
	public static final int _PAGE_WIDTH_PIXELS = (int) (1700);

	private String arg;
	private char flag;
	private boolean vflag = false;
	// fichero o directorio a procesar
	private String inputPath;
	// directorio donde se alojaran los resultados
	private String outputdir;
	// identificador que se utilizar� para marcar los ficheros
	private String userid;
	// identificador que contiene el n�mero de documento que corresponde con una
	// actividad o cuestionario de
	// Moodle. El �ltimo car�cter recoge el n�mero de p�gina en caso de haber
	// m�s de una, p�gina
	private String activitycode;
	// fichero con la descripci�n de las marcas
	private String definitionfile;
	// bandera para la opci�n de alineado
	private boolean autoalign = false;
	// bandera para la opci�n de alineado
	private boolean medianfilter = false;
	// marcador para el campo obligatorio -d
	private boolean dflag = false;
	
	// plantilla para almacenar las p�ginas y los campos de definition file
	PlantillaOMR template;

	/**
	 * @return the template
	 */
	protected PlantillaOMR getTemplate()
	{
		return template;
	}
	/**
	 * @param template the template to set
	 */
	protected void setTemplate(PlantillaOMR template)
	{
		this.template = template;
	}
	/**
	 * Constructor TestManipulation sin par�metros.
	 */
	public OMRProcessor()
	{
	}
	/**
	 * Load template
	 * @param filename
	 * @throws IOException
	 */
	public void loadTemplate(String filename) throws IOException
	{
		
		template = new PlantillaOMR(filename); // se crea la plantilla seg�n el
	}		
	/**
	 * M�todo que lee la l�nea de comandos. Identifica que las opciones y
	 * par�metros sean correctos y los almacena. uso: blended_omr [-i inputdir]
	 * [-o outputdir] [-id1 USERID] [-id2 ACTIVITYCODE] [-a] [-f] -d
	 * definitionfiles -a indica que hay que alinear la p�gina -f indica que hay
	 * que filtrar los campos (para im�genes de mala calidad)
	 */
	public void readCommandLine(String[] args)
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
						if (vflag)
							setAutoalign(true);
						break;
					// Opci�n medianfilter
					case 'f':
						if (vflag)
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
		// si hay m�s par�metros se muestra un texto de error
		if (i < args.length || dflag == false)
			System.err
					.println("uso: blended_omr [-i inputdir] [-o outputdir] [-id1 USERID] [-id2 ACTIVITYCODE] [-a] -d definitionfile");
		else
		{
				logger.debug("leerLineaComandos(String[]) Command-Line OK- arg=" + arg); //$NON-NLS-1$
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
	public String getFieldValue(String fieldName)
	{
		return getTemplate().getPagina(1).getCampos().get(fieldName).getValue();
	
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
	public void setMedianFilter(boolean medianfilter)
	{
		this.medianfilter = medianfilter;
	}

	/**
	 * M�todo para escribir todos los valores de un campo, el par�metro key
	 * indicara el nombre del campo
	 * 
	 * @param key
	 */
	public void escribirValoresCampo(String key)
	{
		Hashtable<String, Field> campos = template.getPagina(1).getCampos();
		Field campo = (Field) campos.get(key);
		System.out.println("Nombre : " + campo.getNombre());
		System.out.println("Numero de P�gina : " + campo.getNumPag());
		System.out.println("Tipo : " + campo.getTipo());
		
		System.out.println("Coordenadas : " + campo.getBBox());
	}

	/**
	 * M�todo para leer todas las p�ginas que haya en inputpath
	 * 
	 * @param inputPath
	 * @return {@link Vector} with {@link File} that was not processed (with errors)
	 */
	public Vector<PageImage> processPath(String inputPath)
	{
		File dir = new File(inputPath);
		// obteneci�n de la lista de ficheros a procesar
		File[] files = obtainFileList(dir);
		PagesCollection pages = getPageCollection(files);
		// procesar ficheros
		return processPages(pages);
	}

	/**
	 * M�todo para procesar las p�ginas
	 * 
	 * @param files
	 * @throws IOException
	 * @return {@link Vector} with Files not processed
	 */
	public Vector<PageImage> processPages(PagesCollection pages)
	{
		Vector<PageImage> errors = new Vector<PageImage>();
		
		
		for (PageImage pageImage : pages)
		{
			try
			{
				long taskStart = System.currentTimeMillis();

				// se procesa la p�gina
				UtilidadesFicheros.procesarPagina(pageImage, isAutoalign(),
						isMedianFilter(), outputdir, template);
				
				// se salvan los resultados en archivo
				UtilidadesFicheros.saveOMRResults(pageImage.getFileName(),
						outputdir, template, activitycode, userid);

				pageImage.outputMarkedPage(outputdir);

				// if (logger.isDebugEnabled())
				// pageImage.outputWorkingPage(outputdir);

				pageImage.freeMemory();
				logger.debug("Page  "+pageImage+" processed in (ms)"+(System.currentTimeMillis()-taskStart)); //$NON-NLS-1$
			}
			catch (Exception e)
			{
				// report files with errors

				
				logger.error("processFileList(File[]) - Can't process page=" + pageImage.toString() + ", e=" + e,e); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				
				errors.add(pageImage);
			}
		}
		
		return errors;
	}

	/**
	 * @param files
	 * @return
	 */
	private PagesCollection getPageCollection(File[] files)
	{
		PagesCollection pages=new PagesCollection();
		for (int i = 0; i < files.length; i++)
		{
			try
			{
				pages.addFile(files[i]);
			}
			catch (IOException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return pages;
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
