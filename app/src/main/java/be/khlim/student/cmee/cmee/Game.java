package be.khlim.student.cmee.cmee;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.FragmentActivity;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.Vector;

public class Game extends FragmentActivity implements com.google.android.gms.location.LocationListener, GooglePlayServicesClient.ConnectionCallbacks, GooglePlayServicesClient.OnConnectionFailedListener {

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private Vector<Player> Players = new Vector<Player>();
    private Vector<Capturepoint> Capturepoints = new Vector<Capturepoint>();

    int nrOfPoints = 10;
    int nrOfPlayers = 10;
    int radius = 100;

    // Milliseconds per second
    private static final int MILLISECONDS_PER_SECOND = 1000;
    // Update frequency in seconds
    public static final int UPDATE_INTERVAL_IN_SECONDS = 10;
    // Update frequency in milliseconds
    private static final long UPDATE_INTERVAL =
            MILLISECONDS_PER_SECOND * UPDATE_INTERVAL_IN_SECONDS;
    // The fastest update frequency, in seconds
    private static final int FASTEST_INTERVAL_IN_SECONDS = 2;
    // A fast frequency ceiling in milliseconds
    private static final long FASTEST_INTERVAL =
            MILLISECONDS_PER_SECOND * FASTEST_INTERVAL_IN_SECONDS;

    private LocationRequest locReq;
    private LocationClient locClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        LocationManager locManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (!locManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Toast.makeText(this, "Pls enable gps", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(intent);
        }


        locClient = new LocationClient(this, this, this);
        locClient.connect();


        locReq = LocationRequest.create();
        // Use high accuracy
        locReq.setPriority(
                LocationRequest.PRIORITY_HIGH_ACCURACY);
        // Set the update interval to 5 seconds
        locReq.setInterval(UPDATE_INTERVAL);
        // Set the fastest update interval to 1 second
        locReq.setFastestInterval(FASTEST_INTERVAL);

        setUpMapIfNeeded();

    }

        @Override
        protected void onResume () {
            super.onResume();
            setUpMapIfNeeded();
        }

        @Override
        protected void onStop () {
            // If the client is connected
            if (locClient.isConnected()) {
            /*
             * Remove location updates for a listener.
             * The current Activity is the listener, so
             * the argument is "this".
             */
                locClient.removeLocationUpdates(this);
            }
        /*
         * After disconnect() is called, the client is
         * considered "dead".
         */
            locClient.disconnect();
            super.onStop();
        }

