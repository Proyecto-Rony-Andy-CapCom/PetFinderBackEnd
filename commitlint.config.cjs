module.exports = { 
  extends: ['@commitlint/config-conventional'],
  // Fuerza a commitlint a buscar en el node_modules de la carpeta actual
  rules: {
    'type-enum': [2, 'always', ['feat', 'fix', 'docs', 'style', 'refactor', 'test', 'chore', 'perf']]
  }
};