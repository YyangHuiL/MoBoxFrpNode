package org.moboxlab.MoBoxFrpNode.Task;

import com.alibaba.fastjson.JSONObject;
import org.moboxlab.MoBoxFrpNode.BasicInfo;
import org.moboxlab.MoBoxFrpNode.Cache.CacheClassID;

public class TaskRemoveLimit {
    public static void executeTask(JSONObject data){
        try {
            String token = data.getString("token");
            String systemType = BasicInfo.config.getString("systemType");
            switch (systemType) {
                case "Windows":
                    removeWindowsLimit(token);
                    break;
                case "Linux":
                    removeLinuxLimit(token);
                    break;
                default:
                    BasicInfo.logger.sendWarn("不支持的平台类型: " + systemType);
                    break;
            }
        } catch (Exception e) {
            BasicInfo.logger.sendException(e);
            BasicInfo.logger.sendWarn("执行任务RemoveLimit时出现异常！");
        }
    }

    @SuppressWarnings("StringBufferReplaceableByString")
    private static void removeWindowsLimit(String token){
        StringBuilder command = new StringBuilder();
        command.append("powershell.exe Remove-NetQosPolicy ");
        command.append("-Name \"frps_").append(token).append("\" ");
        command.append("-Confirm:$false");
        TaskExecuteCommand.executeTask(command.toString());
    }

    private static void removeLinuxLimit(String token){
        String network = BasicInfo.config.getString("network");
        String classID = CacheClassID.getClassID(token);
        //删除过滤器
        StringBuilder command = new StringBuilder();
        command.append("tc filter del dev ").append(network).append(" ");
        command.append("protocol ip ");
        command.append("parent 1:0 ");
        command.append("prio 1 u32 ");
        command.append("flowid 1:").append(classID);
        TaskExecuteCommand.executeTask(command.toString());
        command = new StringBuilder();
        command.append("tc class del dev ").append(network).append(" ");
        command.append("parent 1:1 ");
        command.append("classid 1:").append(classID);
        TaskExecuteCommand.executeTask(command.toString());
        //释放ID
        CacheClassID.releaseClassID(token);
    }
}
