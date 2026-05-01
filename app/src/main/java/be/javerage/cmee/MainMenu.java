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
import com.google.android.gms.games.AchievementsClient;
import com.google.android.gms.games.GamesSignInClient;
import com.google.android.gms.games.LeaderboardsClient;
import com.google.android.gms.games.PlayGames;
import com.google.android.gms.games.PlayGamesSdk;

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


public class MainMenu extends AppCompatActivity {
    PostScoreTask Postscore = null;

    private AchievementsClient mAchievementsClient;
    private LeaderboardsClient mLeaderboardsClient;
    private GamesSignInClient mGamesSignInClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        PlayGamesSdk.initialize(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);

        mAchievementsClient = PlayGames.getAchievementsClient(this);
        mLeaderboardsClient = PlayGames.getLeaderboardsClient(this);
        mGamesSignInClient = PlayGames.getGamesSignInClient(this);

        checkIfAutomaticallySignedIn();
    }

    private void checkIfAutomaticallySignedIn() {
        mGamesSignInClient.isAuthenticated().addOnCompleteListener(task -> {
            boolean isAuthenticated = (task.isSuccessful() && task.getResult().isAuthenticated());
            if (isAuthenticated) {
                updateUIForSignedInState(true);
            } else {
                updateUIForSignedInState(false);
            }
        });
    }

    private void updateUIForSignedInState(boolean signedIn) {
        Button Loginbtn = findViewById(R.id.buttonLogin);
        Button Logoutbtn = findViewById(R.id.buttonLogout);
        if (signedIn) {
            Loginbtn.setVisibility(View.GONE);
            Logoutbtn.setVisibility(View.VISIBLE);
        } else {
            Loginbtn.setVisibility(View.VISIBLE);
            Logoutbtn.setVisibility(View.GONE);
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
    }

    @Override
    protected void onResume() {
        super.onResume();
        App globalVariable = (App) getApplicationContext();
        TextView mloginlbl = findViewById(R.id.loggedIn);
        mloginlbl.setText("");
        if (globalVariable.MainUser().GetUserid() >= 0) {
            mloginlbl.setVisibility(View.VISIBLE);
            mloginlbl.setText("Logged in as " + globalVariable.MainUser().GetUsername());
        }

        TextView mScoreView = findViewById(R.id.Score);
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
            mAchievementsClient.getAchievementsIntent()
                    .addOnSuccessListener(intent -> startActivityForResult(intent, 1));
        }
        return super.onOptionsItemSelected(item);
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
        mAchievementsClient.getAchievementsIntent()
                .addOnSuccessListener(intent -> startActivityForResult(intent, 1));
    }

    public void GoLogin(View view) {
      /*  Intent intent = new Intent(this, Login.class);
        //intent.putExtra(EXTRA_MESSAGE, message); Send extra data
        startActivity(intent);*/
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.GET_ACCOUNTS)
                != PackageManager.PERMISSION_GRANTED) {
            // Check Permissions Now
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.GET_ACCOUNTS, Manifest.permission.READ_CONTACTS, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    2);
        } else {
        //Error?
        }

        mGamesSignInClient.signIn().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult().isAuthenticated()) {
                updateUIForSignedInState(true);
            } else {
                updateUIForSignedInState(false);
            }
        });
    }

    public void GoHighscores(View view) {
        App globalVariable = (App) getApplicationContext();
        mLeaderboardsClient.submitScore(this.getString(R.string.leaderboard_daily_highscores), globalVariable.MainUser().GetScoreDay());
        mLeaderboardsClient.submitScore(this.getString(R.string.leaderboard_weekly_highscores), globalVariable.MainUser().GetScoreWeek());
        mLeaderboardsClient.submitScore(this.getString(R.string.leaderboard_all_time_highscores), globalVariable.MainUser().GetScore());
        Intent intent = new Intent(this, Highscores.class);
        //intent.putExtra(EXTRA_MESSAGE, message); Send extra data
        startActivity(intent);
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
            AdView mAdView = getView().findViewById(R.id.adView);
            AdRequest adRequest = new AdRequest.Builder().build();
            mAdView.loadAd(adRequest);
        }
    }

    //string myParameters = "userid=" + Convert.ToInt64(App.Mainuser.Userid) + "&score=" + App.Mainuser.Score + "&type=all";
    //Uri URI = new Uri("http://cmee.yzi.me/index.php/app/sethighscores", UriKind.Absolute);
    private class PostScoreTask extends AsyncTask<String, Void, Boolean> {

        private Exception exception;
        private final String reply = "No connection";
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
                mAchievementsClient.unlock(getApplicationContext().getString(R.string.achievement_beat_that));
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

