package com.shawn.scriber.Adapter;

import java.util.Date;
import java.util.concurrent.TimeUnit;

public class TimeAgo {

    public String getTimeAgo(long duration){
        Date now=new Date();

        long seconds= TimeUnit.MILLISECONDS.toSeconds(now.getTime()-duration);
        long min= TimeUnit.MILLISECONDS.toMinutes(now.getTime()-duration);
        long hours= TimeUnit.MILLISECONDS.toHours(now.getTime()-duration);
        long days= TimeUnit.MILLISECONDS.toDays(now.getTime()-duration);

        if(seconds<60){
            return "just a few seconds ago";
        }else if (min==1){
            return "a minute ago";
        }else if (min>1 && min<60){
            return min+" minutes ago";
        }else if (hours==1){
            return "an hour ago";
        }else if (hours>1 && hours<24){
            return hours+" hours ago";
        }else if(days==1){
            return "one day ago";
        }else {
            return days+" days ago";
        }

    }
}
