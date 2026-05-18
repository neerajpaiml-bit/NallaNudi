package com.example.nallanudi.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(entities = [Term::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun termDao(): TermDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "nalla_nudi_database"
                )
                    .addCallback(AppDatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class AppDatabaseCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch(Dispatchers.IO) {
                    populateDatabase(database.termDao())
                }
            }
        }

        suspend fun populateDatabase(termDao: TermDao) {
            val terms = listOf(
                // Science
                Term(englishWord = "Gravity", kannadaMeaning = "ಗುರುತ್ವಾಕರ್ಷಣೆ", exampleSentence = "Gravity pulls objects towards Earth.", subject = "Science"),
                Term(englishWord = "Photosynthesis", kannadaMeaning = "ಪ್ರಕಾಶ ಸಂಶ್ಲೇಷಣೆ", exampleSentence = "Plants make food using sunlight via photosynthesis.", subject = "Science"),
                Term(englishWord = "Atom", kannadaMeaning = "ಪರಮಾಣು", exampleSentence = "An atom is the smallest unit of matter.", subject = "Science"),
                Term(englishWord = "Molecule", kannadaMeaning = "ಅಣು", exampleSentence = "Water is a molecule made of hydrogen and oxygen.", subject = "Science"),
                Term(englishWord = "Cell", kannadaMeaning = "ಜೀವಕೋಶ", exampleSentence = "The cell is the basic building block of life.", subject = "Science"),
                Term(englishWord = "Evolution", kannadaMeaning = "ವಿಕಾಸ", exampleSentence = "Evolution explains how species change over time.", subject = "Science"),
                Term(englishWord = "Magnetism", kannadaMeaning = "ಕಾಂತೀಯತೆ", exampleSentence = "Magnetism is a force that can attract or repel.", subject = "Science"),
                Term(englishWord = "Electricity", kannadaMeaning = "ವಿದ್ಯುತ್", exampleSentence = "Electricity powers our homes and gadgets.", subject = "Science"),
                Term(englishWord = "DNA", kannadaMeaning = "ಡಿಎನ್ಎ", exampleSentence = "DNA carries the genetic instructions for life.", subject = "Science"),
                Term(englishWord = "Ecosystem", kannadaMeaning = "ಪರಿಸರ ವ್ಯವಸ್ಥೆ", exampleSentence = "A forest is a complex ecosystem.", subject = "Science"),
                Term(englishWord = "Energy", kannadaMeaning = "ಶಕ್ತಿ", exampleSentence = "Energy is needed to perform any kind of work.", subject = "Science"),
                Term(englishWord = "Force", kannadaMeaning = "ಬಲ", exampleSentence = "Force is a push or pull on an object.", subject = "Science"),

                // Math
                Term(englishWord = "Trigonometry", kannadaMeaning = "ತ್ರಿಕೋನಮಿತಿ", exampleSentence = "Trigonometry studies the relationships in triangles.", subject = "Math"),
                Term(englishWord = "Algebra", kannadaMeaning = "ಬೀಜಗಣಿತ", exampleSentence = "Algebra uses symbols to represent numbers in formulas.", subject = "Math"),
                Term(englishWord = "Geometry", kannadaMeaning = "ರೇಖಾಗಣಿತ", exampleSentence = "Geometry deals with shapes, sizes, and properties of space.", subject = "Math"),
                Term(englishWord = "Calculus", kannadaMeaning = "ಕಲನಶಾಸ್ತ್ರ", exampleSentence = "Calculus is the mathematical study of continuous change.", subject = "Math"),
                Term(englishWord = "Probability", kannadaMeaning = "ಸಂಭವನೀಯತೆ", exampleSentence = "Probability measures the likelihood of an event.", subject = "Math"),
                Term(englishWord = "Statistics", kannadaMeaning = "ಅಂಕಿಅಂಶಗಳು", exampleSentence = "Statistics involves collecting and analyzing data.", subject = "Math"),
                Term(englishWord = "Fraction", kannadaMeaning = "ಭಿನ್ನರಾಶಿ", exampleSentence = "A fraction represents a part of a whole.", subject = "Math"),
                Term(englishWord = "Decimal", kannadaMeaning = "ದಶಮಾಂಶ", exampleSentence = "Decimals are used to express numbers between integers.", subject = "Math"),
                Term(englishWord = "Theorem", kannadaMeaning = "ಪ್ರಮೇಯ", exampleSentence = "Pythagoras theorem is famous in geometry.", subject = "Math"),
                Term(englishWord = "Angle", kannadaMeaning = "ಕೋನ", exampleSentence = "A right angle is exactly 90 degrees.", subject = "Math"),
                Term(englishWord = "Symmetry", kannadaMeaning = "ಸಮ್ಮಿತಿ", exampleSentence = "A butterfly shows perfect bilateral symmetry.", subject = "Math"),

                // Commerce
                Term(englishWord = "Interest", kannadaMeaning = "ಬಡ್ಡಿ", exampleSentence = "Banks pay interest on your savings account.", subject = "Commerce"),
                Term(englishWord = "Inflation", kannadaMeaning = "ಹಣದುಬ್ಬರ", exampleSentence = "Inflation reduces the purchasing power of money.", subject = "Commerce"),
                Term(englishWord = "Asset", kannadaMeaning = "ಆಸ್ತಿ", exampleSentence = "An asset is something that has value to a business.", subject = "Commerce"),
                Term(englishWord = "Liability", kannadaMeaning = "ಹೊಣೆಗಾರಿಕೆ", exampleSentence = "A loan is a liability for the borrower.", subject = "Commerce"),
                Term(englishWord = "Revenue", kannadaMeaning = "ಆದಾಯ", exampleSentence = "Revenue is the total income generated by sales.", subject = "Commerce"),
                Term(englishWord = "Budget", kannadaMeaning = "ಮುಂಗಡ ಪತ್ರ", exampleSentence = "Creating a budget helps in managing expenses.", subject = "Commerce"),
                Term(englishWord = "Audit", kannadaMeaning = "ಲೆಕ್ಕಪರಿಶೋಧನೆ", exampleSentence = "An audit verifies the accuracy of financial records.", subject = "Commerce"),
                Term(englishWord = "Stock", kannadaMeaning = "ಷೇರು", exampleSentence = "Investing in stock means buying a share of a company.", subject = "Commerce"),
                Term(englishWord = "Dividend", kannadaMeaning = "ಲಾಭಾಂಶ", exampleSentence = "A dividend is a portion of profit paid to shareholders.", subject = "Commerce"),
                Term(englishWord = "Entrepreneur", kannadaMeaning = "ಉದ್ಯಮಿ", exampleSentence = "An entrepreneur starts and operates a new business.", subject = "Commerce"),
                Term(englishWord = "Marketing", kannadaMeaning = "ಮಾರುಕಟ್ಟೆ", exampleSentence = "Marketing helps in reaching out to potential customers.", subject = "Commerce")
            )
            terms.forEach { termDao.insertTerm(it) }
        }
    }
}
