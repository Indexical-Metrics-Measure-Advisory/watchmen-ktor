package com.imma.script

import io.ktor.features.*
import org.springframework.core.io.DefaultResourceLoader
import org.springframework.util.FileCopyUtils
import java.io.InputStreamReader
import java.util.concurrent.ConcurrentHashMap
import javax.script.*

object ScriptExecutor {
    private val threadLocal = ThreadLocal<ScriptEngine?>()
    private val compiledScripts: MutableMap<String, CompiledScript> = ConcurrentHashMap()

    private fun loadScript(location: String): CompiledScript {
        val resource = DefaultResourceLoader().getResource(location)

        if (!resource.exists()) {
            throw NotFoundException()
        } else {
            val script = FileCopyUtils.copyToString(InputStreamReader(resource.inputStream))
            val engine = getEngine()
            // kotlin script engine is Compilable
            // see KotlinJsr223JvmLocalScriptEngine for details
            val compiledScript = (engine as Compilable).compile(script)
            compiledScripts[location] = compiledScript
            return compiledScript
        }
    }

    private fun getEngine(): ScriptEngine {
        var engine = threadLocal.get()
        if (engine == null) {
            engine = ScriptEngineManager().getEngineByExtension("kts")
            threadLocal.set(engine)
        }
        return engine!!
    }

    @Suppress("UNCHECKED_CAST")
    fun <T> run(location: String): T {
        val script = compiledScripts[location] ?: loadScript(location)
        return script.eval() as T
    }

    @Suppress("UNCHECKED_CAST")
    fun <T> run(location: String, bindings: Map<String, Any>): T {
        val script = compiledScripts[location] ?: loadScript(location)
        val simpleBindings = SimpleBindings(bindings)
        return script.eval(simpleBindings) as T
    }
}