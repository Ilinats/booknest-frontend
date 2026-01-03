package com.example.booknest.utils

import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

class FileDownloadManager(private val context: Context) {

    private val client = OkHttpClient()

    companion object {
        private const val TAG = "FileDownloadManager"
        private const val DOWNLOADS_FOLDER = "BookNest"

        val SUPPORTED_EXTENSIONS = listOf("pdf", "epub")
        val MAX_FILE_SIZE = 50 * 1024 * 1024
    }

    suspend fun downloadBook(
        bookId: String,
        downloadUrl: String,
        fileName: String,
        fileType: String
    ): Result<File> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Starting download for book: $bookId")

            val extension = when {
                fileType.isNotBlank() && fileType.lowercase() in SUPPORTED_EXTENSIONS -> {
                    fileType.lowercase()
                }

                else -> {
                    val urlExtension = downloadUrl.substringAfterLast('.', "")
                        .substringBefore('?', "")
                        .lowercase()
                    if (urlExtension in SUPPORTED_EXTENSIONS) {
                        urlExtension
                    } else {
                        val fileNameExt = getFileExtension(fileName)
                        if (fileNameExt.isNotEmpty() && fileNameExt in SUPPORTED_EXTENSIONS) {
                            fileNameExt
                        } else {
                            "epub"
                        }
                    }
                }
            }

            if (extension !in SUPPORTED_EXTENSIONS) {
                return@withContext Result.failure(
                    Exception("Unsupported file type: $extension")
                )
            }

            Log.d(TAG, "Using file extension: $extension for book: $bookId")

            val finalFileName = if (fileName.contains('.')) {
                fileName
            } else {
                "$fileName.$extension"
            }

            val file: File

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                file = downloadToMediaStore(finalFileName, extension, downloadUrl, bookId)
                    ?: return@withContext Result.failure(
                        Exception("Failed to download file using MediaStore")
                    )
            } else {
                val downloadsDir = getDownloadsDirectory()
                if (!downloadsDir.exists()) {
                    downloadsDir.mkdirs()
                }
                file = File(downloadsDir, "${bookId}_$finalFileName")

                val request = Request.Builder()
                    .url(downloadUrl)
                    .build()

                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) {
                        return@withContext Result.failure(
                            Exception("Download failed: ${response.code}")
                        )
                    }

                    val body = response.body ?: return@withContext Result.failure(
                        Exception("Empty response body")
                    )

                    val contentLength = body.contentLength()
                    if (contentLength > MAX_FILE_SIZE) {
                        return@withContext Result.failure(
                            Exception("File too large: ${contentLength} bytes")
                        )
                    }

                    body.byteStream().use { inputStream ->
                        FileOutputStream(file).use { outputStream ->
                            inputStream.copyTo(outputStream)
                        }
                    }
                }
            }

            Log.d(TAG, "Download completed: ${file.absolutePath}")
            Result.success(file)

        } catch (e: Exception) {
            Log.e(TAG, "Download failed", e)
            Result.failure(e)
        }
    }

    fun getDownloadedBooks(): List<File> {
        val downloadsDir = getDownloadsDirectory()
        return downloadsDir.listFiles()?.filter { file ->
            file.isFile && getFileExtension(file.name) in SUPPORTED_EXTENSIONS
        } ?: emptyList()
    }

    fun deleteDownloadedBook(file: File): Boolean {
        return try {
            if (file.exists()) {
                file.delete()
            } else {
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to delete file: ${file.name}", e)
            false
        }
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

    fun isBookDownloaded(bookId: String): Boolean {
        val downloadsDir = getDownloadsDirectory()
        return downloadsDir.listFiles()?.any { file ->
            file.name.startsWith("${bookId}_")
        } ?: false
    }

    fun getDownloadedBook(bookId: String): File? {
        val downloadsDir = getDownloadsDirectory()
        return downloadsDir.listFiles()?.find { file ->
            file.name.startsWith("${bookId}_")
        }
    }

    private fun getDownloadsDirectory(): File {
        return File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
            DOWNLOADS_FOLDER
        )
    }

    private fun getFileExtension(fileName: String): String {
        return fileName.substringAfterLast('.', "").lowercase()
    }

    private fun downloadToMediaStore(
        fileName: String,
        extension: String,
        downloadUrl: String,
        bookId: String
    ): File? {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            return null
        }

        val mimeType = when (extension.lowercase()) {
            "pdf" -> "application/pdf"
            "epub" -> "application/epub+zip"
            "mobi" -> "application/x-mobipocket-ebook"
            "mob" -> "application/x-mobipocket-ebook"
            "doc" -> "application/msword"
            "docx" -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
            "txt" -> "text/plain"
            else -> "application/octet-stream"
        }

        try {
            val contentValues = ContentValues().apply {
                put(MediaStore.Downloads.DISPLAY_NAME, "${bookId}_$fileName")
                put(MediaStore.Downloads.MIME_TYPE, mimeType)
                put(
                    MediaStore.Downloads.RELATIVE_PATH,
                    Environment.DIRECTORY_DOWNLOADS + "/" + DOWNLOADS_FOLDER
                )
                put(MediaStore.Downloads.IS_PENDING, 1)
            }

            val uri = context.contentResolver.insert(
                MediaStore.Downloads.EXTERNAL_CONTENT_URI,
                contentValues
            ) ?: return null

            val request = Request.Builder()
                .url(downloadUrl)
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    context.contentResolver.delete(uri, null, null)
                    return null
                }

                val body = response.body ?: return null

                val contentLength = body.contentLength()
                if (contentLength > MAX_FILE_SIZE) {
                    context.contentResolver.delete(uri, null, null)
                    return null
                }

                context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                    body.byteStream().use { inputStream ->
                        inputStream.copyTo(outputStream)
                    }
                }
            }

            val updateValues = ContentValues().apply {
                put(MediaStore.Downloads.IS_PENDING, 0)
            }
            context.contentResolver.update(uri, updateValues, null, null)

            val filePath = getFilePathFromUri(uri)
            Log.d(TAG, "File downloaded to MediaStore: $uri, path: $filePath")

            return filePath?.let { File(it) }
        } catch (e: Exception) {
            Log.e(TAG, "Error downloading to MediaStore: ${e.message}", e)
            return null
        }
    }

    private fun getFilePathFromUri(uri: android.net.Uri): String? {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            return null
        }

        return try {
            val projection = arrayOf(MediaStore.Downloads.DATA)
            context.contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Downloads.DATA)
                    cursor.getString(columnIndex)
                } else {
                    null
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting file path from URI: ${e.message}", e)
            null
        }
    }
}
