package cmu.xprize.util;

import android.util.Log;

/**
 * Created by Karya Technologies on 19-06-2015.
 *
 * Purpose : Keeps track of the time taken to read the passage
 * History : Initial Draft
 */

public class TimerUtils {

    private static long startTime;
    private static long totalTime;
    private static boolean pauseTimer;

    private static final  String  TAG = "TimerUtils";


    public static void startTimer(){
        pauseTimer = false;
        printTimer();
        startTime = System.currentTimeMillis();
    }

    public static void pauseTimer(){
        pauseTimer = true;
        totalTime += System.currentTimeMillis() - startTime;
    }

    public static long getTotalTime(){
        return totalTime;
    }

    public static void setTotalTime(long time){
        totalTime = time;
    }

    public static String getTimeSpent(){
        return formatTime(totalTime);
    }

    private static String formatTime(long totalTime){
        int time = (int)totalTime / 1000;
        int sec = time % 60;
        int min = time / 60;
        return (min < 10 ? "0" + min : min) + " : " + (sec < 10 ? "0" + sec : sec);
    }

    public static void printTimer(){
        new Thread(){
            @Override
            public void run() {
                while (!pauseTimer){
                    Log.d(TAG, formatTime(totalTime + System.currentTimeMillis() - startTime));
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }.start();
    }

}
