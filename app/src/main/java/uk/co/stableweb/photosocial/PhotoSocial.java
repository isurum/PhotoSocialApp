package uk.co.stableweb.photosocial;

import android.app.Application;

import com.google.firebase.database.FirebaseDatabase;

/**
 * Created by Isuru on 09/10/2016.
 */

public class PhotoSocial extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
    }
}
