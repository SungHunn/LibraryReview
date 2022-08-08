package com.example.libraryreview

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.MotionEvent
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.room.Room
import com.example.libraryreview.adapter.BookAdapter
import com.example.libraryreview.adapter.HistoryAdapter
import com.example.libraryreview.api.BookService
import com.example.libraryreview.databinding.ActivityMainBinding
import com.example.libraryreview.model.AppDatabase
import com.example.libraryreview.model.BestSellerDto
import com.example.libraryreview.model.History
import com.example.libraryreview.model.SearchBookDto
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: BookAdapter
    private lateinit var historyadapter: HistoryAdapter
    private lateinit var bookService: BookService

    private lateinit var db: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initBookrecyclerView()
        initHistoryrecyclerView()

        db = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "BookSearchDB"
        ).build()


        val retrofit = Retrofit.Builder()
            .baseUrl("https://book.interpark.com")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        bookService = retrofit.create(BookService::class.java)

        bookService.getBestSellerBooks(getString(R.string.interpark_key))
            .enqueue(object: Callback<BestSellerDto>{

                override fun onResponse(
                    call: Call<BestSellerDto>,
                    response: Response<BestSellerDto>
                ) {
                    if(response.isSuccessful.not()){
                        return
                    }
                    response.body()?.let {
                        Log.d(TAG, it.toString())

                        it.books.forEach {book ->
                            Log.d(TAG, book.toString())
                        }
                        adapter.submitList(it.books)
                    }

                }

                override fun onFailure(call: Call<BestSellerDto>, t: Throwable) {
                    Log.e(TAG, t.toString())
                }

            })



        initSearchEditText()
    }

    private fun initHistoryrecyclerView() {
        historyadapter = HistoryAdapter(historyDeleteClickedListener = {
            deleteSearchKeyword(it)
        })

        binding.historyRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.historyRecyclerView.adapter = historyadapter
    }

    private fun initSearchEditText(){
        binding.searchEditText.setOnKeyListener { view, i, keyEvent ->
            if(i == KeyEvent.KEYCODE_ENTER && keyEvent.action == MotionEvent.ACTION_DOWN){
                search(binding.searchEditText.text.toString())
                return@setOnKeyListener true
            }
            return@setOnKeyListener false
        }

        binding.searchEditText.setOnTouchListener { view, motionEvent ->
            if(motionEvent.action == MotionEvent.ACTION_DOWN){
                showHistoryView()

            }
            return@setOnTouchListener false
        }
    }

    private fun deleteSearchKeyword(keyword: String) {
        Thread{
            db.historyDao().delete(keyword)
            showHistoryView()
        }.start()
    }

    private fun search(keyword: String) {
        bookService.getBooksByName(getString(R.string.interpark_key),keyword)
            .enqueue(object: Callback<SearchBookDto>{

                override fun onResponse(
                    call: Call<SearchBookDto>, response: Response<SearchBookDto>){

                    hideHistoryView()
                    saveSearchKeyword(keyword)

                    if(response.isSuccessful.not()){
                        return
                    }
                    adapter.submitList(response.body()?.books.orEmpty())


                }

                override fun onFailure(call: Call<SearchBookDto>, t: Throwable) {

                    hideHistoryView()
                    Log.e(TAG, t.toString())
                }

            })
    }

    private fun saveSearchKeyword(keyword: String) {
        Thread{
            db.historyDao().insertHistory(History(null,keyword))
        }.start()
    }

    private fun showHistoryView(){

        Thread{
            val keywords = db.historyDao().getAll().reversed()

            runOnUiThread {
                binding.historyRecyclerView.isVisible = true
                historyadapter.submitList(keywords.orEmpty())
            }

        }.start()

        binding.historyRecyclerView.isVisible = true
    }

    private fun hideHistoryView(){
        binding.historyRecyclerView.isVisible = false
    }

    fun initBookrecyclerView(){
        adapter = BookAdapter(itemClickedListener = {
            val intent = Intent(this, DetailActivity::class.java)
            intent.putExtra("bookModel",it)
            startActivity(intent)
        })

        binding.bookRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.bookRecyclerView.adapter = adapter
    }

    companion object{
        private const val TAG = "MainActivity"

    }
}