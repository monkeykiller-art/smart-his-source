# Smart HIS 前后端接口映射文档

> 生成时间: 2026-09-20  
> 前端: React 18 + TypeScript + Vite 8 + Ant Design 5  
> 后端: Spring Boot 3.2.4 + Spring Cloud Gateway + Nacos  
> 总计: **109 个 API 端点**，**6 个微服务**

---

## 1. 架构概览

```
浏览器 (:5173)
  │
  │  axios baseURL: /api
  │  自动附加 Authorization: Bearer <JWT>
  │  401 自动 refresh → POST /api/auth/refresh
  ▼
Vite Dev Proxy (开发) / Nginx (生产)
  │
  │  /api/** → http://localhost:8080
  ▼
API Gateway (his-gateway :8080)
  │
  │  TraceIdGlobalFilter (order:-200) → 生成/透传 X-Trace-Id
  │  JwtAuthGlobalFilter (order:-100) → JWT 验证 + 用户上下文注入
  │
  ├── /api/auth/**       → lb://his-auth       (:8081)
  ├── /api/patient/**    → lb://his-patient    (:8082)
  ├── /api/clinical/**   → lb://his-clinical   (:8083)
  ├── /api/operations/** → lb://his-operations (:8085)
  ├── /api/pharma/**     → lb://his-pharma     (:8087)
  └── /api/emergency/**  → lb://his-emergency  (:8089)
```

---

## 2. 认证流程

| 步骤 | 接口 | 说明 |
|------|------|------|
| 1. 登录 | `POST /api/auth/login` | 返回 accessToken (30min) + refreshToken (7d) |
| 2. 请求携带 | `Authorization: Bearer <accessToken>` | http.ts 拦截器自动注入 |
| 3. Token 过期 | `POST /api/auth/refresh` | 白名单免认证，返回新 accessToken |
| 4. 登出 | `POST /api/auth/logout` | 服务端加入 Redis 黑名单 |

### 网关白名单（免认证）

```
/、/index.html、/assets/**
/api/auth/login
/api/auth/refresh
/api/auth/captcha
/api/*/health
/actuator/**
/*/v3/api-docs/**、/swagger-ui/**
```

### JWT 注入的下游请求头

| 请求头 | JWT Claim | 说明 |
|--------|-----------|------|
| `X-User-Id` | `sub` | 用户 ID |
| `X-Username` | `un` | 用户名 |
| `X-Real-Name` | `rn` | 真实姓名 |
| `X-Roles` | `roles` | 角色列表 |
| `X-Permissions` | `perms` | 权限列表 |
| `X-Dept-Id` | `dept` | 科室 ID（可选） |
| `X-Trace-Id` | — | 链路追踪 ID |

---

## 3. 接口映射明细

### 3.1 his-auth (:8081) — 认证服务

| # | 方法 | 前端路径 | 前端函数 | 前端文件 |
|---|------|---------|---------|---------|
| 1 | POST | `/auth/login` | `login` | authApi.ts |
| 2 | POST | `/auth/logout` | `logout` | authApi.ts |
| 3 | GET | `/auth/logs/operations` | `operationLogs` | m9Api.ts |
| 4 | POST | `/auth/mfa/setup` | `setupMfa` | m9Api.ts |
| 5 | POST | `/auth/mfa/enable` | `enableMfa` | m9Api.ts |

### 3.2 his-patient (:8082) — 患者服务

