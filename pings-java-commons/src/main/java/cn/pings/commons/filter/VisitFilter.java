package cn.pings.commons.filter;


import cn.pings.commons.util.net.NetworkUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 *********************************************************
 ** @desc  ：访问控制过滤器
 **          1.控制IP是否能访问系统
 **          2.控制单个IP每秒的最大访问次数
 ** @author  Pings
 ** @date    2017-10-17
 ** @version v1.1
 **
 ** V1.1 添加允许IP列表  Pings 2017-10-20
 * *******************************************************
 */
public class VisitFilter implements Filter {

    private static Logger log = LoggerFactory.getLogger(VisitFilter.class);
    //**访问日志
    public static final Map<String, NetVisit> VISIT_LOG = new ConcurrentHashMap<>();
    //**排除的访问地址
    private Set<String> excludesPattern;
    //**拒绝访问IP列表
    private static Set<String> rejectIps;
    //**允许访问IP列表
    private static Set<String> allowIps;
    //**IP每秒的最大访问次数
    private static int maxVisit = 50;

    @Override
    public void init(FilterConfig config) throws ServletException {
        String param = config.getInitParameter("exclusions");
        if (param != null && param.trim().length() != 0) {
            this.excludesPattern = new HashSet<>(Arrays.asList(param.split("\\s*,\\s*")));
        }

        param = config.getInitParameter("rejectIps");
        if (param != null && param.trim().length() != 0) {
            rejectIps = new HashSet<>(Arrays.asList(param.split("\\s*,\\s*")));
        }
        
        param = config.getInitParameter("allowIps");
        if (param != null && param.trim().length() != 0) {
        	allowIps = new HashSet<>(Arrays.asList(param.split("\\s*,\\s*")));
        }

        param = config.getInitParameter("maxVisit");
        if (param != null && param.trim().length() != 0) {
            maxVisit = Integer.parseInt(param);
        }
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) servletRequest;
        HttpServletResponse resp = (HttpServletResponse) servletResponse;

        //**排除的请求地址
        if(NetworkUtil.isExclusion(req.getRequestURI(), this.excludesPattern)) {
            filterChain.doFilter(servletRequest, servletResponse);
            return;
        }

        //**禁止访问的IP
        String ip = NetworkUtil.getIp(req);
        if(this.isReject(ip) || !this.isAllow(ip)) {
            log.error(ip + "：禁止访问");

            resp.setContentType("text/html;charset=UTF-8");
            resp.getWriter().write(ip + "：系统拒绝您访问");
            resp.getWriter().close();
            return;
        }

        //**IP每秒的访问次数
        int count = VISIT_LOG.containsKey(ip) ? VISIT_LOG.get(ip).add() : VISIT_LOG.put(ip, new NetVisit(ip)) == null ? 1 : 1;

        //**IP每秒的访问次数 > 最大访问次数
        if(count > maxVisit) {
            log.error(ip + "：访问次数超过允许的每秒最大访问次数，禁止访问");

            resp.setContentType("text/html;charset=UTF-8");
            resp.getWriter().write(ip + "：您的访问次数超过最大限制");
            resp.getWriter().close();
            return;
        }

        //**IP每秒的访问次数 > 最大访问次数/2
        if(count > maxVisit / 2) {
            log.warn(ip + "：访问次数超过允许的每秒最大访问次数的一半");
        }

        filterChain.doFilter(servletRequest, servletResponse);
    }

    @Override
    public void destroy() {}
    
    /*是否匹配拒绝IP列表*/
	private boolean isReject(String ip) {
		//**拒绝列表默认关闭
		if (rejectIps == null) return false;
		
		for (String rejectIp : rejectIps) {
			if (NetworkUtil.matchIp(ip, rejectIp))
				return true;
		}

		return false;
    }
	
	/*是否匹配允许IP列表*/
	private boolean isAllow(String ip) {
		//**允许列表默认关闭
		if (allowIps == null) return true;
		
		for (String allowIp : allowIps) {
			if (NetworkUtil.matchIp(ip, allowIp))
				return true;
		}

		return false;
	}

    /**
     *********************************************************
     ** @desc  ： 网络访问对象
     ** @author  Pings
     ** @date    2017-10-17
     ** @version v1.0
     * *******************************************************
     */
    static class NetVisit {
        public static final SimpleDateFormat format = new SimpleDateFormat("YYYY-MM-dd HH:mm:ss");
        //**访问IP
        private String ip;
        //**访问次数
        private int count;
        //**当前时间
        private Date current;

        public NetVisit(String ip) {
            this(ip, 1, new Date());
        }

        public NetVisit(String ip, int count, Date current) {
            this.ip = ip;
            this.count = count;
            this.current = current;
        }

        public String getIp() {
            return ip;
        }

        public synchronized int getCount() {
            return count;
        }

        public Date getCurrent() {
            return current;
        }

        /**
         *********************************************************
         ** @desc ：  每秒访问的次数增加
         ** @author Pings
         ** @date   2017/10/17
         * *******************************************************
         */
        public int add() {
            synchronized (this) {
                Date now = new Date();

                if (format.format(now).equals(format.format(this.getCurrent()))) {
                    this.count++;
                } else {
                    this.count = 1;
                    this.current = now;
                }

                return this.count;
            }
        }
    }
}
/*
<filter>
	<filter-name>VisitFilter</filter-name>
	<filter-class>cn.pings.commons.filter.VisitFilter</filter-class>
	<!-- 排除的请求地址 -->
	<init-param>
		<param-name>exclusions</param-name>
		<param-value>*.js,*.gif,*.jpg,*.png,*.css,*.ico</param-value>
	</init-param>
	<!-- 拒绝的IP列表,不匹配或者为值空代表关闭 -->
	<init-param>
		<param-name>rejectIps</param-name>
		<param-value>192.168.2.91,127.0.0.2</param-value>
	</init-param>
	<!-- 允许的IP列表,不匹配或者为值空代表关闭 -->
	<init-param>
		<param-name>allowIps</param-name>
		<param-value>*.*.*.*</param-value>
	</init-param>
	<!-- IP每秒访问次数的最大值，不包括对exclusions配置的地址的访问 -->
	<init-param>
		<param-name>maxVisit</param-name>
		<param-value>50</param-value>
	</init-param>
</filter>
*/