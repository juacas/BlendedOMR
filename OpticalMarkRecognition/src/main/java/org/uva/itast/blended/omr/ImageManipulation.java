/*
 * InputImage.java
 *
 * Created on June 30, 2007, 9:56 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.uva.itast.blended.omr;

import omrproj.ConcentricCircle;
import omrproj.ImageUtil;
import omrproj.SolidMark;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import net.sourceforge.jiu.data.*;
import net.sourceforge.jiu.filters.*;
import net.sourceforge.jiu.geometry.*;
import java.util.*;
import java.io.*;


/**
 *
 * @author Aaditeshwar Seth
 */
public class ImageManipulation
{
	/**
	 * Logger for this class
	 */
	private static final Log	logger	= LogFactory.getLog(ImageManipulation.class);

	Gray8Image grayimage, scaledImage;
    int height, width;
    ConcentricCircle topleftpos, bottomrightpos;
    int topleftX, topleftY, bottomrightX, bottomrightY;
    double currAngle, currDiag, realAngle, realDiag;
    int scaleFactor;
    int markLocations[], realMarkLocations[];   // x * 10000 + y
    int ascTemplateLocations[];                 // same order as markLocations: x * 1000 + y
    int[][] ascTemplate;
    int nummarks, realNummarks;
    int numfields;
    Hashtable fields;
    Field[] ascTemplateFields;
    Field field;
    
    public ImageManipulation(Gray8Image grayimage) {
        this.grayimage = grayimage;
        
        height = grayimage.getHeight();
        width = grayimage.getWidth();
        // 1700 x 2339 --> 426 x 560
		if (logger.isDebugEnabled())
		{
			logger.debug("ImageManipulation(Gray8Image) - width = " + width + ": height = " + height); //$NON-NLS-1$ //$NON-NLS-2$
		}
        
        scaleFactor = width / 340;      // 5 --> each mark becomes around 3 pixels wide
    }
    
    public void locateConcentricCircles() {
        int[] topleft = new int[((int)(height/4) + 1) * ((int)(width/4) + 1)];
        int[] bottomright = new int[((int)(height/4) + 1) * ((int)(width/4) + 1)];
        grayimage.getSamples(0, 0, 0, (int)(width/4) + 1, (int)(height/4) + 1, topleft, 0);
        grayimage.getSamples(0, width - (int)(width/4) - 1, height - (int)(height/4) - 1, (int)(width/4) + 1, (int)(height/4) + 1, bottomright, 0);
        
        Gray8Image topleftimg = new MemoryGray8Image((int)(width/4) + 1, (int)(height/4) + 1);
        topleftimg.putSamples(0, 0, 0, (int)(width/4) + 1, (int)(height/4) + 1, topleft, 0);
        Gray8Image bottomrightimg = new MemoryGray8Image((int)(width/4) + 1, (int)(height/4) + 1);
        bottomrightimg.putSamples(0, 0, 0, (int)(width/4) + 1, (int)(height/4) + 1, bottomright, 0);

        topleftpos = new ConcentricCircle(topleftimg, width, height);
        topleftpos.process();
        bottomrightpos = new ConcentricCircle(bottomrightimg, width, height);
        bottomrightpos.process();

//        ImageUtil.saveImage(topleftpos.getImg(), "topleft.png");
//        ImageUtil.saveImage(bottomrightpos.getImg(), "bottomright.png");
//        ImageUtil.saveImage(topleftpos.getBestFit().getTemplate(), "template.png");

        bottomrightpos.getBestFit().setX(width - (int)(width/4) - 1 + bottomrightpos.getBestFit().getX());
        bottomrightpos.getBestFit().setY(height - (int)(height/4) - 1 + bottomrightpos.getBestFit().getY());
        
        topleftX = topleftpos.getBestFit().getX() + topleftpos.getBestFit().getTemplate().getWidth() / 2;
        topleftY = topleftpos.getBestFit().getY() + topleftpos.getBestFit().getTemplate().getHeight() / 2;
        ImageUtil.putMark(grayimage, topleftX, topleftY, true);
        
        bottomrightX = bottomrightpos.getBestFit().getX() + bottomrightpos.getBestFit().getTemplate().getWidth() / 2;
        bottomrightY = bottomrightpos.getBestFit().getY() + bottomrightpos.getBestFit().getTemplate().getHeight() / 2;
        ImageUtil.putMark(grayimage, bottomrightX, bottomrightY, true);

		if (logger.isDebugEnabled())
		{
			logger.debug("locateConcentricCircles() - " + topleftX + ":" + topleftY + ":" + bottomrightX + ":" + bottomrightY); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		}
//        ImageUtil.saveImage(grayimage, "grayimage.png");
        
        currAngle = Math.toDegrees(Math.atan2((bottomrightX - topleftX), 
                (bottomrightY - topleftY)));
        currDiag = Math.sqrt(
                Math.pow((bottomrightY - topleftY), 2) + Math.pow((bottomrightX - topleftX), 2));
		if (logger.isDebugEnabled())
		{
			logger.debug("locateConcentricCircles() - curr angle = " + currAngle); //$NON-NLS-1$
		}
		if (logger.isDebugEnabled())
		{
			logger.debug("locateConcentricCircles() - curr diag = " + currDiag); //$NON-NLS-1$
		}
    }
    
