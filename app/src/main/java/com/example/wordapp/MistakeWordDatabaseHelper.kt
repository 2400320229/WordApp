package com.example.wordapp

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.wordapp.StarWordDatabaseHelper.Companion


//这个数据库里的单词id是从1开始的
data class WordID(val id: Long, val word_id:Long)
class MistakeWordIDDatabaseHelper(context: Context):SQLiteOpenHelper(context, DATABASE_NAME,null,
    DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "mistake_id_words.db"
        private const val DATABASE_VERSION = 3
        private const val TABLE_NAME = "words"
        private const val COLUMN_ID = "id"
        private const val COLUMN_WORDID = "word_id"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        val CREATE_TABLE = """
            CREATE TABLE $TABLE_NAME (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_WORDID TEXT NOT NULL
            );
        """
        db?.execSQL(CREATE_TABLE)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 3) {
            if (db != null) {
                db.execSQL("DROP TABLE IF EXISTS ${TABLE_NAME}")
            }
            onCreate(db)
        }

    }
    // 插入单词和翻译
    fun insertWordId(wordId: Int) {
        val db = this.writableDatabase
        val contentValues = ContentValues().apply {
            put(COLUMN_WORDID, wordId)

        }
        db.insert(TABLE_NAME, null, contentValues)
        db.close()
    }
    //根据单词的id来更改它的翻译

    // 根据ID获取单个单词的id
    fun getWordIdById(id: Int): String? {
        val db = this.readableDatabase
        val cursor = db.query(
            TABLE_NAME, // 表名
            arrayOf(COLUMN_WORDID), // 查询字段
            "$COLUMN_ID = ?", // 查询条件
            arrayOf(id.toString()), // 查询参数
            null, null, null // 不使用分组、排序、限制
        )

        var word: String? = null

        if (cursor != null && cursor.moveToFirst()) {
            word = cursor.getString(cursor.getColumnIndex(COLUMN_WORDID))
            cursor.close()
        }
        db.close()
        return word
    }


    // 查询所有单词及翻译
    fun getAllWords(): List<WordID> {
        val wordList = mutableListOf<WordID>()
        val db = this.readableDatabase

        // 查询数据库中的所有记录
        val cursor = db.query(TABLE_NAME, arrayOf(COLUMN_ID, COLUMN_WORDID), null, null, null, null, null)

        if (cursor != null && cursor.moveToFirst()) {
            do {
                val id = cursor.getLong(cursor.getColumnIndex(COLUMN_ID))
                val word_id = cursor.getLong(cursor.getColumnIndex(COLUMN_WORDID))
                wordList.add(WordID(id, word_id))
            } while (cursor.moveToNext())
        }

        cursor?.close()
        db.close()
        return wordList
    }
    /* fun deleteAllData(){
         val db = writableDatabase

// 执行删除所有数据的 SQL 语句
db.execSQL("DELETE FROM your_table_name") // 清空某个表的数据
// 或者清空所有表的所有数据
val cursor = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table'", null)
while (cursor.moveToNext()) {
    val tableName = cursor.getString(0)
    if (tableName != "android_metadata" && tableName != "sqlite_sequence") {
        db.execSQL("DELETE FROM $tableName")
    }
}
cursor.close()
    }*/

}