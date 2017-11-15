package edu.igti.cep;

import java.util.Random;

import org.apache.flink.annotation.PublicEvolving;
import org.apache.flink.streaming.api.functions.source.RichParallelSourceFunction;

@PublicEvolving
public class MonitoringEventSource extends RichParallelSourceFunction<MonitoringEvent> {

	private static final long serialVersionUID = 5489468186355015680L;

	private boolean running = true;
	
	private Random random;
	
	public MonitoringEventSource() {
		this.random = new Random();
	}
	
	@Override
	public void cancel() {
		running = false;
	}

	@Override
	public void run(SourceContext<MonitoringEvent> sourceContext) throws Exception {
        while (running) {
        	MonitoringEvent monitoringEvent = null;
        	
        	int rackId = 1;
        	double temperature = random.nextInt(100) + 40;

            if (temperature > 40 && temperature < 60) {
                monitoringEvent = new TemperatureHeatingEvent(rackId, temperature);
            } else if(temperature >= 60) {
                monitoringEvent = new TemperatureOverHeatingEvent(rackId, temperature);
            } else {
            	monitoringEvent = new TemperatureNormalEvent(rackId, temperature);
            }

            sourceContext.collect(monitoringEvent);

            Thread.sleep(3000);
        }
	}
}
