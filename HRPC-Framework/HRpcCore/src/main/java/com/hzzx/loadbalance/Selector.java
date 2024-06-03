package com.hzzx.loadbalance;

import java.net.InetSocketAddress;
import java.util.List;

/**
 * @author : HuangZx
 * @date : 2024/6/2 22:34
 */
public interface Selector {
    InetSocketAddress getNext();
    void renewBalance();
}
