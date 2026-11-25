# Workshop Registration System – EX2 Maven Evolution

Maven multi-module project:

```
root                 (aggregator + top parent)            pom.xml
|-- core             (aggregator + parent of impl modules) core/pom.xml
|   |-- model-impl   implementation of the model interfaces
|   `-- logic-impl   WorkshopSystemImpl (+ JsonExporter, CsvStudentImporter)
`-- app              workshop-cli: Main + the uber jar workshop-cli.jar
```

groupId of all modules: `patmal.workshops.student.impl`, version `1.0-SNAPSHOT` (declared in the root pom only).
Java 25, UTF-8.

## 1. Requirements

- JDK 25 (`java -version` and `mvn -v` must both show Java 25)
- Maven 3.9+

## 2. One-time setup: install the API jars and the tester into the local repository

The API jars and the tester are not on Maven Central, so they are installed into `~/.m2` with
`install:install-file`, once on every computer. The jars are in the `lib` folder of this repository.
Run from the root folder of the project.

Linux / macOS / Windows cmd:

```
mvn install:install-file -Dfile=lib/model-2.0.jar -DgroupId=patmal.workshops -DartifactId=model -Dversion=2.0 -Dpackaging=jar -DgeneratePom=true
mvn install:install-file -Dfile=lib/logic-2.0.jar -DgroupId=patmal.workshops -DartifactId=logic -Dversion=2.0 -Dpackaging=jar -DgeneratePom=true
mvn install:install-file -Dfile=lib/workshop-tester-2.0.jar -DgroupId=patmal.workshops -DartifactId=workshop-tester -Dversion=2.0 -Dpackaging=jar -DgeneratePom=true
```

Windows PowerShell (each `-D` argument must be quoted):

```
mvn install:install-file "-Dfile=lib/model-2.0.jar" "-DgroupId=patmal.workshops" "-DartifactId=model" "-Dversion=2.0" "-Dpackaging=jar" "-DgeneratePom=true"
mvn install:install-file "-Dfile=lib/logic-2.0.jar" "-DgroupId=patmal.workshops" "-DartifactId=logic" "-Dversion=2.0" "-Dpackaging=jar" "-DgeneratePom=true"
mvn install:install-file "-Dfile=lib/workshop-tester-2.0.jar" "-DgroupId=patmal.workshops" "-DartifactId=workshop-tester" "-Dversion=2.0" "-Dpackaging=jar" "-DgeneratePom=true"
```

`-DgeneratePom=true` creates a minimal pom (no dependencies) for each jar. Because the jars have no real pom,
Maven does not know that `logic` uses `model`, so every module that uses `logic` declares `model` as well.

## 3. Build

```
mvn clean install
```

This builds all modules in the right order and produces the uber jar `app/target/workshop-cli.jar`.
In the `verify` phase the tester runs automatically (bonus 8.2) and a failing tester fails the build.
To build without running the tester:

```
mvn clean install -DskipTester=true
```

## 4. Run

```
java -jar app/target/workshop-cli.jar                 (demo students from students.csv inside the jar)
java -jar app/target/workshop-cli.jar my-students.csv (demo students from the given CSV file)
```

The application writes `instructors.json`, `students.json` and `teams.json` to the current folder.

Run the tester manually against the uber jar (use `:` instead of `;` on Linux / macOS):

```
java -cp "lib/workshop-tester-2.0.jar;app/target/workshop-cli.jar" patmal.workshops.main.Main
```

## 5. Dependencies and why

All versions are properties in the root pom and are managed in its `dependencyManagement`
(Log4j and Jackson through their BOMs). Child poms never repeat a version.

| Library | Module / scope | Why |
|---|---|---|
| `org.apache.logging.log4j:log4j-api` | logic-impl, app / compile | Logging API used in the code |
| `org.apache.logging.log4j:log4j-core` | app / runtime | Logging implementation, needed only at run time; config in `app/src/main/resources/log4j2.xml` |
| `commons-validator:commons-validator` | logic-impl / compile | `EmailValidator` for createStudent / createInstructor |
| `com.fasterxml.jackson.core:jackson-databind` | logic-impl / compile | JSON exports, built with the tree model (`ObjectNode`/`ArrayNode`) so there are no cycles |
| `org.apache.commons:commons-csv` | logic-impl / compile | CSV import (bonus) |
| `patmal.workshops:workshop-tester` | app / test | Run by the build in `verify`; test scope keeps it out of the uber jar |

Transitive dependencies (see `mvn dependency:tree`): `jackson-databind` brings `jackson-core` and
`jackson-annotations`; `commons-validator` brings `commons-beanutils`, `commons-digester`, `commons-logging`
and `commons-collections`; `commons-csv` brings `commons-io` and `commons-codec`.

Plugins (versions in the root `pluginManagement`): `maven-compiler-plugin`, `maven-shade-plugin`
(uber jar with the main class in the manifest, `finalName` = `workshop-cli`), `exec-maven-plugin` (tester in `verify`).
