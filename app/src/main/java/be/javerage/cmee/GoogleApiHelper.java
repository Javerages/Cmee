package be.javerage.cmee;

import static android.provider.Settings.System.getString;

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

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.games.GamesClientStatusCodes;
import com.google.android.gms.tasks.OnCompleteListener;
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


    private GoogleSignInClient mGoogleSignInClient;
    private GoogleSignInAccount mSignedInAccount = null;
    private final AppCompatActivity mActivity;
    private Context mAppContext = null;
    Handler mHandler;


    public GoogleApiHelper(AppCompatActivity activity) {
        mActivity = activity;
        mAppContext = activity.getApplicationContext();
        mHandler = new Handler();
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {

        if (requestCode == RC_SIGN_IN) {

            Task<GoogleSignInAccount> task =
                    GoogleSignIn.getSignedInAccountFromIntent(intent);

            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                onConnected(account);
            } catch (ApiException apiException) {
                String message = apiException.getMessage();
                if (message == null || message.isEmpty()) {
                    message = "Other error";//getString(R.string.signin_other_error);
                }

                onDisconnected();

                new AlertDialog.Builder(mAppContext)
                        .setMessage(message)
                        .setNeutralButton(android.R.string.ok, null)
                        .show();
            }
        }
    }

    public void startSignin() {
        createApiClientBuilder();
        startSignInIntent();
    }

    private void createApiClientBuilder() {
        mGoogleSignInClient = GoogleSignIn.getClient(mActivity,
                new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_GAMES_SIGN_IN)
                        // Since we are using SavedGames, we need to add the SCOPE_APPFOLDER to access Google Drive.
                        //.requestScopes(Drive.SCOPE_APPFOLDER)
                        .build());
    }

    public void startSignInIntent() {
        ActivityCompat.startActivityForResult(mActivity, mGoogleSignInClient.getSignInIntent(), RC_SIGN_IN, null);
    }

    /**
     * Try to sign in without displaying dialogs to the user.
     * <p>
     * If the user has already signed in previously, it will not show dialog.
     */
    public void signInSilently() {
        Log.d(TAG, "signInSilently()");

        mGoogleSignInClient.silentSignIn().addOnCompleteListener((Executor) this,
                new OnCompleteListener<GoogleSignInAccount>() {
                    @Override
                    public void onComplete(@NonNull Task<GoogleSignInAccount> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "signInSilently(): success");
                            onConnected(task.getResult());
                        } else {
                            Log.d(TAG, "signInSilently(): failure", task.getException());
                            onDisconnected();
                        }
                    }
                });
    }

    public void signOut() {

        mGoogleSignInClient.signOut().addOnCompleteListener((Executor) this,
                new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if (task.isSuccessful()) {
                            Log.d(TAG, "signOut(): success");
                        } else {
                            handleException(task.getException(), "signOut() failed!");
                        }

                        onDisconnected();
                    }
                });
    }

    private void onConnected(GoogleSignInAccount googleSignInAccount) {
        Log.d(TAG, "onConnected(): connected to Google APIs");

        if (mSignedInAccount != googleSignInAccount) {

            mSignedInAccount = googleSignInAccount;

            onAccountChanged(googleSignInAccount);
        }
    }

    private void onAccountChanged(GoogleSignInAccount googleSignInAccount) {
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
        return mGoogleSignInClient != null;
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
