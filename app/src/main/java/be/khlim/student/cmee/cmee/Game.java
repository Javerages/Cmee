package be.khlim.student.cmee.cmee;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Looper;
import android.os.Vibrator;
import android.provider.Settings;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.GestureDetectorCompat;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Vector;

import javax.xml.transform.Result;

public class Game extends FragmentActivity implements com.google.android.gms.location.LocationListener, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        GestureDetector.OnGestureListener,
        GestureDetector.OnDoubleTapListener, GoogleMap.OnMapClickListener {

    // Update frequency in seconds
    public static final int UPDATE_INTERVAL_IN_SECONDS = 10;
    // Milliseconds per second
    private static final int MILLISECONDS_PER_SECOND = 1000;
    // Update frequency in milliseconds
    private static final long UPDATE_INTERVAL =
            MILLISECONDS_PER_SECOND * UPDATE_INTERVAL_IN_SECONDS;
    // The fastest update frequency, in seconds
    private static final int FASTEST_INTERVAL_IN_SECONDS = 5;
    // A fast frequency ceiling in milliseconds
    private static final long FASTEST_INTERVAL =
            MILLISECONDS_PER_SECOND * FASTEST_INTERVAL_IN_SECONDS;
    boolean playing = false;
    PostScoreTask Postscore = null;
    private int nrOfPoints = 5;
    private int nrOfPlayers = 10;
    private int radius = 10;
    private double Pointsize = 10;
    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private Vector<Player> Players = new Vector<Player>();
    private Vector<Capturepoint> Capturepoints = new Vector<Capturepoint>();
    private LocationRequest locReq;
    private GoogleApiClient mGoogleApiClient;
    private GestureDetectorCompat mDetector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        mDetector = new GestureDetectorCompat(this, this);
        // Set the gesture detector as the double tap
        // listener.
        mDetector.setOnDoubleTapListener(this);

        SharedPreferences preferences = ((App) getApplicationContext()).storage;
        radius = Integer.parseInt(preferences.getString("radius", "10"));
        nrOfPoints = Integer.parseInt(preferences.getString("NrOfPoints", "5"));

        Pointsize = Math.pow(radius, 0.7f) * 4;
        LocationManager locManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (!locManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Toast.makeText(this, "Pls enable gps", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(intent);
        }


        // locClient = new LocationClient(this, this, this);
        // locClient.connect();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        mGoogleApiClient.connect();

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
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
    }

    @Override
    protected void onStop() {
        // If the client is connected
        if (mGoogleApiClient.isConnected()) {
            /*
             * Remove location updates for a listener.
             * The current Activity is the listener, so
             * the argument is "this".
             */
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
            mGoogleApiClient.disconnect();
        }
        /*
         * After disconnect() is called, the client is
         * considered "dead".
         */
        super.onStop();
    }

    @Override
    protected void onPause() {
        App globalVariable = (App) getApplicationContext();
        globalVariable.storage.edit().putInt("Score", globalVariable.MainUser().GetScore()).commit();
        globalVariable.storage.edit().putInt("ScoreDay", globalVariable.MainUser().GetScoreDay()).commit();
        globalVariable.storage.edit().putInt("ScoreWeek", globalVariable.MainUser().GetScoreWeek()).commit();
        String date = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        globalVariable.storage.edit().putString("lastPlayDate",date ).commit();
        if (globalVariable.MainUser().GetUserid() >= 0) {
            if (Postscore == null) {
                Postscore = new PostScoreTask();
                Postscore.execute("all");
            }
        }else{
            Toast.makeText(getApplicationContext(),"Log in to upload highscore",Toast.LENGTH_SHORT).show();
        }

        super.onPause();
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
     * <p/>
     * This should only be called once and when we are sure that {@link #mMap} is not null.
     */
    private void setUpMap() {
        if (mMap != null) {
            this.mMap.setOnMapClickListener(this);
            mMap.getUiSettings().setMapToolbarEnabled(false);
            mMap.getUiSettings().setMyLocationButtonEnabled(true);
            mMap.getUiSettings().setCompassEnabled(true);
            mMap.getUiSettings().setZoomControlsEnabled(true);

            Players.add(new Player(Player.Teams.None, null, true));

            if (mGoogleApiClient.isConnected()) {
                RefreshMap();
            }
        }
    }

    @Override
    public void onConnected(Bundle bundle) {

        if (mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.requestLocationUpdates(
                    mGoogleApiClient, locReq, this);
            // Display the connection status
            Toast.makeText(this, "Connected", Toast.LENGTH_SHORT).show();
            // If already requested, start periodic updates #error#

            ConnectivityManager connManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
            NetworkInfo internet = connManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
            if (!internet.isConnected()) {
                internet = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
                if (!internet.isConnected()) {
                    Toast.makeText(this, "Connectivity problems, unexpected behaviour may occur", Toast.LENGTH_SHORT).show();
                }
            }

        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        Toast.makeText(this, "Disconnected. Please re-connect.", Toast.LENGTH_SHORT).show();
        playing = false;
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        playing = false;
        Toast.makeText(this, "Connection lost", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onLocationChanged(Location location) {
        if (mGoogleApiClient.isConnected()) {
            for (int i = 0; i < Players.size(); i++) {
                if (Players.elementAt(i).GetIsMe()) {
                    Players.elementAt(i).SetLocation(location);
                    if (!playing) {
                        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(
                                CameraPosition.fromLatLngZoom(new LatLng(Players.elementAt(i).GetY(),
                                                Players.elementAt(i).GetX()),
                                        (float) (17f / Math.pow(radius / 10, 0.15))
                                )));
                    }
                }
            }

            if (Capturepoints.size() < 1 && !playing) {
                for (int i = 0; i <= nrOfPlayers; i++) {
                    // initialise players *to be added when multiplayer*
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

                    //calculate X (longitude) long->180

                    if (Math.random() < 0.5f) {
                        tmpX = Players.elementAt(0).GetX() - ((Math.random() / 10000.0) * radius);
                    } else {
                        tmpX = Players.elementAt(0).GetX() + ((Math.random() / 10000.0) * radius);
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

                    //calculate Y (latitude)
                    if (Math.random() < 0.5f) {
                        tmpY = Players.elementAt(0).GetY() + ((Math.random() / 10000.0) * radius);
                    } else {
                        tmpY = Players.elementAt(0).GetY() - ((Math.random() / 10000.0) * radius);
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

                    Capturepoint dummy = new Capturepoint(i, tmpX, tmpY);
                    // set point
                    Capturepoints.add(dummy);

                }//next point
            }

            double acc = location.getAccuracy();
            if (acc < 30) {
                CheckHits(Pointsize);
            } else {
                Toast.makeText(this, "Poor connection (acc = " + acc + ")", Toast.LENGTH_SHORT).show();
            }

            RefreshMap();
            playing = true;
        }
    }

    private void RefreshMap() {

        if (mMap != null) {
            mMap.clear();
            float center = 0.5f;
            for (int i = 0; i < Players.size(); i++) {
                if (Players.elementAt(i).GetLocation() != null) {

                    Drawable d = getResources().getDrawable(R.drawable.playercircle);
                    BitmapDrawable bd = (BitmapDrawable) d.getCurrent();
                    Bitmap b = bd.getBitmap();
                    Bitmap bhalfsize = Bitmap.createScaledBitmap(b, ((b.getWidth() - 100) - 2 * (int) mMap.getCameraPosition().zoom), ((b.getHeight() - 100) - 2 * (int) mMap.getCameraPosition().zoom), false);

                    mMap.addMarker(new MarkerOptions()
                            .icon(BitmapDescriptorFactory.fromBitmap(bhalfsize))
                            .anchor(center, center)
                            .position(new LatLng(Players.elementAt(i).GetY(), Players.elementAt(i).GetX())));
                    // Toast.makeText(this, "P Location gotten :" + Players.elementAt(i).GetX() + " " + Players.elementAt(i).GetY(), Toast.LENGTH_LONG).show();

                }
            }

            for (int i = 0; i < Capturepoints.size(); i++) {
                if (!Capturepoints.elementAt(i).Captured()) {
                    mMap.addCircle(new CircleOptions()
                            .center(new LatLng(Capturepoints.elementAt(i).GetY(), Capturepoints.elementAt(i).GetX()))
                            .radius(Pointsize)
                            .strokeColor(android.R.color.black)
                            .strokeWidth(5)
                            .fillColor(Color.argb(200, 180, 180, 255)));
                    // Toast.makeText(this, "C Location gotten :" + Capturepoints.elementAt(i).GetX() + " " + Capturepoints.elementAt(i).GetY(), Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    public void CheckHits(double accuracyMod) //check if player collides with point (accuracymod = sizeofdot)
    {
        App globalVariable = (App) getApplicationContext();
        int Nrcaptured = 0; //If captured=true -> counter rises -> if counter >= nrOfPoints -> All capped: End game

        for (int i = 0; i < Players.size(); i++) {
            for (int j = 0; j < Capturepoints.size(); j++) {
                if (!Capturepoints.elementAt(j).Captured()) {
                    float[] dist = new float[4];
                    Location.distanceBetween(Capturepoints.elementAt(j).GetY(), Capturepoints.elementAt(j).GetX(), Players.elementAt(i).GetY(), Players.elementAt(i).GetX(), dist);
                    if (dist[0] < accuracyMod) {
                        Capturepoints.elementAt(j).Capture();
                        Nrcaptured += 1;
                        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(
                                CameraPosition.fromLatLngZoom(new LatLng(Capturepoints.elementAt(j).GetY(),
                                                Capturepoints.elementAt(j).GetX()),
                                        (float) (17f / Math.pow(radius / 10, 0.15))
                                )));
                        if (Players.elementAt(i).GetIsMe()) {
                            Toast.makeText(this, "Point " + Capturepoints.elementAt(j).GetIndex() + " Captured", Toast.LENGTH_LONG).show();
                            Vibrator v = (Vibrator) this.getSystemService(VIBRATOR_SERVICE);
                            // Vibrate for 500 milliseconds
                            v.vibrate(500);
                            globalVariable.MainUser().AddScore(1);
                        }
                    }
                } else {
                    Nrcaptured += 1;
                }
            }
        }

        //End game
        if (Nrcaptured >= nrOfPoints) {
            globalVariable.MainUser().AddScore((nrOfPoints + radius) / 10);
            finish();
        }

    }//end checkhits

    /*
    Gestures
    */
    @Override
    public void onMapClick(LatLng latLng) {

    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        super.dispatchTouchEvent(ev);
        this.mDetector.onTouchEvent(ev);
        //super.onTouchEvent(ev);
        return true;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        this.mDetector.onTouchEvent(event);
        // Be sure to call the superclass implementation
        return super.onTouchEvent(event);
    }

    @Override
    public boolean onSingleTapConfirmed(MotionEvent e) {
        RefreshMap();
        return false;
    }

    @Override
    public boolean onDoubleTap(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onDoubleTapEvent(MotionEvent e) {
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(
                CameraPosition.fromLatLngZoom(new LatLng(Players.elementAt(0).GetY(),
                                Players.elementAt(0).GetX()),
                        (float) (17f / Math.pow(radius / 10, 0.15))
                )));
        RefreshMap();
        return false;
    }

    @Override
    public boolean onDown(MotionEvent e) {
        return false;
    }

    @Override
    public void onShowPress(MotionEvent e) {

    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        return false;
    }

    @Override
    public void onLongPress(MotionEvent e) {
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(
                CameraPosition.fromLatLngZoom(new LatLng(Players.elementAt(0).GetX(),
                                Players.elementAt(0).GetY()),
                        (float) (17f / Math.pow(radius / 10, 0.15))
                )));
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        if (velocityX / velocityY > 1 && velocityX / velocityY < 100 && velocityX > 1000) { //Cheat
            Captureall();
        }
        return false;
    }

    private void Captureall() {
        if (BuildConfig.DEBUG) {
            Toast.makeText(this, "Cheat", Toast.LENGTH_SHORT).show();
        for (int j = 0; j < Capturepoints.size(); j++) {
                Capturepoints.elementAt(j).Capture();
            }
        }
    }

    //string myParameters = "userid=" + Convert.ToInt64(App.Mainuser.Userid) + "&score=" + App.Mainuser.Score + "&type=all";
    //Uri URI = new Uri("http://cmee.yzi.me/index.php/app/sethighscores", UriKind.Absolute);
    private class PostScoreTask extends AsyncTask<String, Void, Boolean> {

        private Exception exception;
        private String reply = "Error";

        protected Boolean doInBackground(String... type) {
            try {
                postScore(type[0]);
            } catch (Exception e) {
                this.exception = e;
            }
            return true;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {

            SendToast(reply);
            super.onPostExecute(aBoolean);
        }

        private void SendToast(final String message) {

                    View posted=  findViewById(R.id.map);
                    posted.post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(), reply, Toast.LENGTH_LONG).show();
                        }
                    });
        }

        public void postScore(String type) {
            // Create a new HttpClient and Post Header
            HttpClient httpclient = new DefaultHttpClient();
            HttpPost Param = new HttpPost("http://cmee.yzi.me/index.php/app/setAllhighscores");

            try {
                App globalVariable = (App) getApplicationContext();


                // Add your data
                List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(3);
                nameValuePairs.add(new BasicNameValuePair("userid", String.valueOf(globalVariable.MainUser().GetUserid())));
                nameValuePairs.add(new BasicNameValuePair("all", String.valueOf(globalVariable.MainUser().GetScore())));
                nameValuePairs.add(new BasicNameValuePair("daily", String.valueOf(globalVariable.MainUser().GetScoreDay())));
                nameValuePairs.add(new BasicNameValuePair("weekly", String.valueOf(globalVariable.MainUser().GetScoreWeek())));
                Param.setEntity(new UrlEncodedFormEntity(nameValuePairs));

                // Execute HTTP Post Request
                HttpResponse response = httpclient.execute(Param);
                StatusLine statusLine = response.getStatusLine();
                if (statusLine.getStatusCode() == HttpStatus.SC_OK) {
                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    response.getEntity().writeTo(out);
                    out.close();
                    String responseString = out.toString();
                    if (responseString != null) {
                        reply = responseString.toString();
                                            }
                }
            } catch (ClientProtocolException e) {
                // TODO Auto-generated catch block
            } catch (IOException e) {
                // TODO Auto-generated catch block
            }
        }
    }

}
