const fs = require('fs');
const path = require('path');
const { execSync } = require('child_process');

function getJavaFiles(dir) {
  let results = [];
  for (const entry of fs.readdirSync(dir, { withFileTypes: true })) {
    const full = path.join(dir, entry.name);
    if (entry.isDirectory()) {
      results = results.concat(getJavaFiles(full));
    } else if (entry.name.endsWith('.java')) {
      // Relative path without spaces
      results.push(full.replace(/\\/g, '/'));
    }
  }
  return results;
}

const files = getJavaFiles('src');
// In javac argfiles, if paths have spaces, escape them with double quotes or backslashes:
const argContent = files.map(f => '"' + path.resolve(f).replace(/\\/g, '\\\\') + '"').join('\n');
fs.writeFileSync('sources.txt', argContent);

try {
  console.log('Compiling ' + files.length + ' Java files...');
  execSync('javac --release 21 -d bin @sources.txt', { stdio: 'inherit' });
  console.log('Compilation succeeded!');
} finally {
  if (fs.existsSync('sources.txt')) fs.unlinkSync('sources.txt');
}
