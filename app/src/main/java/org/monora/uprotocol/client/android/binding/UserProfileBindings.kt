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

package org.monora.uprotocol.client.android.binding

import android.widget.EditText
import android.widget.ImageView
import androidx.core.widget.addTextChangedListener
import androidx.databinding.BindingAdapter
import androidx.preference.PreferenceManager
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import org.monora.uprotocol.client.android.GlideApp
import org.monora.uprotocol.client.android.data.UserDataRepository
import org.monora.uprotocol.client.android.util.Graphics
import org.monora.uprotocol.client.android.viewmodel.UserProfileViewModel
import org.monora.uprotocol.core.protocol.Client

@BindingAdapter("listenNicknameChanges")
fun listenNicknameChanges(editText: EditText, viewModel: UserProfileViewModel) {
    editText.addTextChangedListener { editable ->
        val nickname = editable.toString().also { if (it.isEmpty()) return@addTextChangedListener }

        PreferenceManager.getDefaultSharedPreferences(editText.context).edit()
            .putString(UserDataRepository.KEY_NICKNAME, nickname)
            .apply()
    }
}

@BindingAdapter("pictureOf")
fun loadPictureOfClient(imageView: ImageView, client: Client?) {
    if (client == null) return

    try {
        val default = Graphics.createIconBuilder(imageView.context).buildRound(client.clientNickname)

        if (client.hasPicture()) {
            GlideApp.with(imageView)
                .load(client.clientPictureData)
                .circleCrop()
                .placeholder(default)
                .error(default)
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(imageView)
        } else {
            imageView.setImageDrawable(default)
        }
    } catch (ignored: Exception) {
    }
}