package be.javerage.cmee;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.games.AchievementsClient;
import com.google.android.gms.games.LeaderboardsClient;
import com.google.android.gms.games.PlayGames;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import be.javerage.cmee.R;


public class Highscores extends AppCompatActivity {
    private AchievementsClient mAchievementsClient;
    private LeaderboardsClient mLeaderboardsClient;
    private final ExecutorService mExecutorService = Executors.newSingleThreadExecutor();
    private String mUploadReply = "No connection";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        GoogleApiHelper.initializePlayGames(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_highscores);

        mAchievementsClient = PlayGames.getAchievementsClient(this);
        mLeaderboardsClient = PlayGames.getLeaderboardsClient(this);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_highscores, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
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

    public void GoDaily(View view) {
        mLeaderboardsClient.getLeaderboardIntent(this.getString(R.string.leaderboard_daily_highscores))
                .addOnSuccessListener(intent -> startActivityForResult(intent, 1));
    }

    public void GoWeekly(View view) {
        mLeaderboardsClient.getLeaderboardIntent(this.getString(R.string.leaderboard_weekly_highscores))
                .addOnSuccessListener(intent -> startActivityForResult(intent, 2));
    }

    public void GoAll(View view) {
        mLeaderboardsClient.getLeaderboardIntent(this.getString(R.string.leaderboard_all_time_highscores))
                .addOnSuccessListener(intent -> startActivityForResult(intent, 3));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mExecutorService.shutdown();
    }

    public void postScore(String type) {
        mExecutorService.execute(() -> {
            try {
                // Logic previously in MainMenu/Game
                // This is a placeholder for the legacy PHP upload logic if ever needed
                // Currently replaced by Play Games Leaderboards
                
                /*
                App globalVariable = (App) getApplicationContext();
                // legacy upload code here...
                */
                
                mUploadReply = "Highscore saved";
            } catch (Exception e) {
                mUploadReply = "Upload failed";
            }

            runOnUiThread(() -> {
                Toast.makeText(getApplicationContext(), mUploadReply, Toast.LENGTH_LONG).show();
                if (mUploadReply.equals("Highscore saved")) {
                    mAchievementsClient.unlock(getString(R.string.achievement_beat_that));
                }
            });
        });
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

    public static class PlaceholderFragment extends Fragment {

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_highscores, container, false);
            return rootView;
        }
    }
}
