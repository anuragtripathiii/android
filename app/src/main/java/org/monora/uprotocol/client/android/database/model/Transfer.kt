/*
 * Copyright (C) 2021 Veli Tasalı
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

package org.monora.uprotocol.client.android.database.model

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.CASCADE
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize
import org.monora.uprotocol.core.transfer.TransferItem

@Parcelize
@Entity(
    tableName = "transfer",
    foreignKeys = [
        ForeignKey(
            entity = UClient::class, parentColumns = ["uid"], childColumns = ["clientUid"], onDelete = CASCADE
        ),
    ]
)
data class Transfer(
    @PrimaryKey
    val id: Long,
    @ColumnInfo(index = true)
    val clientUid: String,
    val type: TransferItem.Type,
    var location: String,
    var accepted: Boolean = false,
    val dateCreated: Long = System.currentTimeMillis(),
) : Parcelable