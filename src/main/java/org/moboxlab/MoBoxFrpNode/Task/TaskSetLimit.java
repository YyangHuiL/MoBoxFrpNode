package org.moboxlab.MoBoxFrpNode.Task;

import com.alibaba.fastjson.JSONObject;
import org.moboxlab.MoBoxFrpNode.BasicInfo;
import org.moboxlab.MoBoxFrpNode.Cache.CacheClassID;

import java.util.ArrayList;
import java.util.List;

public class TaskSetLimit {
    public static void executeTask(JSONObject data){
        try {
            int band = data.getInteger("band");
            int port = data.getInteger("portServer");
            String portOpen = data.getString("portOpen");
            String token = data.getString("token");
            String systemType = BasicInfo.config.getString("systemType");
            switch (systemType) {
                case "Windows":
                    setWindowsLimit(token,band);
                    break;
                case "Linux":
                    setLinuxLimit(band,port,portOpen,token);
                    break;
                default:
                    BasicInfo.logger.sendWarn("不支持的平台类型: " + systemType);
                    break;
            }
        } catch (Exception e) {
            BasicInfo.logger.sendException(e);
            BasicInfo.logger.sendWarn("执行任务SetLimit时出现异常！");
        }
    }

    @SuppressWarnings("StringBufferReplaceableByString")
    private static void setWindowsLimit(String token, int band){
        StringBuilder command = new StringBuilder();
        command.append("powershell.exe New-NetQosPolicy ");
        command.append("-Name \"frps_").append(token).append("\" ");
        command.append("-ThrottleRate ").append(band*1000000).append(" ");
        command.append("-AppPathName \"frps_").append(token).append(".exe\" ");
        command.append("-IPProtocol \"Both\"");
        TaskExecuteCommand.executeTask(command.toString());
    }

    @SuppressWarnings("ExtractMethodRecommender")
    private static void setLinuxLimit(int band, int portServer, String portOpen, String token){
        String network = BasicInfo.config.getString("network");
        String classID = CacheClassID.getClassID(token);
        //创建限速子类
        StringBuilder command = new StringBuilder();
        command.append("tc class add dev ").append(network).append(" ");
        command.append("parent 1:1 ");
        command.append("classid 1:").append(classID).append(" ");
        command.append("htb rate ").append(band).append("mbit ");
        command.append("ceil ").append(band).append("mbit ");
        command.append("burst ").append(band * 32).append("kb ");
        command.append("cburst ").append(band * 40).append("kb");
        TaskExecuteCommand.executeTask(command.toString());
        //构建端口列表
        List<String> ports = new ArrayList<>();
        ports.add(String.valueOf(portServer));
        //处理开放端口范围
        String[] openList = portOpen.split(",");
        for (String port : openList) {
            if (port.contains("-")) {
                String[] range = port.split("-");
                int start = Integer.parseInt(range[0]);
                int end = Integer.parseInt(range[1]);
                for (int i = start; i <= end; i++) {
                    ports.add(String.valueOf(i));
                }
            } else {
                ports.add(port);
            }
        }
        for (String port : ports) {
            //添加出站过滤器
            command = new StringBuilder();
            command.append("tc filter add dev ").append(network).append(" ");
            command.append("protocol ip ");
            command.append("parent 1:0 ");
            command.append("prio 1 u32 match ip ");
            command.append("sport ").append(port).append(" 0xffff ");
            command.append("flowid 1:").append(classID);
            TaskExecuteCommand.executeTask(command.toString());
        }
    }
}
