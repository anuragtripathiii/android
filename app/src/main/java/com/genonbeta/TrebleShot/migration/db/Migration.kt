/*
 * Copyright (C) 2019 Veli Tasalı
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
package com.genonbeta.TrebleShot.migration.db

import com.genonbeta.TrebleShot.dataobject.MappedSelectable.Companion.compileFrom
import com.genonbeta.TrebleShot.dataobject.Identity.Companion.withORs
import com.genonbeta.TrebleShot.dataobject.Identifier.Companion.from
import com.genonbeta.TrebleShot.dataobject.TransferIndex.bytesPending
import com.genonbeta.TrebleShot.dataobject.TransferItem.Flag.bytesValue
import com.genonbeta.TrebleShot.dataobject.TransferItem.flag
import com.genonbeta.TrebleShot.dataobject.TransferItem.putFlag
import com.genonbeta.TrebleShot.dataobject.Identity.Companion.withANDs
import com.genonbeta.TrebleShot.dataobject.TransferItem.Companion.from
import com.genonbeta.TrebleShot.dataobject.DeviceAddress.hostAddress
import com.genonbeta.TrebleShot.dataobject.Container.expand
import com.genonbeta.TrebleShot.dataobject.Device.equals
import com.genonbeta.TrebleShot.dataobject.TransferItem.flags
import com.genonbeta.TrebleShot.dataobject.TransferItem.getFlag
import com.genonbeta.TrebleShot.dataobject.TransferItem.Flag.toString
import com.genonbeta.TrebleShot.dataobject.TransferItem.reconstruct
import com.genonbeta.TrebleShot.dataobject.Device.generatePictureId
import com.genonbeta.TrebleShot.dataobject.TransferItem.setDeleteOnRemoval
import com.genonbeta.TrebleShot.dataobject.MappedSelectable.selectableTitle
import com.genonbeta.TrebleShot.dataobject.TransferIndex.hasOutgoing
import com.genonbeta.TrebleShot.dataobject.TransferIndex.hasIncoming
import com.genonbeta.TrebleShot.dataobject.Comparable.comparisonSupported
import com.genonbeta.TrebleShot.dataobject.Comparable.comparableDate
import com.genonbeta.TrebleShot.dataobject.Comparable.comparableSize
import com.genonbeta.TrebleShot.dataobject.Comparable.comparableName
import com.genonbeta.TrebleShot.dataobject.Editable.applyFilter
import com.genonbeta.TrebleShot.dataobject.Editable.id
import com.genonbeta.TrebleShot.dataobject.Shareable.setSelectableSelected
import com.genonbeta.TrebleShot.dataobject.Shareable.initialize
import com.genonbeta.TrebleShot.dataobject.Shareable.isSelectableSelected
import com.genonbeta.TrebleShot.dataobject.Shareable.comparisonSupported
import com.genonbeta.TrebleShot.dataobject.Shareable.comparableSize
import com.genonbeta.TrebleShot.dataobject.Shareable.applyFilter
import com.genonbeta.TrebleShot.dataobject.Device.hashCode
import com.genonbeta.TrebleShot.dataobject.TransferIndex.percentage
import com.genonbeta.TrebleShot.dataobject.TransferIndex.getMemberAsTitle
import com.genonbeta.TrebleShot.dataobject.TransferIndex.isSelectableSelected
import com.genonbeta.TrebleShot.dataobject.TransferIndex.numberOfCompleted
import com.genonbeta.TrebleShot.dataobject.TransferIndex.numberOfTotal
import com.genonbeta.TrebleShot.dataobject.TransferIndex.bytesTotal
import com.genonbeta.TrebleShot.dataobject.TransferItem.isSelectableSelected
import com.genonbeta.TrebleShot.dataobject.TransferItem.setSelectableSelected
import com.genonbeta.TrebleShot.dataobject.TransferItem.senderFlagList
import com.genonbeta.TrebleShot.dataobject.TransferItem.getPercentage
import com.genonbeta.TrebleShot.dataobject.TransferItem.setId
import com.genonbeta.TrebleShot.dataobject.TransferItem.comparableDate
import com.genonbeta.TrebleShot.dataobject.Identity.equals
import com.genonbeta.TrebleShot.dataobject.Transfer.equals
import com.genonbeta.TrebleShot.dataobject.TransferMember.reconstruct
import android.os.Parcelable
import android.os.Parcel
import com.genonbeta.TrebleShot.io.Containable
import android.os.Parcelable.Creator
import com.genonbeta.TrebleShot.R
import android.content.DialogInterface
import com.genonbeta.TrebleShot.activity.AddDeviceActivity.AvailableFragment
import android.content.Intent
import com.genonbeta.TrebleShot.activity.AddDeviceActivity
import androidx.annotation.DrawableRes
import com.genonbeta.android.framework.util.actionperformer.PerformerEngineProvider
import com.genonbeta.TrebleShot.ui.callback.LocalSharingCallback
import com.genonbeta.android.framework.ui.PerformerMenu
import android.view.MenuInflater
import com.genonbeta.android.framework.util.actionperformer.IPerformerEngine
import com.genonbeta.TrebleShot.ui.callback.SharingPerformerMenuCallback
import com.genonbeta.TrebleShot.dialog.ChooseSharingMethodDialog
import com.genonbeta.TrebleShot.dialog.ChooseSharingMethodDialog.PickListener
import com.genonbeta.TrebleShot.dialog.ChooseSharingMethodDialog.SharingMethod
import com.genonbeta.TrebleShot.task.OrganizeLocalSharingTask
import com.genonbeta.TrebleShot.App
import com.genonbeta.TrebleShot.util.NotificationUtils
import com.genonbeta.TrebleShot.database.Kuick
import com.genonbeta.TrebleShot.util.AppUtils
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import com.genonbeta.TrebleShot.service.backgroundservice.BaseAttachableAsyncTask
import android.content.IntentFilter
import android.content.BroadcastReceiver
import android.os.Bundle
import androidx.annotation.StyleRes
import android.content.pm.PackageManager
import com.genonbeta.TrebleShot.activity.WelcomeActivity
import com.genonbeta.TrebleShot.GlideApp
import com.bumptech.glide.request.target.CustomTarget
import android.graphics.drawable.Drawable
import android.graphics.Bitmap
import com.genonbeta.TrebleShot.config.AppConfig
import kotlin.jvm.Synchronized
import com.genonbeta.TrebleShot.service.BackgroundService
import android.os.PowerManager
import android.graphics.BitmapFactory
import com.genonbeta.TrebleShot.dialog.RationalePermissionRequest
import com.genonbeta.TrebleShot.service.backgroundservice.AttachedTaskListener
import com.genonbeta.TrebleShot.service.backgroundservice.AttachableAsyncTask
import com.genonbeta.TrebleShot.dialog.ProfileEditorDialog
import android.widget.ProgressBar
import android.view.LayoutInflater
import kotlin.jvm.JvmOverloads
import com.genonbeta.android.framework.widget.RecyclerViewAdapter
import com.genonbeta.TrebleShot.widget.EditableListAdapter
import com.genonbeta.android.framework.app.DynamicRecyclerViewFragment
import com.genonbeta.TrebleShot.app.IEditableListFragment
import com.genonbeta.android.framework.util.actionperformer.IEngineConnection
import com.genonbeta.android.framework.util.actionperformer.EngineConnection
import com.genonbeta.android.framework.util.actionperformer.PerformerEngine
import com.genonbeta.TrebleShot.app.EditableListFragment.FilteringDelegate
import android.database.ContentObserver
import com.genonbeta.TrebleShot.app.EditableListFragment.LayoutClickListener
import com.genonbeta.TrebleShot.app.EditableListFragmentBase
import com.genonbeta.TrebleShot.app.EditableListFragment
import android.view.ViewGroup
import com.genonbeta.TrebleShot.view.LongTextBubbleFastScrollViewProvider
import com.genonbeta.TrebleShot.widget.recyclerview.ItemOffsetDecoration
import com.genonbeta.TrebleShot.widget.EditableListAdapterBase
import android.os.Looper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.GridLayoutManager.SpanSizeLookup
import android.view.View.OnLongClickListener
import androidx.collection.ArrayMap
import com.genonbeta.TrebleShot.dataobject.*
import com.genonbeta.android.framework.util.actionperformer.SelectableNotFoundException
import com.genonbeta.android.framework.util.actionperformer.CouldNotAlterException
import com.genonbeta.TrebleShot.widget.recyclerview.SwipeSelectionListener
import com.genonbeta.TrebleShot.util.SelectionUtils
import com.genonbeta.TrebleShot.dialog.SelectionEditorDialog
import com.genonbeta.TrebleShot.migration.db.`object`.TransferObjectV12
import com.genonbeta.android.database.SQLType
import com.genonbeta.android.framework.util.actionperformer.IBaseEngineConnection
import com.genonbeta.android.framework.``object`
import java.lang.Exception
import java.util.ArrayList

/**
 * created by: veli
 * date: 7/31/19 12:02 PM
 */
