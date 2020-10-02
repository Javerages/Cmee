package be.javerage.cmee;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.games.Games;
import com.google.android.gms.plus.Plus;
import com.google.example.games.basegameutils.BaseGameUtils;

import be.javerage.cmee.cmee.cmee.R;

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


public class MainMenu extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks {
    private static int RC_SIGN_IN = 9001;
    PostScoreTask Postscore = null;
    private GoogleApiClient mGoogleApiClient;
    private boolean mResolvingConnectionFailure = false;
    private boolean mAutoStartSignInflow = true;
    private boolean mSignInClicked = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Plus.API).addScope(Plus.SCOPE_PLUS_LOGIN)
                .addApi(Games.API).addScope(Games.SCOPE_GAMES)
                .addOnConnectionFailedListener(this)
                .build();


    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        App globalVariable = (App) getApplicationContext();
        TextView mloginlbl = (TextView) findViewById(R.id.loggedIn);
        mloginlbl.setText("");
        if (globalVariable.MainUser().GetUserid() >= 0) {
            mloginlbl.setVisibility(View.VISIBLE);
            mloginlbl.setText("Logged in as " + globalVariable.MainUser().GetUsername());
        }

        TextView mScoreView = (TextView) findViewById(R.id.Score);
        if (globalVariable.MainUser().GetScore() >= 0) {
            mScoreView.setVisibility(View.VISIBLE);
            mScoreView.setText("Score: " + globalVariable.MainUser().GetScore());
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, Settings.class);
            //intent.putExtra(EXTRA_MESSAGE, message); Send extra data
            startActivity(intent);
        }

        if (id == R.id.action_Chieves) {
            if (mGoogleApiClient.isConnected()) {
                startActivityForResult(Games.Achievements.getAchievementsIntent(
                        mGoogleApiClient), 1);
            } else {
                mSignInClicked = true;
                mGoogleApiClient.connect();
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

        if (mResolvingConnectionFailure) {
            // already resolving
            return;
        }

        // if the sign-in button was clicked or if auto sign-in is enabled,
        // launch the sign-in flow
        if (mSignInClicked || mAutoStartSignInflow) {
            mAutoStartSignInflow = false;
            mSignInClicked = false;
            mResolvingConnectionFailure = true;

            // Attempt to resolve the connection failure using BaseGameUtils.
            // The R.string.signin_other_error value should reference a generic
            // error string in your strings.xml file, such as "There was
            // an issue with sign-in, please try again later."
            if (!BaseGameUtils.resolveConnectionFailure(this,
                    mGoogleApiClient, connectionResult,
                    RC_SIGN_IN, "Error")) {
                mResolvingConnectionFailure = false;

            }
        }
        //Toast.makeText(this, "Connection lost", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if (requestCode == RC_SIGN_IN) {
            mSignInClicked = false;
            mResolvingConnectionFailure = false;
            if (resultCode == RESULT_OK) {
                mGoogleApiClient.connect();
                Button Loginbtn = (Button) findViewById(R.id.buttonLogin);
                Loginbtn.setVisibility(View.GONE);
                Button Logoutbtn = (Button) findViewById(R.id.buttonLogout);
                Logoutbtn.setVisibility(View.VISIBLE);
            } else {
                // Bring up an error dialog to alert the user that sign-in
                // failed. The R.string.signin_failure should reference an error
                // string in your strings.xml file that tells the user they
                // could not be signed in, such as "Unable to sign in."
                BaseGameUtils.showActivityResultError(this,
                        requestCode, resultCode, R.string.signin_failure);
            }
        }
    }

    public void GoPlay(View view) {
        int googlePlayServicesIsAvailable = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(getApplicationContext());
            if (ConnectionResult.SUCCESS != googlePlayServicesIsAvailable) {
                //TODO:: treat error
            }

        //request permissions if needed
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Check Permissions Now
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.VIBRATE, Manifest.permission.ACCESS_NETWORK_STATE, Manifest.permission.INTERNET},
                    2);
        } else {
            Intent intent = new Intent(this, Game.class);
            //intent.putExtra(EXTRA_MESSAGE, message); Send extra data
            startActivity(intent);
        }

    }

    public void GoSettings(View view) {
        Intent intent = new Intent(this, Settings.class);
        //intent.putExtra(EXTRA_MESSAGE, message); Send extra data
        startActivity(intent);
    }

    public void GoChieves(View view) {
        if (mGoogleApiClient.isConnected()) {
            startActivityForResult(Games.Achievements.getAchievementsIntent(
                    mGoogleApiClient), 1);
        } else {
            mSignInClicked = true;
            mGoogleApiClient.connect();
        }
    }

    public void GoLogin(View view) {
      /*  Intent intent = new Intent(this, Login.class);
        //intent.putExtra(EXTRA_MESSAGE, message); Send extra data
        startActivity(intent);*/
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.GET_ACCOUNTS)
                != PackageManager.PERMISSION_GRANTED) {
            // Check Permissions Now
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.GET_ACCOUNTS, Manifest.permission.READ_CONTACTS},
                    2);
        } else {

        }

        if (mGoogleApiClient != null) {
            if (!mGoogleApiClient.isConnected()) {
                mSignInClicked = true;
                mGoogleApiClient.connect();
            } else {
                mGoogleApiClient.disconnect();
                findViewById(R.id.buttonLogin).setVisibility(View.VISIBLE);
                findViewById(R.id.buttonLogout).setVisibility(View.GONE);
            }
        }
    }

    public void GoHighscores(View view) {
        App globalVariable = (App) getApplicationContext();
        if (mGoogleApiClient.isConnected()) {
            Games.Leaderboards.submitScore(mGoogleApiClient, this.getString(R.string.leaderboard_daily_highscores), globalVariable.MainUser().GetScoreDay());
            Games.Leaderboards.submitScore(mGoogleApiClient, this.getString(R.string.leaderboard_weekly_highscores), globalVariable.MainUser().GetScoreWeek());
            Games.Leaderboards.submitScore(mGoogleApiClient, this.getString(R.string.leaderboard_all_time_highscores), globalVariable.MainUser().GetScore());
            Intent intent = new Intent(this, Highscores.class);
            //intent.putExtra(EXTRA_MESSAGE, message); Send extra data
            startActivity(intent);
        } else {
            mSignInClicked = true;
            mGoogleApiClient.connect();
        }

      /*  App globalVariable = (App) getApplicationContext();
        if (globalVariable.MainUser().GetUserid() >= 0) {
            if (Postscore != null) {
                if (Postscore.finished) {
                    Postscore = null;
                }
            }
            if (Postscore == null) {
                Postscore = new PostScoreTask();
                Postscore.execute("all");
            }
        } else {
            Toast.makeText(getApplicationContext(), "Log in to upload highscore", Toast.LENGTH_SHORT).show();
        }*

        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://cmee.yzi.me/index.php/app/highscores")));
        */
    }

    @Override
    public void onConnected(Bundle bundle) {
        Button Loginbtn = (Button) findViewById(R.id.buttonLogin);
        Loginbtn.setVisibility(View.GONE);
        Button Logoutbtn = (Button) findViewById(R.id.buttonLogout);
        Logoutbtn.setVisibility(View.VISIBLE);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main_menu, container, false);
            return rootView;
        }
    }

    public static class AdFragment extends Fragment {
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            return inflater.inflate(R.layout.fragment_ads, container, false);
        }

        @Override
        public void onActivityCreated(Bundle bundle) {
            super.onActivityCreated(bundle);
            AdView mAdView = (AdView) getView().findViewById(R.id.adView);
            AdRequest adRequest = new AdRequest.Builder().build();
            mAdView.loadAd(adRequest);
        }
    }

    //string myParameters = "userid=" + Convert.ToInt64(App.Mainuser.Userid) + "&score=" + App.Mainuser.Score + "&type=all";
    //Uri URI = new Uri("http://cmee.yzi.me/index.php/app/sethighscores", UriKind.Absolute);
    private class PostScoreTask extends AsyncTask<String, Void, Boolean> {

        private Exception exception;
        private String reply = "No connection";
        private boolean finished = false;

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
            if (reply.equals("Highscore saved")) {
                if (mGoogleApiClient.isConnected()) {
                    Games.Achievements.unlock(mGoogleApiClient, getApplicationContext().getString(R.string.achievement_beat_that));
                } else {

                }
            }
            finished = true;
            super.onPostExecute(aBoolean);
        }

        private void SendToast(final String message) {

            View posted = findViewById(R.id.buttonHighscores);
            posted.post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getApplicationContext(), reply, Toast.LENGTH_LONG).show();
                }
            });
        }

        /**
         * old class to post score to website - bad implementation -feelsbadman (use google services instead)
         */
        public void postScore(String type) {
            /*
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
                // Auto-generated catch block
            } catch (IOException e) {
                // Auto-generated catch block
            }*/
        }
    }

}

