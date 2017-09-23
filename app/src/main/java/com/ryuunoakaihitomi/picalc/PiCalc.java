package com.ryuunoakaihitomi.picalc;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

/**
 * Created by ZQY on 2017/9/12.
 * 作者版权所有。用途:计算圆周率
 */

public class PiCalc {
    static int digits, offset;
    static long timer;
    static BigDecimal p1, p2, p3, p4;
    static BigDecimal p = BigDecimal.ZERO;


    //原理：马青公式衍生算法
    static void main(int arg) {
        //防误差用偏移量
        offset = 13;
        digits = arg;
        timer = System.currentTimeMillis();
        //π=176·arctan(1/57)+28·arctan(1/239)−48·arctan(1/682)+96·arctan(1/12943)
        new Thread(() -> p1 = reciArctan(57).multiply(new BigDecimal(176))).start();
        new Thread(() -> p2 = reciArctan(239).multiply(new BigDecimal(28))).start();
        new Thread(() -> p3 = reciArctan(682).multiply(new BigDecimal(-48))).start();
        new Thread(() -> p4 = reciArctan(12943).multiply(new BigDecimal(96))).start();
        new Thread(() -> {
            while (p1 == null || p2 == null || p3 == null || p4 == null) {
            }
            p = p1.add(p2).add(p3).add(p4).round(new MathContext(digits + 1, RoundingMode.HALF_UP));
            timer = System.currentTimeMillis() - timer;
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            MainActivity.calcEndWork(timer, p.toString());
        }).start();
    }

    //倒数反正切，泰勒展开式
    static BigDecimal reciArctan(int x) {
        BigDecimal result, xval, term;
        BigDecimal bx = BigDecimal.valueOf(x);
        BigDecimal bxsqr = BigDecimal.valueOf(x * x);
        xval = BigDecimal.ONE.divide(bx, digits + offset, BigDecimal.ROUND_HALF_EVEN);
        result = xval;
        int i = 1;
        do {
            //循环强制回收内存，尽可能减少内存占用
            System.gc();
            xval = xval.divide(bxsqr, digits + offset, BigDecimal.ROUND_HALF_EVEN);
            int denom = 2 * i + 1;
            term = xval.divide(BigDecimal.valueOf(denom), digits + offset, BigDecimal.ROUND_HALF_EVEN);
            if ((i % 2) != 0)
                result = result.subtract(term);
            else
                result = result.add(term);
            i++;
        } while (term.compareTo(BigDecimal.ZERO) != 0);
        return result;
    }
}
