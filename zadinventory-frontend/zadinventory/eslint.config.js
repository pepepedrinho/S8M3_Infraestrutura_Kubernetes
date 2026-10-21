// @ts-check
const eslint = require("@eslint/js");
const tseslint = require("typescript-eslint");
const angular = require("angular-eslint");

module.exports = tseslint.config(
  {
    // Artefatos de build e dependencias nao sao codigo-fonte do projeto.
    ignores: [
      "dist/**",
      "node_modules/**",
      ".angular/**",
      "coverage/**",
      "out-tsc/**",
    ],
  },
  {
    files: ["**/*.ts"],
    extends: [
      eslint.configs.recommended,
      ...tseslint.configs.recommended,
      ...angular.configs.tsRecommended,
    ],
    processor: angular.processInlineTemplates,
    rules: {
      "@angular-eslint/directive-selector": [
        "error",
        { type: "attribute", prefix: "app", style: "camelCase" },
      ],
      "@angular-eslint/component-selector": [
        "error",
        { type: "element", prefix: "app", style: "kebab-case" },
      ],

      // Divida tecnica pre-existente. Ver a nota no fim do arquivo: estas
      // regras viram aviso para nao reprovar codigo que ja estava no projeto,
      // mas o teto de avisos (--max-warnings no script "lint") impede que o
      // numero cresca. Qualquer OUTRA regra continua sendo erro e bloqueia.
      "@typescript-eslint/no-explicit-any": "warn",
      "@typescript-eslint/no-unused-vars": "warn",
      "@typescript-eslint/no-wrapper-object-types": "warn",
    },
  },
  {
    files: ["**/*.html"],
    extends: [
      ...angular.configs.templateRecommended,
      ...angular.configs.templateAccessibility,
    ],
    rules: {
      // Mesma divida tecnica, do lado dos templates: 43 das 64 ocorrencias da
      // primeira medicao eram regras de acessibilidade. Corrigi-las exigiria
      // reescrever 35 formularios, com risco de alterar o comportamento atual,
      // entao ficam registradas como aviso em vez de serem ignoradas.
      "@angular-eslint/template/label-has-associated-control": "warn",
      "@angular-eslint/template/elements-content": "warn",
      "@angular-eslint/template/click-events-have-key-events": "warn",
      "@angular-eslint/template/interactive-supports-focus": "warn",
    },
  }
);

/*
 * Primeira medicao (antes de qualquer ajuste): 64 erros, 0 avisos.
 *
 *   35  template/label-has-associated-control   acessibilidade
 *   11  no-explicit-any                         tipagem
 *    7  no-unused-vars                          limpeza
 *    6  template/elements-content               acessibilidade
 *    3  no-wrapper-object-types                 tipagem
 *    1  template/click-events-have-key-events   acessibilidade
 *    1  template/interactive-supports-focus     acessibilidade
 *
 * O script "lint" usa --max-warnings com esse total. Assim o CI reprova tanto
 * um erro novo (qualquer regra fora da lista acima) quanto o crescimento da
 * divida existente, sem exigir que ela seja quitada de uma vez. Ao corrigir
 * ocorrencias, baixe o teto no package.json para travar o ganho.
 */

