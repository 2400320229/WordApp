package com.example.wordapp

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log

import java.lang.Error

//这个数据库里的单词id是从1开始的
data class Word_s(val id: Long, val word: String, val translation: String?,val error_count:Int,val Star:Int)
class WordDatabaseHelper(context: Context):SQLiteOpenHelper(context, DATABASE_NAME,null,
    DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "words.db"
        private const val DATABASE_VERSION = 8
        private const val TABLE_NAME = "words"
        private const val COLUMN_ID = "id"
        private const val COLUMN_WORD = "word"
        private const val COLUMN_TRANSLATION = "translation"
        private const val COLUMN_ERROR_COUNT = "error_count"  // 新增错误计数列// 新增翻译列
        private const val COLUMN_STAR = "star"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        val CREATE_TABLE = """
            CREATE TABLE $TABLE_NAME (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_WORD TEXT NOT NULL,
                $COLUMN_TRANSLATION TEXT ,
                $COLUMN_ERROR_COUNT INTEGER DEFAULT 0,
                $COLUMN_STAR INTEGER DEFAULT 0
            );
        """
        db?.execSQL(CREATE_TABLE)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 8) {
            // 如果数据库版本小于2，进行升级，增加翻译列
            val ALTER_TABLE = "ALTER TABLE $TABLE_NAME ADD COLUMN $COLUMN_STAR INTEGER DEFAULT 0"
            db?.execSQL(ALTER_TABLE)
        }
    }
    // 插入单词和翻译
    fun insertWord(word: String,translation: String?) {
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
    // 根据ID获取单个单词和翻译
    @SuppressLint("Range")
    fun getWordById(id: String?): String? {
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

    // 根据单个单词获取ID
    @SuppressLint("Range")
    fun getIdByWord(id: String?): Long? {
        val db = this.readableDatabase
        val cursor = db.query(
            TABLE_NAME, // 表名
            arrayOf(COLUMN_ID,COLUMN_WORD), // 查询字段
            "$COLUMN_WORD = ?", // 查询条件
            arrayOf(id.toString()), // 查询参数
            null, null, null // 不使用分组、排序、限制
        )

        var word: Long? = null

        if (cursor != null && cursor.moveToFirst()) {
            word = cursor.getLong(cursor.getColumnIndex(COLUMN_ID))
            cursor.close()
        }
        db.close()
        return word
    }
    // 根据ID获取单个翻译
    @SuppressLint("Range")
    fun getTranslationById(id: String?): String? {
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

    // 获取所有单词
    @SuppressLint("Range")
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
    // 增加错误次数
    fun incrementErrorCount(wordId: Int) {
        val db = this.writableDatabase
        val contentValues = ContentValues()
        contentValues.put(COLUMN_ERROR_COUNT, getErrorCount(wordId) + 1)  // 获取当前错误次数并加1
        db.update(TABLE_NAME, contentValues, "$COLUMN_ID = ?", arrayOf(wordId.toString()))
    }
    // 获取单词的错误次数
    @SuppressLint("Range")
    fun getErrorCount(wordId: Int): Int {
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT $COLUMN_ERROR_COUNT FROM $TABLE_NAME WHERE $COLUMN_ID = ?", arrayOf(wordId.toString()))
        var errorCount = 0
        if (cursor.moveToFirst()) {
            errorCount = cursor.getInt(cursor.getColumnIndex(COLUMN_ERROR_COUNT))
        }
        cursor.close()
        return errorCount
    }
    fun deleteErrorCount(wordId: Int) {
        val db = this.writableDatabase
        val contentValues = ContentValues()
        contentValues.put(COLUMN_ERROR_COUNT, 0)  // 获取当前错误次数并清零
        db.update(TABLE_NAME, contentValues, "$COLUMN_ID = ?", arrayOf(wordId.toString()))
    }
    // 获取错误次数最多的单词
    @SuppressLint("Range")
    fun getMostMistakenWords(limit: Int): List<String> {
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT $COLUMN_WORD FROM $TABLE_NAME ORDER BY $COLUMN_ERROR_COUNT DESC LIMIT ?", arrayOf(limit.toString()))
        val words = mutableListOf<String>()
        while (cursor.moveToNext()) {
            words.add(cursor.getString(cursor.getColumnIndex(COLUMN_WORD)))
        }
        cursor.close()
        return words
    }
    @SuppressLint("Range")
    fun getWordsWithErrorCountGreaterThanZero(): List<Word_s> {
        val db = this.readableDatabase
        val wordsList = mutableListOf<Word_s>()

        // SQL 查询语句，获取 error_count > 0 的单词，并按 error_count 降序排列
        val query = "SELECT * FROM $TABLE_NAME WHERE $COLUMN_ERROR_COUNT > 0 ORDER BY $COLUMN_ERROR_COUNT DESC"
        val cursor = db.rawQuery(query, null)
        // 遍历查询结果并转换为Word_s对象
        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getLong(cursor.getColumnIndex(COLUMN_ID))
                val word = cursor.getString(cursor.getColumnIndex(COLUMN_WORD))
                val translation = cursor.getString(cursor.getColumnIndex(COLUMN_TRANSLATION))
                val errorCount = cursor.getInt(cursor.getColumnIndex(COLUMN_ERROR_COUNT))
                val star = cursor.getInt(cursor.getColumnIndex(COLUMN_STAR))

                // 将每个查询到的单词对象添加到列表
                wordsList.add(Word_s(id, word, translation, errorCount,star))
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()

        return wordsList
    }
    @SuppressLint("Range")
    fun getWordsWithErrorCount(): List<String> {
        val wordsList = mutableListOf<String>()
        val db = this.readableDatabase
        try {
            // SQL 查询语句，获取 error_count > 0 的单词，没有排序
            val query = "SELECT * FROM $TABLE_NAME WHERE $COLUMN_ERROR_COUNT > 0"
            db.rawQuery(query, null).use { cursor ->
                if (cursor.moveToFirst()) {
                    do {
                        val word = cursor.getString(cursor.getColumnIndex(COLUMN_WORD))
                        wordsList.add(word)
                    } while (cursor.moveToNext())
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            db.close()
        }

        return wordsList
    }
    // 根据查询条件搜索单词
    fun searchWords(query: String): MutableList<Word_s> {
        val wordList = mutableListOf<Word_s>()
        val db = readableDatabase

        // 使用 try-catch 以确保资源能被正确关闭
        db.use {
            val cursor = it.query(
                TABLE_NAME,
                arrayOf(COLUMN_ID, COLUMN_WORD, COLUMN_TRANSLATION, COLUMN_ERROR_COUNT),
                "$COLUMN_WORD LIKE ?",
                arrayOf("$query%"),//$query%可以从首字母开始匹配
                null, null, null
            )

            // 游标不为 null 且有数据时，才进行遍历
            cursor?.use {
                while (it.moveToNext()) {
                    val idIndex = it.getColumnIndex(COLUMN_ID)
                    val wordIndex = it.getColumnIndex(COLUMN_WORD)
                    val translationIndex = it.getColumnIndex(COLUMN_TRANSLATION)
                    val errorCountIndex = it.getColumnIndex(COLUMN_ERROR_COUNT)

                    // 检查列索引是否有效
                    if (idIndex != -1 && wordIndex != -1 && translationIndex != -1 && errorCountIndex != -1) {
                        val id = it.getLong(idIndex)
                        val word = it.getString(wordIndex)
                        val translation = it.getString(translationIndex)
                        val errorCount = it.getInt(errorCountIndex)

                        // 将每个查询到的单词对象添加到列表
                        wordList.add(Word_s(id, word, translation, errorCount,0))
                    } else {
                        // 如果某些列索引无效，输出错误日志
                        Log.e("WordDatabase", "Invalid column index found in query result.")
                    }
                }
            }
        }

        return wordList
    }
    @SuppressLint("Range")
    fun getStarWords(): List<Word_s> {
        val wordsList = mutableListOf<Word_s>()
        val db = this.readableDatabase
        try {
            // SQL 查询语句，获取 error_count > 0 的单词，没有排序
            val query = "SELECT * FROM $TABLE_NAME WHERE $COLUMN_STAR > 0"
            db.rawQuery(query, null).use { cursor ->
                if (cursor.moveToFirst()) {
                    do {
                        val id = cursor.getLong(cursor.getColumnIndex(COLUMN_ID))
                        val word = cursor.getString(cursor.getColumnIndex(COLUMN_WORD))
                        val translation = cursor.getString(cursor.getColumnIndex(COLUMN_TRANSLATION))
                        val errorCount = cursor.getInt(cursor.getColumnIndex(COLUMN_ERROR_COUNT))
                        val star = cursor.getInt(cursor.getColumnIndex(COLUMN_STAR))

                        // 将每个查询到的单词对象添加到列表
                        wordsList.add(Word_s(id, word, translation, errorCount, star))
                    } while (cursor.moveToNext())
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            db.close()
        }

        return wordsList
    }
    // 增加收藏
    fun incrementStar(wordId: Int) {
        val db = this.writableDatabase
        val contentValues = ContentValues()
        contentValues.put(COLUMN_STAR, 1 )
        db.update(TABLE_NAME, contentValues, "$COLUMN_ID = ?", arrayOf(wordId.toString()))
    }
    // 增加收藏
    fun decreaseStar(wordId: Int) {
        val db = this.writableDatabase
        val contentValues = ContentValues()
        contentValues.put(COLUMN_STAR, 0 )
        db.update(TABLE_NAME, contentValues, "$COLUMN_ID = ?", arrayOf(wordId.toString()))
    }


}