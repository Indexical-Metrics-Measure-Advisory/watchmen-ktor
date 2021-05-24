package com.imma.service.core.action

import com.fasterxml.jackson.databind.ObjectMapper
import com.imma.model.EntityColumns
import com.imma.model.core.Factor
import com.imma.model.core.Topic
import com.imma.model.core.compute.*
import com.imma.model.core.mapping.RowMapping
import com.imma.model.core.mapping.WriteAggregateArithmetic
import com.imma.model.core.mapping.takeAsRowMappingOrThrow
import com.imma.persist.DynamicTopicKits
import com.imma.persist.core.Updates
import com.imma.persist.core.Where
import com.imma.persist.core.update
import com.imma.persist.core.where
import com.imma.service.core.PipelineTriggerData
import com.imma.service.core.parameter.*
import com.imma.utils.neverOccur
import com.imma.utils.nothing
import java.math.BigDecimal

class PipelineTriggerDataDelegate : LinkedHashMap<String, Any?>(), PipelineTriggerData {
    override fun get(key: String): Any {
        return super.get(key) ?: 0
    }
}

abstract class AbstractTopicAction(private val context: ActionContext) {
    private val jsonParser by lazy { ObjectMapper() }

    private fun toNumeric(value: Any?): BigDecimal? {
        return value.run {
            ValueKits.computeToNumeric(this) { "Cannot cast value[$this] to numeric." }
        }
    }

    @Suppress("SameParameterValue")
    private fun toNumeric(value: Any?, defaultValue: BigDecimal): BigDecimal {
        return toNumeric(value ?: defaultValue)!!
    }

    private fun toNumericOrZero(value: Any?): BigDecimal {
        return toNumeric(value, BigDecimal.ZERO)
    }

    protected fun prepareVariableName(): String {
        return with(context) {
            val variableName = action["variableName"]?.toString()
            if (variableName.isNullOrBlank()) {
                throw RuntimeException("Variable name of action cannot be null or empty.")
            } else {
                variableName
            }
        }
    }

    protected fun prepareBy(): ParameterJoint {
        return with(context) {
            val by = action["by"]
            when {
                by == null -> throw RuntimeException("By of read action cannot be null.")
                by !is Map<*, *> -> throw RuntimeException("By of read action should be a map, but is [$by] now.")
                by.size == 0 -> throw RuntimeException("By of read action cannot be empty.")
                else -> takeAsParameterJointOrThrow(by)
            }
        }
    }

    protected fun prepareMapping(): RowMapping {
        return with(context) {
            val mapping = action["mapping"]
            @Suppress("UNCHECKED_CAST")
            when {
                mapping == null -> throw RuntimeException("Mapping of insert/merge action cannot be null.")
                mapping !is Collection<*> && mapping !is Array<*> -> throw RuntimeException("By of insert/merge action should be a collection or an array, but is [$mapping] now.")
                mapping is Collection<*> && mapping.size == 0 -> throw RuntimeException("Mapping of insert/merge action cannot be empty.")
                mapping is Array<*> && mapping.size == 0 -> throw RuntimeException("Mapping of insert/merge action cannot be empty.")
                mapping is Collection<*> -> takeAsRowMappingOrThrow(mapping as Collection<Map<*, *>>)
                mapping is Array<*> -> takeAsRowMappingOrThrow(mapping as Array<Map<*, *>>)
                else -> neverOccur()
            }
        }
    }

    protected fun prepareSource(): Parameter {
        return with(context) {
            when (val source = action["source"]) {
                null -> throw RuntimeException("Source of write action cannot be null.")
                !is Map<*, *> -> throw RuntimeException("Source of write action should be a map, but is [$source] now.")
                else -> takeAsParameterOrThrow(source)
            }
        }
    }

    protected fun prepareTopic(): Topic {
        return with(context) {
            val topicId = action["topicId"]?.toString()
            if (topicId.isNullOrBlank()) {
                throw RuntimeException("Topic id of action cannot be null or empty.")
            }

            val topic = topics[topicId] ?: services.topic {
                findTopicById(topicId)
            } ?: throw RuntimeException("Topic[$topicId] of action not found.")
            // put into memory
            topic.also {
                topics[topicId] = it
                // register to persist
                services.persist().registerDynamicTopic(topic)
            }
        }
    }