    public void locateMarks() {
        rescale();
//        Gray8Image scaledImage = ImageUtil.readImage("scaled.png");     // XXX do not read from file
        int scaledtopleftX = topleftX / scaleFactor;
        int scaledtopleftY = topleftY / scaleFactor;
        int scaledbottomrightX = bottomrightX / scaleFactor;
        int scaledbottomrightY = bottomrightY / scaleFactor;

		if (logger.isDebugEnabled())
		{
			logger.debug("locateMarks() - scaledtop: " + scaledtopleftX + ":" + scaledtopleftY); //$NON-NLS-1$ //$NON-NLS-2$
		}
		if (logger.isDebugEnabled())
		{
			logger.debug("locateMarks() - scaledbot: " + scaledbottomrightX + ":" + scaledbottomrightY); //$NON-NLS-1$ //$NON-NLS-2$
		}

        int[] marks = new int[100 * 100 * 10];
        int nummarks = 0;
        for(int i = scaledtopleftX; i <= scaledbottomrightX; i++) {
            for(int j = scaledtopleftY + 20; j <= scaledbottomrightY - 20; j++) {
                int val = (scaledImage.getSample(i, j) + scaledImage.getSample(i - 1, j) +
                        scaledImage.getSample(i + 1, j) + scaledImage.getSample(i, j - 1) +
                        scaledImage.getSample(i, j + 1) + scaledImage.getSample(i - 1, j - 1) +
                        scaledImage.getSample(i + 1, j + 1) + scaledImage.getSample(i + 1, j - 1) +
                        scaledImage.getSample(i - 1, j + 1)) / 9;
                if(val < 200) {         // XXX
                    marks[nummarks++] = i * 1000 + j;
                }
            }
        }

		if (logger.isDebugEnabled())
		{
			logger.debug("locateMarks() - nummarks = " + nummarks); //$NON-NLS-1$
		}
        
        int[] dupmarks = new int[100 * 100 * 10];
        nummarks = filter(marks, dupmarks, nummarks);
        marks = dupmarks;

		if (logger.isDebugEnabled())
		{
			logger.debug("locateMarks() - nummarks = " + nummarks); //$NON-NLS-1$
		}

        int t;
        Gray8Image markedImage = (Gray8Image)(grayimage.createCopy());
        double approxXscale = width / ConcentricCircle.a4width;      // 80.95 pixel/cm
        double approxYscale = height / ConcentricCircle.a4height;    // 78.75 pixel/cm
        int markdispX = (int)(ConcentricCircle.markDiam * approxXscale / 4);
        int markdispY = (int)(ConcentricCircle.markDiam * approxYscale / 4);
        for(int i = 0; i < nummarks; i++) {
			if (logger.isDebugEnabled())
			{
				logger.debug("locateMarks() - " + marks[i]); //$NON-NLS-1$
			}
            ImageUtil.putMark(scaledImage, marks[i] / 1000, marks[i] % 1000, true);
            t = marks[i];
            marks[i] = ((t / 1000) * scaleFactor + markdispX) * 10000 + (t % 1000) * scaleFactor + markdispY;   // XXX
            ImageUtil.putMark(markedImage, marks[i] / 10000, marks[i] % 10000, false);
        }        
        ImageUtil.saveImage(scaledImage, "markedscaled.png");
        ImageUtil.saveImage(markedImage, "marked.png");

        this.markLocations = marks;
        this.nummarks = nummarks;
    }

