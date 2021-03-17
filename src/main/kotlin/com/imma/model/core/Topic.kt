package com.imma.model.core

import com.imma.model.CollectionNames
import com.imma.model.Tuple
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.mapping.Field
import java.time.ZoneOffset
import java.util.*

enum class FactorType(val type: String) {
    sequence("sequence"),

    number("number"),
    unsigned("unsigned"),              // 0 & positive

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
    datetime("datetime"),              // YYYY-MM-DD HH:mm:ss
    date("date"),                      // YYYY-MM-DD
    time("time"),                      // HH:mm:ss
    year("year"),                      // 4 digits
    `half-year`("half-year"),            // 1: first half, 2: second half
    quarter("quarter"),                // 1 - 4
    season("season"),                  // 1: spring, 2: summer, 3: autumn, 4: winter
    month("month"),                    // 1 - 12
    `half-month`("half-month"),          // 1: first half, 2: second half
    `ten-days`("ten-days"),              // 1, 2, 3
    `week-of-year`("week-of-year"),      // 1 - 53
    `week-of-month`("week-of-month"),    // 1 - 6
    `half-week`("half-week"),            // 1: first half, 2: second half
    `day-of-month`("day-of-month"),      // 1 - 31, according to month/year
    `day-of-week`("day-of-week"),        // 1 - 7
    `day-kind`("day-kind"),              // 1: workday, 2: weekend, 3: holiday
    hour("hour"),                      // 0 - 23
    `hour-kind`("hour-kind"),            // 1: work time, 2: off hours, 3: sleeping time
    minute("minute"),                  // 0 - 59
    second("second"),                  // 0 - 59
    `am-pm`("am-pm"),                    // 1, 2

    // individual
    gender("gender"),
    occupation("occupation"),
    `date-of-birth`("date-of-birth"),    // YYYY-MM-DD
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

enum class TopicKind(val kind: String) {
    system("system"),
    business("business")
}

enum class TopicType(val type: String) {
    raw("raw"),
    distinct("distinct"),
    aggregate("aggregate"),
    time("time"),
    ratio("ratio");
}

@Document(collection = CollectionNames.TOPIC)
data class Topic(
    @Id
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
    @Field("create_time")
    override var createTime: String? = null,
    @Field("last_modify_time")
    override var lastModifyTime: String? = null,
    @LastModifiedDate
    @Field("last_modified")
    override var lastModified: Date = Calendar.getInstance(TimeZone.getTimeZone(ZoneOffset.UTC)).time
) : Tuple()