| # | 方法 | 前端路径 | 前端函数 | 前端文件 |
|---|------|---------|---------|---------|
| 1 | GET | `/patient/patients/search` | `search` | patientApi.ts |
| 2 | GET | `/patient/patients` | `query` | patientApi.ts |
| 3 | GET | `/patient/patients/${id}` | `getById` | patientApi.ts |
| 4 | POST | `/patient/patients` | `create` | patientApi.ts |
| 5 | PUT | `/patient/patients/${id}` | `update` | patientApi.ts |
| 6 | GET | `/patient/departments` | `listDepartments` | registrationApi.ts |
| 7 | GET | `/patient/doctors` | `listDoctors` | registrationApi.ts |
| 8 | GET | `/patient/schedules` | `querySchedules` | registrationApi.ts |
| 9 | GET | `/patient/registrations` | `queryRegistrations` | registrationApi.ts |
| 10 | POST | `/patient/registrations` | `create` | registrationApi.ts |
| 11 | PUT | `/patient/registrations/${id}/cancel` | `cancel` | registrationApi.ts |
| 12 | PUT | `/patient/registrations/${id}/pay` | `markPaid` | registrationApi.ts |
| 13 | PUT | `/patient/registrations/${id}/refund` | `refund` | registrationApi.ts |
| 14 | GET | `/patient/encounters/by-reg/${registrationId}` | `getEncounterByRegistration` | registrationApi.ts |
| 15 | POST | `/patient/encounters` | `openEncounter` | registrationApi.ts |
| 16 | PUT | `/patient/encounters/${encounterId}/close` | `closeEncounter` | registrationApi.ts |
| 17 | GET | `/patient/admissions` | `admissions` | m8Api.ts |
| 18 | PUT | `/patient/admissions/${id}/admit` | `admit` | m8Api.ts |
| 19 | GET | `/patient/inpatient-beds` | `beds` | m8Api.ts |
| 20 | PUT | `/patient/admissions/${id}/transfer` | `transfer` | m8Api.ts |
| 21 | PUT | `/patient/admissions/${id}/discharge` | `discharge` | m8Api.ts |
| 22 | GET | `/patient/surgeries` | `surgeries` | m8Api.ts |
| 23 | POST | `/patient/surgeries` | `applySurgery` | m8Api.ts |
| 24 | PUT | `/patient/surgeries/${id}/schedule` | `scheduleSurgery` | m8Api.ts |
| 25 | PUT | `/patient/surgeries/${id}/start` | `surgeryAction` | m8Api.ts |
| 26 | PUT | `/patient/surgeries/${id}/complete` | `surgeryAction` | m8Api.ts |

### 3.3 his-clinical (:8083) — 临床服务

| # | 方法 | 前端路径 | 前端函数 | 前端文件 |
|---|------|---------|---------|---------|
| **病历** |
| 1 | GET | `/clinical/records/patient/${patientId}` | `listRecords` | clinicalApi.ts |
| 2 | POST | `/clinical/records` | `createRecord` | clinicalApi.ts |
| 3 | GET | `/clinical/records/encounter/${encounterId}` | `listRecordsByEncounter` | clinicalApi.ts |
| 4 | PUT | `/clinical/records/${id}` | `updateRecord` | clinicalApi.ts |
| 5 | PUT | `/clinical/records/${id}/sign` | `signRecord` | clinicalApi.ts |
| **诊断** |
| 6 | GET | `/clinical/diagnoses/encounter/${encounterId}` | `listDiagnoses` | clinicalApi.ts |
| 7 | POST | `/clinical/diagnoses` | `createDiagnosis` | clinicalApi.ts |
| 8 | DELETE | `/clinical/diagnoses/${id}` | `deleteDiagnosis` | clinicalApi.ts |
| 9 | GET | `/clinical/icd10/search` | `searchIcd10` | clinicalApi.ts |
| **检查** |
| 10 | GET | `/clinical/exam-requests/patient/${patientId}` | `listExamRequests` | clinicalApi.ts |
| 11 | POST | `/clinical/exam-requests` | `createExamRequest` | clinicalApi.ts |
| 12 | PUT | `/clinical/exam-requests/${id}/status/${status}` | `updateExamRequestStatus` | clinicalApi.ts |
| 13 | PUT | `/clinical/exam-requests/${id}/result` | `reportExamRequest` | clinicalApi.ts |
| 14 | PUT | `/clinical/exam-requests/${id}/critical/acknowledge` | `acknowledgeCriticalExam` | clinicalApi.ts |
| **医嘱** |
| 15 | GET | `/clinical/orders/patient/${patientId}` | `listOrders` | clinicalApi.ts |
| 16 | POST | `/clinical/orders` | `createOrder` | clinicalApi.ts |
| 17 | PUT | `/clinical/orders/${id}/cancel` | `cancelOrder` | clinicalApi.ts |
| 18 | PUT | `/clinical/orders/${id}/submit` | `submitOrder` | clinicalApi.ts |
| **常用短语** |
| 19 | GET | `/clinical/common-phrases` | `listCommonPhrases` | clinicalApi.ts |
| 20 | POST | `/clinical/common-phrases` | `createCommonPhrase` | clinicalApi.ts |
| 21 | PUT | `/clinical/common-phrases/${id}` | `updateCommonPhrase` | clinicalApi.ts |
| 22 | DELETE | `/clinical/common-phrases/${id}` | `deleteCommonPhrase` | clinicalApi.ts |
| **病历模板** |
| 23 | GET | `/clinical/record-templates` | `listRecordTemplates` | clinicalApi.ts |
| 24 | POST | `/clinical/record-templates` | `createRecordTemplate` | clinicalApi.ts |
| 25 | PUT | `/clinical/record-templates/${id}` | `updateRecordTemplate` | clinicalApi.ts |
| 26 | DELETE | `/clinical/record-templates/${id}` | `deleteRecordTemplate` | clinicalApi.ts |

