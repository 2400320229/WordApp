package com.example.wordapp

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log

//这个数据库里的单词id是从1开始的
data class Word_s(
    val id: Long, val word: String, val translation: String?,
    val error_count:Int,
    val Star:Int, val learn:Int,
    val day :Int)
data class Clock_in_record(
    val id:Long, val is_checked_in: Int,val check_in_duration: Int,val check_in_date: String
)
data class Word(val word:String,val translation: String?)
class WordDatabaseHelper(context: Context):SQLiteOpenHelper(context, DATABASE_NAME,null,
    DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "words.db"
        private const val DATABASE_VERSION = 13
        private const val TABLE_NAME = "words"
        private const val COLUMN_ID = "id"
        private const val COLUMN_WORD = "word"
        private const val COLUMN_TRANSLATION = "translation"
        private const val COLUMN_ERROR_COUNT = "error_count"// 新增错误计数列// 新增翻译列
        private const val COLUMN_STAR = "star"
        private const val COLUMN_LEARN = "learn"
        private const val COLUMN_DAY = "day"
        // 新表的相关字段
        private const val CHECK_IN_TABLE_NAME = "check_in_records"
        private const val COLUMN_IS_CHECKED_IN = "is_checked_in" // 是否打卡
        private const val COLUMN_CHECK_IN_DURATION = "check_in_duration" // 打卡时长
        private const val COLUMN_CHECK_IN_DATE = "check_in_date" // 打卡日期
    }

    override fun onCreate(db: SQLiteDatabase?) {
        val CREATE_TABLE = """
            CREATE TABLE $TABLE_NAME (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_WORD TEXT NOT NULL,
                $COLUMN_TRANSLATION TEXT ,
                $COLUMN_ERROR_COUNT INTEGER DEFAULT 0,
                $COLUMN_STAR INTEGER DEFAULT 0,
                $COLUMN_LEARN INTEGER DEFAULT 0,
                $COLUMN_DAY INTEGER DEFAULT 0
            );
        """

        db?.execSQL(CREATE_TABLE)
        // 创建打卡记录表
        val CREATE_CHECK_IN_TABLE = """
            CREATE TABLE $CHECK_IN_TABLE_NAME (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_IS_CHECKED_IN INTEGER DEFAULT 0, 
                $COLUMN_CHECK_IN_DURATION INTEGER DEFAULT 0,
                $COLUMN_CHECK_IN_DATE TEXT
            );
        """
        db?.execSQL(CREATE_CHECK_IN_TABLE)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 13) {

            // 创建新表
            val CREATE_CHECK_IN_TABLE = """
                CREATE TABLE $CHECK_IN_TABLE_NAME (
                    $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                    $COLUMN_IS_CHECKED_IN INTEGER DEFAULT 0,
                    $COLUMN_CHECK_IN_DURATION INTEGER DEFAULT 0,
                    $COLUMN_CHECK_IN_DATE TEXT
                );
            """
            db?.execSQL(CREATE_CHECK_IN_TABLE)
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

    fun clearCheckInRecords() {
        val db = writableDatabase
        val deleteQuery = "DELETE FROM $CHECK_IN_TABLE_NAME"
        db.execSQL(deleteQuery)
        db.close()
    }
    fun insertCheckInRecord(is_checked_in:Int,check_in_duration:Int,check_in_date:String) {
        val db = this.writableDatabase
        val contentValues = ContentValues().apply {
            put(COLUMN_IS_CHECKED_IN,is_checked_in)
            put(COLUMN_CHECK_IN_DURATION,check_in_duration)
            put(COLUMN_CHECK_IN_DATE,check_in_date)
        }
        db.insert(CHECK_IN_TABLE_NAME, null, contentValues)
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
    // 获取所有打卡记录的方法
    @SuppressLint("Range")
    fun getAllCheckInRecords(): List<Clock_in_record> {
        val db = this.readableDatabase
        val cursor = db.query(
            CHECK_IN_TABLE_NAME, null, null, null,//不加条件，获取所有记录
            null, null, null
        )

        val records = mutableListOf<Clock_in_record>()
        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getLong(cursor.getColumnIndex(COLUMN_ID))
                val isCheckedIn = cursor.getInt(cursor.getColumnIndex(COLUMN_IS_CHECKED_IN))
                val duration = cursor.getInt(cursor.getColumnIndex(COLUMN_CHECK_IN_DURATION))
                val checkInDate = cursor.getString(cursor.getColumnIndex(COLUMN_CHECK_IN_DATE))
                records.add(Clock_in_record(id, isCheckedIn, duration, checkInDate))
                Log.d("id","${id}")
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return records
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
    @SuppressLint("Range")
    fun getWord_sById(id: String?): Word_s? {
        val db = this.readableDatabase
        val cursor = db.query(
            TABLE_NAME, // 表名
            arrayOf(COLUMN_ID, COLUMN_WORD, COLUMN_TRANSLATION, COLUMN_ERROR_COUNT, COLUMN_STAR, COLUMN_LEARN,
                COLUMN_DAY), // 查询字段
            "$COLUMN_ID = ?", // 查询条件
            arrayOf(id.toString()), // 查询参数
            null, null, null
        )
        var word: Word_s? = null
        if (cursor != null && cursor.moveToFirst()) {
            word = Word_s(
                cursor.getLong(cursor.getColumnIndex(COLUMN_ID)),
                cursor.getString(cursor.getColumnIndex(COLUMN_WORD)),
                cursor.getString(cursor.getColumnIndex(COLUMN_TRANSLATION)),
                cursor.getInt(cursor.getColumnIndex(COLUMN_ERROR_COUNT)),
                cursor.getInt(cursor.getColumnIndex(COLUMN_STAR)),
                cursor.getInt(cursor.getColumnIndex(COLUMN_LEARN)),
                cursor.getInt(cursor.getColumnIndex(COLUMN_DAY))

            )
        }
        cursor?.close()
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
        val query = "SELECT * FROM $TABLE_NAME WHERE $COLUMN_ERROR_COUNT > 0 AND $COLUMN_DAY = 0 ORDER BY $COLUMN_ERROR_COUNT DESC"
        val cursor = db.rawQuery(query, null)
        // 遍历查询结果并转换为Word_s对象
        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getLong(cursor.getColumnIndex(COLUMN_ID))
                val word = cursor.getString(cursor.getColumnIndex(COLUMN_WORD))
                val translation = cursor.getString(cursor.getColumnIndex(COLUMN_TRANSLATION))
                val errorCount = cursor.getInt(cursor.getColumnIndex(COLUMN_ERROR_COUNT))
                val star = cursor.getInt(cursor.getColumnIndex(COLUMN_STAR))
                val learn = cursor.getInt(cursor.getColumnIndex(COLUMN_LEARN))
                val day = cursor.getInt(cursor.getColumnIndex(COLUMN_DAY))

                // 将每个查询到的单词对象添加到列表
                wordsList.add(Word_s(id, word, translation, errorCount,star,learn,day))
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
                        wordList.add(Word_s(id, word, translation, errorCount,0,0,0))
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
                        val learn = cursor.getInt(cursor.getColumnIndex(COLUMN_LEARN))
                        val day = cursor.getInt(cursor.getColumnIndex(COLUMN_DAY))

                        // 将每个查询到的单词对象添加到列表
                        wordsList.add(Word_s(id, word, translation, errorCount, star,learn,day))
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
    // 减少收藏
    fun decreaseStar(wordId: Int) {
        val db = this.writableDatabase
        val contentValues = ContentValues()
        contentValues.put(COLUMN_STAR, 0 )
        db.update(TABLE_NAME, contentValues, "$COLUMN_ID = ?", arrayOf(wordId.toString()))
    }
    //增加学习
    fun incrementLearn(wordId: Int) {
        val db = this.writableDatabase
        val contentValues = ContentValues()
        contentValues.put(COLUMN_LEARN, 1 )
        db.update(TABLE_NAME, contentValues, "$COLUMN_ID = ?", arrayOf(wordId.toString()))
    }
    fun decreaseLearn(wordId: Int) {
        val db = this.writableDatabase
        val contentValues = ContentValues()
        contentValues.put(COLUMN_LEARN, 0 )
        db.update(TABLE_NAME, contentValues, "$COLUMN_ID = ?", arrayOf(wordId.toString()))
    }
    fun decreaseDay(wordId: Int) {
        val db = this.writableDatabase
        val contentValues = ContentValues()
        contentValues.put(COLUMN_DAY, 0 )
        db.update(TABLE_NAME, contentValues, "$COLUMN_ID = ?", arrayOf(wordId.toString()))
    }
    @SuppressLint("Range")
    fun getWordsByIdAndLearn( min:Int,max:Int): List<Word_s> {
        val words = mutableListOf<Word_s>()
        val db = readableDatabase

        // 查询 id 从 min 到 max，learn 为 0 的单词
        val query = "SELECT * FROM $TABLE_NAME WHERE $COLUMN_ID BETWEEN ? AND ? AND $COLUMN_LEARN = 0"
        val cursor = db.rawQuery(query,arrayOf(min.toString(), max.toString()))
        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getLong(cursor.getColumnIndex(COLUMN_ID))
                val word = cursor.getString(cursor.getColumnIndex(COLUMN_WORD))
                val translation = cursor.getString(cursor.getColumnIndex(COLUMN_TRANSLATION))
                val errorCount = cursor.getInt(cursor.getColumnIndex(COLUMN_ERROR_COUNT))
                val star = cursor.getInt(cursor.getColumnIndex(COLUMN_STAR))
                val learn = cursor.getInt(cursor.getColumnIndex(COLUMN_LEARN))
                val day = cursor.getInt(cursor.getColumnIndex(COLUMN_DAY))

                val wordEntry = Word_s(id, word, translation, errorCount, star, learn,day)
                words.add(wordEntry)
            } while (cursor.moveToNext())
        }

        cursor.close()
        db.close()

        return words
    }
    @SuppressLint("Range")
    fun getWordsLearn():List<Word_s> {
        val wordsList = mutableListOf<Word_s>()
        val db = this.readableDatabase
        try {
            // SQL 查询语句，获取 error_count > 0 的单词，没有排序
            val query = "SELECT * FROM $TABLE_NAME WHERE $COLUMN_LEARN > 0"
            db.rawQuery(query, null).use { cursor ->
                if (cursor.moveToFirst()) {
                    do {
                        val id = cursor.getLong(cursor.getColumnIndex(COLUMN_ID))
                        val word = cursor.getString(cursor.getColumnIndex(COLUMN_WORD))
                        val translation =
                            cursor.getString(cursor.getColumnIndex(COLUMN_TRANSLATION))
                        val errorCount = cursor.getInt(cursor.getColumnIndex(COLUMN_ERROR_COUNT))
                        val star = cursor.getInt(cursor.getColumnIndex(COLUMN_STAR))
                        val learn = cursor.getInt(cursor.getColumnIndex(COLUMN_LEARN))
                        val day = cursor.getInt(cursor.getColumnIndex(COLUMN_DAY))

                        // 将每个查询到的单词对象添加到列表
                        wordsList.add(Word_s(id, word, translation, errorCount, star, learn,day))
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
    @SuppressLint("Range")
    fun getBeforeErrorWord(): List<Word> {
        val wordsList = mutableListOf<Word>()
        val db = this.readableDatabase
        try {
            // SQL 查询语句，获取 error_count > 0 的单词，没有排序
            val query = "SELECT * FROM $TABLE_NAME WHERE $COLUMN_ERROR_COUNT > 0 AND $COLUMN_DAY >0"
            db.rawQuery(query, null).use { cursor ->
                if (cursor.moveToFirst()) {
                    do {
                        val word = cursor.getString(cursor.getColumnIndex(COLUMN_WORD))
                        val translation = cursor.getString(cursor.getColumnIndex(COLUMN_TRANSLATION))
                        wordsList.add(Word(word,translation))
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
    @SuppressLint("Range")
    fun getTodayErrorWord(): List<Word> {
        val wordsList = mutableListOf<Word>()
        val db = this.readableDatabase
        try {
            val query = "SELECT * FROM $TABLE_NAME WHERE $COLUMN_ERROR_COUNT > 0 AND $COLUMN_DAY = 0"
            db.rawQuery(query, null).use { cursor ->
                if (cursor.moveToFirst()) {
                    do {
                        val word = cursor.getString(cursor.getColumnIndex(COLUMN_WORD))
                        val translation = cursor.getString(cursor.getColumnIndex(COLUMN_TRANSLATION))
                        wordsList.add(Word(word,translation))
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
    @SuppressLint("Range")
    fun getTodayLearnWord(): List<String> {
        val wordsList = mutableListOf<String>()
        val db = this.readableDatabase
        try {
            val query = "SELECT * FROM $TABLE_NAME WHERE $COLUMN_LEARN > 0 AND $COLUMN_DAY = 0"
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
    @SuppressLint("Range")
    fun getWordsByDayAndLearnGreaterThanZero(day: Int): List<Word_s> {
        val db = readableDatabase
        val wordsList = mutableListOf<Word_s>()

        // 查询语句，查找指定 day 且 learn > 0 的单词
        val query = """
        SELECT * FROM $TABLE_NAME 
        WHERE $COLUMN_DAY = ? AND $COLUMN_LEARN > 0
    """

        // 使用 SQLite 的 rawQuery 执行 SQL 查询
        val cursor = db.rawQuery(query, arrayOf(day.toString()))

        // 遍历游标并将符合条件的记录添加到列表中
        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getLong(cursor.getColumnIndex(COLUMN_ID))
                val word = cursor.getString(cursor.getColumnIndex(COLUMN_WORD))
                val translation = cursor.getString(cursor.getColumnIndex(COLUMN_TRANSLATION))
                val errorCount = cursor.getInt(cursor.getColumnIndex(COLUMN_ERROR_COUNT))
                val star = cursor.getInt(cursor.getColumnIndex(COLUMN_STAR))
                val learn = cursor.getInt(cursor.getColumnIndex(COLUMN_LEARN))
                val day = cursor.getInt(cursor.getColumnIndex(COLUMN_DAY))

                // 将每一行数据转换为 Word_s 对象
                wordsList.add(Word_s(id, word, translation, errorCount, star, learn, day))

            } while (cursor.moveToNext())
        }

        cursor.close()
        db.close()

        return wordsList
    }
    @SuppressLint("Range")
    //给今天Learn的单词的day+1
    fun updateDayForLearnWords() {

        val db = this.writableDatabase
        // 执行更新操作
        try {


            // 使用 rawQuery 执行 SQL 更新操作
            val updateQuery = "UPDATE $TABLE_NAME SET $COLUMN_DAY = $COLUMN_DAY + 1 WHERE $COLUMN_LEARN = 1"
            db.execSQL(updateQuery)

            db.close()
            Log.d("Database", "day +1 rows updated.")
        } catch (e: Exception) {
            Log.e("DatabaseError", "Error updating day for error words: ${e.message}", e)
        } finally {
            db.close()  // 确保数据库连接关闭
        }
    }
}


