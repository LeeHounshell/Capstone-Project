package com.harlie.radiotheater.radiomysterytheater.firebase;

import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.IgnoreExtraProperties;
import com.harlie.radiotheater.radiomysterytheater.utils.LogHelper;

@IgnoreExtraProperties
public class FirebaseConfiguration {
    private final static String TAG = "LEE: <" + FirebaseConfiguration.class.getSimpleName() + ">";

    public String  firebase_email;
    public String  firebase_user_name;
    public String  firebase_device_id;
    public Long    firebase_authenticated;
    public Boolean firebase_paid_version;
    public Boolean firebase_purchase_access;
    public Boolean firebase_purchase_noads;
    public Long    firebase_total_listen_count;

    public FirebaseConfiguration() {
        // Default constructor required for calls to DataSnapshot.getValue(FirebaseConfiguration.class)
    }

    public FirebaseConfiguration   (

            String  email,
            String  user_name,
            String  device_id,
            Long    authenticated,
            Boolean paid_version,
            Boolean purchase_access,
            Boolean purchase_noads,
            Long    total_listen_count
                                   )
    {
        this.firebase_email              = email;
        this.firebase_user_name          = user_name;
        this.firebase_device_id          = device_id;
        this.firebase_authenticated      = authenticated;
        this.firebase_paid_version       = paid_version;
        this.firebase_purchase_access    = purchase_access;
        this.firebase_purchase_noads     = purchase_noads;
        this.firebase_total_listen_count = total_listen_count;
    }

    public void commit(DatabaseReference firebase_database, final String key) {
        LogHelper.v(TAG, "commit for user configuration email="+firebase_email+", key="+key);
        if (firebase_database != null && key != null) {
            LogHelper.v(TAG, "commit: key=" + key);
            firebase_database.child("configuration").child("device").child(key).setValue(this, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                    if (databaseError != null) {
                        LogHelper.v(TAG, "commit: onComplete -  databaseError=" + databaseError.getMessage());
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

    public String getFirebase_user_name() {
        return firebase_user_name;
    }

    public Long getFirebase_authenticated() {
        return firebase_authenticated;
    }

    public String getFirebase_device_id() {
        return firebase_device_id;
    }

    public Boolean getFirebase_paid_version() {
        return firebase_paid_version;
    }

    public Boolean getFirebase_purchase_access() {
        return firebase_purchase_access;
    }

    public Boolean getFirebase_purchase_noads() {
        return firebase_purchase_noads;
    }

    public Long getFirebase_total_listen_count() {
        return firebase_total_listen_count;
    }

}
