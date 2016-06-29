package com.harlie.radiotheater.radiomysterytheater.firebase;

import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.IgnoreExtraProperties;
import com.harlie.radiotheater.radiomysterytheater.utils.LogHelper;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

@IgnoreExtraProperties
public class FirebaseConfigEpisode {
    private final static String TAG = "LEE: <" + FirebaseConfigEpisode.class.getSimpleName() + ">";

    public String  firebase_email;
    public String  firebase_episode_number;
    public Boolean firebase_purchased_access;
    public Boolean firebase_purchased_noads;
    public Boolean firebase_episode_downloaded;
    public Boolean firebase_episode_heard;
    public Long    firebase_episode_count;
    public Long    firebase_listen_duration;
    public String  firebase_listen_date;

    public FirebaseConfigEpisode() {
        // Default constructor required for calls to DataSnapshot.getValue(FirebaseConfigEpisode.class)
    }

    public FirebaseConfigEpisode   (

            String  email,
            String  episode_number,
            Boolean purchased_access,
            Boolean purchased_noads,
            Boolean episode_downloaded,
            Boolean episode_heard,
            Long    episode_count,
            Long    listen_duration
                                   )
    {
        this.firebase_email              = email;
        this.firebase_episode_number     = episode_number;
        this.firebase_purchased_access   = purchased_access;
        this.firebase_purchased_noads    = purchased_noads;
        this.firebase_episode_downloaded = episode_downloaded;
        this.firebase_episode_heard      = episode_heard;
        this.firebase_episode_count      = episode_count;
        this.firebase_listen_duration    = listen_duration;
        this.firebase_listen_date        = today();
    }

    public String today() {
        DateFormat dateTimeInstance = SimpleDateFormat.getDateTimeInstance();
        return dateTimeInstance.format(Calendar.getInstance().getTime());
    }

    public void commit(DatabaseReference firebase_database, final String key) {
        LogHelper.v(TAG, "commit for episode="+firebase_episode_number);
        if (firebase_database != null && key != null) {
            LogHelper.v(TAG, "commit: key=" + key);
            firebase_database.child("configuration").child(key).child(firebase_episode_number).setValue(this, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                    if (databaseError != null) {
                        LogHelper.v(TAG, "commit: onComplete - databaseError=" + databaseError.getMessage());
                    }
                    if (databaseReference != null) {
                        LogHelper.v(TAG, "commit: onComplete - databaseReference key=" + databaseReference.getKey());
                    }
                    if (databaseError == null && databaseReference != null) {
                        LogHelper.v(TAG, "commit: key="+key+" - SUCCESS!");
                    }
                }
            });
        }
        else {
            if (firebase_database == null) {
                LogHelper.e(TAG, "commit: FAIL - *** null Firebase Database! ***");
            }
            if (key == null) {
                LogHelper.e(TAG, "commit: FAIL - *** null Firebase key! ***");
            }
        }
    }

    public String getFirebase_email() {
        return firebase_email;
    }

    public String getFirebase_episode_number() {
        return firebase_episode_number;
    }

    public Boolean getFirebase_purchased_access() {
        return firebase_purchased_access;
    }

    public Boolean getFirebase_purchased_noads() {
        return firebase_purchased_noads;
    }

    public Boolean getFirebase_episode_downloaded() {
        return firebase_episode_downloaded;
    }

    public Boolean getFirebase_episode_heard() {
        return firebase_episode_heard;
    }

    public Long getFirebase_episode_count() {
        return firebase_episode_count;
    }

    public Long getFirebase_listen_duration() {
        return firebase_listen_duration;
    }

    public String getFirebase_listen_date() {
        return firebase_listen_date;
    }
}
