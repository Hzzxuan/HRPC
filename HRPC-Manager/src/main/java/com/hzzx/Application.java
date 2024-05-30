package com.hzzx;

import com.hzzx.utils.ZookeeperNode;
import com.hzzx.utils.ZookeeperUtils;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooKeeper;
import java.util.List;
import java.util.concurrent.CountDownLatch;


/**注册中心管理页面
 * @author : HuangZx
 * @date : 2024/5/28 22:30
 */
public class Application {
    public static void main(String[] args) {
        //在zookeeper中创建基础目录
        // yrpc-metadata   (持久节点)
        //  └─ providers （持久节点）
        //  		└─ service1  （持久节点，接口的全限定名）
        //  		    ├─ node1 [data]     /ip:port
        //  		    ├─ node2 [data]
        //            └─ node3 [data]
        //  └─ consumers
        //        └─ service1
        //             ├─ node1 [data]
        //             ├─ node2 [data]
        //             └─ node3 [data]
        //  └─ config
        ZooKeeper zooKeeper;
        //连接参数
        String connectString = Constant.DEFAULT_ZK_CONNECT;
        int timeout = Constant.TIME_OUT;
        //创建实例对象
        zooKeeper = ZookeeperUtils.createZookeeper(connectString,timeout);
        //定义节点
        String basePath = "/HRpc-meta";
        String providerPath = basePath +"/provider";
        String consumerPath = basePath +"/consumer";
        ZookeeperNode baseNode = new ZookeeperNode(basePath, null);
        ZookeeperNode providersNode = new ZookeeperNode(providerPath, null);
        ZookeeperNode consumersNode = new ZookeeperNode(consumerPath, null);
        // 创建节点
        List.of(baseNode, providersNode, consumersNode).forEach(node -> {
            ZookeeperUtils.createZookeeperNode(zooKeeper,node,null, CreateMode.PERSISTENT);
        });
        // 关闭连接
        ZookeeperUtils.close(zooKeeper);

    }
}
