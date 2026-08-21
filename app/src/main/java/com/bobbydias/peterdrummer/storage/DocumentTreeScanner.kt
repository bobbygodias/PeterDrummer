package com.bobbydias.peterdrummer.storage

import android.content.Context
import android.net.Uri
import android.provider.DocumentsContract

data class TreeDocument(
    val name: String,
    val mimeType: String,
    val sizeBytes: Long,
    val uri: Uri,
)

class DocumentTreeScanner(private val context: Context) {
    fun list(treeUri: Uri): List<TreeDocument> {
        val treeId = DocumentsContract.getTreeDocumentId(treeUri)
        val childrenUri = DocumentsContract.buildChildDocumentsUriUsingTree(treeUri, treeId)
        val projection = arrayOf(
            DocumentsContract.Document.COLUMN_DOCUMENT_ID,
            DocumentsContract.Document.COLUMN_DISPLAY_NAME,
            DocumentsContract.Document.COLUMN_MIME_TYPE,
            DocumentsContract.Document.COLUMN_SIZE,
        )
        return context.contentResolver.query(childrenUri, projection, null, null, null)?.use { cursor ->
            val idIndex = cursor.getColumnIndexOrThrow(DocumentsContract.Document.COLUMN_DOCUMENT_ID)
            val nameIndex = cursor.getColumnIndexOrThrow(DocumentsContract.Document.COLUMN_DISPLAY_NAME)
            val mimeIndex = cursor.getColumnIndexOrThrow(DocumentsContract.Document.COLUMN_MIME_TYPE)
            val sizeIndex = cursor.getColumnIndex(DocumentsContract.Document.COLUMN_SIZE)
            buildList {
                while (cursor.moveToNext()) {
                    val mime = cursor.getString(mimeIndex) ?: "application/octet-stream"
                    if (mime == DocumentsContract.Document.MIME_TYPE_DIR) continue
                    val name = cursor.getString(nameIndex) ?: continue
                    add(
                        TreeDocument(
                            name = name,
                            mimeType = mime,
                            sizeBytes = if (sizeIndex >= 0 && !cursor.isNull(sizeIndex)) cursor.getLong(sizeIndex) else 0L,
                            uri = DocumentsContract.buildDocumentUriUsingTree(treeUri, cursor.getString(idIndex)),
                        ),
                    )
                }
            }
        }.orEmpty()
    }
}
