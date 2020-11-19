package co.tcc.koga.android.data.database.entity

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize

@Parcelize
@Entity(tableName = "user")
data class UserEntity(
    @PrimaryKey
    val username: String,
    val email: String,
    val fullName: String,
    val phone: String,
    val companyId: String,
    val avatar: String?
) : Parcelable