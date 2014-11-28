package be.khlim.student.cmee.cmee;

import android.location.Location;

/**
 * Created by 10368301 on 28/11/2014.
 */
public abstract class Point  {
    Location loc_;

    Point(){}
    Point(Location loc){
    loc_ = new Location(loc);
    }

}
