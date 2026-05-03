package be.javerage.cmee;

import android.Manifest;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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





public class MainMenu extends AppCompatActivity {

    private AchievementsClient mAchievementsClient;
    private LeaderboardsClient mLeaderboardsClient;
    private GamesSignInClient mGamesSignInClient;

    private final ActivityResultLauncher<Intent> mAchievementsLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        // Handle result if needed
                    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        GoogleApiHelper.initializePlayGames(this);
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
            updateUIForSignedInState(isAuthenticated);
        });
    }

    private void updateUIForSignedInState(boolean signedIn) {
        findViewById(R.id.buttonLogin).setVisibility(signedIn ? View.GONE : View.VISIBLE);
        findViewById(R.id.buttonLogout).setVisibility(signedIn ? View.VISIBLE : View.GONE);
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
            mloginlbl.setText(getString(R.string.logged_in_as, globalVariable.MainUser().GetUsername()));
        }

        TextView mScoreView = findViewById(R.id.Score);
        if (globalVariable.MainUser().GetScore() >= 0) {
            mScoreView.setVisibility(View.VISIBLE);
            mScoreView.setText(getString(R.string.score_format, globalVariable.MainUser().GetScore()));
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
                    .addOnSuccessListener(mAchievementsLauncher::launch);
        }
        return super.onOptionsItemSelected(item);
    }

    public void GoPlay(View view) {
        int googlePlayServicesIsAvailable = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(getApplicationContext());
        if (ConnectionResult.SUCCESS != googlePlayServicesIsAvailable) {
            //TODO:: treat error
            Dialog errorDialog = GoogleApiAvailability.getInstance().getErrorDialog(this, googlePlayServicesIsAvailable, 1);
            if (errorDialog != null) {
                errorDialog.show();
            }
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
                .addOnSuccessListener(mAchievementsLauncher::launch);
    }

    public void GoLogin(View view) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.GET_ACCOUNTS)
                != PackageManager.PERMISSION_GRANTED) {
            // Check Permissions Now
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.GET_ACCOUNTS, Manifest.permission.READ_CONTACTS, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    2);
        }

        mGamesSignInClient.signIn().addOnCompleteListener(task ->
                updateUIForSignedInState(task.isSuccessful() && task.getResult().isAuthenticated()));
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
            return inflater.inflate(R.layout.fragment_main_menu, container, false);
        }
    }

    public static class AdFragment extends Fragment {
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            return inflater.inflate(R.layout.fragment_ads, container, false);
        }

        @Override
        public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            AdView mAdView = view.findViewById(R.id.adView);
            if (mAdView != null) {
                AdRequest adRequest = new AdRequest.Builder().build();
                mAdView.loadAd(adRequest);
            }
        }
    }

}

