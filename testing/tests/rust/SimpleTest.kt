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
package com.intellij.aspect.testing.tests.rust

import com.google.common.truth.Truth.assertThat
import com.google.devtools.intellij.ideinfo.IntellijIdeInfo.Dependency.DependencyType
import com.google.devtools.intellij.ideinfo.IntellijIdeInfo.RustCrateLocation
import com.intellij.aspect.testing.rules.fixture.AspectFixture
import com.intellij.aspect.testing.rules.utils.assertThatDeps
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

// Consulo fork: modules/rust_info.bzl
@RunWith(JUnit4::class)
class SimpleTest {
  @Rule
  @JvmField
  val aspect = AspectFixture()

  @Test
  fun testBinary() {
    val target = aspect.findTarget("//:main")
    assertThat(target.kind).isEqualTo("rust_binary")

    val crate = target.rustCrateInfo
    assertThat(crate.name).isEqualTo("main")
    assertThat(crate.kind).isEqualTo("bin")
    assertThat(crate.crateRoot).isEqualTo("main.rs")
    assertThat(crate.crateId).isEqualTo("main.rs")
    assertThat(crate.location).isEqualTo(RustCrateLocation.WORKSPACE_DIR)
    assertThat(crate.fromWorkspace).isTrue()
    assertThat(crate.crateFeaturesList).containsExactly("foo")
    assertThat(crate.cfgOptionsList).contains("feature=\"foo\"")
    assertThat(crate.dependenciesCrateIdsList).containsExactly("lib/lib.rs")
    assertThat(crate.toolchain.rustc.relativePath).containsMatch("bin/rustc(|.exe)$")
    assertThat(crate.toolchain.targetTriple).isNotEmpty()
    assertThat(crate.toolchain.sysroot).isNotEmpty()

    assertThatDeps(target.depsList).withType(DependencyType.COMPILE_TIME).labels().contains("//lib:lib")
  }

  @Test
  fun testLibrary() {
    val target = aspect.findTarget("//lib:lib")
    assertThat(target.kind).isEqualTo("rust_library")

    val crate = target.rustCrateInfo
    assertThat(crate.kind).isEqualTo("rlib")
    assertThat(crate.edition).isEqualTo("2021")
    assertThat(crate.version).isEqualTo("1.2.3")
    assertThat(crate.dependenciesCrateIdsList).containsExactly("answer_macro/lib.rs")

    // proc macro deps are compile time dependencies of the crate
    assertThatDeps(target.depsList).withType(DependencyType.COMPILE_TIME).labels().contains("//answer_macro:answer_macro")
  }

  @Test
  fun testProcMacro() {
    val target = aspect.findTarget("//answer_macro:answer_macro")
    assertThat(target.kind).isEqualTo("rust_proc_macro")

    val crate = target.rustCrateInfo
    assertThat(crate.kind).isEqualTo("proc-macro")
    assertThat(crate.procMacroArtifactsList).hasSize(1)
    assertThat(crate.procMacroArtifactsList[0]).containsMatch("answer_macro[^/]*\\.(so|dylib|dll)$")
  }

  @Test
  fun testTest() {
    val target = aspect.findTarget("//:main_test")
    assertThat(target.kind).isEqualTo("rust_test")

    // a test of a crate shares the crate root of the crate
    val crate = target.rustCrateInfo
    assertThat(crate.crateRoot).isEqualTo("main.rs")
    assertThat(crate.dependenciesCrateIdsList).isEmpty()
  }
}
