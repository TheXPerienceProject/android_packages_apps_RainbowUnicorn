/*
 * Copyright (C) 2024-2026 Lunaris AOSP
 * Copyright (C) 2026 The XPerience Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package mx.xperience.unicorn.fragments

import android.app.Dialog
import android.graphics.Color as AndroidColor
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import mx.xperience.framework.colorpicker.ColorPickerDialog

class CutoutProgressColorPickerDialogFragment : DialogFragment() {

    private var initialColor: Int = AndroidColor.parseColor("#2196F3")
    private var dialogTitle: String = "Choose color"
    private var onColorSelected: ((Int) -> Unit)? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let { args ->
            args.getString(ARG_COLOR_HEX)?.let { hex ->
                try {
                    initialColor = AndroidColor.parseColor("#${hex.removePrefix("#")}")
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            args.getString(ARG_TITLE)?.let { title ->
                dialogTitle = title
            }
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = ColorPickerDialog(requireContext(), initialColor)

        dialog.setOnColorChangedListener { color ->
            onColorSelected?.invoke(color)
        }

        return dialog
    }

    fun setOnColorSelectedListener(listener: (Int) -> Unit) {
        onColorSelected = listener
    }

    companion object {
        const val TAG = "CutoutProgressColorPickerDialogFragment"
        private const val ARG_COLOR_HEX = "color_hex"
        private const val ARG_TITLE = "dialog_title"

        fun newInstance(title: String, colorHex: String): CutoutProgressColorPickerDialogFragment {
            return CutoutProgressColorPickerDialogFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_TITLE, title)
                    putString(ARG_COLOR_HEX, colorHex)
                }
            }
        }
    }
}
