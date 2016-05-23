package com.hogent.jan.attblegateway;

import android.bluetooth.BluetoothDevice;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.hogent.jan.attblegateway.ATTBLE.IoTGateway;

import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity implements DeviceListFragment.DeviceListListener {
    private final String TAG = getClass().getSimpleName();
    private final String PREFERENCES = "ATTIOTPREFERENCES";
    private final String GATEWAYID = "GATEWAYID";

    @Bind(R.id.viewPager)
    ViewPager mViewPager;

    @Bind(R.id.tabLayout)
    TabLayout mPagerTabs;

    private IoTGateway iotGateway;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);

        setupViewPager();

        iotGateway = new IoTGateway("_YOUR_CLIENT_ID_", "_YOUR_CLIENT_KEY_", null, null);
        connectGateway();
    }

    private void connectGateway() {
        if (tryLoadConfig()) {
            new AuthenticateTask().execute();
        } else {
            final String uid = getUid();
            AsyncTask.execute(new Runnable() {
                @Override
                public void run() {
                    iotGateway.createGateway(Build.MODEL, uid);
                }
            });
            new FinishClaimTask(uid).execute();
        }
    }

    private boolean tryLoadConfig() {
        SharedPreferences preferences = getSharedPreferences(PREFERENCES, MODE_PRIVATE);
        if (preferences.contains(GATEWAYID)) {
            iotGateway.setGatewayId(preferences.getString(GATEWAYID, ""));
            return true;
        }
        return false;
    }

    private String getUid() {
        try {
            String interfaceName = "wlan0";
            List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface intf : interfaces) {
                if (!intf.getName().equalsIgnoreCase(interfaceName)) {
                    continue;
                }

                byte[] mac = intf.getHardwareAddress();
                if (mac == null) {
                    return "";
                }

                StringBuilder buf = new StringBuilder();
                for (byte aMac : mac) {
                    buf.append(String.format("%02X", aMac));
                }
                if (buf.length() > 0) {
                    buf.deleteCharAt(buf.length() - 1);
                }
                return buf.toString();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return "";
    }

    private void setupViewPager() {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());

        DeviceListFragment deviceListFragment = DeviceListFragment.newInstance();
        deviceListFragment.setDeviceClickedListener(this);
        adapter.addFragment(deviceListFragment, "Devices", "");
        mViewPager.setAdapter(adapter);

        mPagerTabs.setupWithViewPager(mViewPager);
    }

    /**
     * Adapter for the viewpager.
     */
    private static class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragments = new ArrayList<>();
        private final List<String> mFragmentTitles = new ArrayList<>();
        private final List<String> mDeviceAddresses = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        public void addFragment(Fragment fragment, String title, String address) {
            mFragments.add(fragment);
            mFragmentTitles.add(title);
            mDeviceAddresses.add(address);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragments.get(position);
        }

        public int hasTag(String deviceAddress) {
            return mDeviceAddresses.indexOf(deviceAddress);
        }

        @Override
        public int getCount() {
            return mFragments.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitles.get(position);
        }
    }


    @Override
    public void deviceClicked(BluetoothDevice device, int rssi) {
        Log.d(TAG, "deviceClicked() called with: " + "device = [" + device.getName() + "]");

        ViewPagerAdapter adapter = (ViewPagerAdapter) mViewPager.getAdapter();
        int index = adapter.hasTag(device.getAddress());

        if (index == -1) {
            DeviceDetailFragment deviceDetailFragment = DeviceDetailFragment.newInstance(device.getName(), device.getAddress(), rssi, iotGateway);
            adapter.addFragment(deviceDetailFragment, device.getName(), device.getAddress());
            adapter.notifyDataSetChanged();
            mPagerTabs.setTabsFromPagerAdapter(adapter);
            mViewPager.setCurrentItem(adapter.getCount(), true);
        } else {
            mViewPager.setCurrentItem(index + 1, true);
        }
    }

    private class FinishClaimTask extends AsyncTask<Void, Void, Boolean> {

        private String uid = "";
        private AlertDialog alert;

        public FinishClaimTask(String uid) {
            this.uid = uid;
        }

        @Override
        protected void onPreExecute() {
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);

            builder.setMessage("Please claim your gateway on https://maker.smartliving.io with code:\n" + uid)
                    .setTitle("Claim gateway");

            alert = builder.create();
            alert.setCancelable(false);
            alert.show();
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            int counter = 0;

            while (counter < 30) {
                counter++;
                if (iotGateway.finishClaim(Build.MODEL, uid)) {
                    return true;
                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if (success) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this, "Gateway claimed", Toast.LENGTH_SHORT).show();
                    }
                });
                alert.cancel();
                SharedPreferences preferences = getSharedPreferences(PREFERENCES, MODE_PRIVATE);
                SharedPreferences.Editor editor = preferences.edit();
                editor.putString(GATEWAYID, iotGateway.getGatewayId());
                editor.commit();
            } else {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this, "Failed to claim gateway in time", Toast.LENGTH_SHORT).show();
                    }
                });
                MainActivity.this.finish();
            }
        }
    }

    private class AuthenticateTask extends AsyncTask<Void, Void, Boolean> {
        private AlertDialog alert;

        @Override
        protected void onPreExecute() {
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);

            builder.setMessage("Authenticating your gateway")
                    .setTitle("Authenticating");

            alert = builder.create();
            alert.setCancelable(false);
            alert.show();
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            return iotGateway.authenticate();
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if (success) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this, "Gateway authenticated", Toast.LENGTH_SHORT).show();
                    }
                });
                alert.cancel();

                AsyncTask.execute(new Runnable() {
                    @Override
                    public void run() {
                        iotGateway.subscribe();
                    }
                });
            } else {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this, "Failed to authenticate the gateway", Toast.LENGTH_SHORT).show();
                    }
                });
                MainActivity.this.finish();
            }
        }
    }
}
