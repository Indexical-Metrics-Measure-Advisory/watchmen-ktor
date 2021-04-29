package com.imma.plugin

import org.slf4j.LoggerFactory
import org.springframework.core.annotation.AnnotationAwareOrderComparator
import org.springframework.core.io.UrlResource
import org.springframework.core.io.support.PropertiesLoaderUtils
import org.springframework.util.ClassUtils
import org.springframework.util.ReflectionUtils
import org.springframework.util.StringUtils
import java.io.IOException

interface PluginInitializer {
	fun register()
}

class PluginLoader {
	companion object {
		/**
		 * The location to look for plugins.
		 *
		 * Can be present in multiple JAR files.
		 */
		private const val PLUGINS_RESOURCE_LOCATION = "META-INF/plugin.factories"
		private val logger = LoggerFactory.getLogger(PluginLoader::class.java)

		private fun loadPluginNames(classLoader: ClassLoader): List<String> {
			return loadSpringFactories(classLoader).getOrDefault(PluginInitializer::class.java.name, mutableListOf())
		}

		private fun loadSpringFactories(classLoader: ClassLoader): MutableMap<String, MutableList<String>> {
			val result = mutableMapOf<String, MutableList<String>>()
			try {
				classLoader.getResources(PLUGINS_RESOURCE_LOCATION).toList().forEach { url ->
					val resource = UrlResource(url)
					val properties = PropertiesLoaderUtils.loadProperties(resource)

					properties.forEach { key, value ->
						val pluginTypeName = (key as String).trim()
						StringUtils.commaDelimitedListToStringArray(value as String).forEach {
							val implementations = result.computeIfAbsent(pluginTypeName) { mutableListOf() }
							implementations += it
						}
					}
				}
			} catch (ex: IOException) {
				throw IllegalArgumentException("Unable to load plugins from location [$PLUGINS_RESOURCE_LOCATION]", ex)
			}
			return result
		}

		private fun instantiatePlugin(pluginImplementationName: String, classLoader: ClassLoader): PluginInitializer {
			return try {
				val factoryImplementationClass = ClassUtils.forName(pluginImplementationName, classLoader)
				require(PluginInitializer::class.java.isAssignableFrom(factoryImplementationClass)) {
					"Class [$pluginImplementationName] is not assignable to plugin type [${PluginInitializer::class.java.name}]"
				}
				@Suppress("UNCHECKED_CAST")
				ReflectionUtils.accessibleConstructor(factoryImplementationClass).newInstance() as PluginInitializer
			} catch (ex: Throwable) {
				throw IllegalArgumentException(
					"Unable to instantiate plugin class [$pluginImplementationName] for plugin type [${PluginInitializer::class.java.name}]",
					ex
				)
			}
		}

		fun loadPlugins() {
			val classLoaderToUse = PluginLoader::class.java.classLoader
			val pluginImplementationNames = loadPluginNames(classLoaderToUse)
			if (logger.isTraceEnabled) {
				logger.trace("Loaded [${PluginInitializer::class.java}] names: " + pluginImplementationNames)
			}
			pluginImplementationNames
				.map { instantiatePlugin(it, classLoaderToUse) }
				.also { AnnotationAwareOrderComparator.sort(it) }
				.forEach { it.register() }
		}
	}
}