package com.imma.service

import com.imma.model.Tuple
import com.imma.model.assignDateTimePair
import com.imma.model.forceAssignDateTimePair

open class TupleService(services: Services) : Service(services) {
	protected fun <T : Tuple> createTuple(tuple: T, entityClass: Class<T>, entityName: String) {
		forceAssignDateTimePair(tuple)
		services.persist().insertOne(tuple, entityClass, entityName)
	}

	protected fun <T : Tuple> updateTuple(tuple: T, entityClass: Class<T>, entityName: String) {
		assignDateTimePair(tuple)
		services.persist().updateOne(tuple, entityClass, entityName)
	}
}