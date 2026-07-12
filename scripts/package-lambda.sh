#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"
SOURCE_FILE="serverless-notification-function/lambda_function.py"
OUTPUT_ZIP="serverless-notification-function/sanos-report-processor.zip"
TEMP_DIR="serverless-notification-function/.package-tmp"

cd "$REPO_ROOT"

if [ ! -f "$SOURCE_FILE" ]; then
  echo "Lambda source file was not found at $SOURCE_FILE" >&2
  exit 1
fi

rm -rf "$TEMP_DIR"
mkdir -p "$TEMP_DIR"
cp "$SOURCE_FILE" "$TEMP_DIR/lambda_function.py"
rm -f "$OUTPUT_ZIP"

python -c "import pathlib, zipfile; temp = pathlib.Path(r'$TEMP_DIR'); output = pathlib.Path(r'$OUTPUT_ZIP'); source = temp / 'lambda_function.py'; zipfile.ZipFile(output, 'w', compression=zipfile.ZIP_DEFLATED).write(source, arcname='lambda_function.py')"

python -c "import pathlib, sys, zipfile; output = pathlib.Path(r'$OUTPUT_ZIP'); zf = zipfile.ZipFile(output); names = zf.namelist(); zf.close(); assert names == ['lambda_function.py'], f'Unexpected ZIP contents: {names}'"

rm -rf "$TEMP_DIR"

printf 'Lambda package created at: %s\n' "$OUTPUT_ZIP"
