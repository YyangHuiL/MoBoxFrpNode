package org.moboxlab.MoBoxFrpNode.Tick;

import org.moboxlab.MoBoxFrpNode.BasicInfo;
import org.moboxlab.MoBoxFrpNode.Task.TaskCodeUpdate;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TickCode {
    public static void runTick() {
        ExecutorService singleThreadExecutor = Executors.newSingleThreadExecutor();
        String name = "tickThread-CodeUpdate";
        Thread thread = new Thread(TickCode::tickThread);
        thread.setName(name);
        singleThreadExecutor.execute(thread);
        BasicInfo.logger.sendInfo("已启动Tick线程："+name);
    }

    @SuppressWarnings("BusyWait")
    private static void tickThread() {
        while (true) {
            try {
                Thread.sleep(5000L);
                TaskCodeUpdate.executeTask();
            } catch (Exception e) {
                BasicInfo.logger.sendException(e);
                BasicInfo.logger.sendWarn("Tick线程"+Thread.currentThread().getName()+"执行时出现错误！");
            }
        }
    }
}
