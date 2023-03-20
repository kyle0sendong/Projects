package com.example.privasee.database.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity( tableName = "restriction",
        foreignKeys = [
            ForeignKey(
                entity = User::class,
                parentColumns = ["id"],
                childColumns = ["userId"],
                onDelete = ForeignKey.CASCADE
            ),
            ForeignKey(
                entity = App::class,
                parentColumns = ["id"],
                childColumns = ["packageId"],
                onDelete = ForeignKey.CASCADE
            )
        ],
        indices = [
            Index(value = ["userId"]),
            Index(value = ["packageId"])
        ]
)

data class Restriction(
    @PrimaryKey (autoGenerate = true) val id : Int,
    val monitored : Boolean,
    val locked : Boolean,
    val userId : Int,
    val packageId : Int
)
