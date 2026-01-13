package org.moboxlab.MoBoxFrpNode.Task;

import com.alibaba.fastjson.JSONObject;
import org.moboxlab.MoBoxFrpNode.BasicInfo;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

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
    private static void clearWindowsLimit() throws Exception{
        StringBuilder command = new StringBuilder();
        command.append("powershell.exe Remove-NetQosPolicy -Confirm:$false");
        Process powerShellProcess = Runtime.getRuntime().exec(command.toString());
        BufferedReader powershellOutput = new BufferedReader(new InputStreamReader(powerShellProcess.getInputStream()));
        BufferedReader powershellError = new BufferedReader(new InputStreamReader(powerShellProcess.getErrorStream()));
        String powershellInfo = powershellOutput.readLine();
        while (powershellInfo != null) {
            BasicInfo.sendDebug(powershellInfo);
            powershellInfo = powershellOutput.readLine();
        }
        powershellInfo = "";
        while (powershellInfo != null) {
            BasicInfo.sendDebug(powershellInfo);
            powershellInfo = powershellError.readLine();
        }
        powerShellProcess.waitFor();
        powerShellProcess.destroy();
        powershellOutput.close();
    }

    private static void clearLinuxLimit() throws Exception{

    }
}
