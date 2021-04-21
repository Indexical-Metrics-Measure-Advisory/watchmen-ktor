package com.imma.persist

private fun String.toSnakeCase(): String {
    var text = ""
    var isFirst = true
    this.forEach {
        if (it.isUpperCase()) {
            if (isFirst) {
                isFirst = false
            } else {
                text += "_"
            }
            text += it.toLowerCase()
        } else {
            text += it
        }
    }
    return text
}

private fun String.beginWithLowerCase(): String {
    return when (this.length) {
        0 -> ""
        1 -> this.toLowerCase()
        else -> this[0].toLowerCase() + this.substring(1)
    }
}

private fun String.beginWithUpperCase(): String {
    return when (this.length) {
        0 -> ""
        1 -> this.toUpperCase()
        else -> this[0].toUpperCase() + this.substring(1)
    }
}

private fun String.toCamelCase(): String {
    return this.split('_').joinToString("") {
        it.beginWithUpperCase()
    }.beginWithLowerCase()
}

class DynamicTopicKits {
    companion object {
        /**
         * replace blank, strikethrough to underline, convert to snake case
         */
        fun toCollectionName(topicName: String): String {
            return topicName.trim {
                it == ' ' || it == '-'
            }.replace(Regex("[\\s-]"), "_").toSnakeCase()
        }

        /**
         * replace blank, strikethrough to underline, convert to snake case
         */
        fun toFieldName(factorName: String): String {
            return factorName.trim {
                it == ' ' || it == '-'
            }.replace(Regex("[\\s-]"), "_").toSnakeCase()
        }

        fun fromFieldName(fieldName: String): String {
            return fieldName.toCamelCase()
        }
    }
}