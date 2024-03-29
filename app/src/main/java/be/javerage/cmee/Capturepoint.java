package be.javerage.cmee;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by 10368301 on 28/11/2014.
 */
public class Capturepoint extends Point{

    private int index = 0;
    private boolean captured;
    private boolean skipped;
    private final LatLng loc_;

    public Capturepoint(int index, double x, double y)
    {
        this.index = index;
        captured = false;
        loc_ = new LatLng(y,x);
    }

    //*********************//
    // Properties:         //
    //*********************//

    public int GetIndex(){ return index; }

    public double GetX (){ return loc_.longitude; }
    public double GetY () { return loc_.latitude; }
    public LatLng GetDistanceTo () { return loc_; }

    public boolean Captured() { return captured; }

    //*********************//
    // All extra functions://
    //*********************//
    public void Capture()
    {
        this.captured = true;
    }
    public void Skip()
    {
        this.captured = true;
        this.skipped = true;
    }
}
