# Style Guide


## Python
- Follow **PEP 8** style.
- Use **Black** formatter for consistent code formatting.


## Java
- Follow **Google Java Style Guide**.


## TypeScript
- Use **Prettier** for formatting.
- Follow Airbnb style conventions.

## Commit Messages
- Format:
  <type>: <short description>
  Types: `feat`, `fix`, `docs`, `style`, `refactor`, `test`.

- Example:

  feat: add Amazon API integration


### Commit Message Types

| Type      | Description                                        | Example                                    |
|-----------|----------------------------------------------------|--------------------------------------------|
| feat      | New feature or functionality                       | feat: add Amazon API integration           |
| fix       | Bug fix                                             | fix: correct null pointer in video worker  |
| docs      | Documentation changes only                         | docs: update system design diagram         |
| style     | Code style changes (formatting, no logic changes)  | style: apply Black formatting to models    |
| refactor  | Code restructuring without changing functionality  | refactor: split posting agent into modules |
| test      | Adding or modifying tests                          | test: add unit tests for API client        |


## Folder Naming
- Use lowercase with underscores for folders: `video_worker/`
- Keep related files grouped by feature.