### 3.4 his-operations (:8085) — 运营服务

| # | 方法 | 前端路径 | 前端函数 | 前端文件 |
|---|------|---------|---------|---------|
| **账单** |
| 1 | GET | `/operations/bills` | `queryBills` | operationsApi.ts |
| 2 | GET | `/operations/bills/${id}` | `getBill` | operationsApi.ts |
| 3 | POST | `/operations/bills/${id}/invoice` | `issueInvoice` | operationsApi.ts |
| 4 | GET | `/operations/bills/${id}/items` | `listBillItems` | operationsApi.ts |
| 5 | GET | `/operations/bills/${id}/transactions` | `listTransactions` | operationsApi.ts |
| 6 | POST | `/operations/bills/${id}/payments` | `payBill` | operationsApi.ts |
| 7 | POST | `/operations/bills/${id}/refunds` | `refundBill` | operationsApi.ts |
| 8 | POST | `/operations/bills/${id}/void` | `voidBill` | operationsApi.ts |
| 9 | POST | `/operations/bills/admission` | `createAdmissionBill` | m8Api.ts |
| **收费项目** |
| 10 | GET | `/operations/fee-items` | `queryFeeItems` | operationsApi.ts |
| 11 | POST | `/operations/fee-items` | `createFeeItem` | operationsApi.ts |
| 12 | PUT | `/operations/fee-items/${id}` | `updateFeeItem` | operationsApi.ts |
| **结算** |
| 13 | GET | `/operations/settlements` | `querySettlements` | operationsApi.ts |
| 14 | GET | `/operations/settlements/preview/${admissionId}` | `previewSettlement` | operationsApi.ts |
| 15 | POST | `/operations/settlements` | `createSettlement` | operationsApi.ts |
| **押金** |
| 16 | GET | `/operations/deposits` | `queryDeposits` | operationsApi.ts |
| 17 | POST | `/operations/deposits` | `createDeposit` | operationsApi.ts |
| 18 | POST | `/operations/deposits/${id}/refund` | `refundDeposit` | operationsApi.ts |
| **收费员** |
| 19 | GET | `/operations/accounts` | `queryCashierAccounts` | operationsApi.ts |
| **医保** |
| 20 | GET | `/operations/insurance/preview/${admissionId}` | `previewInsurance` | operationsApi.ts |
| **分析** |
| 21 | GET | `/operations/analytics/summary` | `summary` | m9Api.ts |
| 22 | GET | `/operations/analytics/departments` | `departments` | m9Api.ts |
| 23 | GET | `/operations/analytics/doctors` | `doctors` | m9Api.ts |
| 24 | GET | `/operations/analytics/filters` | `filters` | m9Api.ts |
| 25 | POST | `/operations/analytics/filters` | `saveFilter` | m9Api.ts |
| 26 | GET | `/operations/analytics/export.{format}` | `exportReport` | m9Api.ts |

### 3.5 his-pharma (:8087) — 药房服务

| # | 方法 | 前端路径 | 前端函数 | 前端文件 |
|---|------|---------|---------|---------|
| **药品管理** |
| 1 | GET | `/pharma/drugs` | `queryDrugs` | pharmaApi.ts |
| 2 | POST | `/pharma/drugs` | `createDrug` | pharmaApi.ts |
| 3 | PUT | `/pharma/drugs/${id}` | `updateDrug` | pharmaApi.ts |
| 4 | PUT | `/pharma/drugs/${id}/active` | `setDrugActive` | pharmaApi.ts |
| **库存** |
| 5 | GET | `/pharma/inventory/batches` | `listBatches` | pharmaApi.ts |
| 6 | GET | `/pharma/inventory/near-expiry` | `listNearExpiry` | pharmaApi.ts |
| 7 | POST | `/pharma/inventory/operations` | `operateInventory` | pharmaApi.ts |
| 8 | GET | `/pharma/inventory/transactions` | `traceInventory` | pharmaApi.ts |
| **发药** |
| 9 | POST | `/pharma/dispenses` | `dispense` | pharmaApi.ts |
| 10 | GET | `/pharma/dispenses/patient/${patientId}` | `listDispenses` | pharmaApi.ts |
| 11 | PUT | `/pharma/dispenses/${id}/return` | `returnDispense` | pharmaApi.ts |
| **处方审核** |
| 12 | GET | `/pharma/rx-reviews` | `queryReviews` | pharmaApi.ts |
| 13 | GET | `/pharma/rx-reviews/${id}` | `getReview` | pharmaApi.ts |
| 14 | PUT | `/pharma/rx-reviews/${id}/approve` | `approveReview` | pharmaApi.ts |
| 15 | PUT | `/pharma/rx-reviews/${id}/reject` | `rejectReview` | pharmaApi.ts |
| **用药安全** |
| 16 | POST | `/pharma/drug-interactions/check` | `checkDrugInteraction` | pharmaApi.ts |
| 17 | GET | `/pharma/dose-limits` | `queryDoseLimits` | pharmaApi.ts |
| 18 | GET | `/pharma/allergy-cross` | `queryAllergyCross` | pharmaApi.ts |

