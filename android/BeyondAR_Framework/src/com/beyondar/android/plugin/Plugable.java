/*
 * Copyright (C) 2013 BeyondAR
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.beyondar.android.plugin;

import java.util.List;

public interface Plugable<T extends Plugin> {

	public static int DEFAULT_PLUGINS_CAPACITY = 3;
	/**
	 * Add a new plug-in. This plug-in's will be initialize ones is added
	 * 
	 * @param plugin
	 */
	public void addPlugin(T plugin);

	/**
	 * Remove an specific plug-in.
	 * 
	 * @param pluginClass
	 * @return true if it has been removed
	 */
	public boolean removePlugin(T pluginClass);

	/**
	 * Clean all the plug-in's
	 */
	public void removeAllPlugins();

	public T getFirstPlugin(Class<? extends T> pluginClass);

	/**
	 * Test whether any plug-in class exist.
	 * 
	 * @param pluginClass
	 * @return true if there is any plug-in, false otherwise
	 */
	public boolean containsAnyPlugin(Class<? extends T> pluginClass);

	/**
	 * Test whether the plug-in exist
	 * 
	 * @param plugin
	 * @return true if there is any plug-in, false otherwise
	 */
	public boolean containsPlugin(T plugin);

	/**
	 * Get all plug-in's which implemented a specific class.
	 * 
	 * @param pluginClass
	 *            The specific class that we want to retrieve
	 * 
	 * @param result
	 *            A list where all the plugins will be stored
	 * @return
	 */
	public List<T> getAllPlugins(Class<? extends T> pluginClass, List<T> result);

	/**
	 * Get all plug-in's which implement an specific class
	 * 
	 * @param pluginClass
	 *            The specific class that we want to retrieve
	 * @return
	 */
	public List<T> getAllPugins(Class<? extends T> pluginClass);

	/**
	 * Get a {@link List} copy of the added plug-in's. Adding/removing plug-in's to
	 * this list will not affect the added plug-in's
	 * 
	 * @return
	 */
	public List<T> getAllPlugins();

}
