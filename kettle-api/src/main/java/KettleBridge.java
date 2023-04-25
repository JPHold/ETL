
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.ChannelLogTable;
import org.pentaho.di.core.logging.JobEntryLogTable;
import org.pentaho.di.core.logging.JobLogTable;
import org.pentaho.di.core.logging.LogLevel;
import org.pentaho.di.job.Job;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Kettle桥接器，通过这个类，调用使用Kettle提供的jar包运行脚本
 * <p>
 * KettleBridge kettleBridge = new KettleBridge();
 * kettleBridge.init();
 * kettleBridge.execute(filepath, null);
 *
 * @author Mr.css
 * @version 2022-03-28 14:44
 */
public class KettleBridge {
    private Logger logger = LoggerFactory.getLogger(KettleBridge.class);

    public static void main(String[] args) {

        KettleBridge kettleBridge = new KettleBridge();

        try {
            kettleBridge.init();
            kettleBridge.tranExecute("D:\\person\\study\\2022\\ETL\\kettle-api\\src\\main\\resources\\hello.ktr"
                    , null);
        } catch (Exception e) {
            e.printStackTrace();
            kettleBridge.shutdown();
        }
    }

    /**
     * Kettle 环境初始化
     */
    public void init() {
        try {
            KettleEnvironment.init(true);
            logger.debug("【Configuration】Kettle environment init succeed！");
        } catch (KettleException e) {
            e.printStackTrace();
        }
    }

    /**
     * 设置输出日志，将日志输出到数据库表里
     *
     * @param jobMeta -
     */
    private void initDatabase(JobMeta jobMeta) {

        // 任务日志
        JobLogTable jobLogTable = JobLogTable.getDefault(jobMeta, jobMeta);
        jobLogTable.setConnectionName("med");
        jobLogTable.setSchemaName("med");
        jobLogTable.setTableName("t_kettle_job_log");
        jobMeta.setJobLogTable(jobLogTable);

        // 任务节点日志
        JobEntryLogTable jobEntryLogTable = JobEntryLogTable.getDefault(jobMeta, jobMeta);
        jobEntryLogTable.setConnectionName("med");
        jobEntryLogTable.setSchemaName("med");
        jobEntryLogTable.setTableName("t_kettle_item_log");
        jobMeta.setJobEntryLogTable(jobEntryLogTable);

        // 任通道日志
        ChannelLogTable channelLogTable = ChannelLogTable.getDefault(jobMeta, jobMeta);
        channelLogTable.setConnectionName("med");
        channelLogTable.setSchemaName("med");
        channelLogTable.setTableName("t_kettle_channel_log");
        jobMeta.setChannelLogTable(channelLogTable);
    }

    /**
     * Kettle 环境销毁
     */
    public void shutdown() {
        KettleEnvironment.shutdown();
    }

    /**
     * 执行kettle脚本，Kettle以文件作为脚本的最小单位，提供脚本所在的绝对路即可
     *
     * @param path   脚本路径
     * @param params 脚本运行所需的参数变量
     * @return 执行结果
     * @throws KettleException run kettle cause any exception
     * @throws IOException     can not read kettle.properties
     */
    public void execute(String path, Map<String, Object> params) throws KettleException, IOException {
        // 初始化job路径
        JobMeta jobMeta = new JobMeta(path, null);
        Job job = new Job(null, jobMeta);
        Trans trans = new Trans();

        // 设置环境变量
        /*Map<String, String> en = this.loadVariable(params);
        for (Map.Entry<String, String> entry : en.entrySet()) {
            job.setVariable(entry.getKey(), entry.getValue());
        }*/

        // 日志设置
//        this.initDatabase(jobMeta);

        // 启动等待直到结束
        job.start();
        job.waitUntilFinished();
        if (trans.getErrors() != 0) {
            System.out.println("执行失败！");
        }

    }

    public void tranExecute(String path, Map<String, Object> params) throws KettleException, IOException {
        TransMeta transMeta = new TransMeta(path);
        Trans trans = new Trans(transMeta);

        // 启动等待直到结束
        trans.execute(null);
        trans.setLogLevel(LogLevel.NOTHING);
        trans.waitUntilFinished();
    }

}