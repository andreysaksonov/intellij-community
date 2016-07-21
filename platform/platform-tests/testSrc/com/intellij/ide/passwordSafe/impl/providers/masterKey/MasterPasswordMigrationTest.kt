/*
 * Copyright 2000-2016 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.intellij.ide.passwordSafe

import com.intellij.ide.passwordSafe.impl.providers.masterKey.PasswordDatabase
import com.intellij.ide.passwordSafe.masterKey.FilePasswordSafeProvider
import com.intellij.ide.passwordSafe.masterKey.convertOldDb
import com.intellij.openapi.util.JDOMUtil
import com.intellij.testFramework.ApplicationRule
import com.intellij.testFramework.runInEdtAndWait
import com.intellij.util.xmlb.XmlSerializer

import org.assertj.core.api.Assertions.assertThat
import org.junit.ClassRule
import org.junit.Test

class MasterPasswordMigrationTest {
  companion object {
    @JvmField
    @ClassRule
    val projectRule = ApplicationRule()
  }

  @Test
  fun emptyPass() {
    val passwordSafe = convertOldDb(getDb("""<State>
      <option name="MASTER_PASSWORD_INFO" value="" />
      <option name="PASSWORDS">
        <array>
          <array>
            <option value="e86d036dc89ed252627ba61b6023ec0b21cbd58fbbacdbe2b530c4a553903dd8" />
            <option value="05e93059f4487f4cc257133e6c54d81f53b7793965d23ca9e4b0a4960edfca33" />
          </array>
          <array>
            <option value="8962f7731a0b30862b4da5c9d9830a2cc5f5fc1648242d888b16d1668c2dd1ad" />
            <option value="116f1b839d1326d6ecd52b78bb050cd1" />
          </array>
        </array>
      </option>
    </State>"""))
    assertThat(passwordSafe).isNotEmpty

    val provider = FilePasswordSafeProvider(passwordSafe)
    assertThat(provider.getPassword("com.intellij.ide.passwordSafe.impl.providers.masterKey.MasterKeyPasswordSafeTest/TEST")).isEqualTo("test")
  }

  @Test
  fun nonEmptyPass() {
    var passwordSafe: Map<String, String>? = null
    runInEdtAndWait {
      passwordSafe = convertOldDb(getDb("""<State>
        <option name="MASTER_PASSWORD_INFO" value="" />
        <option name="PASSWORDS">
          <array>
            <array>
              <option value="a6f02f741d8be093f6f95db718cf4d779a93ff8917e1693eb29072b4f75aade4" />
              <option value="4acfac8ee7e1f0ef8865150707a911d6d1ac6164ff31d09c1ade16c8a4191568" />
            </array>
            <array>
              <option value="05396cc1090959a5cde51670c228bb8fbc6eebb9fae479d8360e11e40642f074" />
              <option value="d8e6e0d1ea8e5b239c4c87583fc263f8" />
            </array>
          </array>
        </option>
      </State>"""))
    }
    assertThat(passwordSafe).isNotEmpty
    val provider = FilePasswordSafeProvider(passwordSafe)
    assertThat(provider.getPassword("com.intellij.ide.passwordSafe.impl.providers.masterKey.MasterKeyPasswordSafeTest/TEST")).isEqualTo("test")
  }

  @Suppress("DEPRECATION")
  private fun getDb(data: String): PasswordDatabase {
    val passwordDatabase = PasswordDatabase()
    val state = PasswordDatabase.State()
    XmlSerializer.deserializeInto(state, JDOMUtil.load(data.reader()))
    passwordDatabase.loadState(state)
    return passwordDatabase
  }
}