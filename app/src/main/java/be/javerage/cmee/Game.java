package be.javerage.cmee;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Vibrator;
import android.provider.Settings;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GestureDetectorCompat;

import com.google.android.gms.games.AchievementsClient;
import com.google.android.gms.games.LeaderboardsClient;
import com.google.android.gms.games.PlayGames;
import com.google.android.gms.games.PlayGamesSdk;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Vector;

import be.javerage.cmee.R;

/*import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;*/

public class Game extends AppCompatActivity implements 
        GestureDetector.OnGestureListener,
        GestureDetector.OnDoubleTapListener, GoogleMap.OnMapClickListener, OnMapReadyCallback, GoogleMap.OnCameraMoveListener {

    // Update frequency in seconds
    public static final int UPDATE_INTERVAL_IN_SECONDS = 20;
    // Milliseconds per second
    private static final int MILLISECONDS_PER_SECOND = 100;
    // Update frequency in milliseconds
    private static final long UPDATE_INTERVAL =
            MILLISECONDS_PER_SECOND * UPDATE_INTERVAL_IN_SECONDS;
    // The fastest update frequency, in seconds
    private static final int FASTEST_INTERVAL_IN_SECONDS = 10;
    // A fast frequency ceiling in milliseconds
    private static final long FASTEST_INTERVAL =
            MILLISECONDS_PER_SECOND * FASTEST_INTERVAL_IN_SECONDS;

    boolean playing = false;
    boolean deletedpoint = false; //check if a point was deleted
    boolean capthis = false; // var for removing a point

    private int nrOfPoints = 5;
    private int nrOfPlayers = 1;
    private int radius = 10;
    private double Pointsize = 10;

    //maps:
    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private final Vector<Player> Players = new Vector<>();
    private final Vector<Capturepoint> Capturepoints = new Vector<>();
    private LocationRequest locReq;
    private FusedLocationProviderClient mFusedLocationClient;
    private AchievementsClient mAchievementsClient;
    private LeaderboardsClient mLeaderboardsClient;

    //Gestures:
    private GestureDetector mDetector;

    private final Handler mHideHandler = new Handler(Looper.getMainLooper());
    private final Runnable mHideRunnable = () -> {
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
    };
    private float mPreviousZoom = -1f;

    private void showActionBarTemporarily() {
        if (getSupportActionBar() != null) {
            getSupportActionBar().show();
            mHideHandler.removeCallbacks(mHideRunnable);
            mHideHandler.postDelayed(mHideRunnable, 3000);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        PlayGamesSdk.initialize(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        mDetector = new GestureDetector(this, this);
        // Set the gesture detector as the double tap
        // listener.
        mDetector.setOnDoubleTapListener(this);

        mAchievementsClient = PlayGames.getAchievementsClient(this);
        mLeaderboardsClient = PlayGames.getLeaderboardsClient(this);
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        SharedPreferences preferences = ((App) getApplicationContext()).storage;
        radius = Integer.parseInt(preferences.getString("radius", "10"));
        nrOfPoints = Integer.parseInt(preferences.getString("NrOfPoints", "5"));


        App globalVariable = (App) getApplicationContext();
        if (globalVariable.MainUser().GetScore() < 2) {

            AlertDialog alertDialog;
            alertDialog = new AlertDialog.Builder(Game.this).create();
            alertDialog.setTitle("How to play");
            alertDialog.setMessage("Walk to every circle on the map to win.");
            alertDialog.show();
        }

        Pointsize = Math.pow(radius, 0.8f) * 3;
        LocationManager locManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (!locManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Toast.makeText(this, "Pls enable gps", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(intent);
        }


        // locClient = new LocationClient(this, this, this);
        // locClient.connect();


        locReq = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, UPDATE_INTERVAL)
                .setMinUpdateIntervalMillis(FASTEST_INTERVAL)
                .build();

        // Restoring the markers on configuration changes
        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey("points")) {
                if (savedInstanceState.containsKey("Captures")) {
                    ArrayList<LatLng> pointList = savedInstanceState.getParcelableArrayList("points");
                    boolean[] capList = savedInstanceState.getBooleanArray("Captures");
                    if (pointList != null && capList != null) {
                        nrOfPoints = pointList.size();
                        for (int index = 0; index < pointList.size(); index++) {
                            LatLng tmp = pointList.get(index);
                            Capturepoints.add(new Capturepoint(index, tmp.longitude, tmp.latitude));
                            if (index < capList.length && capList[index]) {
                                Capturepoints.elementAt(index).Capture();
                            }
                        }
                    }
                }
            }
        }
    }

    // A callback method, which is invoked on configuration is changed
    @Override
    protected void onSaveInstanceState(@androidx.annotation.NonNull Bundle outState) {
        // Adding the pointList arraylist to Bundle

        if (playing) {
            ArrayList<LatLng> pointList = new ArrayList<>();
            boolean[] capList = new boolean[Capturepoints.size()];
            for (int j = 0; j < Capturepoints.size(); j++) {
                pointList.add(new LatLng(Capturepoints.elementAt(j).GetY(), Capturepoints.elementAt(j).GetX()));

                capList[j] = Capturepoints.elementAt(j).Captured();
            }

            outState.putParcelableArrayList("points", pointList);
            outState.putBooleanArray("Captures", capList);
            // Saving the bundle
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
    }

    @Override
    protected void onPause() {
        App globalVariable = (App) getApplicationContext();
        globalVariable.storage.edit()
                .putInt("Score", globalVariable.MainUser().GetScore())
                .putInt("ScoreDay", globalVariable.MainUser().GetScoreDay())
                .putInt("ScoreWeek", globalVariable.MainUser().GetScoreWeek())
                .apply();
        String date = new SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US).format(new Date());
        globalVariable.storage.edit().putString("lastPlayDate", date).apply();

        super.onPause();
    }

    @Override
    protected void onDestroy() {

        super.onDestroy();
    }

    @Override
    protected void onStart() {
        super.onStart();
        setUpMapIfNeeded();
        startLocationUpdates();
    }

    private void startLocationUpdates() {
        try {
            mFusedLocationClient.requestLocationUpdates(locReq, mLocationCallback, null);
        } catch (SecurityException ex) {
            //TODO::handle securityexception
        }
    }

    private final LocationCallback mLocationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            if (locationResult == null) {
                return;
            }
            for (Location location : locationResult.getLocations()) {
                onLocationChanged(location);
            }
        }
    };

    @Override
    protected void onStop() {
        mFusedLocationClient.removeLocationUpdates(mLocationCallback);
        super.onStop();
    }


    /**
     * Sets up the map if it is possible to do so (i.e., the Google Play services APK is correctly
     * installed) and the map has not already been instantiated.. This will ensure that we only ever
     * call {@link #setUpMap()} once when {@link #mMap} is not null.
     * <p/>
     * If it isn't installed {@link SupportMapFragment} (and
     * {@link MapView MapView}) will show a prompt for the user to
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
            SupportMapFragment MapFrag =((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map));
            if(MapFrag != null){
            MapFrag.getMapAsync(this);
            }

        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap){
        mMap = googleMap;
        if (mMap != null) {
                setUpMap();
        }else{
            //just let it crash if it could not fetch the map
        }
    }




    /**
     * <p/>
     * This should only be called once and when we are sure that {@link #mMap} is not null.
     */
    private void setUpMap() {
        if (mMap != null) {
            this.mMap.setOnMapClickListener(this);
            this.mMap.setOnCameraMoveListener(this);
            mMap.getUiSettings().setMapToolbarEnabled(false);
            mMap.getUiSettings().setMyLocationButtonEnabled(true);
            mMap.getUiSettings().setCompassEnabled(true);
            mMap.getUiSettings().setZoomControlsEnabled(true);

            if(Players.size()<1) {
                Players.add(new Player(Player.Teams.None, null, true));//setup me
            }

            RefreshMap();
        }
    }


    @Override
    public void onCameraMove() {
        if (mMap != null) {
            float currentZoom = mMap.getCameraPosition().zoom;
            if (mPreviousZoom != -1f && currentZoom < mPreviousZoom) {
                // Zooming out
                showActionBarTemporarily();
            }
            mPreviousZoom = currentZoom;
        }
    }

    public void onLocationChanged(Location location) {
        mAchievementsClient.increment(this.getString(R.string.achievement_i_started_it), 1);
        for(Player player:Players) {
            if (player.GetIsMe()) {
                player.SetLocation(location);
                if (!playing) {
                    mMap.animateCamera(CameraUpdateFactory.newCameraPosition(
                            CameraPosition.fromLatLngZoom(new LatLng(player.GetY(),
                                            player.GetX()),
                                    (float) (17f / Math.pow(radius / 10.0, 0.15))
                            )));
                }
            }
        }

        if (Capturepoints.size() < 1 && !playing) {
            for (int i = 0; i <= nrOfPlayers; i++) {
                // initialise players *to be added when multiplayer*
            }
        }

        if (Capturepoints.isEmpty() && !playing) {
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
                while (tmpX > 180 || tmpX < -180) {
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
                while (tmpY > 90 || tmpY < -90) {
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
        CheckHits(Pointsize, acc);

        RefreshMap();
        if (!playing) {
            mHideHandler.postDelayed(mHideRunnable, 2000);
        }
        playing = true;

        if (radius > 10000) {
            mAchievementsClient.unlock(this.getString(R.string.achievement_going_worldwide));
        }
    }

    private void RefreshMap() {

        if (mMap != null) {
            mMap.clear();
            float center = 0.5f;
            for (int i = 0; i < Players.size(); i++) {
                if (Players.elementAt(i).GetLocation() != null) {

                    Drawable d = androidx.core.content.res.ResourcesCompat.getDrawable(getResources(), R.drawable.ic_launcher, null);
                    BitmapDrawable bd = (BitmapDrawable) d;
                    if (bd != null) {
                        Bitmap b = bd.getBitmap();
                        int renderWidth = 100 - 2 * (int) mMap.getCameraPosition().zoom;
                        int renderHeight = 100 - 2 * (int) mMap.getCameraPosition().zoom;

                        if (renderWidth <= 0 || renderHeight <= 0) {
                            renderWidth = 100;
                            renderHeight = 100;
                        }

                        Bitmap bHalfsize = Bitmap.createScaledBitmap(b, renderWidth, renderHeight, false);

                        mMap.addMarker(new MarkerOptions()
                                .icon(BitmapDescriptorFactory.fromBitmap(bHalfsize))
                                .anchor(center, center)
                                .position(new LatLng(Players.elementAt(i).GetY(), Players.elementAt(i).GetX())));
                        // Toast.makeText(this, "P Location gotten :" + Players.elementAt(i).GetX() + " " + Players.elementAt(i).GetY(), Toast.LENGTH_LONG).show();
                    }

                }
            }

            for (int i = 0; i < Capturepoints.size(); i++) {
                if (!Capturepoints.elementAt(i).Captured()) {
                    mMap.addCircle(new CircleOptions()
                            .center(new LatLng(Capturepoints.elementAt(i).GetY(), Capturepoints.elementAt(i).GetX()))
                            .radius(Pointsize)
                            .strokeColor(android.R.color.holo_orange_dark)
                            .strokeWidth(1)
                            .fillColor(Color.argb(200, 255, 193, 77)));
                    // Toast.makeText(this, "C Location gotten :" + Capturepoints.elementAt(i).GetX() + " " + Capturepoints.elementAt(i).GetY(), Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    public void CheckHits(double PointSize, double accuracy) //check if player collides with point (accuracymod = sizeofdot)
    {
        App globalVariable = (App) getApplicationContext();
        int Nrcaptured = 0; //If captured=true -> counter rises -> if counter >= nrOfPoints -> All capped: End game

        for(Player player:Players) {
            for (int j = 0; j < Capturepoints.size(); j++) {
                if (!Capturepoints.elementAt(j).Captured()) {// if not captured -> check if hit

                    float[] dist = new float[4];
                    Location.distanceBetween(Capturepoints.elementAt(j).GetY(), Capturepoints.elementAt(j).GetX(), player.GetY(), player.GetX(), dist);
                    //Captrue if accmod is in orde
                    if (dist[0] < PointSize) {

                        if (accuracy < 60) {// if hit capture point and add hit
                            Capturepoints.elementAt(j).Capture(); //todo:: add player with score calc
                            Nrcaptured += 1;
                            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(
                                    CameraPosition.fromLatLngZoom(new LatLng(Capturepoints.elementAt(j).GetY(),
                                                    Capturepoints.elementAt(j).GetX()),
                                            (float) (17f / Math.pow(radius / 10, 0.15))
                                    )));
                            if (player.GetIsMe()) {
                                Toast.makeText(this, "Point " + Capturepoints.elementAt(j).GetIndex() + " Captured", Toast.LENGTH_LONG).show();
                                Vibrator v = (Vibrator) this.getSystemService(VIBRATOR_SERVICE);
                                // Vibrate for 500 milliseconds
                                v.vibrate(500);
                                boolean isMock = player.GetLocation() != null && player.GetLocation().isFromMockProvider();
                                if(!isMock || BuildConfig.DEBUG) { //if cheats -> don't count score (except when debugging)
                                    mAchievementsClient.increment(this.getString(R.string.achievement_generation_i), 1);
                                    if (radius <= 100) {
                                        globalVariable.MainUser().AddScore(radius / 10);
                                    } else {
                                        globalVariable.MainUser().AddScore(50);
                                    }
                                }
                            }
                        } else {
                            Toast.makeText(this, "GPS not accurate enough to capture", Toast.LENGTH_SHORT).show();
                        }
                    }

                } else {
                    Nrcaptured += 1;//count the previously captured
                }
            }

            //End game
            if (Nrcaptured >= nrOfPoints) {
                if (!deletedpoint){
                if (radius <= 1000) { //if you caught all points fair and square -> calculate bonus points
                    globalVariable.MainUser().AddScore((radius / 10) * nrOfPoints / 2);
                } else {
                    globalVariable.MainUser().AddScore((radius / 100) * nrOfPoints / 2);
                }}

                mAchievementsClient.unlock(this.getString(R.string.achievement_until_the_end));
                playing = false;

               /* if (globalVariable.MainUser().GetUserid() >= 0) {
                    if (Postscore == null) {
                        Postscore = new PostScoreTask();
                        Postscore.execute("all");
                    }

                } else {
                    Toast.makeText(getApplicationContext(), "Log in to upload highscore", Toast.LENGTH_SHORT).show();
                }*/

                mLeaderboardsClient.submitScore(this.getString(R.string.leaderboard_daily_highscores), globalVariable.MainUser().GetScoreDay());
                mLeaderboardsClient.submitScore(this.getString(R.string.leaderboard_weekly_highscores), globalVariable.MainUser().GetScoreWeek());
                mLeaderboardsClient.submitScore(this.getString(R.string.leaderboard_all_time_highscores), globalVariable.MainUser().GetScore());

                finish();
            }
        }

    }//end checkhits

    /*
    Gestures
    */
    @Override
    public void onMapClick(LatLng Tappos) {

        for (int j = 0; j < Capturepoints.size(); j++) {
            if (!Capturepoints.elementAt(j).Captured()) {
                float[] dist = new float[4];
                Location.distanceBetween(Capturepoints.elementAt(j).GetY(), Capturepoints.elementAt(j).GetX(), Tappos.latitude, Tappos.longitude, dist);

                if (dist[0] < Pointsize) {
                    new AlertDialog.Builder(this)
                            .setTitle("Delete point")
                            .setMessage("Are you sure you want to delete point " + Capturepoints.elementAt(j).GetIndex() + "? You will lose any bonus points for finishing")
                            .setPositiveButton(android.R.string.yes, (dialog, which) -> {
                                capthis = true;
                                deletedpoint = true;
                            })
                            .setNegativeButton(android.R.string.no, null)
                            .setIcon(android.R.drawable.ic_delete)
                            .show();

                    if (capthis) {
                        capthis = false;
                        Capturepoints.elementAt(j).Skip();
                    }
                }
            }
        }

        RefreshMap();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            if (ev.getY() < 150) {
                showActionBarTemporarily();
            }
        }
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
        return false;
    }

    @Override
    public boolean onDoubleTap(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onDoubleTapEvent(MotionEvent e) {
        CheckHits(Pointsize, 0);
        if (Players.elementAt(0).GetLocation() != null) {
            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(
                    CameraPosition.fromLatLngZoom(new LatLng(Players.elementAt(0).GetY(),
                                    Players.elementAt(0).GetX()),
                            (float) (17f / Math.pow(radius / 10.0, 0.15))
                    )));
        }
        RefreshMap();
        return false;
    }

    @Override
    public void onLongPress(MotionEvent e) {
        if (Players.elementAt(0).GetLocation() != null) {
            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(
                    CameraPosition.fromLatLngZoom(new LatLng(Players.elementAt(0).GetY(),
                                    Players.elementAt(0).GetX()),
                            (float) (17f / Math.pow(radius / 10.0, 0.15))
                    )));
        }
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
            CheckHits(Pointsize, 0);
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
    }

    //string myParameters = "userid=" + Convert.ToInt64(App.Mainuser.Userid) + "&score=" + App.Mainuser.Score + "&type=all";
    //Uri URI = new Uri("http://cmee.yzi.me/index.php/app/sethighscores", UriKind.Absolute);
    private class PostScoreTask extends AsyncTask<String, Void, Boolean> {

        private Exception exception;
        private final String reply = "Highscores not uploaded";

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
            if (!reply.equals("Highscores not uploaded")) {
                mAchievementsClient.unlock(getApplicationContext().getString(R.string.achievement_until_the_end));
            }

            super.onPostExecute(aBoolean);
        }

        private void SendToast(final String message) {

            View posted = findViewById(R.id.map);
            posted.post(() -> Toast.makeText(getApplicationContext(), reply, Toast.LENGTH_LONG).show());
        }

        /**
         *
         * Old way of posting score -sit no longer online
         *
         * */
        public void postScore(String type) {
            // Create a new HttpClient and Post Header
            /*HttpClient httpclient = new DefaultHttpClient();
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
            }*/
        }
    }

}
