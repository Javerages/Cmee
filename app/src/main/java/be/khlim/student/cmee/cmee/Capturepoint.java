package be.khlim.student.cmee.cmee;

import android.location.Location;

/**
 * Created by 10368301 on 28/11/2014.
 */
public class Capturepoint extends Point{

    private int index;
    private boolean captured;
    private Location loc_;

    public Capturepoint(int index, Location loc)
    {
        this.index = index;
        captured = false;
        this.loc_ = loc;
    }

    //*********************//
    // Properties:         //
    //*********************//

    public int GetIndex(){ return index; }

    public double GetX (){ return loc_.getLongitude(); }
    public double GetY () { return loc_.getLatitude(); }

    public boolean GetCaptured () { return captured; }

    //*********************//
    // All extra functions://
    //*********************//
    public void Capture()
    {
        this.captured = true;
    }
}
