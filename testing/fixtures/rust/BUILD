load("//testing/rules:defs.bzl", "test_fixture")

package(
    default_testonly = 1,
    default_visibility = ["//testing/tests:__subpackages__"],
)

test_fixture(
    name = "simple",
    srcs = glob(["simple/**"]),
    bazel = ">=8",
    modules = ["//testing/fixtures:rules_rust_latest"],
    rule_sets = ["rust"],
    targets = ["//..."],
)
