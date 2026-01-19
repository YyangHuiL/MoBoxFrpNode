package org.moboxlab.MoBoxFrpNode.Task;

import org.moboxlab.MoBoxFrpNode.BasicInfo;
import org.mossmc.mosscg.MossLib.File.FileCheck;

import java.io.File;

public class TaskUpdateFile {
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static void executeTask(){
        try {
            FileCheck.checkDirExist("./MoBoxFrp/frp");
            File file = new File("./MoBoxFrp/frp");
            if (!file.exists()) return;
            File[] list = file.listFiles();
            if (list != null) {
                for (File rubbish : list) {
                    if (!rubbish.getName().endsWith(".toml")) {
                        TaskKillProcess.executeTask(rubbish.getName());
                    }
                    rubbish.delete();
                }
            }
        } catch (Exception e) {
            BasicInfo.logger.sendException(e);
            BasicInfo.logger.sendWarn("执行任务UpdateFile时出现异常！");
        }
    }
}
