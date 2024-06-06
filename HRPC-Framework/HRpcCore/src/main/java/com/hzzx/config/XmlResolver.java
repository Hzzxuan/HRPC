package com.hzzx.config;

import com.hzzx.discovery.RegistryConfig;
import com.hzzx.loadbalance.LoadBalancer;
import com.hzzx.utils.IdGenerator;
import lombok.Data;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.*;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;

/**
 * @author : HuangZx
 * @date : 2024/6/6 15:17
 */
public class XmlResolver {
    private Configuration configuration;
    public XmlResolver(Configuration configuration){
        this.configuration = configuration;
    }
    public void loadFromXml() {
        DocumentBuilderFactory doc= DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder documentBuilder = doc.newDocumentBuilder();
            InputStream inputStream = ClassLoader.getSystemClassLoader().getResourceAsStream("HRpc.xml");
            Document document = documentBuilder.parse(inputStream);
            XPathFactory xPathFactory = XPathFactory.newInstance();
            XPath xPath = xPathFactory.newXPath();
            configuration.setPort(resolvePort(xPath,document));
            configuration.setApplicationName(resolveAppName(xPath,document));
            configuration.setRegistryConfig(resolveRegistry(xPath,document));
            configuration.setSerializeType(resolveSerializer(xPath,document));
            configuration.setCompressType(resolveCompressor(xPath,document));
            //configuration.setLoadBalancer(resolveLoadBalancer(xPath,document));
            configuration.setID_GENERATOR(resolveIdGenerator(xPath,document));

        } catch (ParserConfigurationException | SAXException | IOException e) {
            throw new RuntimeException(e);
        }

    }




    private int resolvePort(XPath xPath, Document document) {
        String expression = "/configuration/port";
        return Integer.parseInt(parseString(xPath,document,expression));
    }

    private String resolveAppName(XPath xPath, Document document) {
        String expression = "/configuration/appName";
        return parseString(xPath,document,expression);
    }
    private RegistryConfig resolveRegistry(XPath xPath, Document document) {
        String expression = "/configuration/registry";
        String url = parseAttribute(xPath, document, expression, "url");
        return new RegistryConfig(url);
    }
    private String resolveSerializer(XPath xPath, Document document) {
        String expression = "/configuration/serializeType";
        String s = parseAttribute(xPath, document, expression, "type");
        return s;
    }

    private String resolveCompressor(XPath xPath, Document document) {
        String expression = "/configuration/compressType";
        String s = parseAttribute(xPath, document, expression, "type");
        return s;
    }
    private LoadBalancer resolveLoadBalancer(XPath xPath, Document document) {
        String expression = "/configuration/loadBalancer";
        return parseObject(xPath,document,expression);
    }

    private IdGenerator resolveIdGenerator(XPath xPath, Document document) {
        String expression = "/configuration/idGenerator";
        String aClass = parseAttribute(xPath, document, expression, "class");
        String dataCenterId = parseAttribute(xPath, document, expression, "dataCenterId");
        String machineId = parseAttribute(xPath, document, expression, "MachineId");
        Class<?> clazz = null;
        try {
            clazz = Class.forName(aClass);
            Object o = clazz.getConstructor(new Class[]{long.class, long.class}).newInstance(Long.parseLong(dataCenterId), Long.parseLong(machineId));
            return (IdGenerator) o;
        } catch (ClassNotFoundException | NoSuchMethodException | InstantiationException | IllegalAccessException |
                 InvocationTargetException e) {
            throw new RuntimeException(e);
        }


    }
    private<T> T parseObject(XPath xPath, Document document,String expression,Class<?>[] paramType,Object... args){
        XPathExpression xPathExpression = null;
        try {
            xPathExpression = xPath.compile(expression);
            Node targetNode =(Node)xPathExpression.evaluate(document, XPathConstants.NODE);
            Node className = targetNode.getAttributes().getNamedItem("class");
            String classNameString = className.getTextContent();
            Class<?> aClass = Class.forName(classNameString);
            Object object = aClass.getConstructor(paramType).newInstance(args);
            return (T)object;
        } catch (XPathExpressionException | ClassNotFoundException | NoSuchMethodException | InstantiationException |
                 IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }

    }
    private<T> T parseObject(XPath xPath, Document document,String expression){
        XPathExpression xPathExpression = null;
        try {
            xPathExpression = xPath.compile(expression);
            Node targetNode =(Node)xPathExpression.evaluate(document, XPathConstants.NODE);
            Node className = targetNode.getAttributes().getNamedItem("class");
            String classNameString = className.getTextContent();
            Class<?> aClass = Class.forName(classNameString);
            Object object = aClass.getConstructor().newInstance();
            return (T)object;
        } catch (XPathExpressionException | ClassNotFoundException | NoSuchMethodException | InstantiationException |
                 IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }

    }

    private String parseString(XPath xPath, Document document,String expression){
        XPathExpression xPathExpression = null;
        try {
            xPathExpression = xPath.compile(expression);
            Node targetNode =(Node)xPathExpression.evaluate(document, XPathConstants.NODE);
            return targetNode.getTextContent();
        } catch (XPathExpressionException e) {
            throw new RuntimeException(e);
        }

    }
    private String parseAttribute(XPath xPath, Document document,String expression,String attribute){
        XPathExpression xPathExpression = null;
        try {
            xPathExpression = xPath.compile(expression);
            Node targetNode =(Node)xPathExpression.evaluate(document, XPathConstants.NODE);
            Node attributeName = targetNode.getAttributes().getNamedItem(attribute);
            return attributeName.getTextContent();
        } catch (XPathExpressionException  e) {
            throw new RuntimeException(e);
        }

    }
}
