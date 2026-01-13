package org.moboxlab.MoBoxFrpNode.Task;

import org.moboxlab.MoBoxFrpNode.BasicInfo;
import org.mossmc.mosscg.MossLib.File.FileCheck;

import java.io.File;

public class TaskUpdateFile {
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static void executeTask(){
        try {
            File file = new File("./MoBoxFrp/frp");
            if (!file.exists()) return;
            File[] list = file.listFiles();
            if (list != null) {
                for (File rubbish : list) {
                    rubbish.delete();
                }
            }
            FileCheck.checkDirExist("./MoBoxFrp/frp");
        } catch (Exception e) {
            BasicInfo.logger.sendException(e);
            BasicInfo.logger.sendWarn("执行任务UpdateFile时出现异常！");
        }
    }
}
