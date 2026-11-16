/*
 * Copyright 2026 consulo.io
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.intellij.aspect.testing.tests.dotnet

import com.google.common.truth.Truth.assertThat
import com.google.devtools.intellij.ideinfo.IntellijIdeInfo.Dependency.DependencyType
import com.intellij.aspect.testing.rules.fixture.AspectFixture
import com.intellij.aspect.testing.rules.utils.assertThatDeps
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

// Consulo fork: modules/dotnet_info.bzl
@RunWith(JUnit4::class)
class SimpleTest {
  @Rule
  @JvmField
  val aspect = AspectFixture()

  @Test
  fun testBinary() {
    val target = aspect.findTarget("//:app")
    assertThat(target.kind).isEqualTo("csharp_binary")

    val assembly = target.dotnetAssemblyInfo
    assertThat(assembly.assemblyName).isEqualTo("app")
    assertThat(assembly.language).isEqualTo("csharp")
    assertThat(assembly.targetFramework).isEqualTo("net8.0")
    assertThat(assembly.executable).isTrue()
    assertThat(assembly.sourcesList.map { it.relativePath }).containsExactly("Program.cs")
    assertThat(assembly.assembly.relativePath).isEqualTo("app/net8.0/app.dll")
    assertThat(assembly.pdb.relativePath).isEqualTo("app/net8.0/app.pdb")
    assertThat(assembly.frameworkReferencesList.map { it.relativePath }).contains("ref/net8.0/System.Runtime.dll")
    assertThat(assembly.packageReferencesList).isEmpty()
    assertThat(assembly.dotnet.relativePath).containsMatch("^dotnet(|.exe)$")

    assertThatDeps(target.depsList).withType(DependencyType.COMPILE_TIME).labels().contains("//greeter:greeter")
  }

  @Test
  fun testLibrary() {
    val target = aspect.findTarget("//greeter:greeter")
    assertThat(target.kind).isEqualTo("csharp_library")

    val assembly = target.dotnetAssemblyInfo
    assertThat(assembly.assemblyName).isEqualTo("greeter")
    assertThat(assembly.targetFramework).isEqualTo("net8.0")
    assertThat(assembly.executable).isFalse()
    assertThat(assembly.assembly.relativePath).isEqualTo("greeter/greeter/net8.0/greeter.dll")
    assertThat(assembly.definesList).containsExactly("GREETER")
    assertThat(assembly.languageVersion).isEqualTo("12.0")
    assertThat(assembly.nullable).isEqualTo("enable")
    assertThat(assembly.allowUnsafeBlocks).isTrue()
    assertThat(assembly.internalsVisibleToList).containsExactly("app")
    assertThat(assembly.projectSdk).isEqualTo("default")
  }
}
