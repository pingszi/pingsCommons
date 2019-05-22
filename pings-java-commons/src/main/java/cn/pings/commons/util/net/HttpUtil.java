package cn.pings.commons.util.net;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 *********************************************************
 ** @desc  ： http客户端(HttpURLConnection)
 ** @author  Pings
 ** @date    2018/1/2
 ** @version v1.0
 * *******************************************************
 */
public class HttpUtil {

    //**默认的字符集
    public static final String DEFAULT_CHARTSET = "UTF-8";

    /**
     *********************************************************
     ** @desc ： http get请求
     ** @author Pings
     ** @date   2018/1/2
     ** @param getUrl url地址
     * *******************************************************
     */
    public static String get(String getUrl) {
        StringBuilder rst = new StringBuilder();
        HttpURLConnection connection = null;
        BufferedReader br = null;

        try {
            URL url = new URL(getUrl);

            //**打开连接
            connection = (HttpURLConnection) url.openConnection();
            //**连接会话
            connection.connect();
            //**获取输入流
            br = new BufferedReader(new InputStreamReader(connection.getInputStream(), DEFAULT_CHARTSET));
            String line;
            //**循环读取流
            while ((line = br.readLine()) != null) {
                rst.append(line);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            //**关闭流
            if(br != null)
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            //**断开连接
            if(connection != null)
                connection.disconnect();
        }

        return rst.toString();
    }
}
