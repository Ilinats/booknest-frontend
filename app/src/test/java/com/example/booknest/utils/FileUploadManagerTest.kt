package com.example.booknest.utils

import android.content.Context
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class FileUploadManagerTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    private val uploadManager = FileUploadManager(mockk<Context>(relaxed = true))

    @Test
    fun validateFile_rejectsMissingFile() {
        val missing = File(tempFolder.root, "missing.pdf")
        val result = uploadManager.validateFile(missing)

        assertTrue(result is FileUploadManager.ValidationResult.Error)
        assertEquals("File does not exist", (result as FileUploadManager.ValidationResult.Error).message)
    }

    @Test
    fun validateFile_rejectsUnsupportedExtension() {
        val file = tempFolder.newFile("notes.xyz")
        file.writeText("content")

        val result = uploadManager.validateFile(file)

        assertTrue(result is FileUploadManager.ValidationResult.Error)
        assertEquals("Unsupported file type: xyz", (result as FileUploadManager.ValidationResult.Error).message)
    }

    @Test
    fun validateFile_acceptsPdf() {
        val file = tempFolder.newFile("book.pdf")
        file.writeBytes(byteArrayOf(1, 2, 3))

        assertTrue(uploadManager.validateFile(file) is FileUploadManager.ValidationResult.Success)
    }

    @Test
    fun validateBookFile_onlyAllowsPdfAndEpub() {
        val doc = tempFolder.newFile("book.doc")
        doc.writeBytes(byteArrayOf(1))

        val result = uploadManager.validateBookFile(doc)

        assertTrue(result is FileUploadManager.ValidationResult.Error)
        assertEquals(
            "File type not allowed. Allowed types: pdf, epub",
            (result as FileUploadManager.ValidationResult.Error).message,
        )
    }

    @Test
    fun getFileSizeString_formatsKilobytes() {
        val file = tempFolder.newFile("book.pdf")
        file.writeBytes(ByteArray(2048))

        assertEquals("2 KB", uploadManager.getFileSizeString(file))
    }

    @Test
    fun isFileTypeSupported_checksExtension() {
        val pdf = tempFolder.newFile("book.pdf")
        pdf.writeBytes(byteArrayOf(1))
        val unknown = tempFolder.newFile("book.xyz")
        unknown.writeText("x")

        assertTrue(uploadManager.isFileTypeSupported(pdf))
        assertFalse(uploadManager.isFileTypeSupported(unknown))
    }
}
