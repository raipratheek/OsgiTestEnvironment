package com.rai.mt.mqtt.server;

import java.util.Properties;

import io.moquette.server.config.ClasspathResourceLoader;
import io.moquette.server.config.IConfig;
import io.moquette.server.config.IResourceLoader;

public class MQTTConfigProperties extends IConfig {

	private Properties m_properties;
	
	private IResourceLoader resourceLoader;

	public MQTTConfigProperties() {
		m_properties = new Properties();
		resourceLoader = new ClasspathResourceLoader() ;
	}

	@Override
	public void setProperty(String name, String value) {
		m_properties.put(name, value);
	}

	@Override
	public String getProperty(String name) {
		return m_properties.getProperty(name);
	}

	@Override
	public String getProperty(String name, String defaultValue) {

		String val = m_properties.getProperty(name);
		if (val == null) {
			return defaultValue;
		}
		return val;
	}

	@Override
	public IResourceLoader getResourceLoader() {
		return resourceLoader;
	}

}
