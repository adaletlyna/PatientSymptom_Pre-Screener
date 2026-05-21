package com.prescreener.backend.config

import java.io.File
import java.util.*

object ConfigManager {
    private val properties = Properties()

    fun loadConfig() {
        properties.clear()
        val searchPaths = listOf(
            File("local.properties"),
            File("../local.properties"),
            File("../../local.properties"),
            File("app/local.properties"),
            File("../app/local.properties")
        )

        for (file in searchPaths) {
            val abs = file.absoluteFile
            if (abs.exists()) {
                try {
                    abs.inputStream().use { properties.load(it) }
                    val key = properties.getProperty("GEMINI_API_KEY")
                    if (!key.isNullOrBlank()) {
                        println("[CONFIG] ✓ FOUND KEY IN ${abs.path}")
                        return 
                    }
                } catch (e: Exception) { }
            }
        }
    }

    init {
        loadConfig()
    }

    fun getString(key: String): String? {
        val env = System.getenv(key)
        if (!env.isNullOrBlank()) return env.trim() // Trim environment vars
        
        val prop = properties.getProperty(key)
        return prop?.trim() // Trim property values
    }
}
