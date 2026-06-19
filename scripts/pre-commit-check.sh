#!/bin/bash
# Git pre-commit hook: 检查是否有隐私信息泄露
# 使用方式: 复制到 .git/hooks/pre-commit

SECRETS=("sk-[a-zA-Z0-9]\{30,\}" "MIIEvgIBADAN" "-----BEGIN RSA PRIVATE KEY-----" "-----BEGIN PRIVATE KEY-----")
PASS=true

for pattern in "${SECRETS[@]}"; do
  MATCHES=$(git diff --cached --name-only | xargs grep -l "$pattern" 2>/dev/null)
  if [ -n "$MATCHES" ]; then
    echo "❌ 发现疑似密钥泄露！文件: $MATCHES"
    echo "   匹配模式: $pattern"
    PASS=false
  fi
done

# 检查是否有 .env 被提交
ENV_FILES=$(git diff --cached --name-only | grep "\.env$" | grep -v "\.env\.example$")
if [ -n "$ENV_FILES" ]; then
  echo "❌ 试图提交 .env 文件！这绝不允许！"
  echo "   文件: $ENV_FILES"
  PASS=false
fi

if [ "$PASS" = false ]; then
  echo ""
  echo "⛔ 提交被阻止：存在隐私信息泄露风险"
  echo "   请移除上述文件后再提交"
  exit 1
fi

exit 0
