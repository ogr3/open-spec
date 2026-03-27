# AGENTS.md

This is an OpenSpec specification repository. It uses a spec-driven workflow for managing changes to the project.

## Project Structure

```
open-spec/
├── openspec/
│   ├── config.yaml          # OpenSpec configuration
│   ├── specs/               # Capability specifications (spec.md files)
│   └── changes/
│       ├── <change-name>/   # Active changes with artifacts
│       └── archive/         # Archived completed changes
└── .opencode/               # OpenSpec CLI and skills
    ├── command/             # CLI command definitions
    ├── skills/              # Skill definitions
    └── package.json         # Opencode plugin dependencies
```

## OpenSpec Commands

Use the `openspec` CLI for all change management:

```bash
# List all changes
openspec list
openspec list --json

# Create a new change
openspec new change "<name>"

# Check change status
openspec status --change "<name>"
openspec status --change "<name>" --json

# Get artifact instructions
openspec instructions <artifact-id> --change "<name>" --json

# Other commands
openspec help
```

## Change Workflow

### 1. Propose (`/opsx-propose`)
Create a new change with all artifacts in one step:
- Creates `openspec/changes/<name>/`
- Generates proposal.md, design.md, tasks.md

### 2. Apply (`/opsx-apply`)
Implement tasks from an active change:
- Reads context files (proposal, specs, design, tasks)
- Implements each task sequentially
- Marks tasks complete with `- [x]`

### 3. Archive (`/opsx-archive`)
Archive a completed change:
- Moves change to `openspec/changes/archive/YYYY-MM-DD-<name>/`
- Syncs delta specs to main specs if needed

### 4. Explore (`/opsx-explore`)
Thinking partner mode:
- Read code, investigate, ask questions
- **Never write code** - that's for apply phase
- Can create spec artifacts (captures thinking)

## Naming Conventions

| Element | Convention | Example |
|---------|------------|---------|
| Changes | kebab-case | `add-user-auth`, `fix-login-bug` |
| Capabilities | kebab-case | `user-authentication`, `api-v2` |
| Tasks | sentence case | "Add user validation", "Update config" |

## Task Checkbox Format

Use this format in tasks.md files:

```markdown
## Tasks

- [ ] Task description
- [x] Completed task
```

Mark tasks complete by changing `- [ ]` to `- [x]` immediately after implementation.

## Markdown Style

- Use ATX-style headings (`#`, `##`, etc.)
- One blank line between headings and content
- Wrap lines at 80 characters when practical
- Use fenced code blocks with language hints:
  ```markdown
  ```typescript
  const example = "code";
  ```
  ```

## YAML Conventions

- Use 2-space indentation
- No trailing whitespace
- Use lowercase for keys
- Quote strings that could be misinterpreted

## OpenSpec Artifact Files

Each change directory contains:

| Artifact | Purpose |
|----------|---------|
| `.openspec.yaml` | Change metadata and schema config |
| `proposal.md` | What & why (scope, goals, non-goals) |
| `specs/*.md` | Capability specifications (delta specs) |
| `design.md` | How (architecture, data model, API) |
| `tasks.md` | Implementation checklist |

## Code Implementation Rules

When implementing tasks:

1. **Read context first** - Always read relevant artifacts before writing code
2. **Minimal changes** - Keep each task scoped to its description
3. **Update tasks immediately** - Mark `- [ ]` → `- [x]` after completing each task
4. **No guessing** - If unclear, pause and ask
5. **Suggest artifact updates** - If implementation reveals design issues

## Error Handling

When encountering issues during implementation:

```
## Implementation Paused

**Change:** <name>
**Progress:** N/M tasks complete

### Issue Encountered
<description>

**Options:**
1. <option 1>
2. <option 2>
```

## Output Style

Use ASCII boxes for structured output:

```
┌─────────────────────────────────────┐
│ Section Header                      │
├─────────────────────────────────────┤
│ Content                             │
│                                     │
└─────────────────────────────────────┘
```

Use tables for comparisons:

```
| Before | After | Notes |
|--------|-------|-------|
| foo    | bar   | Good  |
```

## General Guidelines

- Be concise in responses (1-3 sentences unless detail requested)
- Use appropriate formatting (headers, lists, code blocks)
- Never commit secrets or credentials
- Respect existing conventions in the codebase
