/*
 * Copyright 2017-2019 Aljoscha Grebe
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.almightyalpaca.jetbrains.plugins.discord.plugin.settings.options.types

import com.almightyalpaca.jetbrains.plugins.discord.plugin.gui.preview.JPreview
import com.almightyalpaca.jetbrains.plugins.discord.plugin.logging.Logging
import com.almightyalpaca.jetbrains.plugins.discord.plugin.rpc.renderer.Renderer
import com.almightyalpaca.jetbrains.plugins.discord.plugin.settings.options.OptionCreator
import com.almightyalpaca.jetbrains.plugins.discord.plugin.settings.options.OptionHolder
import com.almightyalpaca.jetbrains.plugins.discord.plugin.settings.options.impl.OptionProviderImpl
import com.intellij.util.ui.JBUI
import org.jdom.Element
import java.awt.Dimension
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import javax.swing.Box
import javax.swing.BoxLayout
import javax.swing.JPanel
import kotlin.reflect.KProperty

fun OptionCreator<in Preview>.preview() = OptionProviderImpl(this, OptionPreview())

class OptionPreview : Option<Preview>(""), OptionCreator<Tabs> {
    lateinit var tabsKey: String
    lateinit var tabsOption: Option<out Tabs>

    val value = Preview(this)
    override fun getValue(thisRef: OptionHolder, property: KProperty<*>) = value

    override fun set(key: String, option: Option<out Tabs>) {
        if (this@OptionPreview::tabsOption.isInitialized)
            throw Exception("tabs have already been set")

        tabsKey = key
        tabsOption = option
    }

    override val component by lazy {
        JPanel().apply panel@{
            // layout = BoxLayout(this@panel, BoxLayout.X_AXIS)
            layout = GridBagLayout()

            val previewGbc = GridBagConstraints().apply {
                gridx = 0
                gridy = 0
                gridwidth = 1
                gridheight = 2
                anchor = GridBagConstraints.NORTHWEST
            }

            val previewImpl = JPreview()

            val preview = JPanel().apply innerPanel@{
                layout = BoxLayout(this@innerPanel, BoxLayout.Y_AXIS)

                add(previewImpl)
                add(Box.Filler(Dimension(0, 0), Dimension(10, 0), Dimension(10, Integer.MAX_VALUE)))

                border = JBUI.Borders.empty(10, 25)
            }
            add(preview, previewGbc)

            val tabsGbc = GridBagConstraints().apply {
                gridx = 1
                gridy = 0
                gridwidth = 1
                gridheight = 1
                anchor = GridBagConstraints.NORTHWEST
                fill = GridBagConstraints.HORIZONTAL
                weightx = 1.0
            }
            add(tabsOption.component, tabsGbc)

            tabsOption.addChangeListener { tabs ->
                previewImpl.type = when (val selected = tabs.selected) {
                    0 -> Renderer.Type.APPLICATION
                    1 -> Renderer.Type.PROJECT
                    2 -> Renderer.Type.FILE
                    else -> {
                        log { "Unknown tab with id=$selected selected" }

                        Renderer.Type.APPLICATION
                    }
                }
            }
        }
    }

    override fun addChangeListener(listener: (Preview) -> Unit) = throw Exception("Cannot listen to preview changes")

    override var isComponentEnabled
        get() = tabsOption.isComponentEnabled
        set(value) {
            tabsOption.isComponentEnabled = value
        }
    override val isModified get() = tabsOption.isModified
    override val isDefault get() = tabsOption.isDefault
    override fun apply() = tabsOption.apply()
    override fun reset() = tabsOption.reset()
    override fun writeXml(element: Element, key: String) = tabsOption.writeXml(element, tabsKey)
    override fun readXml(element: Element, key: String) = tabsOption.readXml(element, tabsKey)

    companion object : Logging()
}

class Preview(private val option: OptionPreview) : Value(), OptionCreator<Tabs> by option {
    interface Provider : Value.Provider {
        override operator fun getValue(thisRef: OptionHolder, property: KProperty<*>): Group
    }
}