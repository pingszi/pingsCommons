package cn.pings.commons.util.net;

import java.util.*;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

/**
 *********************************************************
 ** @desc  ：  java web网络工具类                                          
 ** @author    Pings
 ** @date      2017年10月17日
 ** @version   v1.1
 **
 ** V1.1 添加matchIp Pings 2017-10-20                                                                              
 * *******************************************************
 */
public class NetworkUtil {

	/**
	 *********************************************************
	 ** @desc ： 获取请求IP地址                                             
	 ** @author Pings                                    
	 ** @date   2017年10月17日                                      
	 ** @param  request HttpServletRequest请求对象
	 ** @return                                              
	 * *******************************************************
	 */
	public static String getIp(HttpServletRequest request) {     
	     String ip = request.getHeader("x-forwarded-for");     
	     if(ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {     
	         ip = request.getHeader("Proxy-Client-IP");     
	     }     
	     if(ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {     
	         ip = request.getHeader("WL-Proxy-Client-IP");     
	     }     
	     if(ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {     
	         ip = request.getRemoteAddr();     
	     }     
	     return ip;     
	} 
	
	/**
     *********************************************************
     ** @desc ：请求的url是否在排除目录
     ** @author Pings
     ** @date   2017/10/16
     ** @param  requestURI         请求地址
     ** @param  excludesPattern    排除的地址集合
     * *******************************************************
     */
    public static boolean isExclusion(String requestURI, Collection<String> excludesPattern) {
        if (excludesPattern == null) {
            return false;
        } else {
            Iterator<String> it = excludesPattern.iterator();

            String pattern;
            do {
                if (!it.hasNext()) {
                    return false;
                }

                pattern = it.next().replace("*", "/*");
            } while(!Pattern.compile(pattern).matcher(requestURI).find());

            return true;
        }
    }
    
    /**
     *********************************************************
     ** @desc ：匹配IP地址
     ** @author Pings                                    
     ** @date   2017年10月20日                                      
     ** @param  sourceIp 需要匹配的IP 192.168.2.91
     ** @param  match    匹配规则     192.168.*.*
     ** @return                                              
     * *******************************************************
     */
    public static boolean matchIp(String sourceIp, String match) {
    	if(match.startsWith("*"))
    		match = "/" + match;
    	
    	return sourceIp.matches(match);
    }
}
