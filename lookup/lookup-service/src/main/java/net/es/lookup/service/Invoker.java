package net.es.lookup.service;

import static java.util.Arrays.asList;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;
import net.es.lookup.database.ServiceDAOMongoDb;
import net.es.lookup.common.Message;
import net.es.lookup.common.Service;
import net.es.lookup.utils.LookupServiceConfigReader;

import java.util.ArrayList;
import java.util.List;



public class Invoker {

    private static int port = 8080;
    private static LookupService lookupService = null;
    private static ServiceDAOMongoDb dao = null;
    private static String host = "localhost";
    private static LookupServiceConfigReader lcfg;
    private static String cfg="";
    private static String logConfig ="./etc/log4j.properties";

    /**
     * Main program to start the Lookup Service
     * @param args [-h, ?] for help
     *              [-p server-port
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {

        parseArgs( args );
        
        if(cfg == null || cfg.isEmpty()){
        	lcfg = LookupServiceConfigReader.getInstance();
        }else{
        	lcfg = LookupServiceConfigReader.getInstance(cfg);
        }
        
        port = lcfg.getPort();
        host = lcfg.getHost();
        
        System.setProperty("log4j.configuration", "file:" + logConfig);

        System.out.println("starting ServiceDAOMongoDb");
        Invoker.dao = new ServiceDAOMongoDb(cfg);

        System.out.println("starting Lookup Service");
        
        // Create the REST service
        Invoker.lookupService = new LookupService(Invoker.host,Invoker.port);
        // Start the service
        Invoker.lookupService.startService();
                
        // Block forever
        Object blockMe = new Object();
        synchronized (blockMe) {
            blockMe.wait();
        }
    }

    public static void parseArgs(String args[])  throws java.io.IOException {

        OptionParser parser = new OptionParser();
        parser.acceptsAll( asList( "h", "?" ), "show help then exit" );
        OptionSpec<String> PORT = parser.accepts("p", "server port").withRequiredArg().ofType(String.class);
        OptionSpec<String> HOST = parser.accepts("h", "host").withRequiredArg().ofType(String.class);
        OptionSpec<String> CONFIG = parser.accepts("c", "config").withRequiredArg().ofType(String.class);
        OptionSpec<String> LOGCONFIG = parser.accepts("l", "logConfig").withRequiredArg().ofType(String.class);
        OptionSet options = parser.parse( args );

        // check for help
        if ( options.has( "?" ) ) {
            parser.printHelpOn( System.out );
            System.exit(0);
        }
        if (options.has(PORT) ){
            port = Integer.parseInt(options.valueOf(PORT));
        }
        if (options.has(HOST) ){
            host = options.valueOf(HOST);
        }
        
        if (options.has(CONFIG) ){
            cfg = options.valueOf(CONFIG);
        }
        
        if(options.has(LOGCONFIG)){
            logConfig = (String) options.valueOf(LOGCONFIG);
        }
        
        
        
   }

}