package com.blueanvil.kotka

/**
 * @author Cosmin Marginean
 */
data class TestMessage(val name: String,
                       val age: Int,
                       val aliases: List<String>)

data class GenerateExcel(val reportName: String = uuid())
data class PngToPdf(val pngSource: String = uuid())