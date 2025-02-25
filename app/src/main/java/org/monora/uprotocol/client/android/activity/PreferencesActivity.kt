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
package org.monora.uprotocol.client.android.activity

import android.content.DialogInterface
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AlertDialog
import androidx.preference.PreferenceManager
import org.monora.uprotocol.client.android.R
import org.monora.uprotocol.client.android.app.Activity

class PreferencesActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_preferences)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> onBackPressed()
            R.id.actions_preference_main_reset_to_defaults -> {
                AlertDialog.Builder(this)
                    .setTitle(R.string.ques_resetToDefault)
                    .setMessage(R.string.text_resetPreferencesToDefaultSummary)
                    .setNegativeButton(R.string.butn_cancel, null)
                    .setPositiveButton(R.string.butn_proceed) { _: DialogInterface?, _: Int ->
                        PreferenceManager.getDefaultSharedPreferences(applicationContext).edit()
                            .clear()
                            .apply()
                        PreferenceManager.setDefaultValues(
                            applicationContext, R.xml.preferences_defaults_main, true
                        )

                        finish()
                    }
                    .show()
            }
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.actions_preferences_main, menu)
        return super.onCreateOptionsMenu(menu)
    }
}