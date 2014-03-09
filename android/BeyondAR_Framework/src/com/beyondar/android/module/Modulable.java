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
package com.beyondar.android.module;

import java.util.List;

public interface Modulable<T extends Module> {

	/**
	 * Add a new module. This modules will be initialize ones is added
	 * 
	 * @param module
	 */
	public void addModule(T module);

	/**
	 * Remove an specific module.
	 * 
	 * @param module
	 * @return true if it has been removed
	 */
	public boolean removeModule(T module);

	/**
	 * Clean all the modules
	 */
	public void cleanModules();

	public T getFirstModule(Class<? extends T> moduleClass);

	/**
	 * Test whether any class module exist
	 * 
	 * @param moduleClass
	 * @return true if there is any module, false otherwise
	 */
	public boolean containsAnyModule(Class<? extends T> moduleClass);

	/**
	 * Test whether the module exist
	 * 
	 * @param module
	 * @return true if there is any module, false otherwise
	 */
	public boolean containsModule(T module);

	/**
	 * Get all modules which implement an specific class
	 * 
	 * @param moduleClass
	 *            The specific class that we want to retrieve
	 * 
	 * @param result
	 *            A list where all the modules will be stored
	 * @return
	 */
	public List<T> getAllModules(Class<? extends T> moduleClass, List<T> result);

	/**
	 * Get all modules which implement an specific class
	 * 
	 * @param moduleClass
	 *            The specific class that we want to retrieve
	 * @return
	 */
	public List<T> getAllModules(Class<? extends T> moduleClass);

	/**
	 * Get a {@link List} copy of the added modules. Adding/removing modules to
	 * this list will not affect the added modules
	 * 
	 * @return
	 */
	public List<T> getAllModules();

}
