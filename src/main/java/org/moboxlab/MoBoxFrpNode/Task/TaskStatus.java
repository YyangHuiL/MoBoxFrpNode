package org.moboxlab.MoBoxFrpNode.Task;

import com.alibaba.fastjson.JSONObject;
import org.moboxlab.MoBoxFrpNode.API.APIStatus;
import org.moboxlab.MoBoxFrpNode.BasicInfo;
import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.HardwareAbstractionLayer;
import oshi.hardware.NetworkIF;

import java.text.DecimalFormat;
import java.util.concurrent.TimeUnit;

public class TaskStatus {
    public static void executeTask(){
        try {
            //初始化对象
            JSONObject request = new JSONObject();
            SystemInfo info = new SystemInfo();
            HardwareAbstractionLayer hardware = info.getHardware();
            CentralProcessor processor = info.getHardware().getProcessor();

            //内存模块
            long memoryAvailable = hardware.getMemory().getAvailable();
            long memoryTotal = hardware.getMemory().getTotal();
            long memoryUsed = memoryTotal-memoryAvailable;
            double used = (memoryUsed)/1024.0/1024.0/1024.0;
            double total = (memoryTotal)/1024.0/1024.0/1024.0;
            request.put("memoryUsage",used);
            request.put("memoryMax",total);

            //处理器模块
            long[] prevTicks = processor.getSystemCpuLoadTicks();
            TimeUnit.SECONDS.sleep(1);
            long[] ticks = processor.getSystemCpuLoadTicks();
            long nice = ticks[CentralProcessor.TickType.NICE.getIndex()] - prevTicks[CentralProcessor.TickType.NICE.getIndex()];
            long irq = ticks[CentralProcessor.TickType.IRQ.getIndex()] - prevTicks[CentralProcessor.TickType.IRQ.getIndex()];
            long softirq = ticks[CentralProcessor.TickType.SOFTIRQ.getIndex()] - prevTicks[CentralProcessor.TickType.SOFTIRQ.getIndex()];
            long steal = ticks[CentralProcessor.TickType.STEAL.getIndex()] - prevTicks[CentralProcessor.TickType.STEAL.getIndex()];
            long cSys = ticks[CentralProcessor.TickType.SYSTEM.getIndex()] - prevTicks[CentralProcessor.TickType.SYSTEM.getIndex()];
            long user = ticks[CentralProcessor.TickType.USER.getIndex()] - prevTicks[CentralProcessor.TickType.USER.getIndex()];
            long iowait = ticks[CentralProcessor.TickType.IOWAIT.getIndex()] - prevTicks[CentralProcessor.TickType.IOWAIT.getIndex()];
            long idle = ticks[CentralProcessor.TickType.IDLE.getIndex()] - prevTicks[CentralProcessor.TickType.IDLE.getIndex()];
            long totalCpu = user + nice + cSys + idle + iowait + irq + softirq + steal;
            request.put("cpuUsage",new DecimalFormat("#.##").format(1.0-(idle * 1.0 / totalCpu)));

            //网络模块
            long bytesReceiveStart = 0;
            long bytesSendStart = 0;
            for (NetworkIF net : hardware.getNetworkIFs()) {
                bytesReceiveStart = bytesReceiveStart + net.getBytesRecv();
                bytesSendStart = bytesSendStart + net.getBytesSent();
            }
            TimeUnit.SECONDS.sleep(1);
            long bytesReceiveEnd = 0;
            long bytesSendEnd = 0;
            for (NetworkIF net : hardware.getNetworkIFs()) {
                bytesReceiveEnd = bytesReceiveEnd + net.getBytesRecv();
                bytesSendEnd = bytesSendEnd + net.getBytesSent();
            }
            double upload = (bytesSendEnd-bytesSendStart)/128000.0;
            double download = (bytesReceiveEnd-bytesReceiveStart)/128000.0;
            double uploadTotal = bytesSendEnd/1073741824.0;
            double downloadTotal = bytesReceiveEnd/1073741824.0;
            request.put("bandUpload",upload);
            request.put("bandDownload",download);
            request.put("bandUploadTotal",uploadTotal);
            request.put("bandDownloadTotal",downloadTotal);

            JSONObject result = APIStatus.getResult(request);
            if (result == null) {
                BasicInfo.logger.sendWarn("回传状态失败：无法连接到主控！");
                return;
            }
            if (!result.getBoolean("success")) {
                BasicInfo.logger.sendWarn("回传状态失败："+result.getString("message"));
                return;
            }
            BasicInfo.sendDebug("已更新并向主控回传状态！");
        } catch (Exception e) {
            BasicInfo.logger.sendException(e);
            BasicInfo.logger.sendWarn("执行任务Status时出现异常！");
        }
    }
}
