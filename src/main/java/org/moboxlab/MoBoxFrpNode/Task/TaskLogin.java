package org.moboxlab.MoBoxFrpNode.Task;

import com.alibaba.fastjson.JSONObject;
import org.moboxlab.MoBoxFrpNode.API.APILogin;
import org.moboxlab.MoBoxFrpNode.BasicInfo;

public class TaskLogin {
    public static void executeTask(){
        try {
            JSONObject loginData = APILogin.getResult();
            if (loginData == null) {
                BasicInfo.logger.sendError("无法连接到主控，三秒后重试！");
                Thread.sleep(3000L);
                executeTask();
                return;
            }
            if (!loginData.getBoolean("success")) {
                BasicInfo.logger.sendError("登录失败："+loginData.getString("message"));
                return;
            }

            BasicInfo.logger.sendInfo("登录成功！");
            BasicInfo.logger.sendInfo("节点编码："+loginData.getString("nodeID"));
            BasicInfo.logger.sendInfo("节点名称："+loginData.getString("name"));
            BasicInfo.logger.sendInfo("节点端口："+loginData.getString("portStart")+"-"+loginData.getString("portEnd"));
        } catch (Exception e) {
            BasicInfo.logger.sendException(e);
            BasicInfo.logger.sendWarn("执行任务Login时出现异常！");
        }
    }
}
