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

package org.monora.uprotocol.client.android.receiver

import android.content.BroadcastReceiver
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.lifecycle.LifecycleService
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.monora.uprotocol.client.android.R
import org.monora.uprotocol.client.android.backend.Backend
import org.monora.uprotocol.client.android.data.ClientRepository
import org.monora.uprotocol.client.android.data.TaskRepository
import org.monora.uprotocol.client.android.data.TransferRepository
import org.monora.uprotocol.client.android.database.model.SharedText
import org.monora.uprotocol.client.android.database.model.Transfer
import org.monora.uprotocol.client.android.database.model.UClient
import org.monora.uprotocol.client.android.protocol.rejectTransfer
import org.monora.uprotocol.client.android.protocol.startTransfer
import org.monora.uprotocol.client.android.service.backgroundservice.Task
import org.monora.uprotocol.client.android.task.transfer.TransferParams
import org.monora.uprotocol.client.android.util.NotificationBackend
import org.monora.uprotocol.core.CommunicationBridge
import org.monora.uprotocol.core.persistence.PersistenceProvider
import org.monora.uprotocol.core.protocol.ConnectionFactory
import javax.inject.Inject

@AndroidEntryPoint
class BgBroadcastReceiver : BroadcastReceiver() {
    @Inject
    lateinit var backend: Backend

    @Inject
    lateinit var clientRepository: ClientRepository

    @Inject
    lateinit var connectionFactory: ConnectionFactory

    @Inject
    lateinit var persistenceProvider: PersistenceProvider

    @Inject
    lateinit var taskRepository: TaskRepository

    @Inject
    lateinit var transferRepository: TransferRepository

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            ACTION_FILE_TRANSFER -> {
                val client: UClient? = intent.getParcelableExtra(EXTRA_CLIENT)
                val transfer: Transfer? = intent.getParcelableExtra(EXTRA_TRANSFER)
                val notificationId = intent.getIntExtra(NotificationBackend.EXTRA_NOTIFICATION_ID, -1)
                val isAccepted = intent.getBooleanExtra(EXTRA_ACCEPTED, false)

                backend.services.notifications.backend.cancel(notificationId)

                if (client != null && transfer != null) backend.applicationScope.launch(Dispatchers.IO) {
                    val details = transferRepository.getTransferDetailDirect(transfer.id) ?: return@launch

                    taskRepository.register(
                        TransferParams(transfer, client, details.size, details.sizeOfDone)
                    ) { applicationScope, params, state ->
                        applicationScope.launch(Dispatchers.IO) {
                            try {
                                val addresses = clientRepository.getInetAddresses(client.clientUid)

                                CommunicationBridge.Builder(connectionFactory, persistenceProvider, addresses).apply {
                                    setClearBlockedStatus(true)
                                    setClientUid(client.clientUid)
                                }.connect().use {
                                    if (isAccepted) {
                                        it.startTransfer(backend, transferRepository, params, state)
                                    } else {
                                        it.rejectTransfer(transferRepository, transfer)
                                    }
                                }
                            } catch (e: Exception) {
                                state.postValue(Task.State.Error(e))
                            }
                        }
                    }
                }
            }
            ACTION_DEVICE_KEY_CHANGE_APPROVAL -> {
                val client: UClient? = intent.getParcelableExtra(EXTRA_CLIENT)
                val notificationId = intent.getIntExtra(NotificationBackend.EXTRA_NOTIFICATION_ID, -1)

                backend.services.notifications.backend.cancel(notificationId)

                if (client != null && intent.getBooleanExtra(EXTRA_ACCEPTED, false)) {
                    persistenceProvider.approveInvalidationOfCredentials(client)
                }
            }
            ACTION_CLIPBOARD -> {
                val notificationId = intent.getIntExtra(NotificationBackend.EXTRA_NOTIFICATION_ID, -1)
                val sharedText: SharedText? = intent.getParcelableExtra(EXTRA_TEXT_MODEL)
                val accepted = intent.getBooleanExtra(EXTRA_ACCEPTED, false)

                backend.services.notifications.backend.cancel(notificationId)

                if (accepted && sharedText != null) {
                    val cbManager = context.applicationContext.getSystemService(
                        LifecycleService.CLIPBOARD_SERVICE
                    ) as ClipboardManager
                    cbManager.setPrimaryClip(ClipData.newPlainText("receivedText", sharedText.text))
                    Toast.makeText(context, R.string.mesg_textCopiedToClipboard, Toast.LENGTH_SHORT).show()
                }
            }
            ACTION_STOP_ALL_TASKS -> backend.cancelAllTasks()
        }
    }

    companion object {
        const val ACTION_CLIPBOARD = "org.monora.uprotocol.client.android.action.CLIPBOARD"

        const val ACTION_DEVICE_KEY_CHANGE_APPROVAL = "org.monora.uprotocol.client.android.action.DEVICE_APPROVAL"

        const val ACTION_FILE_TRANSFER = "org.monora.uprotocol.client.android.action.FILE_TRANSFER"

        const val ACTION_PIN_USED = "org.monora.uprotocol.client.android.transaction.action.PIN_USED"

        const val ACTION_STOP_ALL_TASKS = "org.monora.uprotocol.client.android.transaction.action.STOP_ALL_TASKS"

        const val EXTRA_TEXT_MODEL = "extraText"

        const val EXTRA_CLIENT = "extraClient"

        const val EXTRA_TRANSFER = "extraTransfer"

        const val EXTRA_ACCEPTED = "extraAccepted"
    }
}