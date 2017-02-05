package de.rbb.battleship.util;

import java.util.Locale;

import de.prodv.framework.configuration.ResourceBundleService;
import de.prodv.framework.configuration.ResourceService;

public class ServiceLocator {

	private static ServiceLocator instance;

	private final ResourceService resourceService;

	public static ServiceLocator getInstance() {
		if (instance == null) {
			instance = new ServiceLocator();
		}
		return instance;
	}

	private ServiceLocator() {
		ResourceBundleService bundleService = new ResourceBundleService();
		bundleService.setResourceBaseName("resources");
		bundleService.setDefaultLocale(new Locale("de"));
		this.resourceService = bundleService;
	}

	public static ResourceService getResourceService() {
		return ServiceLocator.getInstance().resourceService;
	}
}
