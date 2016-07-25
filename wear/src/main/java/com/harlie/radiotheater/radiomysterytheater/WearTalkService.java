package com.harlie.radiotheater.radiomysterytheater;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;
import com.harlie.radiotheater.radiomysterytheater.utils.LogHelper;

import java.util.Arrays;

// data items come from the radiomysterytheater app into the watch
// from: https://gist.github.com/gabrielemariotti/117b05aad4db251f7534
public class WearTalkService
        extends
            WearableListenerService
        implements
            DataApi.DataListener
{
    private static final String TAG = "LEE: <" + WearTalkService.class.getSimpleName() + ">";

    private static ConnectionHandler sConnectionHandler;
    private static GoogleApiClient sGoogleApiClient;

    private static long theTime;
    private static boolean isDirty;
    private static long radioState;
    private static long episodeNumber;
    private static long position;
    private static long duration;
    private static String title;
    private static String description;
    private static String airdate;

    public static final String RADIO_INFO_PATH = "/radiomysterytheater/episode";
    public static final String SYNC_PATH = "/radiomysterytheater/sync";

    private static class ConnectionHandler
            implements
                GoogleApiClient.ConnectionCallbacks,
                GoogleApiClient.OnConnectionFailedListener
    {
        private final String TAG = "LEE: <" + ConnectionHandler.class.getSimpleName() + ">";

        public void connect(Context context) {
            LogHelper.v(TAG, "connect");
            sGoogleApiClient = new GoogleApiClient.Builder(context)
                    .addApi(Wearable.API)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();
            sGoogleApiClient.connect();
        }

        public void disconnect() {
            LogHelper.v(TAG, "disconnect");
            if (sGoogleApiClient != null && sGoogleApiClient.isConnected()) {
                sGoogleApiClient.disconnect();
            }
        }

        @Override
        public void onConnected(@Nullable Bundle bundle) {
            LogHelper.v(TAG, "onConnected");
        }

        @Override
        public void onConnectionSuspended(int i) {
            LogHelper.v(TAG, "onConnectionSuspended");
        }

        @Override
        public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
            LogHelper.v(TAG, "onConnectionFailed");
        }

    }

    @Override
    public void onCreate() {
        LogHelper.v(TAG, "onCreate");
        super.onCreate();
        connect(getApplicationContext());
    }

    @Override
    public void onDestroy() {
        LogHelper.v(TAG, "onDestroy");
        super.onDestroy();
    }

    synchronized public static void connect(Context context) {
        LogHelper.v(TAG, "connect");
        if (sConnectionHandler == null) {
            sConnectionHandler = new ConnectionHandler();
        }
        sConnectionHandler.connect(context);
    }

    public static void disconnect() {
        LogHelper.v(TAG, "disconnect");
        if (sConnectionHandler != null) {
            sConnectionHandler.disconnect();
        }
    }

    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {
        LogHelper.v(TAG, "---------> onDataChanged");
        super.onDataChanged(dataEvents);
        for (DataEvent event : dataEvents) {
            if (event.getType() == DataEvent.TYPE_CHANGED) {
                DataMapItem mapItem = DataMapItem.fromDataItem(event.getDataItem());
                String path = event.getDataItem().getUri().getPath();
                byte[] data = event.getDataItem().getData();
                if (WearTalkService.RADIO_INFO_PATH.equals(path.substring(0, WearTalkService.RADIO_INFO_PATH.length()))) {
                    // EXAMPLE: dmap={title=The Old Ones Are Hard to Kill, episodeNumber=1, description=An old lady rents a room to a sick boarder. She runs into problems with his strange deathbed confession., theTime=1469474279353, airdate=1974-01-06, isDirty=true, radioState=3, duration=2522201, position=13926}
                    DataMap dmap = DataMapItem.fromDataItem(event.getDataItem()).getDataMap();
                    LogHelper.v(TAG, "---> GOT RADIO_INFO_PATH="+path+", dmap="+dmap.toString());
                    // decode the dmap
                    theTime = dmap.getLong("theTime");
                    isDirty = dmap.getBoolean("isDirty");
                    radioState = dmap.getLong("radioState");
                    episodeNumber = dmap.getLong("episodeNumber");
                    position = dmap.getLong("position");
                    duration = dmap.getLong("duration");
                    title = dmap.getString("title");
                    description = dmap.getString("description");
                    airdate = dmap.getString("airDate");
                    // notify the RadioWearActivity of the changes
                    Context context = getApplicationContext();
                    String wear_control_command = context.getResources().getString(R.string.wear_control_command);
                    String message = String.valueOf(radioState);
                    Intent intentMessage = new Intent("com.harlie.radiotheater.radiomysterytheater.WEAR").putExtra(wear_control_command, message);
                    context.sendBroadcast(intentMessage);
                    LogHelper.v(TAG, "---> theTime="+theTime+", isDirty="+isDirty+", radioState="+radioState+", episodeNumber="+episodeNumber
                            +", position="+position+", duration="+duration+", title="+title+", description="+description+", airdate="+airdate);
                    LogHelper.v(TAG, "---> message sent to RadioWearActivity");
                }
                else {
                    LogHelper.w(TAG, "UNEXPECTED PATH: "+path);
                    LogHelper.w(TAG, "UNEXPECTED DATA: "+ Arrays.toString(data));
                }
            }
        }
    }

    public static void createSyncMessage(Context context, final String command) {
        String packageId = context.getPackageName();
        LogHelper.v(TAG, "createSyncMessage: package=" + packageId);
        new Thread(new Runnable() {
            @Override
            public void run() {
                DataMap dmap = new DataMap();
                dmap.putString("time", String.valueOf(System.currentTimeMillis()));
                dmap.putString("command", command);
                NodeApi.GetConnectedNodesResult nodes = Wearable.NodeApi.
                        getConnectedNodes(sGoogleApiClient).await();
                for (final Node node : nodes.getNodes()) {
                    Wearable.MessageApi.sendMessage(
                            sGoogleApiClient,
                            node.getId(),
                            SYNC_PATH,
                            dmap.toString().getBytes()).setResultCallback(new ResultCallback<MessageApi.SendMessageResult>() {
                        @Override
                        public void onResult(@NonNull MessageApi.SendMessageResult result) {
                            if (!result.getStatus().isSuccess()) {
                                LogHelper.e(TAG, "UNABLE TO REQUEST SYNC WITH PHONE " + node.getDisplayName());
                            } else {
                                LogHelper.v(TAG, "SUCCESS! REQUESTED SYNC WITH PHONE " + node.getDisplayName());
                            }
                        }
                    });
                    LogHelper.v(TAG, "*** SYNC MESSAGE SENT FROM WEAR TO PHONE NODE "+node.getDisplayName());
                }
                if (nodes.getNodes().size() == 0) {
                    LogHelper.w(TAG, "*** UNABLE TO SYNC WITH PHONE - NOT CONNECTED ***");
                }
            }
        }).start();
    }

    public static long getTheTime() {
        return theTime;
    }

    public static boolean isDirty() {
        return isDirty;
    }

    public static long getRadioState() {
        return radioState;
    }

    public static long getEpisodeNumber() {
        return episodeNumber;
    }

    public static long getPosition() {
        return position;
    }

    public static long getDuration() {
        return duration;
    }

    public static String getTitle() {
        return title;
    }

    public static String getDescription() {
        return description;
    }

    public static String getAirdate() {
        return airdate;
    }

}
