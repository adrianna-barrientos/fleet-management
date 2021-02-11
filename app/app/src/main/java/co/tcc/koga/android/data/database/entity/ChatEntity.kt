package co.tcc.koga.android.data.database.entity

import android.os.Parcelable
import androidx.room.*
import kotlinx.android.parcel.Parcelize

@Parcelize
@Entity(tableName = "chat")
data class ChatEntity(
    @PrimaryKey
    var id: String,
    var newMessages: Long = 0L,
    var groupName: String? = "",
    var avatar: String? = "",
    var createdAt: String? = "",
    var admin: String? = "",
    var user: UserEntity? = null,
    var members: List<UserEntity>? = null,
    var messages: MutableList<MessageEntity?> = mutableListOf()
) : Parcelable {
}