package com.kneelawk.marionette.gradle

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class TypeNameTests : StringSpec({
    "it should qualify names" {
        val t = TypeName("foo.bar", "Baz")
        t.qualified shouldBe "foo.bar.Baz"
    }

    "it should be able to wrap primitives" {
        val t = TypeName("", "int")
        t.wrapper shouldBe TypeName("java.lang", "Integer")
    }

    "it should be able to unwrap wrappers" {
        val t = TypeName("java.lang", "Double")
        t.primitive shouldBe TypeName("", "double")
    }

    "it should parse qualified names" {
        TypeName.fromString("foo.bar.Baz") shouldBe TypeName("foo.bar", "Baz")
    }

    "it should parse qualified names of nested classes" {
        TypeName.fromString("foo.bar.Baz.Qux") shouldBe TypeName("foo.bar", "Baz.Qux")
    }

    "it should parse qualified names of lower-case classes" {
        TypeName.fromString("foo.bar.baz") shouldBe TypeName("foo.bar", "baz")
    }

    "it should parse primitive names" {
        TypeName.fromString("float") shouldBe TypeName("", "float")
    }
})
