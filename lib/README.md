# Xinchuang Database Drivers

The Xinchuang profiles resolve JDBC drivers through Maven coordinates. Publish the
vendor-provided JARs to your organization Maven repository, or install them into a
developer's local Maven repository before activating a profile.

## Required JARs

| File | Database | Version | Source |
|---|---|---|---|
| `DmJdbcDriver18-8.1.3.62.jar` | 达梦 DM8 | 8.1.3.62 | Download from https://www.dameng.com/list_1033_119.html |
| `kingbase8-8.6.0.jar` | 人大金仓 KingbaseES | V8R6 | Download from https://bbs.kingbase.com.cn/ |

## Local installation

```bash
./mvnw install:install-file \
  -Dfile=/path/to/DmJdbcDriver18-8.1.3.62.jar \
  -DgroupId=com.dameng \
  -DartifactId=DmJdbcDriver18 \
  -Dversion=8.1.3.62 \
  -Dpackaging=jar

./mvnw install:install-file \
  -Dfile=/path/to/kingbase8-8.6.0.jar \
  -DgroupId=cn.com.kingbase \
  -DartifactId=kingbase8 \
  -Dversion=8.6.0 \
  -Dpackaging=jar
```

Do not commit vendor JDBC JARs to this repository.

## Usage

Build with Xinchuang profile:
```bash
./mvnw -Pxinchuang-dm clean verify
./mvnw -Pxinchuang-kingbase clean verify
```

Default build (no profile) uses PostgreSQL and does NOT require these JARs.
