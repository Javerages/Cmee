package be.khlim.student.cmee.cmee;

import android.app.Application;

/**
 * Created by Elsen on 27/12/2014.
 */
public class App extends Application{

    private User mainuser;
    private Boolean playing = false;


       public User ObjMainuser()
        {
            // Delay creation of the view model until necessary
            if (mainuser == null) {
                mainuser = new User();
            }
            return mainuser;
        };
}