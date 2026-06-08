package com.happynovel.reader

import java.io.File

class FileReaderStateStore(
    private val file: File,
) : ReaderStateStore {
    override fun read(): String {
        if (!file.exists()) {
            return ""
        }
        return file.readText()
    }

    override fun write(value: String) {
        file.parentFile?.mkdirs()
        file.writeText(value)
    }
}
