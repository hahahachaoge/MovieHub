# MovieHub 项目规则 — 隐私安全

本项目所有隐私信息已迁移到 `.env` + PEM 文件，详见 `SECRETS_MAP.md`（已 gitignore）。

## 密钥存放位置
| 内容 | 位置 | 是否提交 |
|------|------|---------|
| 所有环境变量/密钥 | `backend/.env` | ❌ .gitignore |
| 支付宝 PEM 密钥 | `backend/certs/*.pem` | ❌ .gitignore |
| Natapp 配置 | `config.ini` | ❌ .gitignore |
| 隐私汇总提醒 | `SECRETS_MAP.md` | ❌ .gitignore |

## 启动方式
```bash
# 后端
cd backend && mvn spring-boot:run

# 前端
cd frontend && npm run dev

# 内网穿透
./natapp.exe
```
