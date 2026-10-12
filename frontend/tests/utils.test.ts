import { formatCurrency, formatDuration, formatKwh } from "../lib/utils";

// Lightweight unit assertions verifying frontend presentation rules
function assertEqual(actual: unknown, expected: unknown, name: string) {
  if (actual !== expected) {
    throw new Error(`Assertion failed for ${name}: expected "${expected}" but got "${actual}"`);
  }
}

export function runFrontendUtilityTests() {
  assertEqual(formatKwh(42.5), "42.50 kWh", "formatKwh decimals");
  assertEqual(formatCurrency(19.5), "$19.50", "formatCurrency USD");
  assertEqual(formatDuration(90), "1m 30s", "formatDuration minutes & seconds");
  assertEqual(formatDuration(3665), "1h 1m", "formatDuration hours & minutes");
  console.log("✓ All frontend presentation utility tests passed successfully.");
}

// Auto-run if executed directly via node or test runner
if (typeof process !== "undefined" && process.argv[1]?.includes("utils.test.ts")) {
  runFrontendUtilityTests();
}
