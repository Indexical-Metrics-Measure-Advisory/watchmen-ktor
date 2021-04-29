package com.imma.persist.core

import com.imma.persist.core.build.JointBuilder

@Suppress("EnumEntryName")
enum class JointType {
	and, or
}

interface Condition

@Suppress("EnumEntryName")
enum class ExpressionOperator {
	empty,
	`not-empty`,
	equals,
	`not-equals`,
	less,
	`less-equals`,
	more,
	`more-equals`,
	`in`,
	`not-in`,
	`has-text`,
	`has-one`;
}

class Expression : Condition {
	var left: Element? = null
	var operator: ExpressionOperator? = null
	var right: Element? = null
	override fun toString(): String {
		return "Expression(left=$left, operator=$operator, right=$right)"
	}
}

abstract class Joint(val type: JointType) : Condition {
	val parts: MutableList<Condition> = mutableListOf()
	override fun toString(): String {
		return "Joint(type=$type, parts=$parts)"
	}
}

class And : Joint(JointType.and)
class Or : Joint(JointType.or)

fun where(block: JointBuilder.() -> Unit): Joint {
	return where(JointType.and, block)
}

fun where(type: JointType, block: JointBuilder.() -> Unit): Joint {
	val joint = if (type === JointType.and) And() else Or()
	val builder = JointBuilder(joint)
	builder.block()
	return joint
}

typealias Where = Joint
