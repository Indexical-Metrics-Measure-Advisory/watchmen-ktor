package com.imma.service.core.parameter

import com.imma.model.compute.*
import com.imma.model.core.Pipeline
import com.imma.model.core.Topic
import com.imma.persist.core.ComputedElement
import com.imma.persist.core.ConstantElement
import com.imma.persist.core.Element
import com.imma.persist.core.build.ElementBuilder
import com.imma.service.core.*

/**
 * condition builder for workout a expression
 * which means:
 * 1. topic/factor which should be kept must be given kept topic
 * 2. any topic in topic/factor parameter, if not (1), then must be source topic.
 * 3. any variable in constant parameter must be source topic or can be found from variables
 */
class ParameterBuilder(
    private val keptTopic: Topic,
    private val pipeline: Pipeline,
    private val topics: PipelineTopics,
    private val sourceData: PipelineSourceData,
    private val variables: PipelineVariables = createPipelineVariables()
) : RunContext {
    override fun isSourceTopic(topicId: String): Boolean {
        return topicId == pipeline.topicId
    }

    private fun isKeptTopic(topicId: String): Boolean {
        return topicId == keptTopic.topicId
    }

    private fun isTopicValid(topicId: String): Boolean {
        return isSourceTopic(topicId) || isKeptTopic(topicId)
    }

    private fun computeVariable(variable: String, parameter: ConstantParameter): Any? {
        return ParameterUtils.computeVariable(variable, parameter) { propertyName ->
            sourceData[propertyName] ?: variables[propertyName]
        }
    }

    @Suppress("DuplicatedCode")
    private fun buildConstant(parameter: ConstantParameter, shouldBe: ParameterShouldBe): ConstantElement {
        val value = parameter.value

        val v = when {
            value == null -> null
            value.isEmpty() && shouldBe == ParameterShouldBe.any -> ""
            value.isEmpty() -> null
            value.isBlank() && shouldBe == ParameterShouldBe.any -> value
            value.isBlank() -> null
            else -> ParameterUtils.computeConstant(value, parameter, shouldBe) { computeVariable(it, parameter) }
        }

        return ElementBuilder.SINGLETON.value(v)
    }

    private fun buildTopicFactor(parameter: TopicFactorParameter, shouldBe: ParameterShouldBe): Element {
        val (topic, factor) = ParameterUtils.readTopicFactorParameter(parameter, topics) { topicId ->
            if (!isTopicValid(topicId)) {
                throw RuntimeException("Topic of parameter[$parameter] must be source topic of pipeline or topic of action.")
            }
        }

        return if (isSourceTopic(topic.topicId!!)) {
            // from source topic, compute to value
            val value = sourceData[factor.name!!]
            val v = when (shouldBe) {
                ParameterShouldBe.any -> value
                ParameterShouldBe.collection -> ParameterUtils.computeToCollection(value, parameter)
                ParameterShouldBe.numeric -> ParameterUtils.computeToNumeric(value, parameter)
                ParameterShouldBe.date -> ParameterUtils.computeToDate(value, parameter)
            }
            ElementBuilder.SINGLETON.value(v)
        } else {
            // from kept topic, just kept it
            ElementBuilder.SINGLETON.factor(topic.topicId!!, factor.factorId!!)
        }
    }

    private fun buildComputed(parameter: ComputedParameter, shouldBe: ParameterShouldBe): ComputedElement {
        ParameterUtils.checkSubParameters(parameter)
        ParameterUtils.checkShouldBe(parameter, shouldBe)

        val parameters = parameter.parameters

        return when (parameter.type) {
            ParameterComputeType.none -> throw RuntimeException("Operator of parameter[$parameter] cannot be none.")
            ParameterComputeType.add -> {
                ElementBuilder.SINGLETON.add {
                    parameter.parameters.forEach { push(buildParameter(it, ParameterShouldBe.numeric)) }
                }
            }
            ParameterComputeType.subtract -> {
                ElementBuilder.SINGLETON.subtract {
                    parameter.parameters.forEach { push(buildParameter(it, ParameterShouldBe.numeric)) }
                }
            }
            ParameterComputeType.multiply -> {
                ElementBuilder.SINGLETON.multiply {
                    parameter.parameters.forEach { push(buildParameter(it, ParameterShouldBe.numeric)) }
                }
            }
            ParameterComputeType.divide -> {
                ElementBuilder.SINGLETON.divide {
                    parameter.parameters.forEach { push(buildParameter(it, ParameterShouldBe.numeric)) }
                }
            }
            ParameterComputeType.modulus -> {
                ElementBuilder.SINGLETON.modulus {
                    push(buildParameter(parameters[0], ParameterShouldBe.numeric))
                    push(buildParameter(parameters[1], ParameterShouldBe.numeric))
                }
            }
            ParameterComputeType.`year-of` -> {
                ElementBuilder.SINGLETON.yearOf {
                    push(buildParameter(parameters[0], ParameterShouldBe.date))
                }
            }
            ParameterComputeType.`half-year-of` -> {
                ElementBuilder.SINGLETON.halfYearOf {
                    push(buildParameter(parameters[0], ParameterShouldBe.date))
                }
            }
            ParameterComputeType.`quarter-of` -> {
                ElementBuilder.SINGLETON.quarterOf {
                    push(buildParameter(parameters[0], ParameterShouldBe.date))
                }
            }
            ParameterComputeType.`month-of` -> {
                ElementBuilder.SINGLETON.monthOf {
                    push(buildParameter(parameters[0], ParameterShouldBe.date))
                }
            }
            ParameterComputeType.`week-of-year` -> {
                ElementBuilder.SINGLETON.weekOfYear {
                    push(buildParameter(parameters[0], ParameterShouldBe.date))
                }
            }
            ParameterComputeType.`week-of-month` -> {
                ElementBuilder.SINGLETON.weekOfMonth {
                    push(buildParameter(parameters[0], ParameterShouldBe.date))
                }
            }
            ParameterComputeType.`day-of-month` -> {
                ElementBuilder.SINGLETON.dayOfMonth {
                    push(buildParameter(parameters[0], ParameterShouldBe.date))
                }
            }
            ParameterComputeType.`day-of-week` -> {
                ElementBuilder.SINGLETON.dayOfWeek {
                    push(buildParameter(parameters[0], ParameterShouldBe.date))
                }
            }
            ParameterComputeType.`case-then` -> {
                val conditionBuilder = ConditionBuilder(keptTopic, pipeline, topics, sourceData, variables)
                ElementBuilder.SINGLETON.case {
                    // find conditional routes
                    parameters.filter { it.conditional == true && it.on != null }.forEach { param ->
                        case(conditionBuilder.build(param.on!!)).then { buildParameter(param, shouldBe) }
                    }
                    // find anyway route and add as else part
                    parameters.find { it.on == null }?.also { param -> `else` { buildParameter(param, shouldBe) } }
                }
            }
        }
    }

    @Suppress("DuplicatedCode")
    fun buildParameter(param: Parameter, shouldBe: ParameterShouldBe): Element {
        @Suppress("REDUNDANT_ELSE_IN_WHEN")
        return when (param.kind) {
            ParameterKind.topic -> buildTopicFactor(param as TopicFactorParameter, shouldBe)
            ParameterKind.constant -> buildConstant(param as ConstantParameter, shouldBe)
            ParameterKind.computed -> buildComputed(param as ComputedParameter, shouldBe)
            else -> throw RuntimeException("Unsupported parameter[$param].")
        }
    }

    fun buildParameter(param: Parameter): Element {
        return buildParameter(param, ParameterShouldBe.any)
    }
}