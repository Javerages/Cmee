package be.khlim.student.cmee.cmee;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.widget.Toast;

/**
 * Created by Elsen on 27/12/2014.
 */
public class App extends Application{

    private User mainuser;
    private Boolean playing = false;
    public SharedPreferences storage;

    @Override
    public void onCreate()
    {
        super.onCreate();
        storage = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        //Toast.makeText(this, "Test toast", Toast.LENGTH_SHORT).show();
        MainUser().SetScore(storage.getInt("Score", 0));
        MainUser().SetUserid(storage.getInt("Userid", -1));
    }

       public User MainUser()
        {
            // Delay creation of the view model until necessary
            if (mainuser == null) {
                mainuser = new User();
            }
            return mainuser;
        };
}