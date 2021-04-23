package com.imma.service.core.action

import com.fasterxml.jackson.databind.ObjectMapper
import com.imma.model.core.Factor
import com.imma.model.core.compute.ValueKits
import com.imma.persist.DynamicTopicKits
import com.imma.service.core.PipelineTriggerData
import java.math.BigDecimal

fun toNumeric(value: Any?): BigDecimal? {
	return value.run {
		ValueKits.computeToNumeric(this) { "Cannot cast value[$this] to numeric." }
	}
}

fun toNumeric(value: Any?, defaultValue: BigDecimal): BigDecimal {
	return toNumeric(value ?: defaultValue)!!
}

fun toNumericOrZero(value: Any?): BigDecimal {
	return toNumeric(value, BigDecimal.ZERO)
}


class PipelineTriggerDataDelegate : LinkedHashMap<String, Any?>(), PipelineTriggerData {
	override fun get(key: String): Any {
		return super.get(key) ?: 0
	}
}

fun delegatePrevious(previousOfTriggerData: PipelineTriggerData?): PipelineTriggerData {
	return if (previousOfTriggerData == null) {
		PipelineTriggerDataDelegate()
	} else {
		PipelineTriggerDataDelegate().apply {
			putAll(previousOfTriggerData)
		}
	}
}

val jsonParer = ObjectMapper()
fun fromAggregateAssist(from: String?, factor: Factor, defaultValue: Any? = null): Any? {
	val assist = jsonParer.readValue(from ?: "{}", Map::class.java)
	return assist[DynamicTopicKits.toFieldName(factor.name!!)] ?: defaultValue
}

fun itemCountAggregateAssist(from: String?, factor: Factor, defaultValue: BigDecimal): BigDecimal {
	return toNumeric(fromAggregateAssist(from, factor), defaultValue)
}