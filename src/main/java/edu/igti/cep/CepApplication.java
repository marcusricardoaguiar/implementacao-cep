package edu.igti.cep;

import java.util.List;
import java.util.Map;

import org.apache.flink.api.java.functions.KeySelector;
import org.apache.flink.cep.CEP;
import org.apache.flink.cep.PatternSelectFunction;
import org.apache.flink.cep.PatternStream;
import org.apache.flink.cep.pattern.Pattern;
import org.apache.flink.cep.pattern.conditions.IterativeCondition;
import org.apache.flink.cep.pattern.conditions.SimpleCondition;
import org.apache.flink.streaming.api.TimeCharacteristic;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.IngestionTimeExtractor;
import org.apache.flink.streaming.api.windowing.time.Time;

public class CepApplication {

	public static void main(String[] args) throws Exception {

		StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
		env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime);
		env.setParallelism(1);
		
		DataStream<MonitoringEvent> input = env.addSource(new MonitoringEventSource())
				.assignTimestampsAndWatermarks(new IngestionTimeExtractor<>());

		DataStream<MonitoringEvent> partitionedInput = input.keyBy(new KeySelector<MonitoringEvent, Integer>() {
			private static final long serialVersionUID = 1L;

			@Override
			public Integer getKey(MonitoringEvent value) throws Exception {
				return value.getRackID();
			}
		});

		Pattern<MonitoringEvent, ?> pattern = Pattern.<MonitoringEvent>begin("start")
			.where(new SimpleCondition<MonitoringEvent>() {
				@Override
				public boolean filter(MonitoringEvent value) throws Exception {
					if(value instanceof TemperatureHeatingEvent){
						return true;
					}
					return false;
				}
			}).next("middle").where(new SimpleCondition<MonitoringEvent>() {
				@Override
				public boolean filter(MonitoringEvent value) throws Exception {
					if(value instanceof TemperatureOverHeatingEvent){
						return true;
					}
					return false;
				}
			}).next("end").where(new IterativeCondition<MonitoringEvent>() {
			    @Override
			    public boolean filter(MonitoringEvent value, Context<MonitoringEvent> ctx) throws Exception {
			    	if(value instanceof TemperatureOverHeatingEvent){
			    		TemperatureOverHeatingEvent currentEvent = (TemperatureOverHeatingEvent)value;
			    		for (MonitoringEvent lastEvent : ctx.getEventsForPattern("middle")) {
			    			if(((TemperatureOverHeatingEvent)lastEvent).getTemperature() 
			    					<= currentEvent.getTemperature()){
			    				return true;
			    			}
			    		}
					}
					return false;
			    }
			}).within(Time.seconds(60));

		PatternStream<MonitoringEvent> patternStream = CEP.pattern(partitionedInput, pattern);

		DataStream<TemperatureAlert> alerts = patternStream
			.select(new PatternSelectFunction<MonitoringEvent, TemperatureAlert>() {
				@Override
				public TemperatureAlert select(Map<String, List<MonitoringEvent>> pattern) throws Exception {
					TemperatureHeatingEvent event1 = (TemperatureHeatingEvent) pattern.get("start").get(0);
					TemperatureOverHeatingEvent event2 = (TemperatureOverHeatingEvent) pattern.get("middle").get(0);
					TemperatureOverHeatingEvent event3 = (TemperatureOverHeatingEvent) pattern.get("end").get(0);
					return new TemperatureAlert(event1.getRackID(), event1.getTemperature(), event2.getTemperature(), 
							event3.getTemperature(), event1.getTime(), event2.getTime(), event3.getTime());
				}
			});
		alerts.print();
		
		env.execute("CEP monitoring job");
	}
}