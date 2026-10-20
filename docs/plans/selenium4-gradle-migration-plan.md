# HtmlElements 现代化改造：Selenium 4 + Gradle + 坐标重建

> 仓库：`/Users/ansonliao/Documents/repo/github/htmlelements`（fork 自 yandex-qatools/htmlelements，HEAD 停留在 2016-10-17，`b8adb3d`）
> 现状：Maven 4 模块 / Selenium 2.53.0 / Java 8 / Thucydides 0.9.275 / Mockito 1.x
> 目标：Selenium 4.51.0 / Java 21 / Gradle 9.6.1 / Serenity 5.4.1 / 新 Maven 坐标

所有 API 结论已用真实 jar（`selenium-api/support/remote-driver 4.51.0`、`serenity-core 5.4.1`、`hamcrest 2.2`、`commons-lang3 3.21.0`）以 `javap` + `unzip` 实测确认，非推测。

---

## 0. 已确认的四项决策

| 决策项 | 选定方案 |
|---|---|
| 构建系统 | **完全替换** Maven → Gradle（Kotlin DSL），删除 `pom.xml` |
| groupId | **`io.github.ansonliao`** |
| thucydides 模块 | **迁移到 Serenity BDD 5.4.1** |
| Java baseline | **Java 21 LTS** |

补充决策（本计划建议，无需再确认）：
- 版本号 `1.18-SNAPSHOT` → **`2.0.0-SNAPSHOT`**（Selenium 2→4、Java 8→21、坐标变更，属破坏性变更，语义化版本必须升 major）
- **Java 包名保留 `ru.yandex.qatools.htmlelements`**，本次只改构建坐标，不动包名 → 下游源码零改动，只需换 groupId。包名重命名留作 3.0 的可选后续。
- 模块 `artifactId` 保留原名（描述性名词，不涉商标），仅根工程改名为 `htmlelements-parent`

---

## 1. 分支策略

```bash
git checkout -b feature/selenium4-gradle-migration
git remote add upstream https://github.com/yandex-qatools/htmlelements.git   # 可选，便于追踪上游
```

- 分支名：`feature/selenium4-gradle-migration` —— 一次覆盖「Selenium 4 升级 + Gradle 迁移」两个主目的
- 基线：`master` @ `b8adb3d`，工作区当前仅有未跟踪的 `mvnw`、`mvnw.cmd`、`tools/`，无未提交改动，可安全切分支

---

## 2. 坐标与法律合规结论

### 2.1 结论：改 groupId 合法且必要，artifactId 可保留

Apache License 2.0 §2 明确授予「准备衍生作品（Derivative Works）并再发布」的永久、全球、免费、不可撤销许可。Maven `groupId`/`artifactId` 属构件元数据，不是许可证限制对象，**改坐标无需 Yandex 授权**。

反过来，**继续使用 `ru.yandex.qatools.htmlelements` 反而是负面选择**：
1. Maven Central / Sonatype 要求 groupId 所有权验证（基于域名或 GitHub 命名空间），无法通过；
2. 在他人命名空间下发版，构成混淆与身份冒用风险。

### 2.2 坐标变更表

| 项目 | 现坐标 | 新坐标 |
|---|---|---|
| groupId | `ru.yandex.qatools.htmlelements` | `io.github.ansonliao` |
| 根工程 | `htmlelements` (pom) | `htmlelements-parent` |
| 核心模块 | `htmlelements-java` | `io.github.ansonliao:htmlelements-java` |
| 匹配器 | `htmlelements-matchers` | `io.github.ansonliao:htmlelements-matchers` |
| Serenity 集成 | `htmlelements-thucydides` | `io.github.ansonliao:htmlelements-thucydides` |
| 聚合模块 | `htmlelements-all` | `io.github.ansonliao:htmlelements-all` |
| version | `1.18-SNAPSHOT` | `2.0.0-SNAPSHOT` |

> `artifactId` 保留原名的理由：`htmlelements-java` 等是纯描述性名称，不含商标；保留可让下游迁移成本降到「只改 groupId + version」两行。`htmlelements-thucydides` 虽然底层已换成 Serenity，但类名 `BlockPageObject` 和包名未变，改名反而增加混淆，故保留。

