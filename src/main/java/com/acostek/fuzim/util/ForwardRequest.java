package com.acostek.fuzim.util;

import com.google.gson.Gson;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Map;

/**
 * Created by ZJ on 2018/5/1.
 * post发送数据需要两种形式，如果接收的controller层使用实体接收，就需要进行以json格式发送，
 * 如果以单独字段进行接收，就需要进行键值对的形式发送
 */
public class ForwardRequest {

    private static final String DEF_CHATSET = "UTF-8";
    private static final int DEF_CONN_TIMEOUT = 60000;
    private static final int DEF_READ_TIMEOUT = 60000;
    private static String userAgent = "Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/29.0.1547.66 Safari/537.36";

    /**
     * @param strUrl  访问地址路径url
     * @param params  提交的参数map集合
     * @param method  访问方式GET或者POST
     * @param token   权限验证，如果没有选择空即可
     * @return String
     * @throws Exception
     */
    public static String net(String method, String strUrl, Map params, String token) throws Exception {
        HttpURLConnection conn = null;
        BufferedReader reader = null;
        String rs = null;
        try {
            StringBuffer sb = new StringBuffer();
            if (method == null || method.equals("GET")) {
                strUrl = strUrl + "?" + urlencode(params);
            }
            URL url = new URL(strUrl);
            conn = (HttpURLConnection) url.openConnection();
            //设置请求方式
            if (method == null || method.equals("GET")) {
                conn.setRequestMethod("GET");
            } else {
                conn.setDoOutput(true);
                conn.setRequestMethod("POST");
                //设置请求体的格式
                if ("entity".equals(params.get("paramType"))) {
                    conn.setRequestProperty("Content-Type","application/json");
                } else {
                    conn.setRequestProperty("Content-Type","application/x-www-form-urlencoded");
                }
                conn.setRequestProperty("Content-Length","content.length");

            }
            conn.setRequestProperty("User-agent", userAgent);
            conn.setRequestProperty("Authorization",token);
            conn.setUseCaches(false);
            conn.setConnectTimeout(DEF_CONN_TIMEOUT);
            conn.setReadTimeout(DEF_READ_TIMEOUT);
            conn.setInstanceFollowRedirects(false);
            conn.connect();
            if (params != null && method.equals("POST")) {
                try {
                    DataOutputStream out = new DataOutputStream(conn.getOutputStream());
                    if ("entity".equals(params.get("paramType"))) {
                        //如果接收参数为实体类型，就进行json转码然后发送
                        Gson gson = new Gson();
                        out.writeBytes(gson.toJson(params));
                    } else {
                        //如果接收的参数为几个字段类型，就是用键值对的形式进行urlencode进行编码发送
                        out.writeBytes(urlencode(params));
                    }
                } catch (Exception e) {
                   e.printStackTrace();
                }
            }
            InputStream is = conn.getInputStream();
            reader = new BufferedReader(new InputStreamReader(is, DEF_CHATSET));
            String strRead = null;
            while ((strRead = reader.readLine()) != null) {
                sb.append(strRead);
            }
            rs = sb.toString();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                reader.close();
            }
            if (conn != null) {
                conn.disconnect();
            }
        }
        return rs;
    }

    //将map型转为请求参数型（get访问时使用）
    public static String urlencode(Map<String, Object> data) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry i : data.entrySet()) {
            try {
                sb.append(i.getKey()).append("=").append(URLEncoder.encode(i.getValue() + "", "UTF-8")).append("&");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        return sb.toString();
    }


}
