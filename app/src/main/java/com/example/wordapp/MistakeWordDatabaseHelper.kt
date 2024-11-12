package com.example.wordapp

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

//这个数据库里的单词id是从1开始的
data class Word(val id: Long, val word: String, val translation: String?)
class MistakeWordDatabaseHelper(context: Context):SQLiteOpenHelper(context, DATABASE_NAME,null,
    DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "mistake_words.db"
        private const val DATABASE_VERSION = 2
        private const val TABLE_NAME = "words"
        private const val COLUMN_ID = "id"
        private const val COLUMN_WORD = "word"
        private const val COLUMN_TRANSLATION = "translation"  // 新增翻译列
    }

    override fun onCreate(db: SQLiteDatabase?) {
        val CREATE_TABLE = """
            CREATE TABLE $TABLE_NAME (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_WORD TEXT NOT NULL,
                $COLUMN_TRANSLATION TEXT  
            );
        """
        db?.execSQL(CREATE_TABLE)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 2) {
            // 如果数据库版本小于2，进行升级，增加翻译列
            val ALTER_TABLE = "ALTER TABLE $TABLE_NAME ADD COLUMN $COLUMN_TRANSLATION TEXT"
            db?.execSQL(ALTER_TABLE)
        }
    }
    // 插入单词和翻译
    fun insertWordAndTranslation(word: String,translation: String?) {
        val db = this.writableDatabase
        val contentValues = ContentValues().apply {
            put(COLUMN_WORD, word)
            put(COLUMN_TRANSLATION,translation)
        }
        db.insert(TABLE_NAME, null, contentValues)
        db.close()
    }
    //根据单词的id来更改它的翻译
    fun updateTranslationById(id: Int, translation: String) {
        val db = this.writableDatabase
        val contentValues = ContentValues().apply {
            put(COLUMN_TRANSLATION, translation)  // 更新翻译列
        }

        // 使用ID更新对应单词的翻译
        db.update(TABLE_NAME, contentValues, "$COLUMN_ID = ?", arrayOf(id.toString()))
        db.close()
    }
    // 根据ID获取单个单词和翻译
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
    // 根据ID获取单个单词和翻译
    fun getTranslationById(id: Int): String? {
        val db = this.readableDatabase
        val cursor = db.query(
            TABLE_NAME, // 表名
            arrayOf(COLUMN_TRANSLATION), // 查询字段
            "$COLUMN_ID = ?", // 查询条件
            arrayOf(id.toString()), // 查询参数
            null, null, null // 不使用分组、排序、限制
        )


        var translation:String?=null
        if (cursor != null && cursor.moveToFirst()) {
            translation=cursor.getString(cursor.getColumnIndex(COLUMN_TRANSLATION))
            cursor.close()
        }
        db.close()
        return translation
    }

    // 查询所有单词及翻译
    fun getAllWords(): List<Word> {
        val wordList = mutableListOf<Word>()
        val db = this.readableDatabase

        // 查询数据库中的所有记录
        val cursor = db.query(TABLE_NAME, arrayOf(COLUMN_ID, COLUMN_WORD, COLUMN_TRANSLATION), null, null, null, null, null)

        if (cursor != null && cursor.moveToFirst()) {
            do {
                val id = cursor.getLong(cursor.getColumnIndex(COLUMN_ID))
                val word = cursor.getString(cursor.getColumnIndex(COLUMN_WORD))
                val translation = cursor.getString(cursor.getColumnIndex(COLUMN_TRANSLATION))
                wordList.add(Word(id, word, translation))
            } while (cursor.moveToNext())
        }

        cursor?.close()
        db.close()
        return wordList
    }
     fun deleteWord(id: Long){
        val db=writableDatabase
        db.delete(TABLE_NAME,"$COLUMN_ID =?", arrayOf(id.toString()))
        db.close()
    }

}