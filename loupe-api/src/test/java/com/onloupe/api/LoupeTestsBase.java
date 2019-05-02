package com.onloupe.api;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestReporter;

import java.io.IOException;

public class LoupeTestsBase {
	@BeforeAll
	public static final void runBeforeAnyTests(TestReporter reporter) throws IOException {
		SetUpTests.runBeforeAnyTests(reporter);
	}

	@AfterAll
	public static final void runAfterAllTests() throws IOException {
		SetUpTests.runAfterAllTests();
	}
}