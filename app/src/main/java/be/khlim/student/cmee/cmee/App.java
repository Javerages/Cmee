package be.khlim.student.cmee.cmee;

import android.app.Application;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.preference.PreferenceManager;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Elsen on 27/12/2014.
 */
public class App extends Application {

    public SharedPreferences storage;
    private User mainuser;
    private Boolean playing = false;

    @Override
    public void onCreate() {
        super.onCreate();

        storage = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        //Toast.makeText(this, "Test toast", Toast.LENGTH_SHORT).show();
        MainUser().SetScore(storage.getInt("Score", 0), storage.getInt("ScoreWeek", 0), storage.getInt("ScoreDay", 0));
        String now = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        String LastPlaydate = storage.getString("lastPlayDate", "1000-01-01");
        if (!LastPlaydate.equals(now)) {
            MainUser().ClearScoreDay();
        }
        if (!LastPlaydate.substring(0,7).equals(now.substring(0, 7))) {
            MainUser().ClearScoreWeek();
        }

        MainUser().SetUserid(storage.getInt("Userid", -1));
        MainUser().SetUsername(storage.getString("Username", "Please log in"));

    }

    public User MainUser() {
        // Delay creation of the view model until necessary
        if (mainuser == null) {
            mainuser = new User();
        }
        return mainuser;
    }

    ;
}