### 2.3 Apache 2.0 §4 强制义务 → 必须补齐的合规文件

当前仓库合规基础「够用但不完整」：`LICENCE` 只有 542 字节的 boilerplate，**没有完整许可证正文**，**没有 NOTICE**，122 个 Java 文件**零许可证头**，README 无 fork 声明。

| 条款 | 现状 | 本次必须做 |
|---|---|---|
| §4(a) 提供许可证副本 | `LICENCE` 仅简短声明 | 新增 `LICENSE`（Apache 2.0 全文 ~11KB），保留 `LICENCE` |
| §4(b) 修改需显著声明 | 无文件头 | 新增 `NOTICE` 说明「fork + 修改内容」 |
| §4(c) 保留原版权/归属 | 仅 `Copyright 2012 YANDEX` | **原样保留该行**，README 保留原项目链接与原始开发者名单 |
| §4(d) NOTICE | 原仓库无 NOTICE | 主动新增，记录来源与变更 |
| §6 商标 | POM/README 大量使用 Yandex 品牌 | 移除 `<organization>Yandex</organization>`、`<name>Yandex QATools ...</name>`；不得以 Yandex 名义发布；仅可描述性表述 "fork of ... by Yandex"，并声明 "not affiliated with or endorsed by Yandex" |

`NOTICE` 建议内容：

```text
HtmlElements
Copyright 2012 YANDEX

This product is a fork of HtmlElements, originally developed at Yandex
(https://github.com/yandex-qatools/htmlelements) and licensed under the
Apache License, Version 2.0.

Modifications Copyright 2026 ansonliao.
Changes: republished under Maven coordinates io.github.ansonliao;
build migrated from Maven to Gradle; upgraded to Selenium 4.x and Java 21;
Thucydides integration migrated to Serenity BDD; removed PhantomJS support.
This fork is not affiliated with or endorsed by Yandex.
```

---

## 3. 目标版本矩阵

| 组件 | 当前 | 目标 | 依据 |
|---|---|---|---|
| Selenium | 2.53.0 | **4.51.0** | Maven Central 最新 release，已实测 |
| Serenity BDD | thucydides-core 0.9.275 | **net.serenity-bdd:serenity-core 5.4.1** | 5.4.1 父 POM `selenium.version=4.51.0`、`maven.compiler.source/target=17` |
| JDK | 1.8 | **21** | 本机已装 zulu-21.0.7 |
| Gradle | — | **9.6.1** | 本机已装，wrapper 固定此版本 |
| JUnit | 4.12 | **4.13.2** | 4.12 有 CVE-2020-15250 |
| Mockito | mockito-all 1.10.19 | **mockito-core 5.24.0** | 1.x 在 JDK 11+ 运行期 `KeyFactory` 崩溃 |
| Hamcrest | hamcrest-all 1.3 | **hamcrest 2.2** | 3.0 无必要；2.2 中 `@Factory` 已删除（实测 jar 内无 `org/hamcrest/Factory.class`） |
| commons-lang3 | 3.4 | **3.21.0** | `org.apache.commons.lang3.text.WordUtils` 在 3.21 仍存在，无需改代码 |
| PhantomJS | phantomjsdriver 1.2.0 + 插件 | **删除** | 2018 年已停止维护；代码零引用；Apple Silicon 上 1.9.8 二进制无法运行 |

---

## 4. Gradle 构建设计

### 4.1 文件布局（`src/main/java`、`src/test/java` 与 Maven 完全一致，无需移动源码）

```
settings.gradle.kts
build.gradle.kts
gradle/libs.versions.toml
gradle/wrapper/gradle-wrapper.properties  (+ .jar)
gradlew / gradlew.bat
htmlelements-java/build.gradle.kts
htmlelements-matchers/build.gradle.kts
htmlelements-thucydides/build.gradle.kts
htmlelements-all/build.gradle.kts
```

### 4.2 `settings.gradle.kts`

```kotlin
rootProject.name = "htmlelements-parent"
include("htmlelements-java", "htmlelements-matchers", "htmlelements-thucydides", "htmlelements-all")

dependencyResolutionManagement {
    repositories { mavenCentral() }
}
```