    protected fun prepareFactor(topic: Topic): Factor {
        return with(context) {
            val factorId = action["factorId"]?.toString()
            if (factorId.isNullOrBlank()) {
                throw RuntimeException("Factor id of action cannot be null or empty.")
            }

            topic.factors.find { it.factorId == factorId }
                ?: throw RuntimeException("Factor[$factorId] of topic[${topic.topicId}] not found.")
        }
    }

    protected fun prepareArithmetic(): WriteAggregateArithmetic {
        return with(context) {
            when (val v = action["arithmetic"]) {
                null -> WriteAggregateArithmetic.none
                else -> WriteAggregateArithmetic.valueOf(v.toString())
            }
        }
    }

    protected fun compute(parameter: Parameter): Any? {
        return with(context) {
            val worker = ParameterWorker(pipeline, topics, currentOfTriggerData, previousOfTriggerData, variables)
            worker.computeParameter(parameter)
        }
    }

    protected fun compute(joint: ParameterJoint): Boolean {
        return with(context) {
            val worker = ConditionWorker(pipeline, topics, currentOfTriggerData, previousOfTriggerData, variables)
            worker.computeJoint(joint)
        }
    }

    protected fun build(keptTopic: Topic, parameter: Parameter) {
        return with(context) {
            val builder =
                ParameterBuilder(keptTopic, pipeline, topics, currentOfTriggerData, previousOfTriggerData, variables)
            builder.buildParameter(parameter)
        }
    }

    protected fun build(keptTopic: Topic, joint: ParameterJoint): Where {
        return with(context) {
            ConditionBuilder(keptTopic, pipeline, topics, currentOfTriggerData, previousOfTriggerData, variables).build(
                joint
            )
        }
    }

    private fun delegatePrevious(previousOfTriggerData: PipelineTriggerData?): PipelineTriggerData {
        return if (previousOfTriggerData == null) {
            PipelineTriggerDataDelegate()
        } else {
            PipelineTriggerDataDelegate().apply {
                putAll(previousOfTriggerData)
            }
        }
    }

    private fun toNumericUsePrevious(param: Parameter): Any? {
        return with(context) {
            val worker = ParameterWorker(
                pipeline,
                topics,
                delegatePrevious(previousOfTriggerData),
                previousOfTriggerData,
                variables
            )
            worker.computeParameter(param, ParameterShouldBe.numeric)
        }
    }

    private fun toNumericUseCurrent(param: Parameter): Any? {
        return with(context) {
            val worker = ParameterWorker(pipeline, topics, currentOfTriggerData, previousOfTriggerData, variables)
            worker.computeParameter(param, ParameterShouldBe.numeric)
        }
    }

    private fun toAnyUseCurrent(param: Parameter): Any? {
        return with(context) {
            val worker = ParameterWorker(pipeline, topics, currentOfTriggerData, previousOfTriggerData, variables)
            worker.computeParameter(param)
        }
    }

    protected fun ActionContext.insertRow(topic: Topic, mapping: RowMapping) {
        val one = mapping.run {
            val aggregates = mutableMapOf<String, BigDecimal>()
            this.map { row ->
                val (source, factorId, arithmetic) = row

                if (arithmetic == WriteAggregateArithmetic.count) {
                    // the first one, count always be 1
                    factorId to 1
                } else {
                    // the first one, arithmetic is ignored
                    compute(source).also {
                        // write aggregate assist
                        if (arithmetic == WriteAggregateArithmetic.avg) {
                            val factor = topic.factors.find { it.factorId == factorId }!!
                            // the first one, count of avg always be 1
                            setCountOfAvg(aggregates, factor, BigDecimal.ONE)
                        }
                    }.run {
                        if (arithmetic == WriteAggregateArithmetic.avg || arithmetic == WriteAggregateArithmetic.sum) {
                            factorId to toNumericOrZero(this)
                        } else {
                            factorId to this
                        }
                    }
                }
            }.toMutableList().also {
                it.add(toAggregateAssist(aggregates))
            }
        }.toMap().toMutableMap()
        services.dynamicTopic { insertOne(topic, one) }
    }