object Migration {
    fun migrate(kuick: Kuick, db: SQLiteDatabase, old: Int, current: Int) {
        val tables: SQLValues = Kuick.Companion.tables()
        val tables12: SQLValues = v12.tables(tables)
        when (old) {
            0, 1, 2, 3, 4, 5 -> {
                for (tableName in tables.getTables().keys) db.execSQL("DROP TABLE IF EXISTS `$tableName`")
                SQLQuery.createTables(db, tables12)
            }
            6 -> {
                val groupTable: SQLValues.Table = tables12.getTable(Kuick.Companion.TABLE_TRANSFER)
                val devicesTable: SQLValues.Table = tables12.getTable(Kuick.Companion.TABLE_DEVICES)
                val targetDevicesTable: SQLValues.Table = tables12.getTable(Kuick.Companion.TABLE_TRANSFERMEMBER)
                db.execSQL("DROP TABLE IF EXISTS `" + groupTable.getName() + "`")
                db.execSQL("DROP TABLE IF EXISTS `" + devicesTable.getName() + "`")
                SQLQuery.createTable(db, groupTable)
                SQLQuery.createTable(db, devicesTable)
                SQLQuery.createTable(db, targetDevicesTable)
                // With version 9, I added deviceId column to the transfer table
                // With version 10, DIVISION section added for TABLE_TRANSFER and made deviceId nullable
                // to allow users distinguish individual transfer file
                try {
                    val tableTransfer: SQLValues.Table = tables12.getTable(Kuick.Companion.TABLE_TRANSFERITEM)
                    val divisTransfer: SQLValues.Table = tables12.getTable(v12.TABLE_DIVISTRANSFER)
                    val mapDist: MutableMap<Long, String> = ArrayMap()
                    val supportedItems: MutableList<TransferObjectV12> = ArrayList<TransferObjectV12>()
                    val availableAssignees: List<TransferAssigneeV12> =
                        kuick.castQuery<NetworkDeviceV12, TransferAssigneeV12>(
                            db,
                            SQLQuery.Select(Kuick.Companion.TABLE_TRANSFERMEMBER),
                            TransferAssigneeV12::class.java, null
                        )
                    val availableTransfers: List<TransferObjectV12> =
                        kuick.castQuery<TransferGroupV12, TransferObjectV12>(
                            db,
                            SQLQuery.Select(Kuick.Companion.TABLE_TRANSFERITEM), TransferObjectV12::class.java, null
                        )
                    for (assignee in availableAssignees) {
                        if (!mapDist.containsKey(assignee.groupId)) mapDist[assignee.groupId] = assignee.deviceId
                    }
                    for (transferObject in availableTransfers) {
                        transferObject.deviceId = mapDist[transferObject.groupId]
                        if (transferObject.deviceId != null) supportedItems.add(transferObject)
                    }
                    db.execSQL("DROP TABLE IF EXISTS `" + tableTransfer.getName() + "`")
                    SQLQuery.createTable(db, tableTransfer)
                    SQLQuery.createTable(db, divisTransfer)
                    kuick.insert(db, supportedItems, null, null)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                val tableFileBookmark: SQLValues.Table = tables12.getTable(Kuick.Companion.TABLE_FILEBOOKMARK)
                SQLQuery.createTable(db, tableFileBookmark)
                val totalGroupList = kuick.castQuery(
                    db, SQLQuery.Select(
                        Kuick.Companion.TABLE_TRANSFER
                    ), Transfer::class.java, null
                )
                val tableTransferGroup: SQLValues.Table = tables12.getTable(Kuick.Companion.TABLE_TRANSFER)
                db.execSQL("DROP TABLE IF EXISTS `" + tableTransferGroup.getName() + "`")
                SQLQuery.createTable(db, tableTransferGroup)
                kuick.insert(db, totalGroupList, null, null)
                run {
                    run {
                        val table: SQLValues.Table = tables.getTable(Kuick.Companion.TABLE_DEVICES)
                        val typeColumn: Column = table.getColumn(Kuick.Companion.FIELD_DEVICES_TYPE)
                        val clientVerCol: Column = table.getColumn(Kuick.Companion.FIELD_DEVICES_PROTOCOLVERSIONMIN)

                        // Added: Type
                        db.execSQL(
                            "ALTER TABLE " + table.getName() + " ADD " + typeColumn.getName()
                                    + " " + typeColumn.getType()
                                .toString() + (if (typeColumn.isNullable()) " NOT" else "")
                                    + " NULL DEFAULT " + Device.Type.NORMAL.toString()
                        )

                        // Added: ClientVersion
                        db.execSQL(
                            "ALTER TABLE " + table.getName() + " ADD " + clientVerCol.getName()
                                    + " " + clientVerCol.getType()
                                .toString() + (if (clientVerCol.isNullable()) " NOT" else "")
                                    + " NULL DEFAULT 0"
                        )
                    }
                    run {
                        val oldList: List<TransferAssigneeV12> = kuick.castQuery<NetworkDeviceV12, TransferAssigneeV12>(
                            db, SQLQuery.Select(
                                Kuick.Companion.TABLE_TRANSFERMEMBER
                            ), TransferAssigneeV12::class.java, null
                        )

                        // Added: Type, Removed: IsClone
                        db.execSQL("DROP TABLE IF EXISTS `" + Kuick.Companion.TABLE_TRANSFERMEMBER + "`")
                        SQLQuery.createTable(db, tables.getTable(Kuick.Companion.TABLE_TRANSFERMEMBER))
                        val newAssignees: MutableList<TransferMember> = ArrayList<TransferMember>()

                        // The `transfer` table will be removed below. We can use the old versions
                        // columns still.
                        for (assigneeV12 in oldList) {
                            val selection: SQLQuery.Select = SQLQuery.Select(Kuick.Companion.TABLE_TRANSFERITEM)
                            selection.setWhere(
                                Kuick.Companion.FIELD_TRANSFERITEM_TYPE + "=? AND "
                                        + Kuick.Companion.FIELD_TRANSFERITEM_TRANSFERID + "=? AND " + v12.FIELD_TRANSFER_DEVICEID
                                        + "=?", TransferObjectV12.Type.INCOMING.toString(),
                                assigneeV12.groupId.toString(), assigneeV12.deviceId
                            )
                            if (kuick.getFirstFromTable(db, selection) != null) {
                                val incomingMember = TransferMember()
                                incomingMember.reconstruct(db, kuick, assigneeV12.getValues())
                                incomingMember.type = TransferItem.Type.INCOMING
                                newAssignees.add(incomingMember)
                            }
                            selection.setWhere(
                                Kuick.Companion.FIELD_TRANSFERITEM_TYPE + "=? AND "
                                        + Kuick.Companion.FIELD_TRANSFERITEM_TRANSFERID + "=? AND " + v12.FIELD_TRANSFER_DEVICEID
                                        + "=?", TransferObjectV12.Type.OUTGOING.toString(),
                                assigneeV12.groupId.toString(), assigneeV12.deviceId
                            )
                            if (kuick.getFirstFromTable(db, selection) != null) {
                                val outgoingMember = TransferMember()
                                outgoingMember.reconstruct(db, kuick, assigneeV12.getValues())
                                outgoingMember.type = TransferItem.Type.OUTGOING
                                newAssignees.add(outgoingMember)
                            }
                        }
                        if (newAssignees.size > 0) kuick.insert(db, newAssignees, null, null)
                    }
                    run {
                        val table: SQLValues.Table = tables.getTable(Kuick.Companion.TABLE_TRANSFERITEM)

                        // Changed Flag as Flag[] for Type.OUTGOING objects
                        val outgoingBaseObjects: List<TransferObjectV12> =
                            kuick.castQuery<TransferGroupV12, TransferObjectV12>(
                                db, SQLQuery.Select(
                                    v12.TABLE_DIVISTRANSFER
                                ), TransferObjectV12::class.java, null
                            )
                        val outgoingMirrorObjects: List<TransferObjectV12> =
                            kuick.castQuery<TransferGroupV12, TransferObjectV12>(
                                db, SQLQuery.Select(
                                    Kuick.Companion.TABLE_TRANSFERITEM
                                ).setWhere(
                                    Kuick.Companion.FIELD_TRANSFERITEM_TYPE + "=?",
                                    TransferObjectV12.Type.OUTGOING.toString()
                                ), TransferObjectV12::class.java, null
                            )
                        val incomingObjects: List<TransferObjectV12> =
                            kuick.castQuery<TransferGroupV12, TransferObjectV12>(
                                db, SQLQuery.Select(
                                    Kuick.Companion.TABLE_TRANSFERITEM
                                ).setWhere(
                                    Kuick.Companion.FIELD_TRANSFERITEM_TYPE + "=?",
                                    TransferObjectV12.Type.INCOMING.toString()
                                ), TransferObjectV12::class.java, null
                            )

                        // Remove: Table `divisTransfer`
                        db.execSQL("DROP TABLE IF EXISTS `" + v12.TABLE_DIVISTRANSFER + "`")

                        // Added: LastChangeTime, Removed: AccessPort, SkippedBytes
                        db.execSQL("DROP TABLE IF EXISTS `" + Kuick.Companion.TABLE_TRANSFERITEM + "`")
                        SQLQuery.createTable(db, table)
                        if (outgoingBaseObjects.size > 0) {
                            val newObjects: MutableMap<Long, TransferItem> = ArrayMap()
                            for (objectV12 in outgoingBaseObjects) {
                                var `object` = newObjects[objectV12.requestId]
                                if (`object` != null) continue
                                `object` = TransferItem()
                                `object`.reconstruct(db, kuick, objectV12.getValues())
                                newObjects[objectV12.requestId] = `object`
                            }
                            for (objectV12 in outgoingMirrorObjects) {
                                val `object` = newObjects[objectV12.requestId] ?: continue
                                try {
                                    `object`.putFlag(
                                        objectV12.deviceId, TransferItem.Flag.valueOf(
                                            objectV12.flag.toString()
                                        )
                                    )
                                } catch (ignored: Exception) {
                                }
                            }
                            if (newObjects.size > 0) kuick.insert(db, ArrayList(newObjects.values), null, null)
                        }
                        if (incomingObjects.size > 0) {
                            val newIncomingInstances: List<TransferItem> = ArrayList()
                            for (objectV12 in incomingObjects) {
                                val newObject = TransferItem()
                                newObject.reconstruct(db, kuick, objectV12.getValues())
                            }
                            kuick.insert(db, newIncomingInstances, null, null)
                        }
                    }
                    run {
                        val table: SQLValues.Table = tables.getTable(Kuick.Companion.TABLE_TRANSFER)
                        val column: Column = table.getColumn(Kuick.Companion.FIELD_TRANSFER_ISPAUSED)

                        // Added: IsPaused
                        db.execSQL(
                            "ALTER TABLE " + table.getName() + " ADD " + column.getName()
                                    + " " + column.getType().toString() + (if (column.isNullable()) " NOT" else "")
                                    + " NULL DEFAULT " + Device.Type.NORMAL.toString()
                        )
                    }
                    run {

                        // Writable path and bookmark objects have been dropped.
                        // The remaining table (of bookmarks) is used to store both, while the object
                        // driving them is reduced to FileHolder.
                        val pathObjectList: List<WritablePathObjectV12> = kuick.castQuery<Any, WritablePathObjectV12>(
                            db, SQLQuery.Select(
                                v12.TABLE_WRITABLEPATH
                            ), WritablePathObjectV12::class.java, null
                        )
                        if (pathObjectList.size > 0) {
                            val fileHolderList: MutableList<FileHolder> = ArrayList<FileHolder>()
                            for (pathObject in pathObjectList) {
                                val fileHolder = FileHolder()
                                fileHolder.reconstruct(db, kuick, pathObject.getValues())
                                fileHolderList.add(fileHolder)
                            }
                            kuick.insert(db, fileHolderList, null, null)
                        }
                        db.execSQL("DROP TABLE IF EXISTS `" + v12.TABLE_WRITABLEPATH + "`")
                    }
                }
            }
            7, 8, 9, 10 -> {
                try {
                    val tableTransfer: SQLValues.Table = tables12.getTable(Kuick.Companion.TABLE_TRANSFERITEM)
                    val divisTransfer: SQLValues.Table = tables12.getTable(v12.TABLE_DIVISTRANSFER)
                    val mapDist: MutableMap<Long, String> = ArrayMap()
                    val supportedItems: MutableList<TransferObjectV12> = ArrayList<TransferObjectV12>()
                    val availableAssignees: List<TransferAssigneeV12> =
                        kuick.castQuery<NetworkDeviceV12, TransferAssigneeV12>(
                            db,
                            SQLQuery.Select(Kuick.Companion.TABLE_TRANSFERMEMBER),
                            TransferAssigneeV12::class.java, null
                        )
                    val availableTransfers: List<TransferObjectV12> =
                        kuick.castQuery<TransferGroupV12, TransferObjectV12>(
                            db,
                            SQLQuery.Select(Kuick.Companion.TABLE_TRANSFERITEM), TransferObjectV12::class.java, null
                        )
                    for (assignee in availableAssignees) {
                        if (!mapDist.containsKey(assignee.groupId)) mapDist[assignee.groupId] = assignee.deviceId
                    }
                    for (transferObject in availableTransfers) {
                        transferObject.deviceId = mapDist[transferObject.groupId]
                        if (transferObject.deviceId != null) supportedItems.add(transferObject)
                    }
                    db.execSQL("DROP TABLE IF EXISTS `" + tableTransfer.getName() + "`")
                    SQLQuery.createTable(db, tableTransfer)
                    SQLQuery.createTable(db, divisTransfer)
                    kuick.insert(db, supportedItems, null, null)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                val tableFileBookmark: SQLValues.Table = tables12.getTable(Kuick.Companion.TABLE_FILEBOOKMARK)
                SQLQuery.createTable(db, tableFileBookmark)
                val totalGroupList = kuick.castQuery(
                    db, SQLQuery.Select(
                        Kuick.Companion.TABLE_TRANSFER
                    ), Transfer::class.java, null
                )
                val tableTransferGroup: SQLValues.Table = tables12.getTable(Kuick.Companion.TABLE_TRANSFER)
                db.execSQL("DROP TABLE IF EXISTS `" + tableTransferGroup.getName() + "`")
                SQLQuery.createTable(db, tableTransferGroup)
                kuick.insert(db, totalGroupList, null, null)
                run {
                    run {
                        val table: SQLValues.Table = tables.getTable(Kuick.Companion.TABLE_DEVICES)
                        val typeColumn: Column = table.getColumn(Kuick.Companion.FIELD_DEVICES_TYPE)
                        val clientVerCol: Column = table.getColumn(Kuick.Companion.FIELD_DEVICES_PROTOCOLVERSIONMIN)
                        db.execSQL(
                            "ALTER TABLE " + table.getName() + " ADD " + typeColumn.getName()
                                    + " " + typeColumn.getType()
                                .toString() + (if (typeColumn.isNullable()) " NOT" else "")
                                    + " NULL DEFAULT " + Device.Type.NORMAL.toString()
                        )
                        db.execSQL(
                            "ALTER TABLE " + table.getName() + " ADD " + clientVerCol.getName()
                                    + " " + clientVerCol.getType()
                                .toString() + (if (clientVerCol.isNullable()) " NOT" else "")
                                    + " NULL DEFAULT 0"
                        )
                    }
                    run {
                        val oldList: List<TransferAssigneeV12> = kuick.castQuery<NetworkDeviceV12, TransferAssigneeV12>(
                            db, SQLQuery.Select(
                                Kuick.Companion.TABLE_TRANSFERMEMBER
                            ), TransferAssigneeV12::class.java, null
                        )
                        db.execSQL("DROP TABLE IF EXISTS `" + Kuick.Companion.TABLE_TRANSFERMEMBER + "`")
                        SQLQuery.createTable(db, tables.getTable(Kuick.Companion.TABLE_TRANSFERMEMBER))
                        val newAssignees: MutableList<TransferMember> = ArrayList<TransferMember>()
                        for (assigneeV12 in oldList) {
                            val selection: SQLQuery.Select = SQLQuery.Select(Kuick.Companion.TABLE_TRANSFERITEM)
                            selection.setWhere(
                                Kuick.Companion.FIELD_TRANSFERITEM_TYPE + "=? AND "
                                        + Kuick.Companion.FIELD_TRANSFERITEM_TRANSFERID + "=? AND " + v12.FIELD_TRANSFER_DEVICEID
                                        + "=?", TransferObjectV12.Type.INCOMING.toString(),
                                assigneeV12.groupId.toString(), assigneeV12.deviceId
                            )
                            if (kuick.getFirstFromTable(db, selection) != null) {
                                val incomingMember = TransferMember()
                                incomingMember.reconstruct(db, kuick, assigneeV12.getValues())
                                incomingMember.type = TransferItem.Type.INCOMING
                                newAssignees.add(incomingMember)
                            }
                            selection.setWhere(
                                Kuick.Companion.FIELD_TRANSFERITEM_TYPE + "=? AND "
                                        + Kuick.Companion.FIELD_TRANSFERITEM_TRANSFERID + "=? AND " + v12.FIELD_TRANSFER_DEVICEID
                                        + "=?", TransferObjectV12.Type.OUTGOING.toString(),
                                assigneeV12.groupId.toString(), assigneeV12.deviceId
                            )
                            if (kuick.getFirstFromTable(db, selection) != null) {
                                val outgoingMember = TransferMember()
                                outgoingMember.reconstruct(db, kuick, assigneeV12.getValues())
                                outgoingMember.type = TransferItem.Type.OUTGOING
                                newAssignees.add(outgoingMember)
                            }
                        }
                        if (newAssignees.size > 0) kuick.insert(db, newAssignees, null, null)
                    }
                    run {
                        val table: SQLValues.Table = tables.getTable(Kuick.Companion.TABLE_TRANSFERITEM)
                        val outgoingBaseObjects: List<TransferObjectV12> =
                            kuick.castQuery<TransferGroupV12, TransferObjectV12>(
                                db, SQLQuery.Select(
                                    v12.TABLE_DIVISTRANSFER
                                ), TransferObjectV12::class.java, null
                            )
                        val outgoingMirrorObjects: List<TransferObjectV12> =
                            kuick.castQuery<TransferGroupV12, TransferObjectV12>(
                                db, SQLQuery.Select(
                                    Kuick.Companion.TABLE_TRANSFERITEM
                                ).setWhere(
                                    Kuick.Companion.FIELD_TRANSFERITEM_TYPE + "=?",
                                    TransferObjectV12.Type.OUTGOING.toString()
                                ), TransferObjectV12::class.java, null
                            )
                        val incomingObjects: List<TransferObjectV12> =
                            kuick.castQuery<TransferGroupV12, TransferObjectV12>(
                                db, SQLQuery.Select(
                                    Kuick.Companion.TABLE_TRANSFERITEM
                                ).setWhere(
                                    Kuick.Companion.FIELD_TRANSFERITEM_TYPE + "=?",
                                    TransferObjectV12.Type.INCOMING.toString()
                                ), TransferObjectV12::class.java, null
                            )
                        db.execSQL("DROP TABLE IF EXISTS `" + v12.TABLE_DIVISTRANSFER + "`")
                        db.execSQL("DROP TABLE IF EXISTS `" + Kuick.Companion.TABLE_TRANSFERITEM + "`")
                        SQLQuery.createTable(db, table)
                        if (outgoingBaseObjects.size > 0) {
                            val newObjects: MutableMap<Long, TransferItem> = ArrayMap()
                            for (objectV12 in outgoingBaseObjects) {
                                var `object` = newObjects[objectV12.requestId]
                                if (`object` != null) continue
                                `object` = TransferItem()
                                `object`!!.reconstruct(db, kuick, objectV12.getValues())
                                newObjects[objectV12.requestId] = `object`!!
                            }
                            for (objectV12 in outgoingMirrorObjects) {
                                val `object` = newObjects[objectV12.requestId] ?: continue
                                try {
                                    `object`.putFlag(
                                        objectV12.deviceId, TransferItem.Flag.valueOf(
                                            objectV12.flag.toString()
                                        )
                                    )
                                } catch (ignored: Exception) {
                                }
                            }
                            if (newObjects.size > 0) kuick.insert(db, ArrayList(newObjects.values), null, null)
                        }
                        if (incomingObjects.size > 0) {
                            val newIncomingInstances: List<TransferItem> = ArrayList()
                            for (objectV12 in incomingObjects) {
                                val newObject = TransferItem()
                                newObject.reconstruct(db, kuick, objectV12.getValues())
                            }
                            kuick.insert(db, newIncomingInstances, null, null)
                        }
                    }
                    run {
                        val table: SQLValues.Table = tables.getTable(Kuick.Companion.TABLE_TRANSFER)
                        val column: Column = table.getColumn(Kuick.Companion.FIELD_TRANSFER_ISPAUSED)
                        db.execSQL(
                            "ALTER TABLE " + table.getName() + " ADD " + column.getName()
                                    + " " + column.getType().toString() + (if (column.isNullable()) " NOT" else "")
                                    + " NULL DEFAULT " + Device.Type.NORMAL.toString()
                        )
                    }
                    run {
                        val pathObjectList: List<WritablePathObjectV12> = kuick.castQuery<Any, WritablePathObjectV12>(
                            db, SQLQuery.Select(
                                v12.TABLE_WRITABLEPATH
                            ), WritablePathObjectV12::class.java, null
                        )
                        if (pathObjectList.size > 0) {
                            val fileHolderList: MutableList<FileHolder> = ArrayList<FileHolder>()
                            for (pathObject in pathObjectList) {
                                val fileHolder = FileHolder()
                                fileHolder.reconstruct(db, kuick, pathObject.getValues())
                                fileHolderList.add(fileHolder)
                            }
                            kuick.insert(db, fileHolderList, null, null)
                        }
                        db.execSQL("DROP TABLE IF EXISTS `" + v12.TABLE_WRITABLEPATH + "`")
                    }
                }
            }
            11 -> {
                val tableFileBookmark: SQLValues.Table = tables12.getTable(Kuick.Companion.TABLE_FILEBOOKMARK)
                SQLQuery.createTable(db, tableFileBookmark)
                val totalGroupList = kuick.castQuery(
                    db, SQLQuery.Select(
                        Kuick.Companion.TABLE_TRANSFER
                    ), Transfer::class.java, null
                )
                val tableTransferGroup: SQLValues.Table = tables12.getTable(Kuick.Companion.TABLE_TRANSFER)
                db.execSQL("DROP TABLE IF EXISTS `" + tableTransferGroup.getName() + "`")
                SQLQuery.createTable(db, tableTransferGroup)
                kuick.insert(db, totalGroupList, null, null)
                run {
                    run {
                        val table: SQLValues.Table = tables.getTable(Kuick.Companion.TABLE_DEVICES)
                        val typeColumn: Column = table.getColumn(Kuick.Companion.FIELD_DEVICES_TYPE)
                        val clientVerCol: Column = table.getColumn(Kuick.Companion.FIELD_DEVICES_PROTOCOLVERSIONMIN)
                        db.execSQL(
                            "ALTER TABLE " + table.getName() + " ADD " + typeColumn.getName()
                                    + " " + typeColumn.getType()
                                .toString() + (if (typeColumn.isNullable()) " NOT" else "")
                                    + " NULL DEFAULT " + Device.Type.NORMAL.toString()
                        )
                        db.execSQL(
                            "ALTER TABLE " + table.getName() + " ADD " + clientVerCol.getName()
                                    + " " + clientVerCol.getType()
                                .toString() + (if (clientVerCol.isNullable()) " NOT" else "")
                                    + " NULL DEFAULT 0"
                        )
                    }
                    run {
                        val oldList: List<TransferAssigneeV12> = kuick.castQuery<NetworkDeviceV12, TransferAssigneeV12>(
                            db, SQLQuery.Select(
                                Kuick.Companion.TABLE_TRANSFERMEMBER
                            ), TransferAssigneeV12::class.java, null
                        )
                        db.execSQL("DROP TABLE IF EXISTS `" + Kuick.Companion.TABLE_TRANSFERMEMBER + "`")
                        SQLQuery.createTable(db, tables.getTable(Kuick.Companion.TABLE_TRANSFERMEMBER))
                        val newAssignees: MutableList<TransferMember> = ArrayList<TransferMember>()
                        for (assigneeV12 in oldList) {
                            val selection: SQLQuery.Select = SQLQuery.Select(Kuick.Companion.TABLE_TRANSFERITEM)
                            selection.setWhere(
                                Kuick.Companion.FIELD_TRANSFERITEM_TYPE + "=? AND "
                                        + Kuick.Companion.FIELD_TRANSFERITEM_TRANSFERID + "=? AND " + v12.FIELD_TRANSFER_DEVICEID
                                        + "=?", TransferObjectV12.Type.INCOMING.toString(),
                                assigneeV12.groupId.toString(), assigneeV12.deviceId
                            )
                            if (kuick.getFirstFromTable(db, selection) != null) {
                                val incomingMember = TransferMember()
                                incomingMember.reconstruct(db, kuick, assigneeV12.getValues())
                                incomingMember.type = TransferItem.Type.INCOMING
                                newAssignees.add(incomingMember)
                            }
                            selection.setWhere(
                                Kuick.Companion.FIELD_TRANSFERITEM_TYPE + "=? AND "
                                        + Kuick.Companion.FIELD_TRANSFERITEM_TRANSFERID + "=? AND " + v12.FIELD_TRANSFER_DEVICEID
                                        + "=?", TransferObjectV12.Type.OUTGOING.toString(),
                                assigneeV12.groupId.toString(), assigneeV12.deviceId
                            )
                            if (kuick.getFirstFromTable(db, selection) != null) {
                                val outgoingMember = TransferMember()
                                outgoingMember.reconstruct(db, kuick, assigneeV12.getValues())
                                outgoingMember.type = TransferItem.Type.OUTGOING
                                newAssignees.add(outgoingMember)
                            }
                        }
                        if (newAssignees.size > 0) kuick.insert(db, newAssignees, null, null)
                    }
                    run {
                        val table: SQLValues.Table = tables.getTable(Kuick.Companion.TABLE_TRANSFERITEM)
                        val outgoingBaseObjects: List<TransferObjectV12> =
                            kuick.castQuery<TransferGroupV12, TransferObjectV12>(
                                db, SQLQuery.Select(
                                    v12.TABLE_DIVISTRANSFER
                                ), TransferObjectV12::class.java, null
                            )
                        val outgoingMirrorObjects: List<TransferObjectV12> =
                            kuick.castQuery<TransferGroupV12, TransferObjectV12>(
                                db, SQLQuery.Select(
                                    Kuick.Companion.TABLE_TRANSFERITEM
                                ).setWhere(
                                    Kuick.Companion.FIELD_TRANSFERITEM_TYPE + "=?",
                                    TransferObjectV12.Type.OUTGOING.toString()
                                ), TransferObjectV12::class.java, null
                            )
                        val incomingObjects: List<TransferObjectV12> =
                            kuick.castQuery<TransferGroupV12, TransferObjectV12>(
                                db, SQLQuery.Select(
                                    Kuick.Companion.TABLE_TRANSFERITEM
                                ).setWhere(
                                    Kuick.Companion.FIELD_TRANSFERITEM_TYPE + "=?",
                                    TransferObjectV12.Type.INCOMING.toString()
                                ), TransferObjectV12::class.java, null
                            )
                        db.execSQL("DROP TABLE IF EXISTS `" + v12.TABLE_DIVISTRANSFER + "`")
                        db.execSQL("DROP TABLE IF EXISTS `" + Kuick.Companion.TABLE_TRANSFERITEM + "`")
                        SQLQuery.createTable(db, table)
                        if (outgoingBaseObjects.size > 0) {
                            val newObjects: MutableMap<Long, TransferItem> = ArrayMap()
                            for (objectV12 in outgoingBaseObjects) {
                                var `object` = newObjects[objectV12.requestId]
                                if (`object` != null) continue
                                `object` = TransferItem()
                                `object`!!.reconstruct(db, kuick, objectV12.getValues())
                                newObjects[objectV12.requestId] = `object`!!
                            }
                            for (objectV12 in outgoingMirrorObjects) {
                                val `object` = newObjects[objectV12.requestId] ?: continue
                                try {
                                    `object`.putFlag(
                                        objectV12.deviceId, TransferItem.Flag.valueOf(
                                            objectV12.flag.toString()
                                        )
                                    )
                                } catch (ignored: Exception) {
                                }
                            }
                            if (newObjects.size > 0) kuick.insert(db, ArrayList(newObjects.values), null, null)
                        }
                        if (incomingObjects.size > 0) {
                            val newIncomingInstances: List<TransferItem> = ArrayList()
                            for (objectV12 in incomingObjects) {
                                val newObject = TransferItem()
                                newObject.reconstruct(db, kuick, objectV12.getValues())
                            }
                            kuick.insert(db, newIncomingInstances, null, null)
                        }
                    }
                    run {
                        val table: SQLValues.Table = tables.getTable(Kuick.Companion.TABLE_TRANSFER)
                        val column: Column = table.getColumn(Kuick.Companion.FIELD_TRANSFER_ISPAUSED)
                        db.execSQL(
                            "ALTER TABLE " + table.getName() + " ADD " + column.getName()
                                    + " " + column.getType().toString() + (if (column.isNullable()) " NOT" else "")
                                    + " NULL DEFAULT " + Device.Type.NORMAL.toString()
                        )
                    }
                    run {
                        val pathObjectList: List<WritablePathObjectV12> = kuick.castQuery<Any, WritablePathObjectV12>(
                            db, SQLQuery.Select(
                                v12.TABLE_WRITABLEPATH
                            ), WritablePathObjectV12::class.java, null
                        )
                        if (pathObjectList.size > 0) {
                            val fileHolderList: MutableList<FileHolder> = ArrayList<FileHolder>()
                            for (pathObject in pathObjectList) {
                                val fileHolder = FileHolder()
                                fileHolder.reconstruct(db, kuick, pathObject.getValues())
                                fileHolderList.add(fileHolder)
                            }
                            kuick.insert(db, fileHolderList, null, null)
                        }
                        db.execSQL("DROP TABLE IF EXISTS `" + v12.TABLE_WRITABLEPATH + "`")
                    }
                }
            }
            12 -> {
                val totalGroupList = kuick.castQuery(
                    db, SQLQuery.Select(
                        Kuick.Companion.TABLE_TRANSFER
                    ), Transfer::class.java, null
                )
                val tableTransferGroup: SQLValues.Table = tables12.getTable(Kuick.Companion.TABLE_TRANSFER)
                db.execSQL("DROP TABLE IF EXISTS `" + tableTransferGroup.getName() + "`")
                SQLQuery.createTable(db, tableTransferGroup)
                kuick.insert(db, totalGroupList, null, null)
                run {
                    run {
                        val table: SQLValues.Table = tables.getTable(Kuick.Companion.TABLE_DEVICES)
                        val typeColumn: Column = table.getColumn(Kuick.Companion.FIELD_DEVICES_TYPE)
                        val clientVerCol: Column = table.getColumn(Kuick.Companion.FIELD_DEVICES_PROTOCOLVERSIONMIN)
                        db.execSQL(
                            "ALTER TABLE " + table.getName() + " ADD " + typeColumn.getName()
                                    + " " + typeColumn.getType()
                                .toString() + (if (typeColumn.isNullable()) " NOT" else "")
                                    + " NULL DEFAULT " + Device.Type.NORMAL.toString()
                        )
                        db.execSQL(
                            "ALTER TABLE " + table.getName() + " ADD " + clientVerCol.getName()
                                    + " " + clientVerCol.getType()
                                .toString() + (if (clientVerCol.isNullable()) " NOT" else "")
                                    + " NULL DEFAULT 0"
                        )
                    }
                    run {
                        val oldList: List<TransferAssigneeV12> = kuick.castQuery<NetworkDeviceV12, TransferAssigneeV12>(
                            db, SQLQuery.Select(
                                Kuick.Companion.TABLE_TRANSFERMEMBER
                            ), TransferAssigneeV12::class.java, null
                        )
                        db.execSQL("DROP TABLE IF EXISTS `" + Kuick.Companion.TABLE_TRANSFERMEMBER + "`")
                        SQLQuery.createTable(db, tables.getTable(Kuick.Companion.TABLE_TRANSFERMEMBER))
                        val newAssignees: MutableList<TransferMember> = ArrayList<TransferMember>()
                        for (assigneeV12 in oldList) {
                            val selection: SQLQuery.Select = SQLQuery.Select(Kuick.Companion.TABLE_TRANSFERITEM)
                            selection.setWhere(
                                Kuick.Companion.FIELD_TRANSFERITEM_TYPE + "=? AND "
                                        + Kuick.Companion.FIELD_TRANSFERITEM_TRANSFERID + "=? AND " + v12.FIELD_TRANSFER_DEVICEID
                                        + "=?", TransferObjectV12.Type.INCOMING.toString(),
                                assigneeV12.groupId.toString(), assigneeV12.deviceId
                            )
                            if (kuick.getFirstFromTable(db, selection) != null) {
                                val incomingMember = TransferMember()
                                incomingMember.reconstruct(db, kuick, assigneeV12.getValues())
                                incomingMember.type = TransferItem.Type.INCOMING
                                newAssignees.add(incomingMember)
                            }
                            selection.setWhere(
                                Kuick.Companion.FIELD_TRANSFERITEM_TYPE + "=? AND "
                                        + Kuick.Companion.FIELD_TRANSFERITEM_TRANSFERID + "=? AND " + v12.FIELD_TRANSFER_DEVICEID
                                        + "=?", TransferObjectV12.Type.OUTGOING.toString(),
                                assigneeV12.groupId.toString(), assigneeV12.deviceId
                            )
                            if (kuick.getFirstFromTable(db, selection) != null) {
                                val outgoingMember = TransferMember()
                                outgoingMember.reconstruct(db, kuick, assigneeV12.getValues())
                                outgoingMember.type = TransferItem.Type.OUTGOING
                                newAssignees.add(outgoingMember)
                            }
                        }
                        if (newAssignees.size > 0) kuick.insert(db, newAssignees, null, null)
                    }
                    run {
                        val table: SQLValues.Table = tables.getTable(Kuick.Companion.TABLE_TRANSFERITEM)
                        val outgoingBaseObjects: List<TransferObjectV12> =
                            kuick.castQuery<TransferGroupV12, TransferObjectV12>(
                                db, SQLQuery.Select(
                                    v12.TABLE_DIVISTRANSFER
                                ), TransferObjectV12::class.java, null
                            )
                        val outgoingMirrorObjects: List<TransferObjectV12> =
                            kuick.castQuery<TransferGroupV12, TransferObjectV12>(
                                db, SQLQuery.Select(
                                    Kuick.Companion.TABLE_TRANSFERITEM
                                ).setWhere(
                                    Kuick.Companion.FIELD_TRANSFERITEM_TYPE + "=?",
                                    TransferObjectV12.Type.OUTGOING.toString()
                                ), TransferObjectV12::class.java, null
                            )
                        val incomingObjects: List<TransferObjectV12> =
                            kuick.castQuery<TransferGroupV12, TransferObjectV12>(
                                db, SQLQuery.Select(
                                    Kuick.Companion.TABLE_TRANSFERITEM
                                ).setWhere(
                                    Kuick.Companion.FIELD_TRANSFERITEM_TYPE + "=?",
                                    TransferObjectV12.Type.INCOMING.toString()
                                ), TransferObjectV12::class.java, null
                            )
                        db.execSQL("DROP TABLE IF EXISTS `" + v12.TABLE_DIVISTRANSFER + "`")
                        db.execSQL("DROP TABLE IF EXISTS `" + Kuick.Companion.TABLE_TRANSFERITEM + "`")
                        SQLQuery.createTable(db, table)
                        if (outgoingBaseObjects.size > 0) {
                            val newObjects: MutableMap<Long, TransferItem> = ArrayMap()
                            for (objectV12 in outgoingBaseObjects) {
                                var `object` = newObjects[objectV12.requestId]
                                if (`object` != null) continue
                                `object` = TransferItem()
                                `object`!!.reconstruct(db, kuick, objectV12.getValues())
                                newObjects[objectV12.requestId] = `object`!!
                            }
                            for (objectV12 in outgoingMirrorObjects) {
                                val `object` = newObjects[objectV12.requestId] ?: continue
                                try {
                                    `object`.putFlag(
                                        objectV12.deviceId, TransferItem.Flag.valueOf(
                                            objectV12.flag.toString()
                                        )
                                    )
                                } catch (ignored: Exception) {
                                }
                            }
                            if (newObjects.size > 0) kuick.insert(db, ArrayList(newObjects.values), null, null)
                        }
                        if (incomingObjects.size > 0) {
                            val newIncomingInstances: List<TransferItem> = ArrayList()
                            for (objectV12 in incomingObjects) {
                                val newObject = TransferItem()
                                newObject.reconstruct(db, kuick, objectV12.getValues())
                            }
                            kuick.insert(db, newIncomingInstances, null, null)
                        }
                    }
                    run {
                        val table: SQLValues.Table = tables.getTable(Kuick.Companion.TABLE_TRANSFER)
                        val column: Column = table.getColumn(Kuick.Companion.FIELD_TRANSFER_ISPAUSED)
                        db.execSQL(
                            "ALTER TABLE " + table.getName() + " ADD " + column.getName()
                                    + " " + column.getType().toString() + (if (column.isNullable()) " NOT" else "")
                                    + " NULL DEFAULT " + Device.Type.NORMAL.toString()
                        )
                    }
                    run {
                        val pathObjectList: List<WritablePathObjectV12> = kuick.castQuery<Any, WritablePathObjectV12>(
                            db, SQLQuery.Select(
                                v12.TABLE_WRITABLEPATH
                            ), WritablePathObjectV12::class.java, null
                        )
                        if (pathObjectList.size > 0) {
                            val fileHolderList: MutableList<FileHolder> = ArrayList<FileHolder>()
                            for (pathObject in pathObjectList) {
                                val fileHolder = FileHolder()
                                fileHolder.reconstruct(db, kuick, pathObject.getValues())
                                fileHolderList.add(fileHolder)
                            }
                            kuick.insert(db, fileHolderList, null, null)
                        }
                        db.execSQL("DROP TABLE IF EXISTS `" + v12.TABLE_WRITABLEPATH + "`")
                    }
                }
            }
            13 -> {
                run {
                    val table: SQLValues.Table = tables.getTable(Kuick.Companion.TABLE_DEVICES)
                    val typeColumn: Column = table.getColumn(Kuick.Companion.FIELD_DEVICES_TYPE)
                    val clientVerCol: Column = table.getColumn(Kuick.Companion.FIELD_DEVICES_PROTOCOLVERSIONMIN)
                    db.execSQL(
                        "ALTER TABLE " + table.getName() + " ADD " + typeColumn.getName()
                                + " " + typeColumn.getType().toString() + (if (typeColumn.isNullable()) " NOT" else "")
                                + " NULL DEFAULT " + Device.Type.NORMAL.toString()
                    )
                    db.execSQL(
                        "ALTER TABLE " + table.getName() + " ADD " + clientVerCol.getName()
                                + " " + clientVerCol.getType()
                            .toString() + (if (clientVerCol.isNullable()) " NOT" else "")
                                + " NULL DEFAULT 0"
                    )
                }
                run {
                    val oldList: List<TransferAssigneeV12> = kuick.castQuery<NetworkDeviceV12, TransferAssigneeV12>(
                        db, SQLQuery.Select(
                            Kuick.Companion.TABLE_TRANSFERMEMBER
                        ), TransferAssigneeV12::class.java, null
                    )
                    db.execSQL("DROP TABLE IF EXISTS `" + Kuick.Companion.TABLE_TRANSFERMEMBER + "`")
                    SQLQuery.createTable(db, tables.getTable(Kuick.Companion.TABLE_TRANSFERMEMBER))
                    val newAssignees: MutableList<TransferMember> = ArrayList<TransferMember>()
                    for (assigneeV12 in oldList) {
                        val selection: SQLQuery.Select = SQLQuery.Select(Kuick.Companion.TABLE_TRANSFERITEM)
                        selection.setWhere(
                            Kuick.Companion.FIELD_TRANSFERITEM_TYPE + "=? AND "
                                    + Kuick.Companion.FIELD_TRANSFERITEM_TRANSFERID + "=? AND " + v12.FIELD_TRANSFER_DEVICEID
                                    + "=?", TransferObjectV12.Type.INCOMING.toString(),
                            assigneeV12.groupId.toString(), assigneeV12.deviceId
                        )
                        if (kuick.getFirstFromTable(db, selection) != null) {
                            val incomingMember = TransferMember()
                            incomingMember.reconstruct(db, kuick, assigneeV12.getValues())
                            incomingMember.type = TransferItem.Type.INCOMING
                            newAssignees.add(incomingMember)
                        }
                        selection.setWhere(
                            Kuick.Companion.FIELD_TRANSFERITEM_TYPE + "=? AND "
                                    + Kuick.Companion.FIELD_TRANSFERITEM_TRANSFERID + "=? AND " + v12.FIELD_TRANSFER_DEVICEID
                                    + "=?", TransferObjectV12.Type.OUTGOING.toString(),
                            assigneeV12.groupId.toString(), assigneeV12.deviceId
                        )
                        if (kuick.getFirstFromTable(db, selection) != null) {
                            val outgoingMember = TransferMember()
                            outgoingMember.reconstruct(db, kuick, assigneeV12.getValues())
                            outgoingMember.type = TransferItem.Type.OUTGOING
                            newAssignees.add(outgoingMember)
                        }
                    }
                    if (newAssignees.size > 0) kuick.insert(db, newAssignees, null, null)
                }
                run {
                    val table: SQLValues.Table = tables.getTable(Kuick.Companion.TABLE_TRANSFERITEM)
                    val outgoingBaseObjects: List<TransferObjectV12> =
                        kuick.castQuery<TransferGroupV12, TransferObjectV12>(
                            db, SQLQuery.Select(
                                v12.TABLE_DIVISTRANSFER
                            ), TransferObjectV12::class.java, null
                        )
                    val outgoingMirrorObjects: List<TransferObjectV12> =
                        kuick.castQuery<TransferGroupV12, TransferObjectV12>(
                            db, SQLQuery.Select(
                                Kuick.Companion.TABLE_TRANSFERITEM
                            ).setWhere(
                                Kuick.Companion.FIELD_TRANSFERITEM_TYPE + "=?",
                                TransferObjectV12.Type.OUTGOING.toString()
                            ), TransferObjectV12::class.java, null
                        )
                    val incomingObjects: List<TransferObjectV12> = kuick.castQuery<TransferGroupV12, TransferObjectV12>(
                        db, SQLQuery.Select(
                            Kuick.Companion.TABLE_TRANSFERITEM
                        ).setWhere(
                            Kuick.Companion.FIELD_TRANSFERITEM_TYPE + "=?",
                            TransferObjectV12.Type.INCOMING.toString()
                        ), TransferObjectV12::class.java, null
                    )
                    db.execSQL("DROP TABLE IF EXISTS `" + v12.TABLE_DIVISTRANSFER + "`")
                    db.execSQL("DROP TABLE IF EXISTS `" + Kuick.Companion.TABLE_TRANSFERITEM + "`")
                    SQLQuery.createTable(db, table)
                    if (outgoingBaseObjects.size > 0) {
                        val newObjects: MutableMap<Long, TransferItem> = ArrayMap()
                        for (objectV12 in outgoingBaseObjects) {
                            var `object` = newObjects[objectV12.requestId]
                            if (`object` != null) continue
                            `object` = TransferItem()
                            `object`!!.reconstruct(db, kuick, objectV12.getValues())
                            newObjects[objectV12.requestId] = `object`!!
                        }
                        for (objectV12 in outgoingMirrorObjects) {
                            val `object` = newObjects[objectV12.requestId] ?: continue
                            try {
                                `object`.putFlag(
                                    objectV12.deviceId, TransferItem.Flag.valueOf(
                                        objectV12.flag.toString()
                                    )
                                )
                            } catch (ignored: Exception) {
                            }
                        }
                        if (newObjects.size > 0) kuick.insert(db, ArrayList(newObjects.values), null, null)
                    }
                    if (incomingObjects.size > 0) {
                        val newIncomingInstances: List<TransferItem> = ArrayList()
                        for (objectV12 in incomingObjects) {
                            val newObject = TransferItem()
                            newObject.reconstruct(db, kuick, objectV12.getValues())
                        }
                        kuick.insert(db, newIncomingInstances, null, null)
                    }
                }
                run {
                    val table: SQLValues.Table = tables.getTable(Kuick.Companion.TABLE_TRANSFER)
                    val column: Column = table.getColumn(Kuick.Companion.FIELD_TRANSFER_ISPAUSED)
                    db.execSQL(
                        "ALTER TABLE " + table.getName() + " ADD " + column.getName()
                                + " " + column.getType().toString() + (if (column.isNullable()) " NOT" else "")
                                + " NULL DEFAULT " + Device.Type.NORMAL.toString()
                    )
                }
                run {
                    val pathObjectList: List<WritablePathObjectV12> = kuick.castQuery<Any, WritablePathObjectV12>(
                        db, SQLQuery.Select(
                            v12.TABLE_WRITABLEPATH
                        ), WritablePathObjectV12::class.java, null
                    )
                    if (pathObjectList.size > 0) {
                        val fileHolderList: MutableList<FileHolder> = ArrayList<FileHolder>()
                        for (pathObject in pathObjectList) {
                            val fileHolder = FileHolder()
                            fileHolder.reconstruct(db, kuick, pathObject.getValues())
                            fileHolderList.add(fileHolder)
                        }
                        kuick.insert(db, fileHolderList, null, null)
                    }
                    db.execSQL("DROP TABLE IF EXISTS `" + v12.TABLE_WRITABLEPATH + "`")
                }
            }
        }
    }

    interface v12 {
        companion object {
            fun tables(currentValues: SQLValues): SQLValues {
                val values = SQLValues()
                values.getTables().putAll(currentValues.getTables())
                values.defineTable(Kuick.Companion.TABLE_TRANSFERITEM)
                    .define(Column(Kuick.Companion.FIELD_TRANSFERITEM_ID, SQLType.LONG, false))
                    .define(Column(Kuick.Companion.FIELD_TRANSFERITEM_TRANSFERID, SQLType.LONG, false))
                    .define(Column(FIELD_TRANSFER_DEVICEID, SQLType.TEXT, true))
                    .define(Column(Kuick.Companion.FIELD_TRANSFERITEM_FILE, SQLType.TEXT, true))
                    .define(Column(Kuick.Companion.FIELD_TRANSFERITEM_NAME, SQLType.TEXT, false))
                    .define(Column(Kuick.Companion.FIELD_TRANSFERITEM_SIZE, SQLType.INTEGER, true))
                    .define(Column(Kuick.Companion.FIELD_TRANSFERITEM_MIME, SQLType.TEXT, true))
                    .define(Column(Kuick.Companion.FIELD_TRANSFERITEM_TYPE, SQLType.TEXT, false))
                    .define(Column(Kuick.Companion.FIELD_TRANSFERITEM_DIRECTORY, SQLType.TEXT, true))
                    .define(Column(FIELD_TRANSFER_ACCESSPORT, SQLType.INTEGER, true))
                    .define(Column(FIELD_TRANSFER_SKIPPEDBYTES, SQLType.LONG, false))
                    .define(Column(Kuick.Companion.FIELD_TRANSFERITEM_FLAG, SQLType.TEXT, true))

                // define the transfer division table based on the transfer table
                val transferTable: SQLValues.Table = values.getTables().get(Kuick.Companion.TABLE_TRANSFERITEM)
                val transDivisionTable: SQLValues.Table = SQLValues.Table(TABLE_DIVISTRANSFER)
                transDivisionTable.getColumns().putAll(transferTable.getColumns())
                values.defineTable(Kuick.Companion.TABLE_TRANSFERMEMBER)
                    .define(Column(Kuick.Companion.FIELD_TRANSFERMEMBER_TRANSFERID, SQLType.LONG, false))
                    .define(Column(Kuick.Companion.FIELD_TRANSFERMEMBER_DEVICEID, SQLType.TEXT, false))
                    .define(Column(FIELD_TRANSFERASSIGNEE_ISCLONE, SQLType.INTEGER, true))
                return values
            }

            const val TABLE_DIVISTRANSFER = "divisionTransfer"
            const val FIELD_TRANSFERASSIGNEE_ISCLONE = "isClone"
            const val FIELD_TRANSFER_DEVICEID = "deviceId"
            const val FIELD_TRANSFER_ACCESSPORT = "accessPort"
            const val FIELD_TRANSFER_SKIPPEDBYTES = "skippedBytes"
            const val TABLE_WRITABLEPATH = "writablePath"
            const val FIELD_WRITABLEPATH_TITLE = "title"
            const val FIELD_WRITABLEPATH_PATH = "path"
        }
    }
}