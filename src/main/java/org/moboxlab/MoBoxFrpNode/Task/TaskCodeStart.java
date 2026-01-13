package org.moboxlab.MoBoxFrpNode.Task;

import com.alibaba.fastjson.JSONObject;
import org.moboxlab.MoBoxFrpNode.BasicInfo;
import org.moboxlab.MoBoxFrpNode.Cache.CacheProcess;
import org.moboxlab.MoBoxFrpNode.Object.ObjectProcess;

public class TaskCodeStart {
    public static void executeTask(String key, JSONObject data){
        try {
            ObjectProcess process = new ObjectProcess();
            process.name = key;
            process.data = data;
            process.start();
            CacheProcess.processMap.put(key,process);
        } catch (Exception e) {
            BasicInfo.logger.sendException(e);
            BasicInfo.logger.sendWarn("执行任务CodeStart时出现异常！");
        }
    }
}
