package com.imma.persist

class DynamicTopicUtils {
    companion object {
        /**
         * replace blank and dash to underline, convert to lower case
         */
        fun toCollectionName(topicName: String): String {
            return topicName.replace(Regex("[\\s-]"), "_").toLowerCase()
        }

        /**
         * replace blank and dash to underline, convert to lower case
         */
        fun toFieldName(factorName: String): String {
            return factorName.replace(Regex("[\\s-]"), "_").toLowerCase()
        }
    }
}