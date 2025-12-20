package com.example.hostelconnect;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "HostelConnect.db";
    private static final int DATABASE_VERSION = 8;  // Incremented version

    // Hosteller table
    private static final String TABLE_HOSTELLER = "hosteller";
    private static final String COL_NAME = "name";
    private static final String COL_PHONE = "phone";
    private static final String COL_EMAIL = "email";
    private static final String COL_ADDRESS = "address";
    private static final String COL_PASSWORD = "password";
    private static final String COL_ROOM_NUMBER = "room_number";
    private static final String COL_RECEIPT = "receipt";
    private static final String COL_IMAGE = "image";

    // Entry/Exit table
    private static final String TABLE_ENTRY_EXIT = "entry_exit";
    private static final String COL_ID = "id";
    private static final String COL_EE_PHONE = "phone";
    private static final String COL_TYPE = "type";
    private static final String COL_TIMESTAMP = "timestamp";
    private static final String COL_REASON = "reason";

    // Payments table
    private static final String TABLE_PAYMENTS = "payments";
    private static final String COL_PAYMENT_ID = "id";
    private static final String COL_P_PHONE = "phone";
    private static final String COL_AMOUNT = "amount";
    private static final String COL_DATE = "date";
    private static final String COL_STATUS = "status";
    private static final String COL_MONTH = "month";

    // Notices table
    private static final String TABLE_NOTICES = "notices";
    private static final String COL_NOTICE_ID = "id";
    private static final String COL_TITLE = "title";
    private static final String COL_MESSAGE = "message";
    private static final String COL_POSTED_BY = "posted_by";
    private static final String COL_POSTED_DATE = "posted_date";

    // Complaints table
    private static final String TABLE_COMPLAINTS = "complaints";
    private static final String COL_COMPLAINT_ID = "id";
    private static final String COL_C_PHONE = "phone";
    private static final String COL_C_NAME = "name";
    private static final String COL_COMPLAINT = "complaint";
    private static final String COL_C_DATE = "date";
    private static final String COL_C_STATUS = "status";

    // Daily Attendance table
    private static final String TABLE_DAILY_ATTENDANCE = "daily_attendance";

    // Owner Table
    private static final String TABLE_OWNER = "owners";
    private static final String OWNER_ID = "id";
    private static final String OWNER_NAME = "name";
    private static final String OWNER_EMAIL = "email";
    private static final String OWNER_PHONE = "phone";
    private static final String OWNER_PROPERTY_NAME = "property_name";
    private static final String OWNER_PASSWORD = "password";
    private static final String OWNER_CREATED_AT = "created_at";

    // Security Table
    private static final String TABLE_SECURITY = "security";
    private static final String SECURITY_ID = "id";
    private static final String SECURITY_NAME = "name";
    private static final String SECURITY_SECURITY_ID = "security_id";
    private static final String SECURITY_PHONE = "phone";
    private static final String SECURITY_SHIFT = "shift";
    private static final String SECURITY_PASSWORD = "password";
    private static final String SECURITY_CREATED_AT = "created_at";

    // Laundry Table
    private static final String TABLE_LAUNDRY = "laundry_service";
    private static final String LAUNDRY_ID = "id";
    private static final String LAUNDRY_PHONE = "phone";
    private static final String LAUNDRY_ITEMS = "items";
    private static final String LAUNDRY_QUANTITY = "quantity";
    private static final String LAUNDRY_PICKUP_DATE = "pickup_date";
    private static final String LAUNDRY_DELIVERY_DATE = "delivery_date";
    private static final String LAUNDRY_STATUS = "status";
    private static final String LAUNDRY_AMOUNT = "amount";
    private static final String LAUNDRY_CREATED_AT = "created_at";

    // Feedback Table
    private static final String TABLE_FEEDBACK = "feedback";
    private static final String FEEDBACK_ID = "id";
    private static final String FEEDBACK_PHONE = "phone";
    private static final String FEEDBACK_NAME = "name";
    private static final String FEEDBACK_CATEGORY = "category";
    private static final String FEEDBACK_RATING = "rating";
    private static final String FEEDBACK_MESSAGE = "message";
    private static final String FEEDBACK_DATE = "date";
    private static final String FEEDBACK_STATUS = "status";

    // Emergency Contacts Table
    private static final String TABLE_EMERGENCY = "emergency_contacts";
    private static final String EMERGENCY_ID = "id";
    private static final String EMERGENCY_NAME = "name";
    private static final String EMERGENCY_DESIGNATION = "designation";
    private static final String EMERGENCY_PHONE = "phone";
    private static final String EMERGENCY_TYPE = "type";

    // Leave Requests Table
    private static final String TABLE_LEAVE = "leave_requests";
    private static final String LEAVE_ID = "id";
    private static final String LEAVE_PHONE = "phone";
    private static final String LEAVE_NAME = "name";
    private static final String LEAVE_FROM_DATE = "from_date";
    private static final String LEAVE_TO_DATE = "to_date";
    private static final String LEAVE_REASON = "reason";
    private static final String LEAVE_STATUS = "status";
    private static final String LEAVE_REQUESTED_DATE = "requested_date";
    private static final String LEAVE_RESPONSE_DATE = "response_date";

    // Mess Menu Table
    private static final String TABLE_MESS_MENU = "mess_menu";
    private static final String MENU_ID = "id";
    private static final String MENU_DAY = "day";
    private static final String MENU_MEAL_TYPE = "meal_type";
    private static final String MENU_ITEMS = "items";
    private static final String MENU_SPECIAL = "special_note";

    // Room Maintenance Table
    private static final String TABLE_MAINTENANCE = "room_maintenance";
    private static final String MAINTENANCE_ID = "id";
    private static final String MAINTENANCE_PHONE = "phone";
    private static final String MAINTENANCE_ROOM = "room_number";
    private static final String MAINTENANCE_ISSUE = "issue";
    private static final String MAINTENANCE_DESCRIPTION = "description";
    private static final String MAINTENANCE_STATUS = "status";
    private static final String MAINTENANCE_PRIORITY = "priority";
    private static final String MAINTENANCE_REPORTED_DATE = "reported_date";
    private static final String MAINTENANCE_RESOLVED_DATE = "resolved_date";

    // Unified Visitors Table (for hosteller visitor management)
    private static final String TABLE_VISITORS = "visitors";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create hosteller table
        String createHostellerTable = "CREATE TABLE " + TABLE_HOSTELLER + " (" +
                COL_PHONE + " TEXT PRIMARY KEY, " +
                COL_NAME + " TEXT, " +
                COL_EMAIL + " TEXT, " +
                COL_ADDRESS + " TEXT, " +
                COL_PASSWORD + " TEXT, " +
                COL_ROOM_NUMBER + " TEXT, " +
                COL_RECEIPT + " TEXT, " +
                COL_IMAGE + " BLOB)";
        db.execSQL(createHostellerTable);

        // Create entry/exit table
        String createEntryExitTable = "CREATE TABLE " + TABLE_ENTRY_EXIT + " (" +
                COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_EE_PHONE + " TEXT, " +
                COL_TYPE + " TEXT, " +
                COL_TIMESTAMP + " TEXT, " +
                COL_REASON + " TEXT)";
        db.execSQL(createEntryExitTable);

        // Create payments table
        String createPaymentsTable = "CREATE TABLE " + TABLE_PAYMENTS + " (" +
                COL_PAYMENT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_P_PHONE + " TEXT, " +
                COL_AMOUNT + " TEXT, " +
                COL_DATE + " TEXT, " +
                COL_STATUS + " TEXT, " +
                COL_MONTH + " TEXT)";
        db.execSQL(createPaymentsTable);

        // Create notices table
        String createNoticesTable = "CREATE TABLE " + TABLE_NOTICES + " (" +
                COL_NOTICE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_TITLE + " TEXT, " +
                COL_MESSAGE + " TEXT, " +
                COL_POSTED_BY + " TEXT, " +
                COL_POSTED_DATE + " TEXT)";
        db.execSQL(createNoticesTable);

        // Create complaints table
        String createComplaintsTable = "CREATE TABLE " + TABLE_COMPLAINTS + " (" +
                COL_COMPLAINT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_C_PHONE + " TEXT, " +
                COL_C_NAME + " TEXT, " +
                COL_COMPLAINT + " TEXT, " +
                COL_C_DATE + " TEXT, " +
                COL_C_STATUS + " TEXT)";
        db.execSQL(createComplaintsTable);

        // Create daily attendance table
        String createDailyAttendanceTable = "CREATE TABLE " + TABLE_DAILY_ATTENDANCE + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "phone TEXT NOT NULL, " +
                "date TEXT NOT NULL, " +
                "status TEXT NOT NULL, " +
                "check_time TEXT, " +
                "entry_time TEXT, " +
                "exit_time TEXT, " +
                "total_entries INTEGER DEFAULT 0, " +
                "total_exits INTEGER DEFAULT 0, " +
                "UNIQUE(phone, date))";
        db.execSQL(createDailyAttendanceTable);

        // Create Owner Table
        String createOwnerTable = "CREATE TABLE " + TABLE_OWNER + " (" +
                OWNER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                OWNER_NAME + " TEXT NOT NULL, " +
                OWNER_EMAIL + " TEXT UNIQUE NOT NULL, " +
                OWNER_PHONE + " TEXT NOT NULL, " +
                OWNER_PROPERTY_NAME + " TEXT NOT NULL, " +
                OWNER_PASSWORD + " TEXT NOT NULL, " +
                OWNER_CREATED_AT + " DATETIME DEFAULT CURRENT_TIMESTAMP)";
        db.execSQL(createOwnerTable);

        // Create Security Table
        String createSecurityTable = "CREATE TABLE " + TABLE_SECURITY + " (" +
                SECURITY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                SECURITY_NAME + " TEXT NOT NULL, " +
                SECURITY_SECURITY_ID + " TEXT UNIQUE NOT NULL, " +
                SECURITY_PHONE + " TEXT NOT NULL, " +
                SECURITY_SHIFT + " TEXT NOT NULL, " +
                SECURITY_PASSWORD + " TEXT NOT NULL, " +
                SECURITY_CREATED_AT + " DATETIME DEFAULT CURRENT_TIMESTAMP)";
        db.execSQL(createSecurityTable);

        // Create Laundry Table
        String createLaundryTable = "CREATE TABLE " + TABLE_LAUNDRY + " (" +
                LAUNDRY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                LAUNDRY_PHONE + " TEXT NOT NULL, " +
                LAUNDRY_ITEMS + " TEXT NOT NULL, " +
                LAUNDRY_QUANTITY + " INTEGER NOT NULL, " +
                LAUNDRY_PICKUP_DATE + " TEXT NOT NULL, " +
                LAUNDRY_DELIVERY_DATE + " TEXT, " +
                LAUNDRY_STATUS + " TEXT DEFAULT 'Pending', " +
                LAUNDRY_AMOUNT + " REAL, " +
                LAUNDRY_CREATED_AT + " DATETIME DEFAULT CURRENT_TIMESTAMP)";
        db.execSQL(createLaundryTable);

        // Create UNIFIED Visitors Table (for hosteller visitor management)
        String createVisitorsTable =
                "CREATE TABLE IF NOT EXISTS " + TABLE_VISITORS + " (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "hosteller_phone TEXT NOT NULL, " +
                        "hosteller_name TEXT NOT NULL, " +
                        "hosteller_room TEXT, " +
                        "visitor_name TEXT NOT NULL, " +
                        "visitor_phone TEXT NOT NULL, " +
                        "purpose TEXT, " +
                        "visit_date TEXT NOT NULL, " +
                        "visit_time TEXT NOT NULL, " +
                        "status TEXT DEFAULT 'Pending', " +
                        "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                        "approved_by TEXT, " +
                        "approved_at TIMESTAMP, " +
                        "check_in_time TIMESTAMP, " +
                        "check_out_time TIMESTAMP, " +
                        "FOREIGN KEY(hosteller_phone) REFERENCES " + TABLE_HOSTELLER + "(" + COL_PHONE + "))";
        db.execSQL(createVisitorsTable);

        // Create Feedback Table
        String createFeedbackTable = "CREATE TABLE " + TABLE_FEEDBACK + " (" +
                FEEDBACK_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                FEEDBACK_PHONE + " TEXT NOT NULL, " +
                FEEDBACK_NAME + " TEXT NOT NULL, " +
                FEEDBACK_CATEGORY + " TEXT NOT NULL, " +
                FEEDBACK_RATING + " INTEGER NOT NULL, " +
                FEEDBACK_MESSAGE + " TEXT, " +
                FEEDBACK_DATE + " TEXT NOT NULL, " +
                FEEDBACK_STATUS + " TEXT DEFAULT 'Submitted')";
        db.execSQL(createFeedbackTable);

        // Create Emergency Table
        String createEmergencyTable = "CREATE TABLE " + TABLE_EMERGENCY + " (" +
                EMERGENCY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                EMERGENCY_NAME + " TEXT NOT NULL, " +
                EMERGENCY_DESIGNATION + " TEXT NOT NULL, " +
                EMERGENCY_PHONE + " TEXT NOT NULL, " +
                EMERGENCY_TYPE + " TEXT NOT NULL)";
        db.execSQL(createEmergencyTable);

        // Create Leave Table - REMOVED DUPLICATE
        String createLeaveTable = "CREATE TABLE " + TABLE_LEAVE + " (" +
                LEAVE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                LEAVE_PHONE + " TEXT NOT NULL, " +
                LEAVE_NAME + " TEXT NOT NULL, " +
                LEAVE_FROM_DATE + " TEXT NOT NULL, " +
                LEAVE_TO_DATE + " TEXT NOT NULL, " +
                LEAVE_REASON + " TEXT NOT NULL, " +
                LEAVE_STATUS + " TEXT DEFAULT 'Pending', " +
                LEAVE_REQUESTED_DATE + " TEXT NOT NULL, " +
                LEAVE_RESPONSE_DATE + " TEXT)";
        db.execSQL(createLeaveTable);

        // Create Mess Menu Table
        String createMessMenuTable = "CREATE TABLE " + TABLE_MESS_MENU + " (" +
                MENU_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                MENU_DAY + " TEXT NOT NULL, " +
                MENU_MEAL_TYPE + " TEXT NOT NULL, " +
                MENU_ITEMS + " TEXT NOT NULL, " +
                MENU_SPECIAL + " TEXT, " +
                "UNIQUE(" + MENU_DAY + ", " + MENU_MEAL_TYPE + "))";
        db.execSQL(createMessMenuTable);

        // Create Maintenance Table
        String createMaintenanceTable = "CREATE TABLE " + TABLE_MAINTENANCE + " (" +
                MAINTENANCE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                MAINTENANCE_PHONE + " TEXT NOT NULL, " +
                MAINTENANCE_ROOM + " TEXT NOT NULL, " +
                MAINTENANCE_ISSUE + " TEXT NOT NULL, " +
                MAINTENANCE_DESCRIPTION + " TEXT, " +
                MAINTENANCE_STATUS + " TEXT DEFAULT 'Pending', " +
                MAINTENANCE_PRIORITY + " TEXT DEFAULT 'Medium', " +
                MAINTENANCE_REPORTED_DATE + " TEXT NOT NULL, " +
                MAINTENANCE_RESOLVED_DATE + " TEXT)";
        db.execSQL(createMaintenanceTable);

        // Insert sample data
        insertSampleNotices(db);
        insertSampleEmergencyContacts(db);
        insertSampleMessMenu(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 7) {
            // Add new columns to existing visitors table if upgrading
            try {
                db.execSQL("ALTER TABLE visitors ADD COLUMN hosteller_name TEXT NOT NULL DEFAULT ''");
                db.execSQL("ALTER TABLE visitors ADD COLUMN hosteller_room TEXT");
                db.execSQL("ALTER TABLE visitors ADD COLUMN approved_by TEXT");
                db.execSQL("ALTER TABLE visitors ADD COLUMN approved_at TIMESTAMP");
                db.execSQL("ALTER TABLE visitors ADD COLUMN check_in_time TIMESTAMP");
                db.execSQL("ALTER TABLE visitors ADD COLUMN check_out_time TIMESTAMP");
            } catch (Exception e) {
                // If upgrade fails, recreate tables
                db.execSQL("DROP TABLE IF EXISTS " + TABLE_HOSTELLER);
                db.execSQL("DROP TABLE IF EXISTS " + TABLE_ENTRY_EXIT);
                db.execSQL("DROP TABLE IF EXISTS " + TABLE_PAYMENTS);
                db.execSQL("DROP TABLE IF EXISTS " + TABLE_NOTICES);
                db.execSQL("DROP TABLE IF EXISTS " + TABLE_COMPLAINTS);
                db.execSQL("DROP TABLE IF EXISTS " + TABLE_DAILY_ATTENDANCE);
                db.execSQL("DROP TABLE IF EXISTS " + TABLE_OWNER);
                db.execSQL("DROP TABLE IF EXISTS " + TABLE_SECURITY);
                db.execSQL("DROP TABLE IF EXISTS " + TABLE_VISITORS);
                db.execSQL("DROP TABLE IF EXISTS " + TABLE_LAUNDRY);
                db.execSQL("DROP TABLE IF EXISTS " + TABLE_FEEDBACK);
                db.execSQL("DROP TABLE IF EXISTS " + TABLE_EMERGENCY);
                db.execSQL("DROP TABLE IF EXISTS " + TABLE_LEAVE);
                db.execSQL("DROP TABLE IF EXISTS " + TABLE_MESS_MENU);
                db.execSQL("DROP TABLE IF EXISTS " + TABLE_MAINTENANCE);
                onCreate(db);
            }
        }

        if (oldVersion < 8) {
            // Add new columns for existing installations
            try {
                // Check if column exists before adding
                Cursor cursor = db.rawQuery("PRAGMA table_info(" + TABLE_HOSTELLER + ")", null);
                boolean bloodGroupExists = false;
                boolean registrationDateExists = false;
                boolean courseExists = false;
                boolean yearExists = false;
                boolean branchExists = false;
                boolean rollNumberExists = false;
                boolean hostelNameExists = false;
                boolean blockNameExists = false;
                boolean floorExists = false;
                boolean roomTypeExists = false;
                boolean emergencyContactExists = false;
                boolean parentNameExists = false;
                boolean parentPhoneExists = false;
                boolean guardianNameExists = false;
                boolean guardianPhoneExists = false;

                if (cursor != null && cursor.moveToFirst()) {
                    do {
                        String columnName = cursor.getString(cursor.getColumnIndex("name"));
                        if ("blood_group".equals(columnName)) bloodGroupExists = true;
                        if ("registration_date".equals(columnName)) registrationDateExists = true;
                        if ("course".equals(columnName)) courseExists = true;
                        if ("year".equals(columnName)) yearExists = true;
                        if ("branch".equals(columnName)) branchExists = true;
                        if ("roll_number".equals(columnName)) rollNumberExists = true;
                        if ("hostel_name".equals(columnName)) hostelNameExists = true;
                        if ("block_name".equals(columnName)) blockNameExists = true;
                        if ("floor".equals(columnName)) floorExists = true;
                        if ("room_type".equals(columnName)) roomTypeExists = true;
                        if ("emergency_contact".equals(columnName)) emergencyContactExists = true;
                        if ("parent_name".equals(columnName)) parentNameExists = true;
                        if ("parent_phone".equals(columnName)) parentPhoneExists = true;
                        if ("guardian_name".equals(columnName)) guardianNameExists = true;
                        if ("guardian_phone".equals(columnName)) guardianPhoneExists = true;
                    } while (cursor.moveToNext());
                    cursor.close();
                }

                // Add columns only if they don't exist
                if (!bloodGroupExists) {
                    db.execSQL("ALTER TABLE " + TABLE_HOSTELLER + " ADD COLUMN blood_group TEXT");
                }
                if (!registrationDateExists) {
                    db.execSQL("ALTER TABLE " + TABLE_HOSTELLER + " ADD COLUMN registration_date TEXT");
                }
                if (!courseExists) {
                    db.execSQL("ALTER TABLE " + TABLE_HOSTELLER + " ADD COLUMN course TEXT");
                }
                if (!yearExists) {
                    db.execSQL("ALTER TABLE " + TABLE_HOSTELLER + " ADD COLUMN year TEXT");
                }
                if (!branchExists) {
                    db.execSQL("ALTER TABLE " + TABLE_HOSTELLER + " ADD COLUMN branch TEXT");
                }
                if (!rollNumberExists) {
                    db.execSQL("ALTER TABLE " + TABLE_HOSTELLER + " ADD COLUMN roll_number TEXT");
                }
                if (!hostelNameExists) {
                    db.execSQL("ALTER TABLE " + TABLE_HOSTELLER + " ADD COLUMN hostel_name TEXT");
                }
                if (!blockNameExists) {
                    db.execSQL("ALTER TABLE " + TABLE_HOSTELLER + " ADD COLUMN block_name TEXT");
                }
                if (!floorExists) {
                    db.execSQL("ALTER TABLE " + TABLE_HOSTELLER + " ADD COLUMN floor TEXT");
                }
                if (!roomTypeExists) {
                    db.execSQL("ALTER TABLE " + TABLE_HOSTELLER + " ADD COLUMN room_type TEXT");
                }
                if (!emergencyContactExists) {
                    db.execSQL("ALTER TABLE " + TABLE_HOSTELLER + " ADD COLUMN emergency_contact TEXT");
                }
                if (!parentNameExists) {
                    db.execSQL("ALTER TABLE " + TABLE_HOSTELLER + " ADD COLUMN parent_name TEXT");
                }
                if (!parentPhoneExists) {
                    db.execSQL("ALTER TABLE " + TABLE_HOSTELLER + " ADD COLUMN parent_phone TEXT");
                }
                if (!guardianNameExists) {
                    db.execSQL("ALTER TABLE " + TABLE_HOSTELLER + " ADD COLUMN guardian_name TEXT");
                }
                if (!guardianPhoneExists) {
                    db.execSQL("ALTER TABLE " + TABLE_HOSTELLER + " ADD COLUMN guardian_phone TEXT");
                }

                Log.d("DatabaseHelper", "Successfully upgraded database to version 8");
            } catch (Exception e) {
                Log.e("DatabaseHelper", "Error upgrading database to version 8", e);
            }
        }
    }

    // ==================== HOSTELLER METHODS ====================

    public boolean insertHosteller(String name, String phone, String email, String address,
                                   String roomNumber, String password, String receipt, byte[] image) {
        SQLiteDatabase db = null;
        try {
            db = this.getWritableDatabase();
            ContentValues contentValues = new ContentValues();
            contentValues.put(COL_NAME, name);
            contentValues.put(COL_PHONE, phone);
            contentValues.put(COL_EMAIL, email);
            contentValues.put(COL_ADDRESS, address);
            contentValues.put(COL_PASSWORD, password);
            contentValues.put(COL_ROOM_NUMBER, roomNumber);
            contentValues.put(COL_RECEIPT, receipt);
            contentValues.put(COL_IMAGE, image);

            Log.d("DatabaseHelper", "Attempting to insert hosteller: " + name + ", " + phone);

            long result = db.insert(TABLE_HOSTELLER, null, contentValues);

            Log.d("DatabaseHelper", "Insert result: " + result);

            if (result != -1) {
                Log.d("DatabaseHelper", "Successfully inserted hosteller");
                return true;
            } else {
                Log.e("DatabaseHelper", "Insert failed - result was -1");
                return false;
            }
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Exception during insert: " + e.getMessage(), e);
            return false;
        }
    }

    public Cursor checkHostellerLogin(String phone, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_HOSTELLER +
                        " WHERE " + COL_PHONE + "=? AND " + COL_PASSWORD + "=?",
                new String[]{phone, password});
    }

    public Cursor getHostellerByPhone(String phone) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_HOSTELLER +
                " WHERE " + COL_PHONE + "=?", new String[]{phone});
    }

    public Cursor getAllHostellers() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_HOSTELLER, null);
    }

    public boolean checkPhoneExists(String phone) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_HOSTELLER +
                " WHERE " + COL_PHONE + "=?", new String[]{phone});
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
    }

    public boolean updateHostellerPassword(String phone, String newPassword) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_PASSWORD, newPassword);

        int result = db.update(TABLE_HOSTELLER, contentValues,
                COL_PHONE + "=?", new String[]{phone});
        return result > 0;
    }

    public boolean updateHostellerProfile(String phone, String name, String email,
                                          String address, String bloodGroup,
                                          String emergencyContact, String parentName,
                                          String parentPhone, String guardianName,
                                          String guardianPhone, String course, String year,
                                          String branch, String rollNumber,
                                          String hostelName, String blockName, String floor,
                                          String roomType, byte[] image) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        // Required fields
        values.put("name", name);
        values.put("email", email);

        // Personal Information
        if (address != null && !address.isEmpty()) {
            values.put("address", address);
        }
        if (bloodGroup != null && !bloodGroup.isEmpty()) {
            values.put("blood_group", bloodGroup);
        }

        // Emergency Contacts
        if (emergencyContact != null && !emergencyContact.isEmpty()) {
            values.put("emergency_contact", emergencyContact);
        }
        if (parentName != null && !parentName.isEmpty()) {
            values.put("parent_name", parentName);
        }
        if (parentPhone != null && !parentPhone.isEmpty()) {
            values.put("parent_phone", parentPhone);
        }
        if (guardianName != null && !guardianName.isEmpty()) {
            values.put("guardian_name", guardianName);
        }
        if (guardianPhone != null && !guardianPhone.isEmpty()) {
            values.put("guardian_phone", guardianPhone);
        }

        // Academic Information
        if (course != null && !course.isEmpty()) {
            values.put("course", course);
        }
        if (year != null && !year.isEmpty()) {
            values.put("year", year);
        }
        if (branch != null && !branch.isEmpty()) {
            values.put("branch", branch);
        }
        if (rollNumber != null && !rollNumber.isEmpty()) {
            values.put("roll_number", rollNumber);
        }

        // Hostel Information
        if (hostelName != null && !hostelName.isEmpty()) {
            values.put("hostel_name", hostelName);
        }
        if (blockName != null && !blockName.isEmpty()) {
            values.put("block_name", blockName);
        }
        if (floor != null && !floor.isEmpty()) {
            values.put("floor", floor);
        }
        if (roomType != null && !roomType.isEmpty()) {
            values.put("room_type", roomType);
        }

        // Update profile image if changed
        if (image != null) {
            values.put("image", image);
        }

        int rowsAffected = db.update(TABLE_HOSTELLER, values, "phone = ?", new String[]{phone});
        db.close();

        return rowsAffected > 0;
    }

    // ==================== ENTRY/EXIT METHODS ====================

    public boolean recordEntryExit(String phone, String type, String reason) {
        SQLiteDatabase db = this.getWritableDatabase();
        String currentTimestamp = getCurrentTimestamp();

        ContentValues values = new ContentValues();
        values.put(COL_EE_PHONE, phone);
        values.put(COL_TYPE, type);
        values.put(COL_TIMESTAMP, currentTimestamp);

        if (reason != null && !reason.isEmpty()) {
            values.put(COL_REASON, reason);
        }

        long result = db.insert(TABLE_ENTRY_EXIT, null, values);

        if (result != -1) {
            String today = getCurrentDate();
            updateDailyAttendanceRecord(phone, today, type, currentTimestamp);
            return true;
        }

        return false;
    }

    private void updateDailyAttendanceRecord(String phone, String date, String type, String timestamp) {
        SQLiteDatabase db = this.getWritableDatabase();

        Cursor cursor = db.rawQuery(
                "SELECT * FROM " + TABLE_DAILY_ATTENDANCE + " WHERE phone = ? AND date = ?",
                new String[]{phone, date}
        );

        if (cursor.moveToFirst()) {
            ContentValues values = new ContentValues();
            values.put("status", "PRESENT");
            values.put("check_time", timestamp);

            int totalEntries = cursor.getInt(cursor.getColumnIndex("total_entries"));
            int totalExits = cursor.getInt(cursor.getColumnIndex("total_exits"));

            if (type.equals("ENTRY")) {
                values.put("total_entries", totalEntries + 1);

                String entryTime = cursor.getString(cursor.getColumnIndex("entry_time"));
                if (entryTime == null || entryTime.isEmpty()) {
                    values.put("entry_time", timestamp);
                }
            } else if (type.equals("EXIT")) {
                values.put("total_exits", totalExits + 1);
                values.put("exit_time", timestamp);
            }

            db.update(TABLE_DAILY_ATTENDANCE, values, "phone = ? AND date = ?",
                    new String[]{phone, date});
        } else {
            ContentValues values = new ContentValues();
            values.put("phone", phone);
            values.put("date", date);
            values.put("status", "PRESENT");
            values.put("check_time", timestamp);

            if (type.equals("ENTRY")) {
                values.put("entry_time", timestamp);
                values.put("total_entries", 1);
                values.put("total_exits", 0);
            } else {
                values.put("exit_time", timestamp);
                values.put("total_entries", 0);
                values.put("total_exits", 1);
            }

            db.insert(TABLE_DAILY_ATTENDANCE, null, values);
        }

        cursor.close();
    }

    private String getCurrentTimestamp() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                .format(new Date());
    }

    private String getCurrentDate() {
        return new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                .format(new Date());
    }

    public boolean insertEntryExit(String phone, String type, String timestamp, String reason) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_EE_PHONE, phone);
        contentValues.put(COL_TYPE, type);
        contentValues.put(COL_TIMESTAMP, timestamp);
        contentValues.put(COL_REASON, reason);

        long result = db.insert(TABLE_ENTRY_EXIT, null, contentValues);
        return result != -1;
    }

    public Cursor getEntryExitRecords(String phone) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_ENTRY_EXIT +
                        " WHERE " + COL_EE_PHONE + "=? ORDER BY " + COL_ID + " DESC",
                new String[]{phone});
    }

    public Cursor getTodayEntryExitRecords(String phone, String date) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_ENTRY_EXIT +
                        " WHERE " + COL_EE_PHONE + "=? AND " + COL_TIMESTAMP + " LIKE ? ORDER BY " + COL_ID + " DESC",
                new String[]{phone, date + "%"});
    }

    // ==================== PAYMENT METHODS ====================

    public boolean insertPayment(String phone, String amount, String date, String status, String month) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_P_PHONE, phone);
        contentValues.put(COL_AMOUNT, amount);
        contentValues.put(COL_DATE, date);
        contentValues.put(COL_STATUS, status);
        contentValues.put(COL_MONTH, month);

        long result = db.insert(TABLE_PAYMENTS, null, contentValues);
        return result != -1;
    }

    public Cursor getPaymentHistory(String phone) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_PAYMENTS +
                        " WHERE " + COL_P_PHONE + "=? ORDER BY " + COL_PAYMENT_ID + " DESC",
                new String[]{phone});
    }



    // ==================== NOTICES METHODS ====================

    public Cursor getAllNotices() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_NOTICES +
                " ORDER BY " + COL_NOTICE_ID + " DESC", null);
    }

    private void insertSampleNotices(SQLiteDatabase db) {
        ContentValues cv1 = new ContentValues();
        cv1.put(COL_TITLE, "Hostel Maintenance Notice");
        cv1.put(COL_MESSAGE, "Water supply will be interrupted tomorrow from 10 AM to 2 PM for maintenance work.");
        cv1.put(COL_POSTED_BY, "Owner");
        cv1.put(COL_POSTED_DATE, "2024-10-28");
        db.insert(TABLE_NOTICES, null, cv1);

        ContentValues cv2 = new ContentValues();
        cv2.put(COL_TITLE, "Payment Reminder");
        cv2.put(COL_MESSAGE, "Monthly rent payment is due by 5th of every month. Please clear your dues on time.");
        cv2.put(COL_POSTED_BY, "Owner");
        cv2.put(COL_POSTED_DATE, "2024-10-25");
        db.insert(TABLE_NOTICES, null, cv2);

        ContentValues cv3 = new ContentValues();
        cv3.put(COL_TITLE, "Security Alert");
        cv3.put(COL_MESSAGE, "Please ensure all visitors are registered at the security desk. Entry without registration is strictly prohibited.");
        cv3.put(COL_POSTED_BY, "Security");
        cv3.put(COL_POSTED_DATE, "2024-10-20");
        db.insert(TABLE_NOTICES, null, cv3);
    }

    public boolean insertNotice(String title, String message, String postedBy) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_TITLE, title);
        values.put(COL_MESSAGE, message);
        values.put(COL_POSTED_BY, postedBy);
        values.put(COL_POSTED_DATE, getCurrentDate());

        long result = db.insert(TABLE_NOTICES, null, values);
        return result != -1;
    }

    public boolean deleteNotice(int noticeId) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rows = db.delete(TABLE_NOTICES, COL_NOTICE_ID + "=?",
                new String[]{String.valueOf(noticeId)});
        return rows > 0;
    }

    // ==================== COMPLAINTS METHODS ====================

    public boolean insertComplaint(String phone, String name, String complaint, String date) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_C_PHONE, phone);
        contentValues.put(COL_C_NAME, name);
        contentValues.put(COL_COMPLAINT, complaint);
        contentValues.put(COL_C_DATE, date);
        contentValues.put(COL_C_STATUS, "Pending");

        long result = db.insert(TABLE_COMPLAINTS, null, contentValues);
        return result != -1;
    }



    public Cursor getComplaintsByPhone(String phone) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_COMPLAINTS +
                        " WHERE " + COL_C_PHONE + "=? ORDER BY " + COL_COMPLAINT_ID + " DESC",
                new String[]{phone});
    }

    public Cursor getAllComplaints() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_COMPLAINTS +
                " ORDER BY " + COL_COMPLAINT_ID + " DESC", null);
    }
    public boolean updateComplaintStatus(int complaintId, String status) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_C_STATUS, status);

        int rows = db.update(TABLE_COMPLAINTS, values,
                COL_COMPLAINT_ID + "=?",
                new String[]{String.valueOf(complaintId)});
        return rows > 0;
    }

    // ========================================
    // VISITOR MANAGEMENT METHODS (UNIFIED)
    // ========================================

    /**
     * Add a new visitor entry with hosteller details
     */
    public long addVisitorEntry(String hostellerPhone, String visitorName, String visitorPhone,
                                String purpose, String visitDate, String visitTime) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        // Get hosteller details
        Cursor hostellerCursor = getHostellerByPhone(hostellerPhone);
        String hostellerName = "";
        String hostellerRoom = "";

        if (hostellerCursor != null && hostellerCursor.moveToFirst()) {
            hostellerName = hostellerCursor.getString(hostellerCursor.getColumnIndexOrThrow("name"));
            hostellerRoom = hostellerCursor.getString(hostellerCursor.getColumnIndexOrThrow("room_number"));
            hostellerCursor.close();
        }

        values.put("hosteller_phone", hostellerPhone);
        values.put("hosteller_name", hostellerName);
        values.put("hosteller_room", hostellerRoom);
        values.put("visitor_name", visitorName);
        values.put("visitor_phone", visitorPhone);
        values.put("purpose", purpose != null ? purpose : "");
        values.put("visit_date", visitDate);
        values.put("visit_time", visitTime);
        values.put("status", "Pending");

        long result = db.insert(TABLE_VISITORS, null, values);
        return result;
    }

    /**
     * Get all visitor entries for a specific hosteller
     */
    public Cursor getVisitorHistory(String hostellerPhone) {
        SQLiteDatabase db = this.getReadableDatabase();

        String query = "SELECT * FROM " + TABLE_VISITORS + " " +
                "WHERE hosteller_phone = ? " +
                "ORDER BY visit_date DESC, visit_time DESC";

        return db.rawQuery(query, new String[]{hostellerPhone});
    }

    /**
     * Get visitor entry by ID
     */
    public Cursor getVisitorById(int visitorId) {
        SQLiteDatabase db = this.getReadableDatabase();

        String query = "SELECT * FROM " + TABLE_VISITORS + " WHERE id = ?";

        return db.rawQuery(query, new String[]{String.valueOf(visitorId)});
    }

    /**
     * Update visitor status (Pending, Approved, Rejected, Completed)
     */
    public boolean updateVisitorStatus(int visitorId, String newStatus) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put("status", newStatus);

        if ("Approved".equals(newStatus)) {
            values.put("approved_at", getCurrentTimestamp());
            values.put("approved_by", "Owner");
        }

        int rowsAffected = db.update(TABLE_VISITORS, values, "id = ?",
                new String[]{String.valueOf(visitorId)});

        return rowsAffected > 0;
    }

    /**
     * Delete a visitor entry
     */
    public boolean deleteVisitorEntry(int visitorId) {
        SQLiteDatabase db = this.getWritableDatabase();

        int rowsDeleted = db.delete(TABLE_VISITORS, "id = ?",
                new String[]{String.valueOf(visitorId)});

        return rowsDeleted > 0;
    }

    /**
     * Get upcoming visitors (future dates)
     */
    public Cursor getUpcomingVisitors(String hostellerPhone) {
        SQLiteDatabase db = this.getReadableDatabase();

        String currentDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                .format(Calendar.getInstance().getTime());

        String query = "SELECT * FROM " + TABLE_VISITORS + " " +
                "WHERE hosteller_phone = ? " +
                "AND visit_date >= ? " +
                "AND status != 'Completed' " +
                "ORDER BY visit_date ASC, visit_time ASC";

        return db.rawQuery(query, new String[]{hostellerPhone, currentDate});
    }

    /**
     * Get visitor count by status
     */
    public int getVisitorCountByStatus(String hostellerPhone, String status) {
        SQLiteDatabase db = this.getReadableDatabase();

        String query = "SELECT COUNT(*) FROM " + TABLE_VISITORS + " " +
                "WHERE hosteller_phone = ? AND status = ?";

        Cursor cursor = db.rawQuery(query, new String[]{hostellerPhone, status});

        int count = 0;
        if (cursor != null && cursor.moveToFirst()) {
            count = cursor.getInt(0);
            cursor.close();
        }

        return count;
    }

    /**
     * Update visitor details
     */
    public boolean updateVisitorEntry(int visitorId, String visitorName, String visitorPhone,
                                      String purpose, String visitDate, String visitTime) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put("visitor_name", visitorName);
        values.put("visitor_phone", visitorPhone);
        values.put("purpose", purpose);
        values.put("visit_date", visitDate);
        values.put("visit_time", visitTime);

        int rowsAffected = db.update(TABLE_VISITORS, values, "id = ?",
                new String[]{String.valueOf(visitorId)});

        return rowsAffected > 0;
    }

    // ==================== OWNER DASHBOARD METHODS ====================

    /**
     * Get all visitors for owner dashboard
     */
    public Cursor getAllVisitorsForOwner() {
        SQLiteDatabase db = this.getReadableDatabase();

        String query = "SELECT * FROM " + TABLE_VISITORS + " " +
                "ORDER BY created_at DESC";

        return db.rawQuery(query, null);
    }

    /**
     * Get pending visitor requests count
     */
    public int getPendingVisitorsCount() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        try {
            String query = "SELECT COUNT(*) FROM " + TABLE_VISITORS + " WHERE status = 'Pending'";
            cursor = db.rawQuery(query, null);
            if (cursor != null && cursor.moveToFirst()) {
                return cursor.getInt(0);
            }
            return 0;
        } finally {
            if (cursor != null) cursor.close();
        }
    }

    /**
     * Get today's visitors count
     */
    public int getTodayVisitorsCount() {
        SQLiteDatabase db = this.getReadableDatabase();
        String today = getCurrentDate();
        Cursor cursor = null;
        try {
            String query = "SELECT COUNT(*) FROM " + TABLE_VISITORS + " WHERE visit_date = ?";
            cursor = db.rawQuery(query, new String[]{today});
            if (cursor != null && cursor.moveToFirst()) {
                return cursor.getInt(0);
            }
            return 0;
        } finally {
            if (cursor != null) cursor.close();
        }
    }

    /**
     * Get total visitors count
     */
    public int getTotalVisitorsCount() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        try {
            String query = "SELECT COUNT(*) FROM " + TABLE_VISITORS;
            cursor = db.rawQuery(query, null);
            if (cursor != null && cursor.moveToFirst()) {
                return cursor.getInt(0);
            }
            return 0;
        } finally {
            if (cursor != null) cursor.close();
        }
    }
    /**
     * Get all pending complaints for owner
     */
    public Cursor getAllPendingComplaints() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_COMPLAINTS +
                " WHERE status = 'Pending' ORDER BY " + COL_COMPLAINT_ID + " DESC", null);
    }

    /**
     * Get pending complaints count
     */
    public int getPendingComplaintsCount() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_COMPLAINTS +
                    " WHERE status = 'Pending'", null);
            if (cursor != null && cursor.moveToFirst()) {
                return cursor.getInt(0);
            }
            return 0;
        } finally {
            if (cursor != null) cursor.close();
        }
    }
    /**
     * Get all pending payments
     */
    public Cursor getAllPendingPayments() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT p.*, h.name, h.room_number FROM " + TABLE_PAYMENTS + " p " +
                "LEFT JOIN " + TABLE_HOSTELLER + " h ON p.phone = h.phone " +
                "WHERE p.status = 'Pending' ORDER BY p.date DESC", null);
    }

    /**
     * Get total pending payments amount
     */
    public double getTotalPendingPaymentsAmount() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.rawQuery(
                    "SELECT SUM(CAST(amount AS REAL)) FROM " + TABLE_PAYMENTS +
                            " WHERE status = 'Pending'", null);
            if (cursor != null && cursor.moveToFirst() && !cursor.isNull(0)) {
                return cursor.getDouble(0);
            }
            return 0;
        } finally {
            if (cursor != null) cursor.close();
        }
    }
    /**
     * Get all pending maintenance requests
     */
    public Cursor getAllPendingMaintenance() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT m.*, h.name FROM " + TABLE_MAINTENANCE + " m " +
                "LEFT JOIN " + TABLE_HOSTELLER + " h ON m.phone = h.phone " +
                "WHERE m.status = 'Pending' ORDER BY " +
                "CASE m.priority WHEN 'High' THEN 1 WHEN 'Medium' THEN 2 WHEN 'Low' THEN 3 END, " +
                "m.reported_date DESC", null);
    }

    /**
     * Get pending maintenance count
     */
    public int getPendingMaintenanceCount() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_MAINTENANCE +
                    " WHERE status = 'Pending'", null);
            if (cursor != null && cursor.moveToFirst()) {
                return cursor.getInt(0);
            }
            return 0;
        } finally {
            if (cursor != null) cursor.close();
        }
    }
    /**
     * Get all feedbacks for owner
     */
    public Cursor getAllFeedbacksForOwner() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_FEEDBACK +
                " ORDER BY " + FEEDBACK_DATE + " DESC", null);
    }

    /**
     * Get average rating for owner dashboard
     */
    public double getAverageRatingForOwner() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        try {
            String query = "SELECT AVG(" + FEEDBACK_RATING + ") FROM " + TABLE_FEEDBACK;
            cursor = db.rawQuery(query, null);
            if (cursor != null && cursor.moveToFirst()) {
                return cursor.getDouble(0);
            }
            return 0;
        } finally {
            if (cursor != null) cursor.close();
        }
    }

    /**
     * Get today's attendance summary
     */
    public Cursor getTodayAttendanceSummary() {
        SQLiteDatabase db = this.getReadableDatabase();
        String today = getCurrentDate();

        return db.rawQuery(
                "SELECT h.name, h.phone, h.room_number, da.status, " +
                        "da.entry_time, da.exit_time, da.total_entries, da.total_exits " +
                        "FROM " + TABLE_HOSTELLER + " h " +
                        "LEFT JOIN " + TABLE_DAILY_ATTENDANCE + " da " +
                        "ON h.phone = da.phone AND da.date = ? " +
                        "ORDER BY h.name",
                new String[]{today}
        );
    }

    /**
     * Get currently inside hostellers count
     */
    public int getCurrentlyInsideHostellersCount() {
        return getCurrentlyInsideCount();
    }

    /**
     * Get all recent activity for owner dashboard
     */
    public Cursor getRecentActivityForOwner(int limit) {
        SQLiteDatabase db = this.getReadableDatabase();

        String query = "SELECT 'Entry' as type, h.name, ee.timestamp, ee.type as action " +
                "FROM " + TABLE_ENTRY_EXIT + " ee " +
                "LEFT JOIN " + TABLE_HOSTELLER + " h ON ee.phone = h.phone " +
                "ORDER BY ee.timestamp DESC LIMIT ?";

        return db.rawQuery(query, new String[]{String.valueOf(limit)});
    }


    // ==================== DAILY ATTENDANCE METHODS ====================

    public boolean markDailyAttendance(String phone, String date, String status, String checkTime) {
        SQLiteDatabase db = this.getWritableDatabase();

        Cursor cursor = db.query(TABLE_DAILY_ATTENDANCE, null,
                "phone=? AND date=?", new String[]{phone, date},
                null, null, null);

        ContentValues values = new ContentValues();
        values.put("phone", phone);
        values.put("date", date);
        values.put("status", status);
        values.put("check_time", checkTime);

        boolean result;
        if (cursor != null && cursor.moveToFirst()) {
            result = db.update(TABLE_DAILY_ATTENDANCE, values,
                    "phone=? AND date=?", new String[]{phone, date}) > 0;
        } else {
            result = db.insertWithOnConflict(TABLE_DAILY_ATTENDANCE, null, values,
                    SQLiteDatabase.CONFLICT_REPLACE) != -1;
        }

        if (cursor != null) cursor.close();

        if (result && "PRESENT".equals(status)) {
            updateDailyAttendanceStats(phone, date);
        }

        return result;
    }

    public boolean updateDailyAttendanceStats(String phone, String date) {
        SQLiteDatabase db = this.getWritableDatabase();

        String query = "SELECT " +
                "MIN(CASE WHEN type='ENTRY' THEN timestamp END) as first_entry, " +
                "MAX(CASE WHEN type='EXIT' THEN timestamp END) as last_exit, " +
                "SUM(CASE WHEN type='ENTRY' THEN 1 ELSE 0 END) as entry_count, " +
                "SUM(CASE WHEN type='EXIT' THEN 1 ELSE 0 END) as exit_count " +
                "FROM " + TABLE_ENTRY_EXIT + " " +
                "WHERE phone=? AND DATE(timestamp)=?";

        Cursor cursor = db.rawQuery(query, new String[]{phone, date});

        if (cursor != null && cursor.moveToFirst()) {
            String firstEntry = cursor.getString(0);
            String lastExit = cursor.getString(1);
            int entryCount = cursor.getInt(2);
            int exitCount = cursor.getInt(3);
            cursor.close();

            ContentValues values = new ContentValues();
            values.put("entry_time", firstEntry);
            values.put("exit_time", lastExit);
            values.put("total_entries", entryCount);
            values.put("total_exits", exitCount);

            int rows = db.update(TABLE_DAILY_ATTENDANCE, values,
                    "phone=? AND date=?",
                    new String[]{phone, date});
            return rows > 0;
        }
        if (cursor != null) cursor.close();
        return false;
    }

    public String checkStudentPresence(String phone, String date) {
        SQLiteDatabase db = this.getReadableDatabase();

        String query = "SELECT type, timestamp FROM " + TABLE_ENTRY_EXIT + " " +
                "WHERE phone=? AND DATE(timestamp)=? " +
                "ORDER BY timestamp DESC LIMIT 1";

        Cursor cursor = db.rawQuery(query, new String[]{phone, date});

        if (cursor != null && cursor.moveToFirst()) {
            String lastType = cursor.getString(0);
            cursor.close();
            return lastType.equals("ENTRY") ? "PRESENT" : "ABSENT";
        }

        return "ABSENT";
    }

    public void performDailyAttendanceCheck() {
        SQLiteDatabase db = this.getWritableDatabase();
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                .format(new Date());
        String checkTime = new SimpleDateFormat("HH:mm:ss", Locale.getDefault())
                .format(new Date());

        Cursor studentsCursor = db.rawQuery("SELECT phone FROM " + TABLE_HOSTELLER, null);

        if (studentsCursor != null && studentsCursor.moveToFirst()) {
            do {
                String phone = studentsCursor.getString(0);
                String status = checkStudentPresence(phone, today);
                markDailyAttendance(phone, today, status, checkTime);
                updateDailyAttendanceStats(phone, today);
            } while (studentsCursor.moveToNext());
            studentsCursor.close();
        }
    }

    public Cursor getDailyAttendance(String phone) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_DAILY_ATTENDANCE, null, "phone=?",
                new String[]{phone}, null, null, "date DESC");
    }

    public Cursor getDailyAttendanceForDate(String phone, String date) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_DAILY_ATTENDANCE, null, "phone=? AND date=?",
                new String[]{phone, date}, null, null, null);
    }

    public int getAttendancePercentage(String phone) {
        SQLiteDatabase db = this.getReadableDatabase();

        String query = "SELECT " +
                "COUNT(*) as total_days, " +
                "SUM(CASE WHEN status='PRESENT' THEN 1 ELSE 0 END) as present_days " +
                "FROM " + TABLE_DAILY_ATTENDANCE + " WHERE phone=?";

        Cursor cursor = db.rawQuery(query, new String[]{phone});

        if (cursor != null && cursor.moveToFirst()) {
            int totalDays = cursor.getInt(0);
            int presentDays = cursor.getInt(1);
            cursor.close();

            if (totalDays > 0) {
                return (presentDays * 100) / totalDays;
            }
        }
        return 0;
    }

    public Cursor getMonthlyAttendance(String phone, String month) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery(
                "SELECT * FROM " + TABLE_DAILY_ATTENDANCE + " " +
                        "WHERE phone=? AND date LIKE ? " +
                        "ORDER BY date DESC",
                new String[]{phone, month + "%"}
        );
    }

    public boolean hasMarkedAttendanceToday(String phone) {
        SQLiteDatabase db = this.getReadableDatabase();
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                .format(new Date());

        Cursor cursor = db.query(TABLE_DAILY_ATTENDANCE,
                new String[]{"status"},
                "phone=? AND date=? AND status='PRESENT'",
                new String[]{phone, today},
                null, null, null);

        boolean marked = cursor != null && cursor.moveToFirst();
        if (cursor != null) cursor.close();

        return marked;
    }

    public boolean isAttendanceOverdue(String phone) {
        if (hasMarkedAttendanceToday(phone)) {
            return false;
        }

        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        return hour >= 21;
    }

    public void markAbsentForUnmarked() {
        SQLiteDatabase db = this.getWritableDatabase();
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                .format(new Date());
        String currentTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                .format(new Date());

        Cursor studentsCursor = db.rawQuery("SELECT phone FROM " + TABLE_HOSTELLER, null);

        if (studentsCursor != null && studentsCursor.moveToFirst()) {
            do {
                String phone = studentsCursor.getString(0);

                if (!hasMarkedAttendanceToday(phone)) {
                    ContentValues values = new ContentValues();
                    values.put("phone", phone);
                    values.put("date", today);
                    values.put("status", "ABSENT");
                    values.put("check_time", currentTime);
                    values.put("total_entries", 0);
                    values.put("total_exits", 0);

                    db.insertWithOnConflict(TABLE_DAILY_ATTENDANCE, null, values,
                            SQLiteDatabase.CONFLICT_REPLACE);

                    Log.d("DatabaseHelper", "Marked " + phone + " as ABSENT for not marking attendance");
                }
            } while (studentsCursor.moveToNext());
            studentsCursor.close();
        }
    }

    public Cursor getUnmarkedAttendanceStudents() {
        SQLiteDatabase db = this.getReadableDatabase();
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                .format(new Date());

        return db.rawQuery(
                "SELECT h.* FROM " + TABLE_HOSTELLER + " h " +
                        "WHERE NOT EXISTS (" +
                        "    SELECT 1 FROM " + TABLE_DAILY_ATTENDANCE + " da " +
                        "    WHERE da.phone = h.phone AND da.date = ? AND da.status = 'PRESENT'" +
                        ")",
                new String[]{today}
        );
    }

    public Cursor getAttendanceSummaryByDate(String date) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery(
                "SELECT h.name, h.phone, h.room_number, da.status, " +
                        "da.entry_time, da.exit_time, da.total_entries, da.total_exits " +
                        "FROM " + TABLE_HOSTELLER + " h " +
                        "LEFT JOIN " + TABLE_DAILY_ATTENDANCE + " da " +
                        "ON h.phone = da.phone AND da.date = ? " +
                        "ORDER BY h.name",
                new String[]{date}
        );
    }

    // ==================== OWNER METHODS ====================

    public long insertOwner(String name, String email, String phone, String propertyName, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(OWNER_NAME, name);
        values.put(OWNER_EMAIL, email);
        values.put(OWNER_PHONE, phone);
        values.put(OWNER_PROPERTY_NAME, propertyName);
        values.put(OWNER_PASSWORD, password);

        long result = db.insert(TABLE_OWNER, null, values);
        return result;
    }

    public boolean checkOwnerEmailExists(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_OWNER, new String[]{OWNER_ID},
                OWNER_EMAIL + "=?", new String[]{email},
                null, null, null);
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
    }

    public Cursor getOwnerByEmailAndPassword(String email, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_OWNER, null,
                OWNER_EMAIL + "=? AND " + OWNER_PASSWORD + "=?",
                new String[]{email, password},
                null, null, null);
    }

    // Kept for backward compatibility but redirects to new methods
    public int getTotalVisitorsForOwner(int ownerId) {
        return getTotalVisitorsCount();
    }

    public int getActiveVisitorsForOwner(int ownerId) {
        return getTodayVisitorsCount();
    }

    // ==================== SECURITY METHODS ====================

    public long insertSecurity(String name, String securityId, String phone, String shift, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(SECURITY_NAME, name);
        values.put(SECURITY_SECURITY_ID, securityId);
        values.put(SECURITY_PHONE, phone);
        values.put(SECURITY_SHIFT, shift);
        values.put(SECURITY_PASSWORD, password);

        long result = db.insert(TABLE_SECURITY, null, values);
        return result;
    }

    public boolean checkSecurityIdExists(String securityId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_SECURITY, new String[]{SECURITY_ID},
                SECURITY_SECURITY_ID + "=?", new String[]{securityId},
                null, null, null);
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
    }

    public Cursor getSecurityByIdAndPassword(String securityId, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_SECURITY, null,
                SECURITY_SECURITY_ID + "=? AND " + SECURITY_PASSWORD + "=?",
                new String[]{securityId, password},
                null, null, null);
    }

    public int getTodayEntriesCount() {
        SQLiteDatabase db = this.getReadableDatabase();
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_ENTRY_EXIT +
                        " WHERE DATE(timestamp)=? AND type='ENTRY'",
                new String[]{today});
        int count = 0;
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }
        cursor.close();
        return count;
    }

    public int getCurrentVisitorsCount() {
        return getTodayVisitorsCount();
    }

    // ==================== LAUNDRY SERVICE METHODS ====================

    public long submitLaundryRequest(String phone, String items, int quantity,
                                     String pickupDate, double amount) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(LAUNDRY_PHONE, phone);
        values.put(LAUNDRY_ITEMS, items);
        values.put(LAUNDRY_QUANTITY, quantity);
        values.put(LAUNDRY_PICKUP_DATE, pickupDate);
        values.put(LAUNDRY_AMOUNT, amount);
        values.put(LAUNDRY_STATUS, "Pending");

        return db.insert(TABLE_LAUNDRY, null, values);
    }

    public Cursor getLaundryHistory(String phone) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_LAUNDRY, null, LAUNDRY_PHONE + "=?",
                new String[]{phone}, null, null, LAUNDRY_CREATED_AT + " DESC");
    }

    public Cursor getActiveLaundryRequests(String phone) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_LAUNDRY, null,
                LAUNDRY_PHONE + "=? AND " + LAUNDRY_STATUS + " IN ('Pending', 'In Progress')",
                new String[]{phone}, null, null, LAUNDRY_CREATED_AT + " DESC");
    }

    public boolean updateLaundryStatus(int laundryId, String status, String deliveryDate) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(LAUNDRY_STATUS, status);
        if (deliveryDate != null) {
            values.put(LAUNDRY_DELIVERY_DATE, deliveryDate);
        }

        int rows = db.update(TABLE_LAUNDRY, values, LAUNDRY_ID + "=?",
                new String[]{String.valueOf(laundryId)});
        return rows > 0;
    }

    // ==================== FEEDBACK METHODS ====================

    public long submitFeedback(String phone, String name, String category,
                               int rating, String message, String date) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(FEEDBACK_PHONE, phone);
        values.put(FEEDBACK_NAME, name);
        values.put(FEEDBACK_CATEGORY, category);
        values.put(FEEDBACK_RATING, rating);
        values.put(FEEDBACK_MESSAGE, message);
        values.put(FEEDBACK_DATE, date);

        return db.insert(TABLE_FEEDBACK, null, values);
    }

    public Cursor getFeedbackHistory(String phone) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_FEEDBACK, null, FEEDBACK_PHONE + "=?",
                new String[]{phone}, null, null, FEEDBACK_DATE + " DESC");
    }

    public Cursor getAllFeedback() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_FEEDBACK, null, null, null, null, null,
                FEEDBACK_DATE + " DESC");
    }

    public double getAverageRating(String category) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT AVG(" + FEEDBACK_RATING + ") FROM " + TABLE_FEEDBACK;
        if (category != null && !category.isEmpty()) {
            query += " WHERE " + FEEDBACK_CATEGORY + "=?";
        }

        Cursor cursor = category != null ?
                db.rawQuery(query, new String[]{category}) :
                db.rawQuery(query, null);

        double avg = 0;
        if (cursor.moveToFirst()) {
            avg = cursor.getDouble(0);
        }
        cursor.close();
        return avg;
    }

    // ==================== EMERGENCY CONTACTS METHODS ====================

    private void insertSampleEmergencyContacts(SQLiteDatabase db) {
        ContentValues[] contacts = new ContentValues[]{
                createEmergencyContact("Dr. Sharma", "Medical Officer", "9876543210", "Medical"),
                createEmergencyContact("Hostel Warden", "Warden", "987654321", "Hostel"),
                createEmergencyContact("Fire Station", "Emergency", "101", "Fire"),
                createEmergencyContact("Police Station", "Emergency", "100", "Police"),
                createEmergencyContact("Ambulance", "Emergency", "108", "Medical"),
                createEmergencyContact("Security Head", "Chief Security", "987654321", "Hostel")
        };

        for (ContentValues cv : contacts) {
            db.insert(TABLE_EMERGENCY, null, cv);
        }
    }

    private ContentValues createEmergencyContact(String name, String designation,
                                                 String phone, String type) {
        ContentValues cv = new ContentValues();
        cv.put(EMERGENCY_NAME, name);
        cv.put(EMERGENCY_DESIGNATION, designation);
        cv.put(EMERGENCY_PHONE, phone);
        cv.put(EMERGENCY_TYPE, type);
        return cv;
    }

    public Cursor getAllEmergencyContacts() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_EMERGENCY, null, null, null, null, null,
                EMERGENCY_TYPE + ", " + EMERGENCY_NAME);
    }

    public Cursor getEmergencyContactsByType(String type) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_EMERGENCY, null, EMERGENCY_TYPE + "=?",
                new String[]{type}, null, null, EMERGENCY_NAME);
    }

    // ==================== LEAVE REQUEST METHODS ====================

    // ==================== LEAVE REQUEST METHODS ====================

    public long submitLeaveRequest(String phone, String name, String fromDate,
                                   String toDate, String reason, String requestedDate) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(LEAVE_PHONE, phone);
        values.put(LEAVE_NAME, name);
        values.put(LEAVE_FROM_DATE, fromDate);
        values.put(LEAVE_TO_DATE, toDate);
        values.put(LEAVE_REASON, reason);
        values.put(LEAVE_REQUESTED_DATE, requestedDate);
        values.put(LEAVE_STATUS, "Pending");

        return db.insert(TABLE_LEAVE, null, values);
    }

    public Cursor getLeaveRequests(String phone) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_LEAVE, null, LEAVE_PHONE + "=?",
                new String[]{phone}, null, null, LEAVE_REQUESTED_DATE + " DESC");
    }

    public Cursor getAllLeaveRequests() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_LEAVE, null, null, null, null, null,
                LEAVE_REQUESTED_DATE + " DESC");
    }

    public boolean updateLeaveStatus(int leaveId, String status, String responseDate) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(LEAVE_STATUS, status);
        values.put(LEAVE_RESPONSE_DATE, responseDate);

        int rows = db.update(TABLE_LEAVE, values, LEAVE_ID + "=?",
                new String[]{String.valueOf(leaveId)});
        return rows > 0;
    }

    // Get total pending leave count (for owner dashboard)
    public int getPendingLeaveCount() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.rawQuery(
                    "SELECT COUNT(*) FROM " + TABLE_LEAVE + " WHERE " + LEAVE_STATUS + "='Pending'",
                    null
            );
            if (cursor != null && cursor.moveToFirst()) {
                return cursor.getInt(0);
            }
            return 0;
        } finally {
            if (cursor != null) cursor.close();
        }
    }

    // Get pending leave count for a specific hosteller
    public int getPendingLeaveCount(String phone) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.query(TABLE_LEAVE, new String[]{"COUNT(*)"},
                    LEAVE_PHONE + "=? AND " + LEAVE_STATUS + "='Pending'",
                    new String[]{phone}, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                return cursor.getInt(0);
            }
            return 0;
        } finally {
            if (cursor != null) cursor.close();
        }
    }

    // ==================== MESS MENU METHODS ====================

    private void insertSampleMessMenu(SQLiteDatabase db) {
        String[] days = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};
        String[] breakfasts = {
                "Poha, Tea/Coffee, Banana",
                "Upma, Tea/Coffee, Apple",
                "Paratha, Curd, Tea/Coffee",
                "Idli, Sambar, Chutney, Tea/Coffee",
                "Bread, Jam, Butter, Tea/Coffee",
                "Dosa, Sambar, Chutney, Tea/Coffee",
                "Aloo Puri, Tea/Coffee, Orange"
        };
        String[] lunches = {
                "Dal, Rice, Roti, Veg Curry, Salad",
                "Rajma, Rice, Roti, Papad, Salad",
                "Chole, Rice, Roti, Raita, Salad",
                "Paneer Curry, Rice, Roti, Dal, Salad",
                "Mixed Dal, Rice, Roti, Aloo Gobi, Salad",
                "Special Pulao, Raita, Papad, Sweet",
                "Kadhi, Rice, Roti, Veg, Salad"
        };
        String[] dinners = {
                "Dal, Rice, Roti, Mix Veg, Salad",
                "Rajma, Rice, Roti, Aloo, Salad",
                "Paneer, Rice, Roti, Dal, Salad",
                "Chole, Rice, Roti, Raita, Salad",
                "Dal Makhani, Rice, Roti, Veg, Salad",
                "Special Dinner - Biryani, Raita",
                "Dal, Rice, Roti, Seasonal Veg, Salad"
        };

        for (int i = 0; i < days.length; i++) {
            ContentValues breakfast = new ContentValues();
            breakfast.put(MENU_DAY, days[i]);
            breakfast.put(MENU_MEAL_TYPE, "Breakfast");
            breakfast.put(MENU_ITEMS, breakfasts[i]);
            db.insertWithOnConflict(TABLE_MESS_MENU, null, breakfast,
                    SQLiteDatabase.CONFLICT_REPLACE);

            ContentValues lunch = new ContentValues();
            lunch.put(MENU_DAY, days[i]);
            lunch.put(MENU_MEAL_TYPE, "Lunch");
            lunch.put(MENU_ITEMS, lunches[i]);
            db.insertWithOnConflict(TABLE_MESS_MENU, null, lunch,
                    SQLiteDatabase.CONFLICT_REPLACE);

            ContentValues dinner = new ContentValues();
            dinner.put(MENU_DAY, days[i]);
            dinner.put(MENU_MEAL_TYPE, "Dinner");
            dinner.put(MENU_ITEMS, dinners[i]);
            db.insertWithOnConflict(TABLE_MESS_MENU, null, dinner,
                    SQLiteDatabase.CONFLICT_REPLACE);
        }
    }

    public Cursor getMessMenuForDay(String day) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_MESS_MENU, null, MENU_DAY + "=?",
                new String[]{day}, null, null,
                "CASE " + MENU_MEAL_TYPE +
                        " WHEN 'Breakfast' THEN 1" +
                        " WHEN 'Lunch' THEN 2" +
                        " WHEN 'Dinner' THEN 3 END");
    }

    public Cursor getFullWeekMenu() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_MESS_MENU, null, null, null, null, null,
                "CASE " + MENU_DAY +
                        " WHEN 'Monday' THEN 1" +
                        " WHEN 'Tuesday' THEN 2" +
                        " WHEN 'Wednesday' THEN 3" +
                        " WHEN 'Thursday' THEN 4" +
                        " WHEN 'Friday' THEN 5" +
                        " WHEN 'Saturday' THEN 6" +
                        " WHEN 'Sunday' THEN 7 END, " +
                        "CASE " + MENU_MEAL_TYPE +
                        " WHEN 'Breakfast' THEN 1" +
                        " WHEN 'Lunch' THEN 2" +
                        " WHEN 'Dinner' THEN 3 END");
    }

    // ==================== ROOM MAINTENANCE METHODS ====================

    public long reportMaintenanceIssue(String phone, String roomNumber, String issue,
                                       String description, String priority, String date) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(MAINTENANCE_PHONE, phone);
        values.put(MAINTENANCE_ROOM, roomNumber);
        values.put(MAINTENANCE_ISSUE, issue);
        values.put(MAINTENANCE_DESCRIPTION, description);
        values.put(MAINTENANCE_PRIORITY, priority);
        values.put(MAINTENANCE_REPORTED_DATE, date);
        values.put(MAINTENANCE_STATUS, "Pending");

        return db.insert(TABLE_MAINTENANCE, null, values);
    }

    public Cursor getMaintenanceRequests(String phone) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_MAINTENANCE, null, MAINTENANCE_PHONE + "=?",
                new String[]{phone}, null, null, MAINTENANCE_REPORTED_DATE + " DESC");
    }

    public Cursor getAllMaintenanceRequests() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_MAINTENANCE, null, null, null, null, null,
                "CASE " + MAINTENANCE_PRIORITY +
                        " WHEN 'High' THEN 1" +
                        " WHEN 'Medium' THEN 2" +
                        " WHEN 'Low' THEN 3 END, " +
                        MAINTENANCE_REPORTED_DATE + " DESC");
    }

    public boolean updateMaintenanceStatus(int maintenanceId, String status,
                                           String resolvedDate) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(MAINTENANCE_STATUS, status);
        if (resolvedDate != null) {
            values.put(MAINTENANCE_RESOLVED_DATE, resolvedDate);
        }

        int rows = db.update(TABLE_MAINTENANCE, values, MAINTENANCE_ID + "=?",
                new String[]{String.valueOf(maintenanceId)});
        return rows > 0;
    }

    public int getPendingMaintenanceCount(String phone) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_MAINTENANCE, new String[]{"COUNT(*)"},
                MAINTENANCE_PHONE + "=? AND " + MAINTENANCE_STATUS + "='Pending'",
                new String[]{phone}, null, null, null);

        int count = 0;
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }
        cursor.close();
        return count;
    }

    // ==================== ADDITIONAL UTILITY METHODS ====================

    public int getTodayEntryExitCount(String phone, String type) {
        SQLiteDatabase db = this.getReadableDatabase();
        String today = getCurrentDate();

        Cursor cursor = db.rawQuery(
                "SELECT COUNT(*) FROM " + TABLE_ENTRY_EXIT +
                        " WHERE phone=? AND type=? AND DATE(timestamp)=?",
                new String[]{phone, type, today}
        );

        int count = 0;
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }
        cursor.close();
        return count;
    }

    public Cursor getLastEntryExit(String phone) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery(
                "SELECT * FROM " + TABLE_ENTRY_EXIT +
                        " WHERE phone=? ORDER BY timestamp DESC LIMIT 1",
                new String[]{phone}
        );
    }

    public boolean isStudentInside(String phone) {
        Cursor cursor = null;
        try {
            cursor = getLastEntryExit(phone);
            if (cursor != null && cursor.moveToFirst()) {
                int typeIndex = cursor.getColumnIndex(COL_TYPE);
                if (typeIndex != -1) {
                    String lastType = cursor.getString(typeIndex);
                    return "ENTRY".equals(lastType);
                }
            }
            return false;
        } finally {
            if (cursor != null) cursor.close();
        }
    }

    public int getTotalHostellersCount() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_HOSTELLER, null);
            if (cursor != null && cursor.moveToFirst()) {
                return cursor.getInt(0);
            }
            return 0;
        } finally {
            if (cursor != null) cursor.close();
        }
    }

    public int getTodayPresentCount() {
        SQLiteDatabase db = this.getReadableDatabase();
        String today = getCurrentDate();
        Cursor cursor = null;
        try {
            cursor = db.rawQuery(
                    "SELECT COUNT(*) FROM " + TABLE_DAILY_ATTENDANCE +
                            " WHERE date=? AND status='PRESENT'",
                    new String[]{today}
            );
            if (cursor != null && cursor.moveToFirst()) {
                return cursor.getInt(0);
            }
            return 0;
        } finally {
            if (cursor != null) cursor.close();
        }
    }

    public int getCurrentlyInsideCount() {
        SQLiteDatabase db = this.getReadableDatabase();
        String today = getCurrentDate();
        Cursor cursor = null;
        try {
            cursor = db.rawQuery(
                    "SELECT DISTINCT phone FROM " + TABLE_ENTRY_EXIT +
                            " WHERE DATE(timestamp)=?",
                    new String[]{today}
            );

            int insideCount = 0;
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    String phone = cursor.getString(0);
                    if (isStudentInside(phone)) {
                        insideCount++;
                    }
                } while (cursor.moveToNext());
            }
            return insideCount;
        } finally {
            if (cursor != null) cursor.close();
        }
    }

    public void clearAllData() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM " + TABLE_ENTRY_EXIT);
        db.execSQL("DELETE FROM " + TABLE_DAILY_ATTENDANCE);
        db.execSQL("DELETE FROM " + TABLE_PAYMENTS);
        db.execSQL("DELETE FROM " + TABLE_COMPLAINTS);
        db.execSQL("DELETE FROM " + TABLE_VISITORS);
    }

    public Cursor getEntryExitStats(String startDate, String endDate) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery(
                "SELECT DATE(timestamp) as date, type, COUNT(*) as count " +
                        "FROM " + TABLE_ENTRY_EXIT + " " +
                        "WHERE DATE(timestamp) BETWEEN ? AND ? " +
                        "GROUP BY DATE(timestamp), type " +
                        "ORDER BY date DESC",
                new String[]{startDate, endDate}
        );
    }

    public int getPendingPaymentsCount(String phone) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT COUNT(*) FROM " + TABLE_PAYMENTS +
                        " WHERE phone=? AND status='Pending'",
                new String[]{phone}
        );

        int count = 0;
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }
        cursor.close();
        return count;
    }

    public double getTotalPendingAmount(String phone) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT SUM(CAST(amount AS REAL)) FROM " + TABLE_PAYMENTS +
                        " WHERE phone=? AND status='Pending'",
                new String[]{phone}
        );

        double total = 0;
        if (cursor.moveToFirst() && !cursor.isNull(0)) {
            total = cursor.getDouble(0);
        }
        cursor.close();
        return total;
    }

    public SQLiteDatabase getDatabase() {
        return this.getReadableDatabase();
    }

}