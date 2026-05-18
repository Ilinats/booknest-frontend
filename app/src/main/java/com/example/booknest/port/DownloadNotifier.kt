package com.example.booknest.port

/**
 * Abstraction for download-related user feedback so ViewModels do not depend on Compose UI types.
 */
interface DownloadNotifier {
    fun showDownloadStarted(bookTitle: String? = null)
    fun showDownloadCompleted(bookTitle: String? = null)
    fun showDownloadError(errorMessage: String)
}