    public void writeAscTemplate(String filename) {
        int[] dupmarks = new int[nummarks];
        int scaleFactor = this.scaleFactor * 3;
        int width = (bottomrightX - topleftX) / scaleFactor;
        int height = (bottomrightY - topleftY) / scaleFactor;
        ascTemplate = new int[height][width];
        boolean linesOccupied[] = new boolean[height];
        ascTemplateLocations = new int[nummarks];
        
        for(int i = 0; i < nummarks; i++) {
            dupmarks[i] = (markLocations[i] / 10000 - topleftY) / scaleFactor + ((markLocations[i] % 10000 - topleftX) / scaleFactor) * 10000;
            ascTemplateLocations[i] = i;
        }
        sort(dupmarks, ascTemplateLocations, nummarks);
 

		if (logger.isDebugEnabled())
		{
			logger.debug("writeAscTemplate(String) - ascTemplateLocations -- "); //$NON-NLS-1$
		}
        for(int i = 0; i < nummarks; i++) {
			if (logger.isDebugEnabled())
			{
				logger.debug("writeAscTemplate(String) - " + ascTemplateLocations[i] + " "); //$NON-NLS-1$ //$NON-NLS-2$
			}
        }
		if (logger.isDebugEnabled())
		{
			logger.debug("writeAscTemplate(String)"); //$NON-NLS-1$
		}
        
        try {
            int prevrow = 0;
            int i = 0;
            while(i < nummarks) {
                for(int j = prevrow; j < (dupmarks[i] / 10000); j++) {
                    linesOccupied[j] = false;
                    for(int k = 0; k < width; k++) {
                        ascTemplate[j][k] = -1;
                    }
                }
                int prevcol = 0;
                int j = i;
                prevrow = (dupmarks[i] / 10000);
                for(; j < nummarks && (dupmarks[j] / 10000) == prevrow; j++) {
                    for(int k = prevcol; k < (dupmarks[j] % 10000); k++) {
                        ascTemplate[prevrow][k] = -1;
                    }
                    ascTemplate[prevrow][dupmarks[j] % 10000] = j;
                    linesOccupied[prevrow] = true;
                    prevcol = (dupmarks[j] % 10000) + 1;
                }
                for(int k = prevcol; k < width; k++) {
                    ascTemplate[prevrow][k] = -1;
                }
                i = j;
                prevrow ++;
            }
            for(int j = prevrow; j < height; j++) {
                for(int k = 0; k < width; k++) {
                    ascTemplate[j][k] = -1;
                }
            }
            
            //int markcount = 0;
            for(int m = 1; m < height; m++) {
                if(linesOccupied[m] && linesOccupied[m - 1]) {
                    for(int n = 0; n < width; n++) {
                        if(ascTemplate[m - 1][n] > -1) {
                            ascTemplate[m][n] = ascTemplate[m - 1][n];
                            ascTemplate[m - 1][n] = -1;
                        }
                    }
                    linesOccupied[m - 1] = false;
                }
            }

            int[][] ascTemplateDup = new int[height][width];
            boolean[] linesOccupiedDup = new boolean[height];
            int heightdup = height;
            boolean skip = false;
            for(int n = 0; n < width; n++) { ascTemplateDup[0][n] = ascTemplate[0][n]; }
            int dupm = 0;
            for(int m = 1; m < height; m++) {
                if(!skip && !linesOccupied[m] && !linesOccupied[m - 1]) {
                    skip = true;
                    heightdup--;
                }
                else {
                    skip = false;
                    dupm++;
                    for(int n = 0; n < width; n++) { ascTemplateDup[dupm][n] = ascTemplate[m][n]; }            
                    linesOccupiedDup[dupm] = linesOccupied[m];
                }
            }
            ascTemplate = ascTemplateDup;
            linesOccupied = linesOccupiedDup;
            height = heightdup;
            
            dumpAscTemplate(ascTemplate, width, height, linesOccupied);

            /**opción b**/
            double x1[] = new double[50];
            double x2[] = new double[50];
            double y1[] = new double[50];
            double y2[] = new double[50];
            String c[] = new String[50];
            
            int j=0;
            int num_var=0; //número de variables q contiene el archivo de configuración (a, b, c...)
            int[] ascTemplateLocationsDup = new int[nummarks];
            
                BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream("coordenadas_campos.fields")));
                String line;

