package org.moboxlab.MoBoxFrpNode.Task;

import com.alibaba.fastjson.JSONObject;
import org.moboxlab.MoBoxFrpNode.BasicInfo;

public class TaskRemoveLimit {
    public static void executeTask(JSONObject data){
        try {
            int band = data.getInteger("band");
            int port = data.getInteger("portServer");
            String token = data.getString("token");
            String systemType = BasicInfo.config.getString("systemType");
            switch (systemType) {
                case "Windows":
                    removeWindowsLimit(token);
                    break;
                case "Linux":
                    removeLinuxLimit(band,port);
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
    private static void removeWindowsLimit(String token) throws Exception{
        StringBuilder command = new StringBuilder();
        command.append("powershell.exe Remove-NetQosPolicy ");
        command.append("-Name \"frps_").append(token).append("\" ");
        command.append("-Confirm:$false");
        TaskExecuteCommand.executeTask(command.toString());
    }

    private static void removeLinuxLimit(int band, int port) throws Exception{

    }
}
