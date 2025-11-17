package io.renren.zapi.channel.channels.crowq;


import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

public class HttpClient {


    private static final int CONNEC_TTIME_OUT = 10000; //设置连接主机服务器的超时时间
    private static final int READ_TIME_OUT = 15000; //设置读取远程返回的数据时间
    private static final String CHARSET = "UTF-8";


    public static final String[] contentTypes = {
            "application/json;charset=utf-8", "application/x-www-form-urlencoded;charset=utf-8", "text/xml;charset=UTF-8", "form-data"
    };

    public static final String[] methods = {
            "GET", "POST", "HEAD", "OPTIONS", "PUT", "DELETE", "TRACE"
    };


    public static String doPost(String httpUrl,String reqParam)throws Exception{
        String contentType = contentTypes[0];
        return doRequest(httpUrl, methods[1], CONNEC_TTIME_OUT, READ_TIME_OUT, reqParam, contentType, null, CHARSET);
    }




    public static String doRequest(String httpUrl,String method,int connecTimeOut,int readTimeOut,String param,String contentType,Map<String, String> header,String charset)throws Exception {
        HttpURLConnection connection = null;
        InputStream inputStream = null;
        BufferedReader bufferedReader = null;
        String result = "";
        try {


            URL realUrl = new URL(httpUrl);
            connection = (HttpURLConnection) realUrl.openConnection();

            connection.setRequestMethod(method);
            connection.setConnectTimeout(connecTimeOut);
            connection.setReadTimeout(readTimeOut);
            // 默认值为：false，当向远程服务器传送数据/写数据时，需要设置为true
            connection.setDoOutput(true);
            // 默认值为：true，当前向远程服务读取数据时，设置为true，该参数可有可无
            connection.setDoInput(true);
            connection.setUseCaches(false);
            connection.setRequestProperty("Content-Type", contentType);
            if (header != null && header.size() != 0){
                for (String key : header.keySet()) {
                    connection.setRequestProperty(key, header.get(key));
                }
            }


            // 通过连接对象获取一个输出流,此处getOutputStream会隐含的进行connect(即：如同调用上面的connect()方法，这里才是正式发送请求
            OutputStream outputStream = connection.getOutputStream();
            // 通过输出流对象将参数写出去/传输出去,它是通过字节数组写出的

            outputStream.write(param.getBytes(charset));
            outputStream.flush();
            outputStream.close();


            inputStream = connection.getInputStream();
            // 对输入流对象进行包装:charset根据工作项目组的要求来设置
            bufferedReader = new BufferedReader(new InputStreamReader(inputStream, charset));
            // 循环遍历一行一行读取数据
            StringBuilder resultBuilder = new StringBuilder();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                resultBuilder.append(line);
            }
            result = resultBuilder.toString();

        } finally {
            try {
                if (bufferedReader != null) {
                    bufferedReader.close();
                }
                if(inputStream != null){
                    inputStream.close();
                }
                if (connection != null) {
                    connection.disconnect();
                }
            } catch (Exception e2) {

            }
        }
        return result;
    }


}