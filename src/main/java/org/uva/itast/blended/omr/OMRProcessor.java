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
* @author MarÃ­a JesÃºs VerdÃº 
* @author Luisa Regueras 
* @author Elena VerdÃº
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

	// valor en pï¿½xeles de la altura de la imï¿½gen con la que se trabajara
	public static final int _PAGE_HEIGHT_PIXELS = (int) (2339);
	// valor en pï¿½xeles de la anchura de la imï¿½gen con la que se trabajara
	public static final int _PAGE_WIDTH_PIXELS = (int) (1700);

	private String arg;
	private char flag;
	private boolean vflag = false;
	// fichero o directorio a procesar
	private String inputPath;
	// directorio donde se alojaran los resultados
	private String outputdir;
	// identificador que se utilizarï¿½ para marcar los ficheros
	private String userid;
	// identificador que contiene el nï¿½mero de documento que corresponde con una
	// actividad o cuestionario de
	// Moodle. El ï¿½ltimo carï¿½cter recoge el nï¿½mero de pï¿½gina en caso de haber
	// mï¿½s de una, pï¿½gina
	private String activitycode;
	// fichero con la descripciï¿½n de las marcas
	private String definitionfile;
	// bandera para la opciï¿½n de alineado
	private boolean autoalign = false;
	// bandera para la opciï¿½n de alineado
	private boolean medianfilter = false;
	// marcador para el campo obligatorio -d
	private boolean dflag = false;
	
	// plantilla para almacenar las pï¿½ginas y los campos de definition file
	Map<String,OMRTemplate> templates=new HashMap<String,OMRTemplate>();

	private OMRTemplate	selectedTemplate;

	/**
	 * @return the template
	 */
	public Map<String,OMRTemplate> getTemplates()
	{
		return templates;
	}
	/**
	 * gets the last selected template
	 * @return
	 */
	public OMRTemplate getActiveTemplate()
	{
		return selectedTemplate;
	}
	/**
	 * Many parts of OMR uses the field {@link #selectedTemplate} through {@link #getActiveTemplate()}
	 * hence it is needed to mark the default template with this method
	 * @param id
	 * @return
	 */
	public OMRTemplate selectTemplate(String id)
	{
		selectedTemplate=templates.get(id);
		return selectTemplate(selectedTemplate);
	}
	public OMRTemplate selectTemplate(OMRTemplate template)
	{
		this.selectedTemplate=template;
		return template;
	}
	/**
	 * @param template the template to set
	 */
	protected void addTemplate(OMRTemplate template)
	{
		templates.put(template.getTemplateID(), template);
	}
	/**
	 * Constructor TestManipulation sin parï¿½metros.
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
		OMRTemplate template=new OMRTemplate(path); // se crea la plantilla segï¿½n el
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
			addTemplate(new OMRTemplate(inputStream));
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
		// detectamos todas las opciones (sï¿½mbolo "-" delante)
		while (i < args.length && args[i].startsWith("-"))
		{
			vflag=true;
			arg=args[i++];

			// opciones que requieren argumentos
			// opciï¿½n -i
			if (arg.equals("-i"))
			{
				if (i < args.length)
					setInputPath(args[i++]);
				else
					System.err.println("-i need a path");
				if (vflag)
					;
			}
			// opciï¿½n -o
			else if (arg.equals("-o"))
			{
				if (i < args.length)
					setOutputdir(args[i++]);
				else
					System.err.println("-o need a path");
				if (vflag)
					;
			}
			// opciï¿½n -id1
			else if (arg.equals("-id1"))
			{
				if (i < args.length)
					setUserid(args[i++]);
				else
					System.err.println("-id1 need an USERID");
				if (vflag)
					;
			}
			// opciï¿½n -id2
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
			// opciï¿½n -d
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
					// Opciï¿½n medianfilter
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

		// si hay mï¿½s parï¿½metros se muestra un texto de error
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
		return getActiveTemplate().getPage(1).getFields().get(fieldName).getValue();
	
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
	 * Devuelve el nombre del archivo de definiciï¿½n de marcas
	 * 
	 * @return definitionfile
	 */
	public String getDefinitionfile()
	{
		return definitionfile;
	}

	/**
	 * Marca el nombre del archivo de definiciï¿½n de marcas
	 * 
	 * @param definitionfile
	 */
	private void setDefinitionfile(String definitionfile)
	{
		this.definitionfile = definitionfile;
	}

	/**
	 * Devuelve true o false en funciï¿½n de si la opciï¿½n autoalign esta activada
	 * o no
	 * 
	 * @return autoalign
	 */
	public boolean isAutoalign()
	{
		return autoalign;
	}

	/**
	 * Marca true o false en funciï¿½n de si la opciï¿½n autoalign esta activada o
	 * no
	 * 
	 * @param autoalign
	 */
	private void setAutoalign(boolean autoalign)
	{
		this.autoalign = autoalign;
	}

	/**
	 * Devuelve true o false en funciï¿½n de si la opciï¿½n medianfilter esta
	 * activada o no
	 * 
	 * @return medianfilter
	 */
	public boolean isMedianFilter()
	{
		return medianfilter;
	}

	/**
	 * Marca true o false en funciï¿½n de si la opciï¿½n medianfilter esta activada
	 * o no
	 * 
	 * @param medianfilter
	 */
	public void setMedianFilter(boolean medianfilter)
	{
		this.medianfilter = medianfilter;
	}

	/**
	 * Mï¿½todo para escribir todos los valores de un campo, el parï¿½metro key
	 * indicara el nombre del campo
	 * 
	 * @param key
	 */
	public void writeFieldValues(String key)
	{
		Hashtable<String, Field> campos = getActiveTemplate().getPage(1).getFields();
		Field campo = (Field) campos.get(key);
		System.out.println("Nombre : " + campo.getName());
		System.out.println("Numero de Pï¿½gina : " + campo.getNumPag());
		System.out.println("Tipo : " + campo.getTipo());
		
		System.out.println("Coordenadas : " + campo.getBBox());
	}

	/**
	 * Mï¿½todo para leer todas las pï¿½ginas que haya en inputpath
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
		// obteneciï¿½n de la lista de ficheros a procesar
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
		
		int count=1;
		for (PageImage pageImage : pages)
		{
			try
			{
				long taskStart = System.currentTimeMillis();

				if (logger.isInfoEnabled())
				{
					logger.info("Start processing pageImage "+count++ +"/"+pages.getNumPages()+"(" + pageImage.getName()+")"); //$NON-NLS-1$
				}

				OMRTemplate template=OMRUtils.findBestSuitedTemplate(pageImage, getTemplates(), medianfilter);
				selectTemplate(template);

				// se procesa la pï¿½gina
				OMRUtils.processPage(pageImage, isAutoalign(),
						isMedianFilter(), outputdir, template);
				
				// se salvan los resultados en archivo
				OMRUtils.saveOMRResults(pageImage.getName(),
						outputdir, template, OMRUtils.TEMPLATEID_FIELDNAME , userid);

				pageImage.outputMarkedPage(outputdir);

				// if (logger.isDebugEnabled())
				// pageImage.outputWorkingPage(outputdir);

				pageImage.freeMemory();
				logger.info("Page  "+pageImage+" processed in "+(System.currentTimeMillis()-taskStart)+" ms."); //$NON-NLS-1$
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
	 * Mï¿½todo que obtiene toda la lista de ficheros dada por dir, sï¿½ dir es un
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
					name.toLowerCase(); // se convierte el nombre a minï¿½sculas
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
			logger.debug("filesFromZip(File) - ZipEntry zipEntry=" + zipEntr); //$NON-NLS-1$
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