                while((line = in.readLine()) != null && !line.equals("")) {
                    StringTokenizer st = new StringTokenizer(line, " ");
                    c[j] = st.nextToken();
                    x1[j] = Double.parseDouble(st.nextToken());
                    x2[j] = Double.parseDouble(st.nextToken());
                    y1[j] = Double.parseDouble(st.nextToken());
                    y2[j] = Double.parseDouble(st.nextToken());
                    j++;
                }
                in.close();

            num_var=j;
            
            PrintWriter salida = new PrintWriter(new FileOutputStream(filename));
            
            for(int m = 0; m < height; m++) {
            	for(int n = 0; n < width; n++) {
            		if(ascTemplate[m][n] > -1) {
            			ascTemplateLocationsDup[ascTemplateLocations[ascTemplate[m][n]]] = m * 1000 + n;
            			for (j=0; j<num_var; j++){
							if (logger.isDebugEnabled())
							{
								logger.debug("writeAscTemplate(String) - " + markLocations[ascTemplateLocations[ascTemplate[m][n]]]); //$NON-NLS-1$
							}
            				if (x1[j]<(markLocations[ascTemplateLocations[ascTemplate[m][n]]] % 10000)
                            		&& (markLocations[ascTemplateLocations[ascTemplate[m][n]]] % 10000)<x2[j]
                            		&& y1[j]<(markLocations[ascTemplateLocations[ascTemplate[m][n]]] / 10000)
                            		&& (markLocations[ascTemplateLocations[ascTemplate[m][n]]] / 10000)<y2[j])
            					/*if (x1[j]<(markLocations[ascTemplateLocations[ascTemplate[m][n]]])
                                		&& (markLocations[ascTemplateLocations[ascTemplate[m][n]]])<x2[j]
                                		&& y1[j]<(markLocations[ascTemplateLocations[ascTemplate[m][n]]])
                                		&& (markLocations[ascTemplateLocations[ascTemplate[m][n]]])<y2[j])*/
            					{
            						//XXX mirar el 10000 a q se refería
            						//se puede ver que en la línea 136 se multiplica por 10000
            					salida.print(c[j]);
            					j=num_var+1;	//se fuerza al salir del bucle
            					//XXX break
            					}
            				}
            			//con esto escribimos "-" en los caracteres detectados fuera de los rectángulos de respuestas
            			if (j==num_var)salida.print("-");
            			}
            		else {
                    	salida.print("-");
                    	}
                    }
                salida.println();
                }
            salida.close();
            ascTemplateLocations = ascTemplateLocationsDup;
            
