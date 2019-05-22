package cn.pings.commons.util.money;

import cn.pings.commons.util.numeric.DoubleLogicUtil;

/**
 *********************************************************
 ** @desc  ：money工具类                                             
 ** @author  Pings                                      
 ** @date    2015年11月23日  
 ** @version v1.0                                                                                  
 * *******************************************************
 */
public class MoneyUtil {

	/**
	 *********************************************************
	 ** @desc ：数字金额转换为大写                                             
	 ** @author Pings                                      
	 ** @date   2016年10月21日                                      
	 ** @param n
	 ** @return                                              
	 * *******************************************************
	 */
    public static String toUppercase(double n){
        String fraction[] = {"角", "分"};
        String digit[] = { "零", "壹", "贰", "叁", "肆", "伍", "陆", "柒", "捌", "玖" };
        String unit[][] = {{"元", "万", "亿"}, {"", "拾", "佰", "仟"}};
 
        String head = n < 0? "负": "";
        n = Math.abs(n);
         
        String s = "";
        for (int i = 0; i < fraction.length; i++) {
            s += (digit[(int)(DoubleLogicUtil.mul(n,  10 * Math.pow(10, i)) % 10)] + fraction[i]).replaceAll("(零.)+", "");
        }
        if(s.length()<1){
            s = "整";    
        }
        int integerPart = (int)Math.floor(n);
 
        for (int i = 0; i < unit[0].length && integerPart > 0; i++) {
            String p ="";
            for (int j = 0; j < unit[1].length && n > 0; j++) {
                p = digit[integerPart%10]+unit[1][j] + p;
                integerPart = integerPart/10;
            }
            s = p.replaceAll("(零.)*零$", "").replaceAll("^$", "零") + unit[0][i] + s;
        }
        return head + s.replaceAll("(零.)*零元", "元").replaceFirst("(零.)+", "").replaceAll("(零.)+", "零").replaceAll("^整$", "零元整");
    }

}