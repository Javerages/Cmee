package be.javerage.cmee;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.games.GamesClientStatusCodes;
import com.google.android.gms.games.GamesSignInClient;
import com.google.android.gms.games.PlayGames;
import com.google.android.gms.games.PlayGamesSdk;
import com.google.android.gms.tasks.Task;

import java.util.concurrent.Executor;

public class GoogleApiHelper {
    private static final String TAG = "CmeeGoogleAPIHelper";
    public static final int RC_SIGN_IN = 9001;
    // Request code for listing saved games
    public static final int RC_LIST_SAVED_GAMES = 9002;
    // Request code for selecting a snapshot
    public static final int RC_SELECT_SNAPSHOT = 9003;
    // Request code for saving the game to a snapshot.
    public static final int RC_SAVE_SNAPSHOT = 9004;
    public static final int RC_LOAD_SNAPSHOT = 9005;


    private GamesSignInClient mGamesSignInClient;
    private final AppCompatActivity mActivity;
    private Context mAppContext = null;
    Handler mHandler;

    private static boolean sIsPlayGamesInitialized = false;

    /**
     * Encapsulated initialization to ensure it happens exactly once.
     */
    public static synchronized void initializePlayGames(Context context) {
        if (!sIsPlayGamesInitialized) {
            PlayGamesSdk.initialize(context);
            sIsPlayGamesInitialized = true;
        }
    }


    public GoogleApiHelper(AppCompatActivity activity) {
        mActivity = activity;
        mAppContext = activity.getApplicationContext();
        mHandler = new Handler();
        initializePlayGames(mAppContext);
        mGamesSignInClient = PlayGames.getGamesSignInClient(mActivity);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        // In PGS v2, many flows no longer use onActivityResult for sign-in
    }

    public void startSignin() {
        mGamesSignInClient.signIn().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult().isAuthenticated()) {
                Log.d(TAG, "signIn(): success");
                onConnected();
            } else {
                Log.d(TAG, "signIn(): failure");
                onDisconnected();
            }
        });
    }

    /**
     * Try to sign in without displaying dialogs to the user.
     * <p>
     * If the user has already signed in previously, it will not show dialog.
     */
    public void signInSilently() {
        Log.d(TAG, "signInSilently()");

        mGamesSignInClient.isAuthenticated().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult().isAuthenticated()) {
                Log.d(TAG, "signInSilently(): success");
                onConnected();
            } else {
                Log.d(TAG, "signInSilently(): failure");
                onDisconnected();
            }
        });
    }

    public void signOut() {
        Log.d(TAG, "signOut(): Not supported in PGS v2. Users manage sign-out via Play Games app.");
        onDisconnected();
    }

    private void onConnected() {
        Log.d(TAG, "onConnected(): connected to Google APIs");
        onAccountChanged();
    }

    private void onAccountChanged() {
        showSignOutBar();
    }

    private void onDisconnected() {
        Log.d(TAG, "onDisconnected()");
        showSignInBar();
    }

    /**
     * Shows the "sign in" bar (explanation and button).
     */
    private void showSignInBar() {
        //findViewById(R.id.sign_in_bar).setVisibility(View.VISIBLE);
        //findViewById(R.id.sign_out_bar).setVisibility(View.GONE);
    }

    /**
     * Shows the "sign out" bar (explanation and button).
     */
    private void showSignOutBar() {
        //findViewById(R.id.sign_in_bar).setVisibility(View.GONE);
        //findViewById(R.id.sign_out_bar).setVisibility(View.VISIBLE);
    }

    private boolean isSignedIn() {
        return mGamesSignInClient != null;
    }

    /**
     * Since a lot of the operations use tasks, we can use a common handler for whenever one fails.
     *
     * @param exception The exception to evaluate.  Will try to display a more descriptive reason for the exception.
     * @param details   Will display alongside the exception if you wish to provide more details for why the exception
     *                  happened
     */
    private void handleException(Exception exception, String details) {
        int status = 0;

        if (exception instanceof ApiException) {
            ApiException apiException = (ApiException) exception;
            status = apiException.getStatusCode();
        }

        String message = "Exception in GoogleAPIHelper";//getString(R.string.status_exception_error, details, status, exception);

        new AlertDialog.Builder(mAppContext)
                .setMessage(message)
                .setNeutralButton(android.R.string.ok, null)
                .show();

        // Note that showing a toast is done here for debugging. Your application should
        // resolve the error appropriately to your app.
        if (status == GamesClientStatusCodes.SNAPSHOT_NOT_FOUND) {
            Log.i(TAG, "Error: Snapshot not found");
            Toast.makeText(mActivity.getBaseContext(), "Error: Snapshot not found",
                    Toast.LENGTH_SHORT).show();
        } else if (status == GamesClientStatusCodes.SNAPSHOT_CONTENTS_UNAVAILABLE) {
            Log.i(TAG, "Error: Snapshot contents unavailable");
            Toast.makeText(mActivity.getBaseContext(), "Error: Snapshot contents unavailable",
                    Toast.LENGTH_SHORT).show();
        } else if (status == GamesClientStatusCodes.SNAPSHOT_FOLDER_UNAVAILABLE) {
            Log.i(TAG, "Error: Snapshot folder unavailable");
            Toast.makeText(mActivity.getBaseContext(), "Error: Snapshot folder unavailable.",
                    Toast.LENGTH_SHORT).show();
        }
    }


}
