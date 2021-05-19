package com.syusuke.mqtt.ui;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Process;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.jeremyliao.liveeventbus.LiveEventBus;
import com.syusuke.mqtt.R;
import com.syusuke.mqtt.bean.ValueBean;
import com.syusuke.mqtt.config.BundleKey;
import com.syusuke.mqtt.config.GlobalConfig;
import com.syusuke.mqtt.config.LiveEventBusConfig;
import com.syusuke.mqtt.config.MQTTServer;
import com.syusuke.mqtt.config.ServiceAction;
import com.syusuke.mqtt.databinding.ActivityMainBinding;
import com.syusuke.mqtt.receiver.WifiStateChangedReceiver;
import com.syusuke.mqtt.service.MQTTService;
import com.syusuke.mqtt.util.WifiUtils;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    public static final String TAG = "MQTTDemo";
    private int point = 40;
    private MyHandler handler = new MyHandler(this);
    private WifiStateChangedReceiver receiver;
    private ActivityMainBinding binding;
    private MyAdapter adapter;
    private List<String> infoList = new ArrayList<>();
    private String host;
    private int port;
    private String clientId;
    private List<String> clientIds = new ArrayList<>();
    private String user;
    private List<String> users = new ArrayList<>();
    private int repeatCount = 0;
    private SparseArray<Integer> repeatCounts = new SparseArray<>();
    //    private Timer timer;
    private SparseArray<Timer> timers = new SparseArray<>();
    private boolean positiveDisconnect = false;
    private Timer timer;

    private static class MyHandler extends Handler {
        WeakReference<MainActivity> weakReference;

        public MyHandler(MainActivity mainActivity) {
            weakReference = new WeakReference<>(mainActivity);
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        View rootView = binding.getRoot();
        setContentView(rootView);
        initEventBus();
        initView();
        startCheckWifi();
        registerWifiReceiver();
    }

    private void registerWifiReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        filter.addAction(WifiManager.RSSI_CHANGED_ACTION);
        receiver = new WifiStateChangedReceiver();
        registerReceiver(receiver, filter);
    }

    private void startCheckWifi() {
        handler.post(checkWifiRunnable);
    }

    private void stopCheckWifi() {
        if (checkWifiRunnable != null && handler != null) {
            handler.removeCallbacks(checkWifiRunnable);
        }
    }

    private Runnable checkWifiRunnable = new Runnable() {
        @Override
        public void run() {
            String s = WifiUtils.checkWifiState(MainActivity.this);
            LiveEventBus.get(LiveEventBusConfig.WIFI).post(s);
            handler.postDelayed(this, 100);
        }
    };

    private void createClientId() {
        clientId = UUID.randomUUID().toString();
        binding.etClientId.setText(clientId);
        user = clientId.substring(0, 8);
    }

    private void resetClientIds() {
        point = Integer.parseInt(binding.etClientPoint.getText().toString());

        clientIds.clear();
        users.clear();
        for (int i = 0; i < point; i++) {
            createClientId();
            clientIds.add(clientId);
            users.add(user);
        }

    }

    private void initView() {
        LinearLayoutManager llm = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        binding.rvShow.setLayoutManager(llm);

        adapter = new MyAdapter();
        binding.rvShow.setAdapter(adapter);

        resetClientIds();

        binding.btnConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                positiveDisconnect = false;
                host = binding.etHost.getText().toString();
                port = Integer.parseInt(binding.etPort.getText().toString());
                Bundle bundle = new Bundle();
                bundle.putString(BundleKey.ACTION, ServiceAction.LOGIN);
                bundle.putString(BundleKey.SERVER, host);
                bundle.putInt(BundleKey.PORT, port);
                String[] a1 = new String[clientIds.size()];
                clientIds.toArray(a1);
                bundle.putStringArray(BundleKey.CLIENT_IDS, a1);
                String[] a2 = new String[users.size()];
                users.toArray(a2);
                bundle.putStringArray(BundleKey.USERS, a2);
                start(MainActivity.this, bundle);
            }
        });
        binding.btnSubscribe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                bundle.putString(BundleKey.ACTION, ServiceAction.SUBSCRIBE);
                bundle.putString(BundleKey.TOPIC, binding.etSubscribe.getText().toString());
                bundle.putInt(BundleKey.QOS, MQTTServer.DEFUALT_QOS);
                start(MainActivity.this, bundle);
            }
        });
        binding.btnPublish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String toString = binding.etPublishRepeat.getText().toString();
                final int countLimit = Integer.parseInt(toString);
                repeatCount = 0;
                repeatCounts.clear();
                for (int i = 0; i < clientIds.size(); i++) {
                    repeatCounts.put(i, 0);
                }
                for (int i = 0; i < timers.size(); i++) {
                    timers.get(i).cancel();
                    timers.get(i).purge();
                    timers.get(i).cancel();
                }
                Log.d("rain", "main当前线程:" + Process.myTid());
                for (int i = 0; i < clientIds.size(); i++) {
                    final Timer timer = new Timer();
                    timers.put(i, timer);
                    final int finalI = i;
                    TimerTask timerTask = new TimerTask() {
                        @Override
                        public void run() {
                            if (repeatCounts.get(finalI) >= countLimit) {
                                boolean end = true;
                                for (int k = 0; k < repeatCounts.size(); k++) {
                                    if (repeatCounts.get(k) < countLimit) {
                                        end = false;
                                        break;
                                    }
                                }
                                if (end) {
                                    Log.d(TAG, "全部发送完毕");
                                    repeatCount = 0;
                                    timer.cancel();
                                }
                            } else {
                                Integer count = repeatCounts.get(finalI);
                                count++;
                                repeatCounts.put(finalI, count);
                                Bundle bundle = new Bundle();
                                bundle.putString(BundleKey.ACTION, ServiceAction.PUBLISH);
                                bundle.putString(BundleKey.TOPIC, binding.etPublishTopic.getText().toString());
                                bundle.putString(BundleKey.MESSAGE, binding.etPublishMessage.getText().toString());
                                bundle.putInt(BundleKey.INDEX, finalI);
                                start(MainActivity.this, bundle);
                            }
                        }
                    };
                    timer.schedule(timerTask, 0, 1000);
                }
            }
        });
        binding.btnCancelPublish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                repeatCount = 0;
                for (int i = 0; i < timers.size(); i++) {
                    timers.get(i).cancel();
                    timers.get(i).purge();
                    timers.get(i).cancel();
                }
            }
        });
        binding.btnRegenClientId.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clientIds.clear();
                users.clear();
                for (int i = 0; i < point; i++) {
                    createClientId();
                    clientIds.add(clientId);
                    users.add(user);
                }
            }
        });
        binding.btnDisconnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                positiveDisconnect = true;
                for (int i = 0; i < timers.size(); i++) {
                    timers.get(i).cancel();
                    timers.get(i).purge();
                    timers.get(i).cancel();
                }
                stopService();
            }
        });
        binding.btnConfirmPoint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binding.btnDisconnect.performClick();
                resetClientIds();
            }
        });
        binding.btnClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (infoList != null) {
                    infoList.clear();
                    adapter.notifyDataSetChanged();
                }
            }
        });
        binding.rgPing.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (binding.rbNotPing.getId() == checkedId) {
                    GlobalConfig.isPing = false;
                    Toast.makeText(MainActivity.this, "不PING", Toast.LENGTH_SHORT).show();
                } else {
                    GlobalConfig.isPing = true;
                    Toast.makeText(MainActivity.this, "PING", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    /**
     * 启动服务
     *
     * @param context
     * @param bundle  封装好的Bundle对象
     */
    private static void start(Context context, Bundle bundle) {
        Intent intent = new Intent(context, MQTTService.class);
        intent.putExtras(bundle);
        context.startService(intent);
    }

    private void stopService() {
        Bundle bundle = new Bundle();
        bundle.putString(BundleKey.ACTION, ServiceAction.STOP_SERVICE);
        start(this, bundle);
    }

    private void connect2(int index) {
        Bundle bundle = new Bundle();
        bundle.putString(BundleKey.ACTION, ServiceAction.CONNECT);
        bundle.putString(BundleKey.SERVER, host);
        bundle.putInt(BundleKey.PORT, port);
        bundle.putInt(BundleKey.INDEX, index);
        bundle.putString(BundleKey.CLIENT_ID, clientIds.get(index));
        bundle.putString(BundleKey.USER, users.get(index));
        Intent intent = new Intent(this, MQTTService.class);
        intent.putExtras(bundle);
        startService(intent);
    }

    private void initEventBus() {
        LiveEventBus.get(LiveEventBusConfig.MQTT, ValueBean.class).observe(this, new Observer<ValueBean>() {
            @Override
            public void onChanged(ValueBean valueBean) {
                String action = valueBean.getAction();
                boolean success = valueBean.isSuccess();
                final Bundle bundle = valueBean.getBundle();
                String info = "LiveEventBus Action: " + action + ", success: " + success + ", bundle=" + bundle.toString();
//                addInfo(info);
                switch (action) {
                    case ServiceAction.CONNECT:
                        if (success) {
                            Bundle bundle1 = new Bundle();
                            bundle1.putString(BundleKey.ACTION, ServiceAction.SUBSCRIBE);
                            bundle1.putString(BundleKey.TOPIC, binding.etSubscribe.getText().toString());
                            bundle1.putInt(BundleKey.QOS, MQTTServer.DEFUALT_QOS);
                            bundle1.putInt(BundleKey.INDEX, bundle.getInt(BundleKey.INDEX));
                            start(MainActivity.this, bundle1);
                            if (timer != null) {
                                timer.cancel();
                            }
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    addInfo("客户端" + bundle.getInt(BundleKey.INDEX) + "已连接");
                                }
                            });
                        } else {
                            timer = new Timer();
                            TimerTask timerTask = new TimerTask() {
                                @Override
                                public void run() {
                                    connect2(bundle.getInt(BundleKey.INDEX));
                                    Log.d(TAG, "timer 重连");
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            addInfo("客户端" + bundle.getInt(BundleKey.INDEX) + "正在重连");

                                        }
                                    });
                                }
                            };
                            timer.schedule(timerTask, 3000);
                        }
                        break;
                    case ServiceAction.DISCONNECT:
                        break;
                    case ServiceAction.SUBSCRIBE:
                        break;
                    case ServiceAction.PUBLISH:
                        String string = bundle.getString(BundleKey.MESSAGE, "");
                        int index = bundle.getInt(BundleKey.INDEX);
//                        addInfo(String.format(Locale.getDefault(), "客户端%1$d第%2$d次发送", index, repeatCounts.get(index)));
//                        addInfo(string);
                        break;
                    case ServiceAction.UNSUBSCRIBE:
                        break;
                    default:
                        break;
                }
            }
        });
        LiveEventBus.get(LiveEventBusConfig.MQTT_MESSAGE, String.class).observe(this, new Observer<String>() {
            @Override
            public void onChanged(String s) {
                Log.d(TAG, "LiveEventBus Message: " + s);
                addInfo(s);
            }
        });
        LiveEventBus.get(LiveEventBusConfig.WIFI, String.class).observe(this, new Observer<String>() {
            @Override
            public void onChanged(String s) {
//                Log.d(TAG, "WIFI信号强度: " + s);
                binding.tvWifiStatus.setText(s);
            }
        });
        LiveEventBus.get(LiveEventBusConfig.WIFI_STATE, String.class).observe(this, new Observer<String>() {
            @Override
            public void onChanged(String s) {
                Log.d(TAG, "广播: " + s);
                addInfo(s);
            }
        });
        LiveEventBus.get(LiveEventBusConfig.MQTT_CONNECT_LOST, Integer.class).observe(this, new Observer<Integer>() {
            @Override
            public void onChanged(Integer s) {
                if (!positiveDisconnect) {
                    Bundle bundle = new Bundle();
                    bundle.putString(BundleKey.ACTION, ServiceAction.RELOGIN);
                    bundle.putInt(BundleKey.INDEX, s);
                    start(MainActivity.this, bundle);
                    addInfo("客户端" + s + "已断开");
                }
            }
        });
        LiveEventBus.get(LiveEventBusConfig.PING, String.class).observe(this, new Observer<String>() {
            @Override
            public void onChanged(String s) {
                addInfo(s);
            }
        });
    }

    private void addInfo(String info) {
        infoList.add(info);
        infoList.add("--------------------------------------");
        binding.rvShow.scrollToPosition(infoList.isEmpty() ? 0 : infoList.size() - 1);
        adapter.notifyDataSetChanged();
    }

    private class MyAdapter extends RecyclerView.Adapter<MyAdapter.VH> {
        @NonNull
        @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(MainActivity.this).inflate(R.layout.item_show, parent, false);
            return new VH(view);
        }

        @Override
        public void onBindViewHolder(@NonNull VH holder, int position) {
            holder.tv.setText(infoList.get(position));
        }

        @Override
        public int getItemCount() {
            return infoList.size();
        }

        public class VH extends RecyclerView.ViewHolder {
            private TextView tv;

            public VH(@NonNull View itemView) {
                super(itemView);
                tv = itemView.findViewById(R.id.tv_show);
            }
        }
    }

    @Override
    protected void onDestroy() {
        stopCheckWifi();
        unregisterReceiver(receiver);
        for (int i = 0; i < timers.size(); i++) {
            timers.get(i).cancel();
            timers.get(i).purge();
            timers.get(i).cancel();
        }
        stopService();
        super.onDestroy();
    }
}