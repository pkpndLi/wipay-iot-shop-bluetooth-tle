package com.example.wipay_iot_shop.transaction

import androidx.room.*
import com.example.testpos.database.transaction.SaleEntity

@Dao
interface TransactionDao {

    @Query("SELECT * FROM TransactionEntity ORDER BY _id DESC LIMIT 1")
    fun getTransaction(): TransactionEntity

//    @Query("SELECT * FROM SaleEntity where _id >= :startId")
//    fun getAllSaleStartWithId(startId: Int) : MutableLiveData<ArrayList<SaleEntity>>

    @Query("SELECT * FROM TransactionEntity where _id = :Id")
    fun getTransactionWithID(Id: Int) : TransactionEntity

    @Insert
    fun insertTransaction(transactionEntity: TransactionEntity)

    @Delete
    fun deleteTransaction(transactionEntity: TransactionEntity)

    @Update
    fun updateTransaction(transactionEntity: TransactionEntity)
}