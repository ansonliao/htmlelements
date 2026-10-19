Html Elements framework
=======================

[![License](https://img.shields.io/badge/license-Apache%202.0-blue.svg)](https://www.apache.org/licenses/LICENSE-2.0.txt)
[![Java](https://img.shields.io/badge/java-21-blue.svg)](https://adoptium.net/)
[![Selenium](https://img.shields.io/badge/selenium-4.x-green.svg)](https://www.selenium.dev/)
[![Maven Central](https://img.shields.io/maven-central/v/io.github.ansonliao/htmlelements-java.svg)](https://central.sonatype.com/artifact/io.github.ansonliao/htmlelements-java)

> **Fork notice**
>
> This is an unofficial fork of [HtmlElements](https://github.com/yandex-qatools/htmlelements),
> originally developed at Yandex and licensed under the Apache License, Version 2.0.
> It is **not affiliated with, sponsored by, or endorsed by Yandex**.
> The original copyright notice is retained in [LICENSE](LICENSE); see [NOTICE](NOTICE)
> for the list of changes made in this fork.
>
> This fork is maintained at <https://github.com/ansonliao/htmlelements> and published
> under the Maven coordinates `io.github.ansonliao`. The Java package names are kept
> unchanged from upstream (`ru.yandex.qatools.htmlelements`) to preserve source
> compatibility.

This framework is designed to provide an easy-to-use way of interacting with web-page elements in your tests.
It can be considered to be an extension of WebDriver Page Object.

With the help of the Html Elements framework you can group web-page elements into blocks, encapsulate logic of interaction within them
and then easily use created blocks in page objects. It also provides a set of helpful matchers to use with web-page elements
and blocks.

Modules
-------

| Module | Description |
|---|---|
| `htmlelements-java` | Core: element types, block loader, page factory decorator |
| `htmlelements-matchers` | Hamcrest matchers and matcher decorators |
| `htmlelements-thucydides` | [Serenity BDD](https://serenity-bdd.info/) (formerly Thucydides) integration |
| `htmlelements-all` | Convenience aggregator that pulls in all of the above |

Build from source
-----------------

Requires JDK 21 and Gradle 9.x (a wrapper is included, no local Gradle installation needed).

```bash
./gradlew build          # compile, test and package
./gradlew test           # run tests only
./gradlew publishToMavenLocal   # publish 2.0.0-SNAPSHOT to ~/.m2
```

Include Html Elements in your project
-------------------------------------

Maven dependencies for Html Elements core:

```xml
<dependency>
    <groupId>io.github.ansonliao</groupId>
    <artifactId>htmlelements-java</artifactId>
    <version>2.0.0</version>
</dependency>
```

And for Serenity BDD integration:

```xml
<dependency>
    <groupId>io.github.ansonliao</groupId>
    <artifactId>htmlelements-thucydides</artifactId>
    <version>2.0.0</version>
</dependency>
```

Or you can include all modules at once if needed:

```xml
<dependency>
    <groupId>io.github.ansonliao</groupId>
    <artifactId>htmlelements-all</artifactId>
    <version>2.0.0</version>
</dependency>
```

Gradle equivalent:

```kotlin
dependencies {
    implementation("io.github.ansonliao:htmlelements-java:2.0.0")
}
```

**Requirements:** Java 21 and Selenium 4.x.

> Migrating from the upstream `ru.yandex.qatools.htmlelements` coordinates?
> Change the `groupId` to `io.github.ansonliao`, bump the version to `2.0.0`, and
> switch to Selenium 4.x. Java package names and public class names are unchanged.
> See the [2.0.0 release notes](releasenotes/2.0.0-releasenotes.en.md) for behaviour changes.

Tips
----

* **Fork source.** This is an independent, unofficial fork of
  [yandex-qatools/htmlelements](https://github.com/yandex-qatools/htmlelements),
  originally developed at Yandex and licensed under the Apache License, Version 2.0.
  It is **not affiliated with, sponsored by, or endorsed by Yandex**. The original
  copyright is retained and the list of changes is documented in [NOTICE](NOTICE).
* **Java package names are unchanged.** Import paths stay
  `ru.yandex.qatools.htmlelements.*` so existing code keeps compiling; only the
  Maven `groupId` changed to `io.github.ansonliao`.
* **Selenium 4 W3C semantics.** `TextInput.getText()` now reads the element's DOM
  `value` *property* (`getDomProperty`) instead of the `value` *attribute*. This is
  the correct W3C behaviour and is normally what you want, but review any code that
  relied on the old attribute-based value.
* **Using the snapshot.** `2.0.0-SNAPSHOT` is not published to Maven Central. Build
  and install it locally with `./gradlew publishToMavenLocal`, then depend on
  `io.github.ansonliao:htmlelements-java:2.0.0-SNAPSHOT`.
* **Requirements.** JDK 21 and Selenium 4.x at runtime.
* **Serenity BDD.** The `htmlelements-thucydides` module now integrates
  [Serenity BDD](https://serenity-bdd.info/) 5.x (the project formerly known as
  Thucydides).

Create blocks of elements
-------------------------
For example, let's create a block for the search form on the page http://www.yandex.com:

```java
@Name("Search form")
@FindBy(xpath = "//form")
public class SearchArrow extends HtmlElement {

    @Name("Search request input")
    @FindBy(id = "searchInput")
    private TextInput requestInput;

    @Name("Search button")
    @FindBy(className = "b-form-button__input")
    private Button searchButton;

    public void search(String request) {
        requestInput.sendKeys(request);
        searchButton.click();
    }
}
```

Construct page object using created blocks
------------------------------------------
You can easily use created blocks in page objects:

```java
public class SearchPage {

    private SearchArrow searchArrow;
    // Other blocks and elements here ...

    public SearchPage(WebDriver driver) {
        PageFactory.initElements(new HtmlElementDecorator(new HtmlElementLocatorFactory(driver)), this);
    }

    public void search(String request) {
        searchArrow.search(request);
    }

    // Other methods here ...
}
```

Use page objects in your tests
------------------------------
Created page objects can be used in your tests. This makes tests more concise, easier to maintain, and easy to write.

```java
public class SampleTest {

    private WebDriver driver = new ChromeDriver();
    private SearchPage searchPage = new SearchPage(driver);

    @Before
    public void loadPage() {
        driver.get("https://www.google.com");
    }

    @Test
    public void sampleTest() {
        searchPage.search("selenium");
        // Some assertion here
    }

    @After
    public void closeDriver() {
        driver.quit();
    }
}
```

Other Languages
-------------
In case you are not a Java guy/gal, don't panic, there are still few options:

* [Html Elements .NET implementation](https://github.com/yandex-qatools/htmlelements-dotnet) - .NET port made by [Vadzim Hushchanskou](https://github.com/HardNorth)
* [Html Elements PHP implementation](https://github.com/qa-tools/qa-tools) - PHP port made by [Alexander Obuhovich](https://github.com/aik099)
* [bumblebee framework (C#)](https://github.com/patrickherrmann/Bumblebee) - same idea, nice implementation

Release Notes
-------------
* [Version 2.0.0 Release Notes](releasenotes/2.0.0-releasenotes.en.md) — Selenium 4, Java 21, Gradle, new coordinates
* [Version 1.17 Release Notes](https://github.com/yandex-qatools/htmlelements/releases/tag/htmlelements-1.17)
* [Version 1.16 Release Notes](https://github.com/yandex-qatools/htmlelements/releases/tag/htmlelements-1.16)
* [Version 1.15 Release Notes](https://github.com/yandex-qatools/htmlelements/releases/tag/htmlelements-1.15)
* [Version 1.14 Release Notes](https://github.com/yandex-qatools/htmlelements/releases/tag/htmlelements-1.14)
* [Version 1.13 Release Notes](https://github.com/yandex-qatools/htmlelements/releases/tag/htmlelements-1.13)
* [Version 1.12 Release Notes](https://github.com/yandex-qatools/htmlelements/releases/tag/htmlelements-1.12)
* [Version 1.11 Release Notes](https://github.com/yandex-qatools/htmlelements/blob/master/releasenotes/1.11-releasenotes.ru.md)
* [Version 1.10 Release Notes](https://github.com/yandex-qatools/htmlelements/blob/master/releasenotes/1.10-releasenotes.ru.md)
* [Version 1.9 Release Notes](https://github.com/yandex-qatools/htmlelements/blob/master/releasenotes/1.9-releasenotes.ru.md)

License
-------
Licensed under the [Apache License, Version 2.0](LICENSE).

Original work: Copyright 2012 YANDEX. Modifications: Copyright 2026 ansonliao.
See [NOTICE](NOTICE) for attribution and the list of changes.

Questions?
----------
In case you can't find an answer in documentation and examples provided above, you can ask it on StackOverflow with the
[![htmlelements](https://img.shields.io/badge/stackoverflow-htmlelements-orange.svg?style=flat)](http://stackoverflow.com/questions/tagged/htmlelements) tag.
