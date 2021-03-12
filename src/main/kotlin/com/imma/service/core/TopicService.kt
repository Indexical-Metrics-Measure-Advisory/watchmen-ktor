package com.imma.service.core

import com.imma.model.CollectionNames
import com.imma.model.assignDateTimePair
import com.imma.model.core.Factor
import com.imma.model.core.Topic
import com.imma.model.core.TopicForHolder
import com.imma.model.determineFakeOrNullId
import com.imma.model.forceAssignDateTimePair
import com.imma.model.page.DataPage
import com.imma.model.page.Pageable
import com.imma.service.Service
import io.ktor.application.*
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import kotlin.contracts.ExperimentalContracts

class TopicService(application: Application) : Service(application) {
    private fun createFactor(factor: Factor) {
        forceAssignDateTimePair(factor)
        this.writeIntoMongo { it.insert(factor) }
    }

    private fun updateFactor(factor: Factor) {
        assignDateTimePair(factor)
        writeIntoMongo { it.save(factor) }
    }

    @ExperimentalContracts
    private fun saveFactor(factor: Factor) {
        val fake = determineFakeOrNullId({ factor.factorId }, true, { factor.factorId = nextSnowflakeId().toString() })

        if (fake) {
            createFactor(factor)
        } else {
            updateFactor(factor)
        }
    }

    private fun createTopic(topic: Topic) {
        forceAssignDateTimePair(topic)
        this.writeIntoMongo { it.insert(topic) }
    }

    private fun updateTopic(topic: Topic) {
        assignDateTimePair(topic)
        writeIntoMongo { it.save(topic) }
    }

    @ExperimentalContracts
    fun saveTopic(topic: Topic) {
        val fake = determineFakeOrNullId({ topic.topicId }, true, { topic.topicId = nextSnowflakeId().toString() })

        if (fake) {
            createTopic(topic)
        } else {
            updateTopic(topic)
        }

        topic.factors.onEach { it.topicId = topic.topicId }.forEach { saveFactor(it) }
    }

    private fun findFactorsByTopicId(topicId: String): List<Factor> {
        val query: Query = Query.query(Criteria.where("topicId").`is`(topicId))
        return findListFromMongo(Factor::class.java, CollectionNames.FACTOR, query)
    }

    fun findTopicById(topicId: String): Topic? {
        return findFromMongo {
            it.findById(topicId, Topic::class.java, CollectionNames.TOPIC).apply {
                factors = findFactorsByTopicId(topicId)
            }
        }
    }

    fun findTopicsByName(name: String? = "", pageable: Pageable): DataPage<Topic> {
        val query: Query = if (name!!.isEmpty()) {
            Query.query(Criteria.where("name").all())
        } else {
            Query.query(Criteria.where("name").regex(name, "i"))
        }
        return findPageFromMongo(Topic::class.java, CollectionNames.TOPIC, query, pageable)
    }

    fun findTopicsByNameForHolder(name: String? = ""): List<TopicForHolder> {
        val query: Query = if (name!!.isEmpty()) {
            Query.query(Criteria.where("name").all())
        } else {
            Query.query(Criteria.where("name").regex(name, "i"))
        }
        query.fields().include("topicId", "name")
        return findListFromMongo(TopicForHolder::class.java, CollectionNames.TOPIC, query)
    }

    fun findTopicsByIdsForHolder(topicIds: List<String>): List<TopicForHolder> {
        val query: Query = Query.query(Criteria.where("topicId").`in`(topicIds))
        query.fields().include("topicId", "name")
        return findListFromMongo(TopicForHolder::class.java, CollectionNames.TOPIC, query)
    }
}

