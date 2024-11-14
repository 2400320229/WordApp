package com.example.wordapp

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper


//这个数据库里的单词id是从1开始的
data class WordID(val id: Long, val word_id:Long)
class MistakeWordIDDatabaseHelper(context: Context):SQLiteOpenHelper(context, DATABASE_NAME,null,
    DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "mistake_id_words.db"
        private const val DATABASE_VERSION = 4
        private const val TABLE_NAME = "words"
        private const val COLUMN_ID = "id"
        private const val COLUMN_WORDID = "word_id"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        val CREATE_TABLE = """
        CREATE TABLE $TABLE_NAME (
            $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
            $COLUMN_WORDID TEXT NOT NULL UNIQUE
        );
    """
        db?.execSQL(CREATE_TABLE)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 4) {
            if (db != null) {
                db.execSQL("DROP TABLE IF EXISTS ${TABLE_NAME}")
            }
            onCreate(db)
        }

    }
    // 插入单词和翻译
    fun insertWordId(wordId: Int) {
        if(wordId!=null) {
            val db = this.writableDatabase
            val contentValues = ContentValues().apply {
                put(COLUMN_WORDID, wordId)

            }
            // 使用 INSERT OR IGNORE 来避免插入重复数据
            db.insertWithOnConflict(TABLE_NAME, null, contentValues, SQLiteDatabase.CONFLICT_IGNORE)
            db.close()
        }

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
    fun deleteWordId(wordId: Int) {
        val db = this.writableDatabase
        val selection = "$COLUMN_WORDID = ?"
        val selectionArgs = arrayOf(wordId.toString())

        // 删除指定 wordId 的数据
        val rowsDeleted = db.delete(TABLE_NAME, selection, selectionArgs)
        db.close()

        if (rowsDeleted > 0) {
            // 表示删除成功
            println("删除成功：删除了 $rowsDeleted 条记录")
        } else {
            // 表示没有找到符合条件的记录
            println("没有找到要删除的记录")
        }
    }

}