---
layout: page
title: Testing guide
---

* Table of Contents
{:toc}

--------------------------------------------------------------------------------------------------------------------

## Running tests

You can run tests in two ways.

* **Method 1: Using IntelliJ JUnit test runner**
  * To run all tests, right-click on the `src/test/java` folder and choose `Run 'All Tests'`
  * To run a subset of tests, you can right-click on a test package,
    test class, or a test and choose `Run 'ABC'`
* **Method 2: Using Gradle**
  * Open a console and run the command `gradlew clean test` (Mac/Linux: `./gradlew clean test`)

<div markdown="span" class="alert alert-secondary">:link: **Link**: Read [this Gradle Tutorial from the se-edu/guides](https://se-education.org/guides/tutorials/gradle.html) to learn more about using Gradle.
</div>

--------------------------------------------------------------------------------------------------------------------

## JavaFX UI tests on supported platforms

The `MainWindowTest`, `ResultDisplayTest`, pet card/list, and appointment card/list tests run on Linux, macOS, and
Windows. They require a working graphical environment. Run `./gradlew clean check` in a macOS desktop session,
`gradlew.bat clean check` on Windows, or `xvfb-run --auto-servernum ./gradlew clean check` on headless Linux.
The CI workflow runs these checks on all three platforms and provides Xvfb on Linux.

The `check` task verifies that all six UI test classes ran at least one test and skipped none. Inspect
`build/reports/tests/test/index.html` or the `tests` and `skipped` attributes in
`build/test-results/test/TEST-<fully-qualified-class-name>.xml` for the result of an individual class. A skipped
test has not validated its UI behavior and does not contribute execution data to the coverage report.

--------------------------------------------------------------------------------------------------------------------

## Types of tests

This project has three types of tests:

1. *Unit tests* target the lowest-level methods and classes.<br>
   For example: `seedu.address.commons.StringUtilTest`
1. *Integration tests* check how multiple code units work together; the individual units are assumed to work.<br>
   For example: `seedu.address.storage.StorageManagerTest`
1. *Hybrid tests* combine unit and integration testing. These tests check both the individual units and how they work together.<br>
   For example: `seedu.address.logic.LogicManagerTest`