            //a partir de aquí
			if (logger.isDebugEnabled())
			{
				logger.debug("writeAscTemplate(String) - ascTemplateLocations -- "); //$NON-NLS-1$
			}
            for(int q = 0; q < nummarks; q++) {
				if (logger.isDebugEnabled())
				{
					logger.debug("writeAscTemplate(String) - " + ascTemplateLocations[q] + " "); //$NON-NLS-1$ //$NON-NLS-2$
				}
            }
			if (logger.isDebugEnabled())
			{
				logger.debug("writeAscTemplate(String)"); //$NON-NLS-1$
			}
            
        } catch(Exception ex) {
			logger.error("writeAscTemplate(String)", ex); //$NON-NLS-1$
        }
    }

    private void dumpAscTemplate(int[][] ascTemplate, int width, int height, boolean[] linesOccupied) {
        for(int i = 0; i < height; i++) {
			if (logger.isDebugEnabled())
			{
				logger.debug("dumpAscTemplate(int[][], int, int, boolean[]) - " + linesOccupied[i] + " "); //$NON-NLS-1$ //$NON-NLS-2$
			}
            for(int j = 0; j < width; j++) {
                if(ascTemplate[i][j] > -1) {
					if (logger.isDebugEnabled())
					{
						logger.debug("dumpAscTemplate(int[][], int, int, boolean[]) - " + ascTemplate[i][j]); //$NON-NLS-1$
					}
                }
                else {
					if (logger.isDebugEnabled())
					{
						logger.debug("dumpAscTemplate(int[][], int, int, boolean[]) - -"); //$NON-NLS-1$
					}
                }
            }
			if (logger.isDebugEnabled())
			{
				logger.debug("dumpAscTemplate(int[][], int, int, boolean[])"); //$NON-NLS-1$
			}
        }
    }
    
    private void sort(int[] marks, int[] ascTemplateLocations, int nummarks) {
        int t;
        for(int i = 0; i < nummarks; i++) {
            for(int j = i + 1; j < nummarks; j++) {
                if(marks[i] > marks[j]) {
                    t = marks[i];
                    marks[i] = marks[j];
                    marks[j] = t;
                    t = ascTemplateLocations[i];
                    ascTemplateLocations[i] = ascTemplateLocations[j];
                    ascTemplateLocations[j] = t;
                }
            }
        }        
    }

    private int filter(int[] marks, int[] dupmarks, int nummarks) {
        int numdupmarks = 0;
        int[] cluster = new int[50];
        int numin;
        int i = 0;
        while(i < nummarks) {
            numin = 0;
            cluster[numin++] = marks[i];
            int j = 0;
			if (logger.isDebugEnabled())
			{
				logger.debug("filter(int[], int[], int) - i->" + marks[i] + ":"); //$NON-NLS-1$ //$NON-NLS-2$
			}
            marks[i] = -1;
            while(j < nummarks) {
                if(marks[j] != -1) {
                    int k = 0;
                    while(k < numin) {
                        if(Math.abs(marks[j] / 1000 - cluster[k] / 1000) < 2 && Math.abs(marks[j] % 1000 - cluster[k] % 1000) < 2) {
                            cluster[numin++] = marks[j];
							if (logger.isDebugEnabled())
							{
								logger.debug("filter(int[], int[], int) - Found j->" + marks[j] + ":"); //$NON-NLS-1$ //$NON-NLS-2$
							}
                            marks[j] = -1;
                            j = i + 1;
                            k = numin + 1;
                            break;
                        }
                        else {
                            k++;
                        }
                    }
                }
                j++;
            }

            int sumx = 0, sumy = 0;
            for(int l = 0; l < numin; l++) {
                sumx += (cluster[l] / 1000);
                sumy += (cluster[l] % 1000);
            }
            sumx /= numin; sumy /= numin;
            dupmarks[numdupmarks++] = sumx * 1000 + sumy;

            i++;
            while(marks[i] == -1) {
                i++;
            }
			if (logger.isDebugEnabled())
			{
				logger.debug("filter(int[], int[], int) - New: i->" + marks[i]); //$NON-NLS-1$
			}
        }

        return numdupmarks;
    }
    
    private void rescale() {
        try {
            MedianFilter filter = new MedianFilter();
            filter.setArea((int)((width / 1700 * 25) / 2) * 2 + 1,(int)(height / 2339 * 25 / 2) * 2 + 1);
            filter.setInputImage(grayimage);
            filter.process();
            Gray8Image medianimage = (Gray8Image)(filter.getOutputImage());
//            ImageUtil.saveImage(medianimage, "median.png");
            
            ScaleReplication scale = new ScaleReplication();
            scale.setInputImage(medianimage);
            scale.setSize(width / scaleFactor, height / scaleFactor);
            scale.process();
            PixelImage scaledImage = scale.getOutputImage();
            
            int scaledtopleftX = topleftX / scaleFactor;
            int scaledtopleftY = topleftY / scaleFactor;
            int scaledbottomrightX = bottomrightX / scaleFactor;
            int scaledbottomrightY = bottomrightY / scaleFactor;

            ImageUtil.putMark((Gray8Image)scaledImage, scaledtopleftX, scaledtopleftY, true);
            ImageUtil.putMark((Gray8Image)scaledImage, scaledbottomrightX, scaledbottomrightY, true);

            ImageUtil.saveImage(scaledImage, "scaled.png");
            this.scaledImage = (Gray8Image)scaledImage;
        } catch(Exception excep) {
			logger.error("rescale()", excep); //$NON-NLS-1$
        }
    }
    
    public void writeConfig(String filename) {
        try {
            PrintWriter out = new PrintWriter(new FileOutputStream(filename));
            out.println("#top left"); out.println(topleftX + " " + topleftY);
            out.println("#bottom right"); out.println(bottomrightX + " " + bottomrightY);
            out.println("#angle"); out.println(currAngle);
            out.println("#diag"); out.println(currDiag);
            out.println("#nummarks"); out.println(nummarks);
            out.println("#marks");
            int x, y;
            double r1, theta1, r2, theta2;
            for(int i = 0; i < nummarks; i++) {
                x = markLocations[i] / 10000; y = markLocations[i] % 10000;
                r1 = Math.sqrt((x - topleftX) * (x - topleftX) + (y - topleftY) * (y - topleftY));
                r2 = Math.sqrt((x - bottomrightX) * (x - bottomrightX) + (y - bottomrightY) * (y - bottomrightY));
                theta1 = Math.toDegrees(Math.atan2(x - topleftX, y - topleftY));
                theta2 = Math.toDegrees(Math.atan2(bottomrightX - x, bottomrightY - y));
                out.println(r1 + " " + theta1 + " " + r2 + " " + theta2 + " " + (ascTemplateLocations[i] / 1000) + " " + (ascTemplateLocations[i] % 1000));
                
                //out.println(x + "x" + y + " y ");
            }
            out.close();
        } catch(Exception ex) {
			logger.error("writeConfig(String)", ex); //$NON-NLS-1$
        }
    }
    
    public void readConfig(String filename) {
        int scaleFactor = this.scaleFactor * 3;
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(filename)));
            in.readLine(); String topleft = in.readLine();
            int index = topleft.indexOf(" ");
            int realTopleftX = Integer.parseInt(topleft.substring(0, index));
            int realTopleftY = Integer.parseInt(topleft.substring(index + 1));
            in.readLine(); String bottomright = in.readLine();
            index = bottomright.indexOf(" ");
            int realBottomrightX = Integer.parseInt(bottomright.substring(0, index));
            int realBottomrightY = Integer.parseInt(bottomright.substring(index + 1));
            in.readLine(); realAngle = Double.parseDouble(in.readLine());
            in.readLine(); realDiag = Double.parseDouble(in.readLine());
            in.readLine(); realNummarks = Integer.parseInt(in.readLine());
            in.readLine(); String line;
            realMarkLocations = new int[realNummarks];

            ascTemplate = new int[(realBottomrightY - realTopleftY) / scaleFactor][(realBottomrightX - realTopleftX) / scaleFactor];
            for(int i = 0; i < (realBottomrightY - realTopleftY) / scaleFactor; i++) {
                for(int j = 0; j < (realBottomrightX - realTopleftX) / scaleFactor; j++) {
                    ascTemplate[i][j] = -1;
                }
            }
            
            Gray8Image markedImage = (Gray8Image)(grayimage.createCopy());

            int i = 0;
            while((line = in.readLine()) != null && !line.equals("")) {
                StringTokenizer st = new StringTokenizer(line, " ");
                double r1 = Double.parseDouble(st.nextToken());
                double theta1 = Double.parseDouble(st.nextToken());
                double r2 = Double.parseDouble(st.nextToken());
                double theta2 = Double.parseDouble(st.nextToken());
                int m = Integer.parseInt(st.nextToken());
                int n = Integer.parseInt(st.nextToken());
                theta1 += (currAngle - realAngle);
                theta2 += (currAngle - realAngle);
                r1 *= (currDiag / realDiag);
                r2 *= (currDiag / realDiag);
                int x1 = (int)(	topleftX + r1 * Math.sin(Math.toRadians(theta1)));
                int y1 = (int)(topleftY + r1 * Math.cos(Math.toRadians(theta1)));
                int x2 = (int)(bottomrightX - r2 * Math.sin(Math.toRadians(theta2)));
                int y2 = (int)(bottomrightY - r2 * Math.cos(Math.toRadians(theta2)));
                realMarkLocations[i++] = ((x1 + x2) / 2) * 10000 + ((y1 + y2) / 2);
                ascTemplate[m][n] = i - 1;
                ImageUtil.putMark(markedImage, (x1 + x2) / 2, (y1 + y2) / 2, true);
            }
            in.close();
