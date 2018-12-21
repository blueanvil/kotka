package com.blueanvil.kotka

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import kotlin.reflect.KClass

/**
 * @author Cosmin Marginean
 */
class FromJson<T : Any>(private val cls: KClass<T>,
                        private val objectMapper: ObjectMapper = jacksonObjectMapper()) : (String) -> T {

    override fun invoke(jsonString: String): T {
        return objectMapper.readValue(jsonString, cls.javaObjectType)
    }
}