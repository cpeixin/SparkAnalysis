package com.zkys.spark.session;

import com.zkys.spark.constant.Constants;
import com.zkys.spark.util.StringUtils;
import org.apache.spark.AccumulatorParam;


/**
 * session聚合统计Accumulator
 *
 * 大家可以看到
 * 其实使用自己定义的一些数据格式，比如String，甚至说，我们可以自己定义model，自己定义的类（必须可序列化）
 * 然后呢，可以基于这种特殊的数据格式，可以实现自己复杂的分布式的计算逻辑
 * 各个task，分布式在运行，可以根据你的需求，task给Accumulator传入不同的值
 * 根据不同的值，去做复杂的逻辑
 *
 * Spark Core里面很实用的高端技术
 *
 * @author Administrator
 *
 */
public class SessionAggrStatAccumulator implements AccumulatorParam<String> {

    private static final long serialVersionUID = 6311074555136039130L;

    /**
     * zero方法，其实主要用于数据的初始化
     * 那么，我们这里，就返回一个值，就是初始化中，所有范围区间的数量，都是0
     * 各个范围区间的统计数量的拼接，还是采用一如既往的key=value|key=value的连接串的格式
     */
    @Override
    public String zero(String v) {
        return Constants.SESSION_COUNT + "=0|"
                + Constants.TIME_PERIOD_1s_3s + "=0|"
                + Constants.TIME_PERIOD_4s_6s + "=0|"
                + Constants.TIME_PERIOD_7s_9s + "=0|"
                + Constants.TIME_PERIOD_10s_30s + "=0|"
                + Constants.TIME_PERIOD_30s_60s + "=0|"
                + Constants.TIME_PERIOD_1m_3m + "=0|"
                + Constants.TIME_PERIOD_3m_10m + "=0|"
                + Constants.TIME_PERIOD_10m_30m + "=0|"
                + Constants.TIME_PERIOD_30m + "=0|"
                + Constants.STEP_PERIOD_1_3 + "=0|"
                + Constants.STEP_PERIOD_4_6 + "=0|"
                + Constants.STEP_PERIOD_7_9 + "=0|"
                + Constants.STEP_PERIOD_10_30 + "=0|"
                + Constants.STEP_PERIOD_30_60 + "=0|"
                + Constants.STEP_PERIOD_60 + "=0";
    }

    /**
     * addInPlace和addAccumulator
     * 不可以理解为是一样的
     *
     * 这两个方法，其实主要就是实现，v1可能就是我们初始化的那个连接串
     * v2，就是我们在遍历session的时候，判断出某个session对应的区间，然后会用Constants.TIME_PERIOD_1s_3s
     * 所以，我们，要做的事情就是
     * 在v1中，找到v2对应的value，累加1，然后再更新回连接串里面去
     *
     */
    @Override
    //参数传进来之后，就会在累加的整个字符串上进行相应字段的累加
    // v1就代表着中间缓冲的结果
    public String addInPlace(String v1, String v2) {
        // 校验：v1为空的话，直接返回v2
        // 因为它第一次的时候呢v1为空，v2是我们初始化的字符串
        if(StringUtils.isEmpty(v1)) {
            return v2;
        }

        String[] keys = new String[]{Constants.SESSION_COUNT, Constants.TIME_PERIOD_1s_3s,
                Constants.TIME_PERIOD_4s_6s, Constants.TIME_PERIOD_7s_9s, Constants.TIME_PERIOD_10s_30s,
                Constants.TIME_PERIOD_30s_60s, Constants.TIME_PERIOD_1m_3m, Constants.TIME_PERIOD_3m_10m,
                Constants.TIME_PERIOD_10m_30m, Constants.TIME_PERIOD_30m, Constants.STEP_PERIOD_1_3,
                Constants.STEP_PERIOD_4_6, Constants.STEP_PERIOD_7_9, Constants.STEP_PERIOD_10_30,
                Constants.STEP_PERIOD_30_60, Constants.STEP_PERIOD_60  };

        String temp = Constants.SESSION_COUNT + "=0|"
                + Constants.TIME_PERIOD_1s_3s + "=0|"
                + Constants.TIME_PERIOD_4s_6s + "=0|"
                + Constants.TIME_PERIOD_7s_9s + "=0|"
                + Constants.TIME_PERIOD_10s_30s + "=0|"
                + Constants.TIME_PERIOD_30s_60s + "=0|"
                + Constants.TIME_PERIOD_1m_3m + "=0|"
                + Constants.TIME_PERIOD_3m_10m + "=0|"
                + Constants.TIME_PERIOD_10m_30m + "=0|"
                + Constants.TIME_PERIOD_30m + "=0|"
                + Constants.STEP_PERIOD_1_3 + "=0|"
                + Constants.STEP_PERIOD_4_6 + "=0|"
                + Constants.STEP_PERIOD_7_9 + "=0|"
                + Constants.STEP_PERIOD_10_30 + "=0|"
                + Constants.STEP_PERIOD_30_60 + "=0|"
                + Constants.STEP_PERIOD_60 + "=0";

        for(String key : keys){
            String oldValue = StringUtils.getFieldFromConcatString(v1, "\\|", key);
            String newValue = StringUtils.getFieldFromConcatString(v2, "\\|", key);
            int value = (Integer.valueOf(oldValue) + Integer.valueOf(newValue));
            temp =  StringUtils.setFieldInConcatString(temp, "\\|", key, String.valueOf(value));
        }

        return temp;
    }

    @Override
    public String addAccumulator(String v1, String v2) {

        // 使用StringUtils工具类，从v1中，提取v2对应的值，并累加1
        String oldValue = StringUtils.getFieldFromConcatString(v1, "\\|", v2);
        if(oldValue != null) {
            // 将范围区间原有的值，累加1
            int newValue = Integer.valueOf(oldValue) + 1;
            // 使用StringUtils工具类，将v1中，v2对应的值，设置成新的累加后的值
            return StringUtils.setFieldInConcatString(v1, "\\|", v2, String.valueOf(newValue));
        }

        // 认为v2传的有问题就不考虑
        return v1;
    }

}