### 3.6 his-emergency (:8089) — 急诊服务

| # | 方法 | 前端路径 | 前端函数 | 前端文件 |
|---|------|---------|---------|---------|
| **分诊** |
| 1 | GET | `/emergency/triage` | `emergencyQueue` | m8Api.ts |
| 2 | POST | `/emergency/triage` | `createTriage` | m8Api.ts |
| 3 | PUT | `/emergency/triage/${id}/status` | `updateTriageStatus` | m8Api.ts |
| **抢救** |
| 4 | GET | `/emergency/resuscitations` | `resuscitations` | m8Api.ts |
| 5 | POST | `/emergency/resuscitations` | `startResuscitation` | m8Api.ts |
| 6 | PUT | `/emergency/resuscitations/${id}/complete` | `completeResuscitation` | m8Api.ts |
| **留观** |
| 7 | GET | `/emergency/observations` | `observations` | m8Api.ts |
| 8 | POST | `/emergency/observations` | `admitObservation` | m8Api.ts |
| 9 | PUT | `/emergency/observations/${id}/discharge` | `dischargeObservation` | m8Api.ts |

---

## 4. 统计汇总

### 按服务分布

| 后端服务 | 端口 | 端点数 | 占比 |
|---------|------|-------|------|
| his-patient | 8082 | 26 | 23.9% |
| his-operations | 8085 | 26 | 23.9% |
| his-clinical | 8083 | 26 | 23.9% |
| his-pharma | 8087 | 18 | 16.5% |
| his-emergency | 8089 | 9 | 8.3% |
| his-auth | 8081 | 4 | 3.7% |
| **合计** | | **109** | **100%** |

### 按 HTTP 方法

| 方法 | 数量 | 用途 |
|------|------|------|
| GET | 55 | 查询/列表/搜索 |
| POST | 27 | 创建/操作/提交 |
| PUT | 25 | 更新/状态变更 |
| DELETE | 2 | 删除 |

### 按前端文件

| 前端文件 | 端点数 | 涉及后端服务 |
|---------|-------|-------------|
| clinicalApi.ts | 26 | his-clinical |
| operationsApi.ts | 19 | his-operations |
| m8Api.ts | 19 | his-patient, his-operations, his-emergency |
| pharmaApi.ts | 18 | his-pharma |
| registrationApi.ts | 11 | his-patient |
| m9Api.ts | 9 | his-operations, his-auth |
| patientApi.ts | 5 | his-patient |
| authApi.ts | 2 | his-auth |

---

## 5. 统一响应格式

所有后端接口返回统一的 `ApiResponse<T>` 格式：

```json
{
  "code": 200,
  "message": "success",
  "data": <T>
}
```

错误响应：

```json
{
  "code": 5004,
  "message": "invalid token",
  "data": null
}
```

### 网关错误码

| 错误码 | 含义 |
|--------|------|
| 5003 | Token 已过期 |
| 5004 | Token 无效/已注销/refresh token 不可用于 API |
| 5006 | Refresh token 过期或无效 |

---

## 6. 关键文件索引

| 类别 | 文件路径 |
|------|---------|
| HTTP 客户端 | `smart-his-frontend/src/services/http.ts` |
| Token 存储 | `smart-his-frontend/src/services/tokenStorage.ts` |
| Vite 代理 | `smart-his-frontend/vite.config.ts` |
| 网关路由 | `his-gateway/src/main/resources/application.yml` |
| JWT 过滤器 | `his-gateway/src/main/java/.../filter/JwtAuthGlobalFilter.java` |
| 链路追踪 | `his-gateway/src/main/java/.../filter/TraceIdGlobalFilter.java` |
| 安全白名单 | `his-gateway/src/main/java/.../config/SecurityProperties.java` |
| 错误处理 | `his-gateway/src/main/java/.../handler/GatewayErrorWebExceptionHandler.java` |
