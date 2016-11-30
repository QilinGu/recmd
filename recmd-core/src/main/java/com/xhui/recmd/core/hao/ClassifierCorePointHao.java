package com.xhui.recmd.core.hao;

import com.xhui.recmd.core.union.ArraySet;
import com.xhui.recmd.core.union.ColumnGoal;
import com.xhui.recmd.core.union.IndividualCommonPoint;
import com.xhui.recmd.spark.utils.HbaseApi;
import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.CellUtil;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by littlehui on 2016/10/27.
 */
public class ClassifierCorePointHao {

    /**
     * 聚类中心
     */
    private static final String CLASS_TABLE_POINT = "classifierCorePoint";

    public void addToHBase(ArraySet<IndividualCommonPoint> individualInterestPointArraySet) {
        for (int i = 0; i < individualInterestPointArraySet.size(); i++) {
            List<ColumnGoal> columnGoals = individualInterestPointArraySet.get(i).getColumnGoals();
            if (columnGoals != null && columnGoals.size() > 0) {
                for (ColumnGoal columnGoal : columnGoals) {
/*                hbaseApi.addRow(STORE_TABLE, commodityPair.toRowKey(), "count", "commodityCode", commodityPair.getCode());
                hbaseApi.addRow(STORE_TABLE, commodityPair.toRowKey(), "count", "fat", commodityPair.getScore() + "");
                hbaseApi.addRow(STORE_TABLE, commodityPair.toRowKey(), "count", "ip", commodityPair.getIp());
                */
                    if (columnGoal.getGoal() > 0) {
                        HbaseApi.addRow(CLASS_TABLE_POINT, individualInterestPointArraySet.get(i).hashCode() + "", "count", columnGoal.getColumn(), columnGoal.getGoal() + "");
                    }
                }
            }
        }
        HbaseApi.closed();
    }

    public List<IndividualCommonPoint> queryAll() {
        ResultScanner resultScanner = HbaseApi.getAllRows(CLASS_TABLE_POINT);
        List<IndividualCommonPoint> individualInterestPoints = new ArrayList<>();
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

}
