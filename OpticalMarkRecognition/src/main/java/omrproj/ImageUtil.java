/*
 * ImageUtil.java
 *
 * Created on June 30, 2007, 7:55 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package omrproj;

import java.io.IOException;

import sun.awt.image.ImageFormatException;

import net.sourceforge.jiu.codecs.*;
import net.sourceforge.jiu.data.*;
import net.sourceforge.jiu.ops.MissingParameterException;
import net.sourceforge.jiu.ops.WrongParameterException;
import net.sourceforge.jiu.color.reduction.*;

/**
 *
 * @author Aaditeshwar Seth
 */
public class ImageUtil {
	
    public static Gray8Image readImage(String filename) throws IOException, ImageFormatException {
        Gray8Image grayimage = null;
        RGB24Image redimage = null;
        try {
        	PixelImage image = ImageLoader.load(filename);
            if(image.getImageType().toString().indexOf("RGB")!= -1) 
            {
                redimage = (RGB24Image)(ImageLoader.load(filename));
                RGBToGrayConversion rgbtogray = new RGBToGrayConversion();
                rgbtogray.setInputImage(redimage);
                // adjust this if needed
                // rgbtogray.setColorWeights(0.3f, 0.3f, 0.4f);
                rgbtogray.process();
                grayimage = (Gray8Image)(rgbtogray.getOutputImage());    
            }
            else if(image.getImageType().toString().indexOf("Gray") != -1) 
            {
                grayimage = (Gray8Image)(image);
            }
            else 
            {
                grayimage = null;
            }
        } catch (IOException e)
       
		{
			throw e;
        }
		catch (InvalidFileStructureException e)
		{
			throw new ImageFormatException(e.getMessage());
		}
		catch (InvalidImageIndexException e)
		{
			throw new ImageFormatException(e.getMessage());
		}
		catch (UnsupportedTypeException e)
		{
			throw new ImageFormatException(e.getMessage());
		}
		catch (MissingParameterException e)
		{
			throw new ImageFormatException(e.getMessage());
		}
		catch (WrongParameterException e)
		{
			throw new ImageFormatException(e.getMessage());
		}

        return grayimage;
    }
    
    public static void saveImage(PixelImage img, String filename) {
        try {
            PNGCodec codec = new PNGCodec();    
            codec.setFile(filename, CodecMode.SAVE);
            codec.setImage(img);
            codec.setCompressionLevel(0);
            codec.process();
        } catch(Exception ex) {
            ex.printStackTrace(System.out);
        } 
    }
    
    public static void putMark(Gray8Image img, int x, int y, boolean color) {
        if(color) {
            img.putBlack(x, y);
            img.putBlack(x + 1, y + 1);
            img.putBlack(x - 1, y - 1);
            img.putBlack(x + 1, y);
            img.putBlack(x - 1, y);
            img.putBlack(x, y + 1);
            img.putBlack(x, y - 1);        
            img.putBlack(x + 1, y - 1);        
            img.putBlack(x - 1, y + 1);
            img.putBlack(x - 1, y - 1);        
        } else {
            img.putWhite(x, y);
            img.putWhite(x + 1, y + 1);
            img.putWhite(x - 1, y - 1);
            img.putWhite(x + 1, y);
            img.putWhite(x - 1, y);
            img.putWhite(x, y + 1);
            img.putWhite(x, y - 1);        
            img.putWhite(x + 1, y - 1);        
            img.putWhite(x - 1, y + 1);
            img.putWhite(x - 1, y - 1);        
        }
    }
}