### 4.3 `gradle/libs.versions.toml`

```toml
[versions]
selenium = "4.51.0"
serenity = "5.4.1"
commons-lang3 = "3.21.0"
junit = "4.13.2"
mockito = "5.24.0"
hamcrest = "2.2"

[libraries]
selenium-java  = { module = "org.seleniumhq.selenium:selenium-java", version.ref = "selenium" }
serenity-core  = { module = "net.serenity-bdd:serenity-core",        version.ref = "serenity" }
commons-lang3  = { module = "org.apache.commons:commons-lang3",      version.ref = "commons-lang3" }
junit          = { module = "junit:junit",                           version.ref = "junit" }
mockito-core   = { module = "org.mockito:mockito-core",              version.ref = "mockito" }
hamcrest       = { module = "org.hamcrest:hamcrest",                 version.ref = "hamcrest" }
```

### 4.4 根 `build.gradle.kts`

要点：
- `group = "io.github.ansonliao"`，`version = "2.0.0-SNAPSHOT"`
- `subprojects { apply(plugin = "java-library"); apply(plugin = "maven-publish") }`
- Toolchain + release 双保险：
  ```kotlin
  configure<JavaPluginExtension> {
      toolchain { languageVersion.set(JavaLanguageVersion.of(21)) }
      withSourcesJar(); withJavadocJar()
  }
  tasks.withType<JavaCompile> { options.release.set(21); options.encoding = "UTF-8" }
  ```
- **关键**：JUnit 4 必须显式 `tasks.test { useJUnit() }`。Gradle 默认 `useJUnitPlatform()`，JUnit 4 测试会被静默跳过（0 tests, BUILD SUCCESSFUL 的假绿）。
- Javadoc 容错：`tasks.withType<Javadoc> { (options as StandardJavadocDocletOptions).addStringOption("Xdoclint:none", "-quiet") }`（替代已废弃的 `additionalparam`）
- `maven-publish` 的 `pom {}` 填 name / description / url / licenses / developers / scm，替换原 `oss-parent:7`

### 4.5 模块依赖映射（Maven scope → Gradle configuration）

| 模块 | 依赖 | 原 scope | Gradle |
|---|---|---|---|
| java | selenium-java | compile | `api` |
| java | commons-lang3 | compile | `api` |
| java | junit / mockito-core / hamcrest | test | `testImplementation` |
| java | ~~phantomjsdriver / phantomjs-maven-plugin~~ | test/build | **删除** |
| matchers | htmlelements-java | compile | `api(project(":htmlelements-java"))` |
| matchers | hamcrest | **compile** | `api` |
| matchers | junit | **compile** | `api`（见下方说明） |
| matchers | mockito-core | test | `testImplementation` |
| thucydides | htmlelements-java | compile | `api(project(...))` |
| thucydides | serenity-core | — | `api` |
| thucydides | junit / mockito-core / hamcrest | 传递获取 | 改为**显式** `testImplementation` |
| all | 3 个内部模块 | compile | `api(project(...))` ×3 |

两个必须注意的点：
1. **matchers 的 JUnit 必须是 `api` 而非 `testImplementation`**：主代码 `ConditionMatcherDecorator.java:4` 使用 `org.junit.AssumptionViolatedException`，原 POM 里 JUnit 就是 compile scope。若降为 test，主源码编译失败。
2. **`htmlelements-all` 用 `java-library` + `api`，不要用 `java-platform`**。`java-platform` 产出 BOM（`dependencyManagement`），会改变下游消费语义；原 Maven 模块是带传递依赖的空 jar，用 `api` 才能等价复刻。

---

## 5. 代码改动清单（逐文件）

### 5.1 Selenium 4 编译级修复（8 个主源码文件）

已实测 Selenium 4.51 中 `org.openqa.selenium.internal` 包仅剩 `Debug`/`Require`/`Either`，`WrapsElement` 已上移到 `org.openqa.selenium`，`Locatable` 移入 `org.openqa.selenium.interactions`。

