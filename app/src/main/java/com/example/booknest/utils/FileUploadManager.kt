package com.example.booknest.utils

import android.content.Context
import android.net.Uri
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileInputStream
import java.io.InputStream

class FileUploadManager(private val context: Context) {

    companion object {
        private const val TAG = "FileUploadManager"

        val BOOK_FILE_EXTENSIONS = listOf("pdf", "epub")

        val SUPPORTED_EXTENSIONS = listOf("pdf", "epub", "mob", "doc", "docx", "txt")
        val MAX_FILE_SIZE = 50 * 1024 * 1024

        val MIME_TYPES = mapOf(
            "pdf" to "application/pdf",
            "epub" to "application/epub+zip",
            "mob" to "application/x-mobipocket-ebook",
            "doc" to "application/msword",
            "docx" to "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "txt" to "text/plain"
        )
    }

    fun validateFile(file: File): ValidationResult {
        return when {
            !file.exists() -> ValidationResult.Error("File does not exist")
            !file.isFile -> ValidationResult.Error("Not a valid file")
            file.length() == 0L -> ValidationResult.Error("File is empty")
            file.length() > MAX_FILE_SIZE -> ValidationResult.Error("File too large (max 50MB)")
            else -> {
                val extension = getFileExtension(file.name)
                if (extension !in SUPPORTED_EXTENSIONS) {
                    ValidationResult.Error("Unsupported file type: $extension")
                } else {
                    ValidationResult.Success
                }
            }
        }
    }

    fun validateBookFile(file: File): ValidationResult {
        return when {
            !file.exists() -> ValidationResult.Error("File does not exist")
            !file.isFile -> ValidationResult.Error("Not a valid file")
            file.length() == 0L -> ValidationResult.Error("File is empty")
            file.length() > MAX_FILE_SIZE -> ValidationResult.Error("File too large (max 50MB)")
            else -> {
                val extension = getFileExtension(file.name)
                if (extension !in BOOK_FILE_EXTENSIONS) {
                    ValidationResult.Error("File type not allowed. Allowed types: pdf, epub")
                } else {
                    ValidationResult.Success
                }
            }
        }
    }

    fun createMultipartBody(file: File): MultipartBody.Part {
        val extension = getFileExtension(file.name)
        val mimeType = MIME_TYPES[extension] ?: "application/octet-stream"

        val requestFile = file.asRequestBody(mimeType.toMediaType())
        return MultipartBody.Part.createFormData("file", file.name, requestFile)
    }

    fun getFileSizeString(file: File): String {
        val bytes = file.length()
        return when {
            bytes < 1024 -> "$bytes B"
            bytes < 1024 * 1024 -> "${bytes / 1024} KB"
            bytes < 1024 * 1024 * 1024 -> "${bytes / (1024 * 1024)} MB"
            else -> "${bytes / (1024 * 1024 * 1024)} GB"
        }
    }

    fun getFileType(file: File): String {
        val extension = getFileExtension(file.name)
        return when (extension) {
            "pdf" -> "PDF Document"
            "epub" -> "EPUB Book"
            "mob" -> "MOBI Book"
            "doc" -> "Word Document"
            "docx" -> "Word Document"
            "txt" -> "Text File"
            else -> "Unknown"
        }
    }

    fun isFileTypeSupported(file: File): Boolean {
        val extension = getFileExtension(file.name)
        return extension in SUPPORTED_EXTENSIONS
    }

    private fun getFileExtension(fileName: String): String {
        return fileName.substringAfterLast('.', "").lowercase()
    }

    sealed class ValidationResult {
        object Success : ValidationResult()
        data class Error(val message: String) : ValidationResult()
    }
}