    protected fun ActionContext.mergeRow(
        topic: Topic,
        mapping: RowMapping,
        oldOne: Map<String, *>
    ): Map<String, *> {
        val newOne = oldOne.map { (key, value) -> key to value }.toMap().toMutableMap()
        val updates = update {
            mapping.forEach { factorMapping ->
                val (source, factorId, arithmetic) = factorMapping
                when (arithmetic) {
                    @Suppress("SENSELESS_NULL_IN_WHEN")
                    null -> set(factorId) to toAnyUseCurrent(source)
                    WriteAggregateArithmetic.none -> set(factorId) to toAnyUseCurrent(source)
                    // count will not be changed when merge
                    WriteAggregateArithmetic.count -> nothing()
                    WriteAggregateArithmetic.sum -> updateSumValue(topic, factorId, oldOne, source)
                    WriteAggregateArithmetic.avg -> updateAvgValue(topic, factorId, oldOne, source)
                }
            }
        }

        // do update
        val id = oldOne[EntityColumns.OBJECT_ID]
        services.dynamicTopic {
            updateOne(topic, updates, where { factor(EntityColumns.OBJECT_ID) eq { value(id) } })
        }

        newOne[EntityColumns.OBJECT_ID] = id
        // merge updated values to new one
        updates.parts.forEach {
            // here is id, see updates creation logic above
            val factorId = it.factor.factorIdOrName

            // convert factor id to factor name
            val factorName = topic.factors.find { it.factorId == factorId }!!.name!!
            newOne[factorName] = it.value
        }

        // return old/new value pair
        return newOne
    }

    private fun Updates.updateAvgValue(
        topic: Topic,
        factorId: String,
        oldOne: Map<String, *>,
        source: Parameter
    ) {
        val toFactor = topic.factors.find { it.factorId == factorId }!!

        val oldAvg = toNumericOrZero(oldOne[toFactor.name!!])
        val itemCount = getCountOfAvg(oldOne, toFactor)
        val newValue = toNumericOrZero(toNumericUseCurrent(source))
        val oldValue = toNumericOrZero(toNumericUsePrevious(source))
        // new avg value = (old avg value * item count + (new value - old value)) / count
        val newAvg = (oldAvg * itemCount + newValue - oldValue) / itemCount

        // build update
        set(factorId) to newAvg
    }

    private fun Updates.updateSumValue(
        topic: Topic,
        factorId: String,
        oldOne: Map<String, *>,
        source: Parameter
    ) {
        val toFactor = topic.factors.find { it.factorId == factorId }!!

        val oldSum = toNumericOrZero(oldOne[toFactor.name!!])
        val newValue = toNumericOrZero(toNumericUseCurrent(source))
        val oldValue = toNumericOrZero(toNumericUsePrevious(source))
        // new sum value = old sum value + (new value - old value)
        val newSum = oldSum + newValue - oldValue

        // build update
        set(factorId) to newSum
    }

    private fun fromAggregateAssist(one: Map<String, *>, key: String, defaultValue: Any? = null): Any? {
        val jsonStr = one[EntityColumns.AGGREGATE_ASSIST] as String?
        val assist = jsonParser.readValue(jsonStr ?: EntityColumns.EMPTY_AGGREGATE_ASSIST, Map::class.java)
        return assist[key] ?: defaultValue
    }

    private fun getCountOfAvg(one: Map<String, *>, factor: Factor): BigDecimal {
        val name = DynamicTopicKits.toFieldName(factor.name!!)
        return toNumeric(fromAggregateAssist(one, "$name.${EntityColumns.AVG_COUNT}"), BigDecimal.ZERO)
    }

    @Suppress("SameParameterValue")
    private fun setCountOfAvg(values: MutableMap<String, BigDecimal>, factor: Factor, value: BigDecimal) {
        val name = DynamicTopicKits.toFieldName(factor.name!!)
        values["$name.${EntityColumns.AVG_COUNT}"] = value
    }

    private fun toAggregateAssist(values: Map<String, BigDecimal>): Pair<String, Map<String, BigDecimal>> {
        return EntityColumns.AGGREGATE_ASSIST to values
    }
}