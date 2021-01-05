package com.kneelawk.marionette.gradle

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe

class ImportResolverTests : StringSpec({
    "it should throw an IllegalArgumentException if getting a non-existent key" {
        val i = ImportResolver()
        shouldThrow<IllegalArgumentException> { i[Any()] }
        i.add("foo.bar", "Baz")
        shouldThrow<IllegalArgumentException> { i[Any()] }
    }

    "it should give an unqualified name if only one name exists with that unqualified name" {
        val i = ImportResolver()
        val baz = i.add("foo.bar", "Baz")
        i[baz] shouldBe "Baz"
    }

    "it should give a qualified name if more than one name exists with that unqualified name" {
        val i = ImportResolver()
        val baz1 = i.add("foo.bar1", "Baz")
        val baz2 = i.add("foo.bar2", "Baz")
        i[baz1] shouldBe "foo.bar1.Baz"
        i[baz2] shouldBe "foo.bar2.Baz"
    }

    "it should give an import if only one name exists with that unqualified name" {
        val i = ImportResolver()
        val baz = i.add("foo.bar", "Baz")
        i.getImport(baz) shouldBe "foo.bar"
    }

    "it should not give an import if more than one name exists with that unqualified name" {
        val i = ImportResolver()
        val baz1 = i.add("foo.bar1", "Baz")
        val baz2 = i.add("foo.bar2", "Baz")
        i.getImport(baz1) shouldBe null
        i.getImport(baz2) shouldBe null
    }

    "it should give an import if only one name exists with that unqualified name and the current package is different" {
        val i = ImportResolver()
        val baz = i.add("foo.bar", "Baz")
        i.getImport(baz, "qux") shouldBe "foo.bar"
    }

    "it should not give an import if more than one name exists with that unqualified name but the current package is different" {
        val i = ImportResolver()
        val baz1 = i.add("foo.bar1", "Baz")
        val baz2 = i.add("foo.bar2", "Baz")
        i.getImport(baz1, "qux") shouldBe null
        i.getImport(baz2, "qux") shouldBe null
    }

    "it should not give an import if only one name exists with that unqualified name but the current package is the same" {
        val i = ImportResolver()
        val baz = i.add("foo.bar", "Baz")
        i.getImport(baz, "foo.bar") shouldBe null
    }

    "it should not give an import if more than one name exists with that unqualified name and the current package is the same" {
        val i = ImportResolver()
        val baz1 = i.add("foo.bar1", "Baz")
        val baz2 = i.add("foo.bar2", "Baz")
        i.getImport(baz1, "foo.bar1") shouldBe null
        i.getImport(baz2, "foo.bar2") shouldBe null
    }

    "it should list all imports" {
        val i = ImportResolver()
        i.add("foo.bar", "Baz")
        i.add("qux.quux", "Quuz")
        i.getImports() shouldContainExactly listOf("foo.bar.Baz", "qux.quux.Quuz")
    }

    "it should not list imports in the same package" {
        val i = ImportResolver()
        i.add("foo.bar", "Baz")
        i.add("qux.quux", "Quuz")
        i.getImports("foo.bar") shouldContainExactly listOf("qux.quux.Quuz")
    }
})