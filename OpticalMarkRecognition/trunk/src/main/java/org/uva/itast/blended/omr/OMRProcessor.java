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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.uva.itast.blended.omr.pages.PageImage;
import org.uva.itast.blended.omr.pages.PagesCollection;
import org.uva.itast.blended.omr.pages.ZippedImageFilePage;

public class OMRProcessor {
	public static final String	IMAGE_TYPES_REG_EXPR	=".*\\.(jpg|png|pdf)";

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
	Map<String,PlantillaOMR> templates=new HashMap<String,PlantillaOMR>();

	private PlantillaOMR	selectedTemplate;

	/**
	 * @return the template
	 */
	public Map<String,PlantillaOMR> getTemplates()
	{
		return templates;
	}
	/**
	 * gets the last selected template
	 * @return
	 */
	public PlantillaOMR getActiveTemplate()
	{
		return selectedTemplate;
	}
	/**
	 * Many parts of OMR uses the field {@link #selectedTemplate} through {@link #getActiveTemplate()}
	 * hence it is needed to mark the default template with this method
	 * @param id
	 * @return
	 */
	public PlantillaOMR selectTemplate(String id)
	{
		selectedTemplate=templates.get(id);
		return selectTemplate(selectedTemplate);
	}
	public PlantillaOMR selectTemplate(PlantillaOMR template)
	{
		this.selectedTemplate=template;
		return template;
	}
	/**
	 * @param template the template to set
	 */
	protected void addTemplate(PlantillaOMR template)
	{
		templates.put(template.getTemplateID(), template);
	}
	/**
	 * Constructor TestManipulation sin par�metros.
	 */
	public OMRProcessor()
	{
	}
	/**
	 * Load templates from a directory of Zip file
	 * @param path
	 * @throws IOException
	 */
	public void loadTemplate(String path) throws IOException
	{
		PlantillaOMR template=new PlantillaOMR(path); // se crea la plantilla seg�n el
		addTemplate(template);
		selectTemplate(template);
	}
	
	public void loadTemplateCollection(String path) throws ZipException, IOException
	{
		File file=new File(path);
		List<InputStream> contents=obtainInputStreamsFromPath(file,".*\\.fields");
		
		// read contents of templates
		for (InputStream inputStream : contents)
		{
			addTemplate(new PlantillaOMR(inputStream));
		}
	}
	/**
	 * @param file
	 * @return 
	 * @throws ZipException
	 * @throws IOException
	 * @throws FileNotFoundException
	 */
	private List<InputStream> obtainInputStreamsFromPath(File file, String regExpr) throws ZipException, IOException, FileNotFoundException
	{
		List<InputStream> contents;
		
		if (file.getName().endsWith(".zip"))
		{
			ZipFile zip=new ZipFile(file);
			contents=inputStreamsFromZip(zip, regExpr);
		}
		else
		{
			File[] filelist=obtainFileList(file,regExpr );
			contents=new ArrayList<InputStream>();
			for (int i=0; i < filelist.length; i++)
			{
				contents.add(new FileInputStream(filelist[i]));
			}
		}
		return contents;
	}
	
