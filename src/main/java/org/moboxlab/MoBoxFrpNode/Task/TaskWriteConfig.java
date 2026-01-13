package org.moboxlab.MoBoxFrpNode.Task;

import com.alibaba.fastjson.JSONObject;
import org.moboxlab.MoBoxFrpNode.BasicInfo;
import org.mossmc.mosscg.MossLib.File.FileCheck;

import java.io.BufferedWriter;
import java.io.FileWriter;

public class TaskWriteConfig {
    public static void executeTask(String path, JSONObject data){
        try {
            //构建配置文件结构
            StringBuilder builder = new StringBuilder();
            builder.append("bindPort = ").append(data.getString("portServer")).append("\r\n");
            builder.append("auth.method = \"token\"\r\n");
            builder.append("auth.token = \"").append(data.getString("token")).append("\"\r\n");
            //构建端口范围
            String[] portList = data.getString("portOpen").split(",");
            String rangePort = "  { start = <start>, end = <end> }";
            String singlePort = "  { single = <single> }";
            builder.append("allowPorts = [\r\n");
            boolean first = true;
            for (String port : portList) {
                if (!first) {
                    builder.append(",\r\n");
                }
                if (port.contains("-")) {
                    String[] range = port.split("-");
                    builder.append(rangePort.replace("<start>",range[0]).replace("<end>",range[1]));
                } else {
                    builder.append(singlePort.replace("<single>",port));
                }

                first = false;
            }
            builder.append("\r\n");
            builder.append("]");
            //写入配置文件
            BufferedWriter writer = new BufferedWriter(new FileWriter(path));
            writer.write(builder.toString());
            writer.flush();
            writer.close();
            //释放frp本体
            String systemType = BasicInfo.config.getString("systemType");
            String token = data.getString("token");
            switch (systemType) {
                case "Windows":
                    FileCheck.checkFileExist("./MoBoxFrp/frp/frps_"+token+".exe","frp/frps.exe");
                    break;
                case "Linux":
                    FileCheck.checkFileExist("./MoBoxFrp/frp/frps_"+token,"frp/frps");
                    break;
                default:
                    BasicInfo.logger.sendWarn("不支持的平台类型: " + systemType);
                    break;
            }
        } catch (Exception e) {
            BasicInfo.logger.sendException(e);
            BasicInfo.logger.sendWarn("执行任务WriteConfig时出现异常！");
        }
    }
}
