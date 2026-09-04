# Xinchuang Database Drivers

This directory holds JDBC drivers that are not available in Maven Central.

## Required JARs

| File | Database | Version | Source |
|---|---|---|---|
| `DmJdbcDriver18-8.1.3.62.jar` | 达梦 DM8 | 8.1.3.62 | Download from https://www.dameng.com/list_1033_119.html |
| `kingbase8-8.6.0.jar` | 人大金仓 KingbaseES | V8R6 | Download from https://bbs.kingbase.com.cn/ |

## Usage

Build with Xinchuang profile:
```bash
mvn -Pxinchuang-dm clean package -DskipTests
mvn -Pxinchuang-kingbase clean package -DskipTests
```

Default build (no profile) uses PostgreSQL and does NOT require these JARs.