| # | 文件 | 改法 |
|---|---|---|
| 1 | `element/HtmlElement.java:10` | `import org.openqa.selenium.internal.WrapsElement;` → `import org.openqa.selenium.WrapsElement;` |
| 2 | `element/TypifiedElement.java:10` | 同上 |
| 3 | `loader/decorator/ProxyFactory.java:4-5,19` | `WrapsElement` 换包；`Locatable` 改为 `org.openqa.selenium.interactions.Locatable`。**保留 Locatable**（4.51 中 `Locatable.getCoordinates()` 仍存在），比删掉它能多保住一层下游兼容性 |
| 4 | `proxyhandlers/WebElementNamedProxyHandler.java:6-7,22,29,39,42` | `org.openqa.selenium.support.ui.Clock`/`SystemClock` 在 4.51 **已彻底删除**。改用 `System.currentTimeMillis()` 计算 deadline，保留 500ms sleep 与 Stale 重试语义 |
| 5 | `decorator/HtmlElementFieldAnnotationsHandler.java:39,44,49,65,84` | `Annotations` 的 `buildByFromFindBy`/`buildByFromFindBys`/`buildBysFromFindByOneOf` 在 4.51 **已删除**，改用构建器（见 5.2） |
| 6 | `decorator/HtmlElementClassAnnotationsHandler.java:27` | 同上，单点改 `new FindBy.FindByBuilder().buildIt(annotation, null)` |
| 7 | `matchers/WrapsElementMatchers.java:5` | `WrapsElement` 换包（另有 18 处方法签名随 import 一起生效） |
| 8 | `matchers/common/WrapsElementMatcher.java:8` | 同上 |

已核验**无需改动**的 API（避免过度改动）：
- `PageFactory.initElements(...)` 三种重载、`ElementLocator`、`ElementLocatorFactory`、`FieldDecorator`、`@FindBy/@FindBys/@FindAll/@CacheLookup`、`ByChained`/`ByAll` 全部保留
- `AjaxElementLocator(SearchContext,int,AbstractAnnotations)` 仍存在且未 deprecated
- `LocatingElementHandler`/`LocatingElementListHandler` 仍存在
- `Select` 全量方法保留；`TakesScreenshot`、`RemoteWebElement.setFileDetector`、`LocalFileDetector` 均保留
- `WebElement` 新增的 `getDomProperty`/`getDomAttribute`/`getAriaRole`/`getAccessibleName`/`getShadowRoot` **均为 default 方法**，`HtmlElement`/`TypifiedElement` 无需补实现

### 5.2 注解构建器改写（精确写法）

已用 `javap -c` 确认三处 `buildIt(T, Field)` 的字节码**完全不读取 Field 参数**（`FindByBuilder` 只做 `assertValidFindBy` → `buildByFromShortFindBy` → `buildByFromLongFindBy`；`FindBys`/`FindAll` 只做 `value()` 遍历 + `ByChained`/`ByAll`）。因此 `HtmlElementClassAnnotationsHandler` 传 `null` 不会 NPE。

```java
// HtmlElementFieldAnnotationsHandler.buildByFromFindAnnotations()
if (field.isAnnotationPresent(FindBys.class)) {
    return new FindBys.FindByBuilder().buildIt(field.getAnnotation(FindBys.class), field);
}
if (field.isAnnotationPresent(FindAll.class)) {
    return new FindAll.FindByBuilder().buildIt(field.getAnnotation(FindAll.class), field);
}
if (field.isAnnotationPresent(FindBy.class)) {
    return new FindBy.FindByBuilder().buildIt(field.getAnnotation(FindBy.class), field);
}
return null;
```

第 65、84 行（遍历父类找 `@FindBy`）同样替换为 `new FindBy.FindByBuilder().buildIt(annotation, getField())`；第 70 行 `buildByFromDefault()` 仍由 `Annotations` 提供（protected，保留）。

### 5.3 `WebElementNamedProxyHandler` 计时改写（含 NPE 修复）

```java
long end = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(timeOutInSeconds);
StaleElementReferenceException lastException = null;
do {
    try {
        return super.invoke(o, method, objects);
    } catch (StaleElementReferenceException e) {
        lastException = e;
        if (System.currentTimeMillis() < end) {
            waitFor();
        }
    }
} while (System.currentTimeMillis() < end);
throw lastException;
```
顺带修掉原实现 `timeOutInSeconds == 0` 时抛出 `null` 的缺陷（`throw lasException;` → NPE 语义丢失）。

