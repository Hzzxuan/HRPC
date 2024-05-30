package com.hzzx.utils;

import com.hzzx.Constant;
import com.hzzx.exceptions.ZookeeperException;
import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.*;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * @author : HuangZx
 * @date : 2024/5/29 10:40
 */
@Slf4j
public class ZookeeperUtils {

    /**
     * 创建zookeeper实例对象，无参构造
     * @return
     */
    public static ZooKeeper createZookeeper(){
        try {
            CountDownLatch countDownLatch = new CountDownLatch(1);
            ZooKeeper zooKeeper = new ZooKeeper(Constant.DEFAULT_ZK_CONNECT,Constant.TIME_OUT, event -> {
                if (event.getState() == Watcher.Event.KeeperState.SyncConnected){
                    System.out.println("客户端连接成功");
                    countDownLatch.countDown();
                }
            });
            countDownLatch.await();
            return zooKeeper;
        } catch (IOException | InterruptedException e) {
            log.error("连接客户端创建异常，创建zookeeper实例失败：",e);
            throw new ZookeeperException();
        }
    }

    /**
     * 创建zookeeper实例对象，有参构造
     * @param connectString
     * @param timeout
     * @return
     */
    public static ZooKeeper createZookeeper(String connectString, int timeout){
        try {
            CountDownLatch countDownLatch = new CountDownLatch(1);
            ZooKeeper zooKeeper = new ZooKeeper(connectString,timeout,event -> {
                if (event.getState() == Watcher.Event.KeeperState.SyncConnected){
                    System.out.println("客户端连接成功");
                    countDownLatch.countDown();
                }
            });
            countDownLatch.await();
            return zooKeeper;
        } catch (IOException | InterruptedException e) {
            log.error("连接客户端创建异常，创建zookeeper实例失败：",e);
            throw new ZookeeperException();
        }
    }

    /**
     * 创造zookeeper节点
     * @param zooKeeper
     * @param node
     * @param watcher
     * @param createMode
     * @return
     */
    public static Boolean createZookeeperNode(ZooKeeper zooKeeper, ZookeeperNode node, Watcher watcher, CreateMode createMode){
        try {
            if(zooKeeper.exists(node.getPath(),null)==null){
                String createNode = zooKeeper.create(node.getPath(), node.getData(), ZooDefs.Ids.OPEN_ACL_UNSAFE, createMode);
                log.info("节点【{}】，成功创建。",createNode);
                return true;
            }
            else {
                if(log.isDebugEnabled()){
                    log.info("节点【{}】已经存在，无需创建。",node.getPath());
                }
                return false;
            }
        } catch (KeeperException| InterruptedException e) {
            log.error("创建基础目录时发生异常：",e);
            throw new ZookeeperException();
        }
    }

    /**
     * 判断节点是否存在，同zk自带exists
     * @param zk
     * @param node
     * @param watcher
     * @return
     */
    public static boolean exists(ZooKeeper zk,String node,Watcher watcher){
        try {
            return zk.exists(node,watcher) != null;
        } catch (KeeperException | InterruptedException e) {
            log.error("判断节点[{}]是否存在是发生异常",node,e);
            throw new ZookeeperException(e);
        }
    }

    /**
     * 关闭zookeeper
     */

    public static void close(ZooKeeper zooKeeper){
        try {
            zooKeeper.close();
        } catch (InterruptedException e) {
            log.error("关闭zookeeper时发生问题：",e);
            throw new ZookeeperException();
        }
    }

    public static List<String> getChildren(ZooKeeper zooKeeper, String servicePath,Watcher watcher) {
        try {
            List<String> children = zooKeeper.getChildren(servicePath, watcher);
            return children;
        } catch (KeeperException | InterruptedException e) {
            log.info("获取节点【{}】的子元素时发生异常！",e);
            throw new ZookeeperException();
        }

    }
}