//            ImageUtil.saveImage(markedImage, "markedform.png");
        } catch(Exception ex) {
			logger.error("readConfig(String)", ex); //$NON-NLS-1$
        }
    }

    public void readFields(String filename) {
        String line;
        fields = new Hashtable();
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(filename)));
            while((line = in.readLine()) != null && !line.equals("")) {
                Field field = new Field(line);
                field = new Field(line);
                fields.put(new Character(field.getCh()), field);
            }
            in.close();
            numfields = fields.size();
        } catch(Exception ex) {
			logger.error("readFields(String)", ex); //$NON-NLS-1$
        }
    }
    
    public void readAscTemplate(String filename) {
        ascTemplateLocations = new int[realNummarks];
        ascTemplateFields = new Field[realNummarks];
        int m = 0, n;
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(filename)));
            String line;
            while((line = in.readLine()) != null && !line.equals("")) {
                n = 0;
                for(int i = 0; i < line.length(); i++) {
                    char ch = line.charAt(i);
                    if(ch != '-' && ch != '0') {
                        ascTemplateLocations[ascTemplate[m][n]] = ch;
                        Field field = (Field)(fields.get(new Character(ch)));
                        ascTemplateFields[ascTemplate[m][n]] = field;
                        field.addPos(ascTemplate[m][n]);    // always added in row, column order
						if (logger.isDebugEnabled())
						{
							logger.debug("readAscTemplate(String) - added " + m + ":" + n + ":" + ascTemplate[m][n] + ":" + realMarkLocations[ascTemplate[m][n]] + ":" + (char) (ch)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
						}
                    }
                    else if(ch=='0') {
                        ascTemplateLocations[ascTemplate[m][n]] = '!'; 
                    }
                    n++;
                }
                m++;
            }
            in.close();
        } catch(Exception ex) {
			logger.error("readAscTemplate(String)", ex); //$NON-NLS-1$
        }
    }
    
    public void searchMarks() {
        for(int i = 0; i < realNummarks; i++) {
			if (logger.isDebugEnabled())
			{
				logger.debug("searchMarks() - " + (char) ascTemplateLocations[i] + " "); //$NON-NLS-1$ //$NON-NLS-2$
			}
        }
		if (logger.isDebugEnabled())
		{
			logger.debug("searchMarks()"); //$NON-NLS-1$
		}
        int x, y;
        SolidMark mark = new SolidMark(grayimage, width / ConcentricCircle.a4width, height / ConcentricCircle.a4height);
        Gray8Image markedImage = (Gray8Image)(grayimage.createCopy());
        //XXX revisar a partir de aquí
        for(int i = 0; i < realNummarks; i++) {
        	if (ascTemplateLocations[i]=='!')continue;//XXX
            x = realMarkLocations[i] / 10000;
            y = realMarkLocations[i] % 10000;
			if (logger.isDebugEnabled())
			{
				logger.debug("searchMarks() - " + i); //$NON-NLS-1$
			}
            if(mark.isMark(x, y)) {
                Field field = (Field)(fields.get(new Character((char)(ascTemplateLocations[i]))));
				if (logger.isDebugEnabled())
				{
					logger.debug("searchMarks() - *** " + i + ":" + (char) (ascTemplateLocations[i]) + ":" + field); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				}
                field.putValue(i);
				if (logger.isDebugEnabled())
				{
					logger.debug("searchMarks() - Found mark at " + x + "," + y + ":" + (char) (ascTemplateLocations[i]) + ":" + field.getName() + "=" + field.getValue(i)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
				}
                mark.putMarkOnImage(markedImage);
            }
        }
        ImageUtil.saveImage(markedImage, "marksfoundform.png");        
    }
    
    public void saveData(String filename) {
        try {
            PrintWriter out = new PrintWriter(new FileOutputStream(filename));
            Enumeration e = fields.keys();
            while(e.hasMoreElements()) {
                Field field = (Field)(fields.get(e.nextElement()));
				if (logger.isDebugEnabled())
				{
					logger.debug("saveData(String) - " + field.getName() + "=" + field.getFieldValues()); //$NON-NLS-1$ //$NON-NLS-2$
				}
                out.println(field.getName() + "=" + field.getFieldValues());
            }
            out.close();
        } catch(Exception ex) {
			logger.error("saveData(String)", ex); //$NON-NLS-1$
        }
    }
    
    int ROW_CHOICE = 0, COLUMN_CHOICE = 1, GRID_CHOICE = 2;
    int SINGLE = 0, MULTIPLE = 1, COLUMN = 2, ROW = 3;
    class Field {
        char ch;
        int type, subtype;
        String name;
        String[] choices;
        Hashtable positions;
        String[] values;
        int numValues = 0;
        boolean[] singleDone;
        
        public Field(String line) {
            StringTokenizer st = new StringTokenizer(line, " ");
            ch = st.nextToken().charAt(0);
            String typestr = st.nextToken();
            if(typestr.equalsIgnoreCase("row")) {
                type = ROW_CHOICE;
            } else if(typestr.equalsIgnoreCase("column")) {
                type = COLUMN_CHOICE;
            } else if(typestr.equalsIgnoreCase("grid")) {
                type = GRID_CHOICE;
            }
            String subtypestr = st.nextToken();
            if(subtypestr.equalsIgnoreCase("single")) {
                subtype = SINGLE;
            } else if(subtypestr.equalsIgnoreCase("multiple")) {
                subtype = MULTIPLE;
            } else if(subtypestr.equalsIgnoreCase("column")) {
                subtype = COLUMN;
            } else if(subtypestr.equalsIgnoreCase("row")) {
                subtype = ROW;
            }
            name = st.nextToken();
            ArrayList choicearr = new ArrayList();
            while(st.hasMoreTokens()) {
                choicearr.add(st.nextToken());
            }
            choices = new String[choicearr.size()];
            for(int i = 0; i < choicearr.size(); i++) {
                choices[i] = (String)(choicearr.get(i));
            }
            if(type != GRID_CHOICE) {
                values = new String[choices.length];
                singleDone = null;
            } else {
                values = new String[100];
                singleDone = new boolean[100];
            }
            positions = new Hashtable();
        }
        
        public Field() {
			// TODO Auto-generated constructor stub
		}

		public char getCh() {
            return ch;
        }
        
        public int getType() {
            return type;
        }
        
        public int getSubtype() {
            return subtype;
        }
        
        public String getName() {
            return name;
        }
        
        public String[] getChoices() {
            return choices;
        }

        int currpos = 0;
        public void addPos(int i) {
            if(type == ROW_CHOICE) {
                positions.put(new Integer(i), new Integer(currpos++));
            } else if(type == COLUMN_CHOICE) {
                positions.put(new Integer(i), new Integer(currpos++));
            } else if(type == GRID_CHOICE && subtype == ROW) {
                positions.put(new Integer(i), new Integer(currpos % choices.length));
                currpos++;
            } else if(type == GRID_CHOICE && subtype == COLUMN) {
				if (logger.isDebugEnabled())
				{
					logger.debug("addPos(int) - addpos -- " + i + ":" + currpos); //$NON-NLS-1$ //$NON-NLS-2$
				}
                positions.put(new Integer(i), new Integer(currpos++));
            }
        }
        
        public String getValue(int i) {
            if(type == GRID_CHOICE && subtype == COLUMN) {
                int mod = currpos / choices.length;
                return choices[((Integer)(positions.get(new Integer(i)))).intValue() / mod];
            } else {
                return choices[((Integer)(positions.get(new Integer(i)))).intValue()];
            }
        }

        public void putValue(int i) {
            if(type == GRID_CHOICE && subtype == COLUMN) {
                int posi = ((Integer)(positions.get(new Integer(i)))).intValue();
				if (logger.isDebugEnabled())
				{
					logger.debug("putValue(int) - currpos = " + currpos + ":" + choices.length + ":" + i + ":" + posi + ":" + (posi % (currpos / choices.length)) + ":" + singleDone[posi % (currpos / choices.length)]); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
				}
                if(!singleDone[posi % (currpos / choices.length)]) {
                    values[numValues++] = getValue(i);
                    singleDone[posi % (currpos / choices.length)] = true;
                }
            } else if(type == GRID_CHOICE && subtype == ROW) {
                int posi = ((Integer)(positions.get(new Integer(i)))).intValue();
                if(!singleDone[posi / choices.length]) {
                    values[numValues++] = getValue(i);
                    singleDone[posi / choices.length] = true;
                }
            } else {
                values[numValues++] = getValue(i);
            }
            }
        
        public String getFieldValues() {
            if(subtype == SINGLE) {
                return values[0];
            }
            else if(subtype == MULTIPLE) {
                String retval = "";
                for(int i = 0; i < numValues; i++) {
                    retval = retval + " " + values[i];
                }
                if(retval.length() > 0) {
                    return retval.substring(1);
                } else {
                    return "";
                }
            }
            else if(type == GRID_CHOICE) {
                String retval = "";
                for(int i = 0; i < numValues; i++) {
                    retval = retval + values[i];
                }
                if(retval.length() > 0) {
                    return retval;
                } else {
                    return "";
                }
            }
            return "";
        }
    }

}
