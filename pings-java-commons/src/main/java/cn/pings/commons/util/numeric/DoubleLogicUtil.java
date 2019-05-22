package cn.pings.commons.util.numeric;

import java.math.BigDecimal;

/**
 *********************************************************
 ** @desc  ：Java浮点数不能够精确进行运算，精确的浮点数运算                                           
 ** @author  Pings                                      
 ** @date    2016年10月21日  
 ** @version v1.0                                                                                  
 * *******************************************************
 */
public class DoubleLogicUtil {

	//**默认除法运算精度 
	private static final int DEF_DIV_SCALE = 10; 

	/**
	 *********************************************************
	 ** @desc ：精确的加法运算                                            
	 ** @author Pings                                      
	 ** @date   2016年10月21日                                      
	 ** @param v1 被加数
	 ** @param v2 加数 
	 ** @return 两个参数的和                                              
	 * *******************************************************
	 */
    public static double add(double v1, double v2){
        BigDecimal b1 = new BigDecimal(Double.toString(v1)); 
        BigDecimal b2 = new BigDecimal(Double.toString(v2)); 
        return b1.add(b2).doubleValue(); 
    } 

    /**
     *********************************************************
     ** @desc ：精确的减法运算                                             
     ** @author Pings                                      
     ** @date   2016年10月21日                                      
     ** @param v1 被减数
     ** @param v1 被减数
     ** @return 两个参数的差                                             
     * *******************************************************
     */
    public static double sub(double v1, double v2){
        BigDecimal b1 = new BigDecimal(Double.toString(v1)); 
        BigDecimal b2 = new BigDecimal(Double.toString(v2)); 
        return b1.subtract(b2).doubleValue(); 
    } 

    /**
     *********************************************************
     ** @desc ：精确的乘法运算                                             
     ** @author Pings                                      
     ** @date   2016年10月21日                                      
     ** @param v1 被乘数 
     ** @param v1 乘数
     ** @return 两个参数的乘积                                             
     * *******************************************************
     */
    public static double mul(double v1, double v2){
        BigDecimal b1 = new BigDecimal(Double.toString(v1)); 
        BigDecimal b2 = new BigDecimal(Double.toString(v2)); 
        return b1.multiply(b2).doubleValue(); 
    } 

    /**
     *********************************************************
     ** @desc ：精确的除法运算，当发生除不尽的情况时，精确到小数点以后10位                                          
     ** @author Pings                                      
     ** @date   2016年10月21日                                      
     ** @param v1 被除数  
     ** @param v1 除数
     ** @return 两个参数的商                                             
     * *******************************************************
     */
    public static double div(double v1, double v2){
        return div(v1, v2, DEF_DIV_SCALE); 
    } 

    /**
     *********************************************************
     ** @desc ：精确的除法运算，当发生除不尽的情况时，scale参数指定精度                                         
     ** @author Pings                                      
     ** @date   2016年10月21日                                      
     ** @param v1 被除数  
     ** @param v1 除数
     ** @param scale 保留的小数位
     ** @return 两个参数的商                                             
     * *******************************************************
     */
    public static double div(double v1, double v2, int scale){ 
        if(scale < 0){ 
            throw new IllegalArgumentException( 
                "The scale must be a positive integer or zero"); 
        } 
        BigDecimal b1 = new BigDecimal(Double.toString(v1)); 
        BigDecimal b2 = new BigDecimal(Double.toString(v2)); 
        return b1.divide(b2, scale, BigDecimal.ROUND_HALF_UP).doubleValue(); 
    } 

    /**
     *********************************************************
     ** @desc ：精确的小数位四舍五入处理                                        
     ** @author Pings                                      
     ** @date   2016年10月21日                                      
     ** @param v 需要四舍五入的数字 
     ** @param scale 小数点后保留几位 
     ** @return 四舍五入后的结果                                             
     * *******************************************************
     */
    public static double round(double v, int scale){ 
        if(scale < 0){ 
            throw new IllegalArgumentException("The scale must be a positive integer or zero");
        }

        BigDecimal b = new BigDecimal(Double.toString(v)); 
        BigDecimal one = new BigDecimal("1"); 
        return b.divide(one, scale, BigDecimal.ROUND_HALF_UP).doubleValue(); 
    } 
    
    /**
     *********************************************************
     ** @desc ：精确的类型转换(Float)                                       
     ** @author Pings                                      
     ** @date   2016年10月21日                                      
     ** @param v 需要类型转换数字 
     ** @return 转换结果                                             
     * *******************************************************
     */
    public static float convertsToFloat(double v){ 
	    BigDecimal b = new BigDecimal(v); 
	    return b.floatValue(); 
    } 
	    
    /**
     *********************************************************
     ** @desc ：精确的类型转换(Int)                                       
     ** @author Pings                                      
     ** @date   2016年10月21日                                      
     ** @param v 需要类型转换的数字 
     ** @return 转换结果                                             
     * *******************************************************
     */ 
	public static int convertsToInt(double v){ 
		BigDecimal b = new BigDecimal(v); 
	    return b.intValue(); 
	} 

	/**
     *********************************************************
     ** @desc ：精确的类型转换(Long)                                       
     ** @author Pings                                      
     ** @date   2016年10月21日                                      
     ** @param v 需要类型转换的数字 
     ** @return 转换结果                                             
     * *******************************************************
     */ 
	public static long convertsToLong(double v){ 
		BigDecimal b = new BigDecimal(v); 
	    return b.longValue(); 
	}

	/**
     *********************************************************
     ** @desc ：精确对比两个数字                                      
     ** @author Pings                                      
     ** @date   2016年10月21日                                      
     ** @param v1 需要被对比的第一个数 
	 ** @param v2 需要被对比的第二个数 
     ** @return 两个数一样则返回0，第一个大返回1，反之返回-1                                             
     * *******************************************************
     */
	public static int compareTo(double v1, double v2){ 
		BigDecimal b1 = new BigDecimal(v1); 
		BigDecimal b2 = new BigDecimal(v2); 
	    return b1.compareTo(b2); 
	} 

}
