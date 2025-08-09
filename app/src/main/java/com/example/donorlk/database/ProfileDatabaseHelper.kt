package com.example.donorlk.database

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.donorlk.models.User

class ProfileDatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "profile.db"
        private const val DATABASE_VERSION = 1

        // Table name
        private const val TABLE_PROFILE = "profile"

        // Column names
        private const val COLUMN_UID = "uid"
        private const val COLUMN_NAME = "name"
        private const val COLUMN_EMAIL = "email"
        private const val COLUMN_ROLE = "role"
        private const val COLUMN_MOBILE = "mobile"
        private const val COLUMN_DATE_OF_BIRTH = "dateOfBirth"
        private const val COLUMN_GENDER = "gender"
        private const val COLUMN_BLOOD_GROUP = "bloodGroup"
        private const val COLUMN_NIC = "nic"
        private const val COLUMN_PROVINCE = "province"
        private const val COLUMN_CITY = "city"
        private const val COLUMN_CREATED_AT = "createdAt"
        private const val COLUMN_LAST_SYNC = "lastSync"
        private const val COLUMN_IS_SYNCED = "isSynced"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createTable = """
            CREATE TABLE $TABLE_PROFILE (
                $COLUMN_UID TEXT PRIMARY KEY,
                $COLUMN_NAME TEXT NOT NULL,
                $COLUMN_EMAIL TEXT NOT NULL,
                $COLUMN_ROLE TEXT NOT NULL,
                $COLUMN_MOBILE TEXT NOT NULL,
                $COLUMN_DATE_OF_BIRTH TEXT NOT NULL,
                $COLUMN_GENDER TEXT NOT NULL,
                $COLUMN_BLOOD_GROUP TEXT NOT NULL,
                $COLUMN_NIC TEXT NOT NULL,
                $COLUMN_PROVINCE TEXT NOT NULL,
                $COLUMN_CITY TEXT NOT NULL,
                $COLUMN_CREATED_AT INTEGER NOT NULL,
                $COLUMN_LAST_SYNC INTEGER DEFAULT 0,
                $COLUMN_IS_SYNCED INTEGER DEFAULT 0
            )
        """.trimIndent()

        db.execSQL(createTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_PROFILE")
        onCreate(db)
    }

    fun saveProfile(user: User, isSynced: Boolean = false): Boolean {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_UID, user.uid)
            put(COLUMN_NAME, user.name)
            put(COLUMN_EMAIL, user.email)
            put(COLUMN_ROLE, user.role)
            put(COLUMN_MOBILE, user.mobile)
            put(COLUMN_DATE_OF_BIRTH, user.dateOfBirth)
            put(COLUMN_GENDER, user.gender)
            put(COLUMN_BLOOD_GROUP, user.bloodGroup)
            put(COLUMN_NIC, user.nic)
            put(COLUMN_PROVINCE, user.province)
            put(COLUMN_CITY, user.city)
            put(COLUMN_CREATED_AT, user.createdAt)
            put(COLUMN_LAST_SYNC, System.currentTimeMillis())
            put(COLUMN_IS_SYNCED, if (isSynced) 1 else 0)
        }

        return try {
            val result = db.insertWithOnConflict(TABLE_PROFILE, null, values, SQLiteDatabase.CONFLICT_REPLACE)
            result != -1L
        } catch (e: Exception) {
            false
        } finally {
            db.close()
        }
    }

    fun getProfile(uid: String): User? {
        val db = this.readableDatabase
        val cursor = db.query(
            TABLE_PROFILE,
            null,
            "$COLUMN_UID = ?",
            arrayOf(uid),
            null,
            null,
            null
        )

        return try {
            if (cursor.moveToFirst()) {
                User(
                    uid = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_UID)),
                    name = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME)),
                    email = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EMAIL)),
                    role = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ROLE)),
                    mobile = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_MOBILE)),
                    dateOfBirth = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DATE_OF_BIRTH)),
                    gender = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_GENDER)),
                    bloodGroup = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_BLOOD_GROUP)),
                    nic = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NIC)),
                    province = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PROVINCE)),
                    city = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CITY)),
                    createdAt = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_CREATED_AT))
                )
            } else {
                null
            }
        } catch (e: Exception) {
            null
        } finally {
            cursor.close()
            db.close()
        }
    }

    fun getUnsyncedProfiles(): List<User> {
        val db = this.readableDatabase
        val profiles = mutableListOf<User>()
        val cursor = db.query(
            TABLE_PROFILE,
            null,
            "$COLUMN_IS_SYNCED = ?",
            arrayOf("0"),
            null,
            null,
            null
        )

        try {
            while (cursor.moveToNext()) {
                val user = User(
                    uid = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_UID)),
                    name = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME)),
                    email = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EMAIL)),
                    role = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ROLE)),
                    mobile = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_MOBILE)),
                    dateOfBirth = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DATE_OF_BIRTH)),
                    gender = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_GENDER)),
                    bloodGroup = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_BLOOD_GROUP)),
                    nic = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NIC)),
                    province = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PROVINCE)),
                    city = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CITY)),
                    createdAt = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_CREATED_AT))
                )
                profiles.add(user)
            }
        } catch (e: Exception) {
            // Handle error
        } finally {
            cursor.close()
            db.close()
        }

        return profiles
    }

    fun markAsSynced(uid: String): Boolean {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_IS_SYNCED, 1)
            put(COLUMN_LAST_SYNC, System.currentTimeMillis())
        }

        return try {
            val result = db.update(TABLE_PROFILE, values, "$COLUMN_UID = ?", arrayOf(uid))
            result > 0
        } catch (e: Exception) {
            false
        } finally {
            db.close()
        }
    }

    fun deleteProfile(uid: String): Boolean {
        val db = this.writableDatabase
        return try {
            val result = db.delete(TABLE_PROFILE, "$COLUMN_UID = ?", arrayOf(uid))
            result > 0
        } catch (e: Exception) {
            false
        } finally {
            db.close()
        }
    }
}
