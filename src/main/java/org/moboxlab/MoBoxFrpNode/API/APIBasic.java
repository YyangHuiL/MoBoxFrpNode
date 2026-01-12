package org.moboxlab.MoBoxFrpNode.API;

import com.alibaba.fastjson.JSONObject;
import org.moboxlab.MoBoxFrpNode.BasicInfo;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class APIBasic {
    public static JSONObject postAPI(String route,JSONObject data) {
        try {
            //初始化必要信息
            String address = BasicInfo.config.getString("address");
            String nodeID = BasicInfo.config.getString("nodeID");
            String nodeAuth = BasicInfo.config.getString("nodeAuth");
            data.put("node",nodeID);
            data.put("auth",nodeAuth);
            //建立连接
            URL targetURL = new URL(address+route);
            HttpURLConnection connection = (HttpURLConnection) targetURL.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Connection", "keep-alive");
            connection.setRequestProperty("Content-Type","application/json;charset=utf8");
            connection.setRequestProperty("User-Agent", "MoBoxFrpNode/"+BasicInfo.version+BasicInfo.versionType);
            connection.setDoOutput(true);
            //写入请求数据
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(connection.getOutputStream(), StandardCharsets.UTF_8));
            writer.write(data.toString());
            writer.flush();
            writer.close();
            //读取响应数据
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8));
            String inputLine;
            StringBuilder readInfo = new StringBuilder();
            while ((inputLine = reader.readLine()) != null) readInfo.append(inputLine);
            reader.close();
            //返回数据
            JSONObject result = JSONObject.parseObject(readInfo.toString());
            BasicInfo.sendDebug(result.toJSONString());
            return result;
        } catch (Exception e) {
            //出错
            BasicInfo.logger.sendException(e);
            BasicInfo.sendDebug(data.toJSONString());
            BasicInfo.logger.sendWarn("API请求失败，请查看debug信息以确认问题！");
            return null;
        }
    }
}
