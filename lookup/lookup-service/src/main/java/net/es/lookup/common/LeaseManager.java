package net.es.lookup.common;

import org.joda.time.Instant;
import org.joda.time.format.ISODateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import net.es.lookup.common.ReservedKeywords;

import org.joda.time.Duration;
import org.joda.time.format.ISOPeriodFormat;
import org.joda.time.format.PeriodFormatter;


public class LeaseManager {

    private static long MAX_LEASE=2*60*60;
    private static LeaseManager instance = null;
    private DateTimeFormatter fmt = ISODateTimeFormat.dateTime();

    static {
        LeaseManager.instance = new LeaseManager();
    }

    public static LeaseManager getInstance() {
        return LeaseManager.instance;
    }

    private LeaseManager () {

    }

    public boolean requestLease (Message message) {
        Instant now = new Instant();
        // Retrieve requested TTL
        String requestedTTL = message.getTTL();
        long ttl = 0;
        if(requestedTTL != null && requestedTTL != ""){
        	PeriodFormatter fmt = ISOPeriodFormat.standard();
        	
            try{
            	Duration duration = fmt.parsePeriod(requestedTTL).toStandardDuration();
                ttl  = new Long(duration.getStandardSeconds());
            }catch(IllegalArgumentException e){
            	return false;
            }
            if (ttl ==0 || ttl > LeaseManager.MAX_LEASE ) {
                ttl = LeaseManager.MAX_LEASE;
            }
           
        }else {
        	ttl = LeaseManager.MAX_LEASE;
        }
        
        Instant expires = now.plus(ttl);
        // Add expires key/value in the message
        message.add(ReservedKeywords.RECORD_EXPIRES, this.fmt.print(expires));
        return true;
 
    }
}
