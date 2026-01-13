package org.moboxlab.MoBoxFrpNode.Task;

import org.moboxlab.MoBoxFrpNode.BasicInfo;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class TaskExecuteCommand {
    public static void executeTask(String command){
        try {
            Process process = Runtime.getRuntime().exec(command);
            BufferedReader output = new BufferedReader(new InputStreamReader(process.getInputStream()));
            BufferedReader error = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            String info = output.readLine();
            while (info != null) {
                BasicInfo.sendDebug(info);
                info = output.readLine();
            }
            info = "";
            while (info != null) {
                BasicInfo.sendDebug(info);
                info = error.readLine();
            }
            process.waitFor();
            process.destroy();
            output.close();
        } catch (Exception e) {
            BasicInfo.logger.sendException(e);
            BasicInfo.logger.sendWarn("执行任务ExecuteCommand时出现异常！");
        }
    }
}
