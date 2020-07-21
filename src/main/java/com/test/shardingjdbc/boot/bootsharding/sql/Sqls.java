package com.test.shardingjdbc.boot.bootsharding.sql;

/**
 * @author wangshuzheng
 * @date 2020/7/15 2:19 下午
 * @description
 */
public class Sqls {

    public static String INSERT = "insert into test32(id, col01, col02, col03, col04, col05, col06, col07, " +
            "col08, col09, col10, col11, col12, col13, col14, col15, col16, col17, col18, col19, " +
            "col20, col21, col22, col23, col24, col25, col26, col27, col28, col29, col30, col31) " +
            "values(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";


    public static String LITTLE_T_INSERT = "insert into test32(id, col01, col02, col05, col06, col07) values(?, ?, ?, ?, ?, ?)";

    public static String SINGLE_ROUTE_SEARCH = "select col03,col31 from test32 where id = ? and col01 = ?";

    public static String LT_SINGLE_ROUTE_SEARCH = "select col02,col05 from test32 where id = ? and col01 = ?";

}
