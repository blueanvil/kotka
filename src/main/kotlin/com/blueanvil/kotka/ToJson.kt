package com.blueanvil.kotka

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper

/**
 * @author Cosmin Marginean
 */
class ToJson<T : Any>(private val objectMapper: ObjectMapper = jacksonObjectMapper()) : (T) -> String {

    override fun invoke(value: T): String {
        return objectMapper.writeValueAsString(value)
    }
}