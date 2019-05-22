package cn.pings.commons.filter;


import cn.pings.commons.util.net.NetworkUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.IOException;
import java.util.*;
import java.util.regex.*;

/**
 *********************************************************
 ** @desc  ：参数过滤器,过滤敏感字符，防止sql注入和xss攻击
 ** @author  Pings
 ** @date    2017-10-16
 ** @version v1.0
 * *******************************************************
 */
public class ParameterFilter implements Filter {

    private static final Logger log = LoggerFactory.getLogger(ParameterFilter.class);

    private Set<String> excludesPattern;

    @Override
    public void init(FilterConfig config) throws ServletException {
        String param = config.getInitParameter("exclusions");
        if (param != null && param.trim().length() != 0) {
            this.excludesPattern = new HashSet<>(Arrays.asList(param.split("\\s*,\\s*")));
        }
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) servletRequest;

        if(!NetworkUtil.isExclusion(req.getRequestURI(), this.excludesPattern))
            filterChain.doFilter(new ParameterRequestWrapper(req), servletResponse);
        else
            filterChain.doFilter(servletRequest, servletResponse);
    }

    @Override
    public void destroy() {}

    /**
     *********************************************************
     ** @desc  ：装饰HttpServletRequest,过滤敏感字符，防止sql注入和xss攻击
     ** @author  Pings
     ** @date    2017-10-16
     ** @version v1.0
     * *******************************************************
     */
    class ParameterRequestWrapper extends HttpServletRequestWrapper {

        public ParameterRequestWrapper(HttpServletRequest request) {
            super(request);
        }

        @Override
        public String getParameter(String name) {
            return this.filter(super.getParameter(name));
        }

        @Override
        public String[] getParameterValues(String name) {
            String[] values = super.getParameterValues(name);

            if(values == null) return null;

            for (int i = 0; i < values.length; i++) {
                values[i] = this.filter(values[i]);
            }

            return values;
        }

        @Override
        public Map<String, String[]> getParameterMap() {
            Map<String, String[]> parameters = new HashMap<>();
            parameters.putAll(super.getParameterMap());

            Iterator<Map.Entry<String, String[]>> it = parameters.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<String, String[]> paramter = it.next();
                paramter.setValue(this.filter(paramter.getValue()));
            }

            return parameters;
        }

        /**
         *********************************************************
         ** @desc ：过滤敏感字符
         ** @author Pings
         ** @date   2017/10/16
         ** @param  values
         * *******************************************************
         */
        private String[] filter(String[] values) {
            if (values == null) return null;

            for(int i = 0; i < values.length; i++) {
                values[i] = this.filter(values[i]);
            }

            return values;
        }

        /**
         *********************************************************
         ** @desc ：过滤敏感字符
         ** @author Pings
         ** @date   2017/10/16
         ** @param  value
         * *******************************************************
         */
        public String filter(String value) {
            if (value == null) return null;

            String reg = "update |delete |insert |truncate |execute |script";

            Matcher matcher = Pattern.compile(reg).matcher(value);
            if(matcher.find()) {
                log.error("不安全的参数: " + value);
                return matcher.replaceAll("");
            }

            return value;
        }
    }
}

/*
<filter>
	<filter-name>ParameterFilter</filter-name>
	<filter-class>cn.pings.commons.filter.ParameterFilter</filter-class>
	<init-param>
	    <param-name>exclusions</param-name>
	    <param-value>*.js,*.gif,*.jpg,*.png,*.css,*.ico,/druid</param-value>
	</init-param>
</filter>
*/
