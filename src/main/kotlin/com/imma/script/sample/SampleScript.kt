import com.imma.script.ScriptExecutor

object SampleScript {
    fun eval() {
        val y = ScriptExecutor.run<Int>(
            "classpath:/META-INF/scripts/sample/sample.kts",
            mutableMapOf("sampleVariable" to 10)
        )
        println(y)
    }
}
