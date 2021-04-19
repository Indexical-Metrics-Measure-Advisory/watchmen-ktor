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

class DynamicTopicUtils {
    companion object {
        /**
         * replace blank and dash to underline, convert to lower case
         */
        fun toCollectionName(topicName: String): String {
            return topicName.trim {
                it == ' ' || it == '-'
            }.replace(Regex("[\\s-]"), "_").toSnakeCase()
        }

        /**
         * replace blank and dash to underline, convert to lower case
         */
        fun toFieldName(factorName: String): String {
            return factorName.trim {
                it == ' ' || it == '-'
            }.replace(Regex("[\\s-]"), "_").toSnakeCase()
        }
    }
}