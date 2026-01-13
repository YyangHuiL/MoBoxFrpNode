package org.moboxlab.MoBoxFrpNode.Task;

import com.alibaba.fastjson.JSONObject;
import org.moboxlab.MoBoxFrpNode.BasicInfo;

import java.io.File;

public class TaskRemoveConfig {
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static void executeTask(String path, JSONObject data){
        try {
            //删除配置文件
            File file = new File(path);
            if (file.exists()) file.delete();
            //删除frp本体
            String systemType = BasicInfo.config.getString("systemType");
            String token = data.getString("token");
            switch (systemType) {
                case "Windows":
                    file = new File("./MoBoxFrp/frp/frps_"+token+".exe");
                    file.delete();
                    break;
                case "Linux":
                    file = new File("./MoBoxFrp/frp/frps_"+token);
                    file.delete();
                    break;
                default:
                    BasicInfo.logger.sendWarn("不支持的平台类型: " + systemType);
                    break;
            }
        } catch (Exception e) {
            BasicInfo.logger.sendException(e);
            BasicInfo.logger.sendWarn("执行任务RemoveConfig时出现异常！");
        }
    }
}