### 5.4 Hamcrest 2.2 → 14 个文件删除 `@Factory`

`org.hamcrest.Factory` 注解在 Hamcrest 2.0 起已删除（实测 `hamcrest-2.2.jar` 中无该类）。17 处 `@Factory` 分布在 14 个文件：

- `matchers/decorators/`：`ActionMatcherDecorator`、`ConditionMatcherDecorator`、`MatcherDecoratorsBuilder`、`WaiterMatcherDecorator`
- `matchers/common/`：`DoesElementExistMatcher`、`HasAttributeMatcher`(×2)、`HasEnteredTextMatcher`(×2)、`HasSelectedOptionMatcher`、`HasSelectedRadioButtonMatcher`、`HasTextMatcher`(×2)、`IsCheckBoxSelectedMatcher`、`IsElementDisplayedMatcher`、`IsElementEnabledMatcher`、`WrapsElementMatcher`

操作：删除 `@Factory` 注解行 + 删除 `import org.hamcrest.Factory;`（13 个文件显式 import；`ConditionMatcherDecorator` 用 `org.hamcrest.*` 通配，无需动 import）。
影响评估：`@Factory` 仅用于 Hamcrest 的 matcher 自动生成工具，本仓库无代码生成，静态工厂方法直接调用不受影响。

### 5.5 Mockito 5.x 迁移（6 个文件）

| 文件 | 改动 |
|---|---|
| `matchers/test/.../mockfactory/MockFactory.java:8` | `import static org.mockito.Matchers.any;` → `org.mockito.ArgumentMatchers.any` |
| `ConditionMatcherDecoratorTest.java:8` | `org.mockito.runners.MockitoJUnitRunner` → `org.mockito.junit.MockitoJUnitRunner` |
| `ActionMatcherDecoratorTest.java:8` | 同上 |
| `RefreshPageMatcherDecoratorTest.java:8` | 同上 |
| `WaiterMatcherDecoratorTest.java:8` | 同上 |
| `NotSatisfiedConditionMatcherDecoratorTest.java:11` | 同上 |

已 grep 确认无 `anyObject`/`anyListOf`/`verifyZeroInteractions`/`initMocks`/`org.mockito.internal` 等其他 1.x 专属 API。

### 5.6 行为修正（2 个文件，非编译阻塞但必须做）

| 文件 | 问题 | 改法 |
|---|---|---|
| `element/TextInput.java:47` | Selenium 4 的 `getAttribute("value")` 按 W3C 返回 **HTML 属性**，不是用户输入后的实时值 → `getText()` 会读到初始值而非键入内容 | `getWrappedElement().getDomProperty("value")`（已实测 `getDomProperty` 是 `WebElement` 的 default 方法，可直接调用） |
| `utils/HtmlElementUtils.java:3,35,154-158` | ① 隐式依赖 Selenium 传递进来的 Guava `Lists`，Selenium 4 一旦剔除即断；② `Class.newInstance()` 自 JDK 9 deprecated；③ 反射读 `RemoteWebElement.parent` 字段，4.51 中字段仍在（`protected RemoteWebDriver parent`）但属高危私有 API | ① 改 `Arrays.asList` 或手写数组拼接；② 改 `getDeclaredConstructor().newInstance()`；③ 改 `remoteWebElement.getWrappedDriver().getClass().equals(RemoteWebDriver.class)`（`getWrappedDriver()` 已实测存在） |

`HtmlElementUtils:128` 的 `WordUtils.capitalizeFully` **保持不动**：`commons-lang3 3.21.0` 中 `org/apache/commons/lang3/text/WordUtils.class` 仍在，仅 deprecated，替换为 `StringUtils` 会改变「全小写化其余字符」的语义，不值得冒险。

### 5.7 Serenity 模块

`BlockPageObject.java` **源码零改动**。已用 `javap` 实测 `serenity-core 5.4.1`：

```
protected net.thucydides.core.pages.PageObject(
    org.openqa.selenium.WebDriver,
    com.google.common.base.Predicate<? super net.serenitybdd.core.pages.PageObject>)
```

