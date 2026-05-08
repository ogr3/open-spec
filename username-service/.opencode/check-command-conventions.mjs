import { existsSync } from "node:fs";
import path from "node:path";

const root = process.cwd();
const standard = path.join(root, "commands", "test.md");

const hasStandard = existsSync(standard);

if (!hasStandard) {
  console.error("Missing /test command: expected commands/test.md");
  process.exit(1);
}

console.log("OK: /test command is in standard path");
