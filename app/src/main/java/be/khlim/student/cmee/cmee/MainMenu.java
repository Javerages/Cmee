package be.khlim.student.cmee.cmee;

import android.app.Activity;
import android.app.ActionBar;
import android.app.Fragment;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.os.Build;
import android.widget.TextView;

import java.net.URI;


public class MainMenu extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);
        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
                    .commit();
        }

    }

    @Override
    protected void onResume(){
        super.onResume();
        App globalVariable = (App) getApplicationContext();
        TextView mloginlbl = (TextView) findViewById(R.id.loggedIn);
        mloginlbl.setText("");
        if(globalVariable.MainUser().GetUserid() >= 0){
            mloginlbl.setVisibility(View.VISIBLE);
            mloginlbl.setText("Logged in as " + globalVariable.MainUser().GetUsername());
        }

        TextView mScoreView = (TextView) findViewById(R.id.Score);
        if(globalVariable.MainUser().GetScore() >= 0){
            mScoreView.setVisibility(View.VISIBLE);
            mScoreView.setText("Score: "+ globalVariable.MainUser().GetScore());
        }

    }

    @Override
    protected void onPause(){
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
        return super.onOptionsItemSelected(item);
    }

    public void GoPlay(View view){

        Intent intent = new Intent(this, Game.class);
        //intent.putExtra(EXTRA_MESSAGE, message); Send extra data
        startActivity(intent);
    }

    public void GoSettings(View view){
        Intent intent = new Intent(this, Settings.class);
        //intent.putExtra(EXTRA_MESSAGE, message); Send extra data
        startActivity(intent);
    }

    public void GoLogin(View view){
        Intent intent = new Intent(this, Login.class);
        //intent.putExtra(EXTRA_MESSAGE, message); Send extra data
        startActivity(intent);
    }

    public void GoHighscores(View view){
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://cmee.yzi.me/index.php/app/highscores")));
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
}
