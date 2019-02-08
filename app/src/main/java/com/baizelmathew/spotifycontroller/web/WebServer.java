package com.baizelmathew.spotifycontroller.web;

import android.content.Context;
import android.content.res.Resources;
import android.util.Log;

import com.baizelmathew.spotifycontroller.R;
import com.baizelmathew.spotifycontroller.spotifywrapper.Player;
import com.baizelmathew.spotifycontroller.utils.DataInjector;
import com.baizelmathew.spotifycontroller.utils.FallbackErrorPage;

import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import fi.iki.elonen.NanoHTTPD;

public class WebServer extends NanoHTTPD {
    private WebSocket webSocket;
    private static boolean isIpv4 = true;
    private static final String IP_ADDRESS = getIPAddress(isIpv4);
    private static final int HTTP_PORT = 8080;
    private static final int SOCKET_PORT = 6969;
    private static String httpAddress;
    private static final String PLAY_ARROW = "play_arrow";
    private static final String pause = "pause";
    private static WebServer server = null;

    public static WebServer getInstance() {
        if (server == null) {
            return server = new WebServer();
        }
        return server;
    }

    private WebServer() {
        super(IP_ADDRESS, HTTP_PORT);
        httpAddress = "http://" + IP_ADDRESS + ":" + HTTP_PORT;
        webSocket = new WebSocket(IP_ADDRESS, SOCKET_PORT).registerCallback(new WebSocketCallback() {
            @Override
            public void onClose(org.java_websocket.WebSocket conn, int code, String reason, boolean remote) {

            }

            @Override
            public void onMessage(org.java_websocket.WebSocket conn, String message) {

            }

            @Override
            public void onError(org.java_websocket.WebSocket conn, Exception ex) {

            }
        });
        webSocket.start();
    }

    private static String getIPAddress(boolean useIPv4) {
        try {
            List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface intf : interfaces) {
                List<InetAddress> addrs = Collections.list(intf.getInetAddresses());
                for (InetAddress addr : addrs) {
                    if (!addr.isLoopbackAddress()) {
                        String sAddr = addr.getHostAddress();
                        //boolean isIPv4 = InetAddressUtils.isIPv4Address(sAddr);
                        boolean isIPv4 = sAddr.indexOf(':') < 0;
                        if (useIPv4) {
                            if (isIPv4)
                                return sAddr;
                        } else {
                            if (!isIPv4) {
                                int delim = sAddr.indexOf('%'); // drop ip6 zone suffix
                                return delim < 0 ? sAddr.toUpperCase() : sAddr.substring(0, delim).toUpperCase();
                            }
                        }
                    }
                }
            }
        } catch (Exception ignored) {
            //TODO: handle error.
        }
        return null;
    }

    public String getHttpAddress() {
        return httpAddress;
    }

    @Override
    public Response serve(IHTTPSession session) {
        HashMap<String, String> data = new HashMap<>();
        data.put("PlayIcon", PLAY_ARROW);
        data.put("SongName", "Loading..");
        data.put("SongDescription", "Loading..");
        data.put("socket", webSocket.getWsAddress());
        data.put("InitialState", Player.getInstance().getInitialPlayerState());

        String page;
        try {
            page = new DataInjector().injectData("index.html", data);
        } catch (IOException e) {
            e.printStackTrace();
            page = FallbackErrorPage.getErrorPage();

        } catch (NullPointerException n) {
            n.printStackTrace();
            page = FallbackErrorPage.getErrorPage();
        }
        return newFixedLengthResponse(page);
    }

    @Override
    public void stop() {
        super.stop();
        try {
            webSocket.stop();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
