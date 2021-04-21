package com.imma.model.core

import com.imma.model.CollectionNames
import com.imma.model.Tuple
import com.imma.persist.annotation.*
import java.util.*

@Suppress("EnumEntryName")
enum class FactorType(val type: String) {
    sequence("sequence"),

    number("number"),
    unsigned("unsigned"),                   // 0 & positive

    text("text"),

    // address
    address("address"),
    continent("continent"),
    region("region"),
    country("country"),
    province("province"),
    city("city"),
    district("district"),
    road("road"),
    community("community"),
    floor("floor"),
    `residence-type`("residence-type"),
    `residential-area`("residential-area"),

    // contact electronic
    email("email"),
    phone("phone"),
    mobile("mobile"),
    fax("fax"),

    // date time related
    datetime("datetime"),                   // YYYY-MM-DD HH:mm:ss
    `full-datetime`("full-datetime"),
    date("date"),                           // YYYY-MM-DD
    time("time"),                           // HH:mm:ss
    year("year"),                           // 4 digits
    `half-year`("half-year"),               // 1: first half, 2: second half
    quarter("quarter"),                     // 1 - 4
    month("month"),                         // 1 - 12
    `half-month`("half-month"),             // 1: first half, 2: second half
    `ten-days`("ten-days"),                 // 1, 2, 3
    `week-of-year`("week-of-year"),         // 0 (the partial week that precedes the first Sunday of the year) - 53 (leap year)
    `week-of-month`("week-of-month"),       // 0 (the partial week that precedes the first Sunday of the year) - 5
    `half-week`("half-week"),               // 1: first half, 2: second half
    `day-of-month`("day-of-month"),         // 1 - 31, according to month/year
    `day-of-week`("day-of-week"),           // 1 (Sunday) - 7 (Saturday)
    `day-kind`("day-kind"),                 // 1: workday, 2: weekend, 3: holiday
    hour("hour"),                           // 0 - 23
    `hour-kind`("hour-kind"),               // 1: work time, 2: off hours, 3: sleeping time
    minute("minute"),                       // 0 - 59
    second("second"),                       // 0 - 59
    millisecond("millisecond"),             // 0 - 999
    `am-pm`("am-pm"),                       // 1, 2

    // individual
    gender("gender"),
    occupation("occupation"),
    `date-of-birth`("date-of-birth"),       // YYYY-MM-DD
    age("age"),
    `id-no`("id-no"),
    religion("religion"),
    nationality("nationality"),

    // organization
    `biz-trade`("biz-trade"),
    `biz-scale`("biz-scale"),

    boolean("boolean"),

    `enum`("enum"),

    `object`("object"),
    array("array");
}

// half year
const val HALF_YEAR_FIRST: Int = 1
const val HALF_YEAR_SECOND: Int = 2

// quarter
const val QUARTER_FIRST: Int = 1
const val QUARTER_SECOND: Int = 2
const val QUARTER_THIRD: Int = 3
const val QUARTER_FOURTH: Int = 4


data class Factor(
    var factorId: String? = null,
    var name: String? = null,
    var label: String? = null,
    var type: FactorType = FactorType.text,
    var enumId: String? = null,
    var defaultValue: String? = null,
    var indexGroup: String? = null,
    var description: String? = null,
)

@Suppress("EnumEntryName")
enum class TopicKind(val kind: String) {
    system("system"),
    business("business")
}

@Suppress("EnumEntryName")
enum class TopicType(val type: String) {
    raw("raw"),
    distinct("distinct"),
    aggregate("aggregate"),
    time("time"),
    ratio("ratio");
}

@Entity(CollectionNames.TOPIC)
data class Topic(
    @Id("_id")
    var topicId: String? = null,
    @Field("name")
    var name: String? = null,
    @Field("kind")
    var kind: TopicKind = TopicKind.business,
    @Field("type")
    var type: TopicType = TopicType.distinct,
    @Field("description")
    var description: String? = null,
    @Field("factors")
    var factors: MutableList<Factor> = mutableListOf(),
    @CreatedAt("_create_time")
    override var createTime: Date? = null,
    @LastModifiedAt("_last_modify_time")
    override var lastModifyTime: Date? = null,
) : Tuple()