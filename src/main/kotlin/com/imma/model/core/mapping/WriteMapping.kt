package com.imma.model.core.mapping

import com.imma.model.core.compute.Parameter
import com.imma.model.core.compute.takeAsParameterOrThrow

@Suppress("EnumEntryName")
enum class WriteAggregateArithmetic(val type: String) {
	none("none"),
	count("count"),
	sum("sum"),
	avg("avg"),
	max("max"),
	min("min"),
	median("med"),
}

data class FactorMapping(
	val source: Parameter,
	val factorId: String = "",
	val arithmetic: WriteAggregateArithmetic = WriteAggregateArithmetic.none
)

typealias RowMapping = MutableList<FactorMapping>

private fun takeAsFactorMappingOrThrow(map: Map<*, *>): FactorMapping {
	val source = map["source"] ?: throw RuntimeException("Source of factor mapping[$map] cannot be null.")
	if (source !is Map<*, *>) {
		throw RuntimeException("Source of factor mapping[$map] should be a map, but is[$source] now.")
	}

	val factorId = map["factorId"]
		?: throw RuntimeException("Target factor of factor mapping[$map] cannot be null.")

	val arithmetic = when (val v = map["arithmetic"]) {
		null -> WriteAggregateArithmetic.none
		else -> WriteAggregateArithmetic.valueOf(v.toString())
	}

	return FactorMapping(takeAsParameterOrThrow(source), factorId.toString(), arithmetic)
}

fun takeAsRowMappingOrThrow(list: List<Map<*, *>>): RowMapping {
	return list.map { takeAsFactorMappingOrThrow(it) }.toMutableList()
}

fun takeAsRowMappingOrThrow(array: Array<Map<*, *>>): RowMapping {
	return array.map { takeAsFactorMappingOrThrow(it) }.toMutableList()
}
