package com.hzzx.config;
import com.hzzx.discovery.RegistryConfig;
import com.hzzx.loadbalance.Impl.RoundRobinLoadBalancer;
import com.hzzx.loadbalance.LoadBalancer;
import com.hzzx.protection.CircuitBreaker;
import com.hzzx.protection.RateLimiter;
import com.hzzx.utils.IdGenerator;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.*;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.SocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


/**
 * @author : HuangZx
 * @date : 2024/6/5 23:56
 */
@Data
@Slf4j
public class Configuration {
    //端口号
    private int port = 8101;
    //应用程序名
    private String applicationName = "default";
    //注册中心
    private RegistryConfig registryConfig = new RegistryConfig("zookeeper://127.0.0.1:2181");

    // 配置信息-->序列化协议
    private String serializeType = "jdk";

    // 配置信息-->压缩使用的协议
    private String compressType = "gzip";
    //ID生成器
    public IdGenerator ID_GENERATOR= new IdGenerator(0,1);
    //负载均衡策略
    private LoadBalancer loadBalancer = new RoundRobinLoadBalancer(this.registryConfig.getRegistry());
    // 为每一个ip配置一个限流器
    private final Map<SocketAddress, RateLimiter> everyIpRateLimiter = new ConcurrentHashMap<>(16);
    // 为每一个ip配置一个断路器，熔断
    private final Map<SocketAddress, CircuitBreaker> everyIpCircuitBreaker = new ConcurrentHashMap<>(16);

    public Configuration() {
        XmlResolver xmlResolver = new XmlResolver(this);
        xmlResolver.loadFromXml();
    }

    public static void main(String[] args) throws ParserConfigurationException, IOException, SAXException {
        DocumentBuilderFactory doc= DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder = doc.newDocumentBuilder();
        InputStream inputStream = ClassLoader.getSystemClassLoader().getResourceAsStream("HRpc.xml");
        Document document = documentBuilder.parse(inputStream);
        XPathFactory xPathFactory = XPathFactory.newInstance();
        XPath xPath = xPathFactory.newXPath();
        String ex = "/configuration/port";
        String s = parseString(xPath, document, ex);
    }

    public static String parseString(XPath xPath, Document document,String expression){
        XPathExpression xPathExpression = null;
        try {
            xPathExpression = xPath.compile(expression);
            Node targetNode =(Node)xPathExpression.evaluate(document, XPathConstants.NODE);
            return targetNode.getTextContent();
        } catch (XPathExpressionException e) {
            throw new RuntimeException(e);
        }

    }

}
