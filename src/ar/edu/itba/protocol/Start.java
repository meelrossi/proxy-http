package ar.edu.itba.protocol;

import ar.edu.itba.protocol.configurator.ConfiguratorImplementation;
import ar.edu.itba.protocol.metrics.MetricsServerSelector;

public class Start {
	public static void main(String[] args) {
		ConfiguratorImplementation conf = new ConfiguratorImplementation(9090);
		MetricsServerSelector metrics = new MetricsServerSelector(9091);
		
		Thread metricsT = new Thread(metrics);
		Thread configT = new Thread(conf);
		
		metricsT.start();
		configT.start();
		while(true) {
			
		}
	}
}
