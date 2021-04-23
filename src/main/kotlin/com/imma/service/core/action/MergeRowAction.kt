package com.imma.service.core.action

import com.imma.model.core.mapping.WriteAggregateArithmetic
import com.imma.persist.core.build.ElementBuilder
import com.imma.persist.core.update
import com.imma.persist.core.where
import com.imma.service.core.parameter.ConditionBuilder
import com.imma.service.core.parameter.ParameterShouldBe
import com.imma.service.core.parameter.ParameterWorker
import com.imma.utils.nothing

class MergeRowAction(private val context: ActionContext, private val logger: ActionLogger) :
	AbstractTopicAction(context, logger) {
	fun run() {
		val values = with(context) {
			val topic = prepareTopic()
			val mapping = prepareMapping()
			val by = prepareBy()

			val findBy = ConditionBuilder(topic, pipeline, topics, currentOfTriggerData, variables).build(by)
			val oldOne = services.dynamicTopic { findOne(topic, findBy) }
				?: throw RuntimeException("Cannot find row from topic[${topic.name}] on filter[$findBy].")

			val newOne = oldOne.map { (key, value) -> key to value }.toMap().toMutableMap()
			val updates = update {
				val currentWorker = ParameterWorker(pipeline, topics, currentOfTriggerData, variables)
				val previousWorker = { ParameterWorker(pipeline, topics, currentOfTriggerData, variables) }
				mapping.forEach {
					val (source, factorId, arithmetic) = it
					when (arithmetic) {
						@Suppress("SENSELESS_NULL_IN_WHEN")
						null -> set(factorId) to currentWorker.computeParameter(source)
						WriteAggregateArithmetic.none -> set(factorId) to currentWorker.computeParameter(source)
						// count will not be changed when merge
						WriteAggregateArithmetic.count -> nothing()
						WriteAggregateArithmetic.sum -> {
							// new sum value = old sum value + (new value - old value)
							set(factorId) to ElementBuilder().add {
								// old sum value
								factor(factorId)
								// new value - old value
								subtract {
									constant(currentWorker.computeParameter(source, ParameterShouldBe.numeric))
									constant(previousWorker().computeParameter(source, ParameterShouldBe.numeric))
								}
							}
						}
						WriteAggregateArithmetic.avg -> {
							val toFactor = topic.factors.find { it.factorId == factorId }!!
							val itemCount = fromAggregateAssist(oldOne["_avg_assist"] as String?, toFactor, 0)
							// new avg value = (old avg value * item count + (new value - old value)) / count
							set(factorId) to ElementBuilder().divide {
								add {
									// old sum value
									multiply {
										// old avg value
										factor(factorId)
										// item count
										constant(itemCount)
									}
									// new value - old value
									subtract {
										constant(currentWorker.computeParameter(source, ParameterShouldBe.numeric))
										constant(previousWorker().computeParameter(source, ParameterShouldBe.numeric))
									}
								}
							}
						}
					}
				}
			}
			val id = oldOne["_id"]
			services.dynamicTopic {
				updateOne(topic, updates, where { factor("_id") eq { value(id) } })
			}
			oldOne to newOne
		}
		logger.log("oldValue" to values.first, "newValue" to values.second, "insertCount" to 0, "updateCount" to 1)
	}
}