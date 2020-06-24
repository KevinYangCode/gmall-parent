package xyz.jianzha.gmall.mock.util;

import java.util.Random;

/**
 * @author Y_Kevin
 * @date 2020-06-13 12:40
 */
public class RandomNum {
    public static final  int getRandInt(int fromNum,int toNum){
        return   fromNum+ new Random().nextInt(toNum-fromNum+1);
    }
}
