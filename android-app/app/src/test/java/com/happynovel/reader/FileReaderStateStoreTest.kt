package com.happynovel.reader

import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.File

class FileReaderStateStoreTest {
    @Test
    fun `file store reads blank when file does not exist and persists writes`() {
        val file = File.createTempFile("reader-state", ".json")
        file.delete()
        val store = FileReaderStateStore(file)

        assertEquals("", store.read())

        store.write("""{"ok":true}""")

        assertEquals("""{"ok":true}""", FileReaderStateStore(file).read())
        file.delete()
    }
}
