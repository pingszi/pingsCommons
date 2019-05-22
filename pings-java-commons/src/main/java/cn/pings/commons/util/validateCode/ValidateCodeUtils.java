package cn.pings.commons.util.validateCode;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Random;

import javax.imageio.ImageIO;

/**
 *********************************************************
 ** @desc  ：  图形验证码
 ** @author   Pings
 ** @date     2017-10-20
 ** @version  v1.0
 * *******************************************************
 */
public class ValidateCodeUtils {
	
	//**使用到Algerian字体
    public static final String VALIDATE_CODES = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static Random random = new Random();
    
    /**
     *********************************************************
     ** @desc ：  生验证码文件,并返回验证码                                            
     ** @author Pings                                    
     ** @date   2017年10月20日        
     ** @param  length 验证码长度                             
     ** @param  w 图片宽度
     ** @param  h 图片高度
     ** @param  outputFile 输出文件
     ** @return
     ** @throws IOException                                              
     * *******************************************************
     */
    public static String outputImage(int length, int w, int h, File outputFile) throws IOException{
        return outputImage(length, w, h, new FileOutputStream(outputFile));
    }
     
    /**
     *********************************************************
     ** @desc ：  生验证码，输出到输出流,并返回验证码                                            
     ** @author Pings                                    
     ** @date   2017年10月20日        
     ** @param  length 验证码长度                             
     ** @param  w 图片宽度
     ** @param  h 图片高度
     ** @param  os 输出流
     ** @return
     ** @throws IOException                                              
     * *******************************************************
     */
    public static String outputImage(int length, int w, int h, OutputStream os) throws IOException {
    	String code = getCode(length);
        
        BufferedImage image = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        try {
	        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
	        
	        Color[] colors = new Color[5];
	        Color[] colorSpaces = new Color[] { Color.WHITE, Color.CYAN, Color.GRAY, Color.LIGHT_GRAY, Color.MAGENTA, Color.ORANGE, Color.PINK, Color.YELLOW };
	        for (int i = 0; i < colors.length; i++) {
	            colors[i] = colorSpaces[random.nextInt(colorSpaces.length)];
	        }
	         
	        //**设置边框色
	        g.setColor(Color.GRAY);
	        g.fillRect(0, 0, w, h);
	         
	        //**设置背景色
	        Color c = getRandColor(200, 250);
	        g.setColor(c);
	        g.fillRect(0, 2, w, h - 4);
	         
	        //**绘制干扰线
	        g.setColor(getRandColor(160, 200));
	        for (int i = 0; i < 20; i++) {
	            int x = random.nextInt(w - 1);
	            int y = random.nextInt(h - 1);
	            int xl = random.nextInt(6) + 1;
	            int yl = random.nextInt(12) + 1;
	            
	            g.drawLine(x, y, x + xl + 40, y + yl + 20);
	        }
	         
	        //**添加噪点
	        float yawpRate = 0.05f;
	        int area = (int) (yawpRate * w * h);
	        for (int i = 0; i < area; i++) {
	            int x = random.nextInt(w);
	            int y = random.nextInt(h);
	            int rgb = getRandomIntColor();
	            
	            image.setRGB(x, y, rgb);
	        }        
	 
	        g.setColor(getRandColor(100, 160));
	        int fontSize = h - 4;
	        Font font = new Font("Algerian", Font.ITALIC, fontSize);
	        g.setFont(font);
	        
	        char[] chars = code.toCharArray();
	        AffineTransform affine = new AffineTransform();
	        for(int i = 0; i < length; i++){
	            affine.setToRotation(Math.PI / 4 * random.nextDouble() * (random.nextBoolean() ? 1 : -1), (w / length) * i + fontSize / 2, h / 2);
	            g.setTransform(affine);
	            g.drawChars(chars, i, 1, ((w - 10) / length) * i + 5, fontSize);
	        }
	     
	        ImageIO.write(image, "png", os);
        } finally {
        	os.close();
            g.dispose();
        }
        
        return code;
    }
    
    /*获取验证码 */
    private static String getCode(int length){
        return getCode(length, VALIDATE_CODES);
    }
    
    /*获取验证码 */
	private static String getCode(int length, String sources) {
		if (sources == null || sources.length() == 0) {
			sources = VALIDATE_CODES;
		}

		int codesLen = sources.length();
		StringBuffer code = new StringBuffer(length);

		for (int i = 0; i < length; i++) {
			code.append(sources.charAt(random.nextInt(codesLen - 1)));
		}

		return code.toString();
	}

	/*获取颜色 */
	private static Color getRandColor(int fc, int bc) {
		if (fc > 255) fc = 255;
		if (bc > 255) bc = 255;

		int r = fc + random.nextInt(bc - fc);
		int g = fc + random.nextInt(bc - fc);
		int b = fc + random.nextInt(bc - fc);

		return new Color(r, g, b);
	}

	/*获取rgb */
	private static int getRandomIntColor() {
		int[] rgb = new int[3];
		for (int i = 0; i < 3; i++) {
			rgb[i] = random.nextInt(255);
		}
		
		int color = 0;
		for (int c : rgb) {
			color = color << 8;
			color = color | c;
		}

		return color;
	}
	
    public static void main(String[] args) throws IOException{
    	ValidateCodeUtils.outputImage(4, 80, 30, new File("d:/test.png"));
    }
}
