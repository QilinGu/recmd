package com.xhui.recmd.core.hao;

import com.xhui.recmd.core.redis.RedisTemplate;
import com.xhui.recmd.core.union.ColumnGoal;
import com.xhui.recmd.core.union.IndividualCommonPoint;
import com.xhui.recmd.spark.utils.HbaseApi;
import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.CellUtil;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hdfs.util.EnumCounters;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by littlehui on 2016/10/19.
 */
public class InvidualInterestPointHao {

    private static final String DEFAULT_VISIT_TABLE = "visitCount";

    private static final String CACHE_PREFIX = "visit";

    private static final Integer EXPIRE_TIME = 360;

    public List<IndividualCommonPoint> getAllIndivialInterestPoints() {
        List<IndividualCommonPoint> individualInterestPoints = new ArrayList<>();
        ResultScanner resultScanner = HbaseApi.getAllRows(DEFAULT_VISIT_TABLE);
        for (Result result : resultScanner) {
            IndividualCommonPoint individualInterestPoint = new IndividualCommonPoint();
            for (Cell cell : result.rawCells()) {
                String valueStr = new String(CellUtil.cloneValue(cell));
                Double value = new Double(valueStr);
                ColumnGoal columnGoal = new ColumnGoal(new String(CellUtil.cloneQualifier(cell)) + "", value);
                individualInterestPoint.addColumnGoal(columnGoal);
                individualInterestPoint.setIp(new String(CellUtil.cloneRow(cell)));
            }
            individualInterestPoints.add(individualInterestPoint);
        }
        return individualInterestPoints;
    }

    public IndividualCommonPoint getByIP(String ip) {
        Map individualCommonPoint =  RedisTemplate.getInstans().get(CACHE_PREFIX + ip, HashMap.class);
        if (individualCommonPoint != null) {
            IndividualCommonPoint cachedPoint = new IndividualCommonPoint();
            for (Object column : individualCommonPoint.keySet()) {
                cachedPoint.put((String)column, (Double)individualCommonPoint.get(column));
            }
            cachedPoint.setIp(ip);
            return cachedPoint;
        }
        IndividualCommonPoint individualInterestPoint = new IndividualCommonPoint();
        Cell[] cells = HbaseApi.getRow(DEFAULT_VISIT_TABLE, ip);
        if (cells != null && cells.length > 0) {
            for (Cell cell : cells) {
                String valueStr = new String(CellUtil.cloneValue(cell));
                Double value = new Double(valueStr);
                ColumnGoal columnGoal = new ColumnGoal(new String(CellUtil.cloneQualifier(cell)) + "", value);
                individualInterestPoint.addColumnGoal(columnGoal);
                individualInterestPoint.setIp(new String(CellUtil.cloneRow(cell)));
            }
        }
        if (individualInterestPoint != null) {
            RedisTemplate.getInstans().set(CACHE_PREFIX + ip, individualInterestPoint, EXPIRE_TIME);
        }
        return individualInterestPoint;
    }
}
