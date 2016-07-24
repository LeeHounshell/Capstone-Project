package com.harlie.radiotheater.radiomysterytheater;

import android.content.Context;
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
                    DataMap dmap = DataMapItem.fromDataItem(event.getDataItem()).getDataMap();
                    LogHelper.v(TAG, "---> GOT RADIO_INFO_PATH="+path+", dmap="+dmap.toString());
                }
                else {
                    LogHelper.w(TAG, "UNEXPECTED PATH: "+path);
                    LogHelper.w(TAG, "UNEXPECTED DATA: "+ Arrays.toString(data));
                }
            }
        }
    }

    public static void createSyncMessage() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                DataMap dmap = new DataMap();
                dmap.putString("hello", "world");
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
                }
            }
        }).start();
    }

}
