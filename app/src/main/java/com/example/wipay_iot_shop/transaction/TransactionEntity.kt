package com.example.wipay_iot_shop.transaction

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import org.jetbrains.annotations.NotNull

@Entity
data class TransactionEntity(

    @PrimaryKey(autoGenerate = true)
    @NotNull
    var _id: Int?,

    @ColumnInfo(name = "iso_msg")
    val isoMsg: String?,

    @ColumnInfo(name = "iso_msg_tle")
    val isoMsgTle: String?,

    @ColumnInfo(name = "STAN")
    val STAN: Int?

)