        /**
         * Sets up the map if it is possible to do so (i.e., the Google Play services APK is correctly
         * installed) and the map has not already been instantiated.. This will ensure that we only ever
         * call {@link #setUpMap()} once when {@link #mMap} is not null.
         * <p/>
         * If it isn't installed {@link SupportMapFragment} (and
         * {@link com.google.android.gms.maps.MapView MapView}) will show a prompt for the user to
         * install/update the Google Play services APK on their device.
         * <p/>
         * A user can return to this FragmentActivity after following the prompt and correctly
         * installing/updating/enabling the Google Play services. Since the FragmentActivity may not
         * have been completely destroyed during this process (it is likely that it would only be
         * stopped or paused), {@link #onCreate(Bundle)} may not be called again so we should call this
         * method in {@link #onResume()} to guarantee that it will be called.
         */

    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                setUpMap();
            }
        }
    }

    /**
     * This is where we can add markers or lines, add listeners or move the camera. In this case, we
     * just add a marker near Africa.
     * <p/>
     * This should only be called once and when we are sure that {@link #mMap} is not null.
     */
    private void setUpMap() {
        if (mMap != null) {
            Players.add(new Player(Player.Teams.None, null, true));
            if (locClient.isConnected()) {
                RefreshMap();
            }
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        if (locClient.isConnected()) {
            // Display the connection status
            Toast.makeText(this, "Connected", Toast.LENGTH_SHORT).show();
            // If already requested, start periodic updates #error#
            locClient.requestLocationUpdates(locReq, this);
        }
    }

    @Override
    public void onDisconnected() {
        Toast.makeText(this, "Disconnected. Please re-connect.", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        if (locClient.isConnected()) {
            for (int i = 0; i < Players.size(); i++) {
                if (Players.elementAt(i).GetIsMe()) {
                    Players.elementAt(i).SetLocation(location);
                }
                }

            if (Capturepoints.size() < 1 ){
                for (int i = 0; i <= 1 - 1; i++) {
                    // initialise players *to be added* (maybe in a download_finished event)
                }

                //add random points to capture
                Capturepoints.clear();
                for (int i = 1; i <= nrOfPoints; i++) {// initialise points

                    double tmpX, tmpY;
                    // for extra randomness
                    //rnd is used for x and whether y is subtracted or added
                    //rnd2 is used for y and whether x is subtracted or added
                    //This may cause some predictable patterns but oh well
                    // X = longitude, Y = latitude

                    //calculate X

                    if (Math.random() <0.5f) {
                        tmpX = Players.elementAt(0).GetX() - (Math.random() / 10000.0) * radius;
                    } else {
                        tmpX = Players.elementAt(0).GetX() + (Math.random()/ 10000.0) * radius;
                    }


                    //check on international dateline
                    while (tmpX > 180 | tmpX < -180) {
                        if (tmpX > 180) {
                            tmpX = -180 + (tmpX - 180);
                        }
                        if (tmpX < -180) {
                            tmpX = 180 - (tmpX + 180);
                        }
                    }

                    //calculate Y
                    if (Math.random() < 0.5f) {
                        tmpY = Players.elementAt(0).GetY() + (Math.random() / 10000.0) * radius;
                    } else {
                        tmpY = Players.elementAt(0).GetY() - (Math.random() / 10000.0) * radius;
                    }

                    //check on international dateline
                    while (tmpY > 90 | tmpY < -90) {
                        if (tmpY < -90) {
                            tmpY = 90 - (tmpY + 90);
                        }
                        if (tmpY > 90) {
                            tmpY = -90 + (tmpY - 90);
                        }
                    }

                    Capturepoint dummy = new Capturepoint(i,tmpX, tmpY);
                    // set point
                    Capturepoints.add(dummy);

                }//next point
            }
            RefreshMap();
        }
    }

    private void RefreshMap() {
        if (mMap != null) {
            mMap.clear();
            float center = 0.5f;
            for (int i = 0; i < Players.size(); i++) {
                if (Players.elementAt(i).GetLocation() != null) {

                    if (Players.elementAt(i).GetIsMe()) {
                        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(
                                CameraPosition.fromLatLngZoom(new LatLng(Players.elementAt(i).GetX(), Players.elementAt(i).GetY()),
                                        (float) (17f / Math.pow(radius / 10, 0.15))
                                )));
                    }

                    Drawable d = getResources().getDrawable(R.drawable.playercircle);
                    BitmapDrawable bd = (BitmapDrawable) d.getCurrent();
                    Bitmap b = bd.getBitmap();
                    Bitmap bhalfsize = Bitmap.createScaledBitmap(b, (b.getWidth() / (int) mMap.getCameraPosition().zoom) * 2, (b.getHeight() / (int) mMap.getCameraPosition().zoom) * 2, false);

                    mMap.addMarker(new MarkerOptions()
                            .icon(BitmapDescriptorFactory.fromBitmap(bhalfsize))
                            .anchor(center, center)
                            .position(new LatLng(Players.elementAt(i).GetX(), Players.elementAt(i).GetY())));
                    Toast.makeText(this, "P Location gotten :" +Players.elementAt(i).GetX() +" " + Players.elementAt(i).GetY(), Toast.LENGTH_LONG).show();

                }
            }

            for (int i = 0; i < Capturepoints.size(); i++) {
                mMap.addCircle(new CircleOptions()
                        .center(new LatLng(Capturepoints.elementAt(i).GetX(), Capturepoints.elementAt(i).GetY()))
                        .radius(10000)
                        .strokeColor(android.R.color.black)
                        .strokeWidth(5)
                        .fillColor(Color.argb(200, 180, 180, 255)));

                Toast.makeText(this, "C Location gotten :" + Capturepoints.elementAt(i).GetX() +" " + Capturepoints.elementAt(i).GetY(), Toast.LENGTH_LONG).show();

            }
        }
    }


}
