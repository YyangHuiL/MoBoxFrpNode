package org.moboxlab.MoBoxFrpNode.Task;

import org.moboxlab.MoBoxFrpNode.BasicInfo;
import org.moboxlab.MoBoxFrpNode.Cache.CacheClassID;

public class TaskClearLimit {
    public static void executeTask(){
        try {
            String systemType = BasicInfo.config.getString("systemType");
            switch (systemType) {
                case "Windows":
                    clearWindowsLimit();
                    break;
                case "Linux":
                    clearLinuxLimit();
                    break;
                default:
                    BasicInfo.logger.sendWarn("不支持的平台类型: " + systemType);
                    break;
            }
        } catch (Exception e) {
            BasicInfo.logger.sendException(e);
            BasicInfo.logger.sendWarn("执行任务ClearLimit时出现异常！");
        }
    }

    @SuppressWarnings("StringBufferReplaceableByString")
    private static void clearWindowsLimit(){
        StringBuilder command = new StringBuilder();
        command.append("powershell.exe Remove-NetQosPolicy -Confirm:$false");
        TaskExecuteCommand.executeTask(command.toString());
    }

    private static void clearLinuxLimit(){
        String network = BasicInfo.config.getString("network");
        TaskExecuteCommand.executeTask("tc qdisc del dev "+network+" root");
        TaskExecuteCommand.executeTask("tc qdisc add dev "+network+" root handle 1: htb default 1");
        TaskExecuteCommand.executeTask("tc class add dev "+network+" parent 1: classid 1:1 htb rate 10000mbit");
        CacheClassID.initClassID();
    }
}
