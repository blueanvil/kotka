package com.blueanvil.kotka

/**
 * @author Cosmin Marginean
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class KotkaMessage(val topic: String,
                              val threads: Int,
                              val pubSub: Boolean = false)