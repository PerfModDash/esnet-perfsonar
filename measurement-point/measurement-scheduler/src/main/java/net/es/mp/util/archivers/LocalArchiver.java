package net.es.mp.util.archivers;

import java.net.URI;

import javax.ws.rs.core.UriBuilder;

import net.es.mp.authn.LocalAuthnSubject;
import net.es.mp.measurement.MPMeasurementService;
import net.es.mp.measurement.MeasurementManager;
import net.es.mp.measurement.types.Measurement;

public class LocalArchiver implements Archiver{

    public URI archive(Measurement measurement) {
        MeasurementManager mgr = MPMeasurementService.getInstance().getManager();
        try {
            mgr.createMeasurement(measurement,  "/mp/measurements/", new LocalAuthnSubject());
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
        return UriBuilder.fromUri(measurement.getURI()).build();
    }

}
