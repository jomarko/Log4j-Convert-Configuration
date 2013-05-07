package cz.muni.pb138.log4j;

import cz.muni.pb138.log4j.model.Appender;
import cz.muni.pb138.log4j.model.Configuration;
import cz.muni.pb138.log4j.model.LoggerFactory;
import cz.muni.pb138.log4j.model.Plugin;
import cz.muni.pb138.log4j.model.Renderer;
import cz.muni.pb138.log4j.model.Threshold;
import cz.muni.pb138.log4j.model.ThrowableRenderer;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.Element;



public class XmlToPropsConverter implements Converter {
    private static Logger log = Logger.getLogger(XmlToPropsConverter.class);
    private Configuration configuration;
    
    public void convert(File sourceFile, File outputFile) throws Exception {
        DtdValidator dtdValidator = new DtdValidator(sourceFile);
        
        Document document = dtdValidator.validate();
        
        if (document == null) {
            AppUtils.crash("Provided file is not valid log4j xml configuration.");
        }
        
        Element rootElement = document.getRootElement();
        String thresholdAtt = rootElement.attributeValue("threshold");
        String debugAtt = rootElement.attributeValue("debug");
        String resetAtt = rootElement.attributeValue("reset");
        
        configuration = new Configuration();
            
        if(thresholdAtt != null){
            
            if(!thresholdAtt.equalsIgnoreCase("null")){
                thresholdAtt = thresholdAtt.toLowerCase(Locale.ENGLISH);
                configuration.setThreshold(thresholdAtt);
            }
        }
        
        if(debugAtt != null){
            if(! debugAtt.equalsIgnoreCase("null")){
                debugAtt = debugAtt.toLowerCase(Locale.ENGLISH);
                configuration.setDebug(debugAtt);
            }
        }
        
        if(resetAtt != null){
            if(! resetAtt.equalsIgnoreCase("null")){
                resetAtt = resetAtt.toLowerCase(Locale.ENGLISH);
                configuration.setReset(resetAtt);
            }
        }
        
        for(Element e : (List<Element>) rootElement.elements("renderer")){
            Renderer renderer = new Renderer();
            renderer.setUpFromElement(e);
            configuration.addRenderer(renderer);
        }
        
        if(rootElement.element("throwableRenderer") != null){
            ThrowableRenderer tRenderer = new ThrowableRenderer();
            tRenderer.setUpFromElement(rootElement.element("throwableRenderer"));
            configuration.setThrowableRenderer(tRenderer);
        }
        
        for(Element e : (List<Element>) rootElement.elements("appender")){
            Appender appender = new Appender();
            appender.setUpFromElement(e);
            configuration.addAppender(appender);
        }
        
        for(Element e : (List<Element>) rootElement.elements("plugin")){
            Plugin plugin = new Plugin();
            plugin.setUpFromElement(e);
            configuration.addPlugin(plugin);
        }
        
        for(Element e : (List<Element>) rootElement.elements("logger")){
            cz.muni.pb138.log4j.model.Logger logger = new cz.muni.pb138.log4j.model.Logger();
            logger.setUpFromElement(e);
            configuration.addLogger(logger);
        }
        
        if(rootElement.element("loggerFactory") != null){
            LoggerFactory loggerFactory = new LoggerFactory();
            loggerFactory.setUpFromElement(rootElement.element("loggerFactory"));
            configuration.setLoggerFactory(loggerFactory);
        }
        
        if(rootElement.element("root") != null){
            cz.muni.pb138.log4j.model.Logger rootLogger = new cz.muni.pb138.log4j.model.Logger();
            rootLogger.setUpFromElement(rootElement.element("root"));
            configuration.addLogger(rootLogger);
        }
                
        //writing out
        PrintStream out = new PrintStream(new FileOutputStream(outputFile));
        
        Properties prop = configuration.toProperties();
        prop.list(out);
    }
}
