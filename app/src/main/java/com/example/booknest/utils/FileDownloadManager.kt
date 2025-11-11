package com.example.booknest.utils

import android.content.Context
import android.os.Environment
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
        
        // Supported file types
        val SUPPORTED_EXTENSIONS = listOf("pdf", "epub", "mob", "doc", "docx", "txt")
        val MAX_FILE_SIZE = 50 * 1024 * 1024 // 50MB
    }
    
    /**
     * Download a book file to the device's Downloads folder
     */
    suspend fun downloadBook(
        bookId: String,
        downloadUrl: String,
        fileName: String,
        fileType: String
    ): Result<File> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Starting download for book: $bookId")
            
            // Validate file type
            val extension = getFileExtension(fileName)
            if (extension !in SUPPORTED_EXTENSIONS) {
                return@withContext Result.failure(
                    Exception("Unsupported file type: $extension")
                )
            }
            
            // Create downloads directory
            val downloadsDir = getDownloadsDirectory()
            if (!downloadsDir.exists()) {
                downloadsDir.mkdirs()
            }
            
            // Create file with proper extension
            val file = File(downloadsDir, "${bookId}_$fileName")
            
            // Download file
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
                
                // Check file size
                val contentLength = body.contentLength()
                if (contentLength > MAX_FILE_SIZE) {
                    return@withContext Result.failure(
                        Exception("File too large: ${contentLength} bytes")
                    )
                }
                
                // Write file to storage
                body.byteStream().use { inputStream ->
                    FileOutputStream(file).use { outputStream ->
                        inputStream.copyTo(outputStream)
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
    
    /**
     * Get list of downloaded books
     */
    fun getDownloadedBooks(): List<File> {
        val downloadsDir = getDownloadsDirectory()
        return downloadsDir.listFiles()?.filter { file ->
            file.isFile && getFileExtension(file.name) in SUPPORTED_EXTENSIONS
        } ?: emptyList()
    }
    
    /**
     * Delete a downloaded book
     */
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
    
    /**
     * Get file size in human readable format
     */
    fun getFileSizeString(file: File): String {
        val bytes = file.length()
        return when {
            bytes < 1024 -> "$bytes B"
            bytes < 1024 * 1024 -> "${bytes / 1024} KB"
            bytes < 1024 * 1024 * 1024 -> "${bytes / (1024 * 1024)} MB"
            else -> "${bytes / (1024 * 1024 * 1024)} GB"
        }
    }
    
    /**
     * Check if a book is already downloaded
     */
    fun isBookDownloaded(bookId: String): Boolean {
        val downloadsDir = getDownloadsDirectory()
        return downloadsDir.listFiles()?.any { file ->
            file.name.startsWith("${bookId}_")
        } ?: false
    }
    
    /**
     * Get downloaded book file by book ID
     */
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
}
