package com.estimote.examples.demos.activities;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.estimote.examples.demos.R;
import com.estimote.sdk.Beacon;
import com.estimote.sdk.BeaconManager;
import com.estimote.sdk.Region;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static com.estimote.sdk.BeaconManager.MonitoringListener;

/**
 * Demo that shows how to use region monitoring. Two important steps are:
 * <ul>
 * <li>start monitoring region, in example in {@link #onResume()}</li>
 * <li>respond to monitoring changes by registering {@link MonitoringListener} in {@link BeaconManager}</li>
 * </ul>
 *
 * @author wiktor@estimote.com (Wiktor Gworek)
 */
public class NotifyDemoActivity extends AppCompatActivity {

    private static final int ICE_NOTIFICATION_ID = 123;
    private static final int BLUEBERRY_NOTIFICATION_ID = 123;

    private BeaconManager beaconManager;
    private NotificationManager notificationManager;
    private Region iceRegion;
    private Region blueberryRegion;

//    @Override
//    protected int getLayoutResId() {
//        return R.layout.notify_demo;
//    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.notify_demo);

        // Beacon 1 "ice" info
        UUID iceUUID = UUID.fromString("B9407F30-F5F8-466E-AFF9-25556B57FE6D");
        int iceMajor = 15011;
        int iceMinor = 3641;
        iceRegion = new Region("ice", iceUUID ,iceMajor,iceMinor);

        // Beacon 2 "blueberry" info
        UUID blueberryUUID = UUID.fromString("B9407F30-F5F8-466E-AFF9-25556B57FE6D");
        int blueberryMajor = 59081;
        int blueberryMinor = 14607;
        blueberryRegion = new Region("blueberry", blueberryUUID ,blueberryMajor,blueberryMinor);
//        Beacon beacon = getIntent().getParcelableExtra(ListBeaconsActivity.EXTRAS_BEACON);
//        region = new Region("regionId", beacon.getProximityUUID(), beacon.getMajor(), beacon.getMinor());
//        region = new Region("regionId", iceUUID ,iceMajor,iceMinor);
//        String uun = beacon.getProximityUUID().toString();
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        beaconManager = new BeaconManager(this);

        // Default values are 5s of scanning and 25s of waiting time to save CPU cycles.
        // In order for this demo to be more responsive and immediate we lower down those values.
//    beaconManager.setBackgroundScanPeriod(TimeUnit.SECONDS.toMillis(1), 0);
        beaconManager.setBackgroundScanPeriod(TimeUnit.SECONDS.toMillis(1), 0);

        beaconManager.setMonitoringListener(new MonitoringListener() {
            @Override
            public void onEnteredRegion(Region region, List<Beacon> beacons) {
                postNotification(region.getIdentifier(),true);
            }

            @Override
            public void onExitedRegion(Region region) {
                postNotification(region.getIdentifier(),false);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        notificationManager.cancel(ICE_NOTIFICATION_ID);
        notificationManager.cancel(BLUEBERRY_NOTIFICATION_ID);

        beaconManager.connect(new BeaconManager.ServiceReadyCallback() {
            @Override
            public void onServiceReady() {
                beaconManager.startMonitoring(iceRegion);
                beaconManager.startMonitoring(blueberryRegion);
            }
        });
    }

    @Override
    protected void onDestroy() {
        notificationManager.cancel(ICE_NOTIFICATION_ID);
        notificationManager.cancel(BLUEBERRY_NOTIFICATION_ID);
        beaconManager.disconnect();
        super.onDestroy();
    }

    private void postNotification(String Identifier , Boolean states) {
        String msg = states ? Identifier+" Entered region" : Identifier+" Exited region";
        int beaconColor = R.drawable.beacon_gray;
        Intent notifyIntent = new Intent(NotifyDemoActivity.this, NotifyDemoActivity.class);
        notifyIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivities(
                NotifyDemoActivity.this,
                0,
                new Intent[]{notifyIntent},
                PendingIntent.FLAG_UPDATE_CURRENT);

        if (Identifier.equals("ice")) {
            beaconColor = R.drawable.beacon_ice;
        }else if (Identifier.equals("blueberry")) {
            beaconColor = R.drawable.beacon_blueberry;
        }
        Notification notification = new Notification.Builder(NotifyDemoActivity.this)
                .setSmallIcon((beaconColor))
                .setContentTitle("Notify Demo")
                .setContentText(msg)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .build();

        notification.defaults |= Notification.DEFAULT_SOUND;
        notification.defaults |= Notification.DEFAULT_LIGHTS;
        notificationManager.notify(ICE_NOTIFICATION_ID, notification);
        notificationManager.notify(BLUEBERRY_NOTIFICATION_ID, notification);

        if (Identifier.equals("ice")) {
            TextView iceStatusTextView = (TextView) findViewById(R.id.ice_status);
            String iceMsg = states ? "Ice Entered region" : "Ice Exited region";
            iceStatusTextView.setText(iceMsg);
        }else if (Identifier.equals("blueberry")) {
            TextView blueberryStatusTextView = (TextView) findViewById(R.id.blueberry_status);
            String blueberryMsg = states ? "Blueberry Entered region" : "Blueberry Exited region";
            blueberryStatusTextView.setText(blueberryMsg);
        }
    }
}
