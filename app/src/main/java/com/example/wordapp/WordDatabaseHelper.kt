package com.example.wordapp

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log

class WordDatabaseHelper(context: Context):SQLiteOpenHelper(context, DATABASE_NAME,null,
    DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "words.db"
        private const val DATABASE_VERSION = 1
        private const val TABLE_NAME = "words"
        private const val COLUMN_ID = "id"
        private const val COLUMN_WORD = "word"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        val CREATE_TABLE = """
            CREATE TABLE $TABLE_NAME (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_WORD TEXT NOT NULL
            );
        """
        db?.execSQL(CREATE_TABLE)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        onCreate(db)
    }
    // 插入单词
    fun insertWord(word: String) {
        val db = this.writableDatabase
        val contentValues = ContentValues().apply {
            put(COLUMN_WORD, word)
        }
        db.insert(TABLE_NAME, null, contentValues)
        db.close()
    }
    // 插入多个单词
    fun insertWords(words: List<String>) {
        val db = this.writableDatabase
        db.beginTransaction()  // 开启事务，确保批量插入时性能更好且数据一致性更强
        try {
            for (word in words) {
                val contentValues = ContentValues().apply {
                    put(COLUMN_WORD, word)
                }
                db.insert(TABLE_NAME, null, contentValues)
            }
            db.setTransactionSuccessful()  // 提交事务
        } catch (e: Exception) {
            // 发生异常时可以打印日志或者进行其他处理
            e.printStackTrace()
        } finally {
            db.endTransaction()  // 结束事务
            db.close()
        }
    }
    // 根据ID获取单个单词
    fun getWordById(id: Int): String? {
        val db = this.readableDatabase
        val cursor = db.query(
            TABLE_NAME, // 表名
            arrayOf(COLUMN_WORD), // 查询字段
            "$COLUMN_ID = ?", // 查询条件
            arrayOf(id.toString()), // 查询参数
            null, null, null // 不使用分组、排序、限制
        )

        var word: String? = null
        if (cursor != null && cursor.moveToFirst()) {
            word = cursor.getString(cursor.getColumnIndex(COLUMN_WORD))
            cursor.close()
        }
        db.close()
        return word
    }

    // 获取所有单词
    fun getAllWords(): List<String> {
        val words = mutableListOf<String>()
        val db = this.readableDatabase
        val cursor = db.query(TABLE_NAME, arrayOf(COLUMN_WORD), null, null, null, null, null)

        if (cursor != null && cursor.moveToFirst()) {
            do {
                val word = cursor.getString(cursor.getColumnIndex(COLUMN_WORD))
                words.add(word)
            } while (cursor.moveToNext())
            cursor.close()
        }
        db.close()
        return words
    }

}