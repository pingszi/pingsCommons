package cn.pings.commons.util.validateCode;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Random;

/**
 *********************************************************
 ** @desc  ：  透明图形验证码
 ** @author   Pings
 ** @date     2017-10-17
 ** @version  v1.0
 * *******************************************************
 */
public class ValidateCode {

    //**图片宽度
    private int width = 120;
    //**图片高度
    private int height = 50;
    //**验证码字符个数
    private int codeCount = 4;
    //**验证码干扰线数
    private int lineCount = 0;
    //**验证码
    private StringBuffer code = new StringBuffer();
    //**验证码图片Buffer
    private BufferedImage buffImg = null;
    //**验证码范围,去掉0(数字)和O(拼音)
    private char[] codeSequence = {'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J',
            'K', 'L', 'M', 'N', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W',
            'X', 'Y', 'Z', '1', '2', '3', '4', '5', '6', '7', '8', '9'};
    private static Random random = new Random();

    public ValidateCode() {
        this(0, 0, 0, 0);
    }

    public ValidateCode(int width, int height) {
        this(width, height, 0, 0);
    }

    public ValidateCode(int width, int height, int codeCount, int lineCount) {
        this.width = width == 0 ? this.width : width;
        this.height = height == 0 ? this.height : height;
        this.codeCount = codeCount == 0 ? this.codeCount : codeCount;
        this.lineCount = lineCount == 0 ? this.lineCount : lineCount;

        this.generateCode();
    }


    /**
     *********************************************************
     ** @desc ：产生随机字符验证码
     ** @author Pings
     ** @date   2017/9/26
     * *******************************************************
     */
    private String[] getCodes() {
        String[] rst = new String[codeCount];

        for (int i = 0; i < codeCount; i++) {
            rst[i] = String.valueOf(codeSequence[random.nextInt(codeSequence.length)]);
            code.append(rst[i]);
        }

        return rst;
    }

    /**
     *********************************************************
     ** @desc ：产生随机颜色值
     ** @author Pings
     ** @date   2017/9/26
     * *******************************************************
     */
    private Color getColor() {
        int red = random.nextInt(255);
        int green = random.nextInt(255);
        int blue = random.nextInt(255);

        return new Color(red, green, blue);
    }

    /**
     *********************************************************
     ** @desc ：生成图形验证码
     ** @author Pings
     ** @date   2017/9/26
     * *******************************************************
     */
    private void generateCode() {
        int x, fontHeight, codeY;

        //**每个字符的宽度(左右各空出一个字符)
        x = width / (codeCount + 2);
        //**字体的高度
        fontHeight = height - 2;
        codeY = height - 4;

        //**图像buffer
        buffImg = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = buffImg.createGraphics();
        
        //**背景透明
        buffImg = g.getDeviceConfiguration().createCompatibleImage(width, height, Transparency.TRANSLUCENT);
    	g.dispose();
    	g = buffImg.createGraphics();
    	
        //**创建字体,可以修改为其它的
        Font font = new Font("Fixedsys", Font.PLAIN, fontHeight);
        g.setFont(font);

        for (int i = 0; i < lineCount; i++) {
            //**设置随机开始和结束坐标
            int xs = random.nextInt(width);   //**x坐标开始
            int ys = random.nextInt(height);  //**y坐标开始
            int xe = xs + random.nextInt(width / 8);   //**x坐标结束
            int ye = ys + random.nextInt(height / 8);  //**y坐标结束

            //**产生随机的颜色值，输出的每个干扰线的颜色值不相同
            g.setColor(getColor());
            g.drawLine(xs, ys, xe, ye);
        }

        String[] codes = getCodes();
        //**产生字符验证码
        for (int i = 0; i < codes.length; i++) {
            g.setColor(getColor());
            g.drawString(codes[i], (i + 1) * x, codeY);
        }
    }
    
    /**
     *********************************************************
     ** @desc ：生成图形验证并写入到指定图片文件
     ** @author Pings
     ** @date   2017/9/26
     ** @param  path 指定图片文件地址
     * *******************************************************
     */
    public void write(String path) throws IOException {
        OutputStream out = new FileOutputStream(path);
        write(out);
    }

    /**
     *********************************************************
     ** @desc ：生成图形验证并写入到指定输出流
     ** @author Pings
     ** @date   2017/9/26
     ** @param  out 指定输出流
     * *******************************************************
     */
    public void write(OutputStream out) throws IOException {
        ImageIO.write(buffImg, "png", out);
        out.close();
    }

    /**
     *********************************************************
     ** @desc ：获取图形验证buffer
     ** @author Pings
     ** @date   2017/9/26
     * *******************************************************
     */
    public BufferedImage getBufferedImage() {
        return buffImg;
    }

    /**
     *********************************************************
     ** @desc ：获取字符
     ** @author Pings
     ** @date   2017/9/26
     * *******************************************************
     */
    public String getCode() {
        return code.toString();
    }

    public static void main(String[] args) throws IOException {
    	ValidateCode code = new ValidateCode();
    	code.write("d:/test.png");
	}
}
