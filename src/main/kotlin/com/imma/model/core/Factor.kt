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
    SEQUENCE("sequence"),

    NUMBER("number"),
    UNSIGNED("unsigned"),              // 0 & positive

    TEXT("text"),

    // address
    ADDRESS("address"),
    CONTINENT("continent"),
    REGION("region"),
    COUNTRY("country"),
    PROVINCE("province"),
    CITY("city"),
    DISTRICT("district"),
    ROAD("road"),
    COMMUNITY("community"),
    FLOOR("floor"),
    RESIDENCE_TYPE("residence-type"),
    RESIDENTIAL_AREA("residential-area"),

    // contact electronic
    EMAIL("email"),
    PHONE("phone"),
    MOBILE("mobile"),
    FAX("fax"),

    // date time related
    DATETIME("datetime"),              // YYYY-MM-DD HH:mm:ss
    DATE("date"),                      // YYYY-MM-DD
    TIME("time"),                      // HH:mm:ss
    YEAR("year"),                      // 4 digits
    HALF_YEAR("half-year"),            // 1: first half, 2: second half
    QUARTER("quarter"),                // 1 - 4
    SEASON("season"),                  // 1: spring, 2: summer, 3: autumn, 4: winter
    MONTH("month"),                    // 1 - 12
    HALF_MONTH("half-month"),          // 1: first half, 2: second half
    TEN_DAYS("ten-days"),              // 1, 2, 3
    WEEK_OF_YEAR("week-of-year"),      // 1 - 53
    WEEK_OF_MONTH("week-of-month"),    // 1 - 6
    HALF_WEEK("half-week"),            // 1: first half, 2: second half
    DAY_OF_MONTH("day-of-month"),      // 1 - 31, according to month/year
    DAY_OF_WEEK("day-of-week"),        // 1 - 7
    DAY_KIND("day-kind"),              // 1: workday, 2: weekend, 3: holiday
    HOUR("hour"),                      // 0 - 23
    HOUR_KIND("hour-kind"),            // 1: work time, 2: off hours, 3: sleeping time
    MINUTE("minute"),                  // 0 - 59
    SECOND("second"),                  // 0 - 59
    AM_PM("am-pm"),                    // 1, 2

    // individual
    GENDER("gender"),
    OCCUPATION("occupation"),
    DATE_OF_BIRTH("date-of-birth"),    // YYYY-MM-DD
    AGE("age"),
    ID_NO("id-no"),
    RELIGION("religion"),
    NATIONALITY("nationality"),

    // organization
    BIZ_TRADE("biz-trade"),
    BIZ_SCALE("biz-scale"),

    BOOLEAN("boolean"),

    ENUM("enum"),

    OBJECT("object"),
    ARRAY("array"),
}

@Document(collection = CollectionNames.FACTOR)
data class Factor(
    @Id
    var factorId: String? = null,
    @Field("name")
    var name: String? = null,
    @Field("type")
    var type: FactorType = FactorType.TEXT,
    @Field("label")
    var label: String? = null,
    @Field("default_value")
    var defaultValue: String? = null,
    @Field("description")
    var description: String? = null,
    @Field("create_time")
    override var createTime: String? = null,
    @Field("last_modify_time")
    override var lastModifyTime: String? = null,
    @LastModifiedDate
    @Field("last_modified")
    override var lastModified: Date = Calendar.getInstance(TimeZone.getTimeZone(ZoneOffset.UTC)).time
) : Tuple()