- 包名 `net.thucydides.core.pages.PageObject` 未变 → import 不变
- 构造器字节码仅 `super()` → 赋 driver → `callback.apply(this)`，不触碰 Serenity 全局环境，Mockito mock driver 可用
- Lambda 参数类型推断为 `net.serenitybdd.core.pages.PageObject`，传给 `PageFactory.initElements(ElementLocatorFactory, Object)` 合法
- 与 `PageObject(WebDriver, int)` 无重载歧义（lambda 不兼容 int）

唯一改动：`htmlelements-thucydides/build.gradle.kts` 把 `thucydides-core` 换成 `serenity-core`。

---

## 6. 文件清理与 .gitignore

删除（均为不可逆操作，执行前需确认）：
- `pom.xml`（根 + 3 个有 POM 的模块，共 4 个）
- `mvnw`、`mvnw.cmd`、`.mvn/`（Maven Wrapper，未被 git 跟踪）
- `tools/apache-maven-3.9.16/`（**11 MB 完整 Maven 发行版，未被跟踪**，若 `git add -A` 会误入库）

`.gitignore` 重写——现有 `.*` 规则有严重副作用（`git check-ignore` 确认 `.mvn` 被忽略），且会连带忽略 `.github/workflows`：

```gitignore
build/
target/
.gradle/
.idea/
*.iml
.DS_Store
```

---

## 7. 文档与发布元数据

| 文件 | 动作 |
|---|---|
| `LICENCE` | **保留原文**（含 `Copyright 2012 YANDEX`） |
| `LICENSE` | **新增** Apache 2.0 全文 |
| `NOTICE` | **新增**（内容见 §2.3） |
| `README.md` | ① 顶部加 fork 声明与「not affiliated with Yandex」；② 替换失效徽章（`github-release-version.herokuapp.com`、`maven-badges.herokuapp.com` 均已停服）；③ 三处依赖示例改新坐标 + `2.0.0`；④ 更新 Java 版本说明（`Since 1.15 Java 8 is required` → `Java 21`）；⑤ 加 Gradle 构建命令 |
| `releasenotes/2.0.0-releasenotes.en.md` | **新增**，记录破坏性变更：坐标迁移、Selenium 4、Java 21、Thucydides→Serenity、移除 PhantomJS、`TextInput.getText()` 语义修正、代理不再保证 `Locatable` 之外的行为 |

`build.gradle.kts` 的 POM 元数据：`name` 去掉 "Yandex QATools"；`url`/`scm`/`issueManagement` 指向 `github.com/ansonliao/htmlelements`；`organization` 删除或改为 fork 维护者；`developers` **保留 5 位原始作者作为 credit**，追加 fork 维护者。

---

## 8. 执行顺序

| 阶段 | 内容 | 产出 |
|---|---|---|
| **P0** | `git checkout -b feature/selenium4-gradle-migration` | 分支就绪 |
| **P1** | 写 `settings.gradle.kts`、`libs.versions.toml`、根 `build.gradle.kts`、4 个模块 `build.gradle.kts`；`gradle wrapper --gradle-version 9.6.1` | Gradle 骨架可解析 |
| **P2** | 5.1 + 5.2 的 8 个主源码修复 + 5.7 无改动确认 | `compileJava` 通过 |
| **P3** | 5.4 Hamcrest（14 文件）+ 5.5 Mockito（6 文件） | `compileTestJava` 通过 |
| **P4** | 5.3 NPE 修复 + 5.6 行为修正 | 语义对齐 Selenium 4 |
| **P5** | 删除 4 个 `pom.xml` + `mvnw`/`.mvn`/`tools`；重写 `.gitignore` | 单一构建入口 |
| **P6** | `LICENSE` / `NOTICE` / `README.md` / release notes | 合规闭环 |
| **P7** | 全量验证（见 §9） | 绿灯 |

---

## 9. 验证清单

