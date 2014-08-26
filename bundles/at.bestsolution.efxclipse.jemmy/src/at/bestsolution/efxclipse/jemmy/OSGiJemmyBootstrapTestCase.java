/*******************************************************************************
 * Copyright (c) 2012 BestSolution.at and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tom Schindl<tom.schindl@bestsolution.at> - initial API and implementation
 *******************************************************************************/
package at.bestsolution.efxclipse.jemmy;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.RegistryFactory;
import org.eclipse.equinox.app.IApplicationContext;
import org.jemmy.input.AWTRobotInputFactory;
import org.junit.BeforeClass;
import org.osgi.framework.ServiceReference;
import org.osgi.service.application.ApplicationDescriptor;
import org.osgi.service.application.ApplicationException;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;

import at.bestsolution.efxclipse.jemmy.internal.Activator;

public class OSGiJemmyBootstrapTestCase {

	private static final String osName = System.getProperty("os.name").toLowerCase();;

	static {
		if (osName.contains("mac os")) {
			AWTRobotInputFactory.runInOtherJVM(true);
		}
	}

	@BeforeClass
	public static void setUp() {
		String tmp = System.getProperty("osgi.jemmyapp.id");

		final List<String> brandingArgs = new ArrayList<String>();
		if (System.getProperty("test.jemmy.product") != null) {
			IConfigurationElement[] elements = RegistryFactory.getRegistry().getConfigurationElementsFor("org.eclipse.core.runtime", "products", System.getProperty("test.jemmy.product"));
			if (elements != null && elements.length > 0) {
				for (IConfigurationElement prop : elements[0].getChildren("property")) {
					brandingArgs.add("-" + prop.getAttribute("name"));
					brandingArgs.add(prop.getAttribute("value"));
				}
				tmp = elements[0].getAttribute("application");
			}
		}
		
		final String applicationId = tmp;

		try {
			Collection<ServiceReference<ApplicationDescriptor>> col = Activator.getContext().getServiceReferences(ApplicationDescriptor.class, "(service.pid=" + applicationId + ")");
			final AtomicBoolean launched = new AtomicBoolean(true);
			if (col.isEmpty()) {
				System.err.println("There's no application with ID '" + applicationId + "' known.");
				throw new IllegalStateException("There's no application with ID '" + applicationId + "' known.");
			} else if (col.size() > 1) {
				System.err.println("There are more than 1 application with ID '" + applicationId + "' known.");
				throw new IllegalStateException("There are more than 1 application with ID '" + applicationId + "' known.");
			} else {
				EventHandler handler = new EventHandler() {
					public void handleEvent(final Event event) {
						launched.set(false);
					}
				};

				Dictionary<String, String> properties = new Hashtable<String, String>();
				properties.put(EventConstants.EVENT_TOPIC, "org/eclipse/fx/E4Application/Launched");
				Activator.getContext().registerService(EventHandler.class, handler, properties);
				
				ServiceReference<ApplicationDescriptor> ref = col.iterator().next();
				final ApplicationDescriptor desc = Activator.getContext().getService(ref);
				new Thread() {
					public void run() {
						try {
							Map<String, Object> test = new HashMap<String, Object>();
							if (!brandingArgs.isEmpty()) {
								test.put(IApplicationContext.APPLICATION_ARGS, brandingArgs.toArray(new String[0]));
							}

							desc.launch(test);
						} catch (ApplicationException e) {
							System.err.println("Failed to launch application '" + applicationId + "'");
							e.printStackTrace();
						}
					}
				}.start();

				while (launched.get()) {
					Thread.sleep(1000);
				}

				Thread.sleep(1000);
			}
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}
	}
}
