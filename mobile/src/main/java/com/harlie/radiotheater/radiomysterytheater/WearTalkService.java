package com.harlie.radiotheater.radiomysterytheater;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.media.session.PlaybackStateCompat;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;
import com.harlie.radiotheater.radiomysterytheater.data.configepisodes.ConfigEpisodesCursor;
import com.harlie.radiotheater.radiomysterytheater.data_helper.DataHelper;
import com.harlie.radiotheater.radiomysterytheater.playback.LocalPlayback;
import com.harlie.radiotheater.radiomysterytheater.utils.LogHelper;
import com.harlie.radiotheater.radiomysterytheater.utils.RadioStateHolder;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// wear messages come from the watch into the Radio Mystery Theater app
// from: https://gist.github.com/gabrielemariotti/117b05aad4db251f7534
public class WearTalkService
        extends
            WearableListenerService
        implements
            MessageApi.MessageListener
{
    private static final String TAG = "LEE: <" + WearTalkService.class.getSimpleName() + ">";

    private static ConnectionHandler sConnectionHandler;
    private static GoogleApiClient sGoogleApiClient;
    private static RadioStateHolder sRadioStateHolder;

    public static final String SYNC_PATH = "/radiomysterytheater/sync";
    public static final String RADIO_INFO_PATH = "/radiomysterytheater/episode";

    private static class ConnectionHandler
            implements
                GoogleApiClient.ConnectionCallbacks,
                GoogleApiClient.OnConnectionFailedListener
    {
        private final String TAG = "LEE: <" + ConnectionHandler.class.getSimpleName() + ">";
        boolean isConnected;

        public ConnectionHandler() {
        }

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
            isConnected = false;
        }

        @Override
        public void onConnected(@Nullable Bundle bundle) {
            LogHelper.v(TAG, "onConnected");
            isConnected = true;
            WearTalkService.sendRadioDataToWear();
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
        if (! sConnectionHandler.isConnected) {
            sConnectionHandler.connect(context);
        }
        else {
            WearTalkService.sendRadioDataToWear();
        }
    }

    public static void disconnect() {
        LogHelper.v(TAG, "disconnect");
        if (sConnectionHandler != null) {
            sConnectionHandler.disconnect();
        }
    }

    static RadioStateHolder radioDesignHolderOldValue;

    public static boolean sendRadioDataToWear() {
        String packageId = RadioTheaterApplication.getRadioTheaterApplicationContext().getPackageName();
        LogHelper.v(TAG, "sendRadioDataToWear: package="+packageId);
        sRadioStateHolder = new RadioStateHolder();
        sRadioStateHolder.reset();
        sRadioStateHolder.setDirty(true);
        final RadioStateHolder radioStateHolder = sRadioStateHolder;
        radioDesignHolderOldValue = null;
        if (radioStateHolder.isDirty()
                && (radioDesignHolderOldValue == null || !radioDesignHolderOldValue.equals(radioStateHolder)))
        {
            LogHelper.w(TAG, "episode has changed - need to send message - ok");
            if (sGoogleApiClient == null) {
                LogHelper.e(TAG, "*** NOT CONNECTED! - GoogleApiClient is null!");
                return false;
            }

            new Thread(new Runnable() {
                @Override
                public void run() {
                    // from: http://stackoverflow.com/questions/33716767/wearlistenerservice-ondatachanged-strange-behavior
                    //NodeApi.GetConnectedNodesResult nodes = Wearable.NodeApi.getConnectedNodes(googleClient);

                    final DataMap dataMap = radioStateHolder.toDataMap();
                    PendingResult<NodeApi.GetConnectedNodesResult> nodes = Wearable.NodeApi.getConnectedNodes(sGoogleApiClient);
                    nodes.setResultCallback(new ResultCallback<NodeApi.GetConnectedNodesResult>() {

                        @Override
                        public void onResult(NodeApi.GetConnectedNodesResult getConnectedNodesResult) {
                            List<Node> nodes = getConnectedNodesResult.getNodes();
                            for (Node node : nodes) {
                                final Node node2 = node;

                                // Construct a DataRequest and send over the data layer
                                // The system time is appended to ensure a unique path and force delivery.
                                // Google won't deliver data items it thinks were already seen.
                                PutDataMapRequest putDMR = PutDataMapRequest.create(WearTalkService.RADIO_INFO_PATH + "/" + System.currentTimeMillis());
                                putDMR.getDataMap().putAll(dataMap);
                                PutDataRequest request = putDMR.asPutDataRequest();
                                request.setUrgent();

                                LogHelper.v(TAG, "requesting send DataMap to " + node.getDisplayName());
                                PendingResult<DataApi.DataItemResult> pendingResult = Wearable.DataApi.putDataItem(sGoogleApiClient, request);
                                pendingResult.setResultCallback(new ResultCallback<DataApi.DataItemResult>() {
                                    @Override
                                    public void onResult(DataApi.DataItemResult dataItemResult) {
                                        if (dataItemResult.getStatus().isSuccess()) {
                                            LogHelper.v(TAG, "radio DataMap: " + dataMap + " requested send to: " + node2.getDisplayName());
                                            radioStateHolder.setDirty(false);
                                            radioDesignHolderOldValue = new RadioStateHolder(radioStateHolder);
                                        } else {
                                            LogHelper.v(TAG, "ERROR: failed to request send radio DataMap to: " + node2.getDisplayName());
                                        }
                                    }
                                });
                                LogHelper.v(TAG, "*** MESSAGE SENT FROM PHONE TO WEAR NODE "+node.getDisplayName());
                            }
                            if (nodes.size() == 0) {
                                LogHelper.w(TAG, "*** UNABLE TO SYNC WITH WEARABLE - NO CONNECTED NODES! ***");
                            }
                        }

                    });

                }
            }).start();
            return true;
        }
        else{
            LogHelper.w(TAG, "no radio message sent - isDirty:"+radioStateHolder.isDirty());
            return false;
        }
    }

    // receive the message from wear
    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        LogHelper.v(TAG, "---------> onMessageReceived FROM WEAR");
        if (messageEvent.getPath().equals(SYNC_PATH)) {
            String data = new String(messageEvent.getData());
            LogHelper.v(TAG, "=========> SYNC MESSAGE RECEIVED: "+data);
            long messageTime = 0L;
            String command = null;

            //
            // we only need two values from the JSON so use a regex pattern
            // EXAMPLE JSON:{command=toggle, time=1469456408954}
            //
            Pattern pattern = Pattern.compile(".*command=([^,]+), time=([^}]+).*");
            Matcher matcher = pattern.matcher((data));
            if (matcher.find()) {
                try {
                    command = matcher.group(1);
                    messageTime = Long.valueOf(matcher.group(2));
                }
                catch (NumberFormatException e) {
                    LogHelper.e(TAG, "*** UNABLE TO DECODE SYNC COMMAND FROM WEAR *** - data="+data);
                }
            }

            LogHelper.v(TAG, "===> messageTime="+messageTime+", command="+command);
            sendRadioDataToWear();

            // for now we ignore the 'command' and 'time' values and just toggle playback ON/OFF
            // make sure we have an episode loaded before playing anything.
            int state = LocalPlayback.getCurrentState();
            Context context = RadioTheaterApplication.getRadioTheaterApplicationContext();
            if (state != PlaybackStateCompat.STATE_PLAYING) {
                if (DataHelper.getEpisodeNumber() == 0) {
                    // load the next episode
                    ConfigEpisodesCursor configCursor = DataHelper.getCursorForNextAvailableEpisode();
                    DataHelper.getEpisodeDataForCursor(configCursor);
                }
                LogHelper.v(TAG, "*** WEAR TOGGLE: START PLAYING EPISODE "+DataHelper.getEpisodeNumber());
                RadioControlIntentService.startActionPlay(context,
                        "WEAR",
                        DataHelper.getEpisodeNumberString(),
                        DataHelper.getEpisodeDownloadUrl(),
                        DataHelper.getEpisodeTitle());
            }
            else {
                LogHelper.v(TAG, "*** WEAR TOGGLE: PAUSE PLAYING EPISODE "+DataHelper.getEpisodeNumber());
                RadioControlIntentService.startActionPause(context,
                        "WEAR",
                        DataHelper.getEpisodeNumberString(),
                        DataHelper.getEpisodeDownloadUrl());
            }
        }
        else {
            LogHelper.v(TAG, "=========> UNKNOWN MESSAGE messageEvent: path="+messageEvent.getPath()+", data="+ Arrays.toString(messageEvent.getData()));
        }
    }

}
