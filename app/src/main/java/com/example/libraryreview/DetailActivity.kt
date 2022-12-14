package com.example.libraryreview

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.room.Room
import com.bumptech.glide.Glide
import com.example.libraryreview.databinding.ActivityDetailBinding
import com.example.libraryreview.model.AppDatabase
import com.example.libraryreview.model.Book
import com.example.libraryreview.model.Review

class DetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailBinding
    private lateinit var db: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityDetailBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        db = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "historyDB"
        ).build()

        val bookModel = intent.getParcelableExtra<Book>("bookModel")

        binding.titleTextView.text = bookModel?.title.orEmpty()

        Glide
            .with(binding.coverImageView.context)
            .load(bookModel?.coverSmallUrl.orEmpty())
            .into(binding.coverImageView)

        binding.descriptionTextView.text = bookModel?.description.orEmpty()


//        Thread {
//            val review = db.reviewDao().getOneReview(bookModel?.id?.toInt() ?: 0)
//            runOnUiThread {
//                binding.reviewEditText.setText(review?.review.orEmpty())
//            }
//        }.start()

        binding.saveButton.setOnClickListener {
            Thread {
                db.reviewDao().saveReview(
                    Review(
                        bookModel?.id?.toInt() ?: 0,
                        binding.reviewEditText.text.toString()
                    )
                )
            }.start()
        }
    }
}