	static final String CMD_USAGE="Command line usage:  command [-a] [-f] [-i inputdir][-o outputdir] [-id1 USERID] -d [definitionfiles]\n" +
			"	-i path with the images to be processed. Can be a file, a multipage PDF, directory with images or a Zip with images.\n" +
			"	-d path with the template definition files to be processed. Can be a file, a directory or a Zip with files with .fields extension. Al template definitions must share the same TEMPLATEFIELD field.\n" +
			"	-a try to align the page using the Align[FRAME] field\n" +
			"	-f filter the images with a median filter to remove dithering or noise\n" +
			"	-id1 name of the field used to compose the output file names with the TEMPLATEFIELD detected value.\n";
	/**
	 *Process command line arguments as stated in {@link #CMD_USAGE} and configure the processor: 
	 * {@value #CMD_USAGE}
	 *@see #CMD_USAGE 
	 */	
	public void readCommandLine(String[] args)
	{
		int i=0, j;
		// first argument may be the command. Ignore it
		if (!args[i].startsWith("-"))
		{
			i++;
		}
		// detectamos todas las opciones (s�mbolo "-" delante)
		while (i < args.length && args[i].startsWith("-"))
		{
			vflag=true;
			arg=args[i++];

			// opciones que requieren argumentos
			// opci�n -i
			if (arg.equals("-i"))
			{
				if (i < args.length)
					setInputPath(args[i++]);
				else
					System.err.println("-i need a path");
				if (vflag)
					;
			}
			// opci�n -o
			else if (arg.equals("-o"))
			{
				if (i < args.length)
					setOutputdir(args[i++]);
				else
					System.err.println("-o need a path");
				if (vflag)
					;
			}
			// opci�n -id1
			else if (arg.equals("-id1"))
			{
				if (i < args.length)
					setUserid(args[i++]);
				else
					System.err.println("-id1 need an USERID");
				if (vflag)
					;
			}
			// opci�n -id2
			else if (arg.equals("-id2"))
			{
				System.err.println("warning -id2 deprecated");
				
				if (i < args.length)
					setActivitycode(args[i++]);
				else
					System.err.println("-id2 need an TEMPLATEID");
				if (vflag)
					;
			}
			// opci�n -d
			else if (arg.equals("-d"))
			{
				if (i < args.length)
					setDefinitionfile(args[i++]);
				else
					System.err.println("-d need a path to definition files");
				if (vflag)
				{
					// System.out.println("DefinitionFile = " + definitionfile);
					dflag=true;
				}
			}
			// opciones que no requieren argumentos (flags)
			else
			{
				for (j=1; j < arg.length(); j++)
				{
					flag=arg.charAt(j);
					switch (flag) {
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
						throw new IllegalArgumentException("Check command line: invalid option " + flag);
						
					}
				}
			}
		}

		// si hay m�s par�metros se muestra un texto de error
		if (i < args.length || dflag == false)
		{
			throw new IllegalArgumentException("Usage: " + args[0] + " [-i inputdir] [-o outputdir] [-id1 USERID] [-id2 ACTIVITYCODE] [-a] -d definitionfile");

		}
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
		return getActiveTemplate().getPagina(1).getCampos().get(fieldName).getValue();
	
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
		Hashtable<String, Field> campos = getActiveTemplate().getPagina(1).getCampos();
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
	 * @throws IOException 
	 * @throws ZipException 
	 */
	public Vector<PageImage> processPath(String inputPath) throws ZipException, IOException
	{
		PagesCollection pages;
		File dir = new File(inputPath);
		// obteneci�n de la lista de ficheros a procesar
		if (!dir.isDirectory() && inputPath.endsWith(".zip"))
		{
			ZipFile zip=new ZipFile(dir);
			
			pages = getPageCollection(zip, selectZipEntries(zip, IMAGE_TYPES_REG_EXPR));
		}
		else
		{
			File[] files = obtainFileList(dir,IMAGE_TYPES_REG_EXPR);
			
			 pages = getPageCollection(files);
		}
			
		// procesar ficheros
		return processPages(pages);
	}

	protected PagesCollection getPageCollection(ZipFile zip, List<ZipEntry> selectedZipEntries)
	{
		PagesCollection pages=new PagesCollection();
		
		for (ZipEntry entry : selectedZipEntries)
		{
			pages.addPage(new ZippedImageFilePage(zip, entry));
		}
		return pages;
	}
	/**
	 * Process a collection of pages
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

				PlantillaOMR template=OMRUtils.findBestSuitedTemplate(pageImage, getTemplates(), medianfilter);
				selectTemplate(template);

				// se procesa la p�gina
				OMRUtils.processPage(pageImage, isAutoalign(),
						isMedianFilter(), outputdir, template);
				
				// se salvan los resultados en archivo
				OMRUtils.saveOMRResults(pageImage.getFileName(),
						outputdir, template, OMRUtils.TEMPLATEID_FIELDNAME , userid);

				pageImage.outputMarkedPage(outputdir);

				// if (logger.isDebugEnabled())
				// pageImage.outputWorkingPage(outputdir);

				pageImage.freeMemory();
				logger.debug("Page  "+pageImage+" processed in (ms)"+(System.currentTimeMillis()-taskStart)); //$NON-NLS-1$
			}
			catch (Exception e)
			{
				// report files with errors

				
				logger.error("processFileList(File[]) - Can't process page=" + pageImage.toString() ,e); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				
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
	private File[] obtainFileList(File path, final String regExp)
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
					return name.matches(regExp);
				}
			});
		} else
		{
			files = new File[] { path };
		}
		return files;
	}
	/**
	 * 
	 * @param path
	 * @return
	 * @throws ZipException
	 * @throws IOException
	 */
	public static List<InputStream> inputStreamsFromZip(ZipFile zipFile, String regExpr) throws ZipException, IOException
	{
		List<InputStream> files=new ArrayList<InputStream>();
		
		List<ZipEntry> selectedEntries=selectZipEntries(zipFile, regExpr);
		
		for (ZipEntry zipEntr : selectedEntries)
		{
			if (logger.isInfoEnabled())
		{
			logger.info("filesFromZip(File) - ZipEntry zipEntry=" + zipEntr); //$NON-NLS-1$
		}
		
		files.add(zipFile.getInputStream(zipEntr));
		}
		
		return files;
	}
	/**
	 * @param zipFile
	 * @param regExpr
	 * @return
	 */
	protected static List<ZipEntry> selectZipEntries(ZipFile zipFile, String regExpr)
	{
		Enumeration<? extends ZipEntry> entries=zipFile.entries();
		List<ZipEntry> selectedEntries=new ArrayList<ZipEntry>();
		while (entries.hasMoreElements())
		{
			ZipEntry zipEntry=(ZipEntry) entries.nextElement();
			if (!zipEntry.isDirectory() && zipEntry.getName().matches(regExpr))
			{
			selectedEntries.add(zipEntry);				
			}

		}
		return selectedEntries;
	}
}
