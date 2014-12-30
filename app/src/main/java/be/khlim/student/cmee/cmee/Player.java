package be.khlim.student.cmee.cmee;

import android.location.Location;

/**
 * Created by 10368301 on 28/11/2014.
 */
public class Player extends Point {
    private boolean me = false;
    private Location loc_;
    private String name = "";

    private Teams team;

    public Player(Teams team, Location loc, boolean me)
    {
        this.team = team;
        this.me = me;
    }

    public enum Teams
    {
        Circles ,
        Squares ,
        None
    };
    //*********************//
    // Properties://
    //*********************//


    public boolean GetIsMe()  { return me; }
    public double GetX () { return loc_.getLongitude(); }
    public double GetY () {return loc_.getLatitude();  }

    public Location GetLocation() { return loc_; }
    public void SetLocation(Location loc) { loc_ = loc; }
}