```bash
# 1. 编译
./gradlew clean compileJava compileTestJava

# 2. 测试（预期 39 个测试类 / 113 个 @Test，全部 Mockito，无需浏览器）
./gradlew test

# 3. 确认测试真的跑了（防 Gradle 默认 JUnit Platform 导致的假绿）
./gradlew test --info | grep -c "PASSED"

# 4. 打包 + 产物校验
./gradlew build
./gradlew :htmlelements-java:javadoc

# 5. 发布元数据校验（生成 POM 到本地仓库后人工检查 groupId/name/scm）
./gradlew publishToMavenLocal
cat ~/.m2/repository/io/github/ansonliao/htmlelements-java/2.0.0-SNAPSHOT/htmlelements-java-2.0.0-SNAPSHOT.pom
```

重点回归用例：
- `HtmlElementLocatorTimeoutTest` —— 超时路径 + `System.setProperty("webdriver.timeouts.implicitlywait","1")` 全局状态
- `FindAllTests` —— `@FindAll` → `ByAll` 构建器改写
- `PageInitializationTest` / `HtmlElementInitializationTest` / `InheritedHtmlElementInitializationTest` —— `@FindBys`/父类注解遍历
- `LazyInitializationTest` + `StaleElementReferenceTest` —— 代理与 Stale 重试（5.3 改动）
- `HtmlElementWorkWithMatcherTest` / `IsCheckedTest` —— `WrapsElementMatcher` 换包
- `BlockPageTest` —— Serenity `PageObject` 构造器

---

## 10. 风险与缓解

| 风险 | 等级 | 缓解 |
|---|---|---|
| Gradle 默认 `useJUnitPlatform()` 导致 JUnit 4 测试被静默跳过，出现「BUILD SUCCESSFUL 但 0 测试」 | **高** | 显式 `useJUnit()`；用 `--info \| grep PASSED` 核对数量 |
| Serenity 5.4.1 传递依赖树很大（含 selenium-java、jackson、httpclient 等），可能顶掉其他版本 | 中 | Gradle 默认取最高版本；`./gradlew :htmlelements-thucydides:dependencies` 审查；必要时 `resolutionStrategy` 锁定 selenium 4.51.0 |
| `BlockPageObject` 在 Serenity 5 下运行期行为未验证（构造器已确认轻量，但 super 无参构造会初始化 environment/clock） | 中 | `BlockPageTest` 作为门禁；若失败，改为最小化 `serenity-core` 依赖或用 `compileOnly` |
| 移除 `Locatable` 若采纳会让下游 `((Locatable) block).getCoordinates()` 失效 | 低 | 本计划选择**保留** `Locatable`（仅换包），风险归零 |
| `TextInput.getText()` 语义变更影响断言逻辑 | 低 | 改为 `getDomProperty("value")`，语义回到「当前输入值」，与 2.x 行为一致 |
| `HtmlElementLocatorTimeoutTest` 污染同一 JVM 的系统属性 | 低 | 与原 surefire 行为一致；必要时 `tasks.test { forkEvery = 1 }` |
| Java 包名仍为 `ru.yandex.qatools.*` | 低 | Apache 2.0 §6 允许「描述来源的合理使用」；且保留包名保住了下游源码兼容。已在 NOTICE 声明非官方 fork |

---

## 11. 本次不做（Out of scope）

- **JUnit 5 迁移**（约 70 个测试文件，与升级目标正交）
- **Java 包名重命名**（`ru.yandex.qatools.htmlelements` → `io.github.ansonliao.htmlelements`，122 文件 + API 全断，留作 3.0）
- **CI 配置**（GitHub Actions）—— 本次只修 `.gitignore` 让 `.github/` 可正常添加
- **Maven Central 实际发布**（需 GPG 签名、Sonatype 账号、namespace 验证；本计划只做到 `publishToMavenLocal` 校验元数据）

---

## 12. 改动文件统计

| 类别 | 文件数 |
|---|---:|
| 新增 Gradle 构建文件 | 7 |
| 生成 wrapper（`gradlew`/`gradlew.bat`/`gradle-wrapper.*`） | 4 |
| Selenium 4 主源码修复 | 8 |
| Hamcrest `@Factory` 清理 | 14 |
| Mockito 5.x 迁移 | 6 |
| 行为修正 | 2 |
| 法律/文档/版本文档 | 4 |
| `.gitignore` | 1 |
| 删除（4 个 pom + mvnw + .mvn + tools） | 7 项 |
| **合计改动文件** | **约 46 个 Java/配置 + 4 个文档** |
