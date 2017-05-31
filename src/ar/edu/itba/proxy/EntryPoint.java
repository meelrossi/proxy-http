
	package ar.edu.itba.proxy;

	import ar.edu.itba.protocol.configurator.ConfiguratorImplementation;
	import ar.edu.itba.protocol.metrics.MetricsServerSelector;
	import ar.edu.itba.proxy.engine.Engine;

	/*
	**	Punto de entrada principal de la aplicacion
	*/

	public final class EntryPoint {

		private static final int CONFIGURATIONS_PORT = Integer.parseInt(PropertiesManager.getProperty("configPort"));
		private static final int METRICS_PORT = Integer.parseInt(PropertiesManager.getProperty("metricsPort"));

		public static void main(String [] arguments) {

			ConfiguratorImplementation configurator = new ConfiguratorImplementation(CONFIGURATIONS_PORT);
			MetricsServerSelector metricsServer = new MetricsServerSelector(METRICS_PORT);

			// Inicializamos los servicios de metricas
			// y de configuracion remota
			new Thread(metricsServer).start();
			new Thread(configurator).start();

			try {

				// Inicializamos el servidor proxy
				new Engine().start();
			}
			catch (Exception exception) {
				Engine.log.error("No se pudo iniciar el servidor proxy");
			}
		}
	}
