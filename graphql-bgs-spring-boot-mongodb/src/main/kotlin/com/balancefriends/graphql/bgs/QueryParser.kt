package com.balancefriends.graphql.bgs

import org.slf4j.LoggerFactory

object QueryParser {
  private val logger = LoggerFactory.getLogger(QueryParser::class.java)
  fun pagination(
    source: Query = Query(),
    first: Int? = 20,
    offset: Long? = 0,
    after: String? = null,
    sort: String? = null
  ): Query {
    if (first != null && first < 0) {
      throw IllegalArgumentException("`first` 는 반드시 0보다 커야 합니다")
    }
    // First Based!!!
    if (first != null) {
      logger.debug { "has first : $first" }
      source.with(PageRequest.of(0, first))
    }
    if (offset != null) {
      source.skip(offset)
    }
    if (after != null) {
      logger.debug { "has after : $after" }
      source.addCriteria(Criteria.where("id").gt(ObjectId(after)))
    }

    source.sort(sort)

    return source

  }

  private fun Query.sort(sort: String?): Query {
    val orders = mutableListOf<Sort.Order>()
    if (!sort.isNullOrBlank()) {
      orders.addAll(sort.split(",").map {
        val order = it.split(":")
        val field = order.first()
        if (order.size > 1) {
          when (order[1].toLowerCase()) {
            "asc" -> Sort.Order.asc(field)
            "desc" -> Sort.Order.desc(field)
            else -> Sort.Order.by(field)
          }
        } else {
          Sort.Order.by(field)
        }
      })
    }
    if (!orders.any { it.property == "id" }) {
      orders.add(Sort.Order.asc("id"))
    }
    logger.info { "sort : ${orders}" }
    return with(Sort.by(orders))
  }

  /*
  fun fromFilter(filter: List<Operator>): Criteria {
    val criteria = Criteria()
    filter.forEach {
      when (it) {
        is GtOperator -> criteria.and(it.field).gt(convert(it.value))
      }
    }
    return criteria
  }*/

  fun parse(
    source: Query = Query(),
    first: Int? = 20,
    offset: Long? = 0,
    after: String? = null,
    sort: String? = null,
    where: Map<String, Any>? = null,
    clazz: KClass<*>
  ): Query {
    val query = pagination(source, first, offset, after, sort)

    where(where, clazz)?.let {
      query.addCriteria(it)
    }

    logger.debug { "query : ${query.queryObject}" }
    logger.debug { "sort : ${query.sortObject}" }
    logger.debug { "fields : ${query.fieldsObject}" }

    return query
  }

  fun where(query: Map<String, Any>?, clazz: KClass<*>? = null): Criteria? {
    if (query == null) {
      return null
    }
    var criteria = Criteria()
    query.forEach {

      val split = it.key.split("_")
      val key = if (it.key == "_id") "id" else split.first()
      logger.debug { "key : $key" }
      logger.debug { "value : ${it.value}" }

      val fieldName = findFieldName(key, clazz)
      logger.debug { "fieldName : $fieldName" }

      criteria = criteria.and(fieldName)

      criteria = when (if (split.size > 1) Operator.parse(split[1]) else Operator.EQ) {
        Operator.EQ -> criteria.isEqualTo(convert(fieldName, it.value, clazz))
        Operator.GT -> criteria.gt(convert(fieldName, it.value, clazz))
        Operator.GTE -> criteria.gte(convert(fieldName, it.value, clazz))
        Operator.LT -> criteria.lt(convert(fieldName, it.value, clazz))
        Operator.LTE -> criteria.lte(convert(fieldName, it.value, clazz))
        Operator.NE -> criteria.ne(convert(fieldName, it.value, clazz))
        Operator.IN -> criteria.inValues(collection(fieldName, it.value, clazz))
        Operator.NIN -> criteria.nin(collection(fieldName, it.value, clazz))
        Operator.CONTAINS -> criteria.all(convert(fieldName, it.value, clazz))
        Operator.EXISTS -> criteria.exists(it.value as Boolean)
        else -> criteria.isEqualTo(convert(fieldName, it.value, clazz))
      }
    }

    return criteria
  }

  private fun findFieldName(key: String, clazz: KClass<*>?): String {
    return if (key == "id" || key == "_id") {
      logger.debug { "find @Id field" }
      // find id
      val idField =
          clazz?.java?.declaredFields?.firstOrNull { field ->
            field.isAnnotationPresent(Id::class.java)
          }
      idField?.name ?: key
    } else {
      key
    }
  }

  private fun convert(fieldName: String, value: Any, clazz: KClass<*>?): Any {

    logger.debug { "field name is $fieldName" }

    try {
      val fieldType = clazz?.java?.getDeclaredField(fieldName)?.type ?: value::class.java

      logger.debug { "field type is $fieldType" }

      return when (value) {
        // custom scalars 는 string 형태??
        is String -> {
          when (fieldType) {
            ObjectId::class.java -> ObjectId(value)
            UUID::class.java -> UUID.fromString(value)
            Instant::class.java -> Instant.parse(value)
            OffsetDateTime::class.java -> Instant.parse(value)
            LocalDate::class.java -> LocalDate.parse(value)
            LocalTime::class.java -> LocalTime.parse(value)
            else -> value
          }
        }
        else -> value
      }
    } catch (e: NoSuchFieldException) {
      logger.debug { "no such field" }

      return value
    }
  }

  private fun collection(key: String, value: Any, clazz: KClass<*>? = null): Any {
    return if (value is Collection<*>) {
      value
    } else {
      convert(key, value, clazz)
    }
  }

  /*
  fun cursorFromOffset(offset: Long): String {
    //return Base64.encodeBase64URLSafeString("offset:$offset".toByteArray())
  }*/

}