package com.test.commons.util;

import java.net.*;
import java.util.*;

public class NetworkUtil {
	private NetworkUtil() {}
    
    /**
     * 取本機所有 IP 位址.
     * @return
     * @throws SocketException 
     */
    public static String[] getLocalHostIPAdresses() throws SocketException {
        List<String> ips = new ArrayList<String>();
        Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces();
        while (en.hasMoreElements()) {
            NetworkInterface i = en.nextElement();
            for(Enumeration<InetAddress> en2 = i.getInetAddresses(); en2.hasMoreElements(); )
                ips.add(en2.nextElement().getHostAddress());
        }
        return ips.toArray(new String[ips.size()]);
    }
    
    /**
     * 取本機之第一個非 loop-back 的 IP(IPv4) 位址.
     * @return
     * @throws SocketException 
     */
    public static String getLocalHostIPAdress() throws SocketException {
        Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces();
        while (en.hasMoreElements()) {
            NetworkInterface i = en.nextElement();
            for(Enumeration<InetAddress> en2 = i.getInetAddresses(); en2.hasMoreElements(); ) {
                InetAddress addr = en2.nextElement();
                if(!addr.isLoopbackAddress()) {
                    if(addr instanceof Inet4Address)
                        return addr.getHostAddress();
                }
            }
        }
        return null;
    }
    
    /**
     * 取本機之第一個非 loop-back 的 IP(IPv6) 位址.
     * @return
     * @throws SocketException 
     */
    public static String getLocalHostIPv6Adress() throws SocketException {
        Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces();
        while (en.hasMoreElements()) {
            NetworkInterface i = en.nextElement();
            for(Enumeration<InetAddress> en2 = i.getInetAddresses(); en2.hasMoreElements(); ) {
                InetAddress addr = en2.nextElement();
                if(!addr.isLoopbackAddress()) {
                    if(addr instanceof Inet6Address)
                        return addr.getHostAddress();
                }
            }
        }
        return null;